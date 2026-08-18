<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="h-9 w-9 bg-indigo-600 rounded-lg flex items-center justify-center">
              <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
            </div>
            <span class="ml-2 text-xl font-bold text-gray-900">BridgeOS</span>
            <span class="ml-2 text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full">v1.0</span>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">{{ user?.name }}</span>
            <span class="text-xs bg-gray-100 text-gray-600 px-3 py-1 rounded-full">
              {{ user?.role }}
            </span>
            <button
              @click="logout"
              class="text-sm text-red-600 hover:text-red-800 hover:bg-red-50 px-3 py-1 rounded-lg transition"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
        <span class="text-sm text-gray-500">Welcome back, {{ user?.name }}! 👋</span>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="bg-white shadow rounded-xl p-6 border border-gray-100 hover:shadow-lg transition duration-200">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-gray-500">Projects</h3>
              <p class="text-3xl font-bold text-gray-900 mt-1">{{ projectCount }}</p>
            </div>
            <div class="h-12 w-12 bg-indigo-100 rounded-xl flex items-center justify-center">
              <svg class="h-6 w-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white shadow rounded-xl p-6 border border-gray-100 hover:shadow-lg transition duration-200">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-gray-500">Tasks</h3>
              <p class="text-3xl font-bold text-gray-900 mt-1">{{ taskCount }}</p>
            </div>
            <div class="h-12 w-12 bg-amber-100 rounded-xl flex items-center justify-center">
              <svg class="h-6 w-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="bg-white shadow rounded-xl p-6 border border-gray-100 hover:shadow-lg transition duration-200">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-gray-500">Users</h3>
              <p class="text-3xl font-bold text-gray-900 mt-1">{{ userCount }}</p>
            </div>
            <div class="h-12 w-12 bg-green-100 rounded-xl flex items-center justify-center">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Welcome Card -->
      <div class="bg-gradient-to-r from-indigo-500 to-cyan-500 rounded-xl p-8 text-white shadow-lg">
        <h2 class="text-2xl font-bold">Welcome to BridgeOS! 🚀</h2>
        <p class="text-indigo-100 mt-2 max-w-lg">
          The collaboration platform for Ethiopia-China teams. Stay connected, stay productive.
        </p>
        <div class="mt-4 flex flex-wrap gap-2">
          <span class="bg-white/20 text-white px-3 py-1 rounded-full text-sm">🌍 Cross-Cultural</span>
          <span class="bg-white/20 text-white px-3 py-1 rounded-full text-sm">🤖 AI-Powered</span>
          <span class="bg-white/20 text-white px-3 py-1 rounded-full text-sm">📊 Analytics</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/API/index'

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const projectCount = ref<number>(0)
const taskCount = ref<number>(0)
const userCount = ref<number>(0)

const logout = (): void => {
  authStore.logout()
  router.push('/login')
}

const fetchStats = async (): Promise<void> => {
  try {
    const [projects, tasks, users] = await Promise.all([
      api.get('/projects'),
      api.get('/work-items'),
      api.get('/users')
    ])
    projectCount.value = projects.data.length || 0
    taskCount.value = tasks.data.length || 0
    userCount.value = users.data.length || 0
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>