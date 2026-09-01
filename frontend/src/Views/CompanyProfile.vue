<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { comfuraData, meshData, type CompanyId } from '@/composables/brand'
import GlassCard from '@/Views/components/GlassCard.vue'
import StatCounter from '@/Views/components/StatCounter.vue'

const route = useRoute()
const router = useRouter()

const companyId = computed(() => route.params.id as CompanyId)

const company = computed(() => {
  if (companyId.value === 'comfura') return comfuraData
  if (companyId.value === 'mesh') return meshData
  return null
})

const goBack = () => {
  router.push('/alliance')
}
</script>

<template>
  <div class="profile-theme-wrapper" v-if="company">
    <!-- Navigation -->
    <nav class="profile-nav">
      <router-link to="/dashboard" class="brand">
        <span class="mark-icon">B</span>
        <span>BridgeOS</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/projects">Projects</router-link>
        <router-link to="/tasks">Tasks</router-link>
        <router-link to="/decisions">Decisions</router-link>
        <router-link class="current" to="/alliance">The Alliance</router-link>
      </div>
      <button type="button" @click="goBack" class="back-btn">← Back to Alliance</button>
    </nav>

    <!-- Main Container -->
    <main class="profile-main">
      <header class="profile-header">
        <div class="title-section">
          <span class="eyebrow-accent" :class="`accent-${company.id}`">
            {{ company.country }} · EST. {{ company.foundedYear }}
          </span>
          <h1>{{ company.name }}</h1>
          <p class="tagline" :class="`accent-text-${company.id}`">{{ company.tagline }}</p>
        </div>
      </header>

      <!-- Mission & Quote Grid -->
      <section class="section-grid">
        <GlassCard :accent="company.id" :hover-glow="false" class="main-info-card">
          <h2 class="section-title">Our Mission</h2>
          <p class="mission-text">{{ company.mission }}</p>
          <div class="founded-badge">
            <span class="badge-label">HEADQUARTERS</span>
            <span class="badge-val">{{ company.founded }}</span>
          </div>
        </GlassCard>

        <GlassCard :accent="company.id" class="quote-card">
          <div class="quote-mark">“</div>
          <p class="quote-text">{{ company.heroQuote.replace(/[“”]/g, '') }}</p>
        </GlassCard>
      </section>

      <!-- Key Statistics -->
      <section class="stats-section">
        <h2 class="section-title text-center">Company Metrics</h2>
        <div class="stats-grid">
          <GlassCard 
            v-for="stat in company.stats" 
            :key="stat.label" 
            :accent="company.id"
            class="stat-box-wrapper"
          >
            <StatCounter 
              :value="stat.value" 
              :suffix="stat.suffix" 
              :label="stat.label" 
              :accent="company.id"
            />
          </GlassCard>
        </div>
      </section>

      <!-- Core Values -->
      <section class="values-section">
        <h2 class="section-title text-center">Core Values</h2>
        <div class="values-grid">
          <GlassCard 
            v-for="val in company.values" 
            :key="val.title" 
            :accent="company.id"
            class="value-card"
          >
            <h3>{{ val.title }}</h3>
            <p>{{ val.desc }}</p>
          </GlassCard>
        </div>
      </section>

      <!-- Leadership -->
      <section class="leadership-section">
        <h2 class="section-title text-center">Leadership Team</h2>
        <div class="leadership-grid">
          <GlassCard 
            v-for="leader in company.leadership" 
            :key="leader.name" 
            :accent="company.id"
            class="leader-card"
          >
            <div class="leader-avatar" :class="`avatar-${company.id}`">
              {{ leader.initials }}
            </div>
            <div class="leader-info">
              <h3>{{ leader.name }}</h3>
              <span class="leader-role">{{ leader.role }}</span>
              <p class="leader-bio">{{ leader.bio }}</p>
            </div>
          </GlassCard>
        </div>
      </section>
    </main>
  </div>
  
  <div class="profile-not-found" v-else>
    <p>Company profile not found.</p>
    <router-link to="/alliance">Return to Alliance page</router-link>
  </div>
</template>

<style scoped>
.profile-theme-wrapper {
  min-height: 100vh;
  background: #06090f;
  color: #e2e8f0;
}

/* Nav setup matching futuristic dark theme */
.profile-nav {
  display: flex;
  align-items: center;
  gap: 2rem;
  min-height: 70px;
  padding: .85rem clamp(1rem, 5vw, 5rem);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(6, 9, 15, 0.85);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 50;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: .6rem;
  color: white;
  font-size: 1.15rem;
  font-weight: 800;
}

.mark-icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--bridge-cyan);
  color: var(--bridge-ink);
}

.nav-links {
  display: flex;
  gap: 1.25rem;
  margin-right: auto;
  color: #64748b;
  font-size: .75rem;
}

.nav-links a:hover, .nav-links .current {
  color: var(--bridge-cyan);
}

.back-btn {
  border: 1px solid rgba(93, 204, 229, 0.3);
  border-radius: 7px;
  padding: 0.5rem 0.9rem;
  color: var(--bridge-cyan);
  background: rgba(93, 204, 229, 0.05);
  font-size: 0.72rem;
  font-weight: 700;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(93, 204, 229, 0.15);
  border-color: var(--bridge-cyan);
}

/* Main Profile Content */
.profile-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 3.5rem 1.5rem;
}

.profile-header {
  margin-bottom: 3rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 2rem;
}

.eyebrow-accent {
  font-family: 'DM Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.accent-comfura { color: #00d4ff; }
.accent-mesh { color: #ff6b35; }

.profile-header h1 {
  font-size: clamp(2.2rem, 5vw, 3.5rem);
  font-weight: 800;
  letter-spacing: -0.05em;
  margin: 0.5rem 0;
  color: white;
}

.tagline {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
}

.accent-text-comfura {
  background: linear-gradient(90deg, #00d4ff, #0088cc);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.accent-text-mesh {
  background: linear-gradient(90deg, #ff6b35, #dd4b1a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* Mission section */
.section-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 1.5rem;
  margin-bottom: 3.5rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-top: 0;
  margin-bottom: 1.25rem;
  color: white;
}

.mission-text {
  font-size: 0.95rem;
  line-height: 1.6;
  color: #94a3b8;
  margin-bottom: 1.5rem;
}

.founded-badge {
  display: flex;
  flex-direction: column;
}

.badge-label {
  font-family: 'DM Mono', monospace;
  font-size: 0.6rem;
  color: #64748b;
  letter-spacing: 0.1em;
}

.badge-val {
  font-size: 0.85rem;
  font-weight: 700;
  color: white;
  margin-top: 0.1rem;
}

/* Quote visual style */
.quote-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.quote-mark {
  font-family: 'Times New Roman', serif;
  font-size: 5rem;
  line-height: 0.5;
  color: rgba(255, 255, 255, 0.1);
  position: absolute;
  top: 1rem;
  left: 1rem;
}

.quote-text {
  font-size: 1.1rem;
  font-style: italic;
  line-height: 1.6;
  color: #cbd5e1;
  position: relative;
  z-index: 10;
  margin: 0;
  padding: 1rem;
}

/* Stats Section */
.stats-section {
  margin-bottom: 4rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.25rem;
  margin-top: 1.5rem;
}

.stat-box-wrapper {
  background: rgba(10, 15, 25, 0.35);
}

.text-center {
  text-align: center;
}

/* Values Grid */
.values-section {
  margin-bottom: 4.5rem;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.value-card h3 {
  font-size: 1.05rem;
  font-weight: 700;
  margin-top: 0;
  margin-bottom: 0.75rem;
  color: white;
}

.value-card p {
  font-size: 0.85rem;
  line-height: 1.6;
  color: #94a3b8;
  margin: 0;
}

/* Leadership layout */
.leadership-section {
  margin-bottom: 2rem;
}

.leadership-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.leader-card {
  display: flex;
  gap: 1.25rem;
  align-items: flex-start;
}

.leader-avatar {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 1.1rem;
  flex-shrink: 0;
  color: #06090f;
  background: var(--bridge-cyan);
}

.avatar-comfura {
  background: #00d4ff;
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.35);
}

.avatar-mesh {
  background: #ff6b35;
  box-shadow: 0 0 15px rgba(255, 107, 53, 0.35);
}

.leader-info h3 {
  font-size: 0.98rem;
  font-weight: 800;
  margin: 0;
  color: white;
}

.leader-role {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--bridge-muted);
  display: block;
  margin-top: 0.15rem;
  margin-bottom: 0.65rem;
}

.leader-bio {
  font-size: 0.8rem;
  line-height: 1.5;
  color: #94a3b8;
  margin: 0;
}

.profile-not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #06090f;
  color: #94a3b8;
  gap: 1rem;
}

.profile-not-found a {
  color: var(--bridge-cyan);
  text-decoration: underline;
}

@media (max-width: 850px) {
  .section-grid {
    grid-template-columns: 1fr;
  }
}
</style>
