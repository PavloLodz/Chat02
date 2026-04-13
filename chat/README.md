# Chat02

# Run with Docker Compose (recommended)

Build and start the app + PostgreSQL together:
```
docker compose up --build
```
For rest assured test mode:
```
docker compose --profile empty-db up  --build
```
docker compose --profile empty-db down
docker compose -p chat down

The app will be available at http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html

Stop and remove containers:
```
docker compose down
```

Stop and also remove the database volume (clean slate):
```
docker compose down -v
```

---

To run everything:

docker compose up --build
The app will be at http://localhost:8080 and Swagger UI at http://localhost:8080/swagger-ui/index.html. 
The first build will take a few minutes while Maven downloads dependencies — subsequent builds will be much faster thanks to Docker layer caching.