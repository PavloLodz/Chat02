import { get, put } from '../api/http.js';
import { store } from '../state/store.js';

const AFK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

let afkTimer = null;

function resetAfkTimer(onOnline, onOffline) {
  
  clearTimeout(afkTimer);
  // User became active - mark online
  onOnline();
  afkTimer = setTimeout(() => {
    // User went AFK
    onOffline();
  }, AFK_TIMEOUT_MS);
}

/**
 * Start AFK detection.
 * Calls onOnline when activity detected, onOffline when idle for AFK_TIMEOUT_MS.
 */
export function startPresenceDetection(onOnline, onOffline) {
  const events = ['mousemove', 'keydown', 'click', 'touchstart', 'scroll'];
  const handler = () => resetAfkTimer(onOnline, onOffline);

  events.forEach(e => window.addEventListener(e, handler, { passive: true }));
  resetAfkTimer(onOnline, onOffline);

  return () => {
    events.forEach(e => window.removeEventListener(e, handler));
    clearTimeout(afkTimer);
  };
}

/**
 * Update the current user's online status on the backend.
 * @param {boolean} online
 */
export async function updateOnlineStatus(online) {
  const { user } = store.getState();
  if (!user?.id) return;
  try {
    await put(`/v1/users/${user.id}`, { ...user, online });
    store.setState({ user: { ...user, online } });
  } catch {
    // silently fail - presence is best-effort
  }
}

/**
 * Poll online status for a list of user IDs.
 * Returns array of user objects with `online` field.
 */
export async function fetchOnlineStatuses() {
  try {
    const data = await get('/v1/users', { size: 200 });
    const users = data?.content || data || [];
    const onlineIds = users.filter(u => u.online).map(u => u.id);
    store.setOnlineUsers(onlineIds);
    return users;
  } catch {
    return [];
  }
}
