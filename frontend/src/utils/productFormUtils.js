const EMPTY = {
  name: '',
  description: '',
  brand: '',
  category: '',
  price: '',
  available: true,
  rating: '',
}

export function emptyProduct() {
  return { ...EMPTY }
}

export function productToForm(product) {
  return {
    name: product.name ?? '',
    description: product.description ?? '',
    brand: product.brand ?? '',
    category: product.category ?? '',
    price: product.price != null ? String(product.price) : '',
    available: product.available !== false,
    rating: product.rating != null ? String(product.rating) : '',
  }
}

export function formToPayload(form) {
  return {
    name: form.name.trim(),
    description: form.description.trim(),
    brand: form.brand.trim(),
    category: form.category.trim(),
    price: Number(form.price),
    available: Boolean(form.available),
    rating: Number(form.rating),
  }
}

export function validateForm(form) {
  const errors = []

  if (!form.name.trim()) errors.push('Name is required')
  if (!form.description.trim()) errors.push('Description is required')
  if (!form.brand.trim()) errors.push('Brand is required')
  if (!form.category.trim()) errors.push('Category is required')

  const price = Number(form.price)
  if (form.price === '' || Number.isNaN(price) || price <= 0) {
    errors.push('Price must be greater than 0')
  }

  const rating = Number(form.rating)
  if (form.rating === '' || Number.isNaN(rating) || rating < 0) {
    errors.push('Rating must be 0 or higher')
  }

  return errors
}
