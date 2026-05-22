export default function Pagination({
  page,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  disabled = false,
}) {
  if (totalPages <= 1 && totalElements <= pageSize) {
    return null
  }

  const current = page + 1
  const canPrev = page > 0
  const canNext = page < totalPages - 1

  return (
    <nav className="pagination" aria-label="Catalog pages">
      <p className="pagination-summary muted">
        Page {current} of {Math.max(totalPages, 1)} · {totalElements} product
        {totalElements === 1 ? '' : 's'}
      </p>
      <div className="pagination-controls">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={disabled || !canPrev}
          onClick={() => onPageChange(page - 1)}
        >
          Previous
        </button>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={disabled || !canNext}
          onClick={() => onPageChange(page + 1)}
        >
          Next
        </button>
      </div>
    </nav>
  )
}
