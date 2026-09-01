<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useAiStore } from '@/stores/ai'
import type { Project, WorkItem, Decision } from '@/types'

const props = defineProps<{
  project?: Project | null
  workItems?: WorkItem[]
  decisions?: Decision[]
  compact?: boolean
}>()

const ai = useAiStore()

const input = ref('')
const scrollerRef = ref<HTMLElement | null>(null)

const messages = computed(() => ai.chatMessages)

const suggestedQuestions = computed(() => {
  const items: string[] = [
    "What's blocked on this project?",
    'Summarize recent decisions',
    'Who is working on what?',
    'What should we prioritize this week?'
  ]
  return items
})

const scrollToBottom = async () => {
  await nextTick()
  if (scrollerRef.value) {
    scrollerRef.value.scrollTop = scrollerRef.value.scrollHeight
  }
}

watch(messages, () => scrollToBottom(), { deep: true })

onMounted(() => scrollToBottom())

const buildContext = () => {
  const ctx: Array<{ type: string; id: number; summary: string }> = []
  ;(props.workItems || []).slice(0, 30).forEach(wi => {
    ctx.push({
      type: 'work_item',
      id: wi.id,
      summary: `[${wi.status}] ${wi.title} — priority ${wi.priority}, assigned ${wi.assignedTo?.name || 'unassigned'}. Notes: ${(wi.description || '').slice(0, 120)}`
    })
  })
  ;(props.decisions || []).slice(0, 15).forEach(d => {
    ctx.push({
      type: 'decision',
      id: d.id,
      summary: `${d.summary || d.decision || d.detail || ''} — made by ${d.actor?.name || 'unknown'} on ${d.createdAt}`
    })
  })
  return ctx
}

const ask = async (text?: string) => {
  const question = (text ?? input.value).trim()
  if (!question) return
  input.value = ''
  const context = buildContext()
  await ai.ask(question, props.project?.id, context)
}

const askSuggested = (q: string) => ask(q)

const clearChat = () => ai.clearChat()

const onKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    ask()
  }
}
</script>

<template>
  <section class="assistant panel" :class="{ compact }">
    <div class="panel-header">
      <div>
        <span class="eyebrow">AI assistant</span>
        <h2>Ask questions about this project</h2>
        <p>Answers cite work items and decisions from the project.</p>
      </div>
      <button
        v-if="messages.length"
        class="refresh-button"
        type="button"
        @click="clearChat"
      >Clear chat</button>
    </div>

    <div class="chat-body">
      <div ref="scrollerRef" class="messages">
        <div v-if="!messages.length" class="empty-state">
          <div class="welcome-chip">BridgeOS AI</div>
          <p class="welcome-title">Hi — I can summarize status, spot blockers, and cite decisions.</p>
          <ul class="suggestions">
            <li v-for="q in suggestedQuestions" :key="q">
              <button type="button" @click="askSuggested(q)">{{ q }}</button>
            </li>
          </ul>
        </div>

        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message', msg.role]"
        >
          <div class="bubble">
            <p>{{ msg.content }}</p>
            <div v-if="msg.sources?.length" class="sources">
              <span class="sources-label">Sources</span>
              <ul>
                <li v-for="s in msg.sources" :key="`${msg.id}-${s.type}-${s.id}`">
                  <span class="src-type">{{ s.type }}</span>
                  <span class="src-id">#{{ s.id }}</span>
                  <span class="src-summary">{{ s.summary.slice(0, 140) }}{{ s.summary.length > 140 ? '…' : '' }}</span>
                </li>
              </ul>
            </div>
          </div>
          <div class="timestamp">
            {{ new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
          </div>
        </div>

        <div v-if="ai.askLoading" class="message assistant">
          <div class="bubble thinking">
            <span class="dots"><span></span><span></span><span></span></span>
          </div>
        </div>
      </div>

      <div class="composer">
        <textarea
          v-model="input"
          rows="2"
          placeholder='Ask anything, e.g. "What decisions are still pending?"'
          @keydown="onKeydown"
        ></textarea>
        <button
          class="primary-button"
          type="button"
          :disabled="ai.askLoading || !input.trim()"
          @click="() => ask()"
        >
          Send
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.assistant { overflow: hidden; display: flex; flex-direction: column; min-height: 480px; }
.chat-body { display: grid; grid-template-rows: 1fr auto; flex: 1; min-height: 0; }

.messages {
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 520px;
  background: linear-gradient(180deg, var(--bridge-paper), white 120px);
}

.empty-state {
  display: grid;
  gap: 1rem;
  justify-items: center;
  text-align: center;
  padding: 2rem 1rem;
}
.welcome-chip {
  font-family: 'DM Mono', monospace;
  font-size: .7rem;
  letter-spacing: .12em;
  text-transform: uppercase;
  color: #247184;
  background: var(--bridge-cyan-soft);
  border-radius: 999px;
  padding: .3rem .8rem;
}
.welcome-title { margin: 0; color: var(--bridge-deep); font-size: .9rem; max-width: 420px; line-height: 1.5; }
.suggestions {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: .5rem;
  width: 100%;
  max-width: 560px;
}
.suggestions button {
  width: 100%;
  text-align: left;
  padding: .6rem .75rem;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  background: white;
  color: var(--bridge-deep);
  font-size: .72rem;
  font-weight: 600;
  cursor: pointer;
  transition: border-color .15s, background .15s;
}
.suggestions button:hover { border-color: var(--bridge-cyan); background: var(--bridge-cyan-soft); }

.message { display: grid; gap: .35rem; max-width: 88%; }
.message.user { justify-self: end; }
.message.assistant { justify-self: start; }

.bubble {
  border-radius: 12px;
  padding: .75rem .9rem;
  font-size: .8rem;
  line-height: 1.55;
  box-shadow: 0 1px 2px rgba(16, 35, 43, .06);
}
.message.user .bubble {
  background: var(--bridge-menu);
  color: white;
  border-bottom-right-radius: 3px;
}
.message.assistant .bubble {
  background: white;
  color: var(--bridge-ink);
  border: 1px solid var(--bridge-line);
  border-bottom-left-radius: 3px;
}
.bubble p { margin: 0; white-space: pre-wrap; word-break: break-word; }

.sources {
  margin-top: .7rem;
  padding-top: .6rem;
  border-top: 1px dashed var(--bridge-line);
}
.sources-label {
  font-family: 'DM Mono', monospace;
  font-size: .6rem;
  letter-spacing: .1em;
  text-transform: uppercase;
  color: var(--bridge-muted);
}
.sources ul { list-style: none; margin: .3rem 0 0; padding: 0; display: grid; gap: .35rem; }
.sources li {
  display: grid;
  grid-template-columns: auto auto 1fr;
  gap: .4rem;
  align-items: start;
  font-size: .7rem;
  color: var(--bridge-deep);
}
.src-type {
  padding: .1rem .35rem;
  border-radius: 4px;
  background: var(--bridge-cyan-soft);
  color: #247184;
  font-weight: 800;
  font-size: .6rem;
  text-transform: uppercase;
}
.src-id {
  color: var(--bridge-muted);
  font-family: 'DM Mono', monospace;
  font-size: .65rem;
}
.src-summary { color: var(--bridge-deep); }

.timestamp {
  font-size: .6rem;
  color: var(--bridge-muted);
  padding: 0 .35rem;
}

.thinking .dots { display: inline-flex; gap: 4px; }
.thinking .dots span {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--bridge-muted);
  animation: blink 1.2s infinite ease-in-out both;
}
.thinking .dots span:nth-child(2) { animation-delay: .15s; }
.thinking .dots span:nth-child(3) { animation-delay: .3s; }
@keyframes blink { 0%, 80%, 100% { opacity: .25; } 40% { opacity: 1; } }

.composer {
  padding: 14px 16px 18px;
  border-top: 1px solid var(--bridge-line);
  background: white;
  display: flex;
  gap: .6rem;
  align-items: flex-end;
}
.composer textarea {
  flex: 1;
  min-height: 44px;
  max-height: 140px;
  resize: none;
  border: 1px solid var(--bridge-line);
  border-radius: 10px;
  padding: .6rem .75rem;
  font-size: .8rem;
  color: var(--bridge-ink);
  background: var(--bridge-paper);
  outline: none;
  font-family: inherit;
}
.composer textarea:focus {
  border-color: var(--bridge-cyan);
  box-shadow: 0 0 0 3px rgba(93, 204, 229, .18);
  background: white;
}

.primary-button {
  border: 0;
  border-radius: 10px;
  padding: .65rem 1.05rem;
  color: white;
  background: var(--bridge-menu);
  font-weight: 800;
  font-size: .78rem;
  cursor: pointer;
  transition: background .2s;
}
.primary-button:hover:not(:disabled) { background: var(--bridge-menu-dark); }
.primary-button:disabled { opacity: .5; cursor: not-allowed; }

.compact { min-height: 380px; }
.compact .messages { max-height: 340px; }
</style>
