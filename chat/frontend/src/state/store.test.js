import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock sessionStorage and localStorage
const mockStorage = (() => {
  let store = {};
  return {
    getItem: vi.fn((key) => store[key] ?? null),
    setItem: vi.fn((key, val) => { store[key] = String(val); }),
    removeItem: vi.fn((key) => { delete store[key]; }),
    clear: vi.fn(() => { store = {}; }),
  };
})();

vi.stubGlobal('sessionStorage', mockStorage);
vi.stubGlobal('localStorage', mockStorage);

// Import after mocking
const { store } = await import('./store.js');

describe('store', () => {
  beforeEach(() => {
    mockStorage.clear();
    store.clearAuth();
  });

  it('has null token and user initially', () => {
    const state = store.getState();
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
  });

  it('setAuth stores token and user', () => {
    const user = { id: '1', username: 'alice' };
    store.setAuth('my-jwt', user);
    const state = store.getState();
    expect(state.token).toBe('my-jwt');
    expect(state.user).toEqual(user);
  });

  it('clearAuth removes token and user', () => {
    store.setAuth('my-jwt', { id: '1', username: 'alice' });
    store.clearAuth();
    const state = store.getState();
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
  });

  it('setActiveRoom updates activeRoom', () => {
    const room = { id: 'r1', name: 'General' };
    store.setActiveRoom(room);
    expect(store.getState().activeRoom).toEqual(room);
  });

  it('setActiveRoom with null clears activeRoom', () => {
    store.setActiveRoom({ id: 'r1', name: 'General' });
    store.setActiveRoom(null);
    expect(store.getState().activeRoom).toBeNull();
  });

  it('setContacts updates contacts list', () => {
    const contacts = [{ id: 'u1' }, { id: 'u2' }];
    store.setContacts(contacts);
    expect(store.getState().contacts).toEqual(contacts);
  });

  it('setOnlineUsers updates onlineUsers set', () => {
    store.setOnlineUsers(['u1', 'u2', 'u3']);
    const state = store.getState();
    expect(state.onlineUsers.has('u1')).toBe(true);
    expect(state.onlineUsers.has('u2')).toBe(true);
    expect(state.onlineUsers.has('u4')).toBe(false);
  });

  it('isOnline returns correct status', () => {
    store.setOnlineUsers(['u1', 'u2']);
    expect(store.isOnline('u1')).toBe(true);
    expect(store.isOnline('u99')).toBe(false);
  });

  it('subscribe fires on state change', () => {
    const listener = vi.fn();
    const unsub = store.subscribe(listener);
    store.setAuth('tok', { id: '1' });
    expect(listener).toHaveBeenCalledOnce();
    unsub();
  });

  it('unsubscribe stops listener from firing', () => {
    const listener = vi.fn();
    const unsub = store.subscribe(listener);
    unsub();
    store.setAuth('tok', { id: '1' });
    expect(listener).not.toHaveBeenCalled();
  });

  it('getState returns a copy, not the internal object', () => {
    store.setAuth('tok', { id: '1' });
    const state1 = store.getState();
    const state2 = store.getState();
    expect(state1).not.toBe(state2);
  });

  it('setState merges partial updates', () => {
    store.setAuth('tok', { id: '1', username: 'alice' });
    store.setState({ activeRoom: { id: 'r1' } });
    const state = store.getState();
    expect(state.token).toBe('tok');
    expect(state.activeRoom).toEqual({ id: 'r1' });
  });
});
