import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

// Mock store
vi.mock('../state/store.js', () => ({
  store: {
    getState: vi.fn(() => ({ token: 'test-jwt' })),
    clearAuth: vi.fn(),
  },
}));

// Mock window.location
vi.stubGlobal('window', {
  location: { hash: '' },
});

const { get, post, put, del } = await import('./http.js');

function mockFetch(status, body, contentType = 'application/json') {
  const headers = new Map([['content-type', contentType]]);
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    headers: { get: (k) => headers.get(k) },
    json: vi.fn().mockResolvedValue(body),
  });
}

describe('API http client', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', mockFetch(200, { data: 'ok' }));
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('GET sends Authorization header', async () => {
    await get('/v1/test');
    const [url, opts] = fetch.mock.calls[0];
    expect(url).toBe('/api/v1/test');
    expect(opts.headers['Authorization']).toBe('Bearer test-jwt');
    expect(opts.method).toBe('GET');
  });

  it('GET appends query params', async () => {
    await get('/v1/rooms', { page: 0, size: 10 });
    const [url] = fetch.mock.calls[0];
    expect(url).toContain('page=0');
    expect(url).toContain('size=10');
  });

  it('POST sends JSON body and Content-Type', async () => {
    await post('/v1/messages', { content: 'Hello' });
    const [url, opts] = fetch.mock.calls[0];
    expect(url).toBe('/api/v1/messages');
    expect(opts.method).toBe('POST');
    expect(opts.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(opts.body)).toEqual({ content: 'Hello' });
  });

  it('POST with FormData does not set Content-Type', async () => {
    const fd = new FormData();
    fd.append('file', new Blob(['x']));
    await post('/v1/attachments', fd);
    const [, opts] = fetch.mock.calls[0];
    expect(opts.headers['Content-Type']).toBeUndefined();
  });

  it('PUT sends correct method', async () => {
    await put('/v1/users/1', { username: 'bob' });
    const [, opts] = fetch.mock.calls[0];
    expect(opts.method).toBe('PUT');
  });

  it('DELETE sends correct method', async () => {
    await del('/v1/rooms/1');
    const [, opts] = fetch.mock.calls[0];
    expect(opts.method).toBe('DELETE');
  });

  it('returns null for 204 No Content', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 204,
      headers: { get: () => null },
      json: vi.fn(),
    }));
    const result = await del('/v1/rooms/1');
    expect(result).toBeNull();
  });

  it('throws on non-OK response', async () => {
    vi.stubGlobal('fetch', mockFetch(404, { message: 'Not found' }));
    await expect(get('/v1/missing')).rejects.toThrow('Not found');
  });

  it('throws on 500 server error', async () => {
    vi.stubGlobal('fetch', mockFetch(500, { error: 'Server error' }));
    await expect(get('/v1/broken')).rejects.toThrow('Server error');
  });

  it('calls clearAuth and redirects on 401', async () => {
    const { store } = await import('../state/store.js');
    vi.stubGlobal('fetch', mockFetch(401, { message: 'Unauthorized' }));
    await expect(get('/v1/protected')).rejects.toThrow('Unauthorized');
    expect(store.clearAuth).toHaveBeenCalled();
  });
});
