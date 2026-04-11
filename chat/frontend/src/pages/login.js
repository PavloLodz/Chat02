import { login, getAllUsers } from '../api/auth.js';
import { store } from '../state/store.js';

/**
 * Render the login page.
 * @param {HTMLElement} container
 * @param {Function} navigate
 */
export function renderLogin(container, navigate) {
  container.innerHTML = `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-card__logo">💬 ChatApp</div>
        <div class="auth-card__subtitle">Sign in to your account</div>

        <div class="form-group">
          <label class="form-label" for="username">Username</label>
          <input class="form-input" type="text" id="username" placeholder="Enter your username" autocomplete="username" />
        </div>

        <div class="form-group">
          <label class="form-label" for="password">Password</label>
          <input class="form-input" type="password" id="password" placeholder="Enter your password" autocomplete="current-password" />
        </div>

        <div class="form-error hidden" id="login-error"></div>

        <button class="btn btn--primary" id="login-btn" style="margin-top:8px">Sign In</button>

        <div class="auth-card__footer">
          Don't have an account?
          <a id="go-register">Create one</a>
        </div>
      </div>
    </div>
  `;

  const usernameInput = container.querySelector('#username');
  const passwordInput = container.querySelector('#password');
  const loginBtn = container.querySelector('#login-btn');
  const errorDiv = container.querySelector('#login-error');

  function showError(msg) {
    errorDiv.textContent = msg;
    errorDiv.classList.remove('hidden');
  }
  function clearError() {
    errorDiv.classList.add('hidden');
  }

  async function doLogin() {
    clearError();
    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username || !password) {
      showError('Please enter both username and password.');
      return;
    }

    loginBtn.disabled = true;
    loginBtn.textContent = 'Signing in...';

    try {
      const data = await login(username, password);
      // Temporarily set token so we can make authenticated requests
      store.setAuth(data.token, { username });

      // Fetch full user profile
      try {
        const users = await getAllUsers({ size: 200 });
        const userList = users?.content || users || [];
        const user = userList.find(u => u.username === username);
        if (user) store.setAuth(data.token, user);
      } catch {
        // Non-fatal — continue with minimal user info
      }

      navigate('#/chat');
    } catch (e) {
      showError(e.message || 'Login failed. Check your credentials.');
    } finally {
      loginBtn.disabled = false;
      loginBtn.textContent = 'Sign In';
    }
  }

  loginBtn.addEventListener('click', doLogin);
  [usernameInput, passwordInput].forEach(el =>
    el.addEventListener('keydown', (e) => { if (e.key === 'Enter') doLogin(); })
  );
  container.querySelector('#go-register').addEventListener('click', () => navigate('#/register'));
  usernameInput.focus();
}
