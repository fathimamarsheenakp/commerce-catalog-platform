import { apiRequest } from './client'

const DEFAULT_PAGE_SIZE = 12

export function getProductsPage({
  page = 0,
  size = DEFAULT_PAGE_SIZE,
  keyword = '',
  category = '',
  brand = '',
  sort = '',
} = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  if (keyword.trim()) params.set('keyword', keyword.trim())
  if (category) params.set('category', category)
  if (brand) params.set('brand', brand)
  if (sort) params.set('sort', sort)
  return apiRequest(`/api/search/products?${params}`)
}

/** @deprecated Use getProductsPage — kept for compatibility */
export function getAllProducts() {
  return getProductsPage({ page: 0, size: 1000 })
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

export { DEFAULT_PAGE_SIZE }
