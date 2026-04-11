import { store } from '../state/store.js';
import { renderTopMenu } from '../components/topMenu.js';
import { renderSidePanel } from '../components/sidePanel.js';
import { renderMessageArea } from '../components/messageArea.js';
import { startPresenceDetection, updateOnlineStatus, fetchOnlineStatuses } from '../utils/presence.js';

const PRESENCE_POLL_INTERVAL = 15000; // 15s

let presencePollTimer = null;
let stopPresence = null;

/**
 * Render the main chat page layout.
 */
export function renderChat(container, navigate) {
  // Cleanup any previous timers
  stopAllPolling();

  container.innerHTML = '';

  const layout = document.createElement('div');
  layout.className = 'app-layout';

  // Top menu
  const topMenuEl = document.createElement('div');
  renderTopMenu(topMenuEl, navigate);

  // Body = center + side panel
  const body = document.createElement('div');
  body.className = 'main-body';

  const centerArea = document.createElement('div');
  centerArea.className = 'center-area';
  centerArea.id = 'center-area';

  const sideEl = document.createElement('div');
  renderSidePanel(sideEl, navigate);

  body.appendChild(centerArea);
  body.appendChild(sideEl);

  layout.appendChild(topMenuEl);
  layout.appendChild(body);
  container.appendChild(layout);

  // Initial message area (welcome screen)
  renderMessageArea(centerArea);

  // Re-render message area whenever active room changes
  store.subscribe(() => {
    renderMessageArea(centerArea);
  });

  // Task 19 & 20: Presence detection + polling
  startPresencePolling();
  stopPresence = startPresenceDetection(
    () => updateOnlineStatus(true),
    () => updateOnlineStatus(false)
  );

  // Mark user online immediately
  updateOnlineStatus(true);
}

function startPresencePolling() {
  clearInterval(presencePollTimer);
  fetchOnlineStatuses();
  presencePollTimer = setInterval(fetchOnlineStatuses, PRESENCE_POLL_INTERVAL);
}

function stopAllPolling() {
  clearInterval(presencePollTimer);
  if (stopPresence) {
    stopPresence();
    stopPresence = null;
  }
}
