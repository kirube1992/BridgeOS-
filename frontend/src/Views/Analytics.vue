<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnalyticsStore } from '@/stores/analytics'
import { useProjectStore } from '@/stores/project'
import api from '@/API/index'
import type { Department, ProjectAnalytics, User, WorkItem } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const analytics = useAnalyticsStore()
const projectStore = useProjectStore()
const departments = ref<Department[]>([])
const activePeriod = ref<'week' | 'month' | 'quarter'>(analytics.period)
const activeMode = ref<'team' | 'project'>('team')
const statusCounts = ref<Record<string, number>>({ TODO: 0, IN_PROGRESS: 0, REVIEW: 0, DONE: 0 })
const locationCounts = ref({ Ethiopia: 0, China: 0 })

const user = computed(() => authStore.user)
const summary = computed(() => analytics.summary || { totalResolved: 0, averageResolutionHours: 0, activeUsers: 0, averageClarityScore: 0, departments: departments.value.length })
const stats = computed(() => [
  { label: 'Total resolved', value: summary.value.totalResolved, trend: summary.value.trends?.totalResolved, suffix: '' },
  { label: 'Avg. resolution time', value: summary.value.averageResolutionHours.toFixed(1), trend: summary.value.trends?.averageResolutionHours, suffix: ' hrs' },
  { label: 'Active users', value: summary.value.activeUsers, trend: summary.value.trends?.activeUsers, suffix: '' },
  { label: 'Avg. clarity score', value: summary.value.averageClarityScore.toFixed(0), trend: summary.value.trends?.averageClarityScore, suffix: ' / 100' },
  { label: 'Departments', value: summary.value.departments || departments.value.length, trend: summary.value.trends?.departments, suffix: '' }
])

const loadData = async (): Promise<void> => {
  await Promise.all([
    analytics.fetchSummary(),
    analytics.fetchLeaderboard(activePeriod.value, analytics.departmentId),
    analytics.fetchProjectAnalytics()
  ])
}

const loadTeamSummary = async (): Promise<void> => {
  try {
    const [tasksResponse, usersResponse] = await Promise.all([
      api.get<WorkItem[]>('/work-items'),
      api.get<User[]>('/users')
    ])
    for (const task of tasksResponse.data) {
      statusCounts.value[task.status] = (statusCounts.value[task.status] || 0) + 1
    }
    for (const teamMember of usersResponse.data) {
      const location = `${teamMember.department?.name || ''} ${teamMember.role}`.toLowerCase()
      if (location.includes('ethiopia') || location.includes('ethiopian')) locationCounts.value.Ethiopia++
      if (location.includes('china') || location.includes('chinese')) locationCounts.value.China++
    }
  } catch {
    return
  }
}

const changePeriod = async (period: typeof activePeriod.value): Promise<void> => {
  activePeriod.value = period
  await loadData()
}

const changeMode = (mode: typeof activeMode.value): void => {
  activeMode.value = mode
}

const handleDepartmentChange = async (event: Event): Promise<void> => {
  const value = (event.target as HTMLSelectElement).value
  analytics.departmentId = value ? Number(value) : null
  await analytics.fetchLeaderboard(activePeriod.value, analytics.departmentId)
}

const initials = (name: string): string => name.split(/\s+/).map(part => part[0] || '').join('').slice(0, 2).toUpperCase()
const goToUser = (userId: number): void => { router.push(`/analytics/user/${userId}`) }
const goToProject = (projectId: number): void => { router.push(`/projects/${projectId}`) }
const completionPercent = (project: ProjectAnalytics): number => project.totalTasks ? Math.round((project.completed / project.totalTasks) * 100) : 0
const completionClass = (project: ProjectAnalytics): string => {
  const percentage = completionPercent(project)
  return percentage === 100 ? 'complete' : percentage >= 50 ? 'in-progress' : 'low'
}
const clarityClass = (score: number): string => score >= 80 ? 'strong' : score >= 50 ? 'steady' : 'needs-attention'
const openTasks = (project: ProjectAnalytics): number => project.todo + project.inProgress
const statusTotal = (project: ProjectAnalytics): number => project.totalTasks || 1
const trendLabel = (trend?: number): string => trend === undefined ? 'No prior data' : `${trend >= 0 ? '+' : ''}${trend.toFixed(1)}% vs prior`
const trendClass = (trend?: number): string => trend === undefined ? 'neutral' : trend >= 0 ? 'up' : 'down'

const logout = (): void => {
  authStore.logout()
  router.push('/login')
}

watch(() => analytics.period, value => { activePeriod.value = value })
onMounted(async () => {
  await Promise.all([
    projectStore.fetchProjects(),
    loadData(),
    loadTeamSummary(),
    api.get<Department[]>('/departments').then(response => { departments.value = response.data }).catch(() => undefined)
  ])
})
</script>

<template>
  <div class="analytics-page">
    <nav class="analytics-nav">
      <router-link to="/dashboard" class="brand"><span class="mark-icon">B</span><span>BridgeOS</span></router-link>
      <div class="nav-links"><router-link to="/dashboard">Dashboard</router-link><router-link to="/projects">Projects</router-link><router-link to="/tasks">Tasks</router-link><router-link to="/decisions">Decisions</router-link><router-link to="/people">People & teams</router-link><router-link class="current" to="/analytics">Analytics</router-link><router-link to="/reports">Weekly Report</router-link></div>
      <div class="user-actions"><span>{{ user?.name || user?.email }}</span><button type="button" @click="logout">Log out</button></div>
    </nav>

    <main class="analytics-main">
      <header class="page-header"><div><span class="eyebrow">{{ activeMode === 'team' ? 'Team performance' : 'Project performance' }}</span><h1>{{ activeMode === 'team' ? 'Team analytics' : 'Project analytics' }}</h1><p>{{ activeMode === 'team' ? 'A clear view of the work getting finished.' : 'A clear view of progress across every project.' }}</p><router-link to="/reports" class="reports-link">View your weekly report →</router-link></div><div class="header-controls"><div class="scope-switch" aria-label="Analytics view"><button class="scope-option" :class="{ active: activeMode === 'team' }" type="button" @click="changeMode('team')">Team</button><button class="scope-option" :class="{ active: activeMode === 'project' }" type="button" @click="changeMode('project')">Project</button></div><div class="period-switch"><button v-for="period in ['week', 'month', 'quarter']" :key="period" type="button" :class="{ active: activePeriod === period }" @click="changePeriod(period as typeof activePeriod)">{{ period }}</button></div></div></header>

      <div v-if="analytics.error" class="alert"><span>{{ analytics.error }}</span><button type="button" @click="loadData">Try again</button></div>
      <section class="stat-grid"><article v-for="stat in stats" :key="stat.label" class="stat-card"><span>{{ stat.label }}</span><strong>{{ stat.value }}<small>{{ stat.suffix }}</small></strong><em :class="trendClass(stat.trend)">{{ trendLabel(stat.trend) }}</em></article></section>

      <div v-if="activeMode === 'team'" class="analytics-grid">
        <section class="leaderboard-panel"><header class="section-header"><div><span class="eyebrow">Ranked by output</span><h2>Leaderboard</h2></div><select :value="analytics.departmentId || ''" aria-label="Filter by department" @change="handleDepartmentChange"><option value="">All departments</option><option v-for="department in departments" :key="department.id" :value="department.id">{{ department.name }}</option></select></header><div v-if="analytics.loading" class="state">Loading leaderboard...</div><div v-else-if="!analytics.leaderboard.length" class="state">No performance data for this period.</div><div v-else class="leaderboard"><button v-for="(entry, index) in analytics.leaderboard" :key="entry.user.id" class="leader-row" type="button" @click="goToUser(entry.user.id)"><span class="rank" :class="index < 3 ? `rank-${index + 1}` : ''">{{ index + 1 }}</span><span class="person-avatar">{{ initials(entry.user.name || entry.user.email) }}</span><span class="person"><strong>{{ entry.user.name || entry.user.email }}</strong><small>{{ entry.department?.name || entry.user.department?.name || 'Unassigned' }}</small></span><span class="metric"><strong>{{ entry.itemsResolved }}</strong><small>resolved</small></span><span class="metric"><strong>{{ entry.averageResolutionHours.toFixed(1) }}h</strong><small>avg. time</small></span><span class="metric"><strong>{{ entry.averageClarityScore.toFixed(0) }}</strong><small>clarity</small></span><span class="arrow" aria-hidden="true">→</span></button></div></section>

        <aside class="summary-panel"><header class="section-header"><div><span class="eyebrow">At a glance</span><h2>Team summary</h2></div></header><div class="summary-section"><h3>Workload by location</h3><div class="bar-row"><span>Ethiopia</span><b>{{ locationCounts.Ethiopia }}</b></div><div class="bar-track"><i :style="{ width: `${Math.min(100, locationCounts.Ethiopia * 10)}%` }"></i></div><div class="bar-row"><span>China</span><b>{{ locationCounts.China }}</b></div><div class="bar-track"><i :style="{ width: `${Math.min(100, locationCounts.China * 10)}%` }"></i></div></div><div class="summary-section"><h3>Status distribution</h3><div v-for="(count, status) in statusCounts" :key="status" class="status-row"><span>{{ status }}</span><b>{{ count }}</b></div></div><div class="summary-section"><h3>Department breakdown</h3><div v-for="department in departments" :key="department.id" class="status-row"><span>{{ department.name }}</span><b>{{ analytics.leaderboard.filter(entry => (entry.department?.id || entry.user.department?.id) === department.id).length }}</b></div><p v-if="!departments.length" class="muted">No departments available.</p></div></aside>
      </div>

      <section v-else id="project-analytics" class="projects-analytics">
        <header class="section-header"><div><span class="eyebrow">Portfolio view</span><h2>Project analytics</h2></div><span class="period-note">{{ activePeriod }} view</span></header>
        <div v-if="analytics.loading && !analytics.projectAnalytics.length" class="state">Loading project analytics...</div>
        <div v-else-if="!analytics.projectAnalytics.length" class="state">No project analytics available.</div>
        <div v-else class="project-grid">
          <button v-for="project in analytics.projectAnalytics" :key="project.projectId" class="project-card" type="button" @click="goToProject(project.projectId)">
            <span class="project-card-header"><strong>{{ project.projectName }}</strong><span class="project-arrow" aria-hidden="true">→</span></span>
            <span class="project-progress-label"><span>{{ completionPercent(project) }}% complete</span><span>{{ project.completed }}/{{ project.totalTasks }} tasks</span></span>
            <span class="progress-track"><i :class="completionClass(project)" :style="{ width: `${completionPercent(project)}%` }"></i></span>
            <span class="project-metrics"><span><small>Avg. clarity</small><b :class="clarityClass(project.avgClarity)">{{ project.avgClarity.toFixed(0) }}<em>/100</em></b></span><span><small>Open tasks</small><b>{{ openTasks(project) }}</b></span><span><small>Resolution</small><b>{{ project.resolutionDays.toFixed(1) }}<em>d</em></b></span></span>
            <span class="status-bar" aria-label="Task status distribution"><i class="todo" :style="{ width: `${project.todo / statusTotal(project) * 100}%` }"></i><i class="in-progress" :style="{ width: `${project.inProgress / statusTotal(project) * 100}%` }"></i><i class="review" :style="{ width: `${project.review / statusTotal(project) * 100}%` }"></i><i class="done" :style="{ width: `${project.completed / statusTotal(project) * 100}%` }"></i></span>
            <span class="status-legend"><span><i class="todo"></i>TODO {{ project.todo }}</span><span><i class="in-progress"></i>IN PROGRESS {{ project.inProgress }}</span><span><i class="review"></i>REVIEW {{ project.review }}</span><span><i class="done"></i>DONE {{ project.completed }}</span></span>
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.analytics-page .mark-icon { color: white; background: #e76f51; }
.analytics-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }
.analytics-nav { display: flex; align-items: center; gap: 2rem; min-height: 70px; padding: .85rem clamp(1rem, 5vw, 5rem); border-bottom: 1px solid var(--bridge-line); background: white; }
.brand { display: inline-flex; align-items: center; gap: .6rem; color: var(--bridge-ink); font-size: 1.15rem; font-weight: 800; }.mark-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 9px; color: var(--bridge-ink); background: var(--bridge-cyan); }.nav-links { display: flex; gap: 1.1rem; margin-right: auto; color: var(--bridge-muted); font-size: .72rem; }.nav-links a:hover, .nav-links .current { color: #247184; }.user-actions { display: flex; gap: 1rem; color: var(--bridge-muted); font-size: .72rem; }.user-actions button { border: 0; color: #914f42; background: transparent; font-size: inherit; }
.analytics-main { width: min(100% - 2rem, 1180px); margin: auto; padding: 3rem 0; }.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 1rem; margin-bottom: 2rem; }.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: .45rem 0 .35rem; font-size: clamp(2rem, 5vw, 3rem); letter-spacing: -.06em; }.page-header p { margin: 0; color: var(--bridge-muted); font-size: .85rem; }.reports-link { display: inline-block; margin-top: .65rem; color: #247184; font-size: .75rem; font-weight: 700; text-decoration: none; }.header-controls { display: grid; gap: .55rem; justify-items: end; }.scope-switch, .period-switch { display: flex; border: 1px solid var(--bridge-line); border-radius: 7px; padding: .2rem; background: white; }.scope-option { border-radius: 5px; padding: .5rem .8rem; color: var(--bridge-muted); font-size: .72rem; }.scope-option:hover, .scope-option.active { color: var(--bridge-ink); background: var(--bridge-cyan-soft); }.period-switch button { border: 0; border-radius: 5px; padding: .5rem .8rem; color: var(--bridge-muted); background: transparent; font-size: .72rem; text-transform: capitalize; }.period-switch button.active { color: var(--bridge-ink); background: var(--bridge-cyan); font-weight: 800; }
.stat-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: .75rem; margin-bottom: 1.5rem; }.stat-card { border: 1px solid var(--bridge-line); border-radius: 10px; padding: 1rem; background: white; }.stat-card > span { color: var(--bridge-muted); font-size: .68rem; font-weight: 700; }.stat-card strong { display: block; margin: .65rem 0 .3rem; color: var(--bridge-ink); font-size: 1.65rem; letter-spacing: -.05em; }.stat-card strong small { margin-left: .15rem; color: var(--bridge-muted); font-size: .65rem; letter-spacing: 0; }.stat-card em { color: var(--bridge-muted); font-size: .65rem; font-style: normal; }.stat-card em.up { color: #31856c; }.stat-card em.down { color: #b05b4a; }
.analytics-grid { display: grid; grid-template-columns: minmax(0, 1.7fr) minmax(260px, .8fr); gap: 1rem; }.leaderboard-panel, .summary-panel { border: 1px solid var(--bridge-line); border-radius: 10px; background: white; }.section-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; border-bottom: 1px solid var(--bridge-line); padding: 1.2rem 1.25rem; }.section-header h2 { margin: .35rem 0 0; font-size: 1.05rem; }.section-header select { border: 1px solid var(--bridge-line); border-radius: 6px; padding: .5rem; color: var(--bridge-deep); background: white; font-size: .7rem; }.leaderboard { display: grid; }.leader-row { display: grid; grid-template-columns: 28px 34px minmax(120px, 1fr) repeat(3, 70px) 18px; gap: .65rem; align-items: center; border: 0; border-bottom: 1px solid #edf3f3; padding: .85rem 1.25rem; text-align: left; background: white; }.leader-row:hover { background: #f7fbfb; }.rank { color: var(--bridge-muted); font-family: 'DM Mono', monospace; font-size: .72rem; text-align: center; }.rank-1 { color: #b8860b; font-weight: 800; }.rank-2 { color: #718096; font-weight: 800; }.rank-3 { color: #a8623c; font-weight: 800; }.person-avatar { display: grid; place-items: center; width: 32px; height: 32px; border-radius: 50%; color: var(--bridge-ink); background: var(--bridge-cyan); font-size: .62rem; font-weight: 800; }.person { min-width: 0; }.person strong, .person small, .metric strong, .metric small { display: block; }.person strong { overflow: hidden; color: var(--bridge-ink); font-size: .75rem; text-overflow: ellipsis; white-space: nowrap; }.person small, .metric small { margin-top: .2rem; color: var(--bridge-muted); font-size: .62rem; }.metric { text-align: right; }.metric strong { color: var(--bridge-deep); font-size: .75rem; }.arrow { color: #247184; font-size: 1rem; }.state { padding: 3rem 1rem; color: var(--bridge-muted); font-size: .78rem; text-align: center; }.summary-section { padding: 1rem 1.25rem; border-bottom: 1px solid #edf3f3; }.summary-section:last-child { border: 0; }.summary-section h3 { margin: 0 0 .75rem; color: var(--bridge-deep); font-size: .7rem; text-transform: uppercase; letter-spacing: .04em; }.bar-row, .status-row { display: flex; justify-content: space-between; margin-top: .55rem; color: var(--bridge-muted); font-size: .7rem; }.bar-row b, .status-row b { color: var(--bridge-ink); }.bar-track { height: 5px; margin-top: .35rem; border-radius: 4px; background: #e5eeee; }.bar-track i { display: block; height: 100%; border-radius: inherit; background: var(--bridge-cyan); }.muted { color: var(--bridge-muted); font-size: .7rem; }.alert { display: flex; justify-content: space-between; margin-bottom: 1rem; border: 1px solid #f2c6bb; border-radius: 8px; padding: .8rem 1rem; color: #914f42; background: #fff3ef; font-size: .75rem; }.alert button { border: 0; color: inherit; background: transparent; font-weight: 800; text-decoration: underline; }
 .projects-analytics { margin-top: 1rem; border: 1px solid var(--bridge-line); border-radius: 10px; background: white; }.period-note { color: var(--bridge-muted); font-size: .68rem; text-transform: capitalize; }.project-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: .75rem; padding: 1rem; }.project-card { display: grid; gap: .75rem; border: 1px solid var(--bridge-line); border-radius: 8px; padding: 1rem; color: var(--bridge-ink); text-align: left; background: #fbfdfd; cursor: pointer; }.project-card:hover, .project-card:focus-visible { border-color: var(--bridge-cyan); background: white; box-shadow: 0 5px 16px rgba(16, 35, 43, .06); outline: none; }.project-card-header, .project-progress-label, .project-metrics { display: flex; justify-content: space-between; gap: .75rem; }.project-card-header strong { overflow: hidden; font-size: .85rem; text-overflow: ellipsis; white-space: nowrap; }.project-arrow { color: #247184; font-size: 1rem; }.project-progress-label { color: var(--bridge-muted); font-size: .65rem; }.progress-track, .status-bar { display: flex; width: 100%; height: 7px; border-radius: 99px; background: #edf3f3; overflow: hidden; }.progress-track i, .status-bar i { display: block; height: 100%; }.progress-track i.complete, .status-bar i.done { background: #42b883; }.progress-track i.in-progress, .status-bar i.review { background: #e3b341; }.progress-track i.low, .status-bar i.todo { background: #d87868; }.status-bar i.in-progress { background: #5b9bea; }.project-metrics { align-items: flex-end; }.project-metrics span { min-width: 0; }.project-metrics small { display: block; color: var(--bridge-muted); font-size: .62rem; }.project-metrics b { display: block; margin-top: .25rem; color: var(--bridge-deep); font-size: .9rem; }.project-metrics b.strong { color: #31856c; }.project-metrics b.steady { color: #b8860b; }.project-metrics b.needs-attention { color: #b05b4a; }.project-metrics em { margin-left: .1rem; color: var(--bridge-muted); font-size: .6rem; font-style: normal; }.status-legend { display: flex; flex-wrap: wrap; gap: .45rem .7rem; color: var(--bridge-muted); font-size: .58rem; }.status-legend span { display: inline-flex; align-items: center; gap: .25rem; }.status-legend i { width: 6px; height: 6px; border-radius: 50%; }.status-legend i.todo { background: #d87868; }.status-legend i.in-progress { background: #5b9bea; }.status-legend i.review { background: #e3b341; }.status-legend i.done { background: #42b883; }
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(3, 1fr); }.analytics-grid { grid-template-columns: 1fr; } }
@media (max-width: 700px) { .analytics-nav { flex-wrap: wrap; gap: 1rem; }.nav-links { order: 3; width: 100%; overflow-x: auto; }.user-actions { margin-left: auto; }.page-header { align-items: flex-start; flex-direction: column; }.header-controls { width: 100%; justify-items: stretch; }.scope-switch, .period-switch { justify-content: center; }.stat-grid { grid-template-columns: repeat(2, 1fr); }.leader-row { grid-template-columns: 25px 32px minmax(100px, 1fr) 55px 18px; }.leader-row .metric:nth-of-type(2), .leader-row .metric:nth-of-type(3) { display: none; }.project-grid { grid-template-columns: 1fr; }}
</style>
