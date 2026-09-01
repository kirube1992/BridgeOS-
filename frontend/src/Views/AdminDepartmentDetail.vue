<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDepartmentStore } from '@/stores/department'
import { useUserStore } from '@/stores/user'
import DepartmentModal from '@/Views/components/DepartmentModal.vue'
import type { Department, User, WorkItem } from '@/types'
import api from '@/API/index'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const departmentStore = useDepartmentStore()
const userStore = useUserStore()

const department = ref<Department | null>(null)
const members = ref<User[]>([])
const workItems = ref<WorkItem[]>([])
const loading = ref(true)
const error = ref('')
const showModal = ref(false)

const departmentId = computed(() => Number(route.params.id))

const isActive = computed(() =>
  (department.value?.status || 'ACTIVE').toString().toLowerCase() !== 'inactive'
)

const initials = (name: string) =>
  name
    .split(/\s+/)
    .map((part) => part[0] || '')
    .join('')
    .slice(0, 2)
    .toUpperCase()

const formatDate = (value?: string) => {
  if (!value) return 'Unknown'
  return new Date(value).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const loadDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const [departmentData] = await Promise.all([
      departmentStore.fetchDepartmentById(departmentId.value),
      userStore.fetchUsers()
    ])

    department.value = departmentData

    members.value = userStore.users.filter(
      (user) => user.department?.id === departmentId.value
    )

    const workItemsResponse = await api.get<WorkItem[]>('/work-items')
    const allWorkItems = Array.isArray(workItemsResponse.data) ? workItemsResponse.data : []
    workItems.value = allWorkItems.filter(
      (item) => item.department?.id === departmentId.value
    )
  } catch {
    error.value = 'Failed to load department details.'
  } finally {
    loading.value = false
  }
}

const handleUpdate = async (data: Record<string, unknown>) => {
  if (!department.value) return
  const result = await departmentStore.updateDepartment(department.value.id, data)
  if (!result.success) {
    alert(result.error || 'Failed to update department.')
    return
  }
  showModal.value = false
  await loadDetail()
}

const handleDelete = async () => {
  if (!department.value) return

  const memberCount = department.value.memberCount || members.value.length
  const memberWarning =
    memberCount > 0
      ? `\n\nThis department has ${memberCount} member(s). Deleting it will unassign them.`
      : ''

  const confirmed = window.confirm(
    `Are you sure you want to delete "${department.value.name}"?\n\nThis will also remove the department from all users and work items.\nThis action cannot be undone.${memberWarning}`
  )

  if (!confirmed) return

  const result = await departmentStore.deleteDepartment(department.value.id)
  if (!result.success) {
    alert(result.error || 'Failed to delete department.')
    return
  }

  router.push('/admin/departments')
}

onMounted(loadDetail)
</script>

<template>
  <div class="detail-page">
    <nav class="page-nav">
      <router-link to="/admin/departments" class="back-link">← Back to departments</router-link>
    </nav>

    <main class="page-main">
      <div v-if="loading" class="state">Loading department...</div>
      <div v-else-if="error" class="state error">{{ error }}</div>
      <template v-else-if="department">
        <header class="detail-header">
          <div>
            <span class="eyebrow">Department detail</span>
            <h1>{{ department.name }}</h1>
            <p>{{ department.description || 'No description provided.' }}</p>
            <span class="status-badge" :class="{ inactive: !isActive }">
              {{ isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
          <div class="header-actions">
            <button type="button" class="secondary-btn" @click="showModal = true">Edit</button>
            <button type="button" class="danger-btn" @click="handleDelete">Delete</button>
          </div>
        </header>

        <section class="info-grid">
          <article class="info-card">
            <span>Members</span>
            <strong>{{ department.memberCount || members.length }}</strong>
          </article>
          <article class="info-card">
            <span>Work items</span>
            <strong>{{ department.workItemCount || workItems.length }}</strong>
          </article>
          <article class="info-card">
            <span>Workflow</span>
            <strong>{{ department.defaultWorkFlow || 'Not set' }}</strong>
          </article>
          <article class="info-card">
            <span>Created</span>
            <strong>{{ formatDate(department.createdAt) }}</strong>
          </article>
        </section>

        <section class="panel">
          <header>
            <h2>Members</h2>
            <p>People assigned to this department</p>
          </header>
          <div v-if="!members.length" class="panel-state">No members in this department.</div>
          <div v-else class="member-list">
            <article v-for="member in members" :key="member.id" class="member-row">
              <span class="avatar">{{ initials(member.name || member.email) }}</span>
              <div>
                <strong>{{ member.name }}</strong>
                <small>{{ member.email }}</small>
                <em>{{ member.role }}</em>
              </div>
              <router-link :to="`/profile/${member.id}`">View profile</router-link>
            </article>
          </div>
        </section>

        <section class="panel">
          <header>
            <h2>Work items</h2>
            <p>Tasks linked to this department</p>
          </header>
          <div v-if="!workItems.length" class="panel-state">No work items linked to this department.</div>
          <div v-else class="work-item-list">
            <article v-for="item in workItems" :key="item.id" class="work-item-row">
              <div>
                <strong>{{ item.title }}</strong>
                <small>{{ item.status }} · {{ item.assignedTo?.name || 'Unassigned' }}</small>
              </div>
              <router-link :to="`/tasks/${item.id}`">Open task</router-link>
            </article>
          </div>
        </section>
      </template>
    </main>

    <DepartmentModal
      :is-open="showModal"
      :editing-department="department"
      :users="userStore.users"
      @close="showModal = false"
      @submit="handleUpdate"
    />
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: var(--bridge-paper);
  color: var(--bridge-ink);
}

.page-nav,
.page-main {
  width: min(100% - 2rem, 1000px);
  margin: 0 auto;
}

.page-nav {
  padding: 1rem 0;
}

.back-link {
  color: var(--bridge-muted);
  text-decoration: none;
  font-size: 0.8rem;
}

.page-main {
  padding-bottom: 3rem;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  border-bottom: 1px solid var(--bridge-line);
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
}

.eyebrow {
  color: #247184;
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.detail-header h1 {
  margin: 0.4rem 0 0.25rem;
  font-size: clamp(1.8rem, 4vw, 2.4rem);
}

.detail-header p {
  margin: 0 0 0.75rem;
  color: var(--bridge-muted);
}

.status-badge {
  display: inline-block;
  border-radius: 999px;
  padding: 0.25rem 0.6rem;
  font-size: 0.68rem;
  font-weight: 800;
  color: #286b78;
  background: var(--bridge-cyan-soft);
}

.status-badge.inactive {
  color: #6b7280;
  background: #f3f4f6;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.secondary-btn,
.danger-btn {
  border-radius: 8px;
  padding: 0.55rem 0.95rem;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}

.secondary-btn {
  border: 1px solid var(--bridge-line);
  background: white;
}

.danger-btn {
  border: 1px solid #f2c6bb;
  background: #fff3ef;
  color: #914f42;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-card,
.panel {
  background: white;
  border: 1px solid var(--bridge-line);
  border-radius: 12px;
}

.info-card {
  padding: 1rem;
}

.info-card span {
  display: block;
  font-size: 0.72rem;
  color: var(--bridge-muted);
}

.info-card strong {
  display: block;
  margin-top: 0.35rem;
  font-size: 1.1rem;
}

.panel {
  margin-bottom: 1rem;
  overflow: hidden;
}

.panel header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--bridge-line);
}

.panel header h2 {
  margin: 0;
  font-size: 1rem;
}

.panel header p {
  margin: 0.2rem 0 0;
  font-size: 0.75rem;
  color: var(--bridge-muted);
}

.panel-state {
  padding: 2rem 1.25rem;
  text-align: center;
  color: var(--bridge-muted);
}

.member-list,
.work-item-list {
  padding: 0.75rem 1.25rem 1.25rem;
  display: grid;
  gap: 0.75rem;
}

.member-row,
.work-item-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  padding: 0.8rem 1rem;
}

.avatar {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--bridge-cyan);
  font-size: 0.7rem;
  font-weight: 800;
}

.member-row strong,
.work-item-row strong {
  display: block;
}

.member-row small,
.member-row em,
.work-item-row small {
  display: block;
  font-size: 0.72rem;
  color: var(--bridge-muted);
}

.member-row a,
.work-item-row a {
  margin-left: auto;
  font-size: 0.75rem;
  color: var(--bridge-deep);
  text-decoration: none;
}

.state {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--bridge-muted);
}

.state.error {
  color: #914f42;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
