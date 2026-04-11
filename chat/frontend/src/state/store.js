/**
 * Lightweight global state manager.
 * Holds JWT, current user session, active room, and contacts.
 */

const TOKEN_KEY = 'chat_token';
const USER_KEY = 'chat_user';

function createStore() {
  let state = {
    token: sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY) || null,
    user: (() => {
      try {
        return JSON.parse(sessionStorage.getItem(USER_KEY) || localStorage.getItem(USER_KEY));
      } catch {
        return null;
      }
    })(),
    activeRoom: null,
    contacts: [],
    onlineUsers: new Set(),
  };

  const listeners = new Set();

  function getState() {
    return { ...state, onlineUsers: new Set(state.onlineUsers) };
  }

  function setState(patch) {
    state = { ...state, ...patch };
    listeners.forEach(fn => fn(getState()));
  }

  function subscribe(fn) {
    listeners.add(fn);
    return () => listeners.delete(fn);
  }

  function setAuth(token, user, remember = false) {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem(TOKEN_KEY, token);
    storage.setItem(USER_KEY, JSON.stringify(user));
    setState({ token, user });
  }

  function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    setState({ token: null, user: null, activeRoom: null, contacts: [], onlineUsers: new Set() });
  }

  function setActiveRoom(room) {
    setState({ activeRoom: room });
  }

  function setContacts(contacts) {
    setState({ contacts });
  }

  function setOnlineUsers(userIds) {
    setState({ onlineUsers: new Set(userIds) });
  }

  function isOnline(userId) {
    return state.onlineUsers.has(userId);
  }

  return { getState, setState, subscribe, setAuth, clearAuth, setActiveRoom, setContacts, setOnlineUsers, isOnline };
}

export const store = createStore();
