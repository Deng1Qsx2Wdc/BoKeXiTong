import React from 'react';
import { Button } from '../common/Button';
import type { AIActionType, AIFieldTarget } from '../../types';
import styles from './AIActionBar.module.css';

interface AIActionBarProps {
  target: AIFieldTarget;
  disabled?: boolean;
  label?: string;
  preserveSelection?: boolean;
  onAction: (target: AIFieldTarget, action: AIActionType) => void;
}

const actionLabels: Record<AIActionType, string> = {
  polish: '润色',
  continue: '续写',
  summarize: '摘要',
};

const targetLabels: Record<AIFieldTarget, string> = {
  title: '标题灵感',
  content: '正文写作',
};

const orderedActions: AIActionType[] = ['polish', 'continue', 'summarize'];

export const AIActionBar: React.FC<AIActionBarProps> = ({
  target,
  disabled = false,
  label,
  preserveSelection = false,
  onAction,
}) => (
  <div className={styles.wrapper}>
    <div className={styles.badge}>{label ?? targetLabels[target]}</div>
    <div className={styles.actions}>
      {orderedActions.map((action) => (
        <Button
          key={action}
          type="button"
          size="small"
          variant="outline"
          className={styles.action}
          disabled={disabled}
          aria-label={`${label ?? targetLabels[target]}：${actionLabels[action]}`}
          onMouseDown={(event) => {
            if (preserveSelection) {
              event.preventDefault();
            }
          }}
          onClick={() => onAction(target, action)}
        >
          AI {actionLabels[action]}
        </Button>
      ))}
    </div>
  </div>
);
