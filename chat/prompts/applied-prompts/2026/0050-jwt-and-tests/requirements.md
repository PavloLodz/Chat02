# JWT Authentication & Authorization Requirements

## 1. Overview
This document defines the requirements for implementing JWT (JSON Web Token) based authentication and authorization in the `pl.ldz.chat` application using Spring Boot 3.4 and Spring Security 6.

The system will transition from the current HTTP Basic authentication to stateless authentication using JWT tokens for securing REST APIs.

---

## 2. Functional Requirements

### 2.1 User Authentication
- **Login Endpoint**: Implement a `POST /api/v1/auth/login` endpoint.
- **Credentials**: Users must authenticate using `username` and `password`.
- **JWT Generation**: Upon successful authentication, the system must generate a JWT containing:
    - Subject: User's username.
    - Claims: User's roles (e.g., `ROLE_USER`, `ROLE_ADMIN`).
    - Expiration time: Configurable (default 24 hours).
- **Response**: The JWT must be returned in a DTO (`JwtResponseDto`) with fields `token` and `expiresAt`.

### 2.2 Token-Based Authorization
- **Security Filter**: Implement a `JwtAuthenticationFilter` that intercepts requests and validates the `Authorization: Bearer <token>` header.
- **Statelessness**: The application must remain stateless. No server-side sessions (`SessionCreationPolicy.STATELESS`).
- **Error Handling**: Requests with missing, expired, or invalid tokens must be rejected with HTTP 401 (Unauthorized).

### 2.3 Role-Based Access Control (RBAC)
- **Role Integration**: Use existing `UserRoles` enum (`VIEWER`, `USER`, `ADMIN`, `AUDITOR`).
- **Authorization**: Secure endpoints using `@PreAuthorize` annotations or `SecurityFilterChain` matchers as per current project guidelines.
- **Prefixing**: JWT roles must be prefixed with `ROLE_` during token parsing for Spring Security compatibility.

### 2.4 Logout
- **Stateless Logout**: Since JWT is stateless, initial logout handling will be client-side (token removal).
- **Future Enhancement**: Optional blacklisting of tokens can be implemented later.

---

## 3. Non-Functional Requirements

### 3.1 Security
- **Signing**: JWTs must be signed using HS256 with a strong, configurable secret key.
- **Hashing**: Passwords must be hashed using BCrypt (already configured in `SecurityConfig`).
- **No Sensitive Data**: The JWT payload must NOT contain passwords or other sensitive user information.

### 3.2 Performance
- **Efficiency**: JWT validation should avoid database hits if possible (validate against the secret key).
- **Caching**: If database lookups for user details are required during validation, consider lightweight caching.

### 3.3 Reliability
- **Consistency**: Map JWT-related exceptions (e.g., `ExpiredJwtException`, `SignatureException`) to clear, consistent API error responses via `GlobalExceptionHandler`.

---

## 4. Technical Requirements

### 4.1 Libraries
- **JWT Library**: Use `io.jsonwebtoken:jjwt-api:0.12.6` (and its implementation/jackson modules).
- **Lombok**: Use `@Getter`, `@Setter`, `@RequiredArgsConstructor`, and `@Builder` for all new security-related classes and DTOs.

### 4.2 Architecture & Package Layout
- `pl.ldz.chat.security.jwt` — JWT utility classes, filters, and configuration.
- `pl.ldz.chat.dto.auth` — Authentication request/response DTOs.
- `pl.ldz.chat.controller.auth` — Auth controller for login/refresh.

### 4.3 Spring Security Configuration
- **SecurityFilterChain**: Update `SecurityConfig` to:
    - Disable CSRF and Frame Options (for H2 console if used, but primarily for statelessness).
    - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
    - Permit all for `/api/v1/auth/**`.
- **AuthenticationManager**: Explicitly expose `AuthenticationManager` as a bean for use in `AuthService`.

---

## 5. Testing Requirements
- **Unit Tests**:
    - Test `JwtService` for token generation, parsing, and validation.
    - Test `JwtAuthenticationFilter` with mocked security context.
- **Integration Tests (IT)**:
    - Create `AuthIT` to verify successful login and subsequent access to protected endpoints using the returned token.
    - Verify that expired tokens or tokens with wrong signatures result in 401.
