import React from 'react';
import { Button } from '../common/Button';
import { Modal } from '../common/Modal';
import type { AIActionType, AIApplyMode, AIContentScope, AIFieldTarget, AIRequestStatus } from '../../types';
import styles from './AIResultModal.module.css';

interface AIResultModalProps {
  isOpen: boolean;
  action: AIActionType | null;
  target: AIFieldTarget | null;
  scope: AIContentScope;
  sourceText: string;
  resultText: string;
  status: AIRequestStatus;
  errorMessage: string | null;
  isStopped: boolean;
  onClose: () => void;
  onRetry: () => void;
  onStop: () => void;
  onApply: (mode: AIApplyMode) => void;
}

const actionLabels: Record<AIActionType, string> = {
  polish: '润色',
  continue: '续写',
  summarize: '摘要',
};

const actionDescriptions: Record<AIActionType, string> = {
  polish: '保留原意，优化语言节奏、表达质感和整体可读性。',
  continue: '延续当前语气和逻辑，把内容自然推向下一段。',
  summarize: '抽取核心信息，收束成更凝练、更便于发布的版本。',
};

const statusLabels: Record<AIRequestStatus, string> = {
  idle: '等待生成',
  streaming: '正在生成',
  success: '结果已就绪',
  error: '生成失败',
};

const getPreviewText = (text: string) => {
  if (!text.trim()) {
    return '当前没有可供处理的内容。';
  }

  return text;
};

const getApplyLabel = (mode: AIApplyMode) => {
  switch (mode) {
    case 'append':
      return '追加到正文末尾';
    case 'replace-selection':
      return '替换选中文本';
    case 'insert-after-selection':
      return '插入到选区后';
    default:
      return '替换正文内容';
  }
};

export const AIResultModal: React.FC<AIResultModalProps> = ({
  isOpen,
  action,
  target,
  scope,
  sourceText,
  resultText,
  status,
  errorMessage,
  isStopped,
  onClose,
  onRetry,
  onStop,
  onApply,
}) => {
  if (!isOpen || !action || !target) {
    return null;
  }

  const isSelection = target === 'content' && scope === 'selection';
  const canApply = status === 'success' && resultText.trim().length > 0;
  const isStreaming = status === 'streaming';

  const primaryMode: AIApplyMode = target === 'title'
    ? 'replace'
    : isSelection
      ? action === 'continue'
        ? 'insert-after-selection'
        : 'replace-selection'
      : action === 'continue'
        ? 'append'
        : 'replace';

  const secondaryMode: AIApplyMode | null = target === 'title'
    ? null
    : isSelection
      ? primaryMode === 'insert-after-selection'
        ? 'replace-selection'
        : 'insert-after-selection'
      : primaryMode === 'append'
        ? 'replace'
        : 'append';

  const modalTitle = target === 'title'
    ? `AI ${actionLabels[action]}标题`
    : isSelection
      ? `AI ${actionLabels[action]}选中片段`
      : `AI ${actionLabels[action]}正文`;

  const sourceMeta = target === 'title'
    ? '基于当前标题生成'
    : isSelection
      ? '只基于当前选中的正文片段生成'
      : '基于当前整段正文生成';

  const eyebrowLabel = target === 'title'
    ? '标题工作台'
    : isSelection
      ? '正文局部改写'
      : '正文写作工作台';

  const applyLabel = target === 'title' ? '应用到标题' : getApplyLabel(primaryMode);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={modalTitle}
      size="large"
      footer={(
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {canApply ? '关闭' : '取消'}
          </Button>

          {status === 'error' && (
            <Button type="button" variant="outline" onClick={onRetry}>
              重新生成
            </Button>
          )}

          {isStreaming && (
            <Button type="button" variant="outline" onClick={onStop}>
              停止生成
            </Button>
          )}

          {secondaryMode && canApply && (
            <Button type="button" variant="outline" onClick={() => onApply(secondaryMode)}>
              {getApplyLabel(secondaryMode)}
            </Button>
          )}

          <Button
            type="button"
            onClick={() => onApply(primaryMode)}
            disabled={!canApply}
          >
            {applyLabel}
          </Button>
        </>
      )}
    >
      <div className={styles.shell}>
        <div className={styles.hero}>
          <div className={styles.eyebrow}>
            <span className={styles['eyebrow-label']}>{eyebrowLabel}</span>
            <span className={`${styles.status} ${styles[`status-${status}`]}`}>
              {statusLabels[status]}
            </span>
          </div>
          <h3 className={styles.title}>AI {actionLabels[action]}</h3>
          <p className={styles.subtitle}>{actionDescriptions[action]}</p>
          {isSelection && (
            <p className={styles.tip}>这次生成只围绕你当前选中的内容，不会拿整篇正文去改写。</p>
          )}
          {isStopped && status === 'success' && (
            <p className={styles.tip}>你已手动停止生成，当前片段仍然可以直接应用。</p>
          )}
        </div>

        <div className={styles.grid}>
          <section className={styles.card}>
            <div className={styles['card-header']}>
              <span className={styles['card-label']}>原始内容</span>
              <span className={styles['card-meta']}>{sourceMeta}</span>
            </div>
            <div className={styles['source-content']}>
              {getPreviewText(sourceText)}
            </div>
          </section>

          <section className={`${styles.card} ${styles['result-card']}`}>
            <div className={styles['card-header']}>
              <span className={styles['card-label']}>AI 结果</span>
              {isStreaming && <span className={styles['stream-indicator']}>流式输出中</span>}
            </div>

            <div className={styles['result-content']} aria-live="polite">
              {resultText || (isStreaming ? 'AI 正在组织文字，请稍候…' : '结果会显示在这里。')}
            </div>

            {errorMessage && status === 'error' && (
              <div className={styles.error}>
                {errorMessage}
              </div>
            )}
          </section>
        </div>
      </div>
    </Modal>
  );
};
