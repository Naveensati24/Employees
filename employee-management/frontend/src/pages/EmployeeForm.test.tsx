import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import EmployeeForm from './EmployeeForm'
import { validate } from '../validation'

const valid = {
  firstName: 'Ada',
  lastName: 'Lovelace',
  email: 'ada@example.com',
  phone: '',
  department: 'Engineering',
  jobTitle: 'Engineer',
  salary: '120000',
  hireDate: '2022-01-01',
  status: 'ACTIVE' as const,
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }))
}

function renderForm() {
  render(
    <MemoryRouter initialEntries={['/employees/new']}>
      <Routes>
        <Route path="/employees/new" element={<EmployeeForm />} />
        <Route path="/employees" element={<p>employee list</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

afterEach(() => vi.restoreAllMocks())

describe('validate', () => {
  it('accepts a valid employee', () => {
    expect(validate(valid)).toEqual({})
  })

  it('flags missing and malformed fields', () => {
    const errors = validate({ ...valid, firstName: ' ', email: 'nope', salary: '-1', phone: 'abc' })
    expect(Object.keys(errors).sort()).toEqual(['email', 'firstName', 'phone', 'salary'])
  })

  it('rejects hire dates in the future', () => {
    expect(validate({ ...valid, hireDate: '2999-01-01' }).hireDate).toBeDefined()
  })
})

describe('EmployeeForm', () => {
  it('shows client-side errors without calling the API', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation(() => jsonResponse([]))
    renderForm()

    await userEvent.click(screen.getByRole('button', { name: 'Create employee' }))

    expect(screen.getByText('First name is required')).toBeInTheDocument()
    expect(fetchMock.mock.calls.every(([url]) => String(url).endsWith('/departments'))).toBe(true)
  })

  it('shows server errors such as a duplicate email', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((_url, init) =>
      init?.method === 'POST'
        ? jsonResponse({ title: 'Conflict', detail: 'An employee with email ada@example.com already exists' }, 409)
        : jsonResponse([]),
    )
    renderForm()

    await userEvent.type(screen.getByLabelText('First name'), 'Ada')
    await userEvent.type(screen.getByLabelText('Last name'), 'Lovelace')
    await userEvent.type(screen.getByLabelText('Email'), 'ada@example.com')
    await userEvent.type(screen.getByLabelText('Department'), 'Engineering')
    await userEvent.type(screen.getByLabelText('Job title'), 'Engineer')
    await userEvent.type(screen.getByLabelText('Annual salary (USD)'), '120000')
    await userEvent.click(screen.getByRole('button', { name: 'Create employee' }))

    expect(await screen.findByText(/already exists/)).toBeInTheDocument()
  })

  it('navigates to the list after a successful create', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation((_url, init) =>
      init?.method === 'POST' ? jsonResponse({ id: 1 }, 201) : jsonResponse([]),
    )
    renderForm()

    await userEvent.type(screen.getByLabelText('First name'), 'Ada')
    await userEvent.type(screen.getByLabelText('Last name'), 'Lovelace')
    await userEvent.type(screen.getByLabelText('Email'), 'ada@example.com')
    await userEvent.type(screen.getByLabelText('Department'), 'Engineering')
    await userEvent.type(screen.getByLabelText('Job title'), 'Engineer')
    await userEvent.type(screen.getByLabelText('Annual salary (USD)'), '120000')
    await userEvent.click(screen.getByRole('button', { name: 'Create employee' }))

    expect(await screen.findByText('employee list')).toBeInTheDocument()
  })
})
