export function formatPrice(price) {
  if (price == null || Number.isNaN(Number(price))) return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(Number(price))
}

export function formatRating(rating) {
  if (rating == null || Number.isNaN(Number(rating))) return '—'
  return Number(rating).toFixed(1)
}
