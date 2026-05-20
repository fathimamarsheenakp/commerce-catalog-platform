import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { deleteProduct } from '../api/products'
import { getSearchProduct } from '../api/search'
import { useAuth } from '../context/useAuth'
import { normalizeProduct } from '../utils/normalizeProducts'

function formatPrice(price) {
  if (price == null) return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(Number(price))
}

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
      navigate('/', { replace: true })
    } catch (err) {
      setError(err.message || 'Delete failed')
      setDeleting(false)
    }
  }

  if (loading) {
    return <p className="muted center">Loading…</p>
  }

  if (error && !product) {
    return (
      <div className="page-narrow">
        <div className="panel">
          <p className="alert alert-error">{error || 'Product not found'}</p>
          <Link to="/" className="text-link">
            ← Back to catalog
          </Link>
        </div>
      </div>
    )
  }

  if (!product) {
    return null
  }

  return (
    <div className="page-narrow">
      <Link to="/" className="text-link breadcrumb">
        ← Back to catalog
      </Link>
      {syncNote && (
        <p className="alert alert-success">
          Saved. Catalog search may take a few seconds to reflect changes.
        </p>
      )}
      <article className="panel product-detail">
        <div className="detail-header">
          <div className="detail-badges">
            <span className="badge">{product.category}</span>
            {product.available === false && (
              <span className="badge badge-muted">Out of stock</span>
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
            <dd>★ {product.rating?.toFixed(1) ?? '—'}</dd>
          </div>
          <div>
            <dt>Availability</dt>
            <dd>{product.available !== false ? 'In stock' : 'Out of stock'}</dd>
          </div>
          <div>
            <dt>ID</dt>
            <dd className="mono">{product.id}</dd>
          </div>
        </dl>
        <section>
          <h2>Description</h2>
          <p>{product.description}</p>
        </section>
        {error && <p className="alert alert-error">{error}</p>}
      </article>
    </div>
  )
}
