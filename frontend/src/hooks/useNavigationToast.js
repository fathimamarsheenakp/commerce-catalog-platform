import { useEffect, useRef } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useToast } from '../context/useToast'

/**
 * Shows a toast from location.state.toast once per navigation (e.g. after save → catalog).
 */
export function useNavigationToast() {
  const location = useLocation()
  const navigate = useNavigate()
  const toast = useToast()
  const processedKeysRef = useRef(new Set())

  useEffect(() => {
    const payload = location.state?.toast
    if (!payload?.message) return
    if (processedKeysRef.current.has(location.key)) return

    processedKeysRef.current.add(location.key)

    const variant = payload.variant ?? 'success'
    const show = toast[variant]
    if (typeof show === 'function') {
      show(payload.message)
    } else {
      toast.success(payload.message)
    }

    navigate(
      { pathname: location.pathname, search: location.search },
      { replace: true, state: null },
    )
  }, [location.key, location.state, location.pathname, location.search, navigate, toast])
}
