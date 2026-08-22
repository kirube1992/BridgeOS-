import { defineStore } from 'pinia'
import api from '@/API/index'
import type { AnalyticsSummary, LeaderboardEntry, UserMetrics } from '@/types'

interface AnalyticsState {
  summary: AnalyticsSummary | null
  leaderboard: LeaderboardEntry[]
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

export const useAnalyticsStore = defineStore('analytics', {
  state: (): AnalyticsState => ({
    summary: null,
    leaderboard: [],
    userMetrics: null,
    loading: false,
    error: null,
    period: 'week',
    departmentId: null
  }),

  actions: {
    async fetchSummary() {
      try {
        const response = await api.get('/analytics/summary', { params: { period: this.period } })
        this.summary = response.data as AnalyticsSummary
        return { success: true, data: this.summary }
      } catch (err: any) {
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
        const params = { period: selectedPeriod, ...(selectedDepartmentId ? { departmentId: selectedDepartmentId } : {}) }
        const response = await api.get('/analytics/leaderboard', { params })
        this.leaderboard = collection<LeaderboardEntry>(response.data).sort(
          (left, right) => right.itemsResolved - left.itemsResolved
        )
        return { success: true, data: this.leaderboard }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch leaderboard'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async fetchUserMetrics(userId: number) {
      try {
        const response = await api.get(`/analytics/users/${userId}`, { params: { period: this.period } })
        this.userMetrics = response.data as UserMetrics
        return { success: true, data: this.userMetrics }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch user metrics'
        return { success: false, error: this.error }
      }
    }
  }
})
