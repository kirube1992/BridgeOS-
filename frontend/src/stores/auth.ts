import { defineStore } from 'pinia'
import api from '@/API'
import type { LoginResponse, User, RegisterRequest } from '@/types'

interface AuthState {
  user: User | null
  token: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('token') || null
  }),

  getters: {
    isAuthenticated: (state): boolean => !!state.token,
    getUser: (state): User | null => state.user,
    getUserRole: (state): string | null => state.user?.role || null,
    isAdmin: (state): boolean => state.user?.role === 'ADMIN'
  },

  actions: {
    async login(email: string, password: string): Promise<{ success: boolean; message?: string }> {
      try {
        const response = await api.post<LoginResponse>('/auth/login', { email, password })
        const { token, id, email: userEmail, role, name } = response.data
        this.token = token
        this.user = { id, name, email: userEmail, role } as User
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(this.user))
        return { success: true }
      } catch (error: any) {
        return { 
          success: false, 
          message: error.response?.data?.message || 'Login failed' 
        }
      }
    },

    async register(name: string, email: string, password: string, role: string): Promise<{ success: boolean; message?: string }> {
      try {
        const request: RegisterRequest = { name, email, password, role }
        await api.post('/auth/register', request)
        return { success: true }
      } catch (error: any) {
        return { 
          success: false, 
          message: error.response?.data || 'Registration failed' 
        }
      }
    },

    logout(): void {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    loadUserFromStorage(): void {
      const user = localStorage.getItem('user')
      if (user) {
        try {
          this.user = JSON.parse(user)
        } catch {
          this.user = null
        }
      }
    }
  }
})