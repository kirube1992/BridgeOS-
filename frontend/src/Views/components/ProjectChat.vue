<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/API/index'

interface Sender {
  id: number
  name: string
  email: string
  role: string
}

interface ChatMessage {
  id: number
  content: string
  createdAt: string
  sender: Sender
}

const props = defineProps<{
  projectId: number
}>()

const authStore = useAuthStore()
const currentUser = computed(() => authStore.user)

const messages = ref<ChatMessage[]>([])
const newMessageContent = ref('')
const loading = ref(true)
const error = ref('')
const messageContainer = ref<HTMLDivElement | null>(null)

let sseSource: EventSource | null = null

// Formats initials from the name
const getInitials = (name: string) => {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/)
  if (parts.length >= 2) {
    const first = parts[0]
    const last = parts[parts.length - 1]
    if (first && last && first[0] && last[0]) {
      return (first[0] + last[0]).toUpperCase()
    }
  }
  return name.slice(0, 2).toUpperCase()
}

// Scans timestamps to format them cleanly
const formatTimestamp = (dateStr: string) => {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)

  const pad = (num: number) => String(num).padStart(2, '0')
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}`

  if (date.toDateString() === today.toDateString()) {
    return `Today at ${time}`
  } else if (date.toDateString() === yesterday.toDateString()) {
    return `Yesterday at ${time}`
  } else {
    return `${date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })} at ${time}`
  }
}

// Automatically scrolls the message panel to the bottom
const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

// Fetches initial message history
const fetchHistory = async () => {
  try {
    loading.value = true
    error.value = ''
    const response = await api.get<ChatMessage[]>(`/projects/${props.projectId}/messages`)
    messages.value = response.data
    scrollToBottom()
  } catch (err: any) {
    console.error('Failed to load chat history:', err)
    error.value = 'Failed to load chat history.'
  } finally {
    loading.value = false
  }
}

// Connects to SseEmitter stream
const connectSSE = () => {
  if (sseSource) {
    sseSource.close()
  }

  const token = authStore.token
  if (!token) return

  // Standard EventSource connection passing token as query parameter
  const streamUrl = `/api/projects/${props.projectId}/messages/stream?token=${encodeURIComponent(token)}`
  sseSource = new EventSource(streamUrl)

  sseSource.addEventListener('message', (event) => {
    try {
      const message: ChatMessage = JSON.parse(event.data)
      // Check if message is already in list (avoid duplicates if POST API responds before SSE fires)
      if (!messages.value.some((m) => m.id === message.id)) {
        messages.value.push(message)
        scrollToBottom()
      }
    } catch (err) {
      console.error('Failed to parse SSE chat message:', err)
    }
  })

  sseSource.onerror = (err) => {
    console.warn('SSE Connection error, attempting reconnection...', err)
    // SseEmitter automatically reconnects in modern browsers, but we log it
  }
}

// Sends a message
const sendMessage = async () => {
  const content = newMessageContent.value.trim()
  if (!content) return

  newMessageContent.value = ''
  try {
    const response = await api.post<ChatMessage>(`/projects/${props.projectId}/messages`, {
      content
    })
    const saved = response.data
    // Prevent duplicate insertion if SSE fires immediately
    if (!messages.value.some((m) => m.id === saved.id)) {
      messages.value.push(saved)
      scrollToBottom()
    }
  } catch (err: any) {
    console.error('Failed to send message:', err)
    alert(err.response?.data?.message || 'Failed to send message. Please try again.')
  }
}

// Setup/Teardown logic
watch(() => props.projectId, () => {
  fetchHistory()
  connectSSE()
})

onMounted(() => {
  fetchHistory()
  connectSSE()
})

onUnmounted(() => {
  if (sseSource) {
    sseSource.close()
  }
})
</script>

<template>
  <div class="chat-panel">
    <div class="chat-header">
      <div class="chat-header-info">
        <span class="chat-badge">Live</span>
        <h3>Project Discussion</h3>
        <p>Cross-border sync room</p>
      </div>
    </div>

    <!-- Message List -->
    <div ref="messageContainer" class="chat-body">
      <div v-if="loading" class="chat-state">
        <p>Loading conversation history...</p>
      </div>
      <div v-else-if="error" class="chat-state chat-error">
        <p>{{ error }}</p>
        <button type="button" @click="fetchHistory">Retry</button>
      </div>
      <div v-else-if="!messages.length" class="chat-state chat-empty">
        <div class="empty-icon">💬</div>
        <strong>No messages yet</strong>
        <p>Be the first to share context on this project!</p>
      </div>
      <div v-else class="messages-list">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          class="message-wrapper"
          :class="{ 'self': msg.sender?.id === currentUser?.id }"
        >
          <!-- Sender Initials Avatar (only for others) -->
          <div v-if="msg.sender?.id !== currentUser?.id" class="chat-avatar" :title="msg.sender?.role">
            {{ getInitials(msg.sender?.name || '') }}
          </div>

          <div class="message-content-wrapper">
            <div class="message-info">
              <span class="sender-name">{{ msg.sender?.name }}</span>
              <span class="sender-role" v-if="msg.sender?.role">· {{ msg.sender?.role }}</span>
              <span class="message-time">{{ formatTimestamp(msg.createdAt) }}</span>
            </div>
            <div class="message-bubble">
              {{ msg.content }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Footer -->
    <form @submit.prevent="sendMessage" class="chat-footer">
      <input 
        v-model="newMessageContent"
        type="text" 
        placeholder="Type a message, press Enter to send..."
        class="chat-input"
        maxLength="1000"
        @keydown.enter.prevent="sendMessage"
      />
      <button 
        type="submit" 
        class="send-btn" 
        :disabled="!newMessageContent.trim()"
      >
        <svg class="send-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="22" y1="2" x2="11" y2="13"></line>
          <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
      </button>
    </form>
  </div>
</template>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 520px;
  background: white;
  border: 1px solid var(--bridge-line);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(16, 35, 43, 0.03);
}

.chat-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--bridge-line);
  padding: 1rem 1.25rem;
  background: #fafcfc;
}

.chat-header-info h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 800;
  color: var(--bridge-ink);
}

.chat-header-info p {
  margin: 0.15rem 0 0;
  font-size: 0.72rem;
  color: var(--bridge-muted);
}

.chat-badge {
  display: inline-block;
  font-size: 0.58rem;
  font-family: 'DM Mono', monospace;
  font-weight: 800;
  text-transform: uppercase;
  color: #10b981;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  border-radius: 4px;
  padding: 0.05rem 0.35rem;
  margin-bottom: 0.25rem;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.25rem;
  background: var(--bridge-paper);
}

.chat-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--bridge-muted);
  font-size: 0.8rem;
  text-align: center;
}

.chat-error button {
  margin-top: 0.5rem;
  border: 1px solid #f2c6bb;
  border-radius: 6px;
  padding: 0.4rem 0.8rem;
  color: #914f42;
  background: #fff3ef;
  font-weight: 800;
  font-size: 0.75rem;
}

.chat-empty .empty-icon {
  font-size: 2.2rem;
  margin-bottom: 0.5rem;
}

.chat-empty strong {
  color: var(--bridge-ink);
  font-size: 0.85rem;
}

.chat-empty p {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  max-width: 85%;
}

.message-wrapper.self {
  align-self: flex-end;
  max-width: 80%;
  flex-direction: row-reverse;
}

.chat-avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--bridge-ink);
  background: var(--bridge-cyan);
  font-size: 0.7rem;
  font-weight: 800;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.message-info {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.68rem;
  color: var(--bridge-muted);
  padding: 0 0.15rem;
}

.sender-name {
  font-weight: 800;
  color: var(--bridge-deep);
}

.message-wrapper.self .sender-name {
  display: none; /* Hide self name to keep it clean */
}

.message-wrapper.self .sender-role {
  display: none;
}

.message-wrapper.self .message-info {
  justify-content: flex-end;
}

.message-bubble {
  border: 1px solid var(--bridge-line);
  border-radius: 0 12px 12px 12px;
  padding: 0.65rem 0.85rem;
  font-size: 0.8rem;
  line-height: 1.45;
  color: var(--bridge-ink);
  background: white;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.message-wrapper.self .message-bubble {
  border: 0;
  border-radius: 12px 0 12px 12px;
  color: white;
  background: var(--bridge-menu);
  box-shadow: 0 2px 6px rgba(36, 83, 96, 0.15);
}

.chat-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-top: 1px solid var(--bridge-line);
  padding: 0.75rem 1rem;
  background: white;
}

.chat-input {
  flex: 1;
  border: 1px solid var(--bridge-line);
  border-radius: 8px;
  padding: 0.65rem 0.85rem;
  font-size: 0.8rem;
  color: var(--bridge-ink);
  outline: none;
  background: #fcfdfd;
}

.chat-input:focus {
  border-color: var(--bridge-cyan);
  background: white;
  box-shadow: 0 0 0 3px rgba(93, 204, 229, 0.15);
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  color: white;
  background: var(--bridge-menu);
  transition: background 0.2s, transform 0.1s;
}

.send-btn:hover:not(:disabled) {
  background: var(--bridge-menu-dark);
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.send-icon {
  width: 15px;
  height: 15px;
  transform: translate(-1px, 1px);
}
</style>
