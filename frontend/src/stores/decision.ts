import { defineStore } from 'pinia'
import api from '@/API/index'
import type { Decision } from '@/types'

interface DecisionState {
  decisions: Decision[]
  loading: boolean
  error: string | null
}

const normalizeDecisions = (data: unknown): Decision[] => {
  if (Array.isArray(data)) return data as Decision[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; data?: unknown; items?: unknown }
    if (Array.isArray(payload.content)) return payload.content as Decision[]
    if (Array.isArray(payload.data)) return payload.data as Decision[]
    if (Array.isArray(payload.items)) return payload.items as Decision[]
  }
  return []
}

export const useDecisionStore = defineStore('decision', {
  state: (): DecisionState => ({
    decisions: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchDecisions() {
      return this.searchDecisions('')
    },

    async fetchByProject(projectId: number) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get(`/audit/project/${projectId}`)
        this.decisions = normalizeDecisions(response.data).sort(
          (left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
        )
        return { success: true, data: this.decisions }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch decisions'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async searchDecisions(query: string, projectId?: number) {
      this.loading = true
      this.error = null
      try {
        const params = {
          ...(query.trim() ? { q: query.trim() } : {}),
          ...(projectId ? { projectId } : {})
        }
        const response = await api.get('/audit/search', { params })
        this.decisions = normalizeDecisions(response.data).sort(
          (left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
        )
        return { success: true, data: this.decisions }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch decisions'
        return { success: false, error: this.error }
      } finally {
        this.loading = false
      }
    },

    async createDecision(data: Partial<Decision> & { projectId: number }) {
      this.error = null
      try {
        const response = await api.post<Decision>('/audit/decisions', {
          projectId: data.projectId,
          decision: data.decision || data.summary,
          context: data.context || data.detail || ''
        })
        const created = normalizeDecisions(response.data)[0] || response.data
        this.decisions = [created, ...this.decisions.filter(decision => decision.id !== created.id)]
        return { success: true, data: created }
      } catch (err: any) {
        this.error = err.response?.status === 403
          ? 'You do not have permission to create decisions.'
          : err.response?.data?.message || 'Failed to create decision'
        return { success: false, error: this.error }
      }
    },

    async updateDecision(id: number, data: { decision: string; context: string; projectId: number }) {
      this.error = null
      try {
        const response = await api.put<Decision>(`/audit/decisions/${id}`, data)
        const index = this.decisions.findIndex(decision => decision.id === id)
        if (index !== -1) this.decisions[index] = response.data
        return { success: true, data: response.data }
      } catch (err: any) {
        this.error = err.response?.status === 403
          ? 'You do not have permission to edit decisions.'
          : err.response?.data?.message || 'Failed to update decision'
        return { success: false, error: this.error }
      }
    },

    async deleteDecision(id: number) {
      this.error = null
      try {
        await api.delete(`/audit/decisions/${id}`)
        this.decisions = this.decisions.filter(decision => decision.id !== id)
        return { success: true }
      } catch (err: any) {
        this.error = err.response?.status === 403
          ? 'You do not have permission to delete decisions.'
          : err.response?.data?.message || 'Failed to delete decision'
        return { success: false, error: this.error }
      }
    }
  }
})
