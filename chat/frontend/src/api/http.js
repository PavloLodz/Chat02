import { store } from '../state/store.js';

const BASE_URL = '/api';

/**
 * Core fetch wrapper. Attaches JWT, handles errors, returns parsed JSON.
 */
async function request(path, options = {}) {
  const { token } = store.getState();
  const headers = { ...(options.headers || {}) };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Only set Content-Type for JSON bodies (skip for FormData)
  if (options.body && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    store.clearAuth();
    window.location.hash = '#/login';
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch {
      // ignore
    }
    throw new Error(message);
  }

  const contentType = response.headers.get('content-type') || '';
  if (response.status === 204 || !contentType.includes('application/json')) {
    return null;
  }
  return response.json();
}

export function get(path, params) {
  let url = path;
  if (params) {
    const qs = new URLSearchParams(params).toString();
    url = `${path}?${qs}`;
  }
  return request(url, { method: 'GET' });
}

export function post(path, body) {
  return request(path, { method: 'POST', body: body instanceof FormData ? body : JSON.stringify(body) });
}

export function put(path, body) {
  return request(path, { method: 'PUT', body: body instanceof FormData ? body : JSON.stringify(body) });
}

export function patch(path, body) {
  return request(path, { method: 'PATCH', body: body instanceof FormData ? body : JSON.stringify(body) });
}

export function del(path) {
  return request(path, { method: 'DELETE' });
}

/**
 * Auth-specific request (hits /auth instead of /api)
 */
export async function authRequest(path, body) {
  const response = await fetch(`/auth${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      // ignore
    }
    throw new Error(message);
  }

  return response.json();
}
