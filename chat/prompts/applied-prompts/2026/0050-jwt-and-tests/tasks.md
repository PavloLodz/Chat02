# JWT Authentication & Authorization Task List

## 1. Dependencies Update
- [x] Add `io.jsonwebtoken:jjwt-api:0.12.6` to `pom.xml`
- [x] Add `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime) to `pom.xml`
- [x] Add `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime) to `pom.xml`
- [x] Run `mvn clean compile` to verify dependencies

## 2. DTO Implementation
- [x] Create `pl.ldz.chat.dto.auth.LoginRequestDto` (username, password)
- [x] Create `pl.ldz.chat.dto.auth.JwtResponseDto` (token, expiresAt)

## 3. JWT Service Layer
- [x] Create `pl.ldz.chat.security.jwt.JwtService`
    - [x] Implement `generateToken(UserDetails userDetails)`
    - [x] Implement `getUsernameFromToken(String token)`
    - [x] Implement `getRolesFromToken(String token)`
    - [x] Implement `validateToken(String token, UserDetails userDetails)`
    - [x] Configure signing key and expiration from `application.properties`

## 4. Authentication Logic
- [x] Create `pl.ldz.chat.service.AuthService`
    - [x] Implement `login(LoginRequestDto loginRequestDto)`
- [x] Create `pl.ldz.chat.controller.auth.AuthController`
    - [x] Implement `POST /api/v1/auth/login`

## 5. Security Filtering & Configuration
- [x] Create `pl.ldz.chat.security.jwt.JwtAuthenticationFilter`
    - [x] Implement `doFilterInternal` to extract and validate Bearer token
    - [x] Set `SecurityContext` upon successful validation
- [x] Update `pl.ldz.chat.security.SecurityConfig`
    - [x] Disable `httpBasic`
    - [x] Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
    - [x] Permit all for `/api/v1/auth/**`

## 6. Global Error Handling
- [x] Update `pl.ldz.chat.exception.GlobalExceptionHandler`
    - [x] Handle `SignatureException`
    - [x] Handle `ExpiredJwtException`
    - [x] Handle `MalformedJwtException`

## 7. Testing
- [x] Unit Tests
    - [x] Create `pl.ldz.chat.security.jwt.JwtServiceTest`
    - [x] Create `pl.ldz.chat.security.jwt.JwtAuthenticationFilterTest`
- [x] Integration Tests
    - [x] Create `src/integration-test/java/pl/ldz/chat/controller/auth/AuthIT.java`
    - [x] Test successful login
    - [x] Test protected endpoint access with valid JWT
    - [x] Test unauthorized access with invalid/expired JWT
