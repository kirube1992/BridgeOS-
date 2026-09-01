<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAiStore } from '@/stores/ai'
import type { AiActionItem, Project, User } from '@/types'

const props = defineProps<{
  project?: Project | null
  users?: User[]
  currentUserId?: number
  compact?: boolean
}>()

const emit = defineEmits<{
  promoted: [taskId: number, item: AiActionItem]
}>()

const ai = useAiStore()

const notes = ref('')
const error = ref('')
const success = ref('')

const hasItems = computed(() => ai.lastExtractedItems.length > 0)

const priorityClasses = (p: string) => {
  switch (p) {
    case 'URGENT': return 'priority-urgent'
    case 'HIGH': return 'priority-high'
    case 'LOW': return 'priority-low'
    default: return 'priority-medium'
  }
}

const extract = async () => {
  error.value = ''
  success.value = ''
  const trimmed = notes.value.trim()
  if (trimmed.length < 5) {
    error.value = 'Paste at least a few lines of meeting notes.'
    return
  }
  const result = await ai.extractMeeting(trimmed, props.project?.id)
  if (!result.success) {
    error.value = result.error || 'Extraction failed.'
  } else if (!result.data?.length) {
    error.value = 'No action items found. Try adding more details.'
  }
}

const toggleConfirm = (idx: number) => {
  const item = ai.lastExtractedItems[idx]
  if (!item) return
  ai.updateActionItem(idx, { confirmed: !item.confirmed })
}

const toggleEdit = (idx: number) => {
  const item = ai.lastExtractedItems[idx]
  if (!item) return
  ai.updateActionItem(idx, { editing: !item.editing })
}

const remove = (idx: number) => {
  ai.removeActionItem(idx)
}

const promote = async (item: AiActionItem) => {
  const assigned = item.suggestedAssignee?.id ?? undefined
  const result = await ai.promoteActionToTask(item, {
    projectId: props.project!.id,
    assignedToUserId: assigned,
    createdByUserId: props.currentUserId,
    departmentId: props.project?.projectManager?.department?.id ?? undefined
  })
  if (!result.success) {
    error.value = result.error || 'Promotion failed.'
  } else {
    emit('promoted', result.data.id, item)
  }
}

const promoteAllConfirmed = async () => {
  const confirmed = ai.lastExtractedItems.filter(i => i.confirmed && !i.promotedId)
  if (!confirmed.length) {
    error.value = 'First confirm the items you want to promote.'
    return
  }
  for (const item of confirmed) {
    await promote(item)
  }
  success.value = `${confirmed.length} action item(s) promoted to tasks.`
}

const reset = () => {
  notes.value = ''
  error.value = ''
  success.value = ''
  ai.resetExtraction()
}

const updateDescription = (idx: number, value: string) => {
  ai.updateActionItem(idx, { description: value })
}

const updateDueDate = (idx: number, value: string) => {
  ai.updateActionItem(idx, { suggestedDueDate: value || null })
}

const updatePriority = (idx: number, value: string) => {
  ai.updateActionItem(idx, { priority: value as 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' })
}

const updateAssignee = (idx: number, userIdStr: string) => {
  const userId = userIdStr ? Number(userIdStr) : null
  const user = props.users?.find(u => u.id === userId) || null
  ai.updateActionItem(idx, {
    suggestedAssignee: user
      ? { id: user.id, name: user.name, confidence: 1.0 }
      : null
  })
}
</script>

<template>
  <section class="extractor panel" :class="{ compact }">
    <div class="panel-header">
      <div>
        <span class="eyebrow">AI meeting notes</span>
        <h2>Extract action items from notes</h2>
        <p>Paste messy meeting notes; the AI returns structured items you can confirm and promote to tasks.</p>
      </div>
      <button v-if="hasItems" class="refresh-button" type="button" @click="reset">New extraction</button>
    </div>

    <div class="panel-body">
      <div v-if="!hasItems" class="input-block">
        <label class="field">
          <span>Meeting notes</span>
          <textarea
            v-model="notes"
            rows="8"
            placeholder="- Discussed export feature&#10;- Assigned to Sarah by Friday&#10;- UAT sign-off ASAP by Alex"
          ></textarea>
        </label>
        <div class="actions">
          <span v-if="project" class="hint">Promoted tasks go to: {{ project.name }}</span>
          <span v-else class="hint">Pick a project to promote items.</span>
          <button
            class="primary-button"
            type="button"
            :disabled="ai.extractLoading || notes.trim().length < 5"
            @click="extract"
          >
            <span v-if="ai.extractLoading">Extracting…</span>
            <span v-else>Extract action items</span>
          </button>
        </div>
      </div>

      <div v-else class="results-block">
        <div class="summary">
          <strong>{{ ai.lastExtractedItems.length }}</strong> item(s) found.
          <span class="muted">Confirm, edit, or discard each item, then promote the ones you want.</span>
        </div>

        <ul class="items">
          <li
            v-for="(item, idx) in ai.lastExtractedItems"
            :key="`${item.description}-${idx}`"
            class="item-card"
            :class="{ confirmed: item.confirmed, promoted: !!item.promotedId }"
          >
            <header class="item-header">
              <label class="confirm">
                <input
                  type="checkbox"
                  :checked="item.confirmed"
                  :disabled="!!item.promotedId"
                  @change="toggleConfirm(idx)"
                />
                <span>{{ item.confirmed ? 'Confirmed' : 'Confirm' }}</span>
              </label>
              <span :class="['priority-chip', priorityClasses(item.priority)]">{{ item.priority }}</span>
            </header>

            <div v-if="item.editing" class="editor">
              <label>
                Description
                <textarea
                  :value="item.description"
                  rows="2"
                  @input="updateDescription(idx, ($event.target as HTMLTextAreaElement).value)"
                ></textarea>
              </label>
              <div class="editor-row">
                <label>
                  Due date
                  <input
                    type="date"
                    :value="item.suggestedDueDate || ''"
                    @change="updateDueDate(idx, ($event.target as HTMLInputElement).value)"
                  />
                </label>
                <label>
                  Priority
                  <select
                    :value="item.priority"
                    @change="updatePriority(idx, ($event.target as HTMLSelectElement).value)"
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="URGENT">URGENT</option>
                  </select>
                </label>
                <label>
                  Assignee
                  <select
                    :value="item.suggestedAssignee?.id ?? ''"
                    @change="updateAssignee(idx, ($event.target as HTMLSelectElement).value)"
                  >
                    <option value="">Unassigned</option>
                    <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }}</option>
                  </select>
                </label>
              </div>
            </div>

            <div v-else class="view">
              <p class="description">{{ item.description }}</p>
              <div class="meta">
                <span v-if="item.suggestedAssignee" class="meta-chip">
                  👤 {{ item.suggestedAssignee.name }}
                  <small v-if="item.suggestedAssignee.confidence < 0.9">({{ Math.round(item.suggestedAssignee.confidence * 100) }}%)</small>
                </span>
                <span v-if="item.suggestedDueDate" class="meta-chip">📅 {{ item.suggestedDueDate }}</span>
                <span v-if="item.promotedId" class="meta-chip promoted-chip">
                  ✅ Promoted to task #{{ item.promotedId }}
                </span>
              </div>
            </div>

            <footer class="item-footer">
              <span class="spacer"></span>
              <button v-if="!item.promotedId" class="ghost-button" type="button" @click="toggleEdit(idx)">
                {{ item.editing ? 'Done editing' : 'Edit' }}
              </button>
              <button v-if="!item.promotedId" class="ghost-danger" type="button" @click="remove(idx)">
                Discard
              </button>
              <button
                v-if="!item.promotedId && project"
                class="primary-button small"
                type="button"
                :disabled="item.promoting"
                @click="promote(item)"
              >
                <span v-if="item.promoting">Promoting…</span>
                <span v-else>Promote to task</span>
              </button>
            </footer>
          </li>
        </ul>

        <p v-if="error" class="form-error">{{ error }}</p>
        <p v-if="success" class="form-success">{{ success }}</p>

        <div class="actions actions-end">
          <button
            v-if="project"
            class="primary-button"
            type="button"
            :disabled="!ai.lastExtractedItems.some(i => i.confirmed && !i.promotedId)"
            @click="promoteAllConfirmed"
          >
            Promote all confirmed
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.extractor { overflow: hidden; }
.panel-body { padding: 20px; }
.input-block, .results-block { display: grid; gap: 1rem; }

.field { display: grid; gap: .4rem; font-size: .75rem; font-weight: 800; color: var(--bridge-deep); }
textarea, input[type="date"], select {
  width: 100%;
  border: 1px solid var(--bridge-line);
  border-radius: 7px;
  padding: .6rem .7rem;
  color: var(--bridge-ink);
  background: white;
  font-size: .78rem;
  outline: none;
  resize: vertical;
  font-family: inherit;
  font-weight: 500;
}
textarea:focus, input:focus, select:focus {
  border-color: var(--bridge-cyan);
  box-shadow: 0 0 0 3px rgba(93, 204, 229, .2);
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}
.actions-end { justify-content: flex-end; }
.hint { color: var(--bridge-muted); font-size: .7rem; font-weight: 600; }
.muted { color: var(--bridge-muted); font-size: .72rem; font-weight: 500; }

.summary {
  display: flex;
  gap: .5rem;
  align-items: baseline;
  padding: .6rem .8rem;
  background: var(--bridge-cyan-soft);
  border-radius: 7px;
  font-size: .75rem;
  color: var(--bridge-deep);
}
.summary strong { color: #247184; font-family: 'DM Mono', monospace; }

.items { list-style: none; margin: 0; padding: 0; display: grid; gap: .8rem; }

.item-card {
  border: 1px solid var(--bridge-line);
  border-radius: 10px;
  padding: 14px;
  background: white;
  display: grid;
  gap: .75rem;
  transition: border-color .2s, box-shadow .2s;
}
.item-card.confirmed {
  border-color: var(--bridge-cyan);
  box-shadow: 0 4px 14px rgba(93, 204, 229, .12);
}
.item-card.promoted { opacity: .7; }

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.confirm {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  font-size: .72rem;
  font-weight: 700;
  color: var(--bridge-deep);
  cursor: pointer;
}
.confirm input { accent-color: var(--bridge-menu); }

.priority-chip {
  padding: .2rem .55rem;
  border-radius: 999px;
  font-size: .6rem;
  font-weight: 800;
  letter-spacing: .08em;
  font-family: 'DM Mono', monospace;
}
.priority-urgent { background: #fff0ec; color: #b14631; }
.priority-high { background: #fff7e0; color: #965d0e; }
.priority-medium { background: var(--bridge-cyan-soft); color: #247184; }
.priority-low { background: #eef3f3; color: var(--bridge-muted); }

.view .description {
  margin: 0;
  color: var(--bridge-ink);
  font-size: .82rem;
  line-height: 1.5;
}
.meta { display: flex; flex-wrap: wrap; gap: .35rem; margin-top: .5rem; }
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: .25rem;
  padding: .25rem .55rem;
  background: var(--bridge-paper);
  border-radius: 999px;
  font-size: .68rem;
  color: var(--bridge-deep);
}
.meta-chip small { color: var(--bridge-muted); font-weight: 600; }
.promoted-chip { background: #edf8f1; color: #2e6c50; }

.editor { display: grid; gap: .6rem; }
.editor label { display: grid; gap: .25rem; font-size: .7rem; font-weight: 700; color: var(--bridge-deep); }
.editor-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: .6rem;
}

.item-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: .5rem;
  border-top: 1px dashed var(--bridge-line);
  padding-top: .6rem;
}
.spacer { flex: 1; }

.primary-button, .ghost-button, .ghost-danger {
  border: 0;
  border-radius: 7px;
  padding: .55rem .85rem;
  font-size: .72rem;
  font-weight: 800;
  cursor: pointer;
  transition: background .15s, color .15s;
}
.primary-button { color: white; background: var(--bridge-menu); }
.primary-button:hover:not(:disabled) { background: var(--bridge-menu-dark); }
.primary-button:disabled { opacity: .5; cursor: not-allowed; }
.primary-button.small { padding: .4rem .7rem; font-size: .68rem; }
.ghost-button { color: var(--bridge-deep); background: #eef3f3; }
.ghost-button:hover { background: #e2ebec; }
.ghost-danger { color: #914f42; background: #fff3ef; }
.ghost-danger:hover { background: #ffe3d8; }

.form-error { margin: 0; border-radius: 7px; padding: .65rem; color: #914f42; background: #fff3ef; font-size: .75rem; }
.form-success { margin: 0; border-radius: 7px; padding: .65rem; color: #2e6c50; background: #edf8f1; font-size: .75rem; }

.compact .panel-header h2 { font-size: 1.05rem; }
.compact .panel-body { padding: 14px; }

@media (max-width: 720px) {
  .editor-row { grid-template-columns: 1fr; }
}
</style>
