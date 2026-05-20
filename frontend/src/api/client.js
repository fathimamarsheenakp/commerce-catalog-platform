const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

export function getToken() {
  return localStorage.getItem('token')
}

export function setToken(token) {
  if (token) {
    localStorage.setItem('token', token)
  } else {
    localStorage.removeItem('token')
  }
}

function handleUnauthorized() {
  setToken(null)
  const onLogin = window.location.pathname.startsWith('/login')
  if (!onLogin) {
    const returnTo = encodeURIComponent(window.location.pathname)
    window.location.assign(`/login?expired=1&from=${returnTo}`)
  }
}

export async function apiRequest(path, options = {}) {
  const { auth = false, ...fetchOptions } = options
  const headers = {
    'Content-Type': 'application/json',
    ...(fetchOptions.headers ?? {}),
  }

  if (auth) {
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...fetchOptions,
      headers,
    })
  } catch {
    const error = new Error(
      'Cannot reach the API. Is the gateway running on port 8080?',
    )
    error.status = 0
    throw error
  }

  if (response.status === 401 && auth) {
    handleUnauthorized()
    const error = new Error('Session expired. Please sign in again.')
    error.status = 401
    throw error
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const body = await response.json()
      if (body?.message) message = body.message
    } catch {
      /* ignore */
    }
    const error = new Error(message)
    error.status = response.status
    throw error
  }

  if (response.status === 204) {
    return null
  }

  const text = await response.text()
  return text ? JSON.parse(text) : null
}
