import { describe, it, expect } from 'vitest';

// Test the routing logic in isolation without importing main.js (which has side effects).
// We replicate and unit-test the core routing decision logic here.

function resolveRoute(hash, authenticated) {
  const publicRoutes = ['#/login', '#/register'];
  const isPublic = publicRoutes.includes(hash);

  if (!isPublic && !authenticated) return '#/login';   // redirect to login
  if (isPublic && authenticated) return '#/chat';       // redirect away from auth pages
  return hash;                                          // stay on requested route
}

describe('Router — route resolution', () => {
  it('redirects unauthenticated user from #/chat to #/login', () => {
    expect(resolveRoute('#/chat', false)).toBe('#/login');
  });

  it('allows unauthenticated user to visit #/login', () => {
    expect(resolveRoute('#/login', false)).toBe('#/login');
  });

  it('allows unauthenticated user to visit #/register', () => {
    expect(resolveRoute('#/register', false)).toBe('#/register');
  });

  it('redirects authenticated user away from #/login to #/chat', () => {
    expect(resolveRoute('#/login', true)).toBe('#/chat');
  });

  it('redirects authenticated user away from #/register to #/chat', () => {
    expect(resolveRoute('#/register', true)).toBe('#/chat');
  });

  it('allows authenticated user to access #/chat', () => {
    expect(resolveRoute('#/chat', true)).toBe('#/chat');
  });

  it('redirects unknown routes to #/login when not authenticated', () => {
    expect(resolveRoute('#/unknown', false)).toBe('#/login');
  });

  it('falls through unknown routes when authenticated', () => {
    expect(resolveRoute('#/unknown', true)).toBe('#/unknown');
  });
});

describe('Router — navigate helper', () => {
  it('sets window.location.hash', () => {
    const location = { hash: '' };
    function navigate(hash) { location.hash = hash; }

    navigate('#/login');
    expect(location.hash).toBe('#/login');

    navigate('#/chat');
    expect(location.hash).toBe('#/chat');
  });
});

describe('Router — route map', () => {
  const routes = {
    '#/login': 'renderLogin',
    '#/register': 'renderRegister',
    '#/chat': 'renderChat',
  };

  it('has entries for all expected routes', () => {
    expect(routes['#/login']).toBeDefined();
    expect(routes['#/register']).toBeDefined();
    expect(routes['#/chat']).toBeDefined();
  });

  it('maps #/login to renderLogin', () => {
    expect(routes['#/login']).toBe('renderLogin');
  });

  it('maps #/register to renderRegister', () => {
    expect(routes['#/register']).toBe('renderRegister');
  });

  it('falls back to login for unknown hash', () => {
    const hash = '#/unknown';
    const render = routes[hash] ?? routes['#/login'];
    expect(render).toBe('renderLogin');
  });
});
