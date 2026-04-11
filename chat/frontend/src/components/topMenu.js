import { store } from '../state/store.js';
import { getInitials } from '../utils/format.js';

/**
 * Render the top navigation bar.
 * @param {HTMLElement} container
 * @param {Function} navigate
 */
export function renderTopMenu(container, navigate) {
  const { user } = store.getState();
  const displayName = user?.displayName || user?.username || 'User';

  const nav = document.createElement('nav');
  nav.className = 'top-menu';
  nav.innerHTML = `
    <span class="top-menu__brand">💬 ChatApp</span>

    <div class="top-menu__user" id="tm-user">
      <div class="avatar">
        ${user?.avatarUrl
          ? `<img class="avatar__img" src="${user.avatarUrl}" alt="${displayName}" />`
          : `<div class="avatar__fallback">${getInitials(displayName)}</div>`}
        <span class="status-dot status-dot--online"></span>
      </div>
      <span style="font-size: 13px; font-weight: 500">${displayName}</span>
    </div>

    <button class="top-menu__btn" id="tm-settings" title="Settings">
      ⚙️ <span style="font-size:13px">Settings</span>
    </button>

    <button class="top-menu__btn top-menu__btn--danger" id="tm-logout" title="Logout">
      🚪 <span style="font-size:13px">Logout</span>
    </button>
  `;

  container.appendChild(nav);

  nav.querySelector('#tm-logout').addEventListener('click', () => {
    store.clearAuth();
    navigate('#/login');
  });

  // Subscribe to state changes to update online dot
  const unsub = store.subscribe((state) => {
    const dot = nav.querySelector('.status-dot');
    if (dot) {
      dot.classList.toggle('status-dot--online', !!state.user?.online);
    }
  });

  // Cleanup on navigation
  nav.dataset.cleanup = 'topMenu';
  nav._cleanup = unsub;

  return nav;
}
