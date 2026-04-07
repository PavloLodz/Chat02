# Chat02 – REST API Tests

file:///home/left10/My/Git/Java/Chat02/chat-api-tests/target/surefire-reports/index.html#update_asAdmin_returns200WithUpdatedFields

Black-box API tests for the Chat02 Spring Boot application,
built with **REST Assured 5** + **TestNG 7** + **Jackson** + **AssertJ**.

---

## Stack

| Library        | Version | Role                              |
|----------------|---------|-----------------------------------|
| TestNG         | 7.10.2  | Test runner & suite orchestration |
| REST Assured   | 5.5.0   | HTTP client / assertion DSL       |
| Jackson        | 2.18.2  | DTO serialisation / deserialisation |
| AssertJ        | 3.27.3  | Fluent assertions on DTO fields   |
| Lombok         | 1.18.36 | Boilerplate reduction on DTOs     |

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java        | 21+     |
| Maven       | 3.9+    |
| Chat02 app  | running (with its PostgreSQL database) |

The tests are **black-box**: they hit a live Chat02 instance over HTTP and
rely on the four accounts seeded by `DataSeeder` at startup.

---

## Running

```bash
# Default – targets http://localhost:8080
mvn test

# Custom base URL
mvn test -Dapi.base-url=http://staging.example.com:9090
```

---

## Project layout

```
src/test/java/pl/ldz/chat/
├── dto/                          ← exact mirrors of source-project DTOs
│   ├── UserRequestDto.java
│   ├── UserResponseDto.java
│   └── auth/
│       ├── LoginRequestDto.java
│       └── JwtResponseDto.java
├── common/
│   ├── BaseApiTest.java          ← REST Assured + Jackson config; withToken()
│   ├── AuthHelper.java           ← login helpers returning JwtResponseDto
│   └── UserRequestFactory.java   ← builds valid & invalid UserRequestDto objects
└── api/
    ├── auth/
    │   └── AuthApiTest.java      ← POST /api/v1/auth/login (9 tests)
    └── users/
        └── UserApiTest.java      ← full CRUD + auth matrix (27 tests)

src/test/resources/
└── testng.xml                    ← suite file: Auth → Users (sequential)
```

---

## DTO mirrors

The test DTOs in `pl.ldz.chat.dto` are direct equivalents of the source records/classes,
re-implemented with Lombok and Jackson annotations for the test module:

| Test DTO              | Source DTO                     | Notes                          |
|-----------------------|--------------------------------|--------------------------------|
| `LoginRequestDto`     | `dto.auth.LoginRequestDto`     | Lombok + `@JsonProperty`       |
| `JwtResponseDto`      | `dto.auth.JwtResponseDto`      | `LocalDateTime` deserialiser   |
| `UserRequestDto`      | `dto.UserRequestDto`           | record → Lombok class          |
| `UserResponseDto`     | `dto.UserResponseDto`          | `UUID` + `Instant` fields      |

---

## Seeded users (DataSeeder)

| Username | Password | Role    |
|----------|----------|---------|
| viewer   | vp       | VIEWER  |
| user     | up       | USER    |
| admin    | ap       | ADMIN   |
| auditor  | ap       | AUDITOR |

---

## Test coverage

### Auth (`AuthApiTest`) – 9 tests
| Scenario                        | Expected |
|---------------------------------|----------|
| Valid login (admin/user/viewer/auditor) | 200 + token + expiresAt |
| Wrong password                  | 401      |
| Unknown username                | 401      |
| Blank username                  | 400      |
| Blank password                  | 400      |
| Empty JSON body                 | 400      |

### Users (`UserApiTest`) – 27 tests

| Endpoint           | Scenario                          | Expected |
|--------------------|-----------------------------------|----------|
| GET /users         | ADMIN / USER / VIEWER / AUDITOR   | 200      |
| GET /users         | No token / malformed token        | 401      |
| GET /users         | Pagination params respected       | 200 + correct metadata |
| GET /users/{id}    | ADMIN / VIEWER / AUDITOR          | 200 + UserResponseDto fields |
| GET /users/{id}    | Non-existent UUID                 | 404      |
| GET /users/{id}    | No token                          | 401      |
| POST /users        | ADMIN creates user                | 201 + full UserResponseDto |
| POST /users        | VIEWER / USER / AUDITOR           | 403      |
| POST /users        | No token                          | 401      |
| POST /users        | Blank username / invalid email / blank password / username > 50 | 422 |
| PUT /users/{id}    | ADMIN / USER role                 | 200 + updated fields |
| PUT /users/{id}    | VIEWER / AUDITOR                  | 403      |
| PUT /users/{id}    | No token                          | 401      |
| PUT /users/{id}    | Non-existent UUID                 | 404      |
| PUT /users/{id}    | Invalid payload                   | 422      |
| DELETE /users/{id} | ADMIN                             | 204      |
| DELETE /users/{id} | User gone after deletion          | 404      |
| DELETE /users/{id} | USER / VIEWER / AUDITOR           | 403      |
| DELETE /users/{id} | No token                          | 401      |
| DELETE /users/{id} | Non-existent UUID                 | 404      |
