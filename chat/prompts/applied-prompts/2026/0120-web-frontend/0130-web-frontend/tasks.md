# Project Tasks

## 1. Project Setup and Infrastructure
1. [x] Initialize the `frontend/` directory with a standard Vite project structure.
2. [x] Configure `package.json` with necessary dependencies:
    - `vite` for building and development.
    - `vitest` and `jsdom` for unit testing.
    - `eslint` for code quality.
    - `emoji-picker-element` for messaging.
3. [x] Configure `vite.config.js` to proxy API requests to the Spring Boot backend (`http://localhost:8080`).
4. [x] Update the root `pom.xml` to include a Maven profile for building the frontend using `frontend-maven-plugin`.

## 2. Core Frontend Architecture
5. [x] Implement a Hash-based Router in `src/main.js` to handle navigation between login, registration, and the chat interface.
6. [x] Develop a lightweight Global State Manager in `src/state/store.js` for handling JWT, current user session, and active room state.
7. [x] Create a Shared API Client in `src/api/http.js` with standard `fetch` wrappers for handling headers and error responses.

## 3. Layout and UI Components
8. [x] Create a responsive CSS grid/flexbox layout for the Main Layout Shell (Top Menu, Center Area, Bottom Area, and Side Panel).
9. [x] Implement the Top Menu Component (user profile display, settings link, and logout button).
10. [x] Develop the Room List with an accordion-style toggle in the Side Panel.
11. [x] Implement the Contacts List with online status indicators in the Side Panel.
12. [x] Create the Member Panel to display members of the active room in the Side Panel.
13. [x] Implement the Message List scrollable area for message history.
14. [x] Implement the Message Input with emoji picker and file attachment support.

## 4. Functional Features
15. [x] Implement Authentication logic (login and registration) including JWT storage and route protection.
16. [x] Implement Room Management functionality (list, search, join, and leave rooms) connected to backend APIs.
17. [x] Implement Messaging functionality: real-time polling to fetch new messages.
18. [x] Implement Messaging functionality: sending text messages and file uploads.
19. [x] Integrate Presence and Contacts: automatic status updates based on user activity (AFK detection).
20. [x] Integrate Presence and Contacts: display real-time status for contacts and room members.

## 5. Testing and Quality Assurance
21. [x] Establish a Unit Testing Strategy using Vitest and jsdom.
22. [x] Write unit tests for Utility functions (date formatting, file size, etc.).
23. [x] Write unit tests for State management logic.
24. [x] Write unit tests for API client wrappers.
25. [x] Write unit tests for Navigation and routing logic.
26. [x] Perform a final code review to ensure compliance with General Principles and Code Style guidelines.

## 6. Deployment and Integration
27. [x] Verify the multi-stage Docker build process.
28. [x] Ensure the built frontend static files are correctly served by the Spring Boot application.
29. [x] Conduct final end-to-end verification of all core chat features.
