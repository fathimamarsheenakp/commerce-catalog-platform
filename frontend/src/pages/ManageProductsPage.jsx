import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { createProduct, getProduct, updateProduct } from '../api/products'
import { getSearchProduct } from '../api/search'
import ProductForm from '../components/ProductForm'
import {
  emptyProduct,
  formToPayload,
  productToForm,
  validateForm,
} from '../utils/productFormUtils'
import { normalizeProduct } from '../utils/normalizeProducts'

function idsMatch(a, b) {
  return a != null && b != null && String(a) === String(b)
}

export default function ManageProductsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const editId = searchParams.get('edit')
  const isEditMode = Boolean(editId)

  const [form, setForm] = useState(emptyProduct())
  const [editingId, setEditingId] = useState(null)
  const [loadingEdit, setLoadingEdit] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const updateId = editId ?? editingId

  function applyProductToForm(product) {
    const normalized = normalizeProduct(product)
    if (!normalized) return false
    setEditingId(normalized.id)
    setForm(productToForm(normalized))
    return true
  }

  useEffect(() => {
    if (!editId) return

    const fromNavigation = location.state?.product
    if (fromNavigation && idsMatch(fromNavigation.id, editId)) {
      queueMicrotask(() => applyProductToForm(fromNavigation))
    }

    let cancelled = false
    ;(async () => {
      setLoadingEdit(true)
      setError('')
      try {
        const product = normalizeProduct(await getProduct(editId))
        if (!cancelled && product) {
          applyProductToForm(product)
          return
        }
        if (!cancelled) {
          setError('Product not found in catalog database.')
        }
      } catch (err) {
        if (cancelled) return
        if (err.status === 404) {
          try {
            const searchOnly = normalizeProduct(await getSearchProduct(editId))
            if (searchOnly) {
              applyProductToForm(searchOnly)
              setError(
                'Product loaded from search. Saving will sync it to the catalog database.',
              )
              return
            }
          } catch {
            /* ignore */
          }
          setError(
            'Product not found. Ensure product-service and Cassandra are running.',
          )
        } else if (err.status === 401) {
          setError('Please sign in as admin to edit products.')
        } else {
          setError(err.message || 'Could not load product for editing')
        }
      } finally {
        if (!cancelled) setLoadingEdit(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [editId, location.state])

  function startCreate() {
    setEditingId(null)
    setForm(emptyProduct())
    setError('')
    setSearchParams({})
    navigate('/manage', { replace: true })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    const validationErrors = validateForm(form)
    if (validationErrors.length > 0) {
      setError(validationErrors.join('. '))
      return
    }

    if (isEditMode && !updateId) {
      setError('Missing product id. Open Edit from the product page again.')
      return
    }

    setSaving(true)
    try {
      const payload = formToPayload(form)

      if (isEditMode && updateId) {
        await updateProduct(updateId, payload)
        navigate('/', {
          replace: true,
          state: { catalogMessage: 'Product updated successfully.' },
        })
      } else {
        const created = await createProduct(payload)
        navigate('/', {
          replace: true,
          state: {
            catalogMessage: created?.name
              ? `"${created.name}" created successfully.`
              : 'Product created successfully.',
          },
        })
      }
    } catch (err) {
      if (err.status === 404) {
        setError(
          'Product not found in catalog database. Sign in as admin and ensure product-service is running on port 8081.',
        )
      } else if (err.status === 401 || err.status === 403) {
        setError('Not authorized. Sign in as admin to create or edit products.')
      } else {
        setError(err.message || 'Save failed')
      }
    } finally {
      setSaving(false)
    }
  }

  const showForm = !loadingEdit || editingId != null || isEditMode

  return (
    <div className="page page-narrow">
      <Link
        to={editId ? `/products/${editId}` : '/'}
        className="text-link breadcrumb"
      >
        ← {editId ? 'Back to product' : 'Back to catalog'}
      </Link>

      <header className="page-header">
        <div>
          <h1>{isEditMode ? 'Edit product' : 'Add product'}</h1>
          <p className="muted">
            {isEditMode
              ? 'Update the fields below, then click Update product.'
              : 'Create a new catalog entry (admin only).'}
          </p>
        </div>
        {isEditMode && (
          <button type="button" className="btn btn-ghost" onClick={startCreate}>
            New product
          </button>
        )}
      </header>

      {error && <p className="alert alert-error">{error}</p>}

      <section className="panel">
        {loadingEdit && !showForm && (
          <p className="muted">Loading product…</p>
        )}
        {showForm && (
          <>
            <ProductForm
              form={form}
              onChange={setForm}
              onSubmit={handleSubmit}
              submitLabel={isEditMode ? 'Update product' : 'Create product'}
              loading={saving}
            />
            {isEditMode && (
              <button type="button" className="btn btn-ghost" onClick={startCreate}>
                Cancel edit
              </button>
            )}
          </>
        )}
      </section>
    </div>
  )
}
