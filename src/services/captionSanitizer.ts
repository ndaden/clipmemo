export const CaptionSanitizer = {
  sanitize(rawText: string): string {
    if (!rawText) return '';

    // Normalize line breaks
    let text = rawText.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

    // Remove repeated emoji trails or excess non-alphanumeric clutter if needed
    // Keep bullet points, numbers, letters, standard punctuation
    text = text
      .split('\n')
      .map(line => line.trim())
      .filter((line, index, arr) => {
        // filter out trailing blank lines
        if (line.length === 0 && (index === 0 || index === arr.length - 1)) return false;
        return true;
      })
      .join('\n');

    return text.trim();
  },

  extractHashtags(caption: string): string[] {
    const hashtagRegex = /#([A-Za-z0-9_]+)/g;
    const tags: string[] = [];
    let match;
    while ((match = hashtagRegex.exec(caption)) !== null) {
      const tag = match[1].toLowerCase();
      if (!tags.includes(tag)) {
        tags.push(tag);
      }
    }
    return tags;
  },
};
