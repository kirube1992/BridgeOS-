import { defineStore } from 'pinia'
import api from '@/API/index'
import type { TeamWeeklyReport, WeeklyReport } from '@/types'

interface ReportsState {
  currentReport: WeeklyReport | null
  teamReport: TeamWeeklyReport | null
  loading: boolean
  error: string | null
  emailOptIn: boolean
}

export const useReportsStore = defineStore('reports', {
  state: (): ReportsState => ({
    currentReport: null,
    teamReport: null,
    loading: false,
    error: null,
    emailOptIn: false
  }),

  actions: {
    async fetchCurrentReport(weekStart?: string) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<WeeklyReport>('/reports/weekly/current', {
          params: weekStart ? { weekStart } : undefined
        })
        this.currentReport = response.data
        this.emailOptIn = !!response.data.emailOptIn
        return { success: true, data: response.data }
      } catch (err: unknown) {
        this.error =
          (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
          'Failed to fetch weekly report'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async fetchTeamReport(weekStart?: string) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<TeamWeeklyReport>('/reports/weekly/team', {
          params: weekStart ? { weekStart } : undefined
        })
        this.teamReport = response.data
        return { success: true, data: response.data }
      } catch (err: unknown) {
        this.error =
          (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
          'Failed to fetch team weekly report'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async refreshReport(weekStart?: string) {
      return this.fetchCurrentReport(weekStart)
    },

    async updateEmailPreference(emailOptIn: boolean) {
      try {
        await api.put('/reports/weekly/preferences', { emailOptIn })
        this.emailOptIn = emailOptIn
        return { success: true }
      } catch (err: unknown) {
        return {
          success: false,
          error:
            (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
            'Failed to update email preferences'
        }
      }
    },

    async sendReportEmail() {
      try {
        await api.post('/reports/weekly/send')
        return { success: true }
      } catch (err: unknown) {
        return {
          success: false,
          error:
            (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
            'Failed to send report email'
        }
      }
    }
  }
})
