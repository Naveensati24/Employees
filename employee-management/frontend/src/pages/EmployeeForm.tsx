import { useEffect, useState, type FormEvent, type InputHTMLAttributes } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError, employeeApi } from '../api/employees'
import { STATUSES, STATUS_LABELS, type EmployeeInput } from '../types'
import { validate, type Errors } from '../validation'

const EMPTY: EmployeeInput = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  department: '',
  jobTitle: '',
  salary: '',
  hireDate: new Date().toISOString().slice(0, 10),
  status: 'ACTIVE',
}

export default function EmployeeForm() {
  const { id } = useParams()
  const isEdit = id !== undefined
  const navigate = useNavigate()

  const [values, setValues] = useState<EmployeeInput>(EMPTY)
  const [errors, setErrors] = useState<Errors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [departments, setDepartments] = useState<string[]>([])

  useEffect(() => {
    employeeApi.departments().then(setDepartments).catch(() => setDepartments([]))
    if (!isEdit) return
    employeeApi
      .get(Number(id))
      .then((e) =>
        setValues({
          firstName: e.firstName,
          lastName: e.lastName,
          email: e.email,
          phone: e.phone ?? '',
          department: e.department,
          jobTitle: e.jobTitle,
          salary: String(e.salary),
          hireDate: e.hireDate,
          status: e.status,
        }),
      )
      .catch((e: Error) => setFormError(e.message))
      .finally(() => setLoading(false))
  }, [id, isEdit])

  const set = (field: keyof EmployeeInput) => (e: { target: { value: string } }) => {
    setValues((v) => ({ ...v, [field]: e.target.value }))
    setErrors((errs) => ({ ...errs, [field]: undefined }))
  }

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const clientErrors = validate(values)
    setErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return

    setSaving(true)
    setFormError(null)
    try {
      if (isEdit) await employeeApi.update(Number(id), values)
      else await employeeApi.create(values)
      navigate('/employees')
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors as Errors)
        setFormError(err.message)
      } else {
        setFormError('Something went wrong')
      }
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="muted">Loading employee…</p>

  const field = (name: keyof EmployeeInput, label: string, props: InputHTMLAttributes<HTMLInputElement> = {}) => (
    <label className="field">
      <span>{label}</span>
      <input
        className={`input ${errors[name] ? 'input-invalid' : ''}`}
        name={name}
        value={values[name]}
        onChange={set(name)}
        aria-invalid={Boolean(errors[name])}
        {...props}
      />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </label>
  )

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{isEdit ? 'Edit employee' : 'New employee'}</h1>
          <p className="muted">{isEdit ? 'Update the employee record' : 'Add a new person to the directory'}</p>
        </div>
      </div>

      <form className="card form" onSubmit={onSubmit} noValidate>
        {formError && <div className="alert alert-error">{formError}</div>}
        <div className="form-grid">
          {field('firstName', 'First name')}
          {field('lastName', 'Last name')}
          {field('email', 'Email', { type: 'email' })}
          {field('phone', 'Phone (optional)', { type: 'tel' })}
          {field('department', 'Department', { list: 'department-options' })}
          <datalist id="department-options">
            {departments.map((d) => (
              <option key={d} value={d} />
            ))}
          </datalist>
          {field('jobTitle', 'Job title')}
          {field('salary', 'Annual salary (USD)', { type: 'number', min: 0, step: '0.01' })}
          {field('hireDate', 'Hire date', { type: 'date' })}
          <label className="field">
            <span>Status</span>
            <select className="input" name="status" value={values.status} onChange={set('status')}>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="form-actions">
          <Link to="/employees" className="btn">
            Cancel
          </Link>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save changes' : 'Create employee'}
          </button>
        </div>
      </form>
    </>
  )
}
