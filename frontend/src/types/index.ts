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

export interface LoginResponse {
  token: string
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