export interface ReelUrlInfo {
  shortcode: string;
  cleanUrl: string;
}

export const UrlParser = {
  INSTAGRAM_URL_REGEX: /https?:\/\/(?:www\.)?(?:instagram\.com|instagr\.am)\/(?:reel|reels|p)\/([A-Za-z0-9_-]+)/i,

  extractReelInfo(input: string): ReelUrlInfo | null {
    const match = input.match(this.INSTAGRAM_URL_REGEX);
    if (!match || !match[1]) return null;
    const shortcode = match[1];
    const cleanUrl = `https://www.instagram.com/reel/${shortcode}/`;
    return { shortcode, cleanUrl };
  },

  extractCaptionFromSharedText(input: string): string {
    return input.replace(/https?:\/\/\S+/gi, '').trim();
  },

  extractAuthorFromSharedText(input: string): string | null {
    const authorRegex = /(?:(?:vidéo|publication|post|reel)\s+de\s+|by\s+)@([A-Za-z0-9_.]+)/i;
    const match = input.match(authorRegex);
    if (match && match[1]) {
      return `@${match[1]}`;
    }
    const fallbackRegex = /@([A-Za-z0-9_.]+)/;
    const fallbackMatch = input.match(fallbackRegex);
    return fallbackMatch && fallbackMatch[1] ? `@${fallbackMatch[1]}` : null;
  },
};
