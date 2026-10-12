export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED'

export const STATUSES: EmployeeStatus[] = ['ACTIVE', 'ON_LEAVE', 'TERMINATED']

export const STATUS_LABELS: Record<EmployeeStatus, string> = {
  ACTIVE: 'Active',
  ON_LEAVE: 'On leave',
  TERMINATED: 'Terminated',
}

export interface Employee {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string | null
  department: string
  jobTitle: string
  salary: number
  hireDate: string
  status: EmployeeStatus
  createdAt: string
  updatedAt: string
}

export type EmployeeInput = Omit<Employee, 'id' | 'createdAt' | 'updatedAt' | 'salary' | 'phone'> & {
  phone: string
  salary: string
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface DepartmentStat {
  department: string
  headcount: number
  averageSalary: number
}

export interface EmployeeStats {
  totalEmployees: number
  active: number
  onLeave: number
  terminated: number
  averageSalary: number
  departments: DepartmentStat[]
}

export interface EmployeeQuery {
  search?: string
  department?: string
  status?: EmployeeStatus | ''
  page?: number
  size?: number
  sortBy?: string
  direction?: 'asc' | 'desc'
}
