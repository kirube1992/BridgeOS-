<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 bg-black/50" @click="close" />

      <div class="relative bg-white rounded-2xl shadow-xl max-w-lg w-full p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-900">
            {{ editingDepartment ? 'Edit Department' : 'New Department' }}
          </h3>
          <button type="button" class="text-gray-400 hover:text-gray-600" @click="close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div>
            <label class="block text-sm font-medium text-gray-700">Department Name *</label>
            <input
              v-model="form.name"
              type="text"
              required
              maxlength="80"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[var(--bridge-cyan)] focus:border-[var(--bridge-cyan)]"
              placeholder="e.g., Engineering"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Description</label>
            <textarea
              v-model="form.description"
              rows="3"
              maxlength="500"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[var(--bridge-cyan)] focus:border-[var(--bridge-cyan)]"
              placeholder="What does this department do?"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Default Workflow</label>
            <select
              v-model="form.defaultWorkFlow"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[var(--bridge-cyan)]"
            >
              <option value="">None</option>
              <option value="Task">Task</option>
              <option value="Support Ticket">Support Ticket</option>
              <option value="Sales Lead">Sales Lead</option>
              <option value="Custom">Custom</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">Department Lead</label>
            <select
              v-model="form.departmentLeadId"
              class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[var(--bridge-cyan)]"
            >
              <option :value="null">No lead assigned</option>
              <option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.name }} ({{ user.email }})
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
            <div class="flex gap-3">
              <label class="inline-flex items-center gap-2 text-sm">
                <input v-model="form.status" type="radio" value="ACTIVE" />
                Active
              </label>
              <label class="inline-flex items-center gap-2 text-sm">
                <input v-model="form.status" type="radio" value="INACTIVE" />
                Inactive
              </label>
            </div>
          </div>

          <div v-if="error" class="text-red-600 text-sm bg-red-50 p-3 rounded-lg">
            {{ error }}
          </div>

          <div class="flex justify-end gap-3 pt-4">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              @click="close"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-4 py-2 text-sm font-medium text-white bg-[var(--bridge-menu)] rounded-lg hover:bg-[var(--bridge-menu-dark)] disabled:opacity-50"
              :disabled="submitting"
            >
              {{ submitting ? 'Saving...' : editingDepartment ? 'Save Changes' : 'Create Department' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Department, User } from '@/types'

const props = defineProps<{
  isOpen: boolean
  editingDepartment: Department | null
  users: User[]
}>()

const emit = defineEmits<{
  close: []
  submit: [data: Record<string, unknown>]
}>()

const form = ref({
  name: '',
  description: '',
  defaultWorkFlow: '',
  departmentLeadId: null as number | null,
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE'
})

const error = ref('')
const submitting = ref(false)

const resetForm = () => {
  if (props.editingDepartment) {
    form.value = {
      name: props.editingDepartment.name,
      description: props.editingDepartment.description || '',
      defaultWorkFlow: props.editingDepartment.defaultWorkFlow || '',
      departmentLeadId: props.editingDepartment.departmentLead?.id ?? null,
      status:
        (props.editingDepartment.status?.toString().toUpperCase() as 'ACTIVE' | 'INACTIVE') ||
        'ACTIVE'
    }
  } else {
    form.value = {
      name: '',
      description: '',
      defaultWorkFlow: '',
      departmentLeadId: null,
      status: 'ACTIVE'
    }
  }
  error.value = ''
}

watch(
  () => [props.isOpen, props.editingDepartment] as const,
  ([open]) => {
    if (open) resetForm()
  }
)

const close = () => emit('close')

const handleSubmit = async () => {
  error.value = ''
  submitting.value = true
  try {
    emit('submit', {
      name: form.value.name.trim(),
      description: form.value.description.trim() || null,
      defaultWorkFlow: form.value.defaultWorkFlow || null,
      departmentLeadId: form.value.departmentLeadId,
      status: form.value.status
    })
  } finally {
    submitting.value = false
  }
}
</script>
