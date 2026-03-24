import { useCallback, useEffect, useRef, useState } from 'react';
import { streamAIGenerate } from '../api/ai';
import type { AIActionType, AIContentScope, AIFieldTarget, AIRequestStatus } from '../types';

export interface OpenAIWriterParams {
  action: AIActionType;
  target: AIFieldTarget;
  text: string;
  scope?: AIContentScope;
}

export interface AIWriterState {
  isOpen: boolean;
  action: AIActionType | null;
  target: AIFieldTarget | null;
  scope: AIContentScope;
  sourceText: string;
  resultText: string;
  status: AIRequestStatus;
  errorMessage: string | null;
  isStopped: boolean;
}

const initialState: AIWriterState = {
  isOpen: false,
  action: null,
  target: null,
  scope: 'full',
  sourceText: '',
  resultText: '',
  status: 'idle',
  errorMessage: null,
  isStopped: false,
};

export const useAIWriter = () => {
  const [state, setState] = useState<AIWriterState>(initialState);
  const requestIdRef = useRef(0);
  const abortControllerRef = useRef<AbortController | null>(null);
  const latestParamsRef = useRef<OpenAIWriterParams | null>(null);

  const runGeneration = useCallback(async (params: OpenAIWriterParams) => {
    latestParamsRef.current = params;

    requestIdRef.current += 1;
    const currentRequestId = requestIdRef.current;

    abortControllerRef.current?.abort();
    const controller = new AbortController();
    abortControllerRef.current = controller;

    setState({
      isOpen: true,
      action: params.action,
      target: params.target,
      scope: params.scope ?? 'full',
      sourceText: params.text,
      resultText: '',
      status: 'streaming',
      errorMessage: null,
      isStopped: false,
    });

    try {
      await streamAIGenerate({
        type: params.action,
        text: params.text,
        signal: controller.signal,
        onChunk: (chunk) => {
          if (requestIdRef.current !== currentRequestId) {
            return;
          }

          setState((previous) => ({
            ...previous,
            resultText: previous.resultText + chunk,
          }));
        },
      });

      if (requestIdRef.current !== currentRequestId) {
        return;
      }

      setState((previous) => ({
        ...previous,
        status: 'success',
        errorMessage: null,
      }));
    } catch (err) {
      if (requestIdRef.current !== currentRequestId) {
        return;
      }

      if (controller.signal.aborted) {
        setState((previous) => {
          const hasPartialResult = previous.resultText.trim().length > 0;
          return {
            ...previous,
            status: hasPartialResult ? 'success' : 'idle',
            errorMessage: hasPartialResult ? null : '已停止生成。',
            isStopped: true,
          };
        });
        return;
      }

      const message = err instanceof Error ? err.message : 'AI 生成失败，请稍后重试。';
      setState((previous) => ({
        ...previous,
        status: 'error',
        errorMessage: message,
        isStopped: false,
      }));
    } finally {
      if (requestIdRef.current === currentRequestId) {
        abortControllerRef.current = null;
      }
    }
  }, []);

  const openWriter = useCallback((params: OpenAIWriterParams) => {
    void runGeneration(params);
  }, [runGeneration]);

  const retry = useCallback(() => {
    if (!latestParamsRef.current) {
      return;
    }

    void runGeneration(latestParamsRef.current);
  }, [runGeneration]);

  const stop = useCallback(() => {
    abortControllerRef.current?.abort();
  }, []);

  const close = useCallback(() => {
    requestIdRef.current += 1;
    abortControllerRef.current?.abort();
    abortControllerRef.current = null;
    latestParamsRef.current = null;
    setState(initialState);
  }, []);

  useEffect(() => () => {
    abortControllerRef.current?.abort();
  }, []);

  return {
    state,
    openWriter,
    retry,
    stop,
    close,
  };
};
