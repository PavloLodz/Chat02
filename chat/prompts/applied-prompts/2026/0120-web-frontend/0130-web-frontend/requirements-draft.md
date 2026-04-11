
# Frontend Requirements — Classic Web Chat Application
> **Target:** IntelliJ IDEA Junie · Vanilla JavaScript (ES2022+, no frameworks)  
> **Backend:** Spring Boot REST API at `http://localhost:8080`  
> **Real-time:** HTTP polling (no WebSocket)  
> **Serving:** Spring Boot serves the built frontend — one port, no separate server  
> **Deployment:** Single Docker image via multi-stage Dockerfile + `docker-compose.yml`  
> **Goal:** A complete, production-ready single-page application that covers every functional requirement from the project specification.

---

## 1. Technology Stack

| Concern | Choice |
|---|---|
| Language | Vanilla JavaScript (ES2022+, ESModules) |
| Build tool | Vite (for dev server, bundling, and proxy) |
| Styling | Plain CSS (one global `style.css` + per-page CSS files) |
| Routing | Hash-based (`#/login`, `#/rooms`, `#/room/123`, `#/dm/456`) |
| Real-time | Short polling via `setInterval` + `fetch` |
| Emoji picker | `emoji-picker-element` (web component, lightweight) |
| Testing | Vitest + jsdom |
| Linting | ESLint with recommended rules |

Do **not** use any UI frameworks, component libraries, or transpiled languages. All code must be plain `.js` and `.css` files.

---

## 2. Project Structure

```
frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/
│   │   ├── auth.js          # Auth REST calls
│   │   ├── rooms.js         # Room REST calls
│   │   ├── messages.js      # Message REST calls
│   │   ├── contacts.js      # Friends/contacts REST calls
│   │   ├── attachments.js   # File upload/download calls
│   │   └── http.js          # Shared fetch wrapper
│   ├── pages/
│   │   ├── login.js         # Login page renderer + logic
│   │   ├── register.js      # Register page renderer + logic
│   │   ├── reset-password.js
│   │   └── chat.js          # Main chat page (shell)
│   ├── components/
│   │   ├── topMenu.js
│   │   ├── sidebar.js
│   │   ├── memberPanel.js
│   │   ├── messageList.js
│   │   ├── messageInput.js
│   │   ├── roomCatalog.js
│   │   ├── modal.js         # Generic modal open/close helper
│   │   └── toast.js         # Toast notification helper
│   ├── state/
│   │   └── store.js         # Lightweight in-memory app state
│   ├── utils/
│   │   ├── formatDate.js
│   │   ├── fileSize.js
│   │   ├── formatDate.test.js
│   │   ├── fileSize.test.js
│   │   ├── polling.js
│   │   ├── polling.test.js
│   │   ├── presence.js      # AFK detection logic
│   │   └── presence.test.js
│   ├── test/
│   │   └── setup.js         # Global test setup
│   ├── style.css            # Global styles + CSS variables
│   ├── main.js              # Entry point: router init
│   └── main.test.js
├── index.html
├── vite.config.js
└── package.json
```

---

## 3. Entry Point & Routing

### `index.html`
Single HTML file. Contains only the shell:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Classic Chat</title>
  <link rel="stylesheet" href="/src/style.css" />
</head>
<body>
  <div id="app"></div>
  <div id="toast-container"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

### `src/main.js` — Router

Implement a simple hash router:

```javascript
import { renderLogin } from './pages/login.js';
import { renderRegister } from './pages/register.js';
import { renderResetPassword } from './pages/reset-password.js';
import { renderChat } from './pages/chat.js';
import { getSession } from './state/store.js';

const routes = {
  '#/login':          renderLogin,
  '#/register':       renderRegister,
  '#/reset-password': renderResetPassword,
  '#/rooms':          renderChat,
  '#/room/:id':       renderChat,
  '#/dm/:id':         renderChat,
  '#/settings':       renderChat,
};

async function navigate() {
  const hash = location.hash || '#/rooms';
  const session = getSession();
  const isPublic = ['#/login', '#/register', '#/reset-password'].some(p => hash.startsWith(p));

  if (!session && !isPublic) { location.hash = '#/login'; return; }
  if (session && isPublic)   { location.hash = '#/rooms'; return; }

  const app = document.getElementById('app');
  app.innerHTML = '';

  // Match dynamic segments like #/room/:id
  for (const [pattern, renderer] of Object.entries(routes)) {
    const params = matchRoute(pattern, hash);
    if (params !== null) { await renderer(app, params); return; }
  }

  location.hash = '#/rooms';
}

window.addEventListener('hashchange', navigate);
window.addEventListener('DOMContentLoaded', navigate);
```

Implement `matchRoute(pattern, hash)` — returns a params object `{ id }` for dynamic segments, or `null` if no match.

---

## 4. Shared HTTP Wrapper (`src/api/http.js`)

```javascript
const BASE = '/api';

export async function request(method, path, body, isFormData = false) {
  const opts = {
    method,
    credentials: 'include',   // always send session cookie
  };
  if (body && !isFormData) {
    opts.headers = { 'Content-Type': 'application/json' };
    opts.body = JSON.stringify(body);
  } else if (body) {
    opts.body = body; // FormData — browser sets Content-Type automatically
  }

  const res = await fetch(BASE + path, opts);

  if (res.status === 401) {
    import('../state/store.js').then(m => m.clearSession());
    location.hash = '#/login';
    return null;
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || 'Request failed');
  }

  if (res.status === 204) return null;
  return res.json();
}

export const get   = (path)        => request('GET',    path);
export const post  = (path, body)  => request('POST',   path, body);
export const put   = (path, body)  => request('PUT',    path, body);
export const patch = (path, body)  => request('PATCH',  path, body);
export const del   = (path)        => request('DELETE', path);
```

---

## 5. In-Memory State (`src/state/store.js`)

A plain JS module that acts as the single source of truth for the current session.  
**Do not use localStorage for sensitive data.**

```javascript
let state = {
  user:       null,   // { id, username, email }
  rooms:      [],     // rooms the user has joined
  contacts:   [],     // friend list
  unread:     {},     // { 'room:123': 5, 'dm:456': 2 }
  presence:   {},     // { userId: 'online' | 'afk' | 'offline' }
  activeChat: null,   // { type: 'room'|'dm', id }
};

export const getSession   = () => state.user;
export const setSession   = (user) => { state.user = user; };
export const clearSession = () => { state = { user: null, rooms: [], contacts: [], unread: {}, presence: {}, activeChat: null }; };

export const getState  = () => state;
export const setState  = (patch) => { Object.assign(state, patch); };
```

Export individual getters/setters for each slice. Do not expose the raw `state` object directly.

---

## 6. API Modules

### `src/api/auth.js`
```javascript
export const login          = (email, password)              => post('/auth/login', { email, password });
export const register       = (email, username, password)    => post('/auth/register', { email, username, password });
export const logout         = ()                             => post('/auth/logout');
export const getMe          = ()                             => get('/users/me');
export const changePassword = (currentPassword, newPassword) => put('/auth/password/change', { currentPassword, newPassword });
export const requestReset   = (email)                        => post('/auth/password/reset-request', { email });
export const confirmReset   = (token, newPassword)           => post('/auth/password/reset', { token, newPassword });
export const deleteAccount  = ()                             => del('/users/me');
export const getSessions    = ()                             => get('/users/me/sessions');
export const revokeSession  = (id)                           => del(`/users/me/sessions/${id}`);
```

### `src/api/rooms.js`
```javascript
export const listJoinedRooms = ()                  => get('/rooms/joined');
export const searchRooms     = (query)             => get(`/rooms?search=${encodeURIComponent(query)}`);
export const getRoom         = (id)                => get(`/rooms/${id}`);
export const createRoom      = (data)              => post('/rooms', data);
export const updateRoom      = (id, data)          => patch(`/rooms/${id}`, data);
export const deleteRoom      = (id)                => del(`/rooms/${id}`);
export const joinRoom        = (id)                => post(`/rooms/${id}/join`);
export const leaveRoom       = (id)                => post(`/rooms/${id}/leave`);
export const getRoomMembers  = (id)                => get(`/rooms/${id}/members`);
export const getBanList      = (id)                => get(`/rooms/${id}/bans`);
export const banMember       = (roomId, userId)    => del(`/rooms/${roomId}/members/${userId}`);
export const unbanMember     = (roomId, userId)    => del(`/rooms/${roomId}/bans/${userId}`);
export const promoteAdmin    = (roomId, userId)    => post(`/rooms/${roomId}/admins/${userId}`);
export const demoteAdmin     = (roomId, userId)    => del(`/rooms/${roomId}/admins/${userId}`);
export const inviteToRoom    = (roomId, username)  => post(`/rooms/${roomId}/invitations`, { username });
export const markRoomRead    = (id)                => post(`/rooms/${id}/read`);
```

### `src/api/messages.js`
```javascript
export const getRoomMessages    = (roomId, before, limit = 50) =>
  get(`/rooms/${roomId}/messages?${before ? `before=${before}&` : ''}limit=${limit}`);

export const getDmMessages      = (userId, before, limit = 50) =>
  get(`/dms/${userId}/messages?${before ? `before=${before}&` : ''}limit=${limit}`);

export const getNewRoomMessages = (roomId, after) =>
  get(`/rooms/${roomId}/messages?after=${after}&limit=50`);

export const getNewDmMessages   = (userId, after) =>
  get(`/dms/${userId}/messages?after=${after}&limit=50`);

export const sendRoomMessage    = (roomId, data) => post(`/rooms/${roomId}/messages`, data);
export const sendDmMessage      = (userId, data) => post(`/dms/${userId}/messages`, data);
export const editMessage        = (id, text)     => patch(`/messages/${id}`, { text });
export const deleteMessage      = (id)           => del(`/messages/${id}`);
```

### `src/api/contacts.js`
```javascript
export const getFriends        = ()                        => get('/friends');
export const sendFriendRequest = (toUsername, message)     => post('/friends/request', { toUsername, message });
export const getRequests       = ()                        => get('/friends/requests/incoming');
export const acceptRequest     = (id)                      => post(`/friends/request/${id}/accept`);
export const declineRequest    = (id)                      => post(`/friends/request/${id}/decline`);
export const removeFriend      = (userId)                  => del(`/friends/${userId}`);
export const banUser           = (userId)                  => post(`/users/${userId}/ban`);
export const getPresence       = (userIds)                 => post('/presence/batch', { userIds });
export const markDmRead        = (userId)                  => post(`/dms/${userId}/read`);
```

### `src/api/attachments.js`
```javascript
export const uploadAttachment = (file, comment = '') => {
  const form = new FormData();
  form.append('file', file);
  if (comment) form.append('comment', comment);
  return request('POST', '/attachments/upload', form, true);
};

export const attachmentUrl = (id) => `/api/attachments/${id}`;
```

---

## 7. Auth Pages

### Login Page (`src/pages/login.js`)

Render into the `app` element:

```html
<div class="auth-page">
  <div class="auth-card">
    <h1>Classic Chat</h1>
    <h2>Sign In</h2>
    <div class="form-group">
      <label for="email">Email</label>
      <input type="email" id="email" autocomplete="email" />
      <span class="field-error" id="email-error"></span>
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input type="password" id="password" autocomplete="current-password" />
    </div>
    <div class="form-error" id="form-error"></div>
    <button id="login-btn" class="btn-primary">Sign In</button>
    <div class="auth-links">
      <a href="#/register">Create account</a>
      <a href="#/reset-password">Forgot password?</a>
    </div>
  </div>
</div>
```

Logic:
- On submit (button click or Enter in any field): validate fields are non-empty, call `login(email, password)`.
- On success: `setSession(user)` then `location.hash = '#/rooms'`.
- On error: display the error in `#form-error`.
- Disable button and show "Signing in…" text while the request is in flight.

### Register Page (`src/pages/register.js`)

Fields: Email, Username, Password, Confirm Password.

Client-side validation before submit:
- Email: valid format (`/^[^\s@]+@[^\s@]+\.[^\s@]+$/`).
- Username: 3–30 chars, alphanumeric + underscore only (`/^\w{3,30}$/`).
- Password: minimum 8 chars.
- Confirm Password: must match Password.

Show inline errors below each field. Only submit if all pass.

On success: redirect to `#/login` with a success toast "Account created. Please sign in."

### Password Reset Page (`src/pages/reset-password.js`)

Two states managed by a local variable:

**State 1 — Request (default):** Email field + [Send Reset Link] button → calls `requestReset(email)`. On success switch to State 2.

**State 2 — Confirm:** Read `token` from the URL query string (`new URLSearchParams(location.search).get('token')`). Show New Password + Confirm Password fields → calls `confirmReset(token, newPassword)`. On success redirect to `#/login`.

---

## 8. Chat Page Shell (`src/pages/chat.js`)

On load:
1. Call `getMe()` to verify session (401 is handled by http wrapper).
2. Load `listJoinedRooms()` and `getFriends()` in parallel with `Promise.all`.
3. Render the full shell HTML into the `app` element.
4. Initialise all components (topMenu, sidebar, memberPanel).
5. Start global pollers (notifications, roomList, contactList).
6. Parse the hash to determine the active chat and open it.

Shell HTML:

```html
<div id="chat-shell">
  <header id="top-menu"></header>
  <div id="main-area">
    <div id="message-area">
      <div id="chat-header"></div>
      <div id="message-list-wrapper">
        <div id="load-more-spinner" class="hidden">Loading…</div>
        <div id="message-list"></div>
        <button id="new-msg-btn" class="hidden floating-btn">↓ New messages</button>
      </div>
      <div id="message-input-area"></div>
    </div>
    <aside id="member-panel" class="hidden"></aside>
    <aside id="sidebar"></aside>
  </div>
</div>
```

When the hash changes to a different room or DM (without a full page reload):
- Stop the current `messages` poller.
- Clear `#message-list`.
- Load the new chat's history.
- Start a new `messages` poller for the new chat.
- Update `state.activeChat`.

---

## 9. Top Menu (`src/components/topMenu.js`)

```html
<div id="app-logo">Classic Chat</div>
<div id="top-menu-right">
  <span id="current-user-info">
    <span class="presence-dot online" id="self-presence-dot"></span>
    <strong id="self-username"></strong>
  </span>
  <button id="btn-notifications" class="icon-btn" title="Notifications">
    🔔<span id="notif-badge" class="badge hidden">0</span>
  </button>
  <button id="btn-settings" class="icon-btn" title="Settings">⚙</button>
  <button id="btn-logout" class="icon-btn" title="Sign out">⏻</button>
</div>
```

- Clicking 🔔 opens a notifications dropdown (absolutely-positioned `<div>`) listing incoming friend requests and room invitations, each with [Accept] / [Decline] buttons.
- Clicking ⚙ sets `location.hash = '#/settings'`.
- Clicking ⏻ calls `logout()`, `clearSession()`, `stopAllPollers()`, then `location.hash = '#/login'`.
- Close the notifications dropdown on click outside.

---

## 10. Sidebar (`src/components/sidebar.js`)

### Rooms Section

```html
<section id="rooms-section">
  <div class="section-header">
    <span>Rooms</span>
    <button id="btn-create-room" title="New room">+</button>
    <button id="btn-room-catalog" title="Browse rooms">🔍</button>
  </div>
  <ul id="room-list"></ul>
</section>
```

Each room `<li>`:
```html
<li class="room-item" data-room-id="123">
  <span class="room-name"># general</span>
  <span class="badge hidden" data-unread-room="123">3</span>
</li>
```

Add class `active` to the currently open room.

**Accordion behaviour:** when a room is active, add class `compacted` to `#rooms-section`. In compacted mode (CSS), only the `active` `<li>` is visible; the rest have `display: none`. Clicking the active item toggles `compacted` off to expand the full list.

### Contacts Section

```html
<section id="contacts-section">
  <div class="section-header">
    <span>Contacts</span>
    <button id="btn-add-friend" title="Add friend">+</button>
  </div>
  <ul id="contact-list"></ul>
</section>
```

Each contact `<li>`:
```html
<li class="contact-item" data-user-id="456">
  <span class="presence-dot offline"></span>
  <span class="contact-name">alice</span>
  <span class="badge hidden" data-unread-dm="456">1</span>
</li>
```

Clicking a contact: `location.hash = '#/dm/456'`.

---

## 11. Member Panel (`src/components/memberPanel.js`)

Show only when a room is active. Hide (`display: none`) for DM chats.

```html
<div id="member-panel-inner">
  <div class="panel-header">
    Members <span id="member-count"></span>
    <button id="btn-invite" class="icon-btn hidden" title="Invite user">➕</button>
  </div>
  <div class="member-group" id="group-online">
    <div class="member-group-label">Online</div>
  </div>
  <div class="member-group" id="group-afk">
    <div class="member-group-label">AFK</div>
  </div>
  <div class="member-group" id="group-offline">
    <div class="member-group-label">Offline</div>
  </div>
</div>
```

Each member row:
```html
<div class="member-row" data-user-id="789">
  <span class="presence-dot online"></span>
  <span class="member-name">bob</span>
  <button class="member-menu-btn" title="Actions">⋮</button>
</div>
```

Clicking ⋮ opens a context menu (small `<div>` positioned below the button) containing:
- "Send message" → `location.hash = '#/dm/789'`
- "Send friend request" → opens Add Friend modal pre-filled with username
- [Admin only] "Remove from room" → confirmation → `banMember(roomId, userId)`
- [Admin only] "Promote to admin" or "Demote admin"

Close the context menu on click outside using a one-time `document.addEventListener('click', handler)`.

Show the [Invite] button only if the current user is the room owner or an admin.

---

## 12. Message List (`src/components/messageList.js`)

### Rendering

Build each message as a DOM element (not innerHTML for security — use `textContent` for user-generated text):

```javascript
function createMessageEl(msg, currentUserId, isAdmin) {
  const el = document.createElement('div');
  el.className = 'message-item' + (msg.authorId === currentUserId ? ' own' : '');
  el.dataset.msgId = msg.id;
  el.id = `msg-${msg.id}`;

  // Reply quote
  if (msg.replyTo) {
    const quote = document.createElement('div');
    quote.className = 'reply-quote';
    quote.dataset.refId = msg.replyTo.id;
    // populate with textContent only
    el.appendChild(quote);
  }

  // Meta: avatar, author, timestamp
  // Body: text (textContent), then attachments
  // Actions toolbar (hidden by default)

  return el;
}
```

Use `element.textContent = value` for all user-supplied strings to prevent XSS. Only use `innerHTML` for static, developer-controlled HTML fragments.

- Add class `clustered` to consecutive messages from the same author within 5 minutes. In CSS, `.clustered .message-meta` has `display: none`.
- Show `.message-actions` on `mouseenter`, hide on `mouseleave`.

### Infinite Scroll (upward)

```javascript
const wrapper = document.getElementById('message-list-wrapper');
wrapper.addEventListener('scroll', async () => {
  if (wrapper.scrollTop > 100 || isLoadingMore) return;
  isLoadingMore = true;
  document.getElementById('load-more-spinner').classList.remove('hidden');

  const oldestId = document.querySelector('.message-item')?.dataset.msgId;
  const older = await fetchOlderMessages(oldestId);

  const prevHeight = wrapper.scrollHeight;
  prependMessages(older);
  wrapper.scrollTop = wrapper.scrollHeight - prevHeight; // preserve position

  document.getElementById('load-more-spinner').classList.add('hidden');
  isLoadingMore = false;
});
```

### Auto-scroll on new messages

```javascript
function appendNewMessages(messages) {
  const wrapper = document.getElementById('message-list-wrapper');
  const nearBottom = (wrapper.scrollHeight - wrapper.scrollTop - wrapper.clientHeight) < 200;

  messages.forEach(msg => {
    document.getElementById('message-list').appendChild(createMessageEl(msg));
  });

  if (nearBottom) {
    wrapper.scrollTop = wrapper.scrollHeight;
  } else {
    document.getElementById('new-msg-btn').classList.remove('hidden');
  }
}
```

The "↓ New messages" floating button scrolls to bottom and hides itself on click.

### Reply click

Clicking a `.reply-quote` scrolls `#msg-{refId}` into view and adds class `flash` to it for 1 second (CSS animation: yellow background fade-out).

---

## 13. Message Input (`src/components/messageInput.js`)

HTML rendered into `#message-input-area`:

```html
<div id="reply-bar" class="hidden">
  Replying to <strong id="reply-author-name"></strong>:
  <span id="reply-excerpt"></span>
  <button id="btn-cancel-reply">×</button>
</div>
<div id="attachment-preview-list"></div>
<div id="input-row">
  <button id="btn-emoji" title="Emoji" class="icon-btn">😊</button>
  <button id="btn-attach" title="Attach file" class="icon-btn">📎</button>
  <input type="file" id="file-input" style="display:none" multiple />
  <textarea id="msg-textarea" rows="1" placeholder="Type a message…"></textarea>
  <button id="btn-send" class="btn-primary">Send</button>
</div>
<div id="char-counter" class="hidden">0 / 3072</div>
<emoji-picker id="emoji-picker" class="hidden"></emoji-picker>
```

**Auto-grow textarea:**
```javascript
textarea.addEventListener('input', () => {
  textarea.style.height = 'auto';
  textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
  updateCharCounter();
});
```

**Send on Enter, newline on Shift+Enter:**
```javascript
textarea.addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
});
```

**Character counter:**
```javascript
function getByteLength(str) { return new TextEncoder().encode(str).length; }

function updateCharCounter() {
  const bytes = getByteLength(textarea.value);
  const counter = document.getElementById('char-counter');
  counter.textContent = `${bytes} / 3072`;
  counter.classList.toggle('hidden', bytes < 2873);  // show when <200 bytes left
  counter.classList.toggle('over-limit', bytes > 3072);
  document.getElementById('btn-send').disabled = bytes > 3072;
}
```

**Emoji picker:**
```javascript
import 'emoji-picker-element';
const picker = document.getElementById('emoji-picker');
picker.addEventListener('emoji-click', e => {
  textarea.value += e.detail.unicode;
  picker.classList.add('hidden');
  textarea.focus();
  updateCharCounter();
});
document.getElementById('btn-emoji').addEventListener('click', () => {
  picker.classList.toggle('hidden');
});
```

**File attach (button + paste):**
```javascript
document.getElementById('btn-attach').addEventListener('click', () => fileInput.click());
fileInput.addEventListener('change', () => handleFiles(Array.from(fileInput.files)));

textarea.addEventListener('paste', e => {
  const files = Array.from(e.clipboardData.files);
  if (files.length) { e.preventDefault(); handleFiles(files); }
});

function handleFiles(files) {
  for (const file of files) {
    const isImage = file.type.startsWith('image/');
    const limit = isImage ? 3 * 1024 * 1024 : 20 * 1024 * 1024;
    if (file.size > limit) {
      showToast(`${file.name} exceeds size limit.`, 'error');
      continue;
    }
    pendingAttachments.push({ file, comment: '' });
    renderAttachmentPreview(file, pendingAttachments.length - 1);
  }
}
```

Each attachment preview:
```html
<div class="attach-preview-item" data-index="0">
  <img class="thumb" />               <!-- images only; src = URL.createObjectURL(file) -->
  <span class="file-icon">📄</span>   <!-- non-images -->
  <span class="fname">report.pdf</span>
  <span class="fsize">2.4 MB</span>
  <input type="text" class="attach-comment" placeholder="Optional comment…" />
  <button class="btn-remove-attach">×</button>
</div>
```

**Send flow:**
1. Upload each pending file: `const result = await uploadAttachment(f.file, f.comment)`.
2. Collect `attachmentIds`.
3. Build payload: `{ text: textarea.value.trim(), replyToId: currentReplyId, attachmentIds }`.
4. Call `sendRoomMessage(roomId, payload)` or `sendDmMessage(userId, payload)`.
5. Clear textarea, pendingAttachments, reply bar. Reset textarea height.
6. Append the returned message to the list.

**Edit mode:**
- Clicking ✏ on a message: populate textarea with the existing text, change [Send] to [Save], show [Cancel ×].
- Save: call `editMessage(msgId, text)`, update the message DOM node's text, show "(edited)" label.
- Escape or [Cancel] exits edit mode without saving.

---

## 14. Polling — Real-Time Updates

### `src/utils/polling.js`

```javascript
const pollers = new Map();

export function startPoller(name, fn, intervalMs) {
  stopPoller(name);
  fn(); // run immediately
  pollers.set(name, setInterval(fn, intervalMs));
}

export function stopPoller(name) {
  if (pollers.has(name)) { clearInterval(pollers.get(name)); pollers.delete(name); }
}

export function stopAllPollers() {
  pollers.forEach(id => clearInterval(id));
  pollers.clear();
}
```

### Active Pollers

| Name | Interval | API call | Action |
|---|---|---|---|
| `messages` | 2 s | `getNewRoomMessages(roomId, lastMsgId)` or `getNewDmMessages(userId, lastMsgId)` | Append new messages, update unread |
| `presence` | 5 s | `getPresence(visibleUserIds)` | Update all presence dots in member panel and contact list |
| `notifications` | 10 s | `getRequests()` | Update bell badge count, refresh dropdown if open |
| `roomList` | 15 s | `listJoinedRooms()` | Refresh sidebar room list and unread badges |
| `contactList` | 15 s | `getFriends()` | Refresh contact list and unread badges |

- Start `messages` + `presence` when a chat is opened. Stop and restart when switching chats.
- Start `notifications` + `roomList` + `contactList` once after login.
- Stop all pollers on logout.
- Track `lastMsgId` as a module-level variable updated whenever new messages arrive.
- If the `messages` poller response is empty, do nothing (no DOM changes).

---

## 15. AFK Detection (`src/utils/presence.js`)

```javascript
const AFK_MS = 60_000; // 1 minute
let afkTimer = null;
let currentStatus = 'online';

const ACTIVITY_EVENTS = ['mousemove', 'keydown', 'mousedown', 'touchstart', 'scroll'];

export function initPresence(onStatusChange) {
  const resetTimer = () => {
    clearTimeout(afkTimer);
    if (currentStatus !== 'online') {
      currentStatus = 'online';
      onStatusChange('online');
    }
    afkTimer = setTimeout(() => {
      currentStatus = 'afk';
      onStatusChange('afk');
    }, AFK_MS);
  };

  ACTIVITY_EVENTS.forEach(ev => window.addEventListener(ev, resetTimer, { passive: true }));

  window.addEventListener('beforeunload', () => onStatusChange('offline'));
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) onStatusChange('afk');
    else resetTimer();
  });

  resetTimer();
}
```

`onStatusChange` calls `post('/presence', { status })` and updates `#self-presence-dot`'s class.

---

## 16. Modal System (`src/components/modal.js`)

```javascript
export function openModal({ title, bodyHTML, buttons }) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';

  const modal = document.createElement('div');
  modal.className = 'modal';
  modal.setAttribute('role', 'dialog');
  modal.setAttribute('aria-modal', 'true');

  const header = document.createElement('div');
  header.className = 'modal-header';
  const h2 = document.createElement('h2');
  h2.textContent = title;           // textContent — safe
  const closeBtn = document.createElement('button');
  closeBtn.className = 'btn-close-modal';
  closeBtn.textContent = '×';
  header.appendChild(h2);
  header.appendChild(closeBtn);

  const body = document.createElement('div');
  body.className = 'modal-body';
  body.innerHTML = bodyHTML;        // bodyHTML must only contain developer-controlled markup

  const footer = document.createElement('div');
  footer.className = 'modal-footer';
  buttons.forEach(({ label, className, onClick }) => {
    const btn = document.createElement('button');
    btn.textContent = label;
    btn.className = className || 'btn-secondary';
    btn.addEventListener('click', () => onClick(overlay));
    footer.appendChild(btn);
  });

  modal.append(header, body, footer);
  overlay.appendChild(modal);

  const close = () => overlay.remove();
  closeBtn.addEventListener('click', close);
  overlay.addEventListener('click', e => { if (e.target === overlay) close(); });
  const escHandler = e => { if (e.key === 'Escape') { close(); document.removeEventListener('keydown', escHandler); } };
  document.addEventListener('keydown', escHandler);

  document.body.appendChild(overlay);
  modal.querySelector('input, textarea, button')?.focus();
  return overlay;
}
```

**Note:** Any user-supplied data inserted into modal body content must use `.textContent`, not `.innerHTML`.

### Modals to Implement

**Create / Edit Room**
Fields: Name (text, required), Description (textarea), Visibility (radio: Public / Private).
Name blur → `GET /api/rooms/check-name?name=<n>` — show inline error if taken.

**Room Catalog**
Search input (debounced 300 ms → `searchRooms(query)`). Results list with [Join] / [Open] buttons.

**Add Friend**
Username text input + optional message textarea. Submit → `sendFriendRequest(username, message)`.

**Room Settings** (owner/admin only)
Tab bar with four tabs: General (edit name/description/visibility), Members (list with Remove / Promote / Demote), Admins, Banned Users (with Unban button and who-banned column).

**Confirmation**
Generic: "Are you sure you want to [action]?" with [Confirm] (danger style) and [Cancel] buttons.

**Delete Account**
Text input where the user must type `DELETE` exactly. [Delete Account] button is disabled until the input matches. On confirm: `deleteAccount()` → `clearSession()` → `location.hash = '#/login'`.

**Invite User**
Username input → `inviteToRoom(roomId, username)`.

---

## 17. Toast Notifications (`src/components/toast.js`)

```javascript
export function showToast(message, type = 'info') {
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.textContent = message;   // textContent — safe
  document.getElementById('toast-container').appendChild(t);
  setTimeout(() => t.remove(), 4000);
}
```

Use `showToast('Room created', 'success')` on successful operations.  
Use `showToast(err.message, 'error')` in all `catch` blocks.

---

## 18. Settings Page

When hash is `#/settings`, render the main area (replace `#message-area` content) with tabs:

**Tab: Profile**
- Current email displayed as read-only text.
- Change Password form: Current Password, New Password, Confirm New Password fields.
- Submit → `changePassword(current, newPass)` → success toast.

**Tab: Sessions**
- Table columns: Browser (user-agent), IP address, Last active, [Revoke].
- Current session row highlighted with a "(current)" label.
- Revoking the current session calls `revokeSession(id)` then triggers logout flow.
- Data loaded from `getSessions()`.

**Tab: Danger Zone**
- Red-bordered section with explanatory text.
- Input: "Type DELETE to confirm".
- [Delete Account] button, disabled until input value is exactly `DELETE`.
- On confirm: `deleteAccount()` → `clearSession()` → `stopAllPollers()` → `location.hash = '#/login'`.

---

## 19. CSS Guidelines (`src/style.css`)

Use CSS custom properties for theming:
```css
:root {
  --color-bg:            #1e1f22;
  --color-surface:       #2b2d31;
  --color-surface-2:     #313338;
  --color-border:        #3f4147;
  --color-primary:       #5865f2;
  --color-primary-hover: #4752c4;
  --color-text:          #dbdee1;
  --color-text-muted:    #949ba4;
  --color-danger:        #da3633;
  --color-success:       #2ea043;
  --color-online:        #23a559;
  --color-afk:           #f0b232;
  --color-offline:       #80848e;
  --sidebar-width:       240px;
  --member-panel-width:  200px;
  --top-menu-height:     48px;
  --font: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
```

**Layout:**
```css
#chat-shell {
  display: grid;
  grid-template-rows: var(--top-menu-height) 1fr;
  height: 100vh;
  overflow: hidden;
}
#main-area {
  display: grid;
  grid-template-columns: 1fr var(--member-panel-width) var(--sidebar-width);
  overflow: hidden;
}
#message-area {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
#message-list-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
```

**Presence dots:**
```css
.presence-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
.presence-dot.online  { background: var(--color-online); }
.presence-dot.afk     { background: var(--color-afk); }
.presence-dot.offline { background: var(--color-offline); }
```

**Unread badge:**
```css
.badge {
  background: var(--color-danger); color: #fff; border-radius: 10px;
  font-size: 11px; padding: 1px 6px; min-width: 18px; text-align: center;
}
.badge.hidden { display: none; }
```

**Message flash animation:**
```css
@keyframes flash-highlight {
  0%   { background: rgba(255, 220, 50, 0.4); }
  100% { background: transparent; }
}
.message-item.flash { animation: flash-highlight 1s ease-out; }
```

**Rooms section accordion:**
```css
#rooms-section.compacted .room-item:not(.active) { display: none; }
```

---

## 20. Vite Configuration (`vite.config.js`)

```javascript
import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    // Output directly into Spring Boot's static resources folder.
    // Running `npm run build` makes the frontend available at localhost:8080
    // with no separate server needed.
    outDir: '../chat/src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    // Dev server proxy: forwards /api calls to the backend during `npm run dev`.
    // Not active in production — frontend and backend share the same origin.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.js'],
    coverage: {
      reporter: ['text', 'html'],
      include: ['src/**/*.js'],
      exclude: ['src/test/**', 'src/main.js'],
      thresholds: {
        lines:      80,
        functions:  80,
        branches:   75,
        statements: 80,
      },
    },
  },
});
```

---

## 21. `package.json`

```json
{
  "name": "chat-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev":           "vite",
    "build":         "vite build",
    "preview":       "vite preview",
    "test":          "vitest run",
    "test:watch":    "vitest",
    "test:coverage": "vitest run --coverage"
  },
  "dependencies": {
    "emoji-picker-element": "^1.21.0"
  },
  "devDependencies": {
    "vite":                "^5.0.0",
    "vitest":              "^1.0.0",
    "@vitest/coverage-v8": "^1.0.0",
    "jsdom":               "^24.0.0",
    "eslint":              "^8.0.0"
  }
}
```

---

## 22. Security Rules

- **Never use `innerHTML` with user-supplied data.** Always use `textContent` or `createElement` + `textContent` for usernames, message text, room names, and any other server-provided strings.
- `innerHTML` is only acceptable for static, developer-written HTML template strings that contain no user data.
- All `fetch` calls include `credentials: 'include'`.
- File downloads go through the authenticated `/api/attachments/:id` endpoint — do not expose direct file system paths.

---

## 23. Error Handling Rules

Every `async` function that calls the API must be wrapped in `try/catch`:
```javascript
try {
  await someApiCall();
  showToast('Done', 'success');
} catch (err) {
  showToast(err.message || 'Something went wrong.', 'error');
}
```

HTTP 403 → toast "Permission denied".  
HTTP 404 → toast "Not found".  
Network failure → toast "Network error. Please try again."  
Never display raw JSON or stack traces to the user.

---

## 24. Implementation Order

1. **Project scaffold** — `index.html`, `main.js`, router, `vite.config.js`, `package.json`, `style.css` skeleton. Add `src/test/setup.js` and configure Vitest in `vite.config.js`.
2. **HTTP wrapper + state** — `http.js`, `store.js`. Write `http.test.js` and `store.test.js`.
3. **Auth pages** — Login, Register, Password Reset. Write `login.test.js` and `register.test.js`.
4. **Chat shell** — `chat.js`, top menu, sidebar (static render), routing between chats.
5. **Rooms** — sidebar room list, catalog modal, create modal, join/leave. Write `rooms.test.js` and `sidebar.test.js`.
6. **Message list** — load history via REST, render messages, infinite scroll, auto-scroll. Write `messageList.test.js`.
7. **Message input** — send, reply, edit, delete, character counter. Write `messageInput.test.js` and `messages.test.js`.
8. **Polling** — `polling.js`, message poller (new messages every 2 s). Write `polling.test.js`.
9. **Contacts** — friend list render, DM chat, friend request modal, accept/decline. Write `contacts.test.js`.
10. **Presence** — AFK detection, presence dots, presence poller. Write `presence.test.js` and `formatDate.test.js`.
11. **Attachments** — upload, inline image preview, paste support, file download.
12. **Admin features** — ban/unban, promote/demote, room settings modal, delete room.
13. **Settings page** — password change, sessions list, account deletion.
14. **Polish** — unread counters in browser tab title, image lightbox (click to expand), notification badge count, all success/error toasts.

---

## 25. Unit Tests

### 25.1 Test Framework and Setup

| Concern | Choice |
|---|---|
| Test runner | Vitest (built-in ESModule support, same config as Vite) |
| DOM environment | `jsdom` |
| Mocking | Vitest built-in `vi.fn()`, `vi.spyOn()`, `vi.mock()` |
| Coverage | `@vitest/coverage-v8` |

All test dependencies and scripts are already declared in `package.json` (section 21) and the `test` block is already part of `vite.config.js` (section 20). No additional configuration files are needed.

### 25.2 Test Setup File (`src/test/setup.js`)

```javascript
import { afterEach, vi } from 'vitest';

// Reset DOM between tests
afterEach(() => {
  document.body.innerHTML = '';
  document.head.innerHTML = '';
  vi.restoreAllMocks();
});

// Stub fetch globally — each test must set up its own fetch mock
global.fetch = vi.fn();

// Stub location — jsdom does not support hash assignment side-effects
delete window.location;
window.location = { hash: '', assign: vi.fn(), reload: vi.fn() };

// Minimal toast container so toast.js never throws
beforeEach(() => {
  const tc = document.createElement('div');
  tc.id = 'toast-container';
  document.body.appendChild(tc);
});
```

### 25.3 Test File Structure

Place test files alongside source files using the `.test.js` suffix:

```
src/
├── api/
│   ├── http.js
│   ├── http.test.js
│   ├── auth.js
│   ├── auth.test.js
│   ├── rooms.js
│   ├── rooms.test.js
│   ├── messages.js
│   ├── messages.test.js
│   ├── contacts.js
│   └── contacts.test.js
├── state/
│   ├── store.js
│   └── store.test.js
├── utils/
│   ├── formatDate.js
│   ├── formatDate.test.js
│   ├── fileSize.js
│   ├── fileSize.test.js
│   ├── polling.js
│   ├── polling.test.js
│   ├── presence.js
│   └── presence.test.js
├── components/
│   ├── toast.js
│   ├── toast.test.js
│   ├── modal.js
│   ├── modal.test.js
│   ├── messageList.js
│   ├── messageList.test.js
│   ├── messageInput.js
│   ├── messageInput.test.js
│   ├── sidebar.js
│   └── sidebar.test.js
├── pages/
│   ├── login.js
│   ├── login.test.js
│   ├── register.js
│   └── register.test.js
└── main.js
    main.test.js
```

---

### 25.4 `src/api/http.test.js`

```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { request, get, post, del } from './http.js';

function mockFetch(status, body) {
  global.fetch = vi.fn().mockResolvedValue({
    status,
    ok: status >= 200 && status < 300,
    json: () => Promise.resolve(body),
    statusText: 'Error',
  });
}

describe('request()', () => {
  it('sends GET with credentials: include', async () => {
    mockFetch(200, { id: 1 });
    await get('/users/me');
    expect(fetch).toHaveBeenCalledWith('/api/users/me', expect.objectContaining({
      method: 'GET',
      credentials: 'include',
    }));
  });

  it('sends POST with JSON body and Content-Type header', async () => {
    mockFetch(200, {});
    await post('/auth/login', { email: 'a@b.com', password: 'secret' });
    const [, opts] = fetch.mock.calls[0];
    expect(opts.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(opts.body)).toEqual({ email: 'a@b.com', password: 'secret' });
  });

  it('returns null and redirects to #/login on 401', async () => {
    mockFetch(401, {});
    const result = await get('/protected');
    expect(result).toBeNull();
    expect(window.location.hash).toBe('#/login');
  });

  it('throws an Error with server message on non-ok response', async () => {
    mockFetch(403, { message: 'Permission denied' });
    await expect(get('/admin')).rejects.toThrow('Permission denied');
  });

  it('throws a fallback error when response body is not JSON', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      status: 500,
      ok: false,
      json: () => Promise.reject(new Error('not json')),
      statusText: 'Internal Server Error',
    });
    await expect(get('/broken')).rejects.toThrow('Internal Server Error');
  });

  it('returns null for 204 No Content', async () => {
    global.fetch = vi.fn().mockResolvedValue({ status: 204, ok: true });
    const result = await del('/something');
    expect(result).toBeNull();
  });

  it('sends FormData without Content-Type header', async () => {
    mockFetch(200, { id: 'att1' });
    const form = new FormData();
    form.append('file', new Blob(['hello']), 'hello.txt');
    const { request } = await import('./http.js');
    await request('POST', '/attachments/upload', form, true);
    const [, opts] = fetch.mock.calls[0];
    expect(opts.headers).toBeUndefined();
    expect(opts.body).toBeInstanceOf(FormData);
  });
});
```

---

### 25.5 `src/state/store.test.js`

```javascript
import { describe, it, expect, beforeEach } from 'vitest';
import { getSession, setSession, clearSession, getState, setState } from './store.js';

beforeEach(() => clearSession());

describe('session management', () => {
  it('getSession returns null initially', () => {
    expect(getSession()).toBeNull();
  });

  it('setSession stores the user object', () => {
    setSession({ id: 1, username: 'alice' });
    expect(getSession()).toEqual({ id: 1, username: 'alice' });
  });

  it('clearSession resets user to null', () => {
    setSession({ id: 1, username: 'alice' });
    clearSession();
    expect(getSession()).toBeNull();
  });

  it('clearSession resets all state slices', () => {
    setState({ rooms: [{ id: 1 }], unread: { 'room:1': 3 } });
    clearSession();
    const s = getState();
    expect(s.rooms).toEqual([]);
    expect(s.unread).toEqual({});
    expect(s.contacts).toEqual([]);
    expect(s.presence).toEqual({});
    expect(s.activeChat).toBeNull();
  });
});

describe('setState()', () => {
  it('merges a patch into state without wiping other keys', () => {
    setSession({ id: 1, username: 'alice' });
    setState({ rooms: [{ id: 10, name: 'general' }] });
    expect(getState().rooms).toHaveLength(1);
    expect(getSession()).toEqual({ id: 1, username: 'alice' }); // still intact
  });

  it('updates unread counts', () => {
    setState({ unread: { 'room:5': 2 } });
    setState({ unread: { 'room:5': 5, 'dm:3': 1 } });
    expect(getState().unread['room:5']).toBe(5);
    expect(getState().unread['dm:3']).toBe(1);
  });
});
```

---

### 25.6 `src/utils/formatDate.test.js`

`formatDate.js` must export two functions:
- `formatRelative(dateString)` → relative string for dates < 1 hour old, absolute otherwise.
- `formatAbsolute(dateString)` → always returns locale date+time string.

```javascript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { formatRelative, formatAbsolute } from './formatDate.js';

describe('formatRelative()', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-04-08T12:00:00Z'));
  });
  afterEach(() => vi.useRealTimers());

  it('returns "just now" for messages less than 60 seconds old', () => {
    expect(formatRelative('2026-04-08T11:59:30Z')).toBe('just now');
  });

  it('returns "X min ago" for messages under 60 minutes old', () => {
    expect(formatRelative('2026-04-08T11:55:00Z')).toBe('5 min ago');
  });

  it('returns "59 min ago" at the boundary', () => {
    expect(formatRelative('2026-04-08T11:01:00Z')).toBe('59 min ago');
  });

  it('returns an absolute date string for messages older than 1 hour', () => {
    const result = formatRelative('2026-04-08T10:59:00Z');
    // Should NOT contain "min ago"
    expect(result).not.toContain('min ago');
    expect(result.length).toBeGreaterThan(5);
  });
});

describe('formatAbsolute()', () => {
  it('returns a non-empty string for a valid ISO date', () => {
    const result = formatAbsolute('2026-01-15T09:30:00Z');
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });
});
```

---

### 25.7 `src/utils/fileSize.test.js`

`fileSize.js` must export `formatFileSize(bytes)` → human-readable string.

```javascript
import { describe, it, expect } from 'vitest';
import { formatFileSize } from './fileSize.js';

describe('formatFileSize()', () => {
  it('formats bytes under 1 KB', () => {
    expect(formatFileSize(512)).toBe('512 B');
  });

  it('formats kilobytes', () => {
    expect(formatFileSize(2048)).toBe('2.0 KB');
  });

  it('formats megabytes', () => {
    expect(formatFileSize(3 * 1024 * 1024)).toBe('3.0 MB');
  });

  it('formats the 20 MB file size limit correctly', () => {
    expect(formatFileSize(20 * 1024 * 1024)).toBe('20.0 MB');
  });

  it('returns "0 B" for zero', () => {
    expect(formatFileSize(0)).toBe('0 B');
  });
});
```

---

### 25.8 `src/utils/polling.test.js`

```javascript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { startPoller, stopPoller, stopAllPollers } from './polling.js';

beforeEach(() => {
  vi.useFakeTimers();
  stopAllPollers(); // clean slate
});
afterEach(() => {
  stopAllPollers();
  vi.useRealTimers();
});

describe('startPoller()', () => {
  it('calls the function immediately on start', () => {
    const fn = vi.fn();
    startPoller('test', fn, 1000);
    expect(fn).toHaveBeenCalledTimes(1);
  });

  it('calls the function again after the interval', () => {
    const fn = vi.fn();
    startPoller('test', fn, 1000);
    vi.advanceTimersByTime(1000);
    expect(fn).toHaveBeenCalledTimes(2);
    vi.advanceTimersByTime(1000);
    expect(fn).toHaveBeenCalledTimes(3);
  });

  it('replaces an existing poller with the same name', () => {
    const fn1 = vi.fn();
    const fn2 = vi.fn();
    startPoller('test', fn1, 1000);
    startPoller('test', fn2, 1000); // replaces fn1
    vi.advanceTimersByTime(1000);
    expect(fn1).toHaveBeenCalledTimes(1); // only the initial call
    expect(fn2).toHaveBeenCalledTimes(2); // initial + interval
  });
});

describe('stopPoller()', () => {
  it('prevents further calls after stopping', () => {
    const fn = vi.fn();
    startPoller('test', fn, 1000);
    stopPoller('test');
    vi.advanceTimersByTime(3000);
    expect(fn).toHaveBeenCalledTimes(1); // only the immediate call
  });

  it('does nothing when stopping a non-existent poller', () => {
    expect(() => stopPoller('nonexistent')).not.toThrow();
  });
});

describe('stopAllPollers()', () => {
  it('stops all running pollers', () => {
    const fn1 = vi.fn();
    const fn2 = vi.fn();
    startPoller('a', fn1, 500);
    startPoller('b', fn2, 500);
    stopAllPollers();
    vi.advanceTimersByTime(2000);
    expect(fn1).toHaveBeenCalledTimes(1);
    expect(fn2).toHaveBeenCalledTimes(1);
  });
});
```

---

### 25.9 `src/utils/presence.test.js`

```javascript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { initPresence } from './presence.js';

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

describe('initPresence()', () => {
  it('calls onStatusChange with "online" immediately', () => {
    const cb = vi.fn();
    initPresence(cb);
    expect(cb).toHaveBeenCalledWith('online');
  });

  it('calls onStatusChange with "afk" after 60 seconds of no activity', () => {
    const cb = vi.fn();
    initPresence(cb);
    cb.mockClear();
    vi.advanceTimersByTime(60_000);
    expect(cb).toHaveBeenCalledWith('afk');
  });

  it('resets to "online" when a user activity event fires after going AFK', () => {
    const cb = vi.fn();
    initPresence(cb);
    vi.advanceTimersByTime(60_000); // go AFK
    cb.mockClear();
    window.dispatchEvent(new Event('mousemove'));
    expect(cb).toHaveBeenCalledWith('online');
  });

  it('does NOT report "online" again if already online and user moves mouse', () => {
    const cb = vi.fn();
    initPresence(cb);
    cb.mockClear();
    vi.advanceTimersByTime(10_000); // still online, not yet AFK
    window.dispatchEvent(new Event('keydown'));
    // should not fire again because status is already 'online'
    expect(cb).not.toHaveBeenCalled();
  });

  it('resets the AFK timer on each activity event', () => {
    const cb = vi.fn();
    initPresence(cb);
    cb.mockClear();
    vi.advanceTimersByTime(50_000);
    window.dispatchEvent(new Event('mousemove')); // reset timer
    vi.advanceTimersByTime(50_000); // would have hit 100s total, but timer reset
    expect(cb).not.toHaveBeenCalledWith('afk');
    vi.advanceTimersByTime(10_001); // now 60s since last activity
    expect(cb).toHaveBeenCalledWith('afk');
  });

  it('calls onStatusChange with "offline" on beforeunload', () => {
    const cb = vi.fn();
    initPresence(cb);
    cb.mockClear();
    window.dispatchEvent(new Event('beforeunload'));
    expect(cb).toHaveBeenCalledWith('offline');
  });

  it('calls onStatusChange with "afk" when tab becomes hidden', () => {
    const cb = vi.fn();
    initPresence(cb);
    cb.mockClear();
    Object.defineProperty(document, 'hidden', { value: true, configurable: true });
    document.dispatchEvent(new Event('visibilitychange'));
    expect(cb).toHaveBeenCalledWith('afk');
    Object.defineProperty(document, 'hidden', { value: false, configurable: true });
  });
});
```

---

### 25.10 `src/components/toast.test.js`

```javascript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { showToast } from './toast.js';

beforeEach(() => {
  // toast-container already added by setup.js
});

describe('showToast()', () => {
  it('appends a toast element to #toast-container', () => {
    showToast('Hello world', 'success');
    const container = document.getElementById('toast-container');
    expect(container.children).toHaveLength(1);
    expect(container.firstChild.textContent).toBe('Hello world');
  });

  it('sets the correct class for each type', () => {
    showToast('Error!', 'error');
    showToast('Info', 'info');
    const toasts = document.getElementById('toast-container').children;
    expect(toasts[0].className).toContain('toast-error');
    expect(toasts[1].className).toContain('toast-info');
  });

  it('defaults to type "info" when no type is provided', () => {
    showToast('Default');
    expect(document.getElementById('toast-container').firstChild.className).toContain('toast-info');
  });

  it('removes the toast after 4 seconds', () => {
    vi.useFakeTimers();
    showToast('Temporary', 'success');
    expect(document.getElementById('toast-container').children).toHaveLength(1);
    vi.advanceTimersByTime(4000);
    expect(document.getElementById('toast-container').children).toHaveLength(0);
    vi.useRealTimers();
  });

  it('uses textContent so HTML is not interpreted', () => {
    showToast('<script>alert(1)</script>', 'error');
    const toast = document.getElementById('toast-container').firstChild;
    expect(toast.innerHTML).toBe('&lt;script&gt;alert(1)&lt;/script&gt;');
  });
});
```

---

### 25.11 `src/components/modal.test.js`

```javascript
import { describe, it, expect, vi } from 'vitest';
import { openModal } from './modal.js';

describe('openModal()', () => {
  it('appends the modal overlay to document.body', () => {
    openModal({ title: 'Test', bodyHTML: '<p>Content</p>', buttons: [] });
    expect(document.querySelector('.modal-overlay')).not.toBeNull();
  });

  it('renders the title using textContent (XSS-safe)', () => {
    openModal({ title: '<b>Danger</b>', bodyHTML: '', buttons: [] });
    const h2 = document.querySelector('.modal h2');
    expect(h2.textContent).toBe('<b>Danger</b>');
    expect(h2.innerHTML).toBe('&lt;b&gt;Danger&lt;/b&gt;');
  });

  it('renders all buttons with correct labels', () => {
    openModal({
      title: 'Confirm',
      bodyHTML: '',
      buttons: [
        { label: 'OK', className: 'btn-primary', onClick: vi.fn() },
        { label: 'Cancel', className: 'btn-secondary', onClick: vi.fn() },
      ],
    });
    const btns = document.querySelectorAll('.modal-footer button');
    expect(btns).toHaveLength(2);
    expect(btns[0].textContent).toBe('OK');
    expect(btns[1].textContent).toBe('Cancel');
  });

  it('calls the button onClick with the overlay element', () => {
    const onClick = vi.fn();
    openModal({ title: 'X', bodyHTML: '', buttons: [{ label: 'Go', onClick }] });
    document.querySelector('.modal-footer button').click();
    expect(onClick).toHaveBeenCalledWith(expect.any(HTMLElement));
  });

  it('removes the overlay when the close (×) button is clicked', () => {
    openModal({ title: 'Close me', bodyHTML: '', buttons: [] });
    document.querySelector('.btn-close-modal').click();
    expect(document.querySelector('.modal-overlay')).toBeNull();
  });

  it('removes the overlay when clicking the backdrop', () => {
    openModal({ title: 'Backdrop', bodyHTML: '', buttons: [] });
    document.querySelector('.modal-overlay').click();
    expect(document.querySelector('.modal-overlay')).toBeNull();
  });

  it('removes the overlay on Escape key', () => {
    openModal({ title: 'Escape', bodyHTML: '', buttons: [] });
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(document.querySelector('.modal-overlay')).toBeNull();
  });

  it('does not close the modal when clicking inside the modal dialog', () => {
    openModal({ title: 'Stay', bodyHTML: '<p>Content</p>', buttons: [] });
    document.querySelector('.modal').click();
    expect(document.querySelector('.modal-overlay')).not.toBeNull();
  });
});
```

---

### 25.12 `src/main.test.js` — Router

```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { matchRoute } from './main.js'; // matchRoute must be a named export

describe('matchRoute()', () => {
  it('matches an exact static route', () => {
    expect(matchRoute('#/login', '#/login')).toEqual({});
  });

  it('returns null for a non-matching static route', () => {
    expect(matchRoute('#/login', '#/register')).toBeNull();
  });

  it('matches a dynamic segment and extracts the id', () => {
    expect(matchRoute('#/room/:id', '#/room/42')).toEqual({ id: '42' });
  });

  it('matches a DM dynamic route', () => {
    expect(matchRoute('#/dm/:id', '#/dm/99')).toEqual({ id: '99' });
  });

  it('returns null when the dynamic pattern does not match', () => {
    expect(matchRoute('#/room/:id', '#/dm/42')).toBeNull();
  });

  it('returns null for an empty hash against a non-empty pattern', () => {
    expect(matchRoute('#/rooms', '')).toBeNull();
  });

  it('matches #/rooms exactly', () => {
    expect(matchRoute('#/rooms', '#/rooms')).toEqual({});
  });
});

describe('navigate() — auth guard', () => {
  beforeEach(async () => {
    // Reset store before each test
    const { clearSession } = await import('./state/store.js');
    clearSession();
  });

  it('redirects to #/login when accessing a protected route without a session', async () => {
    window.location.hash = '#/rooms';
    const { navigate } = await import('./main.js');
    await navigate();
    expect(window.location.hash).toBe('#/login');
  });

  it('redirects to #/rooms when accessing #/login with an active session', async () => {
    const { setSession } = await import('./state/store.js');
    setSession({ id: 1, username: 'alice' });
    window.location.hash = '#/login';
    const { navigate } = await import('./main.js');
    await navigate();
    expect(window.location.hash).toBe('#/rooms');
  });
});
```

> **Note:** `matchRoute` and `navigate` must be exported from `main.js` for testability:
> ```javascript
> export function matchRoute(pattern, hash) { … }
> export async function navigate() { … }
> ```

---

### 25.13 `src/pages/login.test.js`

```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderLogin } from './login.js';

vi.mock('../api/auth.js', () => ({
  login: vi.fn(),
}));
vi.mock('../state/store.js', () => ({
  setSession: vi.fn(),
  getSession: vi.fn(() => null),
}));

import { login } from '../api/auth.js';
import { setSession } from '../state/store.js';

let app;
beforeEach(async () => {
  app = document.createElement('div');
  app.id = 'app';
  document.body.appendChild(app);
  await renderLogin(app, {});
  vi.clearAllMocks();
});

describe('Login page rendering', () => {
  it('renders an email input', () => {
    expect(app.querySelector('#email')).not.toBeNull();
  });

  it('renders a password input', () => {
    expect(app.querySelector('#password')).not.toBeNull();
  });

  it('renders a Sign In button', () => {
    expect(app.querySelector('#login-btn')).not.toBeNull();
  });
});

describe('Login page validation', () => {
  it('does not call login() when email is empty', async () => {
    app.querySelector('#email').value = '';
    app.querySelector('#password').value = 'password123';
    app.querySelector('#login-btn').click();
    expect(login).not.toHaveBeenCalled();
  });

  it('does not call login() when password is empty', async () => {
    app.querySelector('#email').value = 'a@b.com';
    app.querySelector('#password').value = '';
    app.querySelector('#login-btn').click();
    expect(login).not.toHaveBeenCalled();
  });
});

describe('Login page success flow', () => {
  it('calls login() with email and password', async () => {
    const user = { id: 1, username: 'alice' };
    login.mockResolvedValue(user);
    app.querySelector('#email').value = 'alice@example.com';
    app.querySelector('#password').value = 'secret123';
    app.querySelector('#login-btn').click();
    await vi.waitFor(() => expect(login).toHaveBeenCalledWith('alice@example.com', 'secret123'));
  });

  it('calls setSession() with the returned user on success', async () => {
    const user = { id: 1, username: 'alice' };
    login.mockResolvedValue(user);
    app.querySelector('#email').value = 'alice@example.com';
    app.querySelector('#password').value = 'secret123';
    app.querySelector('#login-btn').click();
    await vi.waitFor(() => expect(setSession).toHaveBeenCalledWith(user));
  });

  it('redirects to #/rooms on success', async () => {
    login.mockResolvedValue({ id: 1 });
    app.querySelector('#email').value = 'a@b.com';
    app.querySelector('#password').value = 'pass';
    app.querySelector('#login-btn').click();
    await vi.waitFor(() => expect(window.location.hash).toBe('#/rooms'));
  });
});

describe('Login page error flow', () => {
  it('displays an error message when login() rejects', async () => {
    login.mockRejectedValue(new Error('Invalid credentials'));
    app.querySelector('#email').value = 'a@b.com';
    app.querySelector('#password').value = 'wrongpass';
    app.querySelector('#login-btn').click();
    await vi.waitFor(() => {
      const err = app.querySelector('#form-error');
      expect(err.textContent).toContain('Invalid credentials');
    });
  });
});
```

---

### 25.14 `src/pages/register.test.js`

```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderRegister } from './register.js';

vi.mock('../api/auth.js', () => ({ register: vi.fn() }));

import { register } from '../api/auth.js';

let app;
beforeEach(async () => {
  app = document.createElement('div');
  document.body.appendChild(app);
  await renderRegister(app, {});
  vi.clearAllMocks();
});

function fillForm({ email = '', username = '', password = '', confirm = '' }) {
  app.querySelector('#email').value = email;
  app.querySelector('#username').value = username;
  app.querySelector('#password').value = password;
  app.querySelector('#confirm-password').value = confirm;
}

describe('Register page validation', () => {
  it('shows error when email format is invalid', async () => {
    fillForm({ email: 'notanemail', username: 'alice', password: 'pass1234', confirm: 'pass1234' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(app.querySelector('#email-error').textContent).not.toBe(''));
    expect(register).not.toHaveBeenCalled();
  });

  it('shows error when username is too short', async () => {
    fillForm({ email: 'a@b.com', username: 'ab', password: 'pass1234', confirm: 'pass1234' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(app.querySelector('#username-error').textContent).not.toBe(''));
    expect(register).not.toHaveBeenCalled();
  });

  it('shows error when username contains invalid characters', async () => {
    fillForm({ email: 'a@b.com', username: 'alice!', password: 'pass1234', confirm: 'pass1234' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(app.querySelector('#username-error').textContent).not.toBe(''));
    expect(register).not.toHaveBeenCalled();
  });

  it('shows error when password is under 8 characters', async () => {
    fillForm({ email: 'a@b.com', username: 'alice', password: 'short', confirm: 'short' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(app.querySelector('#password-error').textContent).not.toBe(''));
    expect(register).not.toHaveBeenCalled();
  });

  it('shows error when passwords do not match', async () => {
    fillForm({ email: 'a@b.com', username: 'alice', password: 'pass1234', confirm: 'different' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(app.querySelector('#confirm-error').textContent).not.toBe(''));
    expect(register).not.toHaveBeenCalled();
  });

  it('calls register() when all fields are valid', async () => {
    register.mockResolvedValue({});
    fillForm({ email: 'a@b.com', username: 'alice', password: 'pass1234', confirm: 'pass1234' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(register).toHaveBeenCalledWith('a@b.com', 'alice', 'pass1234'));
  });

  it('redirects to #/login after successful registration', async () => {
    register.mockResolvedValue({});
    fillForm({ email: 'a@b.com', username: 'alice', password: 'pass1234', confirm: 'pass1234' });
    app.querySelector('#register-btn').click();
    await vi.waitFor(() => expect(window.location.hash).toBe('#/login'));
  });
});
```

---

### 25.15 `src/components/messageList.test.js`

`messageList.js` must export `createMessageEl(msg, currentUserId, isAdmin)` as a named export for testability.

```javascript
import { describe, it, expect } from 'vitest';
import { createMessageEl } from './messageList.js';

const baseMsg = {
  id: '1',
  authorId: '10',
  authorUsername: 'alice',
  text: 'Hello world',
  sentAt: '2026-04-08T10:00:00Z',
  edited: false,
  replyTo: null,
  attachments: [],
};

describe('createMessageEl()', () => {
  it('returns an HTMLElement with the correct data-msg-id', () => {
    const el = createMessageEl(baseMsg, '99', false);
    expect(el.dataset.msgId).toBe('1');
    expect(el.id).toBe('msg-1');
  });

  it('adds "own" class when the message belongs to the current user', () => {
    const el = createMessageEl(baseMsg, '10', false);
    expect(el.classList.contains('own')).toBe(true);
  });

  it('does NOT add "own" class for other users\' messages', () => {
    const el = createMessageEl(baseMsg, '99', false);
    expect(el.classList.contains('own')).toBe(false);
  });

  it('renders the message text safely using textContent (XSS check)', () => {
    const xssMsg = { ...baseMsg, text: '<script>alert(1)</script>' };
    const el = createMessageEl(xssMsg, '99', false);
    const textNode = el.querySelector('.message-text');
    expect(textNode.innerHTML).toBe('&lt;script&gt;alert(1)&lt;/script&gt;');
  });

  it('renders the author username safely', () => {
    const msg = { ...baseMsg, authorUsername: '<b>hacker</b>' };
    const el = createMessageEl(msg, '99', false);
    const author = el.querySelector('.msg-author');
    expect(author.innerHTML).toBe('&lt;b&gt;hacker&lt;/b&gt;');
  });

  it('shows the "(edited)" label when message is edited', () => {
    const editedMsg = { ...baseMsg, edited: true };
    const el = createMessageEl(editedMsg, '99', false);
    const label = el.querySelector('.edited-label');
    expect(label.classList.contains('hidden')).toBe(false);
  });

  it('hides the "(edited)" label for non-edited messages', () => {
    const el = createMessageEl(baseMsg, '99', false);
    expect(el.querySelector('.edited-label').classList.contains('hidden')).toBe(true);
  });

  it('renders a reply quote block when replyTo is present', () => {
    const msgWithReply = {
      ...baseMsg,
      replyTo: { id: '0', authorUsername: 'bob', text: 'Original message' },
    };
    const el = createMessageEl(msgWithReply, '99', false);
    expect(el.querySelector('.reply-quote')).not.toBeNull();
    expect(el.querySelector('.reply-quote').dataset.refId).toBe('0');
  });

  it('does NOT render a reply quote block when replyTo is null', () => {
    const el = createMessageEl(baseMsg, '99', false);
    expect(el.querySelector('.reply-quote')).toBeNull();
  });

  it('shows the Delete button for the message author', () => {
    const el = createMessageEl(baseMsg, '10', false); // currentUser is author
    const deleteBtn = el.querySelector('.btn-delete');
    expect(deleteBtn).not.toBeNull();
    expect(deleteBtn.classList.contains('hidden')).toBe(false);
  });

  it('shows the Delete button for an admin even on others\' messages', () => {
    const el = createMessageEl(baseMsg, '99', true); // isAdmin = true
    expect(el.querySelector('.btn-delete').classList.contains('hidden')).toBe(false);
  });

  it('hides the Delete button for non-owner, non-admin users', () => {
    const el = createMessageEl(baseMsg, '99', false);
    expect(el.querySelector('.btn-delete').classList.contains('hidden')).toBe(true);
  });

  it('shows the Edit button only for own messages', () => {
    const own = createMessageEl(baseMsg, '10', false);
    const other = createMessageEl(baseMsg, '99', false);
    expect(own.querySelector('.btn-edit').classList.contains('hidden')).toBe(false);
    expect(other.querySelector('.btn-edit').classList.contains('hidden')).toBe(true);
  });

  it('renders an inline image for image attachments', () => {
    const msgWithImage = {
      ...baseMsg,
      attachments: [{ id: 'att1', type: 'image', filename: 'photo.jpg', size: 1024 }],
    };
    const el = createMessageEl(msgWithImage, '99', false);
    const img = el.querySelector('.inline-img');
    expect(img).not.toBeNull();
    expect(img.src).toContain('/api/attachments/att1');
  });

  it('renders a download link for non-image attachments', () => {
    const msgWithFile = {
      ...baseMsg,
      attachments: [{ id: 'att2', type: 'file', filename: 'report.pdf', size: 2048 }],
    };
    const el = createMessageEl(msgWithFile, '99', false);
    expect(el.querySelector('.file-attachment')).not.toBeNull();
    expect(el.querySelector('.btn-download')).not.toBeNull();
  });
});
```

---

### 25.16 `src/components/messageInput.test.js`

`messageInput.js` must export `getByteLength(str)` as a named export.

```javascript
import { describe, it, expect } from 'vitest';
import { getByteLength } from './messageInput.js';

describe('getByteLength()', () => {
  it('returns 0 for an empty string', () => {
    expect(getByteLength('')).toBe(0);
  });

  it('returns correct byte count for ASCII text', () => {
    expect(getByteLength('hello')).toBe(5);
  });

  it('counts multi-byte UTF-8 characters correctly', () => {
    // Polish ą = 2 bytes in UTF-8
    expect(getByteLength('ą')).toBe(2);
    // Emoji 😊 = 4 bytes in UTF-8
    expect(getByteLength('😊')).toBe(4);
  });

  it('returns 3072 for a string that is exactly 3 KB', () => {
    const str = 'a'.repeat(3072);
    expect(getByteLength(str)).toBe(3072);
  });

  it('returns a value > 3072 for a string over the limit', () => {
    const str = 'a'.repeat(3073);
    expect(getByteLength(str)).toBe(3073);
  });
});
```

---

### 25.17 `src/api/rooms.test.js`

```javascript
import { describe, it, expect, vi } from 'vitest';

vi.mock('./http.js', () => ({
  get:   vi.fn(),
  post:  vi.fn(),
  patch: vi.fn(),
  del:   vi.fn(),
}));

import { get, post, patch, del } from './http.js';
import {
  listJoinedRooms, searchRooms, createRoom, joinRoom, leaveRoom,
  banMember, unbanMember, promoteAdmin, demoteAdmin, inviteToRoom,
  deleteRoom, markRoomRead,
} from './rooms.js';

describe('rooms API', () => {
  it('listJoinedRooms calls GET /rooms/joined', () => {
    listJoinedRooms();
    expect(get).toHaveBeenCalledWith('/rooms/joined');
  });

  it('searchRooms encodes the query parameter', () => {
    searchRooms('hello world');
    expect(get).toHaveBeenCalledWith('/rooms?search=hello%20world');
  });

  it('createRoom posts to /rooms with the data payload', () => {
    createRoom({ name: 'General', visibility: 'public' });
    expect(post).toHaveBeenCalledWith('/rooms', { name: 'General', visibility: 'public' });
  });

  it('joinRoom posts to /rooms/:id/join', () => {
    joinRoom('42');
    expect(post).toHaveBeenCalledWith('/rooms/42/join');
  });

  it('leaveRoom posts to /rooms/:id/leave', () => {
    leaveRoom('42');
    expect(post).toHaveBeenCalledWith('/rooms/42/leave');
  });

  it('banMember calls DELETE /rooms/:roomId/members/:userId', () => {
    banMember('10', '99');
    expect(del).toHaveBeenCalledWith('/rooms/10/members/99');
  });

  it('unbanMember calls DELETE /rooms/:roomId/bans/:userId', () => {
    unbanMember('10', '99');
    expect(del).toHaveBeenCalledWith('/rooms/10/bans/99');
  });

  it('promoteAdmin calls POST /rooms/:roomId/admins/:userId', () => {
    promoteAdmin('10', '99');
    expect(post).toHaveBeenCalledWith('/rooms/10/admins/99');
  });

  it('demoteAdmin calls DELETE /rooms/:roomId/admins/:userId', () => {
    demoteAdmin('10', '99');
    expect(del).toHaveBeenCalledWith('/rooms/10/admins/99');
  });

  it('inviteToRoom calls POST with username payload', () => {
    inviteToRoom('10', 'bob');
    expect(post).toHaveBeenCalledWith('/rooms/10/invitations', { username: 'bob' });
  });

  it('deleteRoom calls DELETE /rooms/:id', () => {
    deleteRoom('5');
    expect(del).toHaveBeenCalledWith('/rooms/5');
  });

  it('markRoomRead calls POST /rooms/:id/read', () => {
    markRoomRead('5');
    expect(post).toHaveBeenCalledWith('/rooms/5/read');
  });
});
```

---

### 25.18 `src/api/messages.test.js`

```javascript
import { describe, it, expect, vi } from 'vitest';

vi.mock('./http.js', () => ({ get: vi.fn(), post: vi.fn(), patch: vi.fn(), del: vi.fn() }));

import { get, post, patch, del } from './http.js';
import {
  getRoomMessages, getDmMessages, getNewRoomMessages, getNewDmMessages,
  sendRoomMessage, sendDmMessage, editMessage, deleteMessage,
} from './messages.js';

describe('messages API', () => {
  it('getRoomMessages with no before param fetches from the beginning', () => {
    getRoomMessages('1', null, 50);
    expect(get).toHaveBeenCalledWith('/rooms/1/messages?limit=50');
  });

  it('getRoomMessages with before param includes it in the query string', () => {
    getRoomMessages('1', '100', 50);
    expect(get).toHaveBeenCalledWith('/rooms/1/messages?before=100&limit=50');
  });

  it('getDmMessages with no before param fetches from the beginning', () => {
    getDmMessages('5', null, 50);
    expect(get).toHaveBeenCalledWith('/dms/5/messages?limit=50');
  });

  it('getNewRoomMessages uses after param', () => {
    getNewRoomMessages('1', '200');
    expect(get).toHaveBeenCalledWith('/rooms/1/messages?after=200&limit=50');
  });

  it('getNewDmMessages uses after param', () => {
    getNewDmMessages('5', '300');
    expect(get).toHaveBeenCalledWith('/dms/5/messages?after=300&limit=50');
  });

  it('sendRoomMessage posts to /rooms/:id/messages', () => {
    sendRoomMessage('1', { text: 'hi', attachmentIds: [] });
    expect(post).toHaveBeenCalledWith('/rooms/1/messages', { text: 'hi', attachmentIds: [] });
  });

  it('sendDmMessage posts to /dms/:id/messages', () => {
    sendDmMessage('5', { text: 'hello' });
    expect(post).toHaveBeenCalledWith('/dms/5/messages', { text: 'hello' });
  });

  it('editMessage patches /messages/:id with new text', () => {
    editMessage('42', 'updated text');
    expect(patch).toHaveBeenCalledWith('/messages/42', { text: 'updated text' });
  });

  it('deleteMessage calls DELETE /messages/:id', () => {
    deleteMessage('42');
    expect(del).toHaveBeenCalledWith('/messages/42');
  });
});
```

---

### 25.19 `src/components/sidebar.test.js`

`sidebar.js` must export `renderRoomItem(room, activeRoomId)` and `renderContactItem(contact, presenceMap)` as named exports for unit testing.

```javascript
import { describe, it, expect } from 'vitest';
import { renderRoomItem, renderContactItem } from './sidebar.js';

describe('renderRoomItem()', () => {
  it('renders a list item with the room name', () => {
    const el = renderRoomItem({ id: '1', name: 'general' }, null);
    expect(el.tagName).toBe('LI');
    expect(el.querySelector('.room-name').textContent).toBe('# general');
  });

  it('adds "active" class when the room is the active one', () => {
    const el = renderRoomItem({ id: '1', name: 'general' }, '1');
    expect(el.classList.contains('active')).toBe(true);
  });

  it('does NOT add "active" class for other rooms', () => {
    const el = renderRoomItem({ id: '1', name: 'general' }, '2');
    expect(el.classList.contains('active')).toBe(false);
  });

  it('sets data-room-id attribute', () => {
    const el = renderRoomItem({ id: '42', name: 'dev' }, null);
    expect(el.dataset.roomId).toBe('42');
  });

  it('shows unread badge when unread count > 0', () => {
    const el = renderRoomItem({ id: '1', name: 'general', unread: 3 }, null);
    const badge = el.querySelector('.badge');
    expect(badge.classList.contains('hidden')).toBe(false);
    expect(badge.textContent).toBe('3');
  });

  it('hides unread badge when unread count is 0', () => {
    const el = renderRoomItem({ id: '1', name: 'general', unread: 0 }, null);
    expect(el.querySelector('.badge').classList.contains('hidden')).toBe(true);
  });
});

describe('renderContactItem()', () => {
  const contact = { id: '5', username: 'alice', unread: 0 };

  it('renders a list item with the contact username', () => {
    const el = renderContactItem(contact, {});
    expect(el.querySelector('.contact-name').textContent).toBe('alice');
  });

  it('sets data-user-id attribute', () => {
    const el = renderContactItem(contact, {});
    expect(el.dataset.userId).toBe('5');
  });

  it('shows correct presence dot class for "online" status', () => {
    const el = renderContactItem(contact, { '5': 'online' });
    expect(el.querySelector('.presence-dot').classList.contains('online')).toBe(true);
  });

  it('shows correct presence dot class for "afk" status', () => {
    const el = renderContactItem(contact, { '5': 'afk' });
    expect(el.querySelector('.presence-dot').classList.contains('afk')).toBe(true);
  });

  it('defaults to "offline" when no presence data is available', () => {
    const el = renderContactItem(contact, {});
    expect(el.querySelector('.presence-dot').classList.contains('offline')).toBe(true);
  });
});
```

---

### 25.20 Running Tests

```bash
# Run all tests once
npm test

# Run in watch mode during development
npm run test:watch

# Generate coverage report (outputs to ./coverage/)
npm run test:coverage
```

### 25.20 Running Tests

```bash
# Run all tests once
npm test

# Run in watch mode during development
npm run test:watch

# Generate coverage report (outputs to ./coverage/)
npm run test:coverage
```

Coverage thresholds (80 % lines/functions/statements, 75 % branches) are enforced automatically — the `npm test` command will exit with a non-zero code if any threshold is not met. Thresholds are configured in the `test.coverage.thresholds` block in `vite.config.js` (section 20).

---

## 26. Deployment

### 26.1 How the Frontend Is Served

The frontend is **served by Spring Boot as static files**. There is no separate frontend server in production. Visiting `http://localhost:8080` (or the deployed URL) loads the frontend directly from Spring Boot.

This works because:
- `npm run build` compiles the frontend into `chat/src/main/resources/static/` (configured in `vite.config.js`).
- Spring Boot automatically serves everything in `src/main/resources/static/` as static HTTP resources.
- The frontend uses **hash-based routing** (`#/login`, `#/room/42`), so the browser never requests those paths from the server — it only ever fetches `/index.html` and handles all navigation itself. No Spring Boot controller or fallback configuration is needed.
- All API calls use `/api/...` paths, which Spring Boot handles via its normal REST controllers.

### 26.2 Directory Layout (Monorepo)

Place the frontend folder next to the backend folder:

```
project-root/
├── frontend/          # Vite project (this document)
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── chat/              # Spring Boot project (Maven)
│   ├── src/
│   │   └── main/
│   │       └── resources/
│   │           └── static/    ← `npm run build` writes here
│   └── pom.xml
├── Dockerfile
└── docker-compose.yml
```

### 26.3 Workflow

**During development (hot reload):**
```bash
# Terminal 1 — backend
cd chat && ./mvnw spring-boot:run

# Terminal 2 — frontend dev server with proxy
cd frontend && npm run dev
# Visit http://localhost:5173
```

**Production build (single port):**
```bash
cd frontend && npm run build   # writes to ../chat/src/main/resources/static/
cd ../chat  && ./mvnw package  # packages everything into a JAR
java -jar target/chat-*.jar
# Visit http://localhost:8080
```

### 26.4 `Dockerfile`

Multi-stage build: Node builds the frontend, Maven packages the backend, the final image runs only the JRE.

```dockerfile
# ── Stage 1: build frontend ──────────────────────────────────────────────────
FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
# Output lands in /frontend/../chat/src/main/resources/static
# because outDir is set to '../chat/src/main/resources/static' in vite.config.js.
# We copy the result explicitly in stage 2 instead.

# ── Stage 2: build backend ───────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY chat/ ./
# Copy the compiled frontend into Spring Boot's static resources folder
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests

# ── Stage 3: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
# Persistent file storage (attachments) — mount a volume here
RUN mkdir -p /app/uploads
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Note on `outDir`:** Because Docker builds in an isolated context, the Vite `outDir: '../chat/...'` path does not apply inside the container. Stage 1 builds into `/frontend/dist` by default when the relative path cannot resolve. Stage 2 then copies from that location explicitly with `COPY --from=frontend /frontend/dist`. To avoid this confusion you can override `outDir` via an environment variable or simply accept the two-step copy shown above.

A cleaner approach is to set `outDir` conditionally in `vite.config.js`:

```javascript
const outDir = process.env.VITE_OUT_DIR ?? '../chat/src/main/resources/static';

export default defineConfig({
  build: { outDir, emptyOutDir: true },
  // ...
});
```

Then in the Dockerfile Stage 1:
```dockerfile
RUN VITE_OUT_DIR=dist npm run build
```
This keeps the local dev build writing to the Spring Boot folder while Docker writes to `dist` for the explicit `COPY`.

### 26.5 `docker-compose.yml`

```yaml
services:
  app:
    build:
      context: ../../../..
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    volumes:
      # Persistent storage for uploaded files (section 3.4 of the spec)
      - uploads:/app/uploads
    environment:
      - SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/chat
    restart: unless-stopped

volumes:
  uploads:
```

**To build and start:**
```bash
docker compose up --build
# Visit http://localhost:8080
```

**To stop:**
```bash
docker compose down
```

Uploaded files survive container restarts because they are stored in the named `uploads` volume.

---

## 27. Non-Goals

The following are explicitly **not** required:

- WebSocket or Server-Sent Events.
- Any JavaScript framework or library (beyond `emoji-picker-element`).
- Mobile / responsive layout (desktop ≥ 1024 px only).
- End-to-end encryption.
- Typing indicators.
- Emoji reactions.
- Read receipts per-message.
- Dark/light mode toggle (dark is the only theme).
- Internationalisation (i18n).
