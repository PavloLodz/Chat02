import { describe, it, expect } from 'vitest';
import { formatDate, formatFileSize, truncate, escapeHtml, getInitials, debounce } from './format.js';

describe('formatDate', () => {
  it('returns empty string for null/undefined', () => {
    expect(formatDate(null)).toBe('');
    expect(formatDate(undefined)).toBe('');
  });

  it('returns empty string for invalid date', () => {
    expect(formatDate('not-a-date')).toBe('');
  });

  it('returns time string for today', () => {
    const now = new Date();
    const result = formatDate(now.toISOString());
    // Should be HH:MM format
    expect(result).toMatch(/^\d{1,2}:\d{2}(\s?(AM|PM))?$/i);
  });

  it('returns "Yesterday" for yesterday', () => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    expect(formatDate(yesterday.toISOString())).toBe('Yesterday');
  });

  it('returns date string for older dates', () => {
    const old = new Date('2020-01-15');
    const result = formatDate(old.toISOString());
    expect(result).toContain('Jan');
  });
});

describe('formatFileSize', () => {
  it('returns empty for null/undefined', () => {
    expect(formatFileSize(null)).toBe('');
    expect(formatFileSize(undefined)).toBe('');
  });

  it('returns "0 B" for 0', () => {
    expect(formatFileSize(0)).toBe('0 B');
  });

  it('formats bytes', () => {
    expect(formatFileSize(500)).toBe('500 B');
  });

  it('formats kilobytes', () => {
    expect(formatFileSize(1024)).toBe('1.0 KB');
  });

  it('formats megabytes', () => {
    expect(formatFileSize(1024 * 1024)).toBe('1.0 MB');
  });

  it('formats gigabytes', () => {
    expect(formatFileSize(1024 * 1024 * 1024)).toBe('1.0 GB');
  });

  it('formats partial KB', () => {
    expect(formatFileSize(1536)).toBe('1.5 KB');
  });
});

describe('truncate', () => {
  it('returns empty string for null/undefined', () => {
    expect(truncate(null)).toBe('');
    expect(truncate(undefined)).toBe('');
  });

  it('returns original string if within limit', () => {
    expect(truncate('hello', 10)).toBe('hello');
  });

  it('truncates with ellipsis', () => {
    expect(truncate('hello world', 8)).toBe('hello...');
  });

  it('uses default max length of 50', () => {
    const long = 'a'.repeat(60);
    const result = truncate(long);
    expect(result.length).toBe(50);
    expect(result.endsWith('...')).toBe(true);
  });
});

describe('escapeHtml', () => {
  it('returns empty string for null/undefined', () => {
    expect(escapeHtml(null)).toBe('');
    expect(escapeHtml(undefined)).toBe('');
  });

  it('escapes ampersands', () => {
    expect(escapeHtml('a & b')).toBe('a &amp; b');
  });

  it('escapes angle brackets', () => {
    expect(escapeHtml('<script>')).toBe('&lt;script&gt;');
  });

  it('escapes double quotes', () => {
    expect(escapeHtml('"hello"')).toBe('&quot;hello&quot;');
  });

  it('escapes single quotes', () => {
    expect(escapeHtml("it's")).toBe('it&#039;s');
  });

  it('handles mixed special chars', () => {
    expect(escapeHtml('<a href="test">O\'Reilly & Sons</a>')).toBe(
      '&lt;a href=&quot;test&quot;&gt;O&#039;Reilly &amp; Sons&lt;/a&gt;'
    );
  });
});

describe('getInitials', () => {
  it('returns ? for empty/null', () => {
    expect(getInitials('')).toBe('?');
    expect(getInitials(null)).toBe('?');
  });

  it('returns first letter uppercase for single word', () => {
    expect(getInitials('alice')).toBe('A');
  });

  it('returns first and last initials for two words', () => {
    expect(getInitials('John Doe')).toBe('JD');
  });

  it('returns first and last word initials for multiple words', () => {
    expect(getInitials('Mary Jane Watson')).toBe('MW');
  });
});

describe('debounce', () => {
  it('delays function execution', async () => {
    let callCount = 0;
    const debounced = debounce(() => { callCount++; }, 50);

    debounced();
    debounced();
    debounced();

    expect(callCount).toBe(0);
    await new Promise(r => setTimeout(r, 80));
    expect(callCount).toBe(1);
  });

  it('resets timer on each call', async () => {
    let callCount = 0;
    const debounced = debounce(() => { callCount++; }, 50);

    debounced();
    await new Promise(r => setTimeout(r, 30));
    debounced(); // Reset
    await new Promise(r => setTimeout(r, 30));
    expect(callCount).toBe(0); // Still not called
    await new Promise(r => setTimeout(r, 30));
    expect(callCount).toBe(1);
  });
});
