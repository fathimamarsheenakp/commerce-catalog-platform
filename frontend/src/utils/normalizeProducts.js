/** Normalize API responses into a plain product array. */
export function normalizeProductList(data) {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.content)) return data.content
  if (Array.isArray(data._embedded?.products)) return data._embedded.products
  return []
}

/** Normalize a single product from search or product API. */
export function normalizeProduct(data) {
  if (!data) return null
  if (data.id != null) return data
  if (data.value?.id != null) return data.value
  return null
}
