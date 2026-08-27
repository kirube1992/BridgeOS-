export interface User {
  id: number
  name: string
  email: string
  role: string
  department?: Department | null
}

export interface Department {
  id: number
  name: string
  description: string
}

export interface Project {
  id: number
  name: string
  description: string
  clientContext: string
  status: 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED'
  deadline: string | null
  createdAt: string
  updatedAt: string
  createdBy: User
  projectManager: User
}

export interface WorkItem {
  id: number
  title: string
  description: string
  businessContextNotes: string
  acceptanceCriteria: string
  clarityScore: number
  status: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE'
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  deadline: string | null
  createdAt: string
  updatedAt: string
  project: Project
  assignedTo: User | null
  createdBy: User
  department: Department
}

export interface AuditEvent {
  id: number
  eventType: string
  entityType: string
  entityId: number | null
  summary: string
  detail: any
  actor: User
  project: Project | null
  createdAt: string
}
export interface Decision {
  id: number
  summary: string
  detail: string
  decision?: string
  context?: string
  eventType: string
  actor: User
  project: Project | null
  createdAt: string
}
export interface SummaryStats {
  totalResolved: number
  avgResolutionHours: number
  totalUsers: number
  avgClarityScore: number
}

export interface MetricsDaily {
  id: number
  metricDate: string
  user: User
  department: Department | null
  itemsResolved: number
  itemsCreated: number
  avgResolutionHours: number
  medianResolutionHours: number
  clarityAvgScore: number
}

export interface AnalyticsSummary {
  totalResolved: number
  averageResolutionHours: number
  activeUsers: number
  averageClarityScore: number
  departments: number
  trends?: {
    totalResolved?: number
    averageResolutionHours?: number
    activeUsers?: number
    averageClarityScore?: number
    departments?: number
  }
}

export interface LeaderboardEntry {
  user: User
  itemsResolved: number
  averageResolutionHours: number
  averageClarityScore: number
  department?: Department | null
}

export interface UserMetrics {
  user: User
  itemsResolved: number
  averageResolutionHours: number
  averageClarityScore: number
  statusCounts?: Partial<Record<WorkItem['status'], number>>
}

export interface LoginResponse {
  token: string
  id: number
  email: string
  role: string
  name: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  role: string
}

export interface ProjectAnalytics {
  projectId: number
  projectName: string
  totalTasks: number
  completed: number
  inProgress: number
  review: number
  todo: number
  avgClarity: number
  resolutionDays: number
}