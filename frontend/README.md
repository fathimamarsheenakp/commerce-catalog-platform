# Commerce Catalog — Frontend

React + Vite UI for the commerce-catalog-platform backend.

## Features

- **Browse** — search and filter products (Elasticsearch via search-service)
- **Product detail** — view a single product
- **Manage** — create, update, delete products (admin JWT; syncs to search via Kafka)

## Prerequisites

Backend must be running:

1. `docker compose up -d` (Cassandra, Elasticsearch, Kafka)
2. `product-service` on port **8081**
3. `search-service` on port **8082**
4. `api-gateway` on port **8080**

## Run locally

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

API calls are proxied to `http://localhost:8080` (see `vite.config.js`).

## Sign in (admin)

| Field    | Value      |
|----------|------------|
| Username | `admin`    |
| Password | `password` |

## Production build

```bash
npm run build
npm run preview
```

Set `VITE_API_BASE_URL` if the API is not on the same origin (e.g. `https://api.example.com`).
