
# Run locally (requires a running PostgreSQL on localhost:5432)

```bash
mvn clean package -DskipTests
```

# Run Tests

## Unit tests only
Fast, no Docker required. Tests are located in `src/test/java`.
```bash
mvn clean test
```

## Integration tests only
Requires Docker (Testcontainers). Tests are located in `src/integration-test/java`.
```bash
mvn clean integration-test
```

## All tests (Unit + Integration)
Requires Docker (Testcontainers).
```bash
mvn clean verify
```

