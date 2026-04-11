import { store } from '../state/store.js';
import { getRooms, getRoomMembers, joinRoom, leaveRoom, getMembership } from '../api/rooms.js';
import { getAllUsers } from '../api/auth.js';
import { getInitials, truncate, debounce } from '../utils/format.js';
import { showToast } from './toast.js';

/**
 * Render the right side panel.
 * Includes: Room List (accordion), Contacts List, Member Panel.
 */
export function renderSidePanel(container) {
  const panel = document.createElement('div');
  panel.className = 'side-panel';

  // Build sections
  const roomSection = createSection('Rooms', true);
  const contactSection = createSection('Contacts', true);
  const memberSection = createSection('Members', false);

  panel.appendChild(roomSection.el);
  panel.appendChild(contactSection.el);
  panel.appendChild(memberSection.el);

  container.appendChild(panel);

  // Load data
  loadRooms(roomSection.body);
  loadContacts(contactSection.body);

  // Subscribe to active room changes to update member panel
  const unsub = store.subscribe((state) => {
    if (state.activeRoom) {
      memberSection.show();
      loadMembers(memberSection.body, state.activeRoom, state.onlineUsers);
      // Collapse rooms accordion when in a room
      roomSection.collapse();
    } else {
      memberSection.hide();
      roomSection.expand();
    }
    highlightActiveRoom(roomSection.body, state.activeRoom);
  });

  panel._cleanup = unsub;
  return panel;
}

function createSection(title, startOpen) {
  const el = document.createElement('div');
  el.className = 'side-section side-section--flex';

  let open = startOpen;

  const header = document.createElement('div');
  header.className = 'side-section__header';
  header.innerHTML = `
    <span class="side-section__title">${title}</span>
    <span class="side-section__toggle ${open ? 'side-section__toggle--open' : ''}">▼</span>
  `;

  const body = document.createElement('div');
  body.className = `side-section__body ${open ? '' : 'side-section__body--collapsed'}`;

  header.addEventListener('click', () => {
    open = !open;
    body.classList.toggle('side-section__body--collapsed', !open);
    header.querySelector('.side-section__toggle').classList.toggle('side-section__toggle--open', open);
  });

  el.appendChild(header);
  el.appendChild(body);

  return {
    el,
    body,
    collapse() {
      open = false;
      body.classList.add('side-section__body--collapsed');
      header.querySelector('.side-section__toggle').classList.remove('side-section__toggle--open');
    },
    expand() {
      open = true;
      body.classList.remove('side-section__body--collapsed');
      header.querySelector('.side-section__toggle').classList.add('side-section__toggle--open');
    },
    show() { el.classList.remove('hidden'); },
    hide() { el.classList.add('hidden'); },
  };
}

async function loadRooms(container) {
  container.innerHTML = '<div style="padding:12px;color:var(--color-text-muted);font-size:13px">Loading...</div>';
  try {
    // Search box
    const searchBox = document.createElement('div');
    searchBox.className = 'search-box';
    searchBox.innerHTML = `<input class="search-input" placeholder="Search rooms..." id="room-search" />`;
    container.innerHTML = '';
    container.appendChild(searchBox);

    const list = document.createElement('div');
    list.id = 'room-list';
    container.appendChild(list);

    const data = await getRooms({ size: 100 });
    const rooms = data?.content || data || [];

    renderRoomItems(list, rooms);

    const searchInput = searchBox.querySelector('#room-search');
    searchInput.addEventListener('input', debounce(() => {
      const q = searchInput.value.toLowerCase();
      const filtered = rooms.filter(r => r.name.toLowerCase().includes(q) || (r.description || '').toLowerCase().includes(q));
      renderRoomItems(list, filtered);
      const { activeRoom } = store.getState();
      if (activeRoom) highlightActiveRoom(list, activeRoom);
    }, 250));

    const { activeRoom } = store.getState();
    if (activeRoom) highlightActiveRoom(list, activeRoom);
  } catch {
    container.innerHTML = `<div style="padding:12px;color:var(--color-danger);font-size:13px">Failed to load rooms</div>`;
  }
}

function renderRoomItems(list, rooms) {
  list.innerHTML = '';
  if (!rooms.length) {
    list.innerHTML = '<div style="padding:12px;color:var(--color-text-muted);font-size:13px">No rooms found</div>';
    return;
  }
  const { user } = store.getState();

  rooms.forEach(room => {
    const item = document.createElement('div');
    item.className = 'room-item';
    item.dataset.roomId = room.id;
    item.innerHTML = `
      <div class="room-item__icon">${room.name.charAt(0).toUpperCase()}</div>
      <div class="room-item__info">
        <div class="room-item__name">${truncate(room.name, 22)}</div>
        <div class="room-item__desc">${truncate(room.description || (room.isPublic ? 'Public' : 'Private'), 28)}</div>
      </div>
    `;

    item.addEventListener('click', async () => {
      try {
        // Try to find/create membership
        const members = await getMembership({ roomId: room.id, size: 200 });
        const memberList = members?.content || members || [];
        const existing = memberList.find(m => m.userId === user.id);
        if (!existing) {
          await joinRoom(room.id, user.id);
        }
        store.setActiveRoom(room);
      } catch {
        showToast('Failed to join room', 'error');
      }
    });

    list.appendChild(item);
  });
}

function highlightActiveRoom(container, activeRoom) {
  container.querySelectorAll('.room-item').forEach(el => {
    el.classList.toggle('room-item--active', el.dataset.roomId === activeRoom?.id);
  });
}

async function loadContacts(container) {
  container.innerHTML = '<div style="padding:12px;color:var(--color-text-muted);font-size:13px">Loading...</div>';
  try {
    const data = await getAllUsers({ size: 100 });
    const users = data?.content || data || [];
    const { user: currentUser, onlineUsers } = store.getState();

    const contacts = users.filter(u => u.id !== currentUser?.id);

    container.innerHTML = '';
    if (!contacts.length) {
      container.innerHTML = '<div style="padding:12px;color:var(--color-text-muted);font-size:13px">No contacts</div>';
      return;
    }

    contacts.forEach(contact => {
      container.appendChild(createContactItem(contact, onlineUsers));
    });

    // Update contacts list on online status changes
    store.subscribe((state) => {
      container.querySelectorAll('.status-dot').forEach(dot => {
        const uid = dot.dataset.uid;
        if (uid) {
          dot.classList.toggle('status-dot--online', state.onlineUsers.has(uid));
        }
      });
    });
  } catch {
    container.innerHTML = '<div style="padding:12px;color:var(--color-danger);font-size:13px">Failed to load contacts</div>';
  }
}

function createContactItem(user, onlineUsers) {
  const displayName = user.displayName || user.username;
  const isOnline = user.online || onlineUsers?.has(user.id);

  const item = document.createElement('div');
  item.className = 'contact-item';
  item.innerHTML = `
    <div class="avatar avatar--sm">
      ${user.avatarUrl
        ? `<img class="avatar__img" src="${user.avatarUrl}" alt="${displayName}" />`
        : `<div class="avatar__fallback">${getInitials(displayName)}</div>`}
      <span class="status-dot ${isOnline ? 'status-dot--online' : ''}" data-uid="${user.id}"></span>
    </div>
    <div style="flex:1;min-width:0">
      <div style="font-size:13px;font-weight:500;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${displayName}</div>
      <div style="font-size:11px;color:var(--color-text-muted)">${isOnline ? '● Online' : '○ Offline'}</div>
    </div>
  `;
  return item;
}

async function loadMembers(container, room, onlineUsers) {
  container.innerHTML = '<div style="padding:12px;color:var(--color-text-muted);font-size:13px">Loading...</div>';
  try {
    const members = await getRoomMembers(room.id);
    const memberList = members?.content || members || [];
    const allUsers = await getAllUsers({ size: 200 });
    const userMap = {};
    (allUsers?.content || allUsers || []).forEach(u => { userMap[u.id] = u; });

    container.innerHTML = '';

    const { user: currentUser } = store.getState();
    const leaveBtn = document.createElement('button');
    leaveBtn.className = 'btn btn--ghost';
    leaveBtn.style.cssText = 'margin:8px 14px;font-size:12px;padding:6px 12px;width:calc(100% - 28px)';
    leaveBtn.textContent = '🚪 Leave Room';
    leaveBtn.addEventListener('click', async () => {
      try {
        const myMembership = memberList.find(m => m.userId === currentUser.id);
        if (myMembership) {
          await leaveRoom(myMembership.id);
        }
        store.setActiveRoom(null);
      } catch {
        showToast('Failed to leave room', 'error');
      }
    });
    container.appendChild(leaveBtn);

    if (!memberList.length) {
      const empty = document.createElement('div');
      empty.style.cssText = 'padding:12px;color:var(--color-text-muted);font-size:13px';
      empty.textContent = 'No members';
      container.appendChild(empty);
      return;
    }

    memberList.forEach(member => {
      const user = userMap[member.userId];
      if (!user) return;
      const displayName = user.displayName || user.username;
      const isOnline = user.online || onlineUsers?.has(user.id);

      const item = document.createElement('div');
      item.className = 'member-item';
      item.innerHTML = `
        <div class="avatar avatar--sm">
          ${user.avatarUrl
            ? `<img class="avatar__img" src="${user.avatarUrl}" alt="${displayName}" />`
            : `<div class="avatar__fallback">${getInitials(displayName)}</div>`}
          <span class="status-dot ${isOnline ? 'status-dot--online' : ''}"></span>
        </div>
        <div style="flex:1;min-width:0">
          <div style="font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
            ${displayName}${member.userId === currentUser.id ? ' <span style="color:var(--color-text-muted);font-size:11px">(you)</span>' : ''}
          </div>
          <div style="font-size:11px;color:var(--color-text-muted)">${member.role || 'MEMBER'}</div>
        </div>
      `;
      container.appendChild(item);
    });
  } catch {
    container.innerHTML = '<div style="padding:12px;color:var(--color-danger);font-size:13px">Failed to load members</div>';
  }
}
