import { defineStore } from 'pinia'
import api from '@/API/index'
import type { AnalyticsSummary, LeaderboardEntry, ProjectAnalytics, User, UserMetrics, WorkItem } from '@/types'

interface AnalyticsState {
  summary: AnalyticsSummary | null
  leaderboard: LeaderboardEntry[]
  projectAnalytics: ProjectAnalytics[]
  userMetrics: UserMetrics | null
  loading: boolean
  error: string | null
  period: 'week' | 'month' | 'quarter'
  departmentId: number | null
}

const collection = <T>(data: unknown): T[] => {
  if (Array.isArray(data)) return data as T[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; data?: unknown; items?: unknown }
    if (Array.isArray(payload.content)) return payload.content as T[]
    if (Array.isArray(payload.data)) return payload.data as T[]
    if (Array.isArray(payload.items)) return payload.items as T[]
  }
  return []
}

const fallbackLeaderboard = (users: User[], workItems: WorkItem[]): LeaderboardEntry[] => users.map(user => {
  const completed = workItems.filter(item => item.assignedTo?.id === user.id && item.status === 'DONE')
  return {
    user,
    itemsResolved: completed.length,
    averageResolutionHours: 0,
    averageClarityScore: completed.length
      ? completed.reduce((total, item) => total + (item.clarityScore || 0), 0) / completed.length
      : 0,
    department: user.department
  }
}).sort((left, right) => right.itemsResolved - left.itemsResolved)

export const useAnalyticsStore = defineStore('analytics', {
  state: (): AnalyticsState => ({
    summary: null,
    leaderboard: [],
    projectAnalytics: [],
    userMetrics: null,
    loading: false,
    error: null,
    period: 'week',
    departmentId: null
  }),

  actions: {
    async fetchSummary() {
      try {
        const response = await api.get('/analytics/summary', { params: { period: this.period.toUpperCase() } })
        this.summary = response.data as AnalyticsSummary
        return { success: true, data: this.summary }
      } catch (err: any) {
        if (err.response?.status === 403) {
          try {
            const [usersResponse, workItemsResponse] = await Promise.all([
              api.get('/users'),
              api.get('/work-items')
            ])
            const users = collection<User>(usersResponse.data)
            const workItems = collection<WorkItem>(workItemsResponse.data)
            const completed = workItems.filter(item => item.status === 'DONE')
            this.summary = {
              totalResolved: completed.length,
              averageResolutionHours: 0,
              activeUsers: new Set(workItems.filter(item => item.status !== 'DONE').map(item => item.assignedTo?.id).filter(Boolean)).size,
              averageClarityScore: completed.length
                ? completed.reduce((total, item) => total + (item.clarityScore || 0), 0) / completed.length
                : 0,
              departments: new Set(users.map(user => user.department?.id).filter(Boolean)).size
            }
            return { success: true, data: this.summary, fallback: true }
          } catch {
            this.error = 'Analytics access is restricted for this account.'
            return { success: false, error: this.error }
          }
        }
        this.error = err.response?.data?.message || 'Failed to fetch analytics summary'
        return { success: false, error: this.error }
      }
    },

    async fetchLeaderboard(period?: AnalyticsState['period'], departmentId?: number | null) {
      const selectedPeriod = period || this.period
      const selectedDepartmentId = departmentId === undefined ? this.departmentId : departmentId
      this.loading = true
      this.error = null
      this.period = selectedPeriod
      this.departmentId = selectedDepartmentId
      try {
        const params = { period: selectedPeriod.toUpperCase(), ...(selectedDepartmentId ? { departmentId: selectedDepartmentId } : {}) }
        const response = await api.get('/analytics/leaderboard', { params })
        this.leaderboard = collection<LeaderboardEntry>(response.data).sort(
          (left, right) => right.itemsResolved - left.itemsResolved
        )
        return { success: true, data: this.leaderboard }
      } catch (err: any) {
        if (err.response?.status === 403) {
          try {
            const [usersResponse, workItemsResponse] = await Promise.all([
              api.get('/users'),
              api.get('/work-items')
            ])
            this.leaderboard = fallbackLeaderboard(collection<User>(usersResponse.data), collection<WorkItem>(workItemsResponse.data))
            return { success: true, data: this.leaderboard, fallback: true }
          } catch {
            this.error = 'Analytics access is restricted for this account.'
            return { success: false, error: this.error }
          }
        }
        this.error = err.response?.data?.message || 'Failed to fetch leaderboard'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async fetchProjectAnalytics() {
      try {
        const response = await api.get('/projects/analytics', { params: { period: this.period.toUpperCase() } })
        this.projectAnalytics = collection<ProjectAnalytics>(response.data)
        return { success: true, data: this.projectAnalytics }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch project analytics'
        return { success: false, error: this.error }
      }
    },

    async fetchUserMetrics(userId: number) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get(`/analytics/users/${userId}`, { params: { period: this.period.toUpperCase() } })
        this.userMetrics = response.data as UserMetrics
        return { success: true, data: this.userMetrics }
      } catch (err: any) {
        if (err.response?.status === 403) {
          try {
            const [usersResponse, workItemsResponse] = await Promise.all([
              api.get('/users'),
              api.get('/work-items')
            ])
            const selectedUser = collection<User>(usersResponse.data).find(user => user.id === userId)
            if (!selectedUser) throw new Error('User not found')
            const userItems = collection<WorkItem>(workItemsResponse.data).filter(item => item.assignedTo?.id === userId)
            const completed = userItems.filter(item => item.status === 'DONE')
            this.userMetrics = {
              user: selectedUser,
              itemsResolved: completed.length,
              averageResolutionHours: 0,
              averageClarityScore: completed.length
                ? completed.reduce((total, item) => total + (item.clarityScore || 0), 0) / completed.length
                : 0,
              statusCounts: userItems.reduce((counts, item) => {
                counts[item.status] = (counts[item.status] || 0) + 1
                return counts
              }, {} as Partial<Record<WorkItem['status'], number>>)
            }
            return { success: true, data: this.userMetrics, fallback: true }
          } catch {
            this.error = 'User analytics access is restricted for this account.'
            return { success: false, error: this.error }
          }
        }
        this.error = err.response?.data?.message || 'Failed to fetch user metrics'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
