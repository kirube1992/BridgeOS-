<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import type { User } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const search = ref('')
const user = computed(() => authStore.user)

const filteredUsers = computed(() => {
  const query = search.value.trim().toLowerCase()
  if (!query) return userStore.users
  return userStore.users.filter(member =>
    `${member.name} ${member.email} ${member.role} ${member.department?.name || ''}`.toLowerCase().includes(query)
  )
})

const initials = (member: User): string => (member.name || member.email || 'U')
  .split(/\s+/).map(part => part[0] || '').join('').slice(0, 2).toUpperCase()

const roleLabel = (role: string): string => role.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, letter => letter.toUpperCase())
const loadUsers = async (): Promise<void> => { await userStore.fetchUsers() }
const logout = (): void => { authStore.logout(); router.push('/login') }

onMounted(loadUsers)
</script>

<template>
  <div class="people-page">
    <nav class="people-nav">
      <router-link to="/dashboard" class="brand"><span class="mark-icon">B</span><span>BridgeOS</span></router-link>
      <div class="nav-links">
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/projects">Projects</router-link>
        <router-link to="/tasks">Tasks</router-link>
        <router-link to="/decisions">Decisions</router-link>
        <router-link class="current" to="/people">People & teams</router-link>
        <router-link to="/analytics">Analytics</router-link>
        <router-link to="/alliance">The Alliance</router-link>
      </div>
      <div class="user-actions"><router-link :to="`/profile/${user?.id || ''}`">{{ user?.name || user?.email }}</router-link><button type="button" @click="logout">Log out</button></div>
    </nav>

    <main class="people-main">
      <header class="page-header">
        <div><span class="eyebrow">Workspace directory</span><h1>People & teams</h1><p>Find the people behind the work and open their profiles.</p></div>
        <button class="refresh-button" type="button" :disabled="userStore.loading" @click="loadUsers">Refresh</button>
      </header>

      <div class="toolbar"><label class="search-field"><span aria-hidden="true">⌕</span><input v-model="search" type="search" placeholder="Search people or teams" aria-label="Search people or teams"></label><span class="count">{{ filteredUsers.length }} people</span></div>
      <div v-if="userStore.error" class="alert"><span>{{ userStore.error }}</span><button type="button" @click="loadUsers">Try again</button></div>
      <div v-if="userStore.loading" class="state">Loading team directory...</div>
      <div v-else-if="!filteredUsers.length" class="state">No people found.</div>
      <section v-else class="people-grid" aria-label="People directory">
        <router-link v-for="member in filteredUsers" :key="member.id" :to="`/profile/${member.id}`" class="person-card">
          <span class="avatar">{{ initials(member) }}</span><span class="person-copy"><strong>{{ member.name || member.email }}</strong><small>{{ member.email }}</small><em>{{ member.department?.name || roleLabel(member.role) }}</em></span><span class="arrow" aria-hidden="true">→</span>
        </router-link>
      </section>
    </main>
  </div>
</template>

<style scoped>
.people-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }
.people-nav { display: flex; align-items: center; gap: 2rem; min-height: 70px; padding: .85rem clamp(1rem, 5vw, 5rem); border-bottom: 1px solid var(--bridge-line); background: white; }
.brand { display: inline-flex; align-items: center; gap: .6rem; color: var(--bridge-ink); font-size: 1.15rem; font-weight: 800; }.mark-icon { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 9px; background: var(--bridge-cyan); }.nav-links { display: flex; gap: 1rem; margin-right: auto; color: var(--bridge-muted); font-size: .72rem; }.nav-links a:hover, .nav-links .current { color: #247184; }.user-actions { display: flex; align-items: center; gap: 1rem; color: var(--bridge-muted); font-size: .72rem; }.user-actions button { border: 0; color: #914f42; background: transparent; font-size: inherit; }
.people-main { width: min(100% - 2rem, 1080px); margin: auto; padding: 3rem 0; }.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 1rem; margin-bottom: 2rem; }.eyebrow { color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: .45rem 0 .35rem; font-size: clamp(2rem, 5vw, 3rem); letter-spacing: -.06em; }.page-header p { margin: 0; color: var(--bridge-muted); font-size: .85rem; }.refresh-button { border: 1px solid var(--bridge-line); border-radius: 7px; padding: .7rem 1rem; color: var(--bridge-ink); background: white; font-size: .75rem; font-weight: 800; }.toolbar { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }.search-field { display: flex; align-items: center; flex: 1; gap: .6rem; border: 1px solid var(--bridge-line); border-radius: 7px; padding: .7rem .8rem; color: var(--bridge-muted); background: white; }.search-field input { width: 100%; border: 0; outline: 0; color: var(--bridge-ink); font-size: .75rem; }.count { color: var(--bridge-muted); font-size: .72rem; white-space: nowrap; }.people-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .75rem; }.person-card { display: flex; align-items: center; gap: .8rem; border: 1px solid var(--bridge-line); border-radius: 10px; padding: 1rem; color: inherit; background: white; }.person-card:hover { border-color: var(--bridge-cyan); }.avatar { display: grid; place-items: center; width: 42px; height: 42px; border-radius: 50%; color: var(--bridge-ink); background: var(--bridge-cyan); font-size: .7rem; font-weight: 800; flex: 0 0 auto; }.person-copy { min-width: 0; flex: 1; }.person-copy strong, .person-copy small, .person-copy em { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.person-copy strong { font-size: .82rem; }.person-copy small, .person-copy em { margin-top: .25rem; color: var(--bridge-muted); font-size: .66rem; font-style: normal; }.arrow { color: #247184; }.alert { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1.5rem; border: 1px solid #f2c6bb; border-radius: 8px; padding: .8rem 1rem; color: #914f42; background: #fff3ef; font-size: .75rem; }.alert button { border: 0; color: inherit; background: transparent; font-weight: 800; text-decoration: underline; }.state { padding: 4rem 1rem; color: var(--bridge-muted); font-size: .8rem; text-align: center; }
@media (max-width: 760px) { .people-nav { flex-wrap: wrap; gap: 1rem; }.nav-links { order: 3; width: 100%; overflow-x: auto; padding-bottom: .2rem; }.user-actions { margin-left: auto; }.page-header { align-items: flex-start; flex-direction: column; }.toolbar { align-items: stretch; flex-direction: column; gap: .5rem; }.people-grid { grid-template-columns: 1fr; }.count { align-self: flex-end; } }
</style>
