# commerce-catalog-platform

Microservices product catalog: **product-service** (Cassandra + Kafka), **search-service** (Elasticsearch), **api-gateway**, and a **React** frontend.

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+ (frontend)
- Docker Desktop

## 1. Start infrastructure (required first)

From the project root:

```bash
docker compose up -d
```

**Wait until Cassandra is ready** (first start can take 1–2 minutes):

```bash
docker exec cassandra cqlsh -e "SELECT release_version FROM system.local;"
```

If that returns a version (e.g. `4.1.11`), Cassandra is ready.

Check container health:

```bash
docker ps
```

`cassandra` should show `healthy` when healthcheck passes.

## 2. Start backend services

In separate terminals:

```bash
cd product-service
mvn spring-boot:run
```

```bash
cd search-service
mvn spring-boot:run
```

```bash
cd api-gateway
mvn spring-boot:run
```

| Service          | Port |
|------------------|------|
| api-gateway      | 8080 |
| product-service  | 8081 |
| search-service   | 8082 |

## 3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Admin login

| Username | Password   |
|----------|------------|
| admin    | password   |

## Troubleshooting

### product-service fails: `Cannot resolve bean 'cassandraTemplate'` / `Could not reach any contact point` on `127.0.0.1:9042`

**Cause:** Cassandra is not running, or product-service was started **before** Cassandra finished booting.

**Fix:**

1. `docker compose up -d`
2. Wait until: `docker exec cassandra cqlsh -e "SELECT now() FROM system.local;"` succeeds
3. Start **product-service** again

Also ensure nothing else is blocking port **9042**.

### Updates slow to appear on catalog

Browse uses **Elasticsearch**. Writes go to **Cassandra** then **Kafka** syncs to search (a few seconds). Use **Refresh** on the catalog page if needed.
