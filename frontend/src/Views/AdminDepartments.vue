<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDepartmentStore } from '@/stores/department'
import { useUserStore } from '@/stores/user'
import DepartmentModal from '@/Views/components/DepartmentModal.vue'
import type { Department } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const departmentStore = useDepartmentStore()
const userStore = useUserStore()

const showModal = ref(false)
const editingDepartment = ref<Department | null>(null)
const deletingId = ref<number | null>(null)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)
let toastTimer: ReturnType<typeof setTimeout> | undefined

const user = computed(() => authStore.user)
const stats = computed(() => departmentStore.stats)
const departments = computed(() => departmentStore.filteredDepartments)
const loading = computed(() => departmentStore.loading)
const error = computed(() => departmentStore.error)
const selectedCount = computed(() => departmentStore.selectedDepartments.length)

const sortOptions = [
  { label: 'Name (A-Z)', value: 'name' },
  { label: 'Name (Z-A)', value: 'name_desc' },
  { label: 'Member Count', value: 'members' },
  { label: 'Creation Date', value: 'created' }
]

const departmentEmoji = (name: string) => {
  const value = name.toLowerCase()
  if (value.includes('engineer')) return '🛠️'
  if (value.includes('sales')) return '💼'
  if (value.includes('hr') || value.includes('people')) return '👥'
  if (value.includes('operation')) return '⚙️'
  return '🏢'
}

const showToast = (message: string, type: 'success' | 'error') => {
  toast.value = { message, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = null
  }, 3200)
}

const initials = (name: string) =>
  name
    .split(/\s+/)
    .map((part) => part[0] || '')
    .join('')
    .slice(0, 2)
    .toUpperCase()

const isActive = (department: Department) =>
  (department.status || 'ACTIVE').toString().toLowerCase() !== 'inactive'

const formatDate = (value?: string) => {
  if (!value) return 'Unknown'
  return new Date(value).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const openCreateModal = () => {
  editingDepartment.value = null
  showModal.value = true
}

const openEditModal = (department: Department) => {
  editingDepartment.value = { ...department }
  showModal.value = true
}

const handleSubmit = async (data: Record<string, unknown>) => {
  const result = editingDepartment.value
    ? await departmentStore.updateDepartment(editingDepartment.value.id, data)
    : await departmentStore.createDepartment(data)

  if (!result.success) {
    showToast(result.error || 'Could not save department.', 'error')
    return
  }

  showModal.value = false
  showToast(
    editingDepartment.value ? 'Department updated successfully.' : 'Department created successfully.',
    'success'
  )
  editingDepartment.value = null
  await departmentStore.fetchDepartments()
}

const handleDelete = async (department: Department) => {
  const memberWarning =
    (department.memberCount || 0) > 0
      ? `\n\nThis department has ${department.memberCount} member(s). Deleting it will unassign them.`
      : ''

  const confirmed = window.confirm(
    `Are you sure you want to delete "${department.name}"?\n\nThis will also remove the department from all users and work items.\nThis action cannot be undone.${memberWarning}`
  )

  if (!confirmed) return

  deletingId.value = department.id
  const result = await departmentStore.deleteDepartment(department.id)
  deletingId.value = null

  if (!result.success) {
    showToast(result.error || 'Could not delete department.', 'error')
    return
  }

  showToast('Department deleted successfully.', 'success')
}

const handleBulkDelete = async () => {
  if (!selectedCount.value) return

  const confirmed = window.confirm(
    `Delete ${selectedCount.value} selected department(s)? This action cannot be undone.`
  )
  if (!confirmed) return

  const result = await departmentStore.bulkDelete([...departmentStore.selectedDepartments])
  if (!result.success) {
    showToast(result.error || 'Bulk delete failed.', 'error')
    return
  }

  showToast('Selected departments deleted.', 'success')
}

const exportCsv = () => {
  const rows = [
    ['Name', 'Description', 'Member Count', 'Work Items', 'Created At', 'Status'],
    ...departmentStore.departments.map((department) => [
      department.name,
      department.description || '',
      String(department.memberCount || 0),
      String(department.workItemCount || 0),
      department.createdAt || '',
      department.status || 'ACTIVE'
    ])
  ]

  const csv = rows
    .map((row) => row.map((cell) => `"${String(cell).replaceAll('"', '""')}"`).join(','))
    .join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'departments.csv'
  link.click()
  URL.revokeObjectURL(url)
  showToast('Departments exported as CSV.', 'success')
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  await Promise.all([departmentStore.fetchDepartments(), userStore.fetchUsers()])
})
</script>

<template>
  <div class="admin-departments-page">
    <nav class="page-nav">
      <router-link to="/dashboard" class="brand">
        <span class="mark-icon">B</span>
        <span>BridgeOS</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/projects">Projects</router-link>
        <router-link to="/people">People</router-link>
        <router-link class="current" to="/admin/departments">Departments</router-link>
      </div>
      <div class="user-actions">
        <span>{{ user?.name || user?.email }}</span>
        <button type="button" @click="logout">Log out</button>
      </div>
    </nav>

    <main class="page-main">
      <header class="page-header">
        <div>
          <span class="eyebrow">Admin workspace</span>
          <h1>Department Management</h1>
          <p>Manage organizational units and team structures</p>
        </div>
        <div class="header-actions">
          <button type="button" class="secondary-btn" @click="exportCsv">Export All</button>
          <button type="button" class="primary-btn" @click="openCreateModal">+ New Department</button>
        </div>
      </header>

      <section class="stats-grid">
        <article class="stat-card">
          <span>Total Departments</span>
          <strong>{{ stats.total }}</strong>
        </article>
        <article class="stat-card">
          <span>Total Members</span>
          <strong>{{ stats.totalMembers }}</strong>
        </article>
        <article class="stat-card">
          <span>Active Departments</span>
          <strong>{{ stats.active }}</strong>
        </article>
        <article class="stat-card">
          <span>Empty Departments</span>
          <strong>{{ stats.empty }}</strong>
        </article>
      </section>

      <section class="toolbar">
        <label class="search-field">
          <span aria-hidden="true">⌕</span>
          <input
            :value="departmentStore.filters.search"
            type="search"
            placeholder="Search by name or description"
            @input="departmentStore.setFilter('search', ($event.target as HTMLInputElement).value)"
          />
        </label>

        <select
          :value="departmentStore.filters.sortBy"
          @change="departmentStore.setFilter('sortBy', ($event.target as HTMLSelectElement).value)"
        >
          <option v-for="option in sortOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>

        <select
          :value="departmentStore.filters.status"
          @change="departmentStore.setFilter('status', ($event.target as HTMLSelectElement).value)"
        >
          <option value="all">All statuses</option>
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
        </select>

        <button
          v-if="selectedCount"
          type="button"
          class="danger-btn"
          @click="handleBulkDelete"
        >
          Delete Selected ({{ selectedCount }})
        </button>
      </section>

      <div v-if="error" class="alert">
        <span>{{ error }}</span>
        <button type="button" @click="departmentStore.fetchDepartments()">Try again</button>
      </div>

      <div v-if="loading" class="state">Loading departments...</div>

      <div v-else-if="!departments.length" class="state empty-state">
        <div class="empty-icon">🏢</div>
        <strong>No departments found</strong>
        <p>Create your first department to organize teams and work items.</p>
        <button type="button" class="primary-btn" @click="openCreateModal">+ New Department</button>
      </div>

      <section v-else class="department-grid">
        <article
          v-for="department in departments"
          :key="department.id"
          class="department-card"
          :class="{ deleting: deletingId === department.id }"
        >
          <div class="card-top">
            <label class="select-box">
              <input
                type="checkbox"
                :checked="departmentStore.selectedDepartments.includes(department.id)"
                @change="departmentStore.toggleSelection(department.id)"
              />
            </label>
            <div class="card-title">
              <span class="dept-emoji">{{ departmentEmoji(department.name) }}</span>
              <div>
                <h2>{{ department.name }}</h2>
                <p>{{ department.description || 'No description provided.' }}</p>
              </div>
            </div>
            <span class="status-badge" :class="{ inactive: !isActive(department) }">
              {{ isActive(department) ? 'Active' : 'Inactive' }}
            </span>
          </div>

          <div class="card-meta">
            <div>
              <strong>{{ department.memberCount || 0 }}</strong>
              <span>members</span>
            </div>
            <div>
              <strong>{{ department.workItemCount || 0 }}</strong>
              <span>tasks</span>
            </div>
            <div>
              <strong>{{ formatDate(department.createdAt) }}</strong>
              <span>created</span>
            </div>
          </div>

          <div v-if="department.members?.length" class="avatar-stack">
            <span
              v-for="member in department.members"
              :key="member.id"
              class="avatar"
              :title="member.name"
            >
              {{ initials(member.name) }}
            </span>
            <span v-if="(department.memberCount || 0) > 3" class="avatar more">
              +{{ (department.memberCount || 0) - 3 }}
            </span>
          </div>

          <div class="card-actions">
            <button type="button" class="icon-btn" title="View details" @click="router.push(`/admin/departments/${department.id}`)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>
            <button type="button" class="icon-btn" title="Edit department" @click="openEditModal(department)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
            <button
              type="button"
              class="icon-btn danger"
              title="Delete department"
              :disabled="deletingId === department.id"
              @click="handleDelete(department)"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                <path d="M10 11v6M14 11v6M9 6V4h6v2" />
              </svg>
            </button>
          </div>
        </article>
      </section>
    </main>

    <DepartmentModal
      :is-open="showModal"
      :editing-department="editingDepartment"
      :users="userStore.users"
      @close="showModal = false"
      @submit="handleSubmit"
    />

    <Transition name="toast">
      <div v-if="toast" class="status-toast" :class="toast.type" role="status">
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.admin-departments-page {
  min-height: 100vh;
  background: var(--bridge-paper);
  color: var(--bridge-ink);
}

.page-nav,
.page-main {
  width: min(100% - 2rem, 1200px);
  margin: 0 auto;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  min-height: 70px;
  border-bottom: 1px solid var(--bridge-line);
  background: white;
  padding: 0.85rem 0;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  color: var(--bridge-ink);
  font-weight: 800;
  text-decoration: none;
}

.mark-icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--bridge-cyan);
}

.nav-links {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.nav-links a {
  color: var(--bridge-muted);
  text-decoration: none;
  font-size: 0.8rem;
}

.nav-links a.current,
.nav-links a:hover {
  color: var(--bridge-deep);
}

.user-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
}

.user-actions button {
  border: 1px solid var(--bridge-line);
  border-radius: 6px;
  background: white;
  padding: 0.35rem 0.7rem;
  cursor: pointer;
}

.page-main {
  padding: 2.5rem 0 3rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.eyebrow {
  color: #247184;
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 0.4rem 0 0.25rem;
  font-size: clamp(1.8rem, 4vw, 2.5rem);
}

.page-header p {
  margin: 0;
  color: var(--bridge-muted);
  font-size: 0.85rem;
}

.header-actions,
.toolbar {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  align-items: center;
}

.primary-btn,
.secondary-btn,
.danger-btn {
  border-radius: 8px;
  padding: 0.55rem 0.95rem;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  border: 0;
  color: white;
  background: var(--bridge-menu);
}

.secondary-btn,
.danger-btn {
  border: 1px solid var(--bridge-line);
  background: white;
  color: var(--bridge-ink);
}

.danger-btn {
  color: #914f42;
  border-color: #f2c6bb;
  background: #fff3ef;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: white;
  border: 1px solid var(--bridge-line);
  border-radius: 12px;
  padding: 1rem 1.1rem;
}

.stat-card span {
  display: block;
  font-size: 0.72rem;
  color: var(--bridge-muted);
}

.stat-card strong {
  display: block;
  margin-top: 0.35rem;
  font-size: 1.6rem;
}

.toolbar {
  margin-bottom: 1.25rem;
}

.search-field {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  min-width: 220px;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  background: white;
  padding: 0.45rem 0.75rem;
}

.search-field input,
.toolbar select {
  border: 0;
  outline: none;
  background: transparent;
  font-size: 0.8rem;
}

.toolbar select {
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  background: white;
  padding: 0.5rem 0.75rem;
}

.alert {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  border: 1px solid #f2c6bb;
  border-radius: 8px;
  background: #fff3ef;
  color: #914f42;
  padding: 0.85rem 1rem;
  margin-bottom: 1rem;
}

.state {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--bridge-muted);
}

.empty-state .empty-icon {
  font-size: 2.4rem;
  margin-bottom: 0.5rem;
}

.department-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.department-card {
  background: white;
  border: 1px solid var(--bridge-line);
  border-radius: 14px;
  padding: 1rem;
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.2s ease;
}

.department-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(16, 35, 43, 0.06);
}

.department-card.deleting {
  opacity: 0.5;
}

.card-top {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 0.75rem;
  align-items: start;
}

.card-title {
  display: flex;
  gap: 0.65rem;
}

.dept-emoji {
  font-size: 1.4rem;
}

.card-title h2 {
  margin: 0;
  font-size: 1rem;
}

.card-title p {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
  color: var(--bridge-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.status-badge {
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  font-size: 0.65rem;
  font-weight: 800;
  color: #286b78;
  background: var(--bridge-cyan-soft);
}

.status-badge.inactive {
  color: #6b7280;
  background: #f3f4f6;
}

.card-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.5rem;
  margin: 1rem 0;
}

.card-meta strong {
  display: block;
  font-size: 0.95rem;
}

.card-meta span {
  font-size: 0.68rem;
  color: var(--bridge-muted);
}

.avatar-stack {
  display: flex;
  align-items: center;
  min-height: 32px;
}

.avatar {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--bridge-cyan);
  color: var(--bridge-ink);
  font-size: 0.65rem;
  font-weight: 800;
  border: 2px solid white;
  margin-left: -8px;
}

.avatar:first-child {
  margin-left: 0;
}

.avatar.more {
  background: #eef2f7;
  color: var(--bridge-muted);
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.35rem;
  margin-top: 0.85rem;
  padding-top: 0.85rem;
  border-top: 1px solid var(--bridge-line);
}

.icon-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  background: white;
  color: var(--bridge-muted);
  cursor: pointer;
}

.icon-btn svg {
  width: 15px;
  height: 15px;
}

.icon-btn.danger:hover {
  color: #914f42;
  border-color: #f2c6bb;
  background: #fff3ef;
}

.status-toast {
  position: fixed;
  right: 1.25rem;
  bottom: 1.25rem;
  z-index: 20;
  border: 1px solid #b8dfd1;
  border-radius: 7px;
  padding: 0.8rem 1rem;
  color: #215b4a;
  background: #effbf5;
  box-shadow: 0 8px 24px rgba(25, 71, 78, 0.16);
  font-size: 0.75rem;
}

.status-toast.error {
  border-color: #edc5bc;
  color: #914f42;
  background: #fff4f1;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 1024px) {
  .department-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .department-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .nav-links {
    display: none;
  }
}
</style>
