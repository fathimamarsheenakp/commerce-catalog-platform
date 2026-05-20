import { apiRequest } from './client'

export function getAllProducts() {
  return apiRequest('/api/search/products')
}

export function getSearchProduct(id) {
  return apiRequest(`/api/search/products/${id}`)
}

export function searchProducts({ keyword = '', category = '', brand = '', sort = '' }) {
  const params = new URLSearchParams({ keyword })
  if (category) params.set('category', category)
  if (brand) params.set('brand', brand)
  if (sort) params.set('sort', sort)
  const qs = params.toString()
  return apiRequest(`/api/search/products/search${qs ? `?${qs}` : ''}`)
}

export function getCategoryAggregations() {
  return apiRequest('/api/search/products/aggregations/categories')
}

export function getBrandAggregations() {
  return apiRequest('/api/search/products/aggregations/brands')
}
