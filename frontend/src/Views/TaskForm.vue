<template>
  <div class="task-form-shell">
    <!-- Navbar (same as other pages) -->
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

    <!-- Main Form -->
    <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">
          {{ isEdit ? 'Edit Task' : 'Create New Task' }}
        </h1>
        <p class="text-sm text-gray-500">
          {{ isEdit ? 'Update task details' : 'Add a new work item to your project' }}
        </p>
      </div>

      <form @submit.prevent="handleSubmit" class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-6">
        <!-- Basic Information -->
        <div class="space-y-4">
          <!-- Title -->
          <div>
            <label for="title" class="block text-sm font-medium text-gray-700">
              Title <span class="text-red-500">*</span>
            </label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              required
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="e.g., Build authentication system"
            />
          </div>

          <!-- Description -->
          <div>
            <label for="description" class="block text-sm font-medium text-gray-700">Description</label>
            <textarea
              id="description"
              v-model="form.description"
              rows="3"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="What needs to be done?"
            />
          </div>
        </div>

        <!-- 🔥 Business Context Notes (Signature Feature) -->
        <div class="bg-indigo-50 border border-indigo-200 rounded-lg p-4 space-y-2">
          <div class="flex items-center gap-2">
            <label for="businessContextNotes" class="block text-sm font-semibold text-indigo-800">
              Business Context Notes 💡
            </label>
            <span class="text-xs text-indigo-600 font-medium">— The WHY</span>
            <span class="ml-auto text-xs text-indigo-600">{{ form.businessContextNotes?.length || 0 }} characters</span>
          </div>
          <p class="text-xs text-indigo-600">Why does this task matter? What problem does it solve for the client or team?</p>
          <textarea
            id="businessContextNotes"
            v-model="form.businessContextNotes"
            rows="3"
            class="mt-1 w-full px-3 py-2 border border-indigo-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
            placeholder="Explain the broader impact and reasoning behind this task..."
          />
        </div>

        <!-- Acceptance Criteria -->
        <div>
          <label for="acceptanceCriteria" class="block text-sm font-medium text-gray-700">
            Acceptance Criteria
          </label>
          <textarea
            id="acceptanceCriteria"
            v-model="form.acceptanceCriteria"
            rows="3"
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            placeholder="- Users can register\n- Users can login\n- JWT tokens are valid"
          />
        </div>

        <!-- Status & Priority -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="status" class="block text-sm font-medium text-gray-700">Status</label>
            <select
              id="status"
              v-model="form.status"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="REVIEW">Review</option>
              <option value="DONE">Done</option>
            </select>
          </div>
          <div>
            <label for="priority" class="block text-sm font-medium text-gray-700">Priority</label>
            <select
              id="priority"
              v-model="form.priority"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
        </div>

        <!-- Project, Assignee, Department -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label for="projectId" class="block text-sm font-medium text-gray-700">
              Project <span class="text-red-500">*</span>
            </label>
            <select
              id="projectId"
              v-model="form.projectId"
              required
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              <option v-for="p in projects" :key="p.id" :value="p.id">
                {{ p.name }}
              </option>
            </select>
          </div>
          <div>
            <label for="assignedTo" class="block text-sm font-medium text-gray-700">Assign to</label>
            <select
              id="assignedTo"
              v-model="form.assignedToId"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              <option :value="null">Unassigned</option>
              <option v-for="u in users" :key="u.id" :value="u.id">
                {{ u.name }}
              </option>
            </select>
          </div>
          <div>
            <label for="departmentId" class="block text-sm font-medium text-gray-700">Department</label>
            <select
              id="departmentId"
              v-model="form.departmentId"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
            >
              <option v-for="d in departments" :key="d.id" :value="d.id">
                {{ d.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- Deadline -->
        <div>
          <label for="deadline" class="block text-sm font-medium text-gray-700">Deadline</label>
          <input
            id="deadline"
            v-model="form.deadline"
            type="date"
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <!-- Clarity Score Preview (read-only) -->
        <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-700">Clarity Score</span>
            <span class="text-sm font-bold" :class="clarityScoreColor">
              {{ clarityScore }} / 100
            </span>
          </div>
          <div class="w-full h-2 bg-gray-200 rounded-full mt-1">
            <div class="h-full rounded-full transition-all duration-300" :style="{ width: clarityScore + '%' }" :class="clarityScoreBarColor"></div>
          </div>
          <p class="text-xs text-gray-400 mt-1">Based on Business Context Notes, Acceptance Criteria, deadline, and field completeness.</p>
        </div>

        <!-- Error message -->
        <div v-if="submitError" class="text-red-600 text-sm bg-red-50 p-3 rounded-lg">
          {{ submitError }}
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <router-link to="/tasks" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition">
            Cancel
          </router-link>
          <button
            type="submit"
            :disabled="submitting"
            class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
          >
            {{ submitting ? 'Saving...' : isEdit ? 'Update Task' : 'Create Task' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTaskStore } from '@/stores/task'
import { useProjectStore } from '@/stores/project'
import api from '@/API/index'
import type { Project, User, Department, WorkItem } from '@/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const taskStore = useTaskStore()
const projectStore = useProjectStore()

const user = computed(() => authStore.user)
const isEdit = computed(() => !!route.params.id)

// Load projects, users, departments
const projects = ref<Project[]>([])
const users = ref<User[]>([])
const departments = ref<Department[]>([])

const form = reactive({
  id: null as number | null,
  title: '',
  description: '',
  businessContextNotes: '',
  acceptanceCriteria: '',
  status: 'TODO' as WorkItem['status'],
  priority: 'MEDIUM' as WorkItem['priority'],
  deadline: '',
  projectId: null as number | null,
  assignedToId: null as number | null,
  departmentId: null as number | null,
})

const submitting = ref(false)
const submitError = ref('')
const loading = ref(false)

// Clarity score calculation (mirroring backend logic)
const clarityScore = computed(() => {
  let score = 0
  if (form.businessContextNotes && form.businessContextNotes.length > 0) score += 25
  if (form.acceptanceCriteria && form.acceptanceCriteria.length > 0) score += 25
  if (form.deadline) score += 15
  if (form.title && form.title.length >= 10) score += 20
  if (form.description && form.description.length >= 20) score += 15
  return Math.min(100, score)
})

const clarityScoreColor = computed(() => {
  const s = clarityScore.value
  if (s >= 80) return 'text-green-600'
  if (s >= 60) return 'text-yellow-600'
  return 'text-red-600'
})

const clarityScoreBarColor = computed(() => {
  const s = clarityScore.value
  if (s >= 80) return 'bg-green-500'
  if (s >= 60) return 'bg-yellow-500'
  return 'bg-red-500'
})

// Fetch dropdown options
const fetchOptions = async () => {
  try {
    const [projectsRes, usersRes, deptsRes] = await Promise.all([
      api.get<Project[]>('/projects'),
      api.get<User[]>('/users'),
      api.get<Department[]>('/departments')
    ])
    projects.value = projectsRes.data
    users.value = usersRes.data
    departments.value = deptsRes.data
  } catch (err) {
    console.error('Failed to load options:', err)
  }
}

// Load existing task for edit
const loadTask = async () => {
  const id = Number(route.params.id)
  if (isEdit.value && id) {
    loading.value = true
    await taskStore.fetchTaskById(id)
    const task = taskStore.currentTask
    if (task) {
      form.id = task.id
      form.title = task.title
      form.description = task.description || ''
      form.businessContextNotes = task.businessContextNotes || ''
      form.acceptanceCriteria = task.acceptanceCriteria || ''
      form.status = task.status
      form.priority = task.priority
      form.deadline = task.deadline || ''
      form.projectId = task.project?.id || null
      form.assignedToId = task.assignedTo?.id || null
      form.departmentId = task.department?.id || null
    }
    loading.value = false
  }
}

// Submit handler
const handleSubmit = async () => {
  if (!form.projectId) {
    submitError.value = 'Please select a project.'
    return
  }
  if (!form.title.trim()) {
    submitError.value = 'Title is required.'
    return
  }

  submitting.value = true
  submitError.value = ''

  const payload = {
    title: form.title,
    description: form.description,
    businessContextNotes: form.businessContextNotes,
    acceptanceCriteria: form.acceptanceCriteria,
    status: form.status,
    priority: form.priority,
    deadline: form.deadline || null,
    // The backend receives these IDs as query parameters for creation.
    projectId: form.projectId,
    assignedToUserId: form.assignedToId,
    departmentId: form.departmentId,
    createdByUserID: user.value?.id,
  }

  let result
  if (isEdit.value && form.id) {
    // Remove extra fields not needed for update
    const { projectId, assignedToUserId, departmentId, createdByUserID, ...updateData } = payload
    const selectedProject = projects.value.find(project => project.id === projectId)
    const selectedUser = users.value.find(selectedUser => selectedUser.id === assignedToUserId)
    const selectedDepartment = departments.value.find(department => department.id === departmentId)
    result = await taskStore.updateTask(form.id, {
      ...updateData,
      project: selectedProject,
      assignedTo: selectedUser || null,
      ...(selectedDepartment ? { department: selectedDepartment } : {}),
    })
  } else {
    // For create, we need to pass query params; the store's createTask expects partial WorkItem
    // We'll call api directly to handle query params
    try {
      const response = await api.post('/work-items', payload, {
        params: {
          projectId: form.projectId,
          createdByUserID: user.value?.id,
          departmentId: form.departmentId || 1,
          assignedToUserId: form.assignedToId || undefined,
        }
      })
      result = { success: true, data: response.data }
    } catch (err: any) {
      const status = err.response?.status
      result = {
        success: false,
        error: status === 403
          ? 'You are not authorized to create this task.'
          : err.response?.data?.message || 'Failed to create task'
      }
    }
  }

  if (result.success) {
    router.push(`/tasks/${result.data.id}`)
  } else {
    submitError.value = result.error || 'Failed to save task'
  }

  submitting.value = false
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  await fetchOptions()
  if (isEdit.value) {
    await loadTask()
  }
})
</script>