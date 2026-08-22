import { defineStore } from 'pinia'
import api from '@/API/index'
import type { Project } from '@/types'

interface ProjectState {
  projects: Project[]
  loading: boolean
  error: string | null
}

export const useProjectStore = defineStore('project', {
  state: (): ProjectState => ({
    projects: [],
    loading: false,
    error: null
  }),

  getters: {
    activeProjects: (state) => state.projects.filter(p => p.status === 'ACTIVE'),
    onHoldProjects: (state) => state.projects.filter(p => p.status === 'ON_HOLD'),
    completedProjects: (state) => state.projects.filter(p => p.status === 'COMPLETED'),
    getProjectById: (state) => (id: number) => state.projects.find(p => p.id === id)
  },

  actions: {
    async fetchProjects() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<Project[]>('/projects')
        this.projects = response.data
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch projects'
        console.error('Fetch projects error:', err)
      } finally {
        this.loading = false
      }
    },

    async createProject(project: Partial<Project>) {
      try {
        const response = await api.post<Project>('/projects', project)
        this.projects.unshift(response.data)
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to create project' }
      }
    },

    async updateProject(id: number, project: Partial<Project>) {
      try {
        const response = await api.put<Project>(`/projects/${id}`, project)
        const index = this.projects.findIndex(p => p.id === id)
        if (index !== -1) {
          this.projects[index] = response.data
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to update project' }
      }
    },

    async deleteProject(id: number) {
      try {
        await api.delete(`/projects/${id}`)
        this.projects = this.projects.filter(p => p.id !== id)
        return { success: true }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to delete project' }
      }
    }
  }
})