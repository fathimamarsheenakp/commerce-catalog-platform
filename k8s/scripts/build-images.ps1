# Build container images for Kubernetes (same Dockerfiles as docker-compose).
# Run from repository root: .\k8s\scripts\build-images.ps1

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

Set-Location $Root

Write-Host "Building Maven JARs for product-service and api-gateway..."
Push-Location product-service
& mvn -q package -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Pop-Location

Push-Location api-gateway
& mvn -q package -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Pop-Location

$images = @(
    @{ Name = "commerce-catalog/product-service:latest"; Context = "product-service" },
    @{ Name = "commerce-catalog/search-service:latest"; Context = "search-service" },
    @{ Name = "commerce-catalog/api-gateway:latest"; Context = "api-gateway" },
    @{ Name = "commerce-catalog/frontend:latest"; Context = "frontend" }
)

foreach ($img in $images) {
    Write-Host "docker build -t $($img.Name) ./$($img.Context)"
    docker build -t $img.Name "./$($img.Context)"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host "Done. For kind: kind load docker-image commerce-catalog/product-service:latest (repeat for each image)"
