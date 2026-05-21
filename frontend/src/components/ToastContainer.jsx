const VARIANT_LABEL = {
  success: 'Success',
  error: 'Error',
  info: 'Info',
  warning: 'Notice',
}

export default function ToastContainer({ toasts, onDismiss }) {
  if (toasts.length === 0) return null

  return (
    <div className="toast-viewport" aria-live="polite" aria-relevant="additions">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`toast toast-${toast.variant}`}
          role={toast.variant === 'error' ? 'alert' : 'status'}
        >
          <div className="toast-body">
            <span className="toast-label">{VARIANT_LABEL[toast.variant]}</span>
            <p className="toast-message">{toast.message}</p>
          </div>
          <button
            type="button"
            className="toast-dismiss"
            onClick={() => onDismiss(toast.id)}
            aria-label="Dismiss notification"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  )
}
