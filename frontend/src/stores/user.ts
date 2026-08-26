import { defineStore } from 'pinia'
import api from '@/API/index'
import type { User, WorkItem } from '@/types'

interface UserState {
  users: User[]
  currentUser: User | null
  loading: boolean
  error: string | null
}

const usersCollection = (data: unknown): User[] => {
  if (Array.isArray(data)) return data as User[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; data?: unknown; items?: unknown }
    if (Array.isArray(payload.content)) return payload.content as User[]
    if (Array.isArray(payload.data)) return payload.data as User[]
    if (Array.isArray(payload.items)) return payload.items as User[]
  }
  return []
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    users: [],
    currentUser: null,
    loading: false,
    error: null
  }),

  getters: {
    getUserById: (state) => (id: number) => {
      return state.users.find(u => u.id === id) || state.currentUser
    }
  },

  actions: {
    async fetchUsers() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<User[]>('/users')
        this.users = usersCollection(response.data)
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch users'
      } finally {
        this.loading = false
      }
    },

    async fetchUserById(id: number) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get<User>(`/users/${id}`)
        this.currentUser = response.data
        // Update in list if present
        const index = this.users.findIndex(u => u.id === id)
        if (index !== -1) {
          this.users[index] = response.data
        }
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Failed to fetch user'
      } finally {
        this.loading = false
      }
    },

    async updateUser(id: number, data: Partial<User>) {
      try {
        const response = await api.put<User>(`/users/${id}`, data)
        const index = this.users.findIndex(u => u.id === id)
        if (index !== -1) {
          this.users[index] = response.data
        }
        if (this.currentUser?.id === id) {
          this.currentUser = response.data
        }
        return { success: true, data: response.data }
      } catch (err: any) {
        return { success: false, error: err.response?.data?.message || 'Failed to update user' }
      }
    }
  }
})