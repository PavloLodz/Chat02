Separate Integration tests (IT) from unit test in different directories.
Make runing only unit test, only integration tests and run all tests.

---
Additional info:
src/
├── test/java/…                    ← unit tests (run with mvn test)
│   ├── controller/UserControllerTest.java
│   ├── service/UserServiceTest.java
│   └── mapper/UserMapperTest.java
└── integration-test/java/…        ← integration tests (run with mvn verify)
├── controller/AbstractControllerIntegrationTest.java
├── controller/UserControllerIntegrationTest.java
├── repository/base/AbstractRepositoryIntegrationTest.java
├── repository/UserRepositoryIntegrationTest.java
├── service/base/AbstractServiceIntegrationTest.java
└── service/UserServiceIntegrationTest.java
New unit tests created (no DB, no Spring context boot, fast):

UserMapperTest — pure unit tests for all mapper methods
UserServiceTest — Mockito mocks for UserRepository and UserMapper, covers all CRUD paths and exceptions
UserControllerTest — @WebMvcTest (no full Spring context), mocks UserService, covers auth/authorization, validation, and 404 handling

pom.xml updated with three plugins:

build-helper-maven-plugin — registers src/integration-test/java as an additional test source root
maven-surefire-plugin — runs **/*Test.java, excludes **/*IntegrationTest.java
maven-failsafe-plugin — runs **/*IntegrationTest.java only during the verify phase
