import { useCallback, useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import Pagination from '../components/Pagination'
import ProductCard from '../components/ProductCard'
import ProductGridSkeleton from '../components/ProductGridSkeleton'
import {
  DEFAULT_PAGE_SIZE,
  getBrandAggregations,
  getCategoryAggregations,
  getProductsPage,
} from '../api/search'
import { useDebouncedValue } from '../hooks/useDebouncedValue'
import { useNavigationToast } from '../hooks/useNavigationToast'
import { useToast } from '../context/useToast'
import { normalizePageResponse } from '../utils/normalizeProducts'

export default function CatalogPage() {
  const location = useLocation()
  useNavigationToast()
  const toast = useToast()
  const toastRef = useRef(toast)
  toastRef.current = toast

  const [products, setProducts] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [keyword, setKeyword] = useState('')
  const debouncedKeyword = useDebouncedValue(keyword)
  const [category, setCategory] = useState('')
  const [brand, setBrand] = useState('')
  const [sort, setSort] = useState('')
  const [categories, setCategories] = useState({})
  const [brands, setBrands] = useState({})
  const [loading, setLoading] = useState(true)
  const [loadFailed, setLoadFailed] = useState(false)
  const filterKey = `${debouncedKeyword}|${category}|${brand}|${sort}`
  const prevFilterKeyRef = useRef(filterKey)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const [cat, br] = await Promise.all([
          getCategoryAggregations(),
          getBrandAggregations(),
        ])
        if (!cancelled) {
          setCategories(cat ?? {})
          setBrands(br ?? {})
        }
      } catch {
        /* filter dropdowns optional */
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const loadCatalog = useCallback(async (pageToLoad) => {
    setLoading(true)
    setLoadFailed(false)
    try {
      const data = await getProductsPage({
        page: pageToLoad,
        size: DEFAULT_PAGE_SIZE,
        keyword: debouncedKeyword,
        category,
        brand,
        sort,
      })
      const result = normalizePageResponse(data)
      setProducts(result.content)
      setPage(result.page)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      setLoadFailed(true)
      setProducts([])
      const message =
        err.status === 0
          ? 'Cannot reach the API. Start Docker, api-gateway (:8080), and search-service.'
          : err.message || 'Failed to load products'
      toastRef.current.error(message)
    } finally {
      setLoading(false)
    }
  }, [debouncedKeyword, category, brand, sort])

  useEffect(() => {
    const filtersChanged = prevFilterKeyRef.current !== filterKey
    prevFilterKeyRef.current = filterKey
    const pageToLoad = filtersChanged ? 0 : page
    if (filtersChanged && page !== 0) {
      setPage(0)
    }
    loadCatalog(pageToLoad)
  }, [page, filterKey, location.key, loadCatalog])

  function clearFilters() {
    setKeyword('')
    setCategory('')
    setBrand('')
    setSort('')
    setPage(0)
  }

  const categoryOptions = Object.keys(categories).sort()
  const brandOptions = Object.keys(brands).sort()
  const hasActiveFilters = Boolean(
    debouncedKeyword.trim() || category || brand || sort,
  )
  const hasProducts = products.length > 0
  const showSkeleton = loading && !hasProducts
  const showEmpty = !loading && !loadFailed && totalElements === 0
  const showNoMatches = !loading && !loadFailed && totalElements === 0 && hasActiveFilters

  const rangeStart = totalElements === 0 ? 0 : page * DEFAULT_PAGE_SIZE + 1
  const rangeEnd = Math.min((page + 1) * DEFAULT_PAGE_SIZE, totalElements)

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Product catalog</h1>
          <p className="muted page-subtitle">
            {loading && !hasProducts
              ? 'Loading catalog…'
              : totalElements > 0
                ? `Showing ${rangeStart}–${rangeEnd} of ${totalElements} products`
                : loadFailed
                  ? 'Could not load products'
                  : 'No products in catalog'}
          </p>
        </div>
        <button
          type="button"
          className="btn btn-ghost"
          onClick={() => loadCatalog(page)}
          disabled={loading}
          aria-busy={loading}
        >
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      <section className="filters panel">
        <h2 className="filters-title">Filter &amp; sort</h2>
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
                <option value="">Name (A–Z)</option>
                <option value="priceAsc">Price: low to high</option>
                <option value="priceDesc">Price: high to low</option>
                <option value="rating">Rating</option>
              </select>
            </label>
          </div>
          {hasActiveFilters && (
            <div className="filter-actions">
              <button type="button" className="btn btn-ghost btn-sm" onClick={clearFilters}>
                Clear filters
              </button>
            </div>
          )}
        </form>
      </section>

      {loading && hasProducts && (
        <p className="loading-banner" role="status">
          Loading page…
        </p>
      )}

      {showSkeleton && <ProductGridSkeleton />}

      {showEmpty && !hasActiveFilters && (
        <div className="empty-state panel">
          <p className="empty-state-title">No products yet</p>
          <p className="muted">
            Sign in as admin to add products. New items appear here after Kafka
            syncs to search (usually a few seconds).
          </p>
        </div>
      )}

      {showNoMatches && (
        <div className="empty-state panel">
          <p className="empty-state-title">No matches</p>
          <p className="muted">Try different filters or clear them all.</p>
          <button type="button" className="btn btn-ghost" onClick={clearFilters}>
            Clear filters
          </button>
        </div>
      )}

      {loadFailed && !hasProducts && (
        <div className="empty-state panel">
          <p className="empty-state-title">Catalog unavailable</p>
          <p className="muted">Check the toast message and try Refresh.</p>
          <button type="button" className="btn btn-primary" onClick={() => loadCatalog(0)}>
            Retry
          </button>
        </div>
      )}

      {hasProducts && (
        <>
          <div className="product-grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={DEFAULT_PAGE_SIZE}
            onPageChange={setPage}
            disabled={loading}
          />
        </>
      )}
    </div>
  )
}
