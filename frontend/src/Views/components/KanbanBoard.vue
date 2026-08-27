<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/stores/task'
import type { WorkItem } from '@/types'

type TaskStatus = WorkItem['status']

type Column = {
  status: TaskStatus
  title: string
  accent: string
}

const columns: Column[] = [
  { status: 'TODO', title: 'Todo', accent: 'coral' },
  { status: 'IN_PROGRESS', title: 'In progress', accent: 'blue' },
  { status: 'REVIEW', title: 'Review', accent: 'gold' },
  { status: 'DONE', title: 'Done', accent: 'green' }
]

const router = useRouter()
const taskStore = useTaskStore()
const draggedTask = ref<WorkItem | null>(null)
const activeDropStatus = ref<TaskStatus | null>(null)
const updatingTaskIds = ref(new Set<number>())
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)
let toastTimer: ReturnType<typeof setTimeout> | undefined

const tasksByStatus = computed(() => {
  return columns.reduce<Record<TaskStatus, WorkItem[]>>((groups, column) => {
    groups[column.status] = taskStore.tasks.filter(task => task.status === column.status)
    return groups
  }, { TODO: [], IN_PROGRESS: [], REVIEW: [], DONE: [] })
})

const showToast = (message: string, type: 'success' | 'error'): void => {
  toast.value = { message, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = null }, 3200)
}

const initials = (assignee: WorkItem['assignedTo']): string => {
  const name = assignee?.name || assignee?.email || 'Unassigned'
  return name.split(/\s+/).map(part => part[0] || '').join('').slice(0, 2).toUpperCase()
}

const priorityClass = (priority: WorkItem['priority']): string => `priority-${priority.toLowerCase()}`
const clarityClass = (score: number): string => score >= 80 ? 'clarity-strong' : score >= 60 ? 'clarity-steady' : 'clarity-needs-attention'
const openTask = (task: WorkItem): void => { router.push(`/tasks/${task.id}`) }

const handleDragStart = (task: WorkItem, event: DragEvent): void => {
  draggedTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(task.id))
  }
}

const handleDragEnd = (): void => {
  draggedTask.value = null
  activeDropStatus.value = null
}

const handleDragOver = (status: TaskStatus, event: DragEvent): void => {
  event.preventDefault()
  if (draggedTask.value?.status !== status) activeDropStatus.value = status
  if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'
}

const updateTaskStatus = async (task: WorkItem, status: TaskStatus): Promise<void> => {
  if (task.status === status || updatingTaskIds.value.has(task.id)) return
  const previousStatus = task.status
  updatingTaskIds.value.add(task.id)
  task.status = status

  const result = await taskStore.updateStatus(task.id, status)
  updatingTaskIds.value.delete(task.id)
  if (!result.success) {
    task.status = previousStatus
    showToast(result.error || 'Could not update task status.', 'error')
    return
  }
  showToast(`Task moved to ${status.replace('_', ' ').toLowerCase()}.`, 'success')
}

const handleDrop = async (status: TaskStatus, event: DragEvent): Promise<void> => {
  event.preventDefault()
  const task = draggedTask.value
  handleDragEnd()
  if (task) await updateTaskStatus(task, status)
}

onMounted(() => {
  if (!taskStore.tasks.length) taskStore.fetchTasks()
})
</script>

<template>
  <section class="kanban-board" aria-label="Task kanban board">
    <div v-if="taskStore.loading && !taskStore.tasks.length" class="board-state">Loading tasks...</div>
    <div v-else-if="taskStore.error && !taskStore.tasks.length" class="board-state board-error">{{ taskStore.error }}</div>
    <div v-else class="kanban-columns">
      <section
        v-for="column in columns"
        :key="column.status"
        class="kanban-column"
        :class="[`column-${column.accent}`, { 'is-drop-target': activeDropStatus === column.status }]"
        @dragover="handleDragOver(column.status, $event)"
        @drop="handleDrop(column.status, $event)"
      >
        <header class="column-header">
          <div><span class="column-kicker">{{ column.status }}</span><h2>{{ column.title }}</h2></div>
          <span class="task-count">{{ tasksByStatus[column.status].length }}</span>
        </header>
        <div class="task-stack">
          <article
            v-for="task in tasksByStatus[column.status]"
            :key="task.id"
            class="kanban-card"
            :class="{ 'is-dragging': draggedTask?.id === task.id, 'is-updating': updatingTaskIds.has(task.id) }"
            draggable="true"
            tabindex="0"
            @click="openTask(task)"
            @keydown.enter="openTask(task)"
            @dragstart="handleDragStart(task, $event)"
            @dragend="handleDragEnd"
          >
            <div class="card-topline"><span class="priority-badge" :class="priorityClass(task.priority)">{{ task.priority }}</span><span class="drag-handle" aria-hidden="true">⋮⋮</span></div>
            <h3>{{ task.title }}</h3>
            <div class="card-footer">
              <span class="clarity-score" :class="clarityClass(task.clarityScore)">Clarity {{ task.clarityScore || 0 }}</span>
              <span class="assignee" :title="task.assignedTo?.name || task.assignedTo?.email || 'Unassigned'">
                <span class="assignee-avatar" :class="{ unassigned: !task.assignedTo }">{{ initials(task.assignedTo) }}</span>
              </span>
            </div>
            <span v-if="updatingTaskIds.has(task.id)" class="updating-label">Updating...</span>
          </article>
          <p v-if="!tasksByStatus[column.status].length" class="empty-column">Drop tasks here</p>
        </div>
      </section>
    </div>
    <Transition name="toast"><div v-if="toast" class="status-toast" :class="toast.type" role="status">{{ toast.message }}</div></Transition>
  </section>
</template>

<style scoped>
.kanban-board { position: relative; width: 100%; color: #17343b; }
.kanban-columns { display: grid; grid-template-columns: repeat(4, minmax(220px, 1fr)); gap: 1rem; align-items: start; overflow-x: auto; padding: .25rem; }
.kanban-column { min-height: 440px; border: 1px solid #dbe8e8; border-top: 3px solid #d87868; border-radius: 10px; padding: .85rem; background: #f7fbfb; transition: border-color .2s ease, background .2s ease, box-shadow .2s ease; }
.column-blue { border-top-color: #5b9bea; }.column-gold { border-top-color: #e3b341; }.column-green { border-top-color: #42b883; }
.kanban-column.is-drop-target { border-color: #247184; background: #edfafa; box-shadow: 0 0 0 3px rgba(36, 113, 132, .13); }
.column-header { display: flex; align-items: center; justify-content: space-between; gap: .5rem; margin-bottom: .8rem; }.column-kicker { color: #799397; font: .62rem 'DM Mono', monospace; letter-spacing: .1em; }.column-header h2 { margin: .25rem 0 0; font-size: .95rem; }.task-count { display: grid; place-items: center; min-width: 1.55rem; height: 1.55rem; border-radius: 50%; color: #247184; background: #dff7fb; font: .68rem 'DM Mono', monospace; }
.task-stack { display: grid; gap: .7rem; }.kanban-card { position: relative; display: grid; gap: .7rem; border: 1px solid #dbe8e8; border-radius: 8px; padding: .9rem; background: white; cursor: grab; transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease, background .2s ease; }.kanban-card:hover, .kanban-card:focus-visible { border-color: #63b4c1; background: #fcffff; box-shadow: 0 8px 20px rgba(25, 71, 78, .12); transform: translateY(-2px) scale(1.01); outline: none; }.kanban-card.is-dragging { opacity: .45; transform: rotate(1deg) scale(.98); }.kanban-card.is-updating { cursor: wait; opacity: .68; }.card-topline, .card-footer { display: flex; align-items: center; justify-content: space-between; gap: .5rem; }.priority-badge { border-radius: 4px; padding: .25rem .4rem; font: .6rem 'DM Mono', monospace; letter-spacing: .04em; }.priority-low { color: #31745e; background: #e2f4ea; }.priority-medium { color: #8a6915; background: #fff3cf; }.priority-high { color: #a54d3b; background: #ffe4dd; }.priority-urgent { color: #fff; background: #9b3d3d; }.drag-handle { color: #aec0c2; font-size: .9rem; letter-spacing: -.25em; }.kanban-card h3 { margin: 0; font-size: .82rem; line-height: 1.4; }.clarity-score { font: .62rem 'DM Mono', monospace; }.clarity-strong { color: #31856c; }.clarity-steady { color: #b8860b; }.clarity-needs-attention { color: #b05b4a; }.assignee-avatar { display: grid; place-items: center; width: 1.65rem; height: 1.65rem; border-radius: 50%; color: #17343b; background: #a9e4ed; font-size: .58rem; font-weight: 800; }.assignee-avatar.unassigned { color: #738689; background: #e8eeee; }.updating-label { color: #247184; font: .6rem 'DM Mono', monospace; }.empty-column { margin: 0; padding: 2rem .5rem; color: #9aabad; font-size: .7rem; text-align: center; border: 1px dashed #cbdcdc; border-radius: 7px; }
.board-state { padding: 4rem 1rem; color: #799397; text-align: center; }.board-error { color: #a54d3b; }.status-toast { position: fixed; right: 1.25rem; bottom: 1.25rem; z-index: 10; border: 1px solid #b8dfd1; border-radius: 7px; padding: .8rem 1rem; color: #215b4a; background: #effbf5; box-shadow: 0 8px 24px rgba(25, 71, 78, .16); font-size: .75rem; }.status-toast.error { border-color: #edc5bc; color: #914f42; background: #fff4f1; }.toast-enter-active, .toast-leave-active { transition: opacity .2s ease, transform .2s ease; }.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(8px); }
@media (max-width: 900px) { .kanban-columns { grid-template-columns: repeat(2, minmax(240px, 1fr)); } } @media (max-width: 600px) { .kanban-columns { grid-template-columns: repeat(4, minmax(240px, 1fr)); } }
</style>
