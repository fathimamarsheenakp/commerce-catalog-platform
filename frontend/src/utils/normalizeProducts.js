/** Normalize API responses into a plain product array. */
export function normalizeProductList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.content)) return data.content
  if (Array.isArray(data._embedded?.products)) return data._embedded.products
  return []
}

/** Normalize paginated search API response. */
export function normalizePageResponse(data) {
  if (!data) {
    return {
      content: [],
      page: 0,
      size: 12,
      totalElements: 0,
      totalPages: 0,
    }
  }
  // PagedProductsResponse: { page, size, ... } — Spring Page: { number, size, ... }
  return {
    content: normalizeProductList(data.content ?? data),
    page: data.page ?? data.number ?? 0,
    size: data.size ?? 12,
    totalElements: data.totalElements ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}

/** Normalize a single product from search or product API. */
export function normalizeProduct(data) {
  if (!data) return null
  if (data.id != null) return data
  if (data.value?.id != null) return data.value
  return null
}
