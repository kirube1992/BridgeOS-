<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import api from '@/API/index'
import { useAuthStore } from '@/stores/auth'
import { useAiStore } from '@/stores/ai'
import RequirementTranslator from '@/Views/components/RequirementTranslator.vue'
import MeetingActionExtractor from '@/Views/components/MeetingActionExtractor.vue'
import AIAssistant from '@/Views/components/AIAssistant.vue'
import type { Project, User, WorkItem, Decision } from '@/types'

const authStore = useAuthStore()
const ai = useAiStore()

const user = computed(() => authStore.user)
const displayName = computed(() => user.value?.name?.trim() || user.value?.email?.split('@')[0] || 'Member')
const initials = computed(() => {
  const parts = displayName.value.split(' ')
  if (parts.length >= 2) return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase()
  return displayName.value.slice(0, 2).toUpperCase()
})

const projects = ref<Project[]>([])
const users = ref<User[]>([])
const selectedProjectId = ref<number | null>(null)
const loading = ref(true)
const healthCheckDone = ref(false)

const selectedProject = computed(() =>
  projects.value.find(p => p.id === selectedProjectId.value) || null
)

const projectWorkItems = ref<WorkItem[]>([])
const projectDecisions = ref<Decision[]>([])

const activeTab = ref<'assistant' | 'translator' | 'extractor'>('assistant')

const fetchData = async () => {
  loading.value = true
  try {
    const [projectsResp, usersResp] = await Promise.all([
      api.get<Project[]>('/projects'),
      api.get<User[]>('/users')
    ])
    projects.value = Array.isArray(projectsResp.data) ? projectsResp.data : []
    users.value = Array.isArray(usersResp.data) ? usersResp.data : []
    if (projects.value.length && selectedProjectId.value == null) {
      selectedProjectId.value = projects.value[0]!.id
    }
    ai.checkHealth()
  } finally {
    loading.value = false
    healthCheckDone.value = true
  }
}

const loadProjectContext = async () => {
  if (!selectedProjectId.value) {
    projectWorkItems.value = []
    projectDecisions.value = []
    return
  }
  try {
    const [itemsResp, auditResp] = await Promise.all([
      api.get<WorkItem[]>(`/work-items/project/${selectedProjectId.value}`),
      api.get<Decision[]>(`/audit/project/${selectedProjectId.value}`).catch(() => ({ data: [] }))
    ])
    projectWorkItems.value = Array.isArray(itemsResp.data) ? itemsResp.data : []
    projectDecisions.value = Array.isArray(auditResp.data) ? auditResp.data : []
  } catch {
    projectWorkItems.value = []
    projectDecisions.value = []
  }
}

onMounted(fetchData)

let contextLoadedFor: number | null = null
const onProjectChange = () => {
  if (selectedProjectId.value !== contextLoadedFor) {
    ai.clearChat()
    loadProjectContext()
    contextLoadedFor = selectedProjectId.value
  }
}
</script>

<template>
  <div class="dashboard-shell">
    <aside class="side-nav">
      <router-link to="/dashboard" class="bridge-mark">
        <span class="mark-icon">B</span>
        <span>BridgeOS</span>
      </router-link>

      <div class="nav-label">WORKSPACE</div>
      <router-link to="/dashboard" class="nav-link"><span class="nav-icon">◈</span><span>Overview</span></router-link>
      <router-link to="/tasks" class="nav-link"><span class="nav-icon">□</span><span>My Tasks</span></router-link>
      <router-link to="/projects" class="nav-link"><span class="nav-icon">⌘</span><span>Projects</span></router-link>
      <router-link to="/decisions" class="nav-link"><span class="nav-icon">◌</span><span>Decisions</span></router-link>
      <router-link to="/analytics" class="nav-link"><span class="nav-icon">▥</span><span>Analytics</span></router-link>
      <router-link to="/reports" class="nav-link"><span class="nav-icon">▤</span><span>Weekly Report</span></router-link>
      <router-link to="/people" class="nav-link"><span class="nav-icon">◎</span><span>People & Teams</span></router-link>
      <router-link to="/ai" class="nav-link active"><span class="nav-icon">✦</span><span>AI Tools</span></router-link>

      <div class="side-bottom">
        <router-link :to="`/profile/${user?.id || ''}`" class="profile-row">
          <span class="avatar">{{ initials }}</span>
          <div>
            <strong>{{ displayName }}</strong>
            <small>{{ user?.role || 'Team Member' }}</small>
          </div>
        </router-link>
        <button class="logout-button" type="button" @click="authStore.logout">Log out</button>
      </div>
    </aside>

    <section class="dashboard-main ai-page">
      <header class="topbar">
        <div>
          <div class="hello-chip">
            <span v-if="healthCheckDone && ai.health?.sidecarReachable" class="ok-dot"></span>
            <span v-else-if="healthCheckDone" class="bad-dot"></span>
            AI tools
            <small v-if="healthCheckDone">
              · {{ ai.health?.provider || 'mock' }}{{ ai.health?.model ? ` · ${ai.health.model}` : '' }}
            </small>
          </div>
          <h1>Smart assistance, built into BridgeOS.</h1>
        </div>
        <div class="topbar-actions">
          <label class="project-picker">
            <span>Scope to project</span>
            <select v-model="selectedProjectId" @change="onProjectChange" :disabled="loading">
              <option :value="null">All projects</option>
              <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </label>
        </div>
      </header>

      <nav class="ai-tabs" role="tablist" aria-label="AI tools">
        <button
          role="tab"
          :aria-selected="activeTab === 'assistant'"
          :class="{ active: activeTab === 'assistant' }"
          type="button"
          @click="activeTab = 'assistant'"
        >
          <span class="tab-icon">💬</span>
          <span>Assistant</span>
          <small>Ask questions, cite sources</small>
        </button>
        <button
          role="tab"
          :aria-selected="activeTab === 'translator'"
          :class="{ active: activeTab === 'translator' }"
          type="button"
          @click="activeTab = 'translator'"
        >
          <span class="tab-icon">📋</span>
          <span>Requirement translator</span>
          <small>Vague → structured spec</small>
        </button>
        <button
          role="tab"
          :aria-selected="activeTab === 'extractor'"
          :class="{ active: activeTab === 'extractor' }"
          type="button"
          @click="activeTab = 'extractor'"
        >
          <span class="tab-icon">📝</span>
          <span>Meeting extractor</span>
          <small>Notes → action items</small>
        </button>
      </nav>

      <div v-if="loading" class="state">Loading AI tools…</div>
      <template v-else>
        <section v-show="activeTab === 'assistant'" class="tab-panel">
          <AIAssistant
            :project="selectedProject"
            :work-items="projectWorkItems"
            :decisions="projectDecisions"
          />
        </section>

        <section v-show="activeTab === 'translator'" class="tab-panel">
          <RequirementTranslator :project="selectedProject" />
        </section>

        <section v-show="activeTab === 'extractor'" class="tab-panel">
          <MeetingActionExtractor
            :project="selectedProject"
            :users="users"
            :current-user-id="user?.id"
          />
        </section>
      </template>
    </section>
  </div>
</template>

<style scoped>
.ai-page .topbar h1 { font-size: clamp(1.5rem, 4vw, 2.2rem); margin: .5rem 0 0; letter-spacing: -.02em; }
.ai-page .hello-chip { display: inline-flex; align-items: center; gap: .5rem; padding: .55rem 1rem; }
.ai-page .hello-chip small { color: #286b78; opacity: .8; }
.ok-dot, .bad-dot { width: 8px; height: 8px; border-radius: 50%; }
.ok-dot { background: #4aaa74; box-shadow: 0 0 0 3px rgba(74, 170, 116, .18); }
.bad-dot { background: var(--bridge-coral); }

.project-picker { display: grid; gap: .35rem; font-size: .7rem; font-weight: 700; color: var(--bridge-deep); }
.project-picker select {
  min-width: 220px;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  padding: .5rem .65rem;
  color: var(--bridge-ink);
  background: white;
  outline: none;
  font-size: .8rem;
}
.project-picker select:focus { border-color: var(--bridge-cyan); box-shadow: 0 0 0 3px rgba(93, 204, 229, .18); }

.ai-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: .85rem;
  margin: 1.5rem 0 .5rem;
}
.ai-tabs button {
  text-align: left;
  border: 1px solid var(--bridge-line);
  background: white;
  border-radius: 12px;
  padding: 1rem 1.1rem;
  cursor: pointer;
  transition: border-color .2s, box-shadow .2s, transform .15s;
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: auto auto;
  column-gap: .75rem;
  row-gap: .2rem;
  align-items: center;
}
.ai-tabs button span { font-weight: 800; color: var(--bridge-deep); font-size: .85rem; }
.ai-tabs button small { grid-column: 2; color: var(--bridge-muted); font-size: .7rem; font-weight: 500; }
.tab-icon { font-size: 1.3rem; grid-row: span 2; justify-self: center; }
.ai-tabs button:hover:not(.active) { border-color: var(--bridge-cyan); transform: translateY(-1px); box-shadow: 0 6px 18px rgba(93, 204, 229, .12); }
.ai-tabs button.active {
  border-color: var(--bridge-menu);
  background: linear-gradient(180deg, #eefbfe, white);
  box-shadow: 0 8px 24px rgba(36, 83, 96, .12);
}
.ai-tabs button.active span { color: var(--bridge-menu-dark); }

.tab-panel { margin-top: 1.2rem; }
.state { padding: 2rem; text-align: center; color: var(--bridge-muted); }

@media (max-width: 860px) {
  .ai-tabs { grid-template-columns: 1fr; }
}
</style>
