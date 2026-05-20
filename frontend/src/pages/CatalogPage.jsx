import { useCallback, useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import ProductCard from '../components/ProductCard'
import ProductGridSkeleton from '../components/ProductGridSkeleton'
import { getAllProducts } from '../api/search'
import { countByField, filterProducts } from '../utils/filterProducts'
import { normalizeProductList } from '../utils/normalizeProducts'

export default function CatalogPage() {
  const location = useLocation()
  const [catalogMessage] = useState(() => location.state?.catalogMessage ?? '')
  const [allProducts, setAllProducts] = useState([])
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [brand, setBrand] = useState('')
  const [sort, setSort] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (location.state?.catalogMessage) {
      window.history.replaceState({}, document.title)
    }
  }, [location.state])

  const loadCatalog = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getAllProducts()
      setAllProducts(normalizeProductList(data))
    } catch (err) {
      setError(
        err.status === 0
          ? 'Cannot reach the API. Start Docker, api-gateway (:8080), and search-service.'
          : err.message || 'Failed to load products',
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError('')
      try {
        const data = await getAllProducts()
        if (!cancelled) setAllProducts(normalizeProductList(data))
      } catch (err) {
        if (!cancelled) {
          setError(
            err.status === 0
              ? 'Cannot reach the API. Start Docker, api-gateway (:8080), and search-service.'
              : err.message || 'Failed to load products',
          )
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [location.key])

  const categories = useMemo(() => countByField(allProducts, 'category'), [allProducts])
  const brands = useMemo(() => countByField(allProducts, 'brand'), [allProducts])

  const filteredProducts = useMemo(
    () => filterProducts(allProducts, { keyword, category, brand, sort }),
    [allProducts, keyword, category, brand, sort],
  )

  const categoryOptions = Object.keys(categories).sort()
  const brandOptions = Object.keys(brands).sort()
  const hasActiveFilters = Boolean(keyword.trim() || category || brand || sort)
  const hasProducts = allProducts.length > 0
  const showSkeleton = loading && !hasProducts
  const showNoMatches = hasProducts && filteredProducts.length === 0

  function clearFilters() {
    setKeyword('')
    setCategory('')
    setBrand('')
    setSort('')
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Product catalog</h1>
          <p className="muted">
            {loading && !hasProducts
              ? 'Loading catalog…'
              : hasProducts
                ? `Showing ${filteredProducts.length} of ${allProducts.length} products`
                : 'No products loaded'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn-ghost"
          onClick={loadCatalog}
          disabled={loading}
        >
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      <section className="filters panel">
        <form
          className="filter-form"
          onSubmit={(e) => e.preventDefault()}
          aria-label="Filter products"
        >
          <div className="filter-row">
            <label className="filter-grow">
              Search
              <input
                type="search"
                placeholder="Name, description, brand, or category…"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </label>
            <label>
              Category
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="">All</option>
                {categoryOptions.map((c) => (
                  <option key={c} value={c}>
                    {c} ({categories[c]})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Brand
              <select value={brand} onChange={(e) => setBrand(e.target.value)}>
                <option value="">All</option>
                {brandOptions.map((b) => (
                  <option key={b} value={b}>
                    {b} ({brands[b]})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Sort
              <select value={sort} onChange={(e) => setSort(e.target.value)}>
                <option value="">Default</option>
                <option value="priceAsc">Price: low to high</option>
                <option value="priceDesc">Price: high to low</option>
                <option value="rating">Rating</option>
              </select>
            </label>
          </div>
          <div className="filter-actions">
            {hasActiveFilters && (
              <button type="button" className="btn btn-ghost" onClick={clearFilters}>
                Clear filters
              </button>
            )}
          </div>
        </form>
      </section>

      {catalogMessage && (
        <p className="alert alert-success">{catalogMessage}</p>
      )}
      {error && <p className="alert alert-error">{error}</p>}

      {loading && hasProducts && (
        <p className="loading-banner" role="status">
          Refreshing catalog…
        </p>
      )}

      {showSkeleton && <ProductGridSkeleton />}

      {!showSkeleton && !hasProducts && !error && (
        <div className="empty-state panel">
          <p>No products in the catalog yet.</p>
          <p className="muted">
            Sign in under Manage to add products. New items appear here after Kafka syncs to search (a few seconds).
          </p>
        </div>
      )}

      {showNoMatches && (
        <div className="empty-state panel">
          <p>No products match your filters.</p>
          <button type="button" className="btn btn-ghost" onClick={clearFilters}>
            Clear filters
          </button>
        </div>
      )}

      {hasProducts && !showNoMatches && (
        <div className="product-grid">
          {filteredProducts.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </div>
  )
}
