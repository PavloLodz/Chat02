
```
mvn clean package -DskipTests
```

# Unit tests only — fast, no Docker required 
```
mvn test
```

# Integration tests only — requires Docker (Testcontainers + EmbeddedKafka)
```
mvn clean verify -P integration-tests
```

# Both unit + integration
```
mvn clean verify -P integration-tests -Dsurefire.excludes=
```