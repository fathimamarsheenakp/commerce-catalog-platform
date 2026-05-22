# Deploy commerce-catalog to Kubernetes. Run from repo root: .\k8s\scripts\deploy.ps1
$ErrorActionPreference = "Stop"
$K8sDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$SecretsFile = Join-Path $K8sDir "secrets\app-secrets.yaml"
$ExampleFile = Join-Path $K8sDir "secrets\app-secrets.example.yaml"

if (-not (Test-Path $SecretsFile)) {
    Write-Host "Creating $SecretsFile from example (edit for production)."
    Copy-Item $ExampleFile $SecretsFile
}

Write-Host "Applying manifests (namespace: commerce-catalog)..."
kubectl apply -k $K8sDir

Write-Host ""
Write-Host "Watch startup: kubectl get pods -n commerce-catalog -w"
Write-Host "UI (NodePort):     http://localhost:30173"
Write-Host "API (NodePort):    http://localhost:30080"
Write-Host "Login (dev):       admin / password"
