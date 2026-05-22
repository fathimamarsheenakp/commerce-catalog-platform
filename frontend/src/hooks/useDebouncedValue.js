import { useEffect, useState } from 'react'

/** Delays value updates (e.g. search input) to avoid firing API calls on every keystroke. */
export function useDebouncedValue(value, delayMs = 350) {
  const [debounced, setDebounced] = useState(value)

  useEffect(() => {
    const id = window.setTimeout(() => setDebounced(value), delayMs)
    return () => window.clearTimeout(id)
  }, [value, delayMs])

  return debounced
}
