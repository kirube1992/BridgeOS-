<template>
  <div class="bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md transition">
    <div class="flex items-start gap-4">
      <div class="shrink-0">
        <span class="inline-flex items-center justify-center w-14 h-14 rounded-full bg-indigo-100 text-indigo-700 text-2xl font-semibold">
          {{ initials }}
        </span>
      </div>
      <div class="flex-1 min-w-0">
        <h3 class="text-lg font-semibold text-gray-900 truncate">{{ user.name }}</h3>
        <div class="flex flex-wrap items-center gap-2 mt-1">
          <span class="inline-flex px-2 py-0.5 text-xs font-medium rounded-full" :class="roleClass(user.role)">
            {{ user.role }}
          </span>
          <span class="text-sm text-gray-500">{{ user.email }}</span>
        </div>
        <div class="mt-2 flex flex-wrap gap-3 text-sm text-gray-500">
          <span v-if="user.department" class="flex items-center gap-1">
            <span class="text-gray-400">🏢</span> {{ user.department.name }}
          </span>
          <span class="flex items-center gap-1">
            <span class="text-gray-400">📋</span> {{ taskCount }} tasks
          </span>
        </div>
      </div>
      <div class="shrink-0">
        <span class="inline-flex px-3 py-1 text-xs font-medium rounded-full" :class="statusClass">
          Active
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { User } from '@/types'

const props = defineProps<{
  user: User
  taskCount?: number
}>()

const initials = computed(() => {
  const name = props.user.name || props.user.email || 'U'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase()
  return name.slice(0, 2).toUpperCase()
})

const roleClass = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: 'bg-purple-100 text-purple-700',
    ETHIOPIAN_TEAM: 'bg-green-100 text-green-700',
    CHINESE_DEVELOPER: 'bg-blue-100 text-blue-700',
    HQ_CONTACT: 'bg-amber-100 text-amber-700'
  }
  return map[role] || 'bg-gray-100 text-gray-700'
}

const statusClass = computed(() => {
  return 'bg-green-100 text-green-700'
})
</script>