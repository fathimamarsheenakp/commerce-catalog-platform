import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

const DEMO_USER = 'admin'
const DEMO_PASS = 'password'

export default function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const sessionExpired = searchParams.get('expired') === '1'
  const from =
    location.state?.from?.pathname ??
    (searchParams.get('from') ? decodeURIComponent(searchParams.get('from')) : '/manage')

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={from} replace />
  }

  function fillDemoCredentials() {
    setUsername(DEMO_USER)
    setPassword(DEMO_PASS)
    setError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-narrow">
      <div className="panel">
        <h1>Sign in</h1>
        <p className="muted">
          Admin access is required to create, update, or delete products.
        </p>
        {sessionExpired && (
          <p className="alert alert-error">Your session expired. Please sign in again.</p>
        )}
        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              placeholder="Enter username"
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              placeholder="Enter password"
              required
            />
          </label>
          {error && <p className="alert alert-error">{error}</p>}
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        {import.meta.env.DEV && (
          <div className="demo-actions">
            <button
              type="button"
              className="btn btn-ghost btn-block"
              onClick={fillDemoCredentials}
            >
              Use demo account
            </button>
            <p className="muted hint">
              Local dev only: <code>{DEMO_USER}</code> / <code>{DEMO_PASS}</code>
            </p>
          </div>
        )}
        <Link to="/" className="text-link">
          ← Back to catalog
        </Link>
      </div>
    </div>
  )
}
