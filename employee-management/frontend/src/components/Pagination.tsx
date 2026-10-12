interface Props {
  page: number
  totalPages: number
  totalElements: number
  size: number
  onChange: (page: number) => void
}

export default function Pagination({ page, totalPages, totalElements, size, onChange }: Props) {
  if (totalElements === 0) return null
  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)
  return (
    <div className="pagination">
      <span className="muted">
        Showing {from}–{to} of {totalElements}
      </span>
      <div className="pagination-buttons">
        <button className="btn" disabled={page === 0} onClick={() => onChange(page - 1)}>
          Previous
        </button>
        <span className="muted">
          Page {page + 1} of {Math.max(totalPages, 1)}
        </span>
        <button className="btn" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
          Next
        </button>
      </div>
    </div>
  )
}
