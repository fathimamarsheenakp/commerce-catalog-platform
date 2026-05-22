# Kubernetes deployment

Deploy the **commerce-catalog-platform** stack to a local cluster (Docker Desktop Kubernetes, [kind](https://kind.sigs.k8s.io/), or [minikube](https://minikube.sigs.k8s.io/)) using the same services as `docker-compose.yml`.

## Architecture in the cluster

```text
frontend (NodePort :30173)
    └── Vite proxy → api-gateway (NodePort :30080)
            ├── product-service → cassandra
            │                   └── kafka (product-events)
            └── search-service → elasticsearch
                                 └── kafka (consumer)
zookeeper ← kafka
```

All workloads run in namespace **`commerce-catalog`**.

## Prerequisites

- `kubectl` configured for your cluster
- `docker` to build images
- **Maven** (for `product-service` and `api-gateway` JARs before image build)
- Enough RAM for Cassandra + Elasticsearch + Kafka (~4–6 GB for the cluster)

### Docker Desktop

Enable **Kubernetes** in Docker Desktop settings.

### kind (example)

```bash
kind create cluster --name commerce-catalog
```

After building images, load them into the cluster:

```bash
kind load docker-image commerce-catalog/product-service:latest --name commerce-catalog
kind load docker-image commerce-catalog/search-service:latest --name commerce-catalog
kind load docker-image commerce-catalog/api-gateway:latest --name commerce-catalog
kind load docker-image commerce-catalog/frontend:latest --name commerce-catalog
```

## 1. Build images

From the **repository root**:

**Windows (PowerShell):**

```powershell
.\k8s\scripts\build-images.ps1
```

**Linux / macOS:**

```bash
chmod +x k8s/scripts/build-images.sh
./k8s/scripts/build-images.sh
```

Image tags:

| Image | Port |
|-------|------|
| `commerce-catalog/product-service:latest` | 8081 |
| `commerce-catalog/search-service:latest` | 8082 |
| `commerce-catalog/api-gateway:latest` | 8080 |
| `commerce-catalog/frontend:latest` | 5173 |

## 2. Configure secrets

On first deploy, the script copies `k8s/secrets/app-secrets.example.yaml` → `app-secrets.yaml` if missing.

Edit `k8s/secrets/app-secrets.yaml` and set a strong `JWT_SECRET` for anything beyond local dev. **Do not commit** `app-secrets.yaml`.

## 3. Deploy

```powershell
.\k8s\scripts\deploy.ps1
```

Or manually:

```bash
cp k8s/secrets/app-secrets.example.yaml k8s/secrets/app-secrets.yaml   # first time only
kubectl apply -k k8s
```

**Startup order:** init containers wait for Cassandra, Elasticsearch, and Kafka. Cassandra can take **2–5 minutes** on first boot.

```bash
kubectl get pods -n commerce-catalog -w
```

## 4. Access the app

| Service | URL |
|---------|-----|
| Frontend | http://localhost:30173 |
| API gateway | http://localhost:30080 |
| Catalog API | http://localhost:30080/api/search/products?page=0&size=12 |

Default login: **admin** / **password** (dev hash in `app-secrets.example.yaml`).

CORS is set in `k8s/config/app-configmap.yaml` to `http://localhost:30173`. Change it if you use port-forward or Ingress instead of NodePort.

## Useful commands

```bash
# Logs
kubectl logs -n commerce-catalog deploy/search-service -f

# Restart an app after image rebuild
kubectl rollout restart deployment/search-service -n commerce-catalog

# Delete everything in the namespace
kubectl delete namespace commerce-catalog
```

## Production notes

This layout is aimed at **local/demo** clusters:

- Single replica, no HPA, no Ingress TLS
- Elasticsearch security disabled (same as Compose)
- NodePorts **30080** / **30173** may conflict on shared machines

For production you would typically:

- Push images to a registry and set `imagePullPolicy: Always`
- Use managed Cassandra / Elasticsearch / Kafka (or operators)
- Add Ingress + cert-manager, NetworkPolicies, and sealed secrets
- Run `spring.profiles.active=prod` and disable Swagger

## File layout

```text
k8s/
  namespace.yaml
  kustomization.yaml
  config/app-configmap.yaml
  secrets/app-secrets.example.yaml   # template
  secrets/app-secrets.yaml           # gitignored, created on deploy
  infrastructure/                    # cassandra, elasticsearch, kafka, zookeeper
  apps/                              # product-service, search-service, api-gateway, frontend
  scripts/build-images.ps1|sh
  scripts/deploy.ps1
```
