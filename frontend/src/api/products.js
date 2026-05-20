import { apiRequest } from './client'

export function getProduct(id) {
  return apiRequest(`/api/products/${encodeURIComponent(String(id))}`, { auth: true })
}

export function createProduct(product) {
  return apiRequest('/api/products', {
    method: 'POST',
    auth: true,
    body: JSON.stringify(product),
  })
}

export function updateProduct(id, product) {
  return apiRequest(`/api/products/${encodeURIComponent(String(id))}`, {
    method: 'PUT',
    auth: true,
    body: JSON.stringify(product),
  })
}

export function deleteProduct(id) {
  return apiRequest(`/api/products/${encodeURIComponent(String(id))}`, {
    method: 'DELETE',
    auth: true,
  })
}
