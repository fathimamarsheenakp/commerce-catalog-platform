import { useCallback, useMemo, useState } from 'react'
import ToastContainer from '../components/ToastContainer'
import { ToastContext } from './toast-context'

let toastId = 0

const DEFAULT_DURATION = {
  success: 4500,
  error: 6000,
  info: 4000,
  warning: 5000,
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const addToast = useCallback(
    (variant, message, durationMs) => {
      const id = ++toastId
      const duration = durationMs ?? DEFAULT_DURATION[variant] ?? 4000

      setToasts((prev) => [...prev, { id, variant, message }])

      if (duration > 0) {
        window.setTimeout(() => dismiss(id), duration)
      }

      return id
    },
    [dismiss],
  )

  const value = useMemo(
    () => ({
      success: (message, durationMs) => addToast('success', message, durationMs),
      error: (message, durationMs) => addToast('error', message, durationMs),
      info: (message, durationMs) => addToast('info', message, durationMs),
      warning: (message, durationMs) => addToast('warning', message, durationMs),
      dismiss,
    }),
    [addToast, dismiss],
  )

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastContainer toasts={toasts} onDismiss={dismiss} />
    </ToastContext.Provider>
  )
}
