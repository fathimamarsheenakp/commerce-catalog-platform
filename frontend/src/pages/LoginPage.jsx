import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

const DEMO_ADMIN = 'admin'
const DEMO_USER = 'user'
const DEMO_PASS = 'password'

export default function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const sessionExpired = searchParams.get('expired') === '1'
  const from =
    location.state?.from?.pathname ??
    (searchParams.get('from')
      ? decodeURIComponent(searchParams.get('from'))
      : '/')

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={from} replace />
  }

  function fillDemo(which) {
    setUsername(which === 'admin' ? DEMO_ADMIN : DEMO_USER)
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
    <div className="login-page">
      <div className="panel login-panel">
        <div className="login-panel-header">
          <span className="brand-mark brand-mark-lg" aria-hidden="true">
            CC
          </span>
          <h1>Sign in</h1>
          <p className="muted">
            Browse the catalog without signing in. Sign in to access admin
            features or a read-only user account.
          </p>
        </div>

        {sessionExpired && (
          <p className="alert alert-error" role="alert">
            Your session expired. Please sign in again.
          </p>
        )}

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              placeholder="admin or user"
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
          {error && (
            <p className="alert alert-error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        {import.meta.env.DEV && (
          <div className="demo-actions">
            <p className="demo-actions-label muted">Local development</p>
            <div className="demo-actions-row">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => fillDemo('admin')}
              >
                Admin demo
              </button>
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => fillDemo('user')}
              >
                User demo
              </button>
            </div>
            <p className="muted hint">
              <code>admin</code> / <code>user</code> — password <code>{DEMO_PASS}</code>
            </p>
          </div>
        )}

        <Link to="/" className="text-link login-back">
          ← Back to catalog
        </Link>
      </div>
    </div>
  )
}
