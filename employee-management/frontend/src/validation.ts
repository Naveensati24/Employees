import type { EmployeeInput } from './types'

export type Errors = Partial<Record<keyof EmployeeInput, string>>

/** Client-side checks mirror the backend's Bean Validation rules for instant feedback. */
export function validate(v: EmployeeInput): Errors {
  const errors: Errors = {}
  if (!v.firstName.trim()) errors.firstName = 'First name is required'
  if (!v.lastName.trim()) errors.lastName = 'Last name is required'
  if (!v.email.trim()) errors.email = 'Email is required'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email)) errors.email = 'Enter a valid email address'
  if (v.phone.trim() && !/^[+0-9 ()-]{7,20}$/.test(v.phone.trim())) errors.phone = 'Enter a valid phone number'
  if (!v.department.trim()) errors.department = 'Department is required'
  if (!v.jobTitle.trim()) errors.jobTitle = 'Job title is required'
  if (!v.salary || Number.isNaN(Number(v.salary)) || Number(v.salary) <= 0) errors.salary = 'Salary must be a positive number'
  if (!v.hireDate) errors.hireDate = 'Hire date is required'
  else if (v.hireDate > new Date().toISOString().slice(0, 10)) errors.hireDate = 'Hire date cannot be in the future'
  return errors
}
