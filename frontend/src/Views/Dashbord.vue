<template>
  <main class="dashboard-shell">
    <aside class="side-nav">
      <div class="bridge-mark"><span class="mark-icon">B</span> BridgeOS</div>
      <div class="nav-label">{{ t('dashboard.workspace') }}</div>
      <a class="nav-link active" href="#overview">◈ <span>{{ t('dashboard.overview') }}</span></a>
      <a class="nav-link" href="#tasks">□ <span>{{ t('dashboard.myTasks') }}</span></a>
      <a class="nav-link" href="#projects">⌘ <span>{{ t('dashboard.projects') }}</span></a>
      <a class="nav-link" href="#people">◎ <span>{{ t('dashboard.people') }}</span></a>
      <div class="side-bottom">
        <div class="profile-row"><span class="avatar">{{ initials }}</span><span>{{ displayName }}<small>{{ user?.role || t('dashboard.teamMember') }}</small></span></div>
        <button class="logout-button" type="button" @click="logout">{{ t('dashboard.logout') }}</button>
      </div>
    </aside>
    <section class="dashboard-main">
      <header class="topbar">
        <div><div class="date-chip">{{ t('dashboard.date') }}</div><h1>{{ t('dashboard.greeting', { name: firstName }) }}</h1></div>
        <div class="topbar-actions"><button class="refresh-button" type="button" :disabled="loading" @click="fetchDashboardData">{{ loading ? t('dashboard.loading') : t('dashboard.refresh') }}</button><div class="hello-chip"><button class="locale-inline" type="button" @click="changeLocale('en')">English</button> · <button class="locale-inline" type="button" @click="changeLocale('zh')">中文</button></div></div>
      </header>
      <section class="stat-grid" :aria-label="t('dashboard.statsLabel')">
        <article class="stat-card"><span>{{ t('dashboard.activeProjects') }} / {{ t('dashboard.activeProjectsZh') }}</span><strong>{{ activeProjectCount }}</strong><em>{{ t('dashboard.totalProjects', { count: projectCount }) }}</em></article>
        <article class="stat-card"><span>{{ t('dashboard.openTasks') }} / {{ t('dashboard.openTasksZh') }}</span><strong>{{ taskCount }}</strong><em>{{ t('dashboard.openTaskSummary', { count: taskCount }) }}</em></article>
        <article class="stat-card"><span>{{ t('dashboard.teamMembers') }} / {{ t('dashboard.teamMembersZh') }}</span><strong>{{ userCount }}</strong><em>{{ t('dashboard.teamSummary') }}</em></article>
      </section>
      <div v-if="errorMessage" class="dashboard-alert"><span>{{ errorMessage }}</span><button type="button" @click="fetchDashboardData">{{ t('dashboard.retry') }}</button></div>
      <div class="workspace-grid">
        <section class="panel" id="tasks">
          <header class="panel-header"><div><h2>{{ t('dashboard.queueTitle') }} / {{ t('dashboard.queueZh') }}</h2><p>{{ t('dashboard.queueDescription') }}</p></div><button class="add-button" type="button">{{ t('dashboard.addTask') }}</button></header>
          <div v-if="loading" class="panel-state">{{ t('dashboard.loadingTasks') }}</div>
          <div v-else-if="!taskRows.length" class="panel-state">{{ t('dashboard.noTasks') }}</div>
          <div v-else class="task-list">
            <article v-for="task in taskRows" :key="task.id" class="task-row"><span class="task-dot" :class="task.tone"></span><div><div class="task-title">{{ task.title }}</div><div class="task-meta">{{ task.owner }} · {{ task.context }}</div></div><span class="task-status" :class="task.tone">{{ task.status }}</span></article>
          </div>
        </section>
        <aside class="panel" id="people">
          <header class="panel-header"><div><h2>{{ t('dashboard.pulseTitle') }} / {{ t('dashboard.pulseZh') }}</h2><p>{{ t('dashboard.pulseDescription') }}</p></div></header>
          <div class="side-panel-body">
            <div v-if="loading" class="panel-state">{{ t('dashboard.loadingTeam') }}</div>
            <div v-else-if="!teamMembers.length" class="panel-state">{{ t('dashboard.noMembers') }}</div>
            <div v-else class="member-list"><div v-for="member in visibleMembers" :key="member.id" class="member-row"><span class="avatar small-avatar">{{ memberInitials(member) }}</span><span><strong>{{ member.name || member.email }}</strong><small>{{ member.department?.name || member.role }}</small></span></div><div v-if="teamMembers.length > visibleMembers.length" class="member-more">{{ t('dashboard.moreMembers', { count: teamMembers.length - visibleMembers.length }) }}</div></div>
          </div>
        </aside>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/API/index'
import { useI18n } from 'vue-i18n'
import { setLocale, type Locale } from '@/i18n'
import type { Project, User, WorkItem } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const changeLocale = (nextLocale: Locale): void => {
  setLocale(nextLocale)
}

const user = computed(() => authStore.user)
const projectCount = ref<number>(0)
const taskCount = ref<number>(0)
const userCount = ref<number>(0)
const projects = ref<Project[]>([])
const recentTasks = ref<WorkItem[]>([])
const teamMembers = ref<User[]>([])
const loading = ref<boolean>(false)
const errorMessage = ref<string>('')

const activeProjectCount = computed(() => projects.value.filter((project) => project.status === 'ACTIVE').length)
const visibleMembers = computed(() => teamMembers.value.slice(0, 5))

const taskRows = computed(() => recentTasks.value.map((task) => ({
  id: task.id,
  title: task.title,
  owner: task.assignedTo?.name || t('dashboard.shared'),
  context: task.deadline || task.project?.name || t('dashboard.workspace'),
  status: task.status,
  tone: task.status === 'DONE' ? 'done' : task.status === 'REVIEW' ? 'review' : ''
})))

const displayName = computed(() => {
  const name = user.value?.name?.trim()
  const role = user.value?.role?.trim()

  if (name && name !== role && !/^[A-Z]+(?:_[A-Z]+)+$/.test(name)) {
    return name
  }

  return user.value?.email?.split('@')[0] || t('dashboard.member')
})
const firstName = computed(() => displayName.value.split(' ')[0] || 'there')
const initials = computed(() => displayName.value.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase() || 'BM')
const memberInitials = (member: User): string => (member.name || member.email || 'TM').split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase()

const logout = (): void => {
  authStore.logout()
  router.push('/login')
}

const fetchDashboardData = async (): Promise<void> => {
  loading.value = true
  errorMessage.value = ''
  try {
    const [projectsResponse, tasks, users, recent] = await Promise.all([
      api.get('/projects'),
      api.get('/work-items'),
      api.get('/users'),
      api.get('/work-items?limit=5')
    ])

    const workItems = normalizeCollection<WorkItem>(tasks.data)
    const usersData = normalizeCollection<User>(users.data)
    const recentItems = normalizeCollection<WorkItem>(recent.data)
    const projectsData = normalizeCollection<Project>(projectsResponse.data)

    projects.value = projectsData
    projectCount.value = projectsData.length
    taskCount.value = workItems.filter((task) => ['TODO', 'IN_PROGRESS', 'REVIEW'].includes(task.status)).length
    userCount.value = usersData.length
    recentTasks.value = recentItems.slice(0, 5)
    teamMembers.value = usersData
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
    errorMessage.value = t('dashboard.loadError')
  } finally {
    loading.value = false
  }
}

const normalizeCollection = <T>(data: unknown): T[] => {
  if (Array.isArray(data)) return data as T[]
  if (data && typeof data === 'object') {
    const payload = data as { content?: unknown; data?: unknown; items?: unknown }
    if (Array.isArray(payload.content)) return payload.content as T[]
    if (Array.isArray(payload.data)) return payload.data as T[]
    if (Array.isArray(payload.items)) return payload.items as T[]
  }
  return []
}

onMounted(() => {
  fetchDashboardData()
})
</script>