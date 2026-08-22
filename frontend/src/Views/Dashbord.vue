<template>
  <div class="dashboard-shell">
    <!-- Side Navigation -->
    <aside class="side-nav">
      <div class="bridge-mark">
        <span class="mark-icon">B</span>
        <span>BridgeOS</span>
      </div>

      <div class="nav-label">WORKSPACE</div>
      <a class="nav-link active" href="#overview">
        <span class="nav-icon">◈</span>
        <span>Overview</span>
      </a>
      <router-link to="/tasks" class="nav-link">
        <span class="nav-icon">□</span>
        <span>My Tasks</span>
      </router-link>
      <router-link to="/projects" class="nav-link">
        <span class="nav-icon">⌘</span>
        <span>Projects</span>
      </router-link>
      <router-link to="/decisions" class="nav-link">
        <span class="nav-icon">◌</span>
        <span>Decisions</span>
      </router-link>
      <router-link to="/analytics" class="nav-link">
        <span class="nav-icon">▥</span>
        <span>Analytics</span>
      </router-link>
      <router-link to="/people" class="nav-link">
        <span class="nav-icon">◎</span>
        <span>People & Teams</span>
      </router-link>

      <div class="side-bottom">
        <div class="profile-row">
          <span class="avatar">{{ initials }}</span>
          <div>
            <strong>{{ displayName }}</strong>
            <small>{{ user?.role || 'Team Member' }}</small>
          </div>
        </div>
        <button class="logout-button" type="button" @click="logout">Log out / 退出</button>
      </div>
    </aside>

    <!-- Main Content -->
    <section class="dashboard-main">
      <!-- Top Bar -->
      <header class="topbar">
        <div>
          <div class="date-chip">{{ today }}</div>
          <h1>Good morning, {{ firstName }}.</h1>
        </div>
        <div class="topbar-actions">
          <button class="refresh-button" type="button" :disabled="loading" @click="fetchDashboardData">
            {{ loading ? 'Loading...' : '↻ Refresh' }}
          </button>
          <div class="locale-toggle">
            <button class="locale-btn" :class="{ active: locale === 'en' }" @click="changeLocale('en')">English</button>
            <span class="divider">·</span>
            <button class="locale-btn" :class="{ active: locale === 'zh' }" @click="changeLocale('zh')">中文</button>
          </div>
        </div>
      </header>

      <!-- Stats -->
      <section class="stat-grid">
        <article class="stat-card">
          <span>Active projects / 活跃项目</span>
          <strong>{{ activeProjectCount }}</strong>
          <em>{{ projectCount }} total projects</em>
        </article>
        <article class="stat-card">
          <span>Open tasks / 待处理任务</span>
          <strong>{{ taskCount }}</strong>
          <em>{{ taskCount }} open tasks</em>
        </article>
        <article class="stat-card">
          <span>Team members / 团队成员</span>
          <strong>{{ userCount }}</strong>
          <em>Live team directory</em>
        </article>
      </section>

      <!-- Error Alert -->
      <div v-if="errorMessage" class="dashboard-alert">
        <span>{{ errorMessage }}</span>
        <button type="button" @click="fetchDashboardData">Try again</button>
      </div>

      <!-- Work Queue & Team Pulse -->
      <div class="workspace-grid">
        <!-- Work Queue -->
        <section class="panel" id="tasks">
          <header class="panel-header">
            <div>
              <h2>Work queue / 工作队列</h2>
              <p>Shared priorities, clear next steps.</p>
            </div>
            <router-link to="/tasks/new" class="add-button">+ Add task</router-link>
          </header>

          <div v-if="loading" class="panel-state">Loading tasks...</div>
          <div v-else-if="!taskRows.length" class="panel-state">No recent tasks found.</div>
          <div v-else class="task-list">
            <article v-for="task in taskRows" :key="task.id" class="task-row">
              <span class="task-dot" :class="task.tone"></span>
              <div>
                <div class="task-title">{{ task.title }}</div>
                <div class="task-meta">{{ task.owner }} · {{ task.context }}</div>
              </div>
              <span class="task-status" :class="task.tone">{{ task.status }}</span>
            </article>
          </div>
        </section>

        <!-- Team Pulse -->
        <aside class="panel" id="people">
          <header class="panel-header">
            <div>
              <h2>Team pulse / 团队动态</h2>
              <p>Workload by location</p>
            </div>
          </header>

          <div v-if="loading" class="panel-state">Loading team...</div>
          <div v-else-if="!teamMembers.length" class="panel-state">No team members found.</div>
          <div v-else class="side-panel-body">
            <div class="member-list">
              <div v-for="member in visibleMembers" :key="member.id" class="member-row">
                <span class="avatar small-avatar">{{ memberInitials(member) }}</span>
                <div>
                  <strong>{{ member.name || member.email }}</strong>
                  <small>{{ member.department?.name || member.role }}</small>
                </div>
              </div>
              <div v-if="teamMembers.length > visibleMembers.length" class="member-more">
                +{{ teamMembers.length - visibleMembers.length }} more
              </div>
            </div>
            <!-- Simple location breakdown -->
            <div class="location-stats">
              <div class="location-item">
                <span class="flag">🇪🇹</span>
                <span>Ethiopia</span>
                <span class="count">{{ ethiopiaCount }}</span>
              </div>
              <div class="location-item">
                <span class="flag">🇨🇳</span>
                <span>China</span>
                <span class="count">{{ chinaCount }}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import api from '@/API/index'
import type { Project, WorkItem, User } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()

// ===== Auth =====
const user = computed(() => authStore.user)
const displayName = computed(() => {
  const name = user.value?.name?.trim()
  if (name) return name
  return user.value?.email?.split('@')[0] || 'Member'
})
const firstName = computed(() => displayName.value.split(' ')[0] || displayName.value)
const initials = computed(() => {
  const parts = displayName.value.split(' ')
  if (parts.length >= 2) return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase()
  return displayName.value.slice(0, 2).toUpperCase()
})

// ===== Locale =====
const locale = ref<'en' | 'zh'>('en')
const changeLocale = (lang: 'en' | 'zh') => {
  locale.value = lang
  // In a real app, you'd update i18n here
}

// ===== Data =====
const loading = ref(false)
const errorMessage = ref('')
const projectCount = ref(0)
const taskCount = ref(0)
const userCount = ref(0)
const recentTasks = ref<WorkItem[]>([])
const teamMembers = ref<User[]>([])

// ===== Derived =====
const activeProjectCount = computed(() => projectStore.activeProjects.length || 0)
const today = computed(() => {
  const d = new Date()
  return d.toLocaleDateString('en-US', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
})

const taskRows = computed(() => {
  return recentTasks.value.slice(0, 5).map(task => {
    const statusMap: Record<string, { tone: string; label: string }> = {
      TODO: { tone: 'pending', label: 'TODO' },
      IN_PROGRESS: { tone: 'in-progress', label: 'IN PROGRESS' },
      REVIEW: { tone: 'review', label: 'IN REVIEW' },
      DONE: { tone: 'done', label: 'DONE' }
    }
    const info = statusMap[task.status] || { tone: 'pending', label: task.status }
    return {
      id: task.id,
      title: task.title,
      owner: task.assignedTo?.name || task.createdBy?.name || 'Unassigned',
      context: task.department?.name || 'General',
      status: info.label,
      tone: info.tone
    }
  })
})

const visibleMembers = computed(() => teamMembers.value.slice(0, 5))

const ethiopiaCount = computed(() => teamMembers.value.filter(
  m => m.department?.name?.toLowerCase().includes('ethiopia') || m.role === 'ETHIOPIAN_TEAM'
).length)
const chinaCount = computed(() => teamMembers.value.filter(
  m => m.department?.name?.toLowerCase().includes('china') || m.role === 'CHINESE_DEVELOPER'
).length)

// ===== Methods =====
const memberInitials = (member: User): string => {
  const name = member.name || member.email || 'U'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase()
  return name.slice(0, 2).toUpperCase()
}

const collection = <T>(data: unknown): T[] => {
  if (Array.isArray(data)) return data as T[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; items?: unknown; data?: unknown }
    if (Array.isArray(payload.content)) return payload.content as T[]
    if (Array.isArray(payload.items)) return payload.items as T[]
    if (Array.isArray(payload.data)) return payload.data as T[]
  }
  return []
}

const fetchDashboardData = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const [projectsRes, tasksRes, usersRes, recentRes] = await Promise.all([
      api.get('/projects'),
      api.get('/work-items'),
      api.get('/users'),
      api.get('/work-items?limit=5')
    ])

    const projects = collection<Project>(projectsRes.data)
    const allTasks = collection<WorkItem>(tasksRes.data)
    const users = collection<User>(usersRes.data)
    const recentTasksData = collection<WorkItem>(recentRes.data)

    projectCount.value = projects.length
    // Filter tasks that are not DONE for open tasks count
    taskCount.value = allTasks.filter((task) => task.status !== 'DONE').length
    userCount.value = users.length
    recentTasks.value = recentTasksData

    // Also update project store for active projects
    await projectStore.fetchProjects()
  } catch (err: any) {
    const status = err.response?.status
    errorMessage.value = status === 401 || status === 403
      ? 'Your session is not authorized. Please log in again.'
      : 'We could not load the latest workspace data.'
    console.error('Dashboard fetch error:', err)
  } finally {
    loading.value = false
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

// ===== Lifecycle =====
onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
/* ===== Global Reset ===== */
.dashboard-shell {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
  font-family: system-ui, -apple-system, sans-serif;
}

/* ===== Side Navigation ===== */
.side-nav {
  width: 260px;
  background: #0d252d;
  border-right: 1px solid #0d252d;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  flex-shrink: 0;
  height: 100vh;
  position: sticky;
  top: 0;
}

.bridge-mark {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #2a4a51;
}

.mark-icon {
  background: #5dcce5;
  color: #10232b;
  width: 2rem;
  height: 2rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.nav-label {
  font-size: 0.7rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 1.5rem 0 0.75rem 0.75rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border-radius: 0.5rem;
  color: #a9c2c5;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.15s;
}
.nav-link .nav-icon {
  width: 1.25rem;
  text-align: center;
}
.nav-link.active {
  background: #245360;
  color: #ffffff;
}
.nav-link:not(.active):hover {
  background: #245360;
  color: #ffffff;
}

.side-bottom {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #2a4a51;
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.25rem;
}
.profile-row strong {
  display: block;
  font-size: 0.9rem;
  color: #ffffff;
}
.profile-row small {
  font-size: 0.7rem;
  color: #94a3b8;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  background: #5dcce5;
  color: #10232b;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
}
.small-avatar {
  width: 1.75rem;
  height: 1.75rem;
  font-size: 0.7rem;
}

.logout-button {
  width: 100%;
  margin-top: 0.75rem;
  padding: 0.5rem;
  background: transparent;
  border: 1px solid #375b62;
  border-radius: 0.5rem;
  color: #b9d4d7;
  font-weight: 500;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}
.logout-button:hover {
  background: transparent;
  border-color: #5dcce5;
  color: #ffffff;
}

/* ===== Main Content ===== */
.dashboard-main {
  flex: 1;
  padding: 1.5rem 2rem;
  overflow-y: auto;
}

/* ===== Top Bar ===== */
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 2rem;
}

.date-chip {
  font-size: 0.75rem;
  font-weight: 500;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.topbar h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #111827;
  margin-top: 0.25rem;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.refresh-button {
  padding: 0.4rem 1rem;
  background: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s;
}
.refresh-button:hover:not(:disabled) {
  background: #f3f4f6;
}
.refresh-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.locale-toggle {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
}
.locale-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-weight: 500;
  transition: all 0.15s;
}
.locale-btn.active {
  color: #4f46e5;
  background: #eef2ff;
}
.locale-btn:hover:not(.active) {
  color: #1e293b;
}
.divider {
  color: #d1d5db;
}

/* ===== Stats ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.stat-card {
  background: white;
  border-radius: 0.75rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid #e9edf2;
}
.stat-card span {
  font-size: 0.8rem;
  font-weight: 500;
  color: #94a3b8;
  display: block;
}
.stat-card strong {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  display: block;
  margin: 0.25rem 0;
}
.stat-card em {
  font-size: 0.8rem;
  color: #94a3b8;
  font-style: normal;
}

/* ===== Alert ===== */
.dashboard-alert {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  border-radius: 0.75rem;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.dashboard-alert button {
  background: transparent;
  border: none;
  color: #dc2626;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
}

/* ===== Workspace Grid ===== */
.workspace-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

/* ===== Panels ===== */
.panel {
  background: white;
  border-radius: 0.75rem;
  border: 1px solid #e9edf2;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e9edf2;
  flex-wrap: wrap;
  gap: 0.75rem;
}
.panel-header h2 {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
}
.panel-header p {
  font-size: 0.85rem;
  color: #94a3b8;
  margin: 0.1rem 0 0;
}

.add-button {
  background: #4f46e5;
  color: white;
  padding: 0.3rem 1rem;
  border-radius: 9999px;
  border: none;
  font-size: 0.8rem;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.add-button:hover {
  background: #4338ca;
}

.panel-state {
  padding: 2rem 1.5rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.9rem;
}

/* ===== Task List ===== */
.task-list {
  padding: 0.5rem 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.task-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  background: #fafcfc;
  border: 1px solid #f1f5f9;
}
.task-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.task-dot.pending { background: #fbbf24; }
.task-dot.in-progress { background: #3b82f6; }
.task-dot.review { background: #8b5cf6; }
.task-dot.done { background: #22c55e; }

.task-row .task-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: #111827;
}
.task-row .task-meta {
  font-size: 0.75rem;
  color: #94a3b8;
}
.task-status {
  margin-left: auto;
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.15rem 0.6rem;
  border-radius: 9999px;
  background: #f1f5f9;
  color: #475569;
  text-transform: uppercase;
}
.task-status.pending { background: #fef3c7; color: #92400e; }
.task-status.in-progress { background: #dbeafe; color: #1e40af; }
.task-status.review { background: #ede9fe; color: #5b21b6; }
.task-status.done { background: #dcfce7; color: #166534; }

/* ===== Team Pulse ===== */
.side-panel-body {
  padding: 1rem 1.5rem 1.5rem;
  flex: 1;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.member-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.member-row strong {
  display: block;
  font-size: 0.85rem;
  color: #111827;
}
.member-row small {
  font-size: 0.7rem;
  color: #94a3b8;
}
.member-more {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #94a3b8;
}

.location-stats {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e9edf2;
}
.location-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.3rem 0;
  font-size: 0.85rem;
}
.location-item .flag {
  margin-right: 0.5rem;
}
.location-item .count {
  font-weight: 600;
  color: #111827;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .side-nav {
    display: none;
  }
  .dashboard-main {
    padding: 1rem;
  }
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .topbar {
    flex-direction: column;
    align-items: stretch;
  }
  .topbar h1 {
    font-size: 1.25rem;
  }
}
</style>