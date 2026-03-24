import type { AIActionType } from '../types';
import { getApiBaseUrl, getAuthToken } from './client';

export interface StreamAIGenerateParams {
  type: AIActionType;
  text: string;
  signal?: AbortSignal;
  onChunk?: (chunk: string) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
}

interface SSEEventPayload {
  event: string;
  data: string;
}

const parseErrorPayload = (payload: string, fallback: string) => {
  try {
    const parsed = JSON.parse(payload) as { msg?: string };
    if (parsed?.msg) {
      return parsed.msg;
    }
  } catch {
    return payload || fallback;
  }

  return payload || fallback;
};

const parseSSEEvent = (block: string): SSEEventPayload | null => {
  const lines = block
    .split('\n')
    .map((line) => line.trimEnd())
    .filter(Boolean);

  if (lines.length === 0) {
    return null;
  }

  let event = 'message';
  const dataLines: string[] = [];

  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim() || 'message';
      return;
    }

    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart());
    }
  });

  return {
    event,
    data: dataLines.join('\n'),
  };
};

const processSSEBuffer = (
  buffer: string,
  onEvent: (event: SSEEventPayload) => void
) => {
  let normalized = buffer.replace(/\r/g, '');
  let boundaryIndex = normalized.indexOf('\n\n');

  while (boundaryIndex !== -1) {
    const block = normalized.slice(0, boundaryIndex);
    const parsed = parseSSEEvent(block);
    if (parsed) {
      onEvent(parsed);
    }

    normalized = normalized.slice(boundaryIndex + 2);
    boundaryIndex = normalized.indexOf('\n\n');
  }

  return normalized;
};

export const streamAIGenerate = async ({
  type,
  text,
  signal,
  onChunk,
  onDone,
  onError,
}: StreamAIGenerateParams) => {
  const formData = new URLSearchParams({
    type,
    text,
  });

  const headers = new Headers({
    'Accept': 'text/event-stream',
    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
  });

  const token = getAuthToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${getApiBaseUrl()}/api/ai/generate`, {
    method: 'POST',
    headers,
    body: formData.toString(),
    signal,
  });

  if (!response.ok) {
    const payload = await response.text();
    throw new Error(parseErrorPayload(payload, 'AI 请求失败，请稍后重试'));
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (contentType.includes('application/json')) {
    const payload = await response.text();
    throw new Error(parseErrorPayload(payload, 'AI 请求失败，请稍后重试'));
  }

  if (!response.body) {
    throw new Error('AI 响应为空，请稍后重试');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';
  let doneReceived = false;

  const handleEvent = ({ event, data }: SSEEventPayload) => {
    if (event === 'message') {
      if (data) {
        onChunk?.(data);
      }
      return;
    }

    if (event === 'error') {
      const message = data || 'AI 生成失败，请稍后重试';
      onError?.(message);
      throw new Error(message);
    }

    if (event === 'done' || data === '[DONE]') {
      doneReceived = true;
      onDone?.();
    }
  };

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      buffer = processSSEBuffer(buffer, handleEvent);

      if (doneReceived) {
        break;
      }
    }

    if (!doneReceived && buffer.trim()) {
      processSSEBuffer(`${buffer}\n\n`, handleEvent);
    }

    if (!doneReceived) {
      onDone?.();
    }
  } finally {
    reader.releaseLock();
  }
};
