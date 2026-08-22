<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnalyticsStore } from '@/stores/analytics'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const analytics = useAnalyticsStore()
const user = computed(() => analytics.userMetrics?.user)
const metrics = computed(() => analytics.userMetrics)

onMounted(() => { analytics.fetchUserMetrics(Number(route.params.userId)) })
const logout = (): void => { authStore.logout(); router.push('/login') }
</script>

<template>
  <div class="user-page">
    <nav class="user-nav"><router-link to="/dashboard" class="brand"><span class="mark-icon">B</span><span>BridgeOS</span></router-link><div class="nav-links"><router-link to="/analytics">Analytics</router-link><router-link to="/decisions">Decisions</router-link></div><button type="button" @click="logout">Log out</button></nav>
    <main class="user-main"><router-link to="/analytics" class="back-link">← Back to analytics</router-link><div v-if="analytics.loading" class="state">Loading user metrics...</div><div v-else-if="analytics.error" class="state error">{{ analytics.error }}</div><section v-else-if="metrics" class="profile"><span class="avatar">{{ (user?.name || user?.email || 'U').slice(0, 2).toUpperCase() }}</span><span class="eyebrow">Individual performance</span><h1>{{ user?.name || user?.email }}</h1><p>{{ user?.department?.name || 'Unassigned department' }}</p><div class="metric-grid"><div><strong>{{ metrics.itemsResolved }}</strong><span>Resolved</span></div><div><strong>{{ metrics.averageResolutionHours.toFixed(1) }}h</strong><span>Avg. resolution</span></div><div><strong>{{ metrics.averageClarityScore.toFixed(0) }}</strong><span>Clarity score</span></div></div></section></main>
  </div>
</template>

<style scoped>
.user-page { min-height: 100vh; background: var(--bridge-paper); color: var(--bridge-ink); }.user-nav { display: flex; align-items: center; gap: 2rem; min-height: 70px; padding: .85rem clamp(1rem, 5vw, 5rem); border-bottom: 1px solid var(--bridge-line); background: white; }.brand { display: flex; align-items: center; gap: .6rem; margin-right: auto; font-size: 1.1rem; font-weight: 800; }.mark-icon, .avatar { display: grid; place-items: center; border-radius: 50%; color: var(--bridge-ink); background: var(--bridge-cyan); }.mark-icon { width: 34px; height: 34px; border-radius: 9px; }.nav-links { display: flex; gap: 1rem; color: var(--bridge-muted); font-size: .75rem; }.user-nav button { border: 0; color: #914f42; background: transparent; font-size: .72rem; }.user-main { width: min(100% - 2rem, 920px); margin: auto; padding: 3rem 0; }.back-link { color: #247184; font-size: .75rem; }.state { padding: 4rem 0; color: var(--bridge-muted); }.error { color: #914f42; }.profile { max-width: 720px; margin-top: 3rem; border: 1px solid var(--bridge-line); border-radius: 10px; padding: 2rem; background: white; }.avatar { width: 54px; height: 54px; margin-bottom: 1.5rem; font-weight: 800; }.eyebrow { display: block; color: #247184; font-family: 'DM Mono', monospace; font-size: .65rem; letter-spacing: .12em; text-transform: uppercase; }.profile h1 { margin: .45rem 0 .25rem; font-size: 2rem; }.profile p { margin: 0; color: var(--bridge-muted); }.metric-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-top: 2rem; }.metric-grid div { border-top: 1px solid var(--bridge-line); padding-top: 1rem; }.metric-grid strong, .metric-grid span { display: block; }.metric-grid strong { font-size: 1.5rem; }.metric-grid span { margin-top: .25rem; color: var(--bridge-muted); font-size: .7rem; }@media (max-width: 600px) { .user-nav { flex-wrap: wrap; }.nav-links { order: 3; width: 100%; }.metric-grid { grid-template-columns: 1fr; } }
</style>
