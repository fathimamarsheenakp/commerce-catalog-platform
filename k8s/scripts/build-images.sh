#!/usr/bin/env bash
# Build container images for Kubernetes. Run from repository root: ./k8s/scripts/build-images.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

echo "Building Maven JARs for product-service and api-gateway..."
(cd product-service && mvn -q package -DskipTests)
(cd api-gateway && mvn -q package -DskipTests)

build() {
  local tag=$1 ctx=$2
  echo "docker build -t ${tag} ./${ctx}"
  docker build -t "${tag}" "./${ctx}"
}

build commerce-catalog/product-service:latest product-service
build commerce-catalog/search-service:latest search-service
build commerce-catalog/api-gateway:latest api-gateway
build commerce-catalog/frontend:latest frontend

echo "Done. For kind: kind load docker-image commerce-catalog/product-service:latest (repeat for each)"
