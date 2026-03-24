import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { articleDraft, articleInsert, articlePublic, articleQueryOne, articleUpdate } from '../../api/article';
import { adminArticleQueryOne, adminArticleUpdate } from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { categoryQuery } from '../../api/category';
import { AIActionBar } from '../../components/ai/AIActionBar';
import { AIResultModal } from '../../components/ai/AIResultModal';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { Loading } from '../../components/common/Loading';
import { Container } from '../../components/layout/Container';
import { PageLayout } from '../../components/layout/PageLayout/PageLayout';
import {
  RichTextEditor,
  type RichTextEditorHandle,
  type RichTextSelectionSnapshot,
} from '../../components/editor/RichTextEditor';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { useAIWriter } from '../../hooks/useAIWriter';
import type { AIActionType, AIApplyMode, AIContentScope, AIFieldTarget, Category, Id } from '../../types';
import { ARTICLE_STATUS, ARTICLE_STATUS_TEXT, PAGINATION, ROUTES } from '../../utils/constants';
import {
  appendRichTextContent,
  convertPlainTextToRichText,
  isRichTextEmpty,
  normalizeStoredRichText,
  richTextToPlainText,
  sanitizeRichText,
} from '../../utils/richText';
import styles from './ArticleEditor.module.css';

const getEditorHeading = (isAdminMode: boolean, isEditing: boolean) => {
  if (isAdminMode) {
    return '管理员编辑文章';
  }

  return isEditing ? '编辑文章' : '写新文章';
};

const getStatusLabel = (status: number) => {
  if (status === ARTICLE_STATUS.PUBLISHED) {
    return ARTICLE_STATUS_TEXT[ARTICLE_STATUS.PUBLISHED];
  }

  if (status === ARTICLE_STATUS.OFFLINE) {
    return ARTICLE_STATUS_TEXT[ARTICLE_STATUS.OFFLINE];
  }

  return ARTICLE_STATUS_TEXT[ARTICLE_STATUS.DRAFT];
};

interface ContentAIOptions {
  scope?: AIContentScope;
  text?: string;
}

const ArticleEditor: React.FC = () => {
  const { id } = useParams<{ id?: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { success, error } = useToast();

  const isEditing = Boolean(id);
  const isAdminMode = location.pathname.startsWith(`${ROUTES.ADMIN_PREFIX}/`);
  const editorRef = useRef<RichTextEditorHandle | null>(null);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState<Id | ''>('');
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(isEditing);
  const [titleError, setTitleError] = useState('');
  const [contentError, setContentError] = useState('');
  const [categoryError, setCategoryError] = useState('');
  const [categoryLoadError, setCategoryLoadError] = useState('');
  const [currentStatus, setCurrentStatus] = useState<number>(ARTICLE_STATUS.DRAFT);
  const [selectionSnapshot, setSelectionSnapshot] = useState<RichTextSelectionSnapshot | null>(null);

  const {
    state: aiState,
    openWriter,
    retry: retryAI,
    stop: stopAI,
    close: closeAI,
  } = useAIWriter();

  const plainContent = useMemo(() => richTextToPlainText(content), [content]);
  const paragraphCount = useMemo(
    () => plainContent.split(/\n{2,}/).filter(Boolean).length,
    [plainContent],
  );
  const contentCharacterCount = useMemo(
    () => plainContent.replace(/\s/g, '').length,
    [plainContent],
  );
  const readingMinutes = useMemo(
    () => Math.max(1, Math.ceil(contentCharacterCount / 450)),
    [contentCharacterCount],
  );
  const titleLength = title.trim().length;

  useEffect(() => {
    categoryQuery({ pageNum: PAGINATION.DEFAULT_PAGE, pageSize: PAGINATION.LARGE_PAGE_SIZE })
      .then((response) => {
        const records = response.data.data?.records ?? [];
        setCategories(records);
        setCategoryLoadError(records.length === 0 ? '暂无分类，请先添加分类。' : '');
      })
      .catch((err: unknown) => {
        setCategories([]);
        setCategoryLoadError(getErrorMessage(err, '分类加载失败，请稍后重试。'));
      });
  }, []);

  useEffect(() => {
    if (!isEditing || !id) {
      return;
    }

    const request = isAdminMode ? adminArticleQueryOne(id) : articleQueryOne(id);

    request
      .then((response) => {
        const article = response.data.data!;
        setTitle(article.title);
        setContent(normalizeStoredRichText(article.content));
        setCategoryId(article.categoryId ?? '');
        setCurrentStatus(article.status);
      })
      .catch((err: unknown) => {
        error(getErrorMessage(err, '文章加载失败。'));
        navigate(isAdminMode ? ROUTES.ADMIN_ARTICLES : ROUTES.HOME);
      })
      .finally(() => setInitializing(false));
  }, [error, id, isAdminMode, isEditing, navigate]);

  const getValidatedCategoryId = () => {
    if (!categoryId) {
      setCategoryError('请选择分类');
      return null;
    }

    setCategoryError('');
    return categoryId;
  };

  const validate = () => {
    let valid = true;

    if (!title.trim()) {
      setTitleError('标题不能为空');
      valid = false;
    } else if (title.trim().length > 200) {
      setTitleError('标题不能超过 200 个字符');
      valid = false;
    } else {
      setTitleError('');
    }

    if (isRichTextEmpty(content)) {
      setContentError('正文不能为空');
      valid = false;
    } else {
      setContentError('');
    }

    if (!getValidatedCategoryId()) {
      valid = false;
    }

    return valid;
  };

  const navigateAfterSave = (status: number) => {
    if (isAdminMode) {
      navigate(ROUTES.ADMIN_ARTICLES);
      return;
    }

    navigate(`${ROUTES.PROFILE_ARTICLES}?status=${status}`);
  };

  const handleSave = async (publish: boolean) => {
    if (!validate()) {
      return;
    }

    if (!isAdminMode && !user?.id) {
      return;
    }

    const validCategoryId = getValidatedCategoryId();
    if (!validCategoryId) {
      return;
    }

    const normalizedContent = sanitizeRichText(content);

    setLoading(true);
    try {
      if (isAdminMode) {
        if (!isEditing || !id) {
          throw new Error('管理员暂不支持新建文章。');
        }

        await adminArticleUpdate({
          id,
          authorId: user?.id ?? '',
          title: title.trim(),
          content: normalizedContent,
          categoryId: validCategoryId,
        });

        success('文章已保存。');
        navigateAfterSave(currentStatus);
        return;
      }

      if (isEditing && id && user?.id) {
        await articleUpdate({
          id,
          authorId: user.id,
          title: title.trim(),
          content: normalizedContent,
          categoryId: validCategoryId,
        });

        const nextStatus = publish ? ARTICLE_STATUS.PUBLISHED : currentStatus;
        if (publish && currentStatus !== ARTICLE_STATUS.PUBLISHED) {
          await articlePublic({ id, authorId: user.id });
        }

        setCurrentStatus(nextStatus);
        success('保存成功');
        navigateAfterSave(nextStatus);
      } else {
        await articleInsert({
          title: title.trim(),
          content: normalizedContent,
          categoryId: validCategoryId,
          status: publish ? ARTICLE_STATUS.PUBLISHED : ARTICLE_STATUS.DRAFT,
        });

        success(publish ? '发布成功' : '草稿已保存。');
        navigateAfterSave(publish ? ARTICLE_STATUS.PUBLISHED : ARTICLE_STATUS.DRAFT);
      }
    } catch (err: unknown) {
      error(getErrorMessage(err, '保存失败。'));
    } finally {
      setLoading(false);
    }
  };

  const handleSetDraft = async () => {
    if (!isEditing || !id || !user?.id || isAdminMode) {
      return;
    }

    try {
      await articleDraft({ id, authorId: user.id });
      setCurrentStatus(ARTICLE_STATUS.DRAFT);
      success('文章已转为草稿。');
      navigateAfterSave(ARTICLE_STATUS.DRAFT);
    } catch (err: unknown) {
      error(getErrorMessage(err, '操作失败。'));
    }
  };

  const handleOpenAIWriter = (target: AIFieldTarget, action: AIActionType, options?: ContentAIOptions) => {
    const scope = target === 'content' ? options?.scope ?? 'full' : 'full';
    const sourceText = (options?.text ?? (target === 'title' ? title : plainContent)).trim();

    if (!sourceText) {
      if (target === 'title') {
        error('请先输入标题后再使用 AI。');
      } else if (scope === 'selection') {
        error('请先选中一段正文，再单独交给 AI 处理。');
      } else {
        error('请先输入正文后再使用 AI。');
      }
      return;
    }

    openWriter({
      target,
      action,
      text: sourceText,
      scope,
    });
  };

  const handleSelectionAIFromSidebar = (_: AIFieldTarget, action: AIActionType) => {
    const triggered = editorRef.current?.startSelectionAIAction(action);
    if (!triggered) {
      error('请先在正文中选中一段文字，再使用局部 AI。');
    }
  };

  const handleCloseAI = () => {
    const shouldClearSelection = aiState.target === 'content' && aiState.scope === 'selection';

    closeAI();

    if (shouldClearSelection) {
      editorRef.current?.clearAISelection();
    }
  };

  const handleApplyAI = (mode: AIApplyMode) => {
    const nextText = aiState.resultText.trim();

    if (!nextText || !aiState.target) {
      return;
    }

    if (aiState.target === 'title') {
      setTitle(nextText);
      setTitleError('');
      success('AI 结果已应用到标题。');
      closeAI();
      return;
    }

    if (aiState.scope === 'selection') {
      const selectionMode = mode === 'replace-selection' || mode === 'insert-after-selection'
        ? mode
        : aiState.action === 'continue'
          ? 'insert-after-selection'
          : 'replace-selection';

      const applied = editorRef.current?.applyAIResultToSelection(nextText, selectionMode);
      if (!applied) {
        error('没有找到刚才选中的正文片段，请重新选中后再试。');
        return;
      }

      setContentError('');
      success(
        selectionMode === 'insert-after-selection'
          ? 'AI 结果已插入到选中文本之后。'
          : 'AI 结果已替换选中的正文片段。',
      );
      closeAI();
      return;
    }

    const nextHtml = convertPlainTextToRichText(nextText);
    if (mode === 'append') {
      setContent((previous) => appendRichTextContent(previous, nextHtml));
      success('AI 结果已追加到正文末尾。');
    } else {
      setContent(nextHtml);
      success('AI 结果已替换正文内容。');
    }

    setContentError('');
    closeAI();
  };

  if (initializing) {
    return <PageLayout><Loading fullscreen /></PageLayout>;
  }

  return (
    <PageLayout>
      <Container>
        <div className={styles.editor}>
          <section className={styles.header}>
            <div className={styles['header-main']}>
              <span className={styles.kicker}>{isAdminMode ? 'Admin Workspace' : 'Writing Workspace'}</span>
              <h1 className={styles.title}>{getEditorHeading(isAdminMode, isEditing)}</h1>
              <p className={styles.subtitle}>
                聚焦标题、正文和发布设置三步完成创作。AI 可用于全文或选中片段，应用前会先在弹窗中预览。
              </p>
            </div>

            <div className={styles['header-stats']}>
              <div className={styles['status-pill']}>
                {isAdminMode ? '后台编辑模式' : `当前状态：${getStatusLabel(currentStatus)}`}
              </div>
              <div className={styles['quick-metrics']}>
                <div className={styles['quick-item']}>
                  <span className={styles['quick-label']}>标题字数</span>
                  <span className={styles['quick-value']}>{titleLength}</span>
                </div>
                <div className={styles['quick-item']}>
                  <span className={styles['quick-label']}>正文字符</span>
                  <span className={styles['quick-value']}>{contentCharacterCount}</span>
                </div>
                <div className={styles['quick-item']}>
                  <span className={styles['quick-label']}>预计阅读</span>
                  <span className={styles['quick-value']}>{readingMinutes} 分钟</span>
                </div>
              </div>
            </div>

            <div className={styles['header-actions']}>
              {!isAdminMode && isEditing && currentStatus === ARTICLE_STATUS.PUBLISHED && (
                <Button variant="outline" size="small" onClick={handleSetDraft}>
                  转为草稿
                </Button>
              )}

              {!isAdminMode && (
                <Button variant="outline" size="small" loading={loading} onClick={() => handleSave(false)}>
                  保存草稿
                </Button>
              )}

              <Button size="small" loading={loading} onClick={() => handleSave(!isAdminMode)}>
                {isAdminMode ? '保存修改' : isEditing ? '保存并发布' : '立即发布'}
              </Button>
            </div>
          </section>

          <div className={styles.workspace}>
            <div className={styles.main}>
              <section className={styles['title-panel']}>
                <div className={styles['section-head']}>
                  <div className={styles['section-copy']}>
                    <span className={styles['section-label']}>标题</span>
                    <p className={styles['section-hint']}>先确定标题方向，再用 AI 快速打磨表达。</p>
                  </div>
                  <span className={styles['title-length']}>{titleLength}/200</span>
                </div>

                <Input
                  placeholder="例如：当一个系统开始有“写作感”，它就不再只是后台"
                  value={title}
                  onChange={(event) => {
                    setTitle((event.target as HTMLInputElement).value);
                    setTitleError('');
                  }}
                  error={titleError}
                  className={styles['title-input']}
                />

                <AIActionBar
                  target="title"
                  label="标题 AI"
                  disabled={loading}
                  onAction={handleOpenAIWriter}
                />
              </section>

              <RichTextEditor
                ref={editorRef}
                value={content}
                onChange={(nextValue) => {
                  setContent(nextValue);
                  setContentError('');
                }}
                error={contentError}
                disabled={loading}
                showSelectionAIActions={false}
                onSelectionInfoChange={setSelectionSnapshot}
                onAIAction={(action, payload) => handleOpenAIWriter('content', action, payload)}
              />
            </div>

            <aside className={styles.sidebar}>
              <section className={`${styles.card} ${styles['ai-card']}`}>
                <div className={styles['card-head-inline']}>
                  <span className={styles['card-label']}>局部 AI</span>
                  <span className={styles['side-tag']}>右侧常驻</span>
                </div>
                <p className={styles['card-copy']}>
                  在正文里先选中需要处理的片段，再在这里执行润色、续写或摘要。
                </p>
                {selectionSnapshot ? (
                  <div className={styles['selection-preview-card']}>
                    <div className={styles['selection-row']}>
                      <span className={styles['selection-title']}>当前选区</span>
                      <span className={styles['selection-count']}>{selectionSnapshot.characterCount} 字</span>
                    </div>
                    <p className={styles['selection-preview']}>“{selectionSnapshot.preview}”</p>
                  </div>
                ) : (
                  <div className={styles['selection-empty-tip']}>
                    还没有选中文本。请在正文中拖拽选中一段内容后再操作。
                  </div>
                )}
                <AIActionBar
                  target="content"
                  label="选中片段"
                  preserveSelection
                  disabled={loading}
                  onAction={handleSelectionAIFromSidebar}
                />
              </section>

              <section className={styles.card}>
                <span className={styles['card-label']}>发布设置</span>
                <p className={styles['card-copy']}>
                  选择分类后可直接保存草稿或发布，正文与标题会按当前内容一起提交。
                </p>
                <div className={styles['status-row']}>
                  <span className={styles['status-name']}>文章状态</span>
                  <span className={styles['status-value']}>{getStatusLabel(currentStatus)}</span>
                </div>
                <label className={styles['field-label']} htmlFor="editor-category">
                  文章分类
                </label>
                <select
                  id="editor-category"
                  className={`${styles.select} ${categoryError ? styles['select-error'] : ''}`}
                  value={categoryId}
                  onChange={(event) => {
                    setCategoryId(event.target.value);
                    setCategoryError('');
                  }}
                  disabled={categories.length === 0}
                >
                  <option value="">请选择分类</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>{category.name}</option>
                  ))}
                </select>
                {categoryError && <div className={styles['error-msg']}>{categoryError}</div>}
                {categoryLoadError && <div className={styles['error-msg']}>{categoryLoadError}</div>}
              </section>

              <section className={styles.card}>
                <span className={styles['card-label']}>写作概览</span>
                <div className={styles['card-metrics']}>
                  <div className={styles.metric}>
                    <span className={styles['metric-value']}>{titleLength}</span>
                    <span className={styles['metric-name']}>标题字数</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles['metric-value']}>{contentCharacterCount}</span>
                    <span className={styles['metric-name']}>正文字符</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles['metric-value']}>{paragraphCount}</span>
                    <span className={styles['metric-name']}>段落数量</span>
                  </div>
                  <div className={styles.metric}>
                    <span className={styles['metric-value']}>{readingMinutes} 分钟</span>
                    <span className={styles['metric-name']}>阅读时长</span>
                  </div>
                </div>
              </section>

              <section className={styles.card}>
                <span className={styles['card-label']}>操作建议</span>
                <ul className={styles.tips}>
                  <li>标题建议控制在 20-60 字，更易阅读与展示。</li>
                  <li>用“标题二 / 标题三”拆分层级，让正文结构更清晰。</li>
                  <li>选中文本后可单独使用 AI 处理该段，避免改动全文。</li>
                </ul>
              </section>
            </aside>
          </div>
        </div>
      </Container>

      <AIResultModal
        isOpen={aiState.isOpen}
        action={aiState.action}
        target={aiState.target}
        scope={aiState.scope}
        sourceText={aiState.sourceText}
        resultText={aiState.resultText}
        status={aiState.status}
        errorMessage={aiState.errorMessage}
        isStopped={aiState.isStopped}
        onClose={handleCloseAI}
        onRetry={retryAI}
        onStop={stopAI}
        onApply={handleApplyAI}
      />
    </PageLayout>
  );
};

export default ArticleEditor;
