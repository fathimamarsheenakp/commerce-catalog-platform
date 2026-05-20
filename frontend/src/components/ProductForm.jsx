export default function ProductForm({ form, onChange, onSubmit, submitLabel, loading }) {
  function update(field, value) {
    onChange({ ...form, [field]: value })
  }

  return (
    <form className="product-form" onSubmit={onSubmit}>
      <div className="form-grid">
        <label>
          Name
          <input
            value={form.name}
            onChange={(e) => update('name', e.target.value)}
            required
          />
        </label>
        <label>
          Brand
          <input
            value={form.brand}
            onChange={(e) => update('brand', e.target.value)}
            required
          />
        </label>
        <label>
          Category
          <input
            value={form.category}
            onChange={(e) => update('category', e.target.value)}
            required
          />
        </label>
        <label>
          Price
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={form.price}
            onChange={(e) => update('price', e.target.value)}
            required
          />
        </label>
        <label>
          Rating (0–5)
          <input
            type="number"
            min="0"
            max="5"
            step="0.1"
            value={form.rating}
            onChange={(e) => update('rating', e.target.value)}
            required
          />
        </label>
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={form.available}
            onChange={(e) => update('available', e.target.checked)}
          />
          Available
        </label>
      </div>
      <label>
        Description
        <textarea
          rows={4}
          value={form.description}
          onChange={(e) => update('description', e.target.value)}
          required
        />
      </label>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  )
}
