import { STATUS_LABELS, type EmployeeStatus } from '../types'

export default function StatusBadge({ status }: { status: EmployeeStatus }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{STATUS_LABELS[status]}</span>
}
