<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/dashboard" class="flex items-center">
              <div class="h-9 w-9 bg-[var(--bridge-cyan)] text-[var(--bridge-ink)] rounded-lg flex items-center justify-center">
                <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
              </div>
              <span class="ml-2 text-xl font-bold text-gray-900">BridgeOS</span>
            </router-link>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">{{ user?.name }}</span>
            <button
              @click="logout"
              class="text-sm text-red-600 hover:text-red-800 px-3 py-1 rounded-lg hover:bg-red-50 transition"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex flex-wrap items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Projects</h1>
          <p class="text-sm text-gray-500 mt-1">Manage all your projects in one place</p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
        <router-link
          to="/dashboard"
          class="inline-flex items-center px-4 py-2 border border-[var(--bridge-deep)] text-[var(--bridge-deep)] text-sm font-medium rounded-lg hover:bg-[var(--bridge-cyan-soft)] transition"
        >
          <span aria-hidden="true" class="mr-2">←</span>
          Back to dashboard
        </router-link>
        <button
          @click="openCreateModal"
          class="inline-flex items-center px-4 py-2 bg-[var(--bridge-cyan)] text-[var(--bridge-ink)] text-sm font-medium rounded-lg hover:brightness-95 transition shadow-sm"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          New Project
        </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="filter in filters"
          :key="filter.value"
          @click="activeFilter = filter.value"
          class="px-4 py-2 text-sm font-medium rounded-full transition"
          :class="activeFilter === filter.value
            ? 'bg-[var(--bridge-cyan-soft)] text-[var(--bridge-deep)]'
            : 'bg-white text-gray-600 hover:bg-gray-100'"
        >
          {{ filter.label }}
          <span class="ml-1 text-xs" :class="activeFilter === filter.value ? 'text-[var(--bridge-deep)]' : 'text-gray-400'">
            ({{ getProjectCount(filter.value) }})
          </span>
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-10 w-10 border-4 border-[var(--bridge-cyan)] border-t-transparent"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p class="text-red-600">{{ error }}</p>
        <button @click="fetchProjects" class="mt-2 text-sm text-red-700 hover:text-red-900 underline">
          Try again
        </button>
      </div>

      <!-- Projects Grid -->
      <div v-else-if="filteredProjects.length === 0" class="bg-white rounded-xl border border-gray-200 p-12 text-center">
        <div class="flex flex-col items-center">
          <svg class="w-16 h-16 text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
          </svg>
          <h3 class="text-lg font-medium text-gray-900">No projects found</h3>
          <p class="text-gray-500 mt-1">Create your first project to get started</p>
          <button
            @click="openCreateModal"
            class="mt-4 px-4 py-2 bg-[var(--bridge-cyan)] text-[var(--bridge-ink)] text-sm font-medium rounded-lg hover:brightness-95 transition"
          >
            Create Project
          </button>
        </div>
      </div>

      <!-- Projects Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="project in filteredProjects"
          :key="project.id"
          class="bg-white rounded-xl border border-gray-200 hover:shadow-lg transition p-6"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-semibold text-gray-900 truncate">
                {{ project.name }}
              </h3>
              <p class="text-sm text-gray-500 mt-1 line-clamp-2">
                {{ project.description || 'No description' }}
              </p>
            </div>
            <div class="flex space-x-1 ml-2 flex-shrink-0">
              <button
                @click="openEditModal(project)"
                class="p-1.5 text-gray-400 hover:text-[var(--bridge-deep)] rounded-lg hover:bg-[var(--bridge-cyan-soft)] transition"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </button>
              <button
                @click="handleDelete(project.id)"
                class="p-1.5 text-gray-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Status Badge -->
          <div class="mt-3 flex flex-wrap items-center gap-2">
            <span
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
              :class="{
                'bg-green-100 text-green-700': project.status === 'ACTIVE',
                'bg-yellow-100 text-yellow-700': project.status === 'ON_HOLD',
                'bg-blue-100 text-blue-700': project.status === 'COMPLETED',
                'bg-gray-100 text-gray-600': project.status === 'ARCHIVED'
              }"
            >
              {{ project.status }}
            </span>
            <span v-if="project.deadline" class="text-xs text-gray-400">
              Due: {{ formatDate(project.deadline) }}
            </span>
          </div>

          <!-- Client Context Preview -->
          <div v-if="project.clientContext" class="mt-3">
            <p class="text-xs text-gray-500 line-clamp-2">
              <span class="font-medium">Context:</span> {{ project.clientContext }}
            </p>
          </div>

          <!-- Footer -->
          <div class="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
            <span>Created: {{ formatDate(project.createdAt) }}</span>
            <span>By: {{ project.createdBy?.name || 'Unknown' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Project Modal -->
    <ProjectModal
      :is-open="showModal"
      :editing-project="editingProject"
      @close="showModal = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import ProjectModal from '@/Views/components/ProjectModal.vue'
import type { Project } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()

const user = computed(() => authStore.user)
const loading = computed(() => projectStore.loading)
const error = computed(() => projectStore.error)

const showModal = ref(false)
const editingProject = ref<Project | null>(null)
const activeFilter = ref<string>('ALL')

const filters = [
  { label: 'All', value: 'ALL' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'On Hold', value: 'ON_HOLD' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Archived', value: 'ARCHIVED' }
]

const filteredProjects = computed(() => {
  if (activeFilter.value === 'ALL') {
    return projectStore.projects
  }
  return projectStore.projects.filter(p => p.status === activeFilter.value)
})

const getProjectCount = (filter: string) => {
  if (filter === 'ALL') return projectStore.projects.length
  return projectStore.projects.filter(p => p.status === filter).length
}

const fetchProjects = async () => {
  await projectStore.fetchProjects()
}

const openCreateModal = () => {
  editingProject.value = null
  showModal.value = true
}

const openEditModal = (project: Project) => {
  editingProject.value = { ...project }
  showModal.value = true
}

const handleSubmit = async (data: Partial<Project>) => {
  if (editingProject.value) {
    await projectStore.updateProject(editingProject.value.id, data)
  } else {
    await projectStore.createProject(data)
  }
}

const handleDelete = async (id: number) => {
  if (confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
    await projectStore.deleteProject(id)
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchProjects()
})
</script>