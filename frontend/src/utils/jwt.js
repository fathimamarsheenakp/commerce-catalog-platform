export function parseJwt(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function getUsernameFromToken(token) {
  const payload = parseJwt(token)
  return payload?.sub ?? null
}

export function getRoleFromToken(token) {
  const payload = parseJwt(token)
  return payload?.role ?? null
}

export function isAdminFromToken(token) {
  return getRoleFromToken(token) === 'ADMIN'
}
