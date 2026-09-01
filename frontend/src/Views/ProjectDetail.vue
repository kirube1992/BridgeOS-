<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/API/index'
import { useAuthStore } from '@/stores/auth'
import type { Project, WorkItem, User, Decision } from '@/types'
import ProjectChat from '@/Views/components/ProjectChat.vue'
import RequirementTranslator from '@/Views/components/RequirementTranslator.vue'
import MeetingActionExtractor from '@/Views/components/MeetingActionExtractor.vue'
import AIAssistant from '@/Views/components/AIAssistant.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const project = ref<Project | null>(null)
const tasks = ref<WorkItem[]>([])
const decisions = ref<Decision[]>([])
const users = ref<User[]>([])
const loading = ref(true)
const error = ref('')

const currentUserId = computed(() => authStore.user?.id)

const loadProject = async (): Promise<void> => {
  try {
    const projectId = Number(route.params.id)
    const [projectResponse, tasksResponse, usersResponse, auditResponse] = await Promise.all([
      api.get<Project>(`/projects/${projectId}`),
      api.get<WorkItem[]>(`/work-items/project/${projectId}`),
      api.get<User[]>('/users'),
      api.get<Decision[]>(`/audit/project/${projectId}`).catch(() => ({ data: [] as Decision[] }))
    ])
    project.value = projectResponse.data
    tasks.value = tasksResponse.data
    users.value = Array.isArray(usersResponse.data) ? usersResponse.data : []
    decisions.value = Array.isArray(auditResponse.data) ? auditResponse.data : []
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load project'
  } finally {
    loading.value = false
  }
}

const formatDate = (value: string | null): string => value ? new Date(value).toLocaleDateString() : 'Not set'

const aiSection = ref<'assistant' | 'translator' | 'extractor' | null>('assistant')

onMounted(loadProject)
</script>

<template>
  <div class="project-detail-page">
    <nav class="project-nav"><router-link to="/dashboard" class="brand"><span class="mark-icon">B</span><span>BridgeOS</span></router-link><router-link to="/projects" class="back-link">← Projects</router-link></nav>
    <main class="project-detail-main">
      <div v-if="loading" class="state">Loading project...</div>
      <div v-else-if="error" class="state error">{{ error }}</div>
      <template v-else-if="project">
        <div class="project-layout-grid">
          <!-- Info / Metadata column -->
          <div class="project-left-col">
            <header class="project-header"><span class="eyebrow">Project detail</span><h1>{{ project.name }}</h1><p>{{ project.description || 'No description available.' }}</p><span class="project-status">{{ project.status }}</span></header>
            <section class="project-info"><div><small>Client context</small><p>{{ project.clientContext || 'No client context available.' }}</p></div><div><small>Deadline</small><p>{{ formatDate(project.deadline) }}</p></div></section>
            <section class="tasks-section"><h2>Tasks</h2><div v-if="!tasks.length" class="state">No tasks in this project.</div><div v-else class="task-list"><router-link v-for="task in tasks" :key="task.id" :to="`/tasks/${task.id}`" class="task-row"><div><strong>{{ task.title }}</strong><small>{{ task.status }} · Clarity {{ task.clarityScore || 0 }}/100</small></div><span>{{ task.assignedTo?.name || 'Unassigned' }}</span></router-link></div></section>

            <!-- AI tools collapsible -->
            <section class="ai-section">
              <div class="ai-section-header">
                <div>
                  <span class="eyebrow">AI tools</span>
                  <h2>Smart help for this project</h2>
                </div>
                <div class="ai-chips">
                  <button
                    :class="['chip', { active: aiSection === 'assistant' }]"
                    type="button"
                    @click="aiSection = aiSection === 'assistant' ? null : 'assistant'"
                  >💬 Assistant</button>
                  <button
                    :class="['chip', { active: aiSection === 'translator' }]"
                    type="button"
                    @click="aiSection = aiSection === 'translator' ? null : 'translator'"
                  >📋 Spec translator</button>
                  <button
                    :class="['chip', { active: aiSection === 'extractor' }]"
                    type="button"
                    @click="aiSection = aiSection === 'extractor' ? null : 'extractor'"
                  >📝 Meeting extractor</button>
                  <router-link to="/ai" class="chip ghost">Open all →</router-link>
                </div>
              </div>

              <div v-if="aiSection === 'assistant'" class="ai-panel">
                <AIAssistant
                  compact
                  :project="project"
                  :work-items="tasks"
                  :decisions="decisions"
                />
              </div>
              <div v-if="aiSection === 'translator'" class="ai-panel">
                <RequirementTranslator compact :project="project" />
              </div>
              <div v-if="aiSection === 'extractor'" class="ai-panel">
                <MeetingActionExtractor
                  compact
                  :project="project"
                  :users="users"
                  :current-user-id="currentUserId"
                />
              </div>
            </section>
          </div>

          <!-- Chat panel column -->
          <div class="project-right-col">
            <ProjectChat :project-id="project.id" />
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.project-detail-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }
.project-nav { display: flex; align-items: center; gap: 2rem; min-height: 70px; padding: .85rem clamp(1rem, 5vw, 5rem); border-bottom: 1px solid var(--bridge-line); background: white; }
.brand { display: inline-flex; align-items: center; gap: .6rem; color: var(--bridge-ink); font-size: 1.15rem; font-weight: 800; }
.mark-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 9px; background: var(--bridge-cyan); }
.back-link { margin-left: auto; color: var(--bridge-muted); font-size: .75rem; }
.project-detail-main { width: min(100% - 2rem, 1320px); margin: auto; padding: 2rem 0 4rem; }
.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }
.project-header { border-bottom: 1px solid var(--bridge-line); padding-bottom: 1.5rem; }
.project-header h1 { margin: .45rem 0 .5rem; font-size: clamp(2rem, 5vw, 3rem); letter-spacing: -.05em; }
.project-header p { margin: 0 0 1rem; color: var(--bridge-muted); font-size: .85rem; }
.project-status { display: inline-block; border-radius: 5px; padding: .35rem .55rem; color: #286b78; background: var(--bridge-cyan-soft); font-size: .68rem; font-weight: 800; }
.project-info { display: grid; grid-template-columns: 2fr 1fr; gap: 1rem; padding: 1.5rem 0; }
.project-info, .tasks-section, .ai-section { border-bottom: 1px solid var(--bridge-line); }
.project-info small, .task-row small { color: var(--bridge-muted); font-size: .68rem; }
.project-info p { margin: .4rem 0 0; font-size: .8rem; line-height: 1.5; }
.tasks-section { padding: 1.5rem 0; }
.tasks-section h2 { margin: 0 0 1rem; font-size: 1.1rem; }
.task-list { display: grid; gap: .6rem; }
.task-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  padding: .9rem 1rem;
  background: white;
  color: inherit;
  text-decoration: none;
  transition: border-color .15s, transform .15s;
}
.task-row:hover { border-color: var(--bridge-cyan); transform: translateY(-1px); }
.task-row strong, .task-row small { display: block; }
.task-row small { margin-top: .3rem; }
.task-row > span { color: var(--bridge-muted); font-size: .7rem; }
.state { padding: 3rem 1rem; color: var(--bridge-muted); text-align: center; }
.error { color: #914f42; }

.ai-section { padding: 1.5rem 0; }
.ai-section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}
.ai-section-header h2 { margin: .45rem 0 0; font-size: 1.2rem; }
.ai-chips { display: flex; flex-wrap: wrap; gap: .4rem; }
.chip {
  border: 1px solid var(--bridge-line);
  background: white;
  border-radius: 999px;
  padding: .35rem .75rem;
  font-size: .72rem;
  font-weight: 700;
  color: var(--bridge-deep);
  cursor: pointer;
  transition: all .15s;
  text-decoration: none;
}
.chip:hover:not(.ghost) { border-color: var(--bridge-cyan); background: var(--bridge-cyan-soft); color: #247184; }
.chip.active { background: var(--bridge-menu); color: white; border-color: var(--bridge-menu); }
.chip.ghost { color: var(--bridge-muted); background: transparent; }
.chip.ghost:hover { color: var(--bridge-menu); }
.ai-panel { margin-top: 1rem; }

/* Grid layout rules */
.project-layout-grid {
  display: grid;
  grid-template-columns: 1.35fr 1fr;
  gap: 2.5rem;
  align-items: start;
}

@media (max-width: 1024px) {
  .project-layout-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
}
@media (max-width: 600px) { .project-info { grid-template-columns: 1fr; }.task-row { flex-direction: column; } }
</style>
