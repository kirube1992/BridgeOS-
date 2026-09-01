export interface User {
  id: number
  name: string
  email: string
  role: string
  department?: Department | null
}

export interface DepartmentMemberPreview {
  id: number
  name: string
  email: string
  role: string
}

export interface Department {
  id: number
  name: string
  description?: string | null
  defaultWorkFlow?: string | null
  status?: 'ACTIVE' | 'INACTIVE' | 'active' | 'inactive'
  departmentLead?: DepartmentMemberPreview | null
  memberCount?: number
  workItemCount?: number
  members?: DepartmentMemberPreview[]
  createdAt?: string
  updatedAt?: string
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

export interface WeeklyReportUser {
  id: number
  name: string
  email: string
}

export interface WeeklyReportSummary {
  tasksResolved: number
  tasksCreated: number
  avgClarityScore: number
  completionRate: number
  avgResolutionHours: number
  rankInTeam: number
  totalTeamResolved: number
  teamAvgClarity: number
}

export interface WeeklyReportTrends {
  clarityScores: number[]
  resolvedByDay: number[]
  createdByDay: number[]
}

export interface WeeklyReportTask {
  id: number
  title: string
  status: string
  completedAt: string
}

export interface WeeklyReportDecision {
  id: number
  summary: string
  createdAt: string
}

export interface WeeklyReport {
  weekStart: string
  weekEnd: string
  user: WeeklyReportUser
  summary: WeeklyReportSummary
  trends: WeeklyReportTrends
  recentTasks: WeeklyReportTask[]
  createdTasks?: WeeklyReportTask[]
  recentDecisions: WeeklyReportDecision[]
  availableWeeks?: string[]
  emailOptIn?: boolean
}

export interface TeamWeeklyReportEntry {
  user: WeeklyReportUser
  summary: WeeklyReportSummary
}

export interface TeamWeeklyReport {
  weekStart: string
  weekEnd: string
  totalTeamResolved: number
  teamAvgClarity: number
  members: TeamWeeklyReportEntry[]
}

export interface AiSuggestedAssignee {
  id: number | null
  name: string | null
  confidence: number
}

export interface AiActionItem {
  description: string
  suggestedAssignee: AiSuggestedAssignee | null
  suggestedDueDate: string | null
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  confirmed?: boolean
  promotedId?: number
  promoting?: boolean
  editing?: boolean
}

export interface AiTranslateResponse {
  originalText: string
  whatToBuild: string
  whyItMatters: string
  acceptanceCriteria: string[]
  edgeCases: string[]
  technicalNotes: string
  savedId?: number
}

export interface AiExtractMeetingResponse {
  actionItems: AiActionItem[]
}

export interface AiAskContextItem {
  type: string
  id: number
  summary: string
}

export interface AiAskSource {
  type: string
  id: number
  summary: string
}

export interface AiAskResponse {
  question: string
  answer: string
  sources: AiAskSource[]
}

export interface AiHealthResponse {
  status: string
  provider: string
  model?: string | null
  sidecarReachable: boolean
}

export interface RequirementTranslationEntity {
  id: number
  originalText: string
  whatToBuild: string
  whyItMatters: string
  acceptanceCriteria: string
  edgeCases: string
  technicalNotes: string
  createdAt: string
}

export interface AiChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: AiAskSource[]
  timestamp: string
}