import { get, post, put } from './http.js';

const USERS_BASE = '/v1/users';

/**
 * Login with username + password. Returns { token, expiresAt }.
 */
export async function login(username, password) {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch { /* ignore */ }
    throw new Error(message);
  }
  return response.json();
}

/** Register a new user. Returns the created UserResponseDto. */
export function register(userData) {
  return post(USERS_BASE, userData);
}

/** Get a user by ID. */
export function getUserById(userId) {
  return get(`${USERS_BASE}/${userId}`);
}

/** Get all users (paginated). */
export function getAllUsers(params) {
  return get(USERS_BASE, params);
}

/** Update a user's profile. */
export function updateUser(userId, data) {
  return put(`${USERS_BASE}/${userId}`, data);
}
