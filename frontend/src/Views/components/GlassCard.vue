<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  accent?: 'comfura' | 'mesh' | 'bridge' | 'default'
  hoverGlow?: boolean
  tilt?: boolean
}>(), {
  accent: 'default',
  hoverGlow: true,
  tilt: false
})

const glowClass = computed(() => {
  return {
    'glow-comfura': props.accent === 'comfura',
    'glow-mesh': props.accent === 'mesh',
    'glow-bridge': props.accent === 'bridge',
    'glow-default': props.accent === 'default',
    'hover-effect': props.hoverGlow
  }
})
</script>

<template>
  <div class="glass-card" :class="glowClass">
    <!-- Corner Tech Accents -->
    <div class="tech-corner top-left"></div>
    <div class="tech-corner top-right"></div>
    <div class="tech-corner bottom-left"></div>
    <div class="tech-corner bottom-right"></div>

    <div class="glass-card-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  position: relative;
  background: rgba(10, 20, 30, 0.45);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.07);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}

.glass-card-content {
  position: relative;
  z-index: 2;
}

/* Corner details to look high-tech */
.tech-corner {
  position: absolute;
  width: 6px;
  height: 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  z-index: 1;
  transition: all 0.3s ease;
}
.top-left { top: 6px; left: 6px; border-right: 0; border-bottom: 0; }
.top-right { top: 6px; right: 6px; border-left: 0; border-bottom: 0; }
.bottom-left { bottom: 6px; left: 6px; border-right: 0; border-top: 0; }
.bottom-right { bottom: 6px; right: 6px; border-left: 0; border-top: 0; }

/* Custom glows based on Brand Accents */
.glow-default.hover-effect:hover {
  border-color: rgba(93, 204, 229, 0.5);
  box-shadow: 0 0 25px rgba(93, 204, 229, 0.2);
  transform: translateY(-3px);
}
.glow-comfura.hover-effect:hover {
  border-color: rgba(0, 212, 255, 0.6);
  box-shadow: 0 0 25px rgba(0, 212, 255, 0.25);
  transform: translateY(-3px);
}
.glow-mesh.hover-effect:hover {
  border-color: rgba(255, 107, 53, 0.6);
  box-shadow: 0 0 25px rgba(255, 107, 53, 0.25);
  transform: translateY(-3px);
}
.glow-bridge.hover-effect:hover {
  border-color: rgba(93, 204, 229, 0.6);
  box-shadow: 0 0 30px rgba(93, 204, 229, 0.25);
  transform: translateY(-3px);
}

/* Highlight corner indicators on hover */
.glass-card.hover-effect:hover .tech-corner {
  border-color: rgba(255, 255, 255, 0.4);
}
.glow-comfura.hover-effect:hover .tech-corner { border-color: rgba(0, 212, 255, 0.8); }
.glow-mesh.hover-effect:hover .tech-corner { border-color: rgba(255, 107, 53, 0.8); }
.glow-bridge.hover-effect:hover .tech-corner { border-color: rgba(93, 204, 229, 0.8); }
</style>
