import { register, login, getAllUsers } from '../api/auth.js';
import { store } from '../state/store.js';

/**
 * Render the registration page.
 * @param {HTMLElement} container
 * @param {Function} navigate
 */
export function renderRegister(container, navigate) {
  container.innerHTML = `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-card__logo">💬 ChatApp</div>
        <div class="auth-card__subtitle">Create your account</div>

        <div class="form-group">
          <label class="form-label" for="reg-username">Username</label>
          <input class="form-input" type="text" id="reg-username" placeholder="Choose a username" autocomplete="username" />
        </div>

        <div class="form-group">
          <label class="form-label" for="reg-displayname">Display Name</label>
          <input class="form-input" type="text" id="reg-displayname" placeholder="Your display name (optional)" />
        </div>

        <div class="form-group">
          <label class="form-label" for="reg-email">Email</label>
          <input class="form-input" type="email" id="reg-email" placeholder="your@email.com" autocomplete="email" />
        </div>

        <div class="form-group">
          <label class="form-label" for="reg-password">Password</label>
          <input class="form-input" type="password" id="reg-password" placeholder="Create a password" autocomplete="new-password" />
        </div>

        <div class="form-group">
          <label class="form-label" for="reg-confirm">Confirm Password</label>
          <input class="form-input" type="password" id="reg-confirm" placeholder="Repeat your password" autocomplete="new-password" />
        </div>

        <div class="form-error hidden" id="reg-error"></div>

        <button class="btn btn--primary" id="register-btn" style="margin-top:8px">Create Account</button>

        <div class="auth-card__footer">
          Already have an account?
          <a id="go-login">Sign in</a>
        </div>
      </div>
    </div>
  `;

  const fields = {
    username: container.querySelector('#reg-username'),
    displayName: container.querySelector('#reg-displayname'),
    email: container.querySelector('#reg-email'),
    password: container.querySelector('#reg-password'),
    confirm: container.querySelector('#reg-confirm'),
  };
  const btn = container.querySelector('#register-btn');
  const errorDiv = container.querySelector('#reg-error');

  function showError(msg) {
    errorDiv.textContent = msg;
    errorDiv.classList.remove('hidden');
  }
  function clearError() {
    errorDiv.classList.add('hidden');
  }

  async function doRegister() {
    clearError();
    const username = fields.username.value.trim();
    const displayName = fields.displayName.value.trim() || username;
    const email = fields.email.value.trim();
    const password = fields.password.value;
    const confirm = fields.confirm.value;

    if (!username || !email || !password) {
      showError('Username, email, and password are required.');
      return;
    }
    if (password !== confirm) {
      showError('Passwords do not match.');
      return;
    }
    if (password.length < 6) {
      showError('Password must be at least 6 characters.');
      return;
    }

    btn.disabled = true;
    btn.textContent = 'Creating account...';

    try {
      await register({
        username,
        email,
        passwordHash: password,
        displayName,
        online: false,
        role: 'USER',
      });

      // Auto-login after registration
      const data = await login(username, password);
      store.setAuth(data.token, { username, displayName, email });

      // Try to get full profile
      try {
        const users = await getAllUsers({ size: 200 });
        const userList = users?.content || users || [];
        const user = userList.find(u => u.username === username);
        if (user) store.setAuth(data.token, user);
      } catch { /* Non-fatal */ }

      navigate('#/chat');
    } catch (e) {
      showError(e.message || 'Registration failed. Username or email may already be taken.');
    } finally {
      btn.disabled = false;
      btn.textContent = 'Create Account';
    }
  }

  btn.addEventListener('click', doRegister);
  Object.values(fields).forEach(el =>
    el.addEventListener('keydown', (e) => { if (e.key === 'Enter') doRegister(); })
  );
  container.querySelector('#go-login').addEventListener('click', () => navigate('#/login'));
  fields.username.focus();
}
