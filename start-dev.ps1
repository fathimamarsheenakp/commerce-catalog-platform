Write-Host "Starting infrastructure..."

docker compose up -d

Write-Host "Waiting for Cassandra..."
Start-Sleep -Seconds 90

Write-Host "Waiting for Elasticsearch..."
Start-Sleep -Seconds 20

Write-Host "Waiting for Kafka..."
Start-Sleep -Seconds 20

Write-Host "Infrastructure ready."

Write-Host "Start services manually in separate terminals:"
Write-Host "1. product-service"
Write-Host "2. search-service"
Write-Host "3. api-gateway"
Write-Host "4. frontend"