import { store } from './state/store.js';
import { renderLogin } from './pages/login.js';
import { renderRegister } from './pages/register.js';
import { renderChat } from './pages/chat.js';
import './style.css';

const routes = {
  '#/login': renderLogin,
  '#/register': renderRegister,
  '#/chat': renderChat,
};

function getCurrentRoute() {
  return window.location.hash || '#/login';
}

function navigate(hash) {
  window.location.hash = hash;
}

function isAuthenticated() {
  return !!store.getState().token;
}

function router() {
  const hash = getCurrentRoute();
  const app = document.getElementById('app');
  app.innerHTML = '';

  // Route protection
  if (hash !== '#/login' && hash !== '#/register' && !isAuthenticated()) {
    navigate('#/login');
    return;
  }
  if ((hash === '#/login' || hash === '#/register') && isAuthenticated()) {
    navigate('#/chat');
    return;
  }

  const render = routes[hash] ?? routes['#/login'];
  render(app, navigate);
}

window.addEventListener('hashchange', router);
window.addEventListener('load', router);

export { navigate };
