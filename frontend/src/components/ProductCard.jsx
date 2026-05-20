import { Link } from 'react-router-dom'

function formatPrice(price) {
  if (price == null) return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(Number(price))
}

export default function ProductCard({ product }) {
  const available = product.available !== false

  return (
    <article className="product-card">
      <div className="product-card-top">
        <span className="badge">{product.category ?? 'General'}</span>
        {!available && <span className="badge badge-muted">Out of stock</span>}
      </div>
      <h3 className="product-card-title">
        <Link to={`/products/${product.id}`}>{product.name}</Link>
      </h3>
      <p className="product-card-desc">{product.description}</p>
      <div className="product-card-meta">
        <span>{product.brand}</span>
        <span className="rating">★ {product.rating?.toFixed(1) ?? '—'}</span>
      </div>
      <div className="product-card-footer">
        <strong>{formatPrice(product.price)}</strong>
        <Link to={`/products/${product.id}`} className="btn btn-sm btn-ghost">
          View
        </Link>
      </div>
    </article>
  )
}
