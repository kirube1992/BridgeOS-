import { defineStore } from 'pinia'
import api from '@/API/index'
import type {
  AiTranslateResponse,
  AiExtractMeetingResponse,
  AiActionItem,
  AiAskResponse,
  AiHealthResponse,
  RequirementTranslationEntity,
  AiChatMessage
} from '@/types'

interface AiState {
  health: AiHealthResponse | null
  healthLoading: boolean

  translateLoading: boolean
  translateError: string | null
  lastTranslation: AiTranslateResponse | null
  savedTranslations: RequirementTranslationEntity[]

  extractLoading: boolean
  extractError: string | null
  lastExtractedItems: AiActionItem[]

  askLoading: boolean
  askError: string | null
  chatMessages: AiChatMessage[]

  genericError: string | null
}

export const useAiStore = defineStore('ai', {
  state: (): AiState => ({
    health: null,
    healthLoading: false,

    translateLoading: false,
    translateError: null,
    lastTranslation: null,
    savedTranslations: [],

    extractLoading: false,
    extractError: null,
    lastExtractedItems: [],

    askLoading: false,
    askError: null,
    chatMessages: [],

    genericError: null
  }),

  actions: {
    async checkHealth() {
      this.healthLoading = true
      try {
        const response = await api.get<AiHealthResponse>('/ai/health')
        this.health = response.data
        return { success: true, data: this.health }
      } catch (err: any) {
        this.health = {
          status: 'unreachable',
          provider: 'none',
          sidecarReachable: false
        }
        return { success: false, data: this.health }
      } finally {
        this.healthLoading = false
      }
    },

    async translate(text: string, projectId?: number) {
      this.translateLoading = true
      this.translateError = null
      try {
        const response = await api.post<AiTranslateResponse>('/ai/translate', {
          text,
          projectId
        })
        this.lastTranslation = response.data
        return { success: true, data: response.data }
      } catch (err: any) {
        this.translateError = err.response?.data?.message || 'Failed to translate requirement'
        return { success: false, error: this.translateError }
      } finally {
        this.translateLoading = false
      }
    },

    async saveTranslation(data: {
      originalText: string
      whatToBuild: string
      whyItMatters: string
      acceptanceCriteria: string[]
      edgeCases: string[]
      technicalNotes: string
      projectId?: number
    }) {
      this.translateLoading = true
      this.translateError = null
      try {
        const response = await api.post<RequirementTranslationEntity>('/ai/translate/save', data)
        if (this.lastTranslation) {
          this.lastTranslation.savedId = response.data.id
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        this.translateError = err.response?.data?.message || 'Failed to save translation'
        return { success: false, error: this.translateError }
      } finally {
        this.translateLoading = false
      }
    },

    async fetchMyTranslations() {
      this.translateLoading = true
      try {
        const response = await api.get<RequirementTranslationEntity[]>('/ai/translate/my')
        this.savedTranslations = Array.isArray(response.data) ? response.data : []
        return { success: true, data: this.savedTranslations }
      } catch (err: any) {
        return { success: false, error: 'Failed to load saved translations' }
      } finally {
        this.translateLoading = false
      }
    },

    async extractMeeting(notes: string, projectId?: number) {
      this.extractLoading = true
      this.extractError = null
      try {
        const response = await api.post<AiExtractMeetingResponse>('/ai/extract-meeting', {
          notes,
          projectId
        })
        this.lastExtractedItems = (response.data?.actionItems || []).map(item => ({
          ...item,
          confirmed: false
        }))
        return { success: true, data: this.lastExtractedItems }
      } catch (err: any) {
        this.extractError = err.response?.data?.message || 'Failed to extract action items'
        return { success: false, error: this.extractError }
      } finally {
        this.extractLoading = false
      }
    },

    updateActionItem(index: number, patch: Partial<AiActionItem>) {
      if (index >= 0 && index < this.lastExtractedItems.length) {
        const existing = this.lastExtractedItems[index]!
        const merged: AiActionItem = {
          description: patch.description ?? existing.description,
          suggestedAssignee: patch.suggestedAssignee !== undefined ? patch.suggestedAssignee : existing.suggestedAssignee,
          suggestedDueDate: patch.suggestedDueDate !== undefined ? patch.suggestedDueDate : existing.suggestedDueDate,
          priority: patch.priority ?? existing.priority,
          confirmed: patch.confirmed ?? existing.confirmed,
          promotedId: patch.promotedId ?? existing.promotedId,
          promoting: patch.promoting ?? existing.promoting,
          editing: patch.editing ?? existing.editing
        }
        this.lastExtractedItems[index] = merged
      }
    },

    removeActionItem(index: number) {
      if (index >= 0 && index < this.lastExtractedItems.length) {
        this.lastExtractedItems.splice(index, 1)
      }
    },

    async promoteActionToTask(item: AiActionItem, payload: {
      projectId: number
      createdByUserId?: number
      assignedToUserId?: number
      departmentId?: number
    }) {
      const items = this.lastExtractedItems
      const idx = items.findIndex(i =>
        i.description === item.description &&
        i.suggestedDueDate === item.suggestedDueDate
      )
      if (idx !== -1) {
        items[idx]!.promoting = true
      }
      try {
        const response = await api.post('/ai/extract-meeting/promote', {
          description: item.description,
          assignedToUserId: item.suggestedAssignee?.id ?? payload.assignedToUserId ?? undefined,
          dueDate: item.suggestedDueDate ?? undefined,
          priority: item.priority,
          projectId: payload.projectId,
          createdByUserId: payload.createdByUserId ?? undefined,
          departmentId: payload.departmentId ?? undefined
        })
        if (idx !== -1) {
          items[idx]!.promoting = false
          items[idx]!.promotedId = response.data.id
          items[idx]!.confirmed = true
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        if (idx !== -1) items[idx]!.promoting = false
        return { success: false, error: err.response?.data?.message || 'Failed to promote action item' }
      }
    },

    async ask(question: string, projectId?: number, context?: Array<{ type: string; id: number; summary: string }>) {
      const userMsg: AiChatMessage = {
        id: `u_${Date.now()}`,
        role: 'user',
        content: question,
        timestamp: new Date().toISOString()
      }
      this.chatMessages.push(userMsg)
      this.askLoading = true
      this.askError = null

      try {
        const response = await api.post<AiAskResponse>('/ai/ask', {
          question,
          projectId,
          context: context || []
        })
        const assistantMsg: AiChatMessage = {
          id: `a_${Date.now()}`,
          role: 'assistant',
          content: response.data.answer,
          sources: response.data.sources,
          timestamp: new Date().toISOString()
        }
        this.chatMessages.push(assistantMsg)
        return { success: true, data: response.data }
      } catch (err: any) {
        this.askError = err.response?.data?.message || 'AI assistant failed to respond'
        const failMsg: AiChatMessage = {
          id: `a_${Date.now()}`,
          role: 'assistant',
          content: 'Sorry, I ran into a problem answering that. Please try again.',
          timestamp: new Date().toISOString()
        }
        this.chatMessages.push(failMsg)
        return { success: false, error: this.askError }
      } finally {
        this.askLoading = false
      }
    },

    clearChat() {
      this.chatMessages = []
    },

    resetTranslation() {
      this.lastTranslation = null
      this.translateError = null
    },

    resetExtraction() {
      this.lastExtractedItems = []
      this.extractError = null
    }
  }
})
