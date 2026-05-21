import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { useToast } from '../context/useToast'

export default function Layout() {
  const { isAuthenticated, isAdmin, username, logout } = useAuth()
  const toast = useToast()

  function handleSignOut() {
    logout()
    toast.info('Signed out')
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="brand">
          <span className="brand-mark" aria-hidden="true">
            CC
          </span>
          <span className="brand-text">Commerce Catalog</span>
        </Link>
        <nav className="app-nav" aria-label="Main">
          <NavLink to="/" end>
            Browse
          </NavLink>
          {isAdmin && (
            <NavLink to="/manage" end>
              Manage
            </NavLink>
          )}
        </nav>
        <div className="header-actions">
          {isAuthenticated && (
            <span
              className={`role-badge ${isAdmin ? 'role-badge-admin' : 'role-badge-user'}`}
              title={isAdmin ? 'Administrator' : 'Read-only user'}
            >
              {username ?? 'User'}
              <span className="role-badge-label">
                {isAdmin ? 'Admin' : 'User'}
              </span>
            </span>
          )}
          {isAuthenticated ? (
            <button type="button" className="btn btn-ghost" onClick={handleSignOut}>
              Sign out
            </button>
          ) : (
            <Link to="/login" className="btn btn-primary">
              Sign in
            </Link>
          )}
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
      <footer className="app-footer">
        Commerce Catalog Platform — API gateway, search &amp; product services
      </footer>
    </div>
  )
}
