const BLOCK_LIKE_TAGS = new Set([
  'P',
  'DIV',
  'H1',
  'H2',
  'H3',
  'H4',
  'H5',
  'H6',
  'UL',
  'OL',
  'LI',
  'BLOCKQUOTE',
  'PRE',
]);

const ALLOWED_TAGS = new Set([
  'P',
  'BR',
  'STRONG',
  'B',
  'EM',
  'I',
  'U',
  'S',
  'H2',
  'H3',
  'H4',
  'UL',
  'OL',
  'LI',
  'BLOCKQUOTE',
  'PRE',
  'CODE',
  'A',
  'HR',
]);

const SAFE_URL_PROTOCOLS = ['http:', 'https:', 'mailto:'];

const hasHtmlLikeMarkup = (value: string) => /<\/?[a-z][\s\S]*>/i.test(value);

const escapeHtml = (value: string) => value
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;');

const normalizeEmptyHtml = (value: string) => {
  const normalized = value
    .replace(/<p><br><\/p>/gi, '')
    .replace(/<p>(?:\s|&nbsp;)*<\/p>/gi, '')
    .trim();

  return normalized;
};

const sanitizeElement = (element: Element) => {
  const tagName = element.tagName.toUpperCase();

  if (!ALLOWED_TAGS.has(tagName)) {
    if (tagName === 'SCRIPT' || tagName === 'STYLE' || tagName === 'IFRAME' || tagName === 'OBJECT') {
      element.remove();
      return;
    }

    const parent = element.parentNode;
    if (!parent) {
      element.remove();
      return;
    }

    while (element.firstChild) {
      parent.insertBefore(element.firstChild, element);
    }
    parent.removeChild(element);
    return;
  }

  Array.from(element.attributes).forEach((attribute) => {
    const name = attribute.name.toLowerCase();
    if (tagName === 'A' && name === 'href') {
      try {
        const url = new URL(attribute.value, window.location.origin);
        if (!SAFE_URL_PROTOCOLS.includes(url.protocol)) {
          element.removeAttribute(attribute.name);
          return;
        }
      } catch {
        element.removeAttribute(attribute.name);
        return;
      }

      element.setAttribute('target', '_blank');
      element.setAttribute('rel', 'noopener noreferrer');
      return;
    }

    if (tagName === 'A' && (name === 'target' || name === 'rel')) {
      return;
    }

    element.removeAttribute(attribute.name);
  });
};

const sanitizeTree = (root: ParentNode) => {
  Array.from(root.childNodes).forEach((node) => {
    if (node.nodeType === Node.ELEMENT_NODE) {
      const element = node as Element;
      sanitizeTree(element);
      sanitizeElement(element);
      return;
    }

    if (node.nodeType === Node.COMMENT_NODE) {
      node.parentNode?.removeChild(node);
    }
  });
};

const ensureParagraphs = (container: HTMLElement) => {
  if (container.childNodes.length === 0) {
    return;
  }

  const needsWrapping = Array.from(container.childNodes).some((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      return !!node.textContent?.trim();
    }

    if (node.nodeType === Node.ELEMENT_NODE) {
      return !BLOCK_LIKE_TAGS.has((node as Element).tagName.toUpperCase()) && (node as Element).tagName.toUpperCase() !== 'HR';
    }

    return false;
  });

  if (!needsWrapping) {
    return;
  }

  const content = container.innerHTML;
  container.innerHTML = content
    .split(/\n{2,}/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean)
    .map((paragraph) => `<p>${paragraph}</p>`)
    .join('');
};

export const convertPlainTextToRichText = (text: string) => {
  const normalized = text.replace(/\r\n?/g, '\n').trim();
  if (!normalized) {
    return '';
  }

  return normalized
    .split(/\n{2,}/)
    .map((paragraph) => `<p>${escapeHtml(paragraph).replace(/\n/g, '<br>')}</p>`)
    .join('');
};

export const sanitizeRichText = (value: string) => {
  if (!value.trim()) {
    return '';
  }

  if (!hasHtmlLikeMarkup(value)) {
    return convertPlainTextToRichText(value);
  }

  if (typeof window === 'undefined') {
    return value.trim();
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(`<div>${value}</div>`, 'text/html');
  const container = doc.body.firstElementChild as HTMLElement | null;
  if (!container) {
    return '';
  }

  sanitizeTree(container);
  ensureParagraphs(container);

  return normalizeEmptyHtml(container.innerHTML);
};

export const normalizeStoredRichText = (value: string) => {
  if (!value.trim()) {
    return '';
  }

  return hasHtmlLikeMarkup(value) ? sanitizeRichText(value) : convertPlainTextToRichText(value);
};

export const richTextToPlainText = (value: string) => {
  if (!value.trim()) {
    return '';
  }

  if (!hasHtmlLikeMarkup(value)) {
    return value.replace(/\r\n?/g, '\n').trim();
  }

  if (typeof window !== 'undefined') {
    const parser = new DOMParser();
    const doc = parser.parseFromString(value, 'text/html');
    const text = (doc.body.innerText || doc.body.textContent || '')
      .replace(/\u00a0/g, ' ')
      .replace(/\r\n?/g, '\n')
      .replace(/\n{3,}/g, '\n\n')
      .trim();

    return text;
  }

  return value
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/(p|div|h1|h2|h3|h4|h5|h6|li|blockquote|pre)>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
};

export const appendRichTextContent = (currentValue: string, nextValue: string) => {
  const currentHtml = sanitizeRichText(currentValue);
  const nextHtml = sanitizeRichText(nextValue);

  if (!currentHtml) {
    return nextHtml;
  }

  if (!nextHtml) {
    return currentHtml;
  }

  return `${currentHtml}<p><br></p>${nextHtml}`;
};

export const getSafeRichTextHtml = (value: string) => normalizeStoredRichText(value);

export const isRichTextEmpty = (value: string) => richTextToPlainText(value).trim().length === 0;
