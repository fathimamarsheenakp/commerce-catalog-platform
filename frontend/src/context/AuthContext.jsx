import { useMemo, useState } from 'react'
import { getToken } from '../api/client'
import { login as apiLogin, logout as apiLogout } from '../api/auth'
import { getUsernameFromToken, isAdminFromToken } from '../utils/jwt'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => getToken())

  const username = useMemo(() => getUsernameFromToken(token), [token])
  const isAdmin = useMemo(() => isAdminFromToken(token), [token])

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(token),
      isAdmin,
      username,
      login: async (username, password) => {
        const data = await apiLogin(username, password)
        setTokenState(data.token)
      },
      logout: () => {
        apiLogout()
        setTokenState(null)
      },
      clearSession: () => {
        apiLogout()
        setTokenState(null)
      },
    }),
    [token, username, isAdmin],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
