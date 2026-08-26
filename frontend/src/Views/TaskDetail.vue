<template>
  <div class="task-detail-shell">
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
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">{{ user?.name }}</span>
            <button @click="logout" class="text-sm text-red-600 hover:text-red-800">Logout</button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Back & Actions -->
      <div class="flex items-center justify-between mb-6">
        <button @click="goBack" class="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1">
          ← Back
        </button>
        <div class="flex gap-2">
          <button
            @click="editTask"
            class="px-4 py-2 text-sm font-medium text-white bg-[var(--bridge-menu)] rounded-lg hover:bg-[var(--bridge-menu-dark)] transition"
          >
            Edit
          </button>
          <button
            @click="deleteTask"
            class="px-4 py-2 text-sm font-medium text-red-600 bg-red-50 rounded-lg hover:bg-red-100 transition"
          >
            Delete
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-10 w-10 border-4 border-indigo-600 border-t-transparent"></div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p class="text-red-600">{{ error }}</p>
        <button @click="fetchTask" class="mt-2 text-sm text-red-700 hover:text-red-900 underline">Retry</button>
      </div>

      <!-- Task Detail -->
      <div v-else-if="task" class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <!-- Header -->
        <div class="p-6 border-b border-gray-100">
          <div class="flex items-start justify-between">
            <div>
              <h1 class="text-2xl font-bold text-gray-900">{{ task.title }}</h1>
              <div class="flex flex-wrap gap-2 mt-2">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="statusClass(task.status)"
                >
                  {{ task.status }}
                </span>
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="priorityClass(task.priority)"
                >
                  {{ task.priority }}
                </span>
                <span class="text-xs text-gray-400">
                  Created: {{ formatDate(task.createdAt) }}
                </span>
                <span class="text-xs text-gray-400">
                  Updated: {{ formatDate(task.updatedAt) }}
                </span>
              </div>
            </div>
            <div class="text-right">
              <div class="text-sm text-gray-500">Assigned to</div>
              <div class="font-medium text-gray-900">
                {{ task.assignedTo?.name || 'Unassigned' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="p-6 space-y-6">
          <!-- Description -->
          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-1">Description</h3>
            <p class="text-gray-700 whitespace-pre-wrap">{{ task.description || 'No description' }}</p>
          </div>

          <!-- Business Context Notes (🔥 Signature Feature) -->
          <div class="bg-indigo-50 border border-indigo-100 rounded-lg p-4">
            <h3 class="text-sm font-semibold text-indigo-800 mb-1 flex items-center gap-2">
              <span>💡 Business Context Notes</span>
              <span class="text-xs font-normal text-indigo-600">— The WHY</span>
            </h3>
            <p class="text-indigo-900 whitespace-pre-wrap">
              {{ task.businessContextNotes || 'No context notes provided.' }}
            </p>
          </div>

          <!-- Acceptance Criteria -->
          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-1">Acceptance Criteria</h3>
            <div class="bg-gray-50 rounded-lg p-4 text-gray-700 whitespace-pre-wrap">
              {{ task.acceptanceCriteria || 'No acceptance criteria defined.' }}
            </div>
          </div>

          <!-- Clarity Score -->
          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-1">Clarity Score</h3>
            <ClarityScore
              :score="task.clarityScore || 0"
              :show-breakdown="true"
              :rules="clarityRules"
            />
          </div>

          <!-- Meta -->
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-500">Project</span>
              <div class="font-medium text-gray-900">{{ task.project?.name || 'N/A' }}</div>
            </div>
            <div>
              <span class="text-gray-500">Department</span>
              <div class="font-medium text-gray-900">{{ task.department?.name || 'N/A' }}</div>
            </div>
            <div>
              <span class="text-gray-500">Created by</span>
              <div class="font-medium text-gray-900">{{ task.createdBy?.name || 'Unknown' }}</div>
            </div>
            <div>
              <span class="text-gray-500">Deadline</span>
              <div class="font-medium text-gray-900">{{ task.deadline ? formatDate(task.deadline) : 'No deadline' }}</div>
            </div>
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
import { useTaskStore } from '@/stores/task'
import ClarityScore from '@/Views/components/ClarityScore.vue'
import type { WorkItem } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const taskStore = useTaskStore()

const user = computed(() => authStore.user)
const loading = computed(() => taskStore.loading)
const error = computed(() => taskStore.error)
const task = computed(() => taskStore.currentTask)

const taskId = computed(() => Number(route.params.id))

// Mock clarity rules breakdown (we'll compute from actual task later)
// const clarityRules = computed(() => {
//   const t = task.value
//   if (!t) return []
//   return [
//     { name: 'Business Context provided', passed: !!t.businessContextNotes?.length, points: 25 },
//     { name: 'Acceptance Criteria defined', passed: !!t.acceptanceCriteria?.length, points: 25 },
//     { name: 'Deadline set', passed: !!t.deadline, points: 15 },
//     { name: 'Title length ≥ 10 chars', passed: (t.title?.length || 0) >= 10, points: 20 },
//     { name: 'Description length ≥ 20 chars', passed: (t.description?.length || 0) >= 20, points: 15 },
//   ]
// })
const clarityRules = computed(() => {
  const t = task.value
  if (!t) return []
  return [
    {
      name: 'Business Context provided',
      passed: !!t.businessContextNotes?.length,
      points: 25
    },
    {
      name: 'Acceptance Criteria defined',
      passed: !!t.acceptanceCriteria?.length,
      points: 25
    },
    {
      name: 'Deadline set',
      passed: !!t.deadline,
      points: 15
    },
    {
      name: 'Title length ≥ 10 characters',
      passed: (t.title?.length || 0) >= 10,
      points: 20
    },
    {
      name: 'Description length ≥ 20 characters',
      passed: (t.description?.length || 0) >= 20,
      points: 15
    }
  ]
})

const fetchTask = async () => {
  if (taskId.value) {
    await taskStore.fetchTaskById(taskId.value)
  }
}

const goBack = () => {
  router.push('/tasks') // or previous page
}

const editTask = () => {
  if (task.value) {
    router.push(`/tasks/${task.value.id}/edit`)
  }
}

const deleteTask = async () => {
  if (!task.value) return
  if (confirm(`Are you sure you want to delete task "${task.value.title}"?`)) {
    const result = await taskStore.deleteTask(task.value.id)
    if (result.success) {
      router.push('/tasks')
    } else {
      alert(result.error)
    }
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
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

const priorityClass = (priority: string) => {
  const map: Record<string, string> = {
    LOW: 'bg-gray-100 text-gray-600',
    MEDIUM: 'bg-yellow-100 text-yellow-700',
    HIGH: 'bg-orange-100 text-orange-700',
    URGENT: 'bg-red-100 text-red-700'
  }
  return map[priority] || 'bg-gray-100 text-gray-600'
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchTask()
})
</script>