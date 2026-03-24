import React, {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState,
} from 'react';
import { Button } from '../common/Button';
import { AIActionBar } from '../ai/AIActionBar';
import type { AIActionType, AIApplyMode, AIContentScope } from '../../types';
import { convertPlainTextToRichText, richTextToPlainText, sanitizeRichText } from '../../utils/richText';
import styles from './RichTextEditor.module.css';

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  error?: string;
  disabled?: boolean;
  showGlobalAIActions?: boolean;
  showSelectionAIActions?: boolean;
  onSelectionInfoChange?: (selection: RichTextSelectionSnapshot | null) => void;
  onAIAction: (action: AIActionType, payload: { text: string; scope: AIContentScope }) => void;
}

export interface RichTextSelectionSnapshot {
  characterCount: number;
  preview: string;
}

export interface RichTextEditorHandle {
  applyAIResultToSelection: (text: string, mode: Extract<AIApplyMode, 'replace-selection' | 'insert-after-selection'>) => boolean;
  clearAISelection: () => void;
  startSelectionAIAction: (action: AIActionType) => boolean;
}

interface ToolbarButton {
  label: string;
  command: string;
  value?: string;
  icon: string;
  activeKey?: string;
}

interface SelectionInfo {
  text: string;
  characterCount: number;
  preview: string;
  range: Range;
}

const toolbarGroups: ToolbarButton[][] = [
  [
    { label: '加粗', command: 'bold', icon: 'B', activeKey: 'bold' },
    { label: '斜体', command: 'italic', icon: 'I', activeKey: 'italic' },
    { label: '下划线', command: 'underline', icon: 'U', activeKey: 'underline' },
  ],
  [
    { label: '正文', command: 'formatBlock', value: '<p>', icon: 'P', activeKey: 'paragraph' },
    { label: '标题二', command: 'formatBlock', value: '<h2>', icon: 'H2', activeKey: 'h2' },
    { label: '标题三', command: 'formatBlock', value: '<h3>', icon: 'H3', activeKey: 'h3' },
    { label: '引用', command: 'formatBlock', value: '<blockquote>', icon: '"', activeKey: 'blockquote' },
  ],
  [
    { label: '无序列表', command: 'insertUnorderedList', icon: '•', activeKey: 'unorderedList' },
    { label: '有序列表', command: 'insertOrderedList', icon: '1.', activeKey: 'orderedList' },
    { label: '代码块', command: 'formatBlock', value: '<pre>', icon: '</>', activeKey: 'pre' },
    { label: '分隔线', command: 'insertHorizontalRule', icon: '—' },
  ],
];

const selectionPreviewLimit = 110;

const queryActiveState = (editor: HTMLDivElement) => {
  const selection = window.getSelection();
  const anchorNode = selection?.anchorNode;
  if (!selection || !anchorNode || !editor.contains(anchorNode)) {
    return {};
  }

  const anchorElement = anchorNode.nodeType === Node.ELEMENT_NODE
    ? anchorNode as HTMLElement
    : anchorNode.parentElement;

  return {
    bold: document.queryCommandState('bold'),
    italic: document.queryCommandState('italic'),
    underline: document.queryCommandState('underline'),
    unorderedList: document.queryCommandState('insertUnorderedList'),
    orderedList: document.queryCommandState('insertOrderedList'),
    h2: !!anchorElement?.closest('h2'),
    h3: !!anchorElement?.closest('h3'),
    blockquote: !!anchorElement?.closest('blockquote'),
    pre: !!anchorElement?.closest('pre'),
    paragraph: !!anchorElement?.closest('p'),
  };
};

const selectionBelongsToEditor = (editor: HTMLDivElement, selection: Selection) => {
  const anchorNode = selection.anchorNode;
  const focusNode = selection.focusNode;

  return Boolean(anchorNode && focusNode && editor.contains(anchorNode) && editor.contains(focusNode));
};

const normalizeSelectionPreview = (text: string) => {
  const condensed = text.replace(/\s+/g, ' ').trim();
  if (condensed.length <= selectionPreviewLimit) {
    return condensed;
  }

  return `${condensed.slice(0, selectionPreviewLimit).trim()}...`;
};

const getSelectionInfo = (editor: HTMLDivElement): SelectionInfo | null => {
  const selection = window.getSelection();
  if (!selection || selection.rangeCount === 0 || selection.isCollapsed || !selectionBelongsToEditor(editor, selection)) {
    return null;
  }

  const range = selection.getRangeAt(0).cloneRange();
  const text = selection.toString().replace(/\u00a0/g, ' ').trim();
  if (!text) {
    return null;
  }

  return {
    text,
    characterCount: text.replace(/\s/g, '').length,
    preview: normalizeSelectionPreview(text),
    range,
  };
};

export const RichTextEditor = forwardRef<RichTextEditorHandle, RichTextEditorProps>(({
  value,
  onChange,
  placeholder = '开始书写，让段落、呼吸和重点都落进同一张写作桌面。',
  error,
  disabled = false,
  showGlobalAIActions = true,
  showSelectionAIActions = true,
  onSelectionInfoChange,
  onAIAction,
}, ref) => {
  const editorRef = useRef<HTMLDivElement | null>(null);
  const liveSelectionRef = useRef<SelectionInfo | null>(null);
  const aiSelectionRangeRef = useRef<Range | null>(null);

  const [activeState, setActiveState] = useState<Partial<Record<string, boolean>>>({});
  const [selectionInfo, setSelectionInfo] = useState<Omit<SelectionInfo, 'range'> | null>(null);

  const plainText = useMemo(() => richTextToPlainText(value), [value]);
  const wordCount = useMemo(() => {
    const words = plainText.trim().split(/\s+/).filter(Boolean);
    return words.length;
  }, [plainText]);
  const characterCount = plainText.replace(/\s/g, '').length;
  const readingMinutes = Math.max(1, Math.ceil(characterCount / 450));

  const clearVisibleSelection = useCallback(() => {
    liveSelectionRef.current = null;
    setSelectionInfo(null);
    onSelectionInfoChange?.(null);
  }, [onSelectionInfoChange]);

  const syncSelectionState = useCallback(() => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }

    setActiveState(queryActiveState(editor));

    const nextSelection = disabled ? null : getSelectionInfo(editor);
    liveSelectionRef.current = nextSelection;

    if (!nextSelection) {
      setSelectionInfo(null);
      onSelectionInfoChange?.(null);
      return;
    }

    const nextSnapshot = {
      text: nextSelection.text,
      characterCount: nextSelection.characterCount,
      preview: nextSelection.preview,
    };
    setSelectionInfo(nextSnapshot);
    onSelectionInfoChange?.({
      characterCount: nextSnapshot.characterCount,
      preview: nextSnapshot.preview,
    });
  }, [disabled, onSelectionInfoChange]);

  const emitChange = useCallback(() => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }

    const nextValue = sanitizeRichText(editor.innerHTML);
    if (editor.innerHTML !== nextValue) {
      editor.innerHTML = nextValue;
    }

    onChange(nextValue);
    syncSelectionState();
  }, [onChange, syncSelectionState]);

  const clearAISelection = useCallback(() => {
    aiSelectionRangeRef.current = null;
    clearVisibleSelection();
    window.getSelection()?.removeAllRanges();
  }, [clearVisibleSelection]);

  const applyAIResultToSelection = useCallback((
    text: string,
    mode: Extract<AIApplyMode, 'replace-selection' | 'insert-after-selection'>,
  ) => {
    const editor = editorRef.current;
    const storedRange = aiSelectionRangeRef.current;
    if (!editor || !storedRange || disabled) {
      return false;
    }

    const nextHtml = convertPlainTextToRichText(text.trim());
    if (!nextHtml) {
      return false;
    }

    const selection = window.getSelection();
    if (!selection) {
      return false;
    }

    const nextRange = storedRange.cloneRange();
    if (mode === 'insert-after-selection') {
      nextRange.collapse(false);
    }

    editor.focus();
    selection.removeAllRanges();
    selection.addRange(nextRange);
    document.execCommand('insertHTML', false, nextHtml);

    aiSelectionRangeRef.current = null;
    clearVisibleSelection();
    emitChange();
    return true;
  }, [clearVisibleSelection, disabled, emitChange]);

  const startSelectionAIAction = useCallback((action: AIActionType) => {
    if (disabled) {
      return false;
    }

    const editor = editorRef.current;
    const currentSelection = liveSelectionRef.current ?? (editor ? getSelectionInfo(editor) : null);
    if (!currentSelection) {
      return false;
    }

    liveSelectionRef.current = currentSelection;
    aiSelectionRangeRef.current = currentSelection.range.cloneRange();
    onAIAction(action, {
      text: currentSelection.text,
      scope: 'selection',
    });
    return true;
  }, [disabled, onAIAction]);

  useImperativeHandle(ref, () => ({
    applyAIResultToSelection,
    clearAISelection,
    startSelectionAIAction,
  }), [applyAIResultToSelection, clearAISelection, startSelectionAIAction]);

  useEffect(() => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }

    if (editor.innerHTML !== value) {
      editor.innerHTML = value;
    }

    aiSelectionRangeRef.current = null;
    clearVisibleSelection();
    setActiveState(queryActiveState(editor));
  }, [clearVisibleSelection, value]);

  useEffect(() => {
    document.addEventListener('selectionchange', syncSelectionState);
    return () => document.removeEventListener('selectionchange', syncSelectionState);
  }, [syncSelectionState]);

  const applyCommand = (command: string, commandValue?: string) => {
    const editor = editorRef.current;
    if (!editor || disabled) {
      return;
    }

    editor.focus();
    document.execCommand(command, false, commandValue);
    emitChange();
  };

  const handlePaste = (event: React.ClipboardEvent<HTMLDivElement>) => {
    event.preventDefault();

    const text = event.clipboardData.getData('text/plain');
    const html = convertPlainTextToRichText(text);

    document.execCommand('insertHTML', false, html);
    emitChange();
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Tab') {
      event.preventDefault();
      const editor = editorRef.current;
      if (!editor || disabled) {
        return;
      }

      editor.focus();
      document.execCommand('insertHTML', false, '<span data-indent="true">&nbsp;&nbsp;&nbsp;&nbsp;</span>');
      emitChange();
      return;
    }

    if (!(event.metaKey || event.ctrlKey)) {
      return;
    }

    const shortcut = event.key.toLowerCase();
    if (shortcut === 'b') {
      event.preventDefault();
      applyCommand('bold');
    } else if (shortcut === 'i') {
      event.preventDefault();
      applyCommand('italic');
    } else if (shortcut === 'u') {
      event.preventDefault();
      applyCommand('underline');
    }
  };

  const handleSelectionAIAction = (action: AIActionType) => {
    startSelectionAIAction(action);
  };

  return (
    <section className={styles.shell}>
      <div className={styles.head}>
        <div className={styles['head-copy']}>
          <span className={styles.eyebrow}>Content Editor</span>
          <h2 className={styles.title}>正文</h2>
          <p className={styles.subtitle}>保留你的写作节奏，同时支持富文本排版与 AI 协同改写。</p>
        </div>
        <div className={styles.metrics}>
          <div className={styles.metric}>
            <span className={styles['metric-value']}>{characterCount}</span>
            <span className={styles['metric-label']}>字数</span>
          </div>
          <div className={styles.metric}>
            <span className={styles['metric-value']}>{wordCount}</span>
            <span className={styles['metric-label']}>词数</span>
          </div>
          <div className={styles.metric}>
            <span className={styles['metric-value']}>{readingMinutes} 分钟</span>
            <span className={styles['metric-label']}>阅读时长</span>
          </div>
        </div>
      </div>

      <div className={styles.toolbox}>
        <div className={styles.toolbar}>
          {toolbarGroups.map((group, index) => (
            <div key={index} className={styles.group}>
              {group.map((item) => {
                const isActive = Boolean(item.activeKey && activeState[item.activeKey]);
                return (
                  <button
                    key={`${item.command}-${item.label}`}
                    type="button"
                    className={`${styles.tool} ${isActive ? styles['tool-active'] : ''}`}
                    onClick={() => applyCommand(item.command, item.value)}
                    disabled={disabled}
                    title={item.label}
                    aria-pressed={isActive}
                  >
                    <span className={styles['tool-icon']}>{item.icon}</span>
                    <span className={styles['tool-label']}>{item.label}</span>
                  </button>
                );
              })}
            </div>
          ))}

          <div className={styles['toolbar-actions']}>
            <Button
              type="button"
              size="small"
              variant="ghost"
              className={styles['clear-button']}
              onClick={() => applyCommand('removeFormat')}
              disabled={disabled}
            >
              清除样式
            </Button>
          </div>
        </div>

        {showGlobalAIActions && (
          <AIActionBar
            target="content"
            label="全文 AI"
            disabled={disabled}
            onAction={(_, action) => onAIAction(action, { text: plainText.trim(), scope: 'full' })}
          />
        )}
      </div>

      {!disabled && showSelectionAIActions && (
        <div className={styles['selection-shell']}>
          {selectionInfo ? (
            <>
              <div className={styles['selection-head']}>
                <div>
                  <span className={styles['selection-badge']}>局部 AI</span>
                  <p className={styles['selection-hint']}>
                    已选中 {selectionInfo.characterCount} 个字，这次 AI 只会处理这一段内容。
                  </p>
                </div>
                <span className={styles['selection-count']}>已选择片段</span>
              </div>

              <p className={styles['selection-preview']}>“{selectionInfo.preview}”</p>

              <AIActionBar
                target="content"
                label="选中片段"
                disabled={disabled}
                preserveSelection
                onAction={(_, action) => handleSelectionAIAction(action)}
              />
            </>
          ) : (
            <div className={styles['selection-empty']}>
              <span className={styles['selection-badge']}>局部 AI</span>
              <p className={styles['selection-hint']}>
                在正文中选中任意一段文字后，可单独对该片段执行润色、续写或摘要。
              </p>
            </div>
          )}
        </div>
      )}

      <div className={`${styles.editorFrame} ${error ? styles['editorFrame-error'] : ''}`}>
        <div className={styles.ruler} aria-hidden="true">
          {Array.from({ length: 12 }).map((_, index) => (
            <span key={index} className={styles.tick} />
          ))}
        </div>

        <div
          ref={editorRef}
          className={styles.editor}
          contentEditable={!disabled}
          suppressContentEditableWarning
          data-placeholder={placeholder}
          onInput={emitChange}
          onBlur={emitChange}
          onKeyUp={emitChange}
          onMouseUp={emitChange}
          onPaste={handlePaste}
          onKeyDown={handleKeyDown}
          role="textbox"
          aria-multiline="true"
        />
      </div>

      <div className={styles.footer}>
        <span className={styles.hint}>快捷键：`Ctrl/Cmd + B / I / U`，`Tab` 缩进，支持全文 AI 与选中片段 AI。</span>
        {error && <span className={styles.error}>{error}</span>}
      </div>
    </section>
  );
});

RichTextEditor.displayName = 'RichTextEditor';
