import { apiRequest, setToken } from './client'

export async function login(username, password) {
  const data = await apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  setToken(data.token)
  return data
}

export function logout() {
  setToken(null)
}
