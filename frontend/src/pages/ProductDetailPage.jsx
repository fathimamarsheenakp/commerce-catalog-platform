import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import DetailSkeleton from '../components/DetailSkeleton'
import { deleteProduct } from '../api/products'
import { getSearchProduct } from '../api/search'
import { useAuth } from '../context/useAuth'
import { formatPrice, formatRating } from '../utils/format'
import { normalizeProduct } from '../utils/normalizeProducts'

function idsMatch(a, b) {
  return a != null && b != null && String(a) === String(b)
}

export default function ProductDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { isAdmin } = useAuth()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')
  const [syncNote, setSyncNote] = useState(false)

  useEffect(() => {
    const fromNavigation = location.state?.product
    if (fromNavigation && idsMatch(fromNavigation.id, id)) {
      queueMicrotask(() => {
        setProduct(normalizeProduct(fromNavigation))
        setLoading(false)
        setSyncNote(Boolean(location.state?.fromUpdate))
      })
      return
    }

    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      setSyncNote(false)
      try {
        const data = await getSearchProduct(id)
        if (!cancelled) setProduct(normalizeProduct(data))
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Product not found')
          setProduct(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [id, location.state])

  async function handleDelete() {
    if (!product || !window.confirm(`Delete "${product.name}"?`)) return
    setDeleting(true)
    setError('')
    try {
      await deleteProduct(product.id)
      navigate('/', {
        replace: true,
        state: { catalogMessage: `"${product.name}" was deleted.` },
      })
    } catch (err) {
      setError(err.message || 'Delete failed')
      setDeleting(false)
    }
  }

  if (loading) {
    return <DetailSkeleton />
  }

  if (error && !product) {
    return (
      <div className="page-narrow">
        <div className="panel empty-state">
          <p className="empty-state-title">Product not found</p>
          <p className="alert alert-error" role="alert">
            {error}
          </p>
          <Link to="/" className="btn btn-primary">
            Back to catalog
          </Link>
        </div>
      </div>
    )
  }

  if (!product) {
    return null
  }

  const available = product.available !== false

  return (
    <div className="page-narrow">
      <Link to="/" className="text-link breadcrumb">
        ← Back to catalog
      </Link>
      {syncNote && (
        <p className="alert alert-success" role="status">
          Saved. Catalog search may take a few seconds to reflect changes.
        </p>
      )}
      <article className="panel product-detail">
        <div className="detail-header">
          <div className="detail-badges">
            <span className="badge">{product.category}</span>
            <span className="badge badge-muted">{product.brand}</span>
            {!available && (
              <span className="badge badge-warn">Out of stock</span>
            )}
          </div>
          {isAdmin && (
            <div className="detail-actions">
              <Link
                to={`/manage?edit=${product.id}`}
                state={{ product }}
                className="btn btn-primary btn-sm"
              >
                Edit
              </Link>
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting…' : 'Delete'}
              </button>
            </div>
          )}
        </div>
        <h1>{product.name}</h1>
        <p className="detail-price">{formatPrice(product.price)}</p>
        <dl className="detail-meta">
          <div>
            <dt>Brand</dt>
            <dd>{product.brand}</dd>
          </div>
          <div>
            <dt>Rating</dt>
            <dd className="rating">★ {formatRating(product.rating)}</dd>
          </div>
          <div>
            <dt>Availability</dt>
            <dd>{available ? 'In stock' : 'Out of stock'}</dd>
          </div>
          <div>
            <dt>Product ID</dt>
            <dd className="mono">{product.id}</dd>
          </div>
        </dl>
        <section className="detail-description">
          <h2>Description</h2>
          <p>{product.description || 'No description provided.'}</p>
        </section>
        {error && (
          <p className="alert alert-error" role="alert">
            {error}
          </p>
        )}
      </article>
    </div>
  )
}
