# Project Improvement Plan

## 1. Project Setup and Infrastructure
1. Initialize the `frontend/` directory with a standard Vite project structure.
2. Configure `package.json` with necessary dependencies:
    - `vite` for building and development.
    - `vitest` and `jsdom` for unit testing.
    - `eslint` for code quality.
    - `emoji-picker-element` for messaging.
3. Configure `vite.config.js` to proxy API requests to the Spring Boot backend (`http://localhost:8080`).
4. Update the root `pom.xml` to include a Maven profile for building the frontend using `frontend-maven-plugin`.

## 2. Core Frontend Architecture
1. Implement a **Hash-based Router** in `src/main.js` to handle navigation between login, registration, and the chat interface.
2. Develop a lightweight **Global State Manager** in `src/state/store.js` for handling JWT, current user session, and active room state.
3. Create a **Shared API Client** in `src/api/http.js` with standard `fetch` wrappers for handling headers and error responses.

## 3. Layout and UI Components
1. **Main Layout Shell:** Create a responsive CSS grid/flexbox layout with Top Menu, Center Area, Bottom Area, and Side Panel.
2. **Top Menu Component:** Implement user profile display, settings link, and logout button.
3. **Side Panel:**
    - Develop the **Room List** with an accordion-style toggle.
    - Implement the **Contacts List** with online status indicators.
    - Create the **Member Panel** to display members of the active room.
4. **Message Area:**
    - **Message List:** Scrollable area for message history.
    - **Message Input:** Sticky input with emoji picker and file attachment support.

## 4. Functional Features
1. **Authentication:** Implement login and registration logic, including JWT storage and route protection.
2. **Room Management:** Connect to backend APIs to list, search, join, and leave rooms.
3. **Messaging:**
    - Implement real-time polling to fetch new messages.
    - Handle sending text messages and file uploads.
4. **Presence and Contacts:**
    - Integrate automatic status updates based on user activity (AFK detection).
    - Display real-time status for contacts and room members.

## 5. Testing and Quality Assurance
1. Establish a **Unit Testing Strategy** using Vitest and jsdom.
2. Write tests for:
    - Utility functions (date formatting, file size, etc.).
    - State management logic.
    - API client wrappers.
    - Navigation and routing logic.
3. Ensure all new code follows the project's **General Principles** and **Code Style** guidelines.

## 6. Deployment and Integration
1. Verify the multi-stage Docker build process.
2. Ensure the built frontend static files are correctly served by the Spring Boot application.
3. Conduct final end-to-end verification of all core chat features.
