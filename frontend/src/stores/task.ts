import { defineStore } from 'pinia'
import axios from 'axios'
import api from '@/API'
import type { WorkItem } from '@/types'

interface TaskState {
  tasks: WorkItem[]
  currentTask: WorkItem | null
  loading: boolean
  error: string | null
}

export const useTaskStore = defineStore('task', {
  state: (): TaskState => ({
    tasks: [],
    currentTask: null,
    loading: false,
    error: null
  }),

  getters: {
    getTaskById: (state) => (id: number) => {
      return state.tasks.find(t => t.id === id) || state.currentTask
    },
    // For filtering by project or status
    getTasksByProject: (state) => (projectId: number) => {
      return state.tasks.filter(t => t.project?.id === projectId)
    },
    getTasksByStatus: (state) => (status: string) => {
      return state.tasks.filter(t => t.status === status)
    }
  },

  actions: {
    async fetchTasks() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<WorkItem[]>('/work-items')
        this.tasks = response.data
      } catch (err: any) {
        this.error = axios.isAxiosError(err) && !err.response
          ? 'The task service is unavailable. Make sure the backend is running on port 8080, then retry.'
          : err.response?.data?.message || 'Failed to fetch tasks'
        console.error('Fetch tasks error:', err)
      } finally {
        this.loading = false
      }
    },

    async fetchTaskById(id: number) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<WorkItem>(`/work-items/${id}`)
        this.currentTask = response.data
        // Update in list if present
        const index = this.tasks.findIndex(t => t.id === id)
        if (index !== -1) {
          this.tasks[index] = response.data
        }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch task'
      } finally {
        this.loading = false
      }
    },

    async createTask(data: Partial<WorkItem>) {
      try {
        // Add required query parameters as needed (projectId, createdByUserID)
        const response = await api.post<WorkItem>('/work-items', data)
        this.tasks.unshift(response.data)
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to create task' }
      }
    },

    async updateTask(id: number, data: Partial<WorkItem>) {
      try {
        const response = await api.put<WorkItem>(`/work-items/${id}`, data)
        const index = this.tasks.findIndex(t => t.id === id)
        if (index !== -1) {
          this.tasks[index] = response.data
        }
        if (this.currentTask?.id === id) {
          this.currentTask = response.data
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to update task' }
      }
    },

    async deleteTask(id: number) {
      try {
        await api.delete(`/work-items/${id}`)
        this.tasks = this.tasks.filter(t => t.id !== id)
        if (this.currentTask?.id === id) {
          this.currentTask = null
        }
        return { success: true }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to delete task' }
      }
    },

    // Update task status (convenience)
    async updateStatus(id: number, status: string) {
      try {
        const response = await api.patch(`/work-items/${id}/status?status=${status}`)
        const task = this.tasks.find(t => t.id === id)
        if (task) {
          task.status = response.data.status
        }
        if (this.currentTask?.id === id) {
          this.currentTask.status = response.data.status
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to update status' }
      }
    }
  }
})