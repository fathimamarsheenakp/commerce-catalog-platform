import { useEffect, useState } from 'react'

export default function AlertBanner({
  variant = 'success',
  children,
  onDismiss,
  autoDismissMs = 0,
}) {
  const [visible, setVisible] = useState(Boolean(children))

  useEffect(() => {
    setVisible(Boolean(children))
  }, [children])

  useEffect(() => {
    if (!autoDismissMs || !visible) return undefined
    const id = window.setTimeout(() => {
      setVisible(false)
      onDismiss?.()
    }, autoDismissMs)
    return () => window.clearTimeout(id)
  }, [autoDismissMs, visible, onDismiss])

  if (!visible || !children) return null

  return (
    <div
      className={`alert alert-${variant} alert-banner`}
      role={variant === 'error' ? 'alert' : 'status'}
    >
      <span className="alert-banner-text">{children}</span>
      {onDismiss && (
        <button
          type="button"
          className="alert-dismiss"
          onClick={() => {
            setVisible(false)
            onDismiss()
          }}
          aria-label="Dismiss"
        >
          ×
        </button>
      )}
    </div>
  )
}
