<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useReportsStore } from '@/stores/reports'
import type { TeamWeeklyReportEntry } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const reportsStore = useReportsStore()

const selectedWeek = ref('')
const showTeamView = ref(false)
const toast = ref('')
const dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

const user = computed(() => authStore.user)
const report = computed(() => reportsStore.currentReport)
const teamReport = computed(() => reportsStore.teamReport)
const isAdmin = computed(() => authStore.isAdmin)

const weekLabel = computed(() => {
  if (!report.value) return ''
  return `${formatDate(report.value.weekStart)} – ${formatDate(report.value.weekEnd)}`
})

const weekOptions = computed(() => {
  const weeks = report.value?.availableWeeks || []
  return weeks.map((weekStart) => ({
    value: weekStart,
    label: `Week of ${formatDate(weekStart)}`
  }))
})

const summaryCards = computed(() => {
  if (!report.value) return []
  const summary = report.value.summary
  return [
    { label: 'Tasks Resolved', value: summary.tasksResolved, suffix: '' },
    { label: 'Tasks Created', value: summary.tasksCreated, suffix: '' },
    { label: 'Avg. Clarity Score', value: summary.avgClarityScore.toFixed(0), suffix: ' / 100' },
    { label: 'Completion Rate', value: summary.completionRate.toFixed(1), suffix: '%' }
  ]
})

const maxResolvedByDay = computed(() =>
  Math.max(...(report.value?.trends.resolvedByDay || [1]), 1)
)

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })

const formatDateTime = (value: string) =>
  new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  })

const showMessage = (message: string) => {
  toast.value = message
  setTimeout(() => {
    toast.value = ''
  }, 3000)
}

const loadReport = async () => {
  await reportsStore.fetchCurrentReport(selectedWeek.value || undefined)
  if (selectedWeek.value === '' && report.value?.weekStart) {
    selectedWeek.value = report.value.weekStart
  }
}

const loadTeamReport = async () => {
  if (!isAdmin.value) return
  await reportsStore.fetchTeamReport(selectedWeek.value || undefined)
}

const handleWeekChange = async () => {
  await loadReport()
  if (showTeamView.value) await loadTeamReport()
}

const refreshReport = async () => {
  const result = await reportsStore.refreshReport(selectedWeek.value || undefined)
  showMessage(result.success ? 'Report refreshed with latest data.' : result.error || 'Refresh failed.')
}

const toggleEmailOptIn = async () => {
  const result = await reportsStore.updateEmailPreference(!reportsStore.emailOptIn)
  showMessage(
    result.success
      ? `Email reports ${reportsStore.emailOptIn ? 'enabled' : 'disabled'}.`
      : result.error || 'Could not update preference.'
  )
}

const sendEmailNow = async () => {
  const result = await reportsStore.sendReportEmail()
  showMessage(result.success ? 'Weekly report email sent.' : result.error || 'Email send failed.')
}

const exportTeamCsv = () => {
  if (!teamReport.value) return
  const rows = [
    ['Name', 'Email', 'Tasks Resolved', 'Tasks Created', 'Avg Clarity', 'Completion Rate', 'Rank'],
    ...teamReport.value.members.map((entry: TeamWeeklyReportEntry) => [
      entry.user.name,
      entry.user.email,
      String(entry.summary.tasksResolved),
      String(entry.summary.tasksCreated),
      String(entry.summary.avgClarityScore),
      String(entry.summary.completionRate),
      String(entry.summary.rankInTeam)
    ])
  ]
  const csv = rows.map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `team-weekly-report-${teamReport.value.weekStart}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const toggleTeamView = async () => {
  showTeamView.value = !showTeamView.value
  if (showTeamView.value) await loadTeamReport()
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(loadReport)
</script>

<template>
  <div class="reports-page">
    <nav class="reports-nav">
      <router-link to="/dashboard" class="brand">
        <span class="mark-icon">B</span>
        <span>BridgeOS</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/analytics">Analytics</router-link>
        <router-link class="current" to="/reports">Weekly Report</router-link>
      </div>
      <div class="user-actions">
        <span>{{ user?.name || user?.email }}</span>
        <button type="button" @click="logout">Log out</button>
      </div>
    </nav>

    <main class="reports-main">
      <header class="page-header">
        <div>
          <span class="eyebrow">Personal insights</span>
          <h1>Weekly Report</h1>
          <p v-if="report">{{ weekLabel }}</p>
        </div>
        <div class="header-actions">
          <select v-model="selectedWeek" @change="handleWeekChange">
            <option v-for="option in weekOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <button type="button" class="secondary-btn" @click="refreshReport">Refresh Report</button>
          <button v-if="isAdmin" type="button" class="secondary-btn" @click="toggleTeamView">
            {{ showTeamView ? 'My Report' : 'Team Reports' }}
          </button>
        </div>
      </header>

      <div v-if="reportsStore.error" class="alert">
        <span>{{ reportsStore.error }}</span>
        <button type="button" @click="loadReport">Try again</button>
      </div>

      <div v-if="reportsStore.loading" class="state">Loading weekly report...</div>

      <template v-else-if="report && !showTeamView">
        <section class="stat-grid">
          <article v-for="card in summaryCards" :key="card.label" class="stat-card">
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}<small>{{ card.suffix }}</small></strong>
          </article>
        </section>

        <div class="content-grid">
          <section class="panel">
            <header><h2>Personal stats</h2><p>Your output compared to what you started</p></header>
            <div class="comparison-row">
              <div class="comparison-card resolved">
                <span>Completed</span>
                <strong>{{ report.summary.tasksResolved }}</strong>
              </div>
              <div class="comparison-card created">
                <span>Created</span>
                <strong>{{ report.summary.tasksCreated }}</strong>
              </div>
              <div class="comparison-card resolution">
                <span>Avg. resolution</span>
                <strong>{{ report.summary.avgResolutionHours.toFixed(1) }}h</strong>
              </div>
            </div>

            <div class="chart-block">
              <h3>Resolved by day</h3>
              <div class="bar-chart">
                <div v-for="(count, index) in report.trends.resolvedByDay" :key="`resolved-${index}`" class="bar-col">
                  <div class="bar resolved" :style="{ height: `${(count / maxResolvedByDay) * 100}%` }" />
                  <span>{{ dayLabels[index] }}</span>
                  <em>{{ count }}</em>
                </div>
              </div>
            </div>

            <div class="chart-block">
              <h3>Clarity score trend</h3>
              <div class="sparkline">
                <div
                  v-for="(score, index) in report.trends.clarityScores"
                  :key="`clarity-${index}`"
                  class="spark-bar"
                  :style="{ height: `${Math.max(score, 4)}%` }"
                  :title="`${dayLabels[index]}: ${score}`"
                />
              </div>
            </div>
          </section>

          <aside class="panel">
            <header><h2>Team context</h2><p>How you compare this week</p></header>
            <div class="team-stat">
              <span>Your rank</span>
              <strong>#{{ report.summary.rankInTeam }}</strong>
            </div>
            <div class="team-stat">
              <span>Your clarity vs team avg</span>
              <strong>{{ report.summary.avgClarityScore.toFixed(0) }} / {{ report.summary.teamAvgClarity.toFixed(0) }}</strong>
            </div>
            <div class="team-stat">
              <span>Your resolved vs team total</span>
              <strong>{{ report.summary.tasksResolved }} / {{ report.summary.totalTeamResolved }}</strong>
            </div>

            <div class="email-prefs">
              <label class="toggle-row">
                <input type="checkbox" :checked="reportsStore.emailOptIn" @change="toggleEmailOptIn" />
                <span>Email me weekly reports</span>
              </label>
              <button type="button" class="secondary-btn full" @click="sendEmailNow">Send report now</button>
            </div>
          </aside>
        </div>

        <section class="panel activity-panel">
          <header><h2>Recent activity</h2><p>What you moved forward this week</p></header>
          <div class="activity-grid">
            <div>
              <h3>Completed tasks</h3>
              <div v-if="!report.recentTasks.length" class="mini-state">No completed tasks this week.</div>
              <article v-for="task in report.recentTasks" :key="task.id" class="activity-row">
                <div>
                  <strong>{{ task.title }}</strong>
                  <small>{{ formatDateTime(task.completedAt) }}</small>
                </div>
                <span class="badge done">{{ task.status }}</span>
              </article>
            </div>
            <div>
              <h3>Created tasks</h3>
              <div v-if="!report.createdTasks?.length" class="mini-state">No tasks created this week.</div>
              <article v-for="task in report.createdTasks || []" :key="`created-${task.id}`" class="activity-row">
                <div>
                  <strong>{{ task.title }}</strong>
                  <small>{{ formatDateTime(task.completedAt) }}</small>
                </div>
                <span class="badge">{{ task.status }}</span>
              </article>
            </div>
            <div>
              <h3>Decisions recorded</h3>
              <div v-if="!report.recentDecisions.length" class="mini-state">No decisions recorded this week.</div>
              <article v-for="decision in report.recentDecisions" :key="decision.id" class="activity-row">
                <div>
                  <strong>{{ decision.summary }}</strong>
                  <small>{{ formatDateTime(decision.createdAt) }}</small>
                </div>
              </article>
            </div>
          </div>
        </section>
      </template>

      <template v-else-if="showTeamView && teamReport">
        <section class="stat-grid">
          <article class="stat-card"><span>Team resolved</span><strong>{{ teamReport.totalTeamResolved }}</strong></article>
          <article class="stat-card"><span>Team avg clarity</span><strong>{{ teamReport.teamAvgClarity.toFixed(0) }}</strong></article>
          <article class="stat-card"><span>Team members</span><strong>{{ teamReport.members.length }}</strong></article>
          <article class="stat-card"><span>Week</span><strong class="week-range">{{ formatDate(teamReport.weekStart) }}</strong></article>
        </section>

        <section class="panel team-panel">
          <header class="team-header">
            <div><h2>Team weekly overview</h2><p>Admin view across all users</p></div>
            <button type="button" class="secondary-btn" @click="exportTeamCsv">Export CSV</button>
          </header>
          <div class="team-table">
            <div class="team-row team-head">
              <span>Rank</span><span>Member</span><span>Resolved</span><span>Created</span><span>Clarity</span><span>Completion</span>
            </div>
            <div v-for="(entry, index) in teamReport.members" :key="entry.user.id" class="team-row">
              <span>#{{ index + 1 }}</span>
              <span><strong>{{ entry.user.name }}</strong><small>{{ entry.user.email }}</small></span>
              <span>{{ entry.summary.tasksResolved }}</span>
              <span>{{ entry.summary.tasksCreated }}</span>
              <span>{{ entry.summary.avgClarityScore.toFixed(0) }}</span>
              <span>{{ entry.summary.completionRate.toFixed(1) }}%</span>
            </div>
          </div>
        </section>
      </template>
    </main>

    <div v-if="toast" class="toast" role="status">{{ toast }}</div>
  </div>
</template>

<style scoped>
.reports-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }
.reports-nav, .reports-main { width: min(100% - 2rem, 1180px); margin: 0 auto; }
.reports-nav { display: flex; align-items: center; gap: 1.5rem; min-height: 70px; border-bottom: 1px solid var(--bridge-line); background: white; padding: 0.85rem 0; }
.brand { display: inline-flex; align-items: center; gap: 0.6rem; color: var(--bridge-ink); font-weight: 800; text-decoration: none; }
.mark-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 9px; background: var(--bridge-cyan); }
.nav-links { display: flex; gap: 1rem; margin-right: auto; font-size: 0.8rem; }
.nav-links a { color: var(--bridge-muted); text-decoration: none; }
.nav-links .current, .nav-links a:hover { color: var(--bridge-deep); }
.user-actions { display: flex; gap: 0.75rem; font-size: 0.8rem; color: var(--bridge-muted); }
.user-actions button { border: 1px solid var(--bridge-line); border-radius: 6px; background: white; padding: 0.35rem 0.7rem; cursor: pointer; }
.reports-main { padding: 2.5rem 0 3rem; }
.page-header { display: flex; justify-content: space-between; gap: 1rem; flex-wrap: wrap; margin-bottom: 1.5rem; }
.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: 0.65rem; letter-spacing: 0.12em; text-transform: uppercase; }
.page-header h1 { margin: 0.4rem 0 0.25rem; font-size: clamp(1.8rem, 4vw, 2.5rem); }
.page-header p { margin: 0; color: var(--bridge-muted); font-size: 0.85rem; }
.header-actions { display: flex; gap: 0.6rem; flex-wrap: wrap; align-items: center; }
.header-actions select, .secondary-btn { border: 1px solid var(--bridge-line); border-radius: 8px; background: white; padding: 0.55rem 0.85rem; font-size: 0.78rem; cursor: pointer; }
.secondary-btn.full { width: 100%; margin-top: 0.75rem; }
.stat-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.75rem; margin-bottom: 1rem; }
.stat-card { border: 1px solid var(--bridge-line); border-radius: 10px; background: white; padding: 1rem; }
.stat-card span { display: block; font-size: 0.68rem; color: var(--bridge-muted); font-weight: 700; }
.stat-card strong { display: block; margin-top: 0.5rem; font-size: 1.6rem; }
.stat-card strong small { font-size: 0.65rem; color: var(--bridge-muted); }
.week-range { font-size: 1rem !important; }
.content-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 1rem; margin-bottom: 1rem; }
.panel { border: 1px solid var(--bridge-line); border-radius: 10px; background: white; overflow: hidden; }
.panel header { padding: 1rem 1.2rem; border-bottom: 1px solid var(--bridge-line); }
.panel header h2 { margin: 0; font-size: 1rem; }
.panel header p { margin: 0.2rem 0 0; font-size: 0.75rem; color: var(--bridge-muted); }
.comparison-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.75rem; padding: 1rem 1.2rem; }
.comparison-card { border: 1px solid var(--bridge-line); border-radius: 8px; padding: 0.85rem; background: #fafcfc; }
.comparison-card span { display: block; font-size: 0.68rem; color: var(--bridge-muted); }
.comparison-card strong { display: block; margin-top: 0.35rem; font-size: 1.2rem; }
.chart-block { padding: 0 1.2rem 1.2rem; }
.chart-block h3 { margin: 0 0 0.75rem; font-size: 0.78rem; color: var(--bridge-deep); text-transform: uppercase; letter-spacing: 0.04em; }
.bar-chart { display: grid; grid-template-columns: repeat(7, 1fr); gap: 0.5rem; align-items: end; height: 140px; }
.bar-col { display: flex; flex-direction: column; align-items: center; justify-content: end; height: 100%; gap: 0.25rem; }
.bar { width: 100%; min-height: 4px; border-radius: 6px 6px 2px 2px; background: var(--bridge-menu); }
.bar-col span, .bar-col em { font-size: 0.62rem; color: var(--bridge-muted); font-style: normal; }
.sparkline { display: grid; grid-template-columns: repeat(7, 1fr); gap: 0.35rem; align-items: end; height: 80px; }
.spark-bar { background: var(--bridge-cyan); border-radius: 4px 4px 0 0; min-height: 4px; }
.team-stat { display: flex; justify-content: space-between; gap: 1rem; padding: 0.85rem 1.2rem; border-bottom: 1px solid #edf3f3; font-size: 0.8rem; }
.team-stat strong { color: var(--bridge-deep); }
.email-prefs { padding: 1rem 1.2rem; }
.toggle-row { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; }
.activity-panel header, .team-panel header { border-bottom: 1px solid var(--bridge-line); }
.activity-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem; padding: 1rem 1.2rem 1.2rem; }
.activity-grid h3 { margin: 0 0 0.75rem; font-size: 0.78rem; color: var(--bridge-deep); }
.activity-row { display: flex; justify-content: space-between; gap: 0.75rem; border: 1px solid var(--bridge-line); border-radius: 8px; padding: 0.75rem; margin-bottom: 0.6rem; }
.activity-row strong, .activity-row small { display: block; }
.activity-row small { margin-top: 0.2rem; color: var(--bridge-muted); font-size: 0.68rem; }
.badge { border-radius: 999px; padding: 0.15rem 0.5rem; font-size: 0.62rem; font-weight: 800; background: #eef2f7; color: var(--bridge-muted); }
.badge.done { background: #ecfdf5; color: #047857; }
.team-header { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.team-table { padding: 1rem 1.2rem 1.2rem; }
.team-row { display: grid; grid-template-columns: 60px 1.4fr repeat(4, 1fr); gap: 0.75rem; align-items: center; padding: 0.75rem 0; border-bottom: 1px solid #edf3f3; font-size: 0.78rem; }
.team-row strong, .team-row small { display: block; }
.team-row small { color: var(--bridge-muted); font-size: 0.68rem; }
.team-head { font-weight: 800; color: var(--bridge-muted); text-transform: uppercase; font-size: 0.65rem; }
.state, .mini-state { padding: 2rem 1rem; text-align: center; color: var(--bridge-muted); font-size: 0.8rem; }
.mini-state { padding: 1rem 0; }
.alert { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1rem; border: 1px solid #f2c6bb; border-radius: 8px; background: #fff3ef; color: #914f42; padding: 0.85rem 1rem; font-size: 0.78rem; }
.toast { position: fixed; right: 1.25rem; bottom: 1.25rem; z-index: 20; border: 1px solid #b8dfd1; border-radius: 7px; padding: 0.8rem 1rem; color: #215b4a; background: #effbf5; box-shadow: 0 8px 24px rgba(25, 71, 78, 0.16); font-size: 0.75rem; }
@media (max-width: 900px) { .content-grid, .activity-grid, .stat-grid { grid-template-columns: 1fr; } .team-row { grid-template-columns: 1fr; } }
</style>
