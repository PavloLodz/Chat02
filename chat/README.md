# Chat application with demonstration of using Junie AI assistant 

For running java build & test commands please use file RUN_ME.md 

For more detailed information please see file info/PRJ_DDESCRIPTION.md

# Run with Docker Compose (recommended)

Build and start the app + PostgreSQL together:
```
docker compose up --build
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

Stop and remove containers:
```
docker compose down
```

Stop and also remove the database volume (clean slate):
```
docker compose down -v
```

## Default Seed Users

On startup, the application automatically seeds the following users (created only if they do not already exist):

| Username | Password | Role    |
|----------|----------|---------|
| `viewer` | `vp`     | VIEWER  |
| `user`   | `up`     | USER    |
| `admin`  | `ap`     | ADMIN   |
| `auditor`| `ap`     | AUDITOR |


