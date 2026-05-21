export default function DetailSkeleton() {
  return (
    <div className="page-narrow" aria-busy="true" aria-label="Loading product">
      <div className="skeleton skeleton-line short breadcrumb-skeleton" />
      <div className="panel product-detail">
        <div className="detail-header">
          <div className="skeleton skeleton-badge" />
        </div>
        <div className="skeleton skeleton-title detail-skeleton-title" />
        <div className="skeleton skeleton-line detail-skeleton-price" />
        <div className="detail-meta">
          {[1, 2, 3, 4].map((n) => (
            <div key={n}>
              <div className="skeleton skeleton-line short" />
              <div className="skeleton skeleton-line" />
            </div>
          ))}
        </div>
        <div className="skeleton skeleton-line" />
        <div className="skeleton skeleton-line" />
        <div className="skeleton skeleton-line short" />
      </div>
    </div>
  )
}
