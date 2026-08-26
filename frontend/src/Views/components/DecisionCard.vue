<script setup lang="ts">
import type { Decision } from '@/types'

defineProps<{
  decision: Decision
}>()

const emit = defineEmits<{
  edit: []
  delete: []
}>()

const relativeTime = (date: string): string => {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(date).getTime()) / 1000))
  if (seconds < 60) return 'Just now'
  if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`
  if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`
  return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const initials = (decision: Decision): string => {
  const name = decision.actor?.name || decision.actor?.email || 'U'
  return name.split(/\s+/).map(part => part[0] || '').join('').slice(0, 2).toUpperCase()
}
</script>

<template>
  <article class="decision-card">
    <div class="decision-marker" aria-hidden="true"></div>
    <div class="decision-content">
      <div class="decision-meta">
        <span class="avatar">{{ initials(decision) }}</span>
        <span class="recorded-by">{{ decision.actor?.name || decision.actor?.email || 'Unknown user' }}</span>
        <time :datetime="decision.createdAt">{{ relativeTime(decision.createdAt) }}</time>
      </div>
      <h2>{{ decision.summary || decision.decision }}</h2>
      <p v-if="decision.detail || decision.context" class="decision-context">{{ decision.detail || decision.context }}</p>
      <span v-if="decision.project" class="project-badge">{{ decision.project.name }}</span>
      <div class="decision-actions">
        <button type="button" @click="emit('edit')">Edit</button>
        <button class="delete-action" type="button" @click="emit('delete')">Delete</button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.decision-card { position: relative; display: flex; gap: 1rem; padding: 0 0 1.5rem; }
.decision-marker { width: 12px; height: 12px; margin: 0.35rem 0 0 -0.35rem; border: 3px solid var(--bridge-cyan); border-radius: 50%; background: white; flex: 0 0 auto; z-index: 1; }
.decision-content { width: min(100%, 720px); border: 1px solid var(--bridge-line); border-radius: 10px; padding: 1.25rem 1.35rem; background: white; box-shadow: 0 3px 12px rgba(16, 35, 43, .04); }
.decision-meta { display: flex; align-items: center; gap: .6rem; color: var(--bridge-muted); font-size: .72rem; }
.avatar { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%; color: var(--bridge-ink); background: var(--bridge-cyan); font-size: .65rem; font-weight: 800; }
.recorded-by { color: var(--bridge-deep); font-weight: 800; }
time { margin-left: auto; white-space: nowrap; }
h2 { margin: 1rem 0 .45rem; color: var(--bridge-ink); font-size: 1rem; line-height: 1.4; }
.decision-context { margin: 0 0 .9rem; color: var(--bridge-muted); font-size: .8rem; line-height: 1.65; white-space: pre-line; }
.project-badge { display: inline-block; border-radius: 5px; padding: .3rem .55rem; color: #286b78; background: var(--bridge-cyan-soft); font-size: .68rem; font-weight: 800; }
.decision-actions { display: flex; gap: .8rem; margin-top: 1rem; }
.decision-actions button { border: 0; padding: 0; color: var(--bridge-menu); background: transparent; font-size: .7rem; font-weight: 800; text-decoration: underline; }
.decision-actions .delete-action { color: #914f42; }
</style>
