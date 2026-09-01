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
  message: string
  createdAt: string
  sender: Sender
}

interface PagedChatResponse {
  content: ChatMessage[]
  totalPages: number
  totalElements: number
}

const props = defineProps<{
  projectId: number
}>()

const authStore = useAuthStore()
const currentUser = computed(() => authStore.user)

const messages = ref<ChatMessage[]>([])
const newMessageContent = ref('')
const loading = ref(true)
const sending = ref(false)
const loadingMore = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const messageContainer = ref<HTMLDivElement | null>(null)

let sseSource: EventSource | null = null

const hasMoreMessages = computed(() => currentPage.value + 1 < totalPages.value)

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

const formatTimestamp = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins} min ago`

  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)

  const pad = (num: number) => String(num).padStart(2, '0')
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}`

  if (date.toDateString() === today.toDateString()) {
    return `Today at ${time}`
  }
  if (date.toDateString() === yesterday.toDateString()) {
    return `Yesterday at ${time}`
  }
  return `${date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })} at ${time}`
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

const addMessage = (message: ChatMessage) => {
  if (!messages.value.some((m) => m.id === message.id)) {
    messages.value.push(message)
  }
}

const fetchHistory = async (page = 0, append = false) => {
  try {
    if (append) {
      loadingMore.value = true
    } else {
      loading.value = true
    }
    error.value = ''

    const response = await api.get<PagedChatResponse>(
      `/projects/${props.projectId}/messages`,
      { params: { page, size: 50 } }
    )

    const { content, totalPages: pages } = response.data
    currentPage.value = page
    totalPages.value = pages

    if (append) {
      const container = messageContainer.value
      const previousHeight = container?.scrollHeight ?? 0
      const older = content.filter((m) => !messages.value.some((existing) => existing.id === m.id))
      messages.value = [...older, ...messages.value]

      await nextTick()
      if (container) {
        container.scrollTop = container.scrollHeight - previousHeight
      }
    } else {
      messages.value = content
      scrollToBottom()
    }
  } catch (err: unknown) {
    console.error('Failed to load chat history:', err)
    error.value = 'Failed to load chat history.'
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMoreMessages = () => {
  if (!hasMoreMessages.value || loadingMore.value) return
  fetchHistory(currentPage.value + 1, true)
}

const connectSSE = () => {
  if (sseSource) {
    sseSource.close()
  }

  const token = authStore.token
  if (!token) return

  const streamUrl = `/api/projects/${props.projectId}/messages/stream?token=${encodeURIComponent(token)}`
  sseSource = new EventSource(streamUrl)

  sseSource.addEventListener('message', (event) => {
    try {
      const message: ChatMessage = JSON.parse(event.data)
      addMessage(message)
      scrollToBottom()
    } catch (err) {
      console.error('Failed to parse SSE chat message:', err)
    }
  })

  sseSource.onerror = (err) => {
    console.warn('SSE connection error:', err)
  }
}

const sendMessage = async () => {
  const content = newMessageContent.value.trim()
  if (!content || sending.value) return

  newMessageContent.value = ''
  sending.value = true
  try {
    const response = await api.post<ChatMessage>(`/projects/${props.projectId}/messages`, {
      content
    })
    addMessage(response.data)
    scrollToBottom()
  } catch (err: unknown) {
    console.error('Failed to send message:', err)
    newMessageContent.value = content
    const message =
      (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
      'Failed to send message. Please try again.'
    alert(message)
  } finally {
    sending.value = false
  }
}

watch(
  () => props.projectId,
  () => {
    messages.value = []
    currentPage.value = 0
    totalPages.value = 0
    fetchHistory()
    connectSSE()
  }
)

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

    <div ref="messageContainer" class="chat-body">
      <div v-if="loading" class="chat-state">
        <p>Loading conversation history...</p>
      </div>
      <div v-else-if="error" class="chat-state chat-error">
        <p>{{ error }}</p>
        <button type="button" @click="fetchHistory()">Retry</button>
      </div>
      <div v-else-if="!messages.length" class="chat-state chat-empty">
        <div class="empty-icon">💬</div>
        <strong>No messages yet</strong>
        <p>Start the conversation!</p>
      </div>
      <div v-else class="messages-list">
        <button
          v-if="hasMoreMessages"
          type="button"
          class="load-more-btn"
          :disabled="loadingMore"
          @click="loadMoreMessages"
        >
          {{ loadingMore ? 'Loading...' : 'Load older messages' }}
        </button>

        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-wrapper"
          :class="{ self: msg.sender?.id === currentUser?.id }"
        >
          <div class="chat-avatar" :title="msg.sender?.role">
            {{ getInitials(msg.sender?.name || '') }}
          </div>

          <div class="message-content-wrapper">
            <div class="message-info">
              <span class="sender-name">{{ msg.sender?.name }}</span>
              <span v-if="msg.sender?.role" class="sender-role">· {{ msg.sender.role }}</span>
              <span class="message-time">{{ formatTimestamp(msg.createdAt) }}</span>
            </div>
            <div class="message-bubble">
              {{ msg.message }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <form class="chat-footer" @submit.prevent="sendMessage">
      <input
        v-model="newMessageContent"
        type="text"
        placeholder="Type a message..."
        class="chat-input"
        maxlength="1000"
        :disabled="sending"
        @keydown.enter.prevent="sendMessage"
      />
      <button
        type="submit"
        class="send-btn"
        :disabled="!newMessageContent.trim() || sending"
        :aria-label="sending ? 'Sending message' : 'Send message'"
      >
        <span v-if="sending" class="send-spinner" />
        <svg
          v-else
          class="send-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
        >
          <line x1="22" y1="2" x2="11" y2="13" />
          <polygon points="22 2 15 22 11 13 2 9 22 2" />
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
  cursor: pointer;
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

.load-more-btn {
  align-self: center;
  border: 1px solid var(--bridge-line);
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--bridge-muted);
  background: white;
  cursor: pointer;
}

.load-more-btn:hover:not(:disabled) {
  color: var(--bridge-ink);
  border-color: var(--bridge-cyan);
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.message-wrapper.self .chat-avatar {
  background: var(--bridge-menu);
  color: white;
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

.message-wrapper.self .sender-name,
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

.chat-input:disabled {
  opacity: 0.6;
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
  cursor: pointer;
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

.send-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
