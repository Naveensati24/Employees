import { useCallback, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { employeeApi } from '../api/employees'
import ConfirmDialog from '../components/ConfirmDialog'
import Pagination from '../components/Pagination'
import StatusBadge from '../components/StatusBadge'
import { formatCurrency, formatDate } from '../format'
import { useDebounced } from '../hooks'
import { STATUSES, STATUS_LABELS, type Employee, type EmployeeStatus, type Page } from '../types'

const PAGE_SIZE = 10

const COLUMNS: { key: string; label: string }[] = [
  { key: 'lastName', label: 'Name' },
  { key: 'department', label: 'Department' },
  { key: 'jobTitle', label: 'Title' },
  { key: 'salary', label: 'Salary' },
  { key: 'hireDate', label: 'Hired' },
  { key: 'status', label: 'Status' },
]

export default function EmployeeList() {
  // Filters live in the URL so views are shareable and survive refresh/back navigation.
  const [params, setParams] = useSearchParams()
  const department = params.get('department') ?? ''
  const status = (params.get('status') ?? '') as EmployeeStatus | ''
  const sortBy = params.get('sortBy') ?? 'lastName'
  const direction = params.get('direction') === 'desc' ? 'desc' : 'asc'
  const page = Number(params.get('page') ?? 0)

  const [searchInput, setSearchInput] = useState(params.get('search') ?? '')
  const search = useDebounced(searchInput)

  const [data, setData] = useState<Page<Employee> | null>(null)
  const [departments, setDepartments] = useState<string[]>([])
  const [error, setError] = useState<string | null>(null)
  const [reloadToken, setReloadToken] = useState(0)
  const [loadedKey, setLoadedKey] = useState<string | null>(null)
  const [toDelete, setToDelete] = useState<Employee | null>(null)
  const [deleting, setDeleting] = useState(false)

  const update = useCallback(
    (changes: Record<string, string | number>, resetPage = true) => {
      setParams((prev) => {
        const next = new URLSearchParams(prev)
        for (const [k, v] of Object.entries(changes)) {
          if (v === '' || v === undefined) next.delete(k)
          else next.set(k, String(v))
        }
        if (resetPage) next.delete('page')
        return next
      })
    },
    [setParams],
  )

  useEffect(() => {
    if (search !== (params.get('search') ?? '')) update({ search })
  }, [search, params, update])

  const requestKey = `${params.toString()}#${reloadToken}`
  const loading = loadedKey !== requestKey

  useEffect(() => {
    let ignore = false
    employeeApi
      .list({ search: params.get('search') ?? '', department, status, sortBy, direction, page, size: PAGE_SIZE })
      .then((res) => {
        if (ignore) return
        setData(res)
        setError(null)
      })
      .catch((e: Error) => !ignore && setError(e.message))
      .finally(() => !ignore && setLoadedKey(requestKey))
    return () => {
      ignore = true
    }
  }, [requestKey, params, department, status, sortBy, direction, page])

  useEffect(() => {
    employeeApi.departments().then(setDepartments).catch(() => setDepartments([]))
  }, [])

  const toggleSort = (key: string) =>
    update({ sortBy: key, direction: sortBy === key && direction === 'asc' ? 'desc' : 'asc' })

  const confirmDelete = async () => {
    if (!toDelete) return
    setDeleting(true)
    try {
      await employeeApi.remove(toDelete.id)
      setToDelete(null)
      // Step back a page if we just removed the last row on it.
      if (data && data.content.length === 1 && page > 0) update({ page: page - 1 }, false)
      else setReloadToken((t) => t + 1)
    } catch (e) {
      setError((e as Error).message)
      setToDelete(null)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Employees</h1>
          <p className="muted">{data ? `${data.totalElements} matching records` : ' '}</p>
        </div>
        <Link to="/employees/new" className="btn btn-primary">
          + Add employee
        </Link>
      </div>

      <div className="toolbar">
        <input
          type="search"
          className="input grow"
          placeholder="Search name, email or title…"
          aria-label="Search employees"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
        />
        <select
          className="input"
          aria-label="Filter by department"
          value={department}
          onChange={(e) => update({ department: e.target.value })}
        >
          <option value="">All departments</option>
          {departments.map((d) => (
            <option key={d}>{d}</option>
          ))}
        </select>
        <select
          className="input"
          aria-label="Filter by status"
          value={status}
          onChange={(e) => update({ status: e.target.value })}
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {STATUS_LABELS[s]}
            </option>
          ))}
        </select>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card table-card">
        <table className="table">
          <thead>
            <tr>
              {COLUMNS.map((c) => (
                <th key={c.key} aria-sort={sortBy === c.key ? (direction === 'asc' ? 'ascending' : 'descending') : 'none'}>
                  <button className="sort-btn" onClick={() => toggleSort(c.key)}>
                    {c.label}
                    <span className="sort-indicator">{sortBy === c.key ? (direction === 'asc' ? '▲' : '▼') : ''}</span>
                  </button>
                </th>
              ))}
              <th className="actions-col">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody className={loading ? 'loading' : undefined}>
            {data?.content.map((e) => (
              <tr key={e.id}>
                <td>
                  <div className="name-cell">
                    <span className="avatar" aria-hidden="true">
                      {e.firstName[0]}
                      {e.lastName[0]}
                    </span>
                    <div>
                      <div className="strong">
                        {e.firstName} {e.lastName}
                      </div>
                      <div className="muted small">{e.email}</div>
                    </div>
                  </div>
                </td>
                <td>{e.department}</td>
                <td>{e.jobTitle}</td>
                <td className="num">{formatCurrency(e.salary)}</td>
                <td>{formatDate(e.hireDate)}</td>
                <td>
                  <StatusBadge status={e.status} />
                </td>
                <td className="actions-col">
                  <Link to={`/employees/${e.id}/edit`} className="btn btn-small">
                    Edit
                  </Link>
                  <button className="btn btn-small btn-danger-ghost" onClick={() => setToDelete(e)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {data && data.content.length === 0 && (
              <tr>
                <td colSpan={COLUMNS.length + 1} className="empty">
                  No employees match your filters.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {data && (
        <Pagination
          page={data.page}
          size={data.size}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onChange={(p) => update({ page: p }, false)}
        />
      )}

      {toDelete && (
        <ConfirmDialog
          title="Delete employee?"
          message={`${toDelete.firstName} ${toDelete.lastName} will be permanently removed.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  )
}
