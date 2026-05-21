import { Link, Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

export default function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (!isAdmin) {
    return (
      <div className="page-narrow">
        <div className="panel empty-state">
          <h2>Admin access required</h2>
          <p className="muted">
            You are signed in as a read-only user. Creating and editing products
            requires an admin account.
          </p>
          <Link to="/" className="btn btn-primary">
            Back to catalog
          </Link>
        </div>
      </div>
    )
  }

  return children
}
