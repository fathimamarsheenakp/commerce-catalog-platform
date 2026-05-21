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
import { useToast } from '../context/useToast'
import { normalizeProduct } from '../utils/normalizeProducts'
import { navigationToastState } from '../utils/navigationToast'

function idsMatch(a, b) {
  return a != null && b != null && String(a) === String(b)
}

export default function ManageProductsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const editId = searchParams.get('edit')
  const isEditMode = Boolean(editId)
  const toast = useToast()

  const [form, setForm] = useState(emptyProduct())
  const [editingId, setEditingId] = useState(null)
  const [loadingEdit, setLoadingEdit] = useState(false)
  const [saving, setSaving] = useState(false)
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
      try {
        const product = normalizeProduct(await getProduct(editId))
        if (!cancelled && product) {
          applyProductToForm(product)
          return
        }
        if (!cancelled) {
          toast.error('Product not found in catalog database.')
        }
      } catch (err) {
        if (cancelled) return
        if (err.status === 404) {
          try {
            const searchOnly = normalizeProduct(await getSearchProduct(editId))
            if (searchOnly) {
              applyProductToForm(searchOnly)
              toast.warning(
                'Loaded from search only. Saving will sync it to the catalog database.',
              )
              return
            }
          } catch {
            /* ignore */
          }
          toast.error(
            'Product not found. Ensure product-service and Cassandra are running.',
          )
        } else if (err.status === 401) {
          toast.error('Please sign in as admin to edit products.')
        } else {
          toast.error(err.message || 'Could not load product for editing')
        }
      } finally {
        if (!cancelled) setLoadingEdit(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [editId, location.state, toast])

  function startCreate() {
    setEditingId(null)
    setForm(emptyProduct())
    setSearchParams({})
    navigate('/manage', { replace: true })
  }

  async function handleSubmit(e) {
    e.preventDefault()

    const validationErrors = validateForm(form)
    if (validationErrors.length > 0) {
      toast.error(validationErrors.join('. '))
      return
    }

    if (isEditMode && !updateId) {
      toast.error('Missing product id. Open Edit from the product page again.')
      return
    }

    setSaving(true)
    try {
      const payload = formToPayload(form)

      if (isEditMode && updateId) {
        await updateProduct(updateId, payload)
        navigate('/', {
          replace: true,
          state: navigationToastState(
            'success',
            'Product updated successfully.',
          ),
        })
      } else {
        const created = await createProduct(payload)
        const message = created?.name
          ? `"${created.name}" created successfully.`
          : 'Product created successfully.'
        navigate('/', {
          replace: true,
          state: navigationToastState('success', message),
        })
      }
    } catch (err) {
      if (err.status === 404) {
        toast.error(
          'Product not found in catalog database. Ensure product-service is running.',
        )
      } else if (err.status === 401 || err.status === 403) {
        toast.error('Not authorized. Sign in as admin to create or edit products.')
      } else {
        toast.error(err.message || 'Save failed')
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
