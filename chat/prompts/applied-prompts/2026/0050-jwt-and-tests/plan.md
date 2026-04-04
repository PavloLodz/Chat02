# JWT Authentication & Authorization Implementation Plan

This plan outlines the steps for transitioning from HTTP Basic to JWT-based authentication in the `pl.ldz.chat` project, based on the requirements in `prompts/requirements.md`.

## 1. Dependencies Update
- Update `pom.xml` to include:
    - `io.jsonwebtoken:jjwt-api:0.12.6`
    - `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime)
    - `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime)

## 2. DTO Implementation
- Create `pl.ldz.chat.dto.auth.LoginRequestDto`:
    - `String username`
    - `String password`
- Create `pl.ldz.chat.dto.auth.JwtResponseDto`:
    - `String token`
    - `LocalDateTime expiresAt`

## 3. JWT Service Layer
- Create `pl.ldz.chat.security.jwt.JwtService`:
    - Configuration for HS256 secret key and expiration time.
    - `String generateToken(UserDetails userDetails)`
    - `String getUsernameFromToken(String token)`
    - `List<String> getRolesFromToken(String token)`
    - `boolean validateToken(String token, UserDetails userDetails)`

## 4. Authentication Logic
- Create `pl.ldz.chat.service.AuthService`:
    - Inject `AuthenticationManager`, `JwtService`, and `ChatUserDetailsService`.
    - `JwtResponseDto login(LoginRequestDto loginRequestDto)`: 
        - Authenticate using `AuthenticationManager`.
        - Generate JWT for the user.
        - Return `JwtResponseDto`.
- Create `pl.ldz.chat.controller.auth.AuthController`:
    - Endpoint: `POST /api/v1/auth/login`.
    - Call `AuthService.login()`.

## 5. Security Filtering
- Create `pl.ldz.chat.security.jwt.JwtAuthenticationFilter`:
    - Extend `OncePerRequestFilter`.
    - Extract JWT from `Authorization: Bearer <token>` header.
    - Validate token with `JwtService`.
    - Set `SecurityContext` with `UsernamePasswordAuthenticationToken`.
- Update `pl.ldz.chat.security.SecurityConfig`:
    - Disable `httpBasic`.
    - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
    - Permit all for `/api/v1/auth/**`.

## 6. Error Handling
- Update `pl.ldz.chat.exception.GlobalExceptionHandler`:
    - Handle `io.jsonwebtoken.security.SignatureException`.
    - Handle `io.jsonwebtoken.ExpiredJwtException`.
    - Handle `io.jsonwebtoken.MalformedJwtException`.
    - Map these to 401 Unauthorized with meaningful messages.

## 7. Testing & Verification
### 7.1 Unit Tests
- `JwtServiceTest`: Verify token generation and parsing (including role extraction).
- `JwtAuthenticationFilterTest`: Mock `HttpServletRequest` and `SecurityContext` to verify filter behavior.

### 7.2 Integration Tests
- `AuthIT`: 
    - Test successful login returns a valid token.
    - Test failed login returns 401.
    - Test protected endpoints with a valid token.
    - Test protected endpoints with an invalid/expired token.
- Verify that roles are correctly prefixed with `ROLE_` during token parsing.
