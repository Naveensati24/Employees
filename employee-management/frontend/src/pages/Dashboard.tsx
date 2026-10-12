import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { employeeApi } from '../api/employees'
import { formatCurrency } from '../format'
import type { EmployeeStats } from '../types'

export default function Dashboard() {
  const [stats, setStats] = useState<EmployeeStats | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    employeeApi.stats().then(setStats).catch((e: Error) => setError(e.message))
  }, [])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!stats) return <p className="muted">Loading dashboard…</p>

  const maxHeadcount = Math.max(1, ...stats.departments.map((d) => d.headcount))

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="muted">Workforce overview at a glance</p>
        </div>
        <Link to="/employees/new" className="btn btn-primary">
          + Add employee
        </Link>
      </div>

      <section className="stat-grid">
        <StatCard label="Total employees" value={stats.totalEmployees} />
        <StatCard label="Active" value={stats.active} />
        <StatCard label="On leave" value={stats.onLeave} />
        <StatCard label="Avg. salary (current staff)" value={formatCurrency(stats.averageSalary)} />
      </section>

      <section className="card">
        <h2>Headcount by department</h2>
        {stats.departments.length === 0 ? (
          <p className="muted">No current employees yet.</p>
        ) : (
          <ul className="bars">
            {stats.departments.map((d) => (
              <li key={d.department}>
                <Link to={`/employees?department=${encodeURIComponent(d.department)}`} className="bar-label">
                  {d.department}
                </Link>
                <div className="bar-track">
                  <div className="bar-fill" style={{ width: `${(d.headcount / maxHeadcount) * 100}%` }} />
                </div>
                <span className="bar-value">{d.headcount}</span>
                <span className="bar-meta muted">avg {formatCurrency(d.averageSalary)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="stat-card">
      <span className="stat-label">{label}</span>
      <span className="stat-value">{value}</span>
    </div>
  )
}
