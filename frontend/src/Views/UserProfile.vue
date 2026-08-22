<template>
  <div class="user-profile-shell">
    <!-- Navbar -->
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
              <router-link to="/analytics" class="text-sm text-gray-600 hover:text-gray-900">Analytics</router-link>
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">{{ authUser?.name }}</span>
            <button @click="logout" class="text-sm text-red-600 hover:text-red-800">Logout</button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Back -->
      <button @click="goBack" class="text-sm text-gray-500 hover:text-gray-700 mb-6 flex items-center gap-1">
        ← Back
      </button>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-10 w-10 border-4 border-indigo-600 border-t-transparent"></div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p class="text-red-600">{{ error }}</p>
        <button @click="fetchUser" class="mt-2 text-sm text-red-700 hover:text-red-900 underline">Retry</button>
      </div>

      <!-- Profile Content -->
      <div v-else-if="user" class="space-y-6">
        <!-- User Card -->
        <UserStatsCard :user="user" :task-count="userTasks.length" />

        <!-- Stats Grid -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-indigo-600">{{ userTasks.length }}</p>
            <p class="text-sm text-gray-500">Total Tasks</p>
          </div>
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-green-600">{{ completedTasks }}</p>
            <p class="text-sm text-gray-500">Completed</p>
          </div>
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-amber-600">{{ inProgressTasks }}</p>
            <p class="text-sm text-gray-500">In Progress</p>
          </div>
        </div>

        <!-- User Info Details -->
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Profile Details</h3>
          <dl class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <dt class="text-sm font-medium text-gray-500">Full Name</dt>
              <dd class="text-sm text-gray-900">{{ user.name }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-gray-500">Email</dt>
              <dd class="text-sm text-gray-900">{{ user.email }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-gray-500">Role</dt>
              <dd class="text-sm text-gray-900">
                <span class="inline-flex px-2 py-0.5 text-xs font-medium rounded-full" :class="roleClass(user.role)">
                  {{ user.role }}
                </span>
              </dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-gray-500">Department</dt>
              <dd class="text-sm text-gray-900">{{ user.department?.name || '—' }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-gray-500">User ID</dt>
              <dd class="text-sm text-gray-900">{{ user.id }}</dd>
            </div>
          </dl>
        </div>

        <!-- Recent Tasks -->
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">Recent Tasks</h3>
            <router-link to="/tasks" class="text-sm text-indigo-600 hover:text-indigo-800">
              View all →
            </router-link>
          </div>
          <div v-if="!userTasks.length" class="text-gray-500 text-sm text-center py-4">
            No tasks assigned yet.
          </div>
          <div v-else class="space-y-2">
            <router-link
              v-for="task in recentTasks"
              :key="task.id"
              :to="`/tasks/${task.id}`"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
            >
              <div>
                <p class="text-sm font-medium text-gray-900">{{ task.title }}</p>
                <p class="text-xs text-gray-500">{{ task.project?.name || 'No project' }}</p>
              </div>
              <span class="inline-flex px-2 py-0.5 text-xs font-medium rounded-full" :class="statusClass(task.status)">
                {{ task.status }}
              </span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { useTaskStore } from '@/stores/task'
import UserStatsCard from '@/Views/components/UserStatsCard.vue'
import type { WorkItem } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()
const taskStore = useTaskStore()

const authUser = computed(() => authStore.user)
const user = computed(() => userStore.currentUser)
const loading = computed(() => userStore.loading)
const error = computed(() => userStore.error)

const userId = computed(() => Number(route.params.id) || authUser.value?.id || 0)

// Tasks assigned to this user
const userTasks = computed(() => {
  if (!user.value) return []
  return taskStore.tasks.filter(t => t.assignedTo?.id === user.value?.id)
})

const completedTasks = computed(() => {
  return userTasks.value.filter(t => t.status === 'DONE').length
})

const inProgressTasks = computed(() => {
  return userTasks.value.filter(t => t.status === 'IN_PROGRESS' || t.status === 'REVIEW').length
})

const recentTasks = computed(() => {
  return userTasks.value.slice(0, 5)
})

const fetchUser = async () => {
  await userStore.fetchUserById(userId.value)
}

const goBack = () => {
  router.push('/people')
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const formatDate = (date?: string) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const roleClass = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: 'bg-purple-100 text-purple-700',
    ETHIOPIAN_TEAM: 'bg-green-100 text-green-700',
    CHINESE_DEVELOPER: 'bg-blue-100 text-blue-700',
    HQ_CONTACT: 'bg-amber-100 text-amber-700'
  }
  return map[role] || 'bg-gray-100 text-gray-700'
}

const statusClass = (status: string) => {
  const map: Record<string, string> = {
    TODO: 'bg-gray-100 text-gray-700',
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    REVIEW: 'bg-purple-100 text-purple-700',
    DONE: 'bg-green-100 text-green-700'
  }
  return map[status] || 'bg-gray-100 text-gray-700'
}

onMounted(async () => {
  await taskStore.fetchTasks()
  await fetchUser()
})
</script>