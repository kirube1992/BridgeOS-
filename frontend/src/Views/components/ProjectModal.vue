<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <!-- Backdrop -->
      <div class="fixed inset-0 bg-black/50" @click="close"></div>

      <!-- Modal -->
      <div class="relative bg-white rounded-2xl shadow-xl max-w-lg w-full p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-900">
            {{ editingProject ? 'Edit Project' : 'Create New Project' }}
          </h3>
          <button @click="close" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Project Name *</label>
            <input
              v-model="form.name"
              type="text"
              required
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="e.g., BridgeOS Phase 2"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Description</label>
            <textarea
              v-model="form.description"
              rows="3"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="What is this project about?"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Client Context</label>
            <textarea
              v-model="form.clientContext"
              rows="2"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="Why does this project matter to the client?"
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Status</label>
              <select
                v-model="form.status"
                class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
              >
                <option value="ACTIVE">Active</option>
                <option value="ON_HOLD">On Hold</option>
                <option value="COMPLETED">Completed</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Deadline</label>
              <input
                v-model="form.deadline"
                type="date"
                class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div v-if="error" class="text-red-600 text-sm bg-red-50 p-3 rounded-lg">
            {{ error }}
          </div>

          <div class="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              @click="close"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {{ submitting ? 'Saving...' : editingProject ? 'Update Project' : 'Create Project' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Project } from '@/types'

const props = defineProps<{
  isOpen: boolean
  editingProject?: Project | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: Partial<Project>]
}>()

const form = ref<Partial<Project>>({
  name: '',
  description: '',
  clientContext: '',
  status: 'ACTIVE',
  deadline: ''
})

const submitting = ref(false)
const error = ref('')

const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    clientContext: '',
    status: 'ACTIVE',
    deadline: ''
  }
  error.value = ''
}

// Watch for editing project changes
watch(() => props.editingProject, (project) => {
  if (project) {
    form.value = {
      name: project.name || '',
      description: project.description || '',
      clientContext: project.clientContext || '',
      status: project.status || 'ACTIVE',
      deadline: project.deadline || ''
    }
  }
}, { immediate: true })

const close = () => {
  resetForm()
  emit('close')
}

const handleSubmit = async () => {
  if (!form.value.name?.trim()) {
    error.value = 'Project name is required'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    await emit('submit', { ...form.value })
    close()
  } catch (err: any) {
    error.value = err.message || 'Failed to save project'
  } finally {
    submitting.value = false
  }
}
</script>