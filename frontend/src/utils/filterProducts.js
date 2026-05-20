export function filterProducts(products, { keyword = '', category = '', brand = '', sort = '' }) {
  let result = [...products]

  const kw = keyword.trim().toLowerCase()
  if (kw) {
    result = result.filter(
      (p) =>
        p.name?.toLowerCase().includes(kw) ||
        p.description?.toLowerCase().includes(kw) ||
        p.brand?.toLowerCase().includes(kw) ||
        p.category?.toLowerCase().includes(kw),
    )
  }

  if (category) {
    const cat = category.toLowerCase()
    result = result.filter((p) => p.category?.toLowerCase() === cat)
  }

  if (brand) {
    const br = brand.toLowerCase()
    result = result.filter((p) => p.brand?.toLowerCase() === br)
  }

  if (sort === 'priceAsc') {
    result.sort((a, b) => Number(a.price) - Number(b.price))
  } else if (sort === 'priceDesc') {
    result.sort((a, b) => Number(b.price) - Number(a.price))
  } else if (sort === 'rating') {
    result.sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0))
  }

  return result
}

export function countByField(products, field) {
  return products.reduce((acc, p) => {
    const key = p[field]
    if (key) acc[key] = (acc[key] ?? 0) + 1
    return acc
  }, {})
}
