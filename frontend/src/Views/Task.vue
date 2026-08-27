<template>
  <div class="tasks-shell">
    <!-- Navbar (reuse the same as other pages) -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center space-x-6">
            <router-link to="/dashboard" class="flex items-center">
              <div class="h-9 w-9 bg-indigo-600 rounded-lg flex items-center justify-center">
                <svg class="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
              </div>
              <span class="ml-2 text-xl font-bold text-gray-900">BridgeOS</span>
            </router-link>
            <div class="hidden md:flex items-center space-x-4">
              <router-link to="/dashboard" class="text-sm text-gray-600 hover:text-gray-900">Dashboard</router-link>
              <router-link to="/projects" class="text-sm text-gray-600 hover:text-gray-900">Projects</router-link>
              <router-link to="/tasks" class="text-sm text-gray-600 hover:text-gray-900">Tasks</router-link>
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">{{ user?.name }}</span>
            <button @click="logout" class="text-sm text-red-600 hover:text-red-800">Logout</button>
          </div>
        </div>
      </div>
    </nav>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Tasks</h1>
          <p class="text-sm text-gray-500">All work items across projects</p>
        </div>
        <router-link to="/tasks/new" class="task-action px-4 py-2 bg-[var(--bridge-menu)] text-white rounded-lg hover:bg-[var(--bridge-menu-dark)] transition" style="color: white">
          + New Task
        </router-link>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-10 w-10 border-4 border-indigo-600 border-t-transparent"></div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p class="text-red-600">{{ error }}</p>
        <button @click="fetchTasks" class="mt-2 text-sm text-red-700 hover:text-red-900 underline">Retry</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="!tasks.length" class="bg-white rounded-xl border border-gray-200 p-12 text-center">
        <div class="flex flex-col items-center">
          <svg class="w-16 h-16 text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
          </svg>
          <h3 class="text-lg font-medium text-gray-900">No tasks yet</h3>
          <p class="text-gray-500 mt-1">Create your first task to get started</p>
          <router-link to="/tasks/new" class="task-action mt-4 px-4 py-2 bg-[var(--bridge-menu)] text-white rounded-lg hover:bg-[var(--bridge-menu-dark)]" style="color: white">Create Task</router-link>
        </div>
      </div>

      <KanbanBoard v-else />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTaskStore } from '@/stores/task'
import KanbanBoard from '@/Views/components/KanbanBoard.vue'

const router = useRouter()
const authStore = useAuthStore()
const taskStore = useTaskStore()

const user = computed(() => authStore.user)
const tasks = computed(() => taskStore.tasks)
const loading = computed(() => taskStore.loading)
const error = computed(() => taskStore.error)

const fetchTasks = () => taskStore.fetchTasks()

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchTasks()
})
</script>