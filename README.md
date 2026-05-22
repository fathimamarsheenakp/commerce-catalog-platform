# Commerce Catalog Platform

Microservices product catalog with **API Gateway**, **product-service** (Cassandra + Kafka), **search-service** (Elasticsearch), and a **React** frontend.

## Architecture

```text
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐     ┌───────────┐
│ React UI    │────▶│ API Gateway  │────▶│ product-service │────▶│ Cassandra │
│  :5173      │     │    :8080     │     │     :8081       │     └───────────┘
└─────────────┘     └──────┬───────┘     └────────┬────────┘
                           │                      │ Kafka (product-events)
                           │                      ▼
                           │             ┌─────────────────┐     ┌───────────────┐
                           └────────────▶│ search-service  │────▶│ Elasticsearch │
                                         │     :8082*      │     └───────────────┘
                                         └─────────────────┘
   * search-service binds to 127.0.0.1 — use gateway :8080, not :8082 directly
```

| Path | Service | Auth |
|------|---------|------|
| Browse / search | search-service via gateway | Public read |
| Create / update / delete products | product-service via gateway | **ADMIN** JWT |
| Login | product-service via gateway | Public |

Writes go to **Cassandra**; **Kafka** syncs the search index in Elasticsearch (usually a few seconds).

**Catalog browse** uses paginated search: `GET /api/search/products?page=0&size=12` (optional `keyword`, `category`, `brand`, `sort`).

## Prerequisites

- Java **17+**
- Maven **3.9+**
- Node.js **18+**
- Docker Desktop

## Kubernetes (optional)

Deploy the full stack to a local cluster (Docker Desktop K8s, kind, minikube):

```powershell
.\k8s\scripts\build-images.ps1
.\k8s\scripts\deploy.ps1
```

Open **http://localhost:30173** (UI) and **http://localhost:30080** (API). See [k8s/README.md](k8s/README.md) for details, kind image loading, and production notes.

## 1. Start infrastructure

From the project root:

```bash
docker compose up -d
```

Wait until Cassandra is ready:

```bash
docker exec cassandra cqlsh -e "SELECT release_version FROM system.local;"
```

`docker ps` should show `cassandra` as **healthy**.

## 2. Configure secrets (recommended)

```bash
# product-service
copy product-service\.env.example product-service\.env

# api-gateway (same JWT_SECRET as product-service)
copy api-gateway\.env.example api-gateway\.env
```

Edit both `.env` files:

- Set **`JWT_SECRET`** to a long random string (32+ characters).
- Optionally change **`ADMIN_USERNAME`** and generate **`ADMIN_PASSWORD_HASH`** (see [Security](#security)).

**Local dev default:** username `admin`, password `password` (BCrypt hash baked into `application.properties`).

Load env on Windows PowerShell before starting Java apps:

```powershell
cd product-service
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') { Set-Item -Path "env:$($matches[1].Trim())" -Value $matches[2].Trim() }
}
mvn spring-boot:run
```

Or set variables manually:

```powershell
$env:JWT_SECRET="your-long-random-secret-here-minimum-32-chars"
$env:ADMIN_USERNAME="admin"
```

## 3. Start backend services

Use the **same `JWT_SECRET`** for product-service and api-gateway.

| Service | Port | Command |
|---------|------|---------|
| product-service | 8081 | `cd product-service && mvn spring-boot:run` |
| search-service | 8082 | `cd search-service && mvn spring-boot:run` |
| api-gateway | 8080 | `cd api-gateway && mvn spring-boot:run` |

Health checks:

- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8080/actuator/health

## 4. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 (proxies API calls to gateway **8080**).

## Admin login (local dev)

| Field | Default |
|-------|---------|
| Username | `admin` |
| Password | `password` |

Override with `ADMIN_USERNAME` and `ADMIN_PASSWORD_HASH` in `product-service/.env`.

## Security

### Implemented

- **JWT** authentication (gateway + product-service)
- **Role-based access:** `ADMIN` required for POST/PUT/DELETE on products; search **POST** (direct index write) requires ADMIN at gateway
- **Public catalog read** via GET search endpoints (by design)
- **BCrypt** password verification (no plaintext password in code)
- **Secrets via environment:** `JWT_SECRET`, `ADMIN_USERNAME`, `ADMIN_PASSWORD_HASH`
- **CORS** restricted at gateway (`CORS_ALLOWED_ORIGINS`, default `http://localhost:5173`)
- **Actuator** exposes only `health` and `info`; no detailed health payload
- **search-service** listens on **127.0.0.1** only — not reachable from other machines; use gateway
- **Invalid login** returns **401** with a clear message
- **Swagger** disabled when running with `--spring.profiles.active=prod`

### Generate a new admin password hash

```bash
cd product-service
$env:NEW_ADMIN_PASSWORD="YourNewPassword"
mvn test -Dtest=PasswordHashGeneratorTest#generateHash
```

Copy the printed `ADMIN_PASSWORD_HASH` into `product-service/.env`.

### Production checklist

- [ ] Strong unique `JWT_SECRET` on all services
- [ ] Custom `ADMIN_PASSWORD_HASH` (not default)
- [ ] `spring.profiles.active=prod` (disables Swagger)
- [ ] HTTPS in front of gateway
- [ ] Do not expose ports 8081/8082 publicly
- [ ] Never commit `.env` files

## API documentation (dev only)

- product-service: http://localhost:8081/swagger-ui.html
- search-service: http://localhost:8082/swagger-ui.html

## Troubleshooting

### Cassandra connection error on startup

Cassandra is not ready. Run `docker compose up -d`, wait for `cqlsh` to succeed, then restart product-service.

### Catalog not showing updates immediately

Browse uses Elasticsearch. After create/update/delete, wait a few seconds and click **Refresh** on the catalog page.

### 401 on login

Check username/password. If you changed `ADMIN_PASSWORD_HASH`, ensure it matches your password (regenerate hash if needed).

### CORS errors from frontend

Ensure gateway runs with `CORS_ALLOWED_ORIGINS` including your UI origin (default `http://localhost:5173`).

### Search returns `pageable` / `number` (old API) or keyword filter ignored

The catalog endpoint must return:

```json
{ "content": [], "page": 0, "size": 12, "totalElements": 0, "totalPages": 0 }
```

If you see `pageable`, `number`, `first`, `last`, Docker is running a **stale JAR**. Rebuild **inside Docker** (the `search-service` Dockerfile compiles on build):

```bash
docker compose build --no-cache search-service
docker compose up -d search-service api-gateway
```

Verify:

```powershell
(Invoke-RestMethod "http://localhost:8080/api/search/products?keyword=apple&size=12").PSObject.Properties.Name
```

Expect: `content`, `page`, `size`, `totalElements`, `totalPages` — **not** `pageable`.

With only Sony/OnePlus in the index, `keyword=apple` should return **empty** `content`.

### 500 on `/api/search/products` (catalog empty / error)

Common causes:

1. **Elasticsearch not running** — `docker compose up -d` and wait until `elasticsearch` is healthy.
2. **search-service not reachable from api-gateway** — If you use Docker Compose, rebuild after config changes:
   ```bash
   docker compose up -d --build search-service api-gateway
   ```
   search-service must listen on `0.0.0.0` inside Docker (profile `docker`), not only `127.0.0.1`.
3. **Mixed setup** — Gateway in Docker but search-service on the host with `local` profile: set
   `SEARCH_SERVICE_URL=http://host.docker.internal:8082` on api-gateway.

Check health: http://localhost:8082/actuator/health and http://localhost:9200

## Tech stack

**Backend:** Java 17, Spring Boot 3.5, Spring Cloud Gateway, Spring Security, JWT, Cassandra, Kafka, Elasticsearch  

**Frontend:** React, Vite, React Router  

**Infra:** Docker Compose, **Kubernetes** ([k8s/README.md](k8s/README.md))
