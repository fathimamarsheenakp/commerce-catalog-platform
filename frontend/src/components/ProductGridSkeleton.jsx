export default function ProductGridSkeleton({ count = 6 }) {
  return (
    <div className="product-grid" aria-busy="true" aria-label="Loading products">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="product-card skeleton-card">
          <div className="skeleton skeleton-badge" />
          <div className="skeleton skeleton-title" />
          <div className="skeleton skeleton-line" />
          <div className="skeleton skeleton-line short" />
          <div className="skeleton skeleton-footer" />
        </div>
      ))}
    </div>
  )
}
