<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDecisionStore } from '@/stores/decision'
import { useProjectStore } from '@/stores/project'
import DecisionCard from '@/Views/components/DecisionCard.vue'
import DecisionModal from '@/Views/components/DecisionModal.vue'
import type { Decision } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const decisionStore = useDecisionStore()
const projectStore = useProjectStore()

const user = computed(() => authStore.user)
const search = ref('')
const selectedProject = ref<number | null>(null)
const showModal = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | undefined

const groups = computed(() => {
  const grouped = new Map<string, Decision[]>()
  for (const decision of decisionStore.decisions) {
    const date = new Date(decision.createdAt)
    const key = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
    const entries = grouped.get(key) || []
    entries.push(decision)
    grouped.set(key, entries)
  }
  return Array.from(grouped, ([key, decisions]) => {
    const date = new Date(decisions[0]?.createdAt || key)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(today.getDate() - 1)
    const sameDay = (left: Date, right: Date) => left.toDateString() === right.toDateString()
    const label = sameDay(date, today) ? 'Today' : sameDay(date, yesterday) ? 'Yesterday' : date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
    return { date: label, key, decisions }
  })
})

const loadDecisions = async (): Promise<void> => {
  if (selectedProject.value && !search.value.trim()) {
    await decisionStore.fetchByProject(selectedProject.value)
  } else {
    await decisionStore.searchDecisions(search.value, selectedProject.value || undefined)
  }
}

const scheduleSearch = (): void => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { loadDecisions() }, 300)
}

const handleCreate = async (data: { decision: string; context: string; projectId: number }): Promise<void> => {
  const result = await decisionStore.createDecision(data)
  if (result.success) {
    showModal.value = false
  } else {
    decisionStore.error = result.error || 'Failed to create decision'
  }
}

const logout = (): void => {
  authStore.logout()
  router.push('/login')
}

watch(selectedProject, () => { loadDecisions() })
onMounted(async () => {
  await Promise.all([projectStore.fetchProjects(), loadDecisions()])
})
</script>

<template>
  <div class="decision-page">
    <nav class="decision-nav">
      <router-link to="/dashboard" class="brand"><span class="mark-icon">B</span><span>BridgeOS</span></router-link>
      <div class="nav-links">
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/projects">Projects</router-link>
        <router-link to="/tasks">Tasks</router-link>
        <router-link class="current" to="/decisions">Decisions</router-link>
      </div>
      <div class="user-actions"><span>{{ user?.name || user?.email }}</span><button type="button" @click="logout">Log out</button></div>
    </nav>

    <main class="decision-main">
      <header class="page-header">
        <div><span class="eyebrow">Shared memory</span><h1>Decision log</h1><p>Trace the choices that keep every project moving.</p></div>
        <button class="new-button" type="button" @click="showModal = true"><span aria-hidden="true">+</span> New decision</button>
      </header>

      <section class="filters" aria-label="Decision filters">
        <label class="search-field"><span aria-hidden="true">⌕</span><input v-model="search" type="search" placeholder="Search decisions" @input="scheduleSearch"></label>
        <select v-model="selectedProject" aria-label="Filter by project">
          <option :value="null">All projects</option>
          <option v-for="project in projectStore.projects" :key="project.id" :value="project.id">{{ project.name }}</option>
        </select>
      </section>

      <div v-if="decisionStore.error" class="alert"><span>{{ decisionStore.error }}</span><button type="button" @click="loadDecisions">Try again</button></div>
      <div v-if="decisionStore.loading" class="state">Loading decisions...</div>
      <div v-else-if="!groups.length" class="empty-state"><strong>No decisions found</strong><span>Record the reasoning behind your next important choice.</span><button class="new-button" type="button" @click="showModal = true">New decision</button></div>
      <section v-else class="timeline" aria-label="Decision timeline">
        <div v-for="group in groups" :key="group.date" class="timeline-group">
          <h2>{{ group.date }}</h2>
          <DecisionCard v-for="decision in group.decisions" :key="decision.id" :decision="decision" />
        </div>
      </section>
    </main>

    <DecisionModal :is-open="showModal" :projects="projectStore.projects" @close="showModal = false" @submit="handleCreate" />
  </div>
</template>

<style scoped>
.decision-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }
.decision-nav { display: flex; align-items: center; gap: 2rem; min-height: 70px; padding: .85rem clamp(1rem, 5vw, 5rem); border-bottom: 1px solid var(--bridge-line); background: white; }
.brand { display: inline-flex; align-items: center; gap: .6rem; color: var(--bridge-ink); font-size: 1.15rem; font-weight: 800; }
.mark-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 9px; color: var(--bridge-ink); background: var(--bridge-cyan); }
.nav-links { display: flex; gap: 1.25rem; margin-right: auto; color: var(--bridge-muted); font-size: .75rem; }
.nav-links a:hover, .nav-links .current { color: #247184; }
.user-actions { display: flex; align-items: center; gap: 1rem; color: var(--bridge-muted); font-size: .72rem; }
.user-actions button { border: 0; color: #914f42; background: transparent; font-size: inherit; }
.decision-main { width: min(100% - 2rem, 920px); margin: 0 auto; padding: 3.5rem 0; }
.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 1rem; margin-bottom: 2rem; }
.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }
h1 { margin: .45rem 0 .35rem; font-size: clamp(2rem, 5vw, 3rem); letter-spacing: -.06em; }
.page-header p { margin: 0; color: var(--bridge-muted); font-size: .85rem; }
.new-button { display: inline-flex; align-items: center; gap: .45rem; border: 0; border-radius: 7px; padding: .75rem 1rem; color: var(--bridge-ink); background: var(--bridge-cyan); font-size: .75rem; font-weight: 800; white-space: nowrap; }
.new-button span { font-size: 1.1rem; line-height: .8; }
.filters { display: flex; gap: .75rem; margin-bottom: 2rem; }
.search-field { display: flex; align-items: center; flex: 1; gap: .6rem; border: 1px solid var(--bridge-line); border-radius: 7px; padding: .65rem .8rem; color: var(--bridge-muted); background: white; }
.search-field input, select { border: 1px solid var(--bridge-line); border-radius: 7px; padding: .7rem .8rem; color: var(--bridge-ink); background: white; font-size: .75rem; outline: none; }
.search-field input { width: 100%; border: 0; padding: 0; }
.search-field:focus-within, select:focus { border-color: var(--bridge-cyan); box-shadow: 0 0 0 3px rgba(93, 204, 229, .16); }
.alert { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1.5rem; border: 1px solid #f2c6bb; border-radius: 8px; padding: .8rem 1rem; color: #914f42; background: #fff3ef; font-size: .75rem; }
.alert button { border: 0; color: inherit; background: transparent; font-weight: 800; text-decoration: underline; }
.state, .empty-state { display: grid; place-items: center; gap: .7rem; min-height: 220px; color: var(--bridge-muted); font-size: .8rem; text-align: center; }
.empty-state strong { color: var(--bridge-ink); font-size: 1rem; }
.timeline { position: relative; padding-left: 1rem; }
.timeline::before { content: ''; position: absolute; top: .25rem; bottom: 1rem; left: .32rem; width: 1px; background: var(--bridge-line); }
.timeline-group { position: relative; }
.timeline-group > h2 { margin: 0 0 1rem; color: #247184; font-family: 'DM Mono', monospace; font-size: .7rem; letter-spacing: .08em; text-transform: uppercase; }
@media (max-width: 700px) { .decision-nav { flex-wrap: wrap; gap: 1rem; } .nav-links { order: 3; width: 100%; overflow-x: auto; padding-bottom: .2rem; } .user-actions { margin-left: auto; } .page-header { align-items: flex-start; flex-direction: column; } .filters { flex-direction: column; } select { width: 100%; } .decision-main { padding-top: 2rem; } }
</style>
