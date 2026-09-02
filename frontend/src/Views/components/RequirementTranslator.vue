<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAiStore } from '@/stores/ai'
import type { Project } from '@/types'

const props = defineProps<{
  project?: Project | null
  compact?: boolean
}>()

const emit = defineEmits<{
  saved: [id: number]
}>()

const ai = useAiStore()
const { locale } = useI18n()

const inputText = ref('')
const editing = reactive({
  whatToBuild: '',
  whyItMatters: '',
  acceptanceCriteria: [] as string[],
  edgeCases: [] as string[],
  technicalNotes: ''
})
const isEditing = ref(false)
const success = ref('')
const error = ref('')

const hasResult = computed(() => !!ai.lastTranslation)

const syncFromResult = () => {
  if (!ai.lastTranslation) return
  editing.whatToBuild = ai.lastTranslation.whatToBuild
  editing.whyItMatters = ai.lastTranslation.whyItMatters
  editing.acceptanceCriteria = [...(ai.lastTranslation.acceptanceCriteria || [])]
  editing.edgeCases = [...(ai.lastTranslation.edgeCases || [])]
  editing.technicalNotes = ai.lastTranslation.technicalNotes
}

const startEditing = () => {
  syncFromResult()
  isEditing.value = true
}

const cancelEditing = () => {
  isEditing.value = false
  syncFromResult()
}

const addAcceptanceCriterion = () => {
  editing.acceptanceCriteria.push('')
}

const addEdgeCase = () => {
  editing.edgeCases.push('')
}

const removeAcceptanceCriterion = (i: number) => {
  editing.acceptanceCriteria.splice(i, 1)
}

const removeEdgeCase = (i: number) => {
  editing.edgeCases.splice(i, 1)
}

const doTranslate = async () => {
  error.value = ''
  success.value = ''
  const text = inputText.value.trim()
  if (text.length < 3) {
    error.value = 'Please write at least a few words describing the requirement.'
    return
  }

  const localizedText = await ai.translateText(text, (locale.value as 'en' | 'zh') || 'en')
  const sourceText = localizedText.success && localizedText.data?.translatedText ? localizedText.data.translatedText : text
  const result = await ai.translate(sourceText, props.project?.id)
  if (!result.success) {
    error.value = result.error || 'Translation failed.'
    return
  }
  isEditing.value = false
  syncFromResult()
}

const doSave = async () => {
  error.value = ''
  success.value = ''
  const original = ai.lastTranslation?.originalText || inputText.value.trim()
  const payload = {
    originalText: original,
    whatToBuild: editing.whatToBuild.trim(),
    whyItMatters: editing.whyItMatters.trim(),
    acceptanceCriteria: editing.acceptanceCriteria.map(s => s.trim()).filter(Boolean),
    edgeCases: editing.edgeCases.map(s => s.trim()).filter(Boolean),
    technicalNotes: editing.technicalNotes.trim(),
    projectId: props.project?.id
  }
  if (!payload.whatToBuild) {
    error.value = '"What to build" is required.'
    return
  }
  const result = await ai.saveTranslation(payload)
  if (!result.success) {
    error.value = result.error || 'Failed to save.'
    return
  }
  success.value = 'Specification saved to your library.'
  isEditing.value = false
  emit('saved', result.data!.id)
}

const reset = () => {
  inputText.value = ''
  isEditing.value = false
  success.value = ''
  error.value = ''
  ai.resetTranslation()
}
</script>

<template>
  <section class="translator panel" :class="{ compact }">
    <div class="panel-header">
      <div>
        <span class="eyebrow">AI requirement translator</span>
        <h2>Turn a rough idea into a dev-ready spec</h2>
        <p>Paste a vague request or quick note; the AI produces a structured spec you can edit and save.</p>
      </div>
      <button v-if="hasResult" class="refresh-button" type="button" @click="reset">New translation</button>
    </div>

    <div class="panel-body">
      <div v-if="!hasResult" class="input-block">
        <label class="field">
          <span>Rough requirement</span>
          <textarea
            v-model="inputText"
            rows="5"
            placeholder='e.g. "Need export button for sales report, client wants it soon"'
          ></textarea>
        </label>
        <div class="actions">
          <span v-if="project" class="hint">Attached to project: {{ project.name }}</span>
          <span v-else class="hint">Not attached to any project.</span>
          <button
            class="primary-button"
            type="button"
            :disabled="ai.translateLoading || inputText.trim().length < 3"
            @click="doTranslate"
          >
            <span v-if="ai.translateLoading">Translating…</span>
            <span v-else>Generate spec</span>
          </button>
        </div>
      </div>

      <div v-else class="result-block">
        <div class="original">
          <span class="label">Original</span>
          <p>{{ ai.lastTranslation?.originalText }}</p>
        </div>

        <div class="result-grid">
          <label class="field field-span-2">
            <span>What to build</span>
            <textarea
              v-if="isEditing"
              v-model="editing.whatToBuild"
              rows="4"
            ></textarea>
            <div v-else class="static-text">{{ editing.whatToBuild }}</div>
          </label>

          <label class="field field-span-2">
            <span>Why it matters</span>
            <textarea
              v-if="isEditing"
              v-model="editing.whyItMatters"
              rows="3"
            ></textarea>
            <div v-else class="static-text">{{ editing.whyItMatters }}</div>
          </label>

          <label class="field">
            <span>
              Acceptance criteria
              <button
                v-if="isEditing"
                type="button"
                class="chip-button"
                @click="addAcceptanceCriterion"
              >+ Add</button>
            </span>
            <div v-if="isEditing" class="list-editor">
              <div v-for="(item, idx) in editing.acceptanceCriteria" :key="idx" class="list-item">
                <input v-model="editing.acceptanceCriteria[idx]" type="text" />
                <button type="button" class="x-button" aria-label="Remove" @click="removeAcceptanceCriterion(idx)">×</button>
              </div>
            </div>
            <ul v-else class="static-list">
              <li v-for="(item, idx) in editing.acceptanceCriteria" :key="idx">{{ item }}</li>
            </ul>
          </label>

          <label class="field">
            <span>
              Edge cases
              <button
                v-if="isEditing"
                type="button"
                class="chip-button"
                @click="addEdgeCase"
              >+ Add</button>
            </span>
            <div v-if="isEditing" class="list-editor">
              <div v-for="(item, idx) in editing.edgeCases" :key="idx" class="list-item">
                <input v-model="editing.edgeCases[idx]" type="text" />
                <button type="button" class="x-button" aria-label="Remove" @click="removeEdgeCase(idx)">×</button>
              </div>
            </div>
            <ul v-else class="static-list">
              <li v-for="(item, idx) in editing.edgeCases" :key="idx">{{ item }}</li>
            </ul>
          </label>

          <label class="field field-span-2">
            <span>Technical notes</span>
            <textarea
              v-if="isEditing"
              v-model="editing.technicalNotes"
              rows="3"
            ></textarea>
            <div v-else class="static-text">{{ editing.technicalNotes }}</div>
          </label>
        </div>

        <p v-if="error" class="form-error">{{ error }}</p>
        <p v-if="success" class="form-success">{{ success }}</p>

        <div class="actions actions-end">
          <button
            v-if="!isEditing"
            class="secondary-button"
            type="button"
            @click="startEditing"
          >Edit output</button>
          <template v-else>
            <button class="secondary-button" type="button" @click="cancelEditing">Cancel edits</button>
            <button
              class="primary-button"
              type="button"
              :disabled="ai.translateLoading"
              @click="doSave"
            >
              <span v-if="ai.translateLoading">Saving…</span>
              <span v-else>Save specification</span>
            </button>
          </template>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.translator { overflow: hidden; }
.panel-body { padding: 20px; }
.input-block, .result-block { display: grid; gap: 1rem; }

.field { display: grid; gap: .4rem; font-size: .75rem; font-weight: 800; color: var(--bridge-deep); }
.field > span { display: flex; justify-content: space-between; align-items: center; }
.field-span-2 { grid-column: span 2; }
textarea, input[type="text"] {
  width: 100%;
  border: 1px solid var(--bridge-line);
  border-radius: 7px;
  padding: .7rem;
  color: var(--bridge-ink);
  background: white;
  font-size: .8rem;
  outline: none;
  resize: vertical;
  font-family: inherit;
  font-weight: 500;
}
textarea:focus, input[type="text"]:focus {
  border-color: var(--bridge-cyan);
  box-shadow: 0 0 0 3px rgba(93, 204, 229, .2);
}

.static-text {
  border-radius: 7px;
  padding: .8rem;
  background: var(--bridge-paper);
  font-size: .8rem;
  color: var(--bridge-ink);
  white-space: pre-wrap;
  line-height: 1.5;
}
.static-list { margin: 0; padding-left: 1.1rem; font-size: .8rem; color: var(--bridge-ink); line-height: 1.5; }
.static-list li { margin: .2rem 0; }

.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.original {
  border-left: 3px solid var(--bridge-cyan);
  padding: .5rem .9rem;
  background: var(--bridge-cyan-soft);
  border-radius: 0 7px 7px 0;
}
.original .label { color: #247184; font-family: 'DM Mono', monospace; font-size: .6rem; letter-spacing: .1em; text-transform: uppercase; }
.original p { margin: .25rem 0 0; color: var(--bridge-deep); font-size: .8rem; line-height: 1.5; }

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}
.actions-end {
  justify-content: flex-end;
}

.hint { color: var(--bridge-muted); font-size: .7rem; font-weight: 600; }

.primary-button, .secondary-button {
  border: 0;
  border-radius: 7px;
  padding: .65rem 1rem;
  font-size: .75rem;
  font-weight: 800;
  cursor: pointer;
  transition: background .2s, transform .15s;
}
.primary-button { color: white; background: var(--bridge-menu); }
.primary-button:hover:not(:disabled) { background: var(--bridge-menu-dark); }
.primary-button:disabled { opacity: .55; cursor: not-allowed; }
.secondary-button { color: var(--bridge-deep); background: #eef3f3; }
.secondary-button:hover { background: #e2ebec; }

.chip-button {
  border: 0;
  background: var(--bridge-cyan-soft);
  color: #247184;
  border-radius: 999px;
  padding: .15rem .55rem;
  font-size: .65rem;
  font-weight: 800;
  cursor: pointer;
}
.list-editor { display: grid; gap: .35rem; }
.list-item { display: flex; gap: .4rem; align-items: center; }
.list-item input { border-radius: 6px; padding: .45rem .55rem; font-size: .75rem; }
.x-button {
  border: 1px solid var(--bridge-line);
  background: white;
  color: var(--bridge-muted);
  border-radius: 6px;
  width: 26px; height: 26px;
  font-size: 1rem;
  line-height: 1;
  cursor: pointer;
}
.x-button:hover { border-color: var(--bridge-coral); color: var(--bridge-coral); }

.form-error { margin: 0; border-radius: 7px; padding: .65rem; color: #914f42; background: #fff3ef; font-size: .75rem; }
.form-success { margin: 0; border-radius: 7px; padding: .65rem; color: #2e6c50; background: #edf8f1; font-size: .75rem; }

.compact .panel-header h2 { font-size: 1.05rem; }
.compact .panel-body { padding: 14px; }

@media (max-width: 720px) {
  .result-grid { grid-template-columns: 1fr; }
  .field-span-2 { grid-column: span 1; }
}
</style>
