import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

export default function Layout() {
  const { isAuthenticated, username, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="brand">
          <span className="brand-mark">CC</span>
          <span className="brand-text">Commerce Catalog</span>
        </Link>
        <nav className="app-nav">
          <NavLink to="/" end>
            Browse
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/manage" end>
              Add product
            </NavLink>
          )}
        </nav>
        <div className="header-actions">
          {isAuthenticated && (
            <span className="user-badge" title="Signed in">
              {username ?? 'Admin'}
            </span>
          )}
          {isAuthenticated ? (
            <button type="button" className="btn btn-ghost" onClick={logout}>
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
        Commerce Catalog Platform — product-service &amp; search-service
      </footer>
    </div>
  )
}
