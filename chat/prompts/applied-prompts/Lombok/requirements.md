# Requirements for Lombok application

1. Use `@Getter` and `@Setter` on JPA entities.
2. Avoid `@Data` on JPA entities to prevent issues with `equals()`, `hashCode()`, and `toString()` on lazy relationships.
3. Manually implement `equals()`, `hashCode()`, and `toString()` for JPA entities, following best practices (e.g., using a business key or ID).
4. Use `@NoArgsConstructor` and `@AllArgsConstructor` on entities as needed for Hibernate and builders.
5. Use `@Builder` on entities and DTOs where it improves object creation.
6. Use `@RequiredArgsConstructor` for constructor-based dependency injection in Services, Controllers, and other Spring components.
7. Use `@Slf4j` for logging in Services and Controllers.
8. Follow the existing project code style (e.g., 2 spaces for indentation).
9. Ensure the project builds and all tests pass after changes.
10. Suffix of integration tests is `IT` and they are located in `src/integration-test/java`.
11. Unit tests are in `src/test/java`.
