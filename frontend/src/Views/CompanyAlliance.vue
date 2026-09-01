<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  comfuraData, 
  meshData, 
  allianceTimeline, 
  jointProjects,
  type CompanyId 
} from '@/composables/brand'
import AnimatedHero from '@/Views/components/AnimatedHero.vue'
import GlassCard from '@/Views/components/GlassCard.vue'

const router = useRouter()

const viewCompany = (id: CompanyId) => {
  router.push(`/company/${id}`)
}
</script>

<template>
  <div class="alliance-theme-wrapper">
    <!-- Navigation -->
    <nav class="alliance-nav">
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
    </nav>

    <!-- Animated Banner -->
    <AnimatedHero />

    <main class="alliance-main">
      <!-- Side by Side Companies -->
      <section class="companies-section">
        <h2 class="alliance-title-sec text-center">Alliance Members</h2>
        
        <div class="companies-grid">
          <!-- Comfura Profile -->
          <GlassCard accent="comfura" class="alliance-company-card">
            <span class="flag-icon">ET</span>
            <h3>{{ comfuraData.name }}</h3>
            <p class="tagline">{{ comfuraData.tagline }}</p>
            <p class="desc">{{ comfuraData.mission }}</p>
            
            <div class="card-footer">
              <span class="location">{{ comfuraData.founded }}</span>
              <button @click="viewCompany('comfura')" class="explore-btn btn-comfura">
                Explore Profile →
              </button>
            </div>
          </GlassCard>

          <!-- Mesh Asia Profile -->
          <GlassCard accent="mesh" class="alliance-company-card">
            <span class="flag-icon">CN</span>
            <h3>{{ meshData.name }}</h3>
            <p class="tagline">{{ meshData.tagline }}</p>
            <p class="desc">{{ meshData.mission }}</p>

            <div class="card-footer">
              <span class="location">{{ meshData.founded }}</span>
              <button @click="viewCompany('mesh')" class="explore-btn btn-mesh">
                Explore Profile →
              </button>
            </div>
          </GlassCard>
        </div>
      </section>

      <!-- Alliance Timeline -->
      <section class="timeline-section">
        <h2 class="alliance-title-sec text-center">Alliance Timeline</h2>
        <p class="section-desc text-center">The journey of Comfura and Mesh Asia joining forces.</p>

        <div class="timeline-wrapper">
          <div class="timeline-axis"></div>
          
          <div 
            v-for="(milestone, idx) in allianceTimeline" 
            :key="milestone.title" 
            class="timeline-item"
            :class="idx % 2 === 0 ? 'left' : 'right'"
          >
            <div class="timeline-node" :class="`node-accent-${milestone.side}`">
              <span class="node-bullet"></span>
            </div>
            
            <GlassCard :accent="milestone.side === 'bridge' ? 'bridge' : (milestone.side === 'comfura' ? 'comfura' : 'mesh')" class="timeline-card">
              <div class="timeline-date-chip">
                {{ milestone.month ? `${milestone.month} ` : '' }}{{ milestone.year }}
              </div>
              <h4>{{ milestone.title }}</h4>
              <p>{{ milestone.description }}</p>
            </GlassCard>
          </div>
        </div>
      </section>

      <!-- Joint Projects Showcase -->
      <section class="projects-section">
        <h2 class="alliance-title-sec text-center">Active Joint Initiatives</h2>
        <p class="section-desc text-center">Co-designed products driving shared growth.</p>
        
        <div class="projects-grid">
          <GlassCard 
            v-for="project in jointProjects" 
            :key="project.code" 
            accent="bridge"
            class="joint-project-card"
          >
            <div class="proj-header">
              <span class="proj-code">{{ project.code }}</span>
              <span class="proj-status" :class="project.status.toLowerCase().replace(' ', '-')">
                {{ project.status }}
              </span>
            </div>
            
            <h3>{{ project.title }}</h3>
            <p>{{ project.description }}</p>

            <div class="progress-bar-container">
              <div class="progress-info">
                <span>Implementation Progress</span>
                <span>{{ project.progress }}%</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: project.progress + '%' }"></div>
              </div>
            </div>

            <div class="tags-row">
              <span v-for="tag in project.tags" :key="tag" class="proj-tag">
                {{ tag }}
              </span>
            </div>
          </GlassCard>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.alliance-theme-wrapper {
  min-height: 100vh;
  background: #06090f;
  color: #e2e8f0;
}

/* Nav setup matching futuristic dark theme */
.alliance-nav {
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

.alliance-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 4rem 1.5rem;
}

.alliance-title-sec {
  font-size: clamp(1.6rem, 3.5vw, 2.2rem);
  font-weight: 800;
  letter-spacing: -0.04em;
  color: white;
  margin-top: 0;
  margin-bottom: 0.5rem;
}

.section-desc {
  color: var(--bridge-muted);
  font-size: 0.9rem;
  margin-bottom: 2.5rem;
}

.text-center { text-align: center; }

/* Companies Section */
.companies-section {
  margin-bottom: 5rem;
}

.companies-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.75rem;
}

.alliance-company-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.flag-icon {
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  font-weight: 800;
  color: var(--bridge-cyan);
  background: rgba(93, 204, 229, 0.1);
  border: 1px solid rgba(93, 204, 229, 0.25);
  border-radius: 4px;
  padding: 0.1rem 0.4rem;
  align-self: flex-start;
  margin-bottom: 1.25rem;
}

.alliance-company-card h3 {
  font-size: 1.35rem;
  font-weight: 800;
  color: white;
  margin: 0 0 0.5rem;
}

.alliance-company-card .tagline {
  font-size: 0.88rem;
  font-weight: 700;
  color: var(--bridge-muted);
  margin-bottom: 1rem;
}

.alliance-company-card .desc {
  font-size: 0.85rem;
  line-height: 1.6;
  color: #94a3b8;
  flex-grow: 1;
  margin-bottom: 2rem;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 1.25rem;
  margin-top: auto;
}

.location {
  font-size: 0.72rem;
  color: var(--bridge-muted);
}

.explore-btn {
  border: 0;
  border-radius: 7px;
  padding: 0.55rem 1rem;
  color: white;
  font-size: 0.72rem;
  font-weight: 800;
  transition: all 0.3s ease;
}

.btn-comfura {
  background: #00d4ff;
  color: #06090f;
}
.btn-comfura:hover {
  background: #00b6dc;
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.4);
}

.btn-mesh {
  background: #ff6b35;
}
.btn-mesh:hover {
  background: #e85822;
  box-shadow: 0 0 15px rgba(255, 107, 53, 0.4);
}

/* Timeline Section */
.timeline-section {
  margin-bottom: 6rem;
}

.timeline-wrapper {
  position: relative;
  max-width: 820px;
  margin: 3rem auto 0;
  padding: 1.5rem 0;
}

.timeline-axis {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  width: 2px;
  background: rgba(255, 255, 255, 0.06);
  transform: translateX(-50%);
}

.timeline-item {
  display: flex;
  margin-bottom: 2.5rem;
  position: relative;
  width: 50%;
}

.timeline-item.left {
  left: 0;
  padding-right: 2.5rem;
  justify-content: flex-end;
  text-align: right;
}

.timeline-item.right {
  left: 50%;
  padding-left: 2.5rem;
  justify-content: flex-start;
}

.timeline-node {
  position: absolute;
  top: 1.5rem;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #06090f;
  z-index: 10;
  display: grid;
  place-items: center;
}

.timeline-item.left .timeline-node {
  right: -7px;
}

.timeline-item.right .timeline-node {
  left: -7px;
}

.node-bullet {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.node-accent-comfura { border: 1px solid #00d4ff; }
.node-accent-comfura .node-bullet { background: #00d4ff; box-shadow: 0 0 8px #00d4ff; }

.node-accent-mesh { border: 1px solid #ff6b35; }
.node-accent-mesh .node-bullet { background: #ff6b35; box-shadow: 0 0 8px #ff6b35; }

.node-accent-bridge { border: 1px solid var(--bridge-cyan); }
.node-accent-bridge .node-bullet { background: var(--bridge-cyan); box-shadow: 0 0 8px var(--bridge-cyan); }

.timeline-card {
  width: min(100%, 360px);
}

.timeline-date-chip {
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--bridge-cyan);
  background: rgba(93, 204, 229, 0.05);
  border: 1px solid rgba(93, 204, 229, 0.2);
  border-radius: 4px;
  padding: 0.1rem 0.4rem;
  display: inline-block;
  margin-bottom: 0.5rem;
}

.timeline-card h4 {
  font-size: 0.95rem;
  font-weight: 800;
  color: white;
  margin: 0 0 0.4rem;
}

.timeline-card p {
  font-size: 0.78rem;
  line-height: 1.5;
  color: #94a3b8;
  margin: 0;
}

/* Joint Projects Section */
.projects-section {
  margin-bottom: 2rem;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.joint-project-card {
  display: flex;
  flex-direction: column;
}

.proj-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.proj-code {
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--bridge-muted);
}

.proj-status {
  font-family: 'DM Mono', monospace;
  font-size: 0.6rem;
  font-weight: 800;
  text-transform: uppercase;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
}

.proj-status.live {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
}

.proj-status.in-progress {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.25);
}

.proj-status.concept {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.25);
}

.joint-project-card h3 {
  font-size: 1.1rem;
  font-weight: 800;
  color: white;
  margin: 0 0 0.5rem;
}

.joint-project-card p {
  font-size: 0.8rem;
  line-height: 1.55;
  color: #94a3b8;
  margin: 0 0 1.5rem;
  flex-grow: 1;
}

/* Progress bar style */
.progress-bar-container {
  margin-bottom: 1.25rem;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.72rem;
  color: var(--bridge-muted);
  margin-bottom: 0.35rem;
}

.bar-track {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 9999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 9999px;
  background: linear-gradient(90deg, var(--bridge-cyan), #00d4ff);
  box-shadow: 0 0 10px rgba(93, 204, 229, 0.3);
}

/* Tags Row */
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.proj-tag {
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--bridge-muted);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  padding: 0.1rem 0.4rem;
}

@media (max-width: 820px) {
  .companies-grid {
    grid-template-columns: 1fr;
  }
  .timeline-axis {
    left: 20px;
  }
  .timeline-item {
    width: 100%;
    left: 0 !important;
    padding-left: 3rem !important;
    padding-right: 0 !important;
    text-align: left !important;
    justify-content: flex-start !important;
  }
  .timeline-item.left .timeline-node, .timeline-item.right .timeline-node {
    left: 13px !important;
  }
}
</style>
