<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Project } from '@/types'

defineProps<{
  isOpen: boolean
  projects: Project[]
}>()

const emit = defineEmits<{
  close: []
  submit: [data: { decision: string; context: string; projectId: number }]
}>()

const form = reactive({ decision: '', context: '', projectId: null as number | null })
const error = ref('')

const close = (): void => {
  form.decision = ''
  form.context = ''
  form.projectId = null
  error.value = ''
  emit('close')
}

const submit = (): void => {
  if (!form.decision.trim()) {
    error.value = 'Decision text is required.'
    return
  }
  if (!form.projectId) {
    error.value = 'Please select a project.'
    return
  }
  emit('submit', { decision: form.decision.trim(), context: form.context.trim(), projectId: form.projectId })
}
</script>

<template>
  <div v-if="isOpen" class="modal-layer" role="dialog" aria-modal="true" aria-labelledby="decision-modal-title">
    <button class="modal-backdrop" type="button" aria-label="Close dialog" @click="close"></button>
    <form class="decision-modal" @submit.prevent="submit">
      <div class="modal-header">
        <div>
          <span class="eyebrow">Decision log</span>
          <h2 id="decision-modal-title">New decision</h2>
        </div>
        <button class="close-button" type="button" aria-label="Close" @click="close">×</button>
      </div>
      <label>Decision text <textarea v-model="form.decision" rows="3" required placeholder="What was decided?"></textarea></label>
      <label>Context <textarea v-model="form.context" rows="4" placeholder="Why was this decision made?"></textarea></label>
      <label>Project
        <select v-model="form.projectId" required>
          <option :value="null" disabled>Select a project</option>
          <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
        </select>
      </label>
      <p v-if="error" class="form-error">{{ error }}</p>
      <div class="modal-actions">
        <button class="secondary-button" type="button" @click="close">Cancel</button>
        <button class="primary-button" type="submit">Save decision</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.modal-layer { position: fixed; inset: 0; display: grid; place-items: center; padding: 1rem; z-index: 10; }
.modal-backdrop { position: absolute; inset: 0; border: 0; background: rgba(16, 35, 43, .58); }
.decision-modal { position: relative; display: grid; gap: 1rem; width: min(100%, 520px); border-radius: 12px; padding: 1.5rem; background: white; box-shadow: 0 20px 60px rgba(16, 35, 43, .22); }
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; }
.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }
h2 { margin: .35rem 0 0; color: var(--bridge-ink); font-size: 1.35rem; }
.close-button { border: 0; color: var(--bridge-muted); background: transparent; font-size: 1.5rem; line-height: 1; }
label { display: grid; gap: .4rem; color: var(--bridge-deep); font-size: .75rem; font-weight: 800; }
textarea, select { width: 100%; border: 1px solid var(--bridge-line); border-radius: 7px; padding: .7rem; color: var(--bridge-ink); background: white; font-size: .8rem; outline: none; resize: vertical; }
textarea:focus, select:focus { border-color: var(--bridge-cyan); box-shadow: 0 0 0 3px rgba(93, 204, 229, .2); }
.form-error { margin: 0; border-radius: 7px; padding: .65rem; color: #914f42; background: #fff3ef; font-size: .75rem; }
.modal-actions { display: flex; justify-content: flex-end; gap: .65rem; padding-top: .5rem; }
.secondary-button, .primary-button { border: 0; border-radius: 7px; padding: .65rem .9rem; font-size: .75rem; font-weight: 800; }
.secondary-button { color: var(--bridge-deep); background: #eef3f3; }
.primary-button { color: var(--bridge-ink); background: var(--bridge-cyan); }
</style>
