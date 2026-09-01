<script setup lang="ts">
import { ref, onMounted } from 'vue'

const subtitle = "Bridging Continents, Building Futures"
const titleText = "COMFURA × MESH ASIA"
const tagline = "A strategic tech alliance powering cross-border enterprise solutions between East Africa and China."
</script>

<template>
  <div class="hero-container">
    <!-- Cyber Grid Background -->
    <div class="grid-overlay"></div>
    <div class="cyber-scan"></div>
    
    <!-- Neon Nodes -->
    <div class="node node-et">
      <span class="pulse-ring"></span>
      <span class="label">ADDIS ABABA (UTC+3)</span>
    </div>
    <div class="node node-cn">
      <span class="pulse-ring"></span>
      <span class="label">GUANGZHOU (UTC+8)</span>
    </div>

    <!-- Central Bridge Line -->
    <div class="bridge-line-animated"></div>

    <div class="hero-content">
      <div class="alliance-tag">
        <span class="tag-glow"></span>
        <span class="tag-text">GLOBAL STRATEGIC ALLIANCE</span>
      </div>

      <h1 class="hero-title">
        <span class="brand-comfura">COMFURA</span>
        <span class="brand-divider">×</span>
        <span class="brand-mesh">MESH ASIA</span>
      </h1>
      
      <p class="hero-subtitle">{{ subtitle }}</p>
      <p class="hero-tagline">{{ tagline }}</p>

      <!-- Logos Showcase -->
      <div class="logos-row">
        <div class="logo-box logo-comfura-box">
          <div class="logo-glow-bg"></div>
          <span class="logo-text">C</span>
          <span class="logo-subtext">Comfura</span>
        </div>
        
        <div class="bridge-connector">
          <div class="pulse-bullet"></div>
        </div>

        <div class="logo-box logo-mesh-box">
          <div class="logo-glow-bg"></div>
          <span class="logo-text text-mesh">M</span>
          <span class="logo-subtext">Mesh Asia</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero-container {
  position: relative;
  min-height: 520px;
  background: radial-gradient(circle at 50% 30%, #112233 0%, #0a0e17 70%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 1.5rem;
  overflow: hidden;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

/* Futuristic digital grid */
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 40px 40px;
  background-position: center;
  mask-image: radial-gradient(circle at 50% 50%, black 60%, transparent 100%);
  -webkit-mask-image: radial-gradient(circle at 50% 50%, black 60%, transparent 100%);
}

/* Glowing green scan line */
.cyber-scan {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, transparent, rgba(93, 204, 229, 0.3), transparent);
  animation: scan 6s linear infinite;
  z-index: 1;
}

@keyframes scan {
  0% { top: 0%; }
  100% { top: 100%; }
}

/* Nodes representing countries */
.node {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  z-index: 2;
}

.node-et {
  top: 20%;
  left: 15%;
}

.node-cn {
  bottom: 25%;
  right: 15%;
}

.node::before {
  content: '';
  width: 12px;
  height: 12px;
  border-radius: 50%;
  z-index: 3;
}

.node-et::before {
  background: #00d4ff;
  box-shadow: 0 0 15px #00d4ff;
}

.node-cn::before {
  background: #ff6b35;
  box-shadow: 0 0 15px #ff6b35;
}

.pulse-ring {
  position: absolute;
  width: 32px;
  height: 32px;
  border: 1px solid currentColor;
  border-radius: 50%;
  opacity: 0;
  animation: pulse-ring-animation 3s cubic-bezier(0.215, 0.610, 0.355, 1) infinite;
}

.node-et .pulse-ring { color: #00d4ff; }
.node-cn .pulse-ring { color: #ff6b35; }

@keyframes pulse-ring-animation {
  0% { transform: scale(0.3); opacity: 0; }
  50% { opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}

.node .label {
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--bridge-muted);
  letter-spacing: 0.1em;
  background: rgba(10, 14, 23, 0.8);
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

/* Translucent connecting link */
.bridge-line-animated {
  position: absolute;
  top: 20%;
  left: 15%;
  width: 70%;
  height: 55%;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.08), rgba(255, 107, 53, 0.08));
  clip-path: polygon(0% 0%, 100% 100%, 100% 98%, 0% 2%);
  z-index: 1;
}

/* Hero Content */
.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 720px;
  color: white;
}

.alliance-tag {
  position: relative;
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(93, 204, 229, 0.25);
  border-radius: 9999px;
  padding: 0.35rem 1rem;
  background: rgba(93, 204, 229, 0.05);
  margin-bottom: 2rem;
  overflow: hidden;
}

.tag-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  animation: shine 4s ease-in-out infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 200%; }
}

.tag-text {
  font-family: 'DM Mono', monospace;
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--bridge-cyan);
  letter-spacing: 0.15em;
}

.hero-title {
  font-size: clamp(2rem, 6vw, 4rem);
  font-weight: 800;
  letter-spacing: -.05em;
  line-height: 1;
  margin-bottom: 1.5rem;
}

.brand-comfura {
  background: linear-gradient(135deg, #00d4ff, #0088cc);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(0, 212, 255, 0.15);
}

.brand-divider {
  margin: 0 0.75rem;
  color: rgba(255, 255, 255, 0.25);
  font-size: clamp(1.5rem, 5vw, 3rem);
}

.brand-mesh {
  background: linear-gradient(135deg, #ff6b35, #dd4b1a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(255, 107, 53, 0.15);
}

.hero-subtitle {
  font-size: clamp(1.2rem, 3vw, 1.8rem);
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -.03em;
  margin-bottom: 1rem;
}

.hero-tagline {
  font-size: 0.95rem;
  line-height: 1.6;
  color: #94a3b8;
  max-width: 580px;
  margin: 0 auto 3rem;
}

/* Logos display row */
.logos-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
}

.logo-box {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(10, 20, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
}

.logo-glow-bg {
  position: absolute;
  inset: 0;
  opacity: 0.15;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.logo-box:hover .logo-glow-bg {
  opacity: 0.3;
}

.logo-comfura-box .logo-glow-bg { background: radial-gradient(circle, #00d4ff, transparent 70%); }
.logo-mesh-box .logo-glow-bg { background: radial-gradient(circle, #ff6b35, transparent 70%); }

.logo-comfura-box { border-color: rgba(0, 212, 255, 0.3); }
.logo-mesh-box { border-color: rgba(255, 107, 53, 0.3); }

.logo-text {
  font-size: 2.2rem;
  font-weight: 800;
  line-height: 1;
  z-index: 2;
  color: #00d4ff;
  font-family: 'Manrope', sans-serif;
}

.text-mesh {
  color: #ff6b35;
}

.logo-subtext {
  font-size: 0.58rem;
  font-weight: 700;
  color: var(--bridge-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 0.2rem;
  z-index: 2;
}

.bridge-connector {
  position: relative;
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg, #00d4ff, #ff6b35);
  border-radius: 9999px;
}

.pulse-bullet {
  position: absolute;
  top: -3px;
  left: 0;
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 10px white;
  animation: pulse-travel 3s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes pulse-travel {
  0% { left: 0%; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { left: 100%; opacity: 0; }
}

@media (max-width: 768px) {
  .node { display: none; }
  .bridge-line-animated { display: none; }
  .bridge-connector { width: 50px; }
  .hero-container { min-height: 450px; }
}
</style>
