import { store } from '../state/store.js';
import { getMessages, sendMessage, uploadAttachment } from '../api/messages.js';
import { formatDate, formatFileSize, escapeHtml } from '../utils/format.js';
import { showToast } from './toast.js';

const POLL_INTERVAL = 3000;

let pollTimer = null;

/**
 * Render the message area (list + input) into the center-area div.
 */
export function renderMessageArea(container) {
  container.innerHTML = '';

  const { activeRoom } = store.getState();

  if (!activeRoom) {
    container.innerHTML = `
      <div class="welcome-screen">
        <div class="welcome-screen__icon">💬</div>
        <div class="welcome-screen__title">Welcome to ChatApp</div>
        <p>Select a room from the side panel to start chatting.</p>
      </div>
    `;
    return;
  }

  // Room header
  const header = document.createElement('div');
  header.className = 'room-header';
  header.innerHTML = `
    <span style="font-size:20px">💬</span>
    <span class="room-header__name">${escapeHtml(activeRoom.name)}</span>
    ${activeRoom.description ? `<span class="room-header__desc">${escapeHtml(activeRoom.description)}</span>` : ''}
  `;

  // Message list
  const msgList = document.createElement('div');
  msgList.className = 'message-list';
  msgList.id = 'message-list';

  // Input area
  const inputArea = buildInputArea(activeRoom);

  container.appendChild(header);
  container.appendChild(msgList);
  container.appendChild(inputArea);

  // Initial load
  loadMessages(msgList, activeRoom.id, true);

  // Start polling
  clearInterval(pollTimer);
  pollTimer = setInterval(() => {
    loadMessages(msgList, activeRoom.id, false);
  }, POLL_INTERVAL);

  // Stop polling when room changes
  const unsub = store.subscribe((state) => {
    if (!state.activeRoom || state.activeRoom.id !== activeRoom.id) {
      clearInterval(pollTimer);
      unsub();
    }
  });
}

async function loadMessages(container, roomId, scrollToBottom) {
  try {
    const data = await getMessages({ chatRoomId: roomId, size: 50, sort: 'createdAt,asc' });
    const messages = data?.content || data || [];

    if (scrollToBottom || messages.length !== container.querySelectorAll('.message-group').length) {
      renderMessages(container, messages);
      if (scrollToBottom) {
        container.scrollTop = container.scrollHeight;
      } else {
        const wasAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 80;
        renderMessages(container, messages);
        if (wasAtBottom) container.scrollTop = container.scrollHeight;
      }
    }
  } catch {
    // silently fail for polling
  }
}

function renderMessages(container, messages) {
  const { user } = store.getState();
  const wasAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 80;
  const prevCount = container.querySelectorAll('.message-item').length;

  if (messages.length === prevCount) return; // No change

  container.innerHTML = '';

  if (!messages.length) {
    container.innerHTML = `
      <div class="message-empty">
        <div style="font-size:32px">🌟</div>
        <div>No messages yet — be the first!</div>
      </div>
    `;
    return;
  }

  // Group consecutive messages by same sender
  let currentSenderId = null;
  let currentGroup = null;

  messages.forEach(msg => {
    const isOwn = msg.sender?.id === user?.id;
    const senderId = msg.sender?.id;

    if (senderId !== currentSenderId) {
      currentGroup = document.createElement('div');
      currentGroup.className = 'message-group';
      currentSenderId = senderId;

      if (!isOwn) {
        const senderLabel = document.createElement('div');
        senderLabel.className = 'message-item__sender';
        senderLabel.style.marginLeft = '46px';
        senderLabel.textContent = msg.sender?.displayName || msg.sender?.username || 'Unknown';
        currentGroup.appendChild(senderLabel);
      }

      container.appendChild(currentGroup);
    }

    const item = buildMessageItem(msg, isOwn);
    currentGroup.appendChild(item);
  });

  if (wasAtBottom) container.scrollTop = container.scrollHeight;
}

function buildMessageItem(msg, isOwn) {
  const wrapper = document.createElement('div');
  wrapper.className = `message-item ${isOwn ? 'message-item--own' : 'message-item--other'}`;

  const bubble = document.createElement('div');
  bubble.className = 'message-item__bubble';

  if (msg.deleted) {
    bubble.innerHTML = `<em style="color:var(--color-text-muted)">Message deleted</em>`;
  } else {
    bubble.innerHTML = escapeHtml(msg.content).replace(/\n/g, '<br>');
  }

  const meta = document.createElement('div');
  meta.className = 'message-item__meta';
  meta.textContent = formatDate(msg.createdAt);
  if (msg.edited) {
    const editedLabel = document.createElement('span');
    editedLabel.textContent = '(edited)';
    meta.appendChild(editedLabel);
  }

  const inner = document.createElement('div');
  inner.style.display = 'flex';
  inner.style.flexDirection = 'column';
  inner.style.maxWidth = '100%';
  inner.appendChild(bubble);
  inner.appendChild(meta);

  wrapper.appendChild(inner);
  return wrapper;
}

function buildInputArea(room) {
  const { user } = store.getState();
  let pendingFile = null;

  const area = document.createElement('div');
  area.className = 'message-input-area';

  area.innerHTML = `
    <div id="attachment-preview-wrap" style="display:none;width:100%;position:absolute;bottom:100%;left:0;padding:0 16px 4px">
      <div class="attachment-preview" id="attachment-preview">
        <span>📎</span>
        <span class="attachment-preview__name" id="attachment-filename"></span>
        <button class="attachment-preview__remove" id="attachment-remove" title="Remove">✕</button>
      </div>
    </div>

    <label class="input-btn" title="Attach file" style="cursor:pointer">
      📎
      <input type="file" id="file-input" style="display:none" />
    </label>

    <textarea
      class="message-input"
      id="message-input"
      placeholder="Type a message..."
      rows="1"
    ></textarea>

    <button class="input-btn" id="emoji-btn" title="Emoji">😊</button>
    <button class="input-btn input-btn--send" id="send-btn" title="Send">➤</button>
  `;

  const input = area.querySelector('#message-input');
  const sendBtn = area.querySelector('#send-btn');
  const emojiBtn = area.querySelector('#emoji-btn');
  const fileInput = area.querySelector('#file-input');
  const previewWrap = area.querySelector('#attachment-preview-wrap');
  const previewName = area.querySelector('#attachment-filename');
  const removeBtn = area.querySelector('#attachment-remove');

  // Auto-resize textarea
  input.addEventListener('input', () => {
    input.style.height = 'auto';
    input.style.height = Math.min(input.scrollHeight, 120) + 'px';
  });

  // Send on Enter (Shift+Enter = newline)
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      doSend();
    }
  });

  sendBtn.addEventListener('click', doSend);

  // Emoji picker
  let emojiPicker = null;
  emojiBtn.addEventListener('click', () => {
    if (emojiPicker) {
      emojiPicker.remove();
      emojiPicker = null;
      return;
    }
    import('emoji-picker-element').then(() => {
      emojiPicker = document.createElement('emoji-picker');
      emojiPicker.classList.add('emoji-picker-wrapper');
      emojiPicker.style.cssText = 'position:absolute;bottom:72px;right:16px;z-index:100';
      area.appendChild(emojiPicker);

      emojiPicker.addEventListener('emoji-click', (e) => {
        const emoji = e.detail.unicode;
        const start = input.selectionStart;
        const end = input.selectionEnd;
        input.value = input.value.slice(0, start) + emoji + input.value.slice(end);
        input.selectionStart = input.selectionEnd = start + emoji.length;
        input.focus();
      });

      document.addEventListener('click', (e) => {
        if (!emojiPicker.contains(e.target) && e.target !== emojiBtn) {
          emojiPicker.remove();
          emojiPicker = null;
        }
      }, { once: true });
    });
  });

  // File attachment
  fileInput.addEventListener('change', () => {
    const file = fileInput.files[0];
    if (file) {
      pendingFile = file;
      previewName.textContent = `${file.name} (${formatFileSize(file.size)})`;
      previewWrap.style.display = 'block';
    }
  });

  removeBtn.addEventListener('click', () => {
    pendingFile = null;
    fileInput.value = '';
    previewWrap.style.display = 'none';
  });

  async function doSend() {
    const content = input.value.trim();
    if (!content && !pendingFile) return;

    sendBtn.disabled = true;
    try {
      let messageId = null;

      if (content) {
        const msg = await sendMessage({
          senderId: user.id,
          chatRoomId: room.id,
          content,
        });
        messageId = msg?.id;
        input.value = '';
        input.style.height = 'auto';
      }

      if (pendingFile && messageId) {
        const fd = new FormData();
        fd.append('file', pendingFile);
        fd.append('messageId', messageId);
        fd.append('fileName', pendingFile.name);
        fd.append('fileType', pendingFile.type);
        fd.append('fileSize', pendingFile.size);
        await uploadAttachment(fd);
        pendingFile = null;
        fileInput.value = '';
        previewWrap.style.display = 'none';
      } else if (pendingFile && !messageId) {
        // Send with placeholder content
        const msg = await sendMessage({
          senderId: user.id,
          chatRoomId: room.id,
          content: `📎 ${pendingFile.name}`,
        });
        const fd = new FormData();
        fd.append('file', pendingFile);
        fd.append('messageId', msg.id);
        fd.append('fileName', pendingFile.name);
        fd.append('fileType', pendingFile.type);
        fd.append('fileSize', pendingFile.size);
        await uploadAttachment(fd);
        pendingFile = null;
        fileInput.value = '';
        previewWrap.style.display = 'none';
      }

      // Immediately refresh messages
      const msgList = document.getElementById('message-list');
      if (msgList) {
        const { activeRoom } = store.getState();
        if (activeRoom) await loadMessages(msgList, activeRoom.id, true);
      }
    } catch (e) {
      showToast('Failed to send message: ' + e.message, 'error');
    } finally {
      sendBtn.disabled = false;
      input.focus();
    }
  }

  return area;
}

export function stopMessagePolling() {
  clearInterval(pollTimer);
}
