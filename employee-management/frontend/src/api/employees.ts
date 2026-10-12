import type { Employee, EmployeeInput, EmployeeQuery, EmployeeStats, Page } from '../types'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api/employees`

/** Error thrown for non-2xx responses; carries per-field validation errors from the backend. */
export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      ...init,
      headers: { 'Content-Type': 'application/json', ...init?.headers },
    })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Is the backend running on port 8080?')
  }
  if (!res.ok) {
    const problem = await res.json().catch(() => ({}))
    throw new ApiError(res.status, problem.detail ?? `Request failed (${res.status})`, problem.errors ?? {})
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

function toQueryString(query: EmployeeQuery): string {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== '') params.set(key, String(value))
  }
  const qs = params.toString()
  return qs ? `?${qs}` : ''
}

function toPayload(input: EmployeeInput) {
  return { ...input, salary: Number(input.salary), phone: input.phone.trim() || null }
}

export const employeeApi = {
  list: (query: EmployeeQuery) => request<Page<Employee>>(toQueryString(query)),
  get: (id: number) => request<Employee>(`/${id}`),
  create: (input: EmployeeInput) =>
    request<Employee>('', { method: 'POST', body: JSON.stringify(toPayload(input)) }),
  update: (id: number, input: EmployeeInput) =>
    request<Employee>(`/${id}`, { method: 'PUT', body: JSON.stringify(toPayload(input)) }),
  remove: (id: number) => request<void>(`/${id}`, { method: 'DELETE' }),
  stats: () => request<EmployeeStats>('/stats'),
  departments: () => request<string[]>('/departments'),
}
