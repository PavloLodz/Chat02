# Frontend Requirements — Classic Web Chat Application

## 1. Overview
The frontend is a single-page application (SPA) providing a modern, responsive web chat experience. It integrates with the Spring Boot backend and is served by the same application.

### Key Features:
- Classic web chat layout with a top menu, center message area, bottom input, and a side panel.
- Real-time updates via short polling.
- Room and contact management.
- Dynamic side layout with room members and online status.

## 2. Technology Stack

| Concern | Choice |
|---|---|
| Language | Vanilla JavaScript (ES2022+, ESModules) |
| Build tool | Vite (for dev server, bundling, and proxy) |
| Styling | Plain CSS (modularized per component/page) |
| Routing | Hash-based (`#/login`, `#/rooms`, `#/room/:id`) |
| Real-time | Short polling via `setInterval` + `fetch` |
| Emoji picker | `emoji-picker-element` (web component) |
| Testing | Vitest + jsdom (Unit tests) |
| Linting | ESLint with recommended rules |

No UI frameworks (React, Vue, Angular) or heavy libraries are allowed.

## 3. Layout and UI/UX Requirements

The application follows a typical web chat layout with a focus on usability and responsiveness.

### 3.1 Main Layout Structure
- **Top Menu:** Persistent navigation bar for user profile, settings, and logout.
- **Center Area:** Main message history area that scrolls as new messages arrive.
- **Bottom Area:** Sticky message input field with support for text, emojis, and file attachments.
- **Side Panel (Right):** Displays rooms, contacts, and room members.

### 3.2 Side Layout Behavior
- **Initial View:** Displays a list of available rooms and the user's contacts.
- **Active Room View:** 
  - Once a user enters a room, the room list becomes compacted into an **accordion style** to save space.
  - The side panel expands to show **Room Members** currently in the active room.
  - Member list includes **Online Status** indicators (e.g., green for online, gray for offline).

## 4. Project Structure

The frontend code resides in a `frontend/` directory within the project root.

```
frontend/
├── public/              # Static assets (favicons, etc.)
├── src/
│   ├── api/             # API clients (auth, rooms, messages, etc.)
│   ├── components/      # Reusable UI components (topMenu, sidebar, etc.)
│   ├── pages/           # Page-level containers and logic
│   ├── state/           # Global state management (store.js)
│   ├── utils/           # Helper functions and logic
│   ├── style.css        # Global CSS and variables
│   └── main.js          # Entry point and router initialization
├── index.html           # Main entry HTML file
├── package.json         # NPM dependencies and scripts
├── vite.config.js       # Vite configuration (proxy to backend)
└── vitest.config.js     # Vitest configuration for unit testing
```

## 5. Functional Requirements

### 5.1 Authentication
- Login and Registration pages.
- JWT storage in `localStorage` or `sessionStorage`.
- Route protection (redirect to login if unauthenticated).

### 5.2 Room Management
- List available public rooms.
- Search/Filter rooms.
- Join/Leave room functionality.
- Compacted accordion view for rooms when inside a room.

### 5.3 Messaging
- Display message history for the active room.
- Real-time polling for new messages (configurable interval).
- Send message functionality (Text + Emojis).
- File attachment support (upload/download).

### 5.4 Contacts and Presence
- List user's contacts.
- Show online/offline status for contacts and room members.
- Presence detection (automatic status update based on activity).

## 6. Testing Requirements

- The project **must support unit tests**.
- Unit tests should be co-located with the code they test (e.g., `utils/formatDate.test.js`) or in a dedicated `test/` folder.
- Run tests using `npm test` or `vitest`.
- All utility functions, state transitions, and API wrappers should have test coverage.

## 7. Build and Integration

- The frontend is built using `vite build`.
- The build output (static files) must be integrated into the Spring Boot project's `src/main/resources/static` or served via a multi-stage Docker build.
- The `pom.xml` should be configured to optionally build the frontend during the Maven lifecycle.
