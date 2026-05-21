import { Link } from 'react-router-dom'
import { formatPrice, formatRating } from '../utils/format'

export default function ProductCard({ product }) {
  const available = product.available !== false

  return (
    <article
      className={`product-card${available ? '' : ' product-card--unavailable'}`}
    >
      <div className="product-card-top">
        <span className="badge">{product.category ?? 'General'}</span>
        {!available && <span className="badge badge-muted">Out of stock</span>}
      </div>
      <h3 className="product-card-title">
        <Link to={`/products/${product.id}`} state={{ product }}>
          {product.name}
        </Link>
      </h3>
      <p className="product-card-desc">{product.description}</p>
      <div className="product-card-meta">
        <span>{product.brand}</span>
        <span className="rating">★ {formatRating(product.rating)}</span>
      </div>
      <div className="product-card-footer">
        <strong className="product-card-price">{formatPrice(product.price)}</strong>
        <Link
          to={`/products/${product.id}`}
          state={{ product }}
          className="btn btn-sm btn-ghost"
        >
          View details
        </Link>
      </div>
    </article>
  )
}
