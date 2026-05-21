import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import Layout from './components/Layout'
import AdminRoute from './components/AdminRoute'
import CatalogPage from './pages/CatalogPage'
import LoginPage from './pages/LoginPage'
import ManageProductsPage from './pages/ManageProductsPage'
import ProductDetailPage from './pages/ProductDetailPage'

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<CatalogPage />} />
            <Route path="products/:id" element={<ProductDetailPage />} />
            <Route path="login" element={<LoginPage />} />
            <Route
              path="manage"
              element={
                <AdminRoute>
                  <ManageProductsPage />
                </AdminRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  )
}
