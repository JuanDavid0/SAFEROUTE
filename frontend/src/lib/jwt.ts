/**
 * Small JWT helpers to inspect exp claim without external deps
 */
export function parseJwt(token: string | null): any | null {
  try {
    if (!token) return null;
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payload = parts[1];
    // base64url -> base64
    let str = payload.replace(/-/g, '+').replace(/_/g, '/');
    const pad = str.length % 4;
    if (pad) str += '='.repeat(4 - pad);

    // Decode
    if (typeof window !== 'undefined' && typeof window.atob === 'function') {
      const decoded = window.atob(str);
      try {
        return JSON.parse(decodeURIComponent(decoded.split('').map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')));
      } catch (e) {
        return JSON.parse(decoded);
      }
    }

    // Node fallback
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    if (typeof Buffer !== 'undefined') {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      return JSON.parse(Buffer.from(str, 'base64').toString('utf8'));
    }

    return null;
  } catch (e) {
    return null;
  }
}

export function getExpiry(token: string | null): number | null {
  try {
    const parsed = parseJwt(token);
    if (!parsed) return null;
    // 'exp' is seconds since epoch
    if (typeof parsed.exp === 'number') return parsed.exp;
    // Some tokens may put it under payload.exp
    return null;
  } catch (e) {
    return null;
  }
}

export function isExpired(token: string | null): boolean {
  try {
    const exp = getExpiry(token);
    if (!exp) return false; // no exp claim -> assume non-expiring
    return Date.now() >= exp * 1000;
  } catch (e) {
    return false;
  }
}
