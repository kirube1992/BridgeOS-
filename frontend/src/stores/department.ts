import { defineStore } from 'pinia'
import api from '@/API/index'
import type { Department } from '@/types'

interface DepartmentFilters {
  search: string
  sortBy: string
  status: string
}

interface DepartmentState {
  departments: Department[]
  currentDepartment: Department | null
  loading: boolean
  error: string | null
  selectedDepartments: number[]
  filters: DepartmentFilters
}

const departmentsCollection = (data: unknown): Department[] => {
  if (Array.isArray(data)) return data as Department[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; data?: unknown; items?: unknown }
    if (Array.isArray(payload.content)) return payload.content as Department[]
    if (Array.isArray(payload.data)) return payload.data as Department[]
    if (Array.isArray(payload.items)) return payload.items as Department[]
  }
  return []
}

const departmentStatus = (department: Department): string =>
  (department.status || 'ACTIVE').toString().toLowerCase()

export const useDepartmentStore = defineStore('department', {
  state: (): DepartmentState => ({
    departments: [],
    currentDepartment: null,
    loading: false,
    error: null,
    selectedDepartments: [],
    filters: {
      search: '',
      sortBy: 'name',
      status: 'all'
    }
  }),

  getters: {
    filteredDepartments: (state) => {
      let result = [...state.departments]

      if (state.filters.search) {
        const query = state.filters.search.toLowerCase()
        result = result.filter(
          (department) =>
            department.name.toLowerCase().includes(query) ||
            department.description?.toLowerCase().includes(query)
        )
      }

      if (state.filters.status !== 'all') {
        result = result.filter(
          (department) => departmentStatus(department) === state.filters.status
        )
      }

      switch (state.filters.sortBy) {
        case 'name':
          result.sort((a, b) => a.name.localeCompare(b.name))
          break
        case 'name_desc':
          result.sort((a, b) => b.name.localeCompare(a.name))
          break
        case 'members':
          result.sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0))
          break
        case 'created':
          result.sort(
            (a, b) =>
              new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
          )
          break
      }

      return result
    },

    stats: (state) => {
      const total = state.departments.length
      const totalMembers = state.departments.reduce(
        (sum, department) => sum + (department.memberCount || 0),
        0
      )
      const active = state.departments.filter(
        (department) => departmentStatus(department) !== 'inactive'
      ).length
      const empty = state.departments.filter(
        (department) => (department.memberCount || 0) === 0
      ).length

      return { total, totalMembers, active, empty }
    }
  },

  actions: {
    async fetchDepartments() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<Department[]>('/departments')
        this.departments = departmentsCollection(response.data)
      } catch (err: unknown) {
        this.error =
          (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
          'Failed to fetch departments'
        console.error('Fetch departments error:', err)
      } finally {
        this.loading = false
      }
    },

    async fetchDepartmentById(id: number) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<Department>(`/departments/${id}`)
        this.currentDepartment = response.data
        return response.data
      } catch (err: unknown) {
        this.error =
          (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
          'Failed to fetch department'
        throw err
      } finally {
        this.loading = false
      }
    },

    async createDepartment(data: Partial<Department>) {
      try {
        const response = await api.post<Department>('/departments', data)
        this.departments.unshift(response.data)
        return { success: true, data: response.data }
      } catch (err: unknown) {
        return {
          success: false,
          error:
            (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
            'Failed to create department'
        }
      }
    },

    async updateDepartment(id: number, data: Partial<Department>) {
      try {
        const response = await api.put<Department>(`/departments/${id}`, data)
        const index = this.departments.findIndex((department) => department.id === id)
        if (index !== -1) {
          this.departments[index] = response.data
        }
        if (this.currentDepartment?.id === id) {
          this.currentDepartment = response.data
        }
        return { success: true, data: response.data }
      } catch (err: unknown) {
        return {
          success: false,
          error:
            (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
            'Failed to update department'
        }
      }
    },

    async deleteDepartment(id: number) {
      try {
        await api.delete(`/departments/${id}`)
        this.departments = this.departments.filter((department) => department.id !== id)
        if (this.currentDepartment?.id === id) {
          this.currentDepartment = null
        }
        this.selectedDepartments = this.selectedDepartments.filter(
          (selectedId) => selectedId !== id
        )
        return { success: true }
      } catch (err: unknown) {
        return {
          success: false,
          error:
            (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
            'Failed to delete department'
        }
      }
    },

    async bulkDelete(ids: number[]) {
      try {
        await Promise.all(ids.map((id) => api.delete(`/departments/${id}`)))
        this.departments = this.departments.filter(
          (department) => !ids.includes(department.id)
        )
        this.selectedDepartments = []
        return { success: true }
      } catch {
        return { success: false, error: 'Failed to delete selected departments' }
      }
    },

    setFilter(key: keyof DepartmentFilters, value: string) {
      this.filters = { ...this.filters, [key]: value }
    },

    toggleSelection(id: number) {
      const index = this.selectedDepartments.indexOf(id)
      if (index === -1) {
        this.selectedDepartments.push(id)
      } else {
        this.selectedDepartments.splice(index, 1)
      }
    },

    clearSelection() {
      this.selectedDepartments = []
    }
  }
})
