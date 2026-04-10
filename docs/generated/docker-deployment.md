# Docker Deployment

## Files

- [Dockerfile](/Users/youngrok/Project/VibeCoding/Vibe2guys-backend/Dockerfile)
- [compose.yml](/Users/youngrok/Project/VibeCoding/Vibe2guys-backend/compose.yml)
- [.dockerignore](/Users/youngrok/Project/VibeCoding/Vibe2guys-backend/.dockerignore)
- [.env.production.example](/Users/youngrok/Project/VibeCoding/Vibe2guys-backend/.env.production.example)

## Recommended Runtime

- application container
- PostgreSQL container
- bind app only through a reverse proxy in real production if possible

## First Run

1. Create `.env.production` from `.env.production.example`
2. Fill strong secrets and passwords
3. Run:

```bash
docker compose --env-file .env.production up -d --build
```

## Security Defaults

- Swagger UI disabled by default in compose
- OpenAPI docs disabled by default in compose
- backoffice APIs require both admin JWT and `X-Backoffice-Key`
- DB port defaults to loopback-only exposure
- secrets are externalized through env file

## Recommended Production Additions

- add Nginx or Caddy for TLS termination
- restrict inbound firewall rules
- rotate JWT and backoffice secrets periodically
- store `.env.production` outside public repositories and backups with least access
