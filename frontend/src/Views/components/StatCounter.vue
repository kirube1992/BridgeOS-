<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useScrollReveal, useCountUp } from '@/composables/brand'

const props = withDefaults(defineProps<{
  value: number
  suffix?: string
  label: string
  accent?: 'comfura' | 'mesh' | 'bridge' | 'default'
}>(), {
  suffix: '',
  accent: 'default'
})

const root = ref<HTMLElement | null>(null)
const valueRef = computed(() => props.value)

// Trigger reveal on scroll
const { visible } = useScrollReveal(root)

// Animated counter
const { display, begin } = useCountUp(valueRef, visible)

// Start count up when visible
watch(visible, (newVal) => {
  if (newVal) {
    begin()
  }
})

const colorClass = computed(() => {
  return {
    'text-comfura': props.accent === 'comfura',
    'text-mesh': props.accent === 'mesh',
    'text-bridge': props.accent === 'bridge',
    'text-default': props.accent === 'default'
  }
})
</script>

<template>
  <div ref="root" class="stat-counter">
    <div class="stat-number" :class="colorClass">
      <span class="num">{{ display }}</span>
      <span class="suffix">{{ suffix }}</span>
    </div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<style scoped>
.stat-counter {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
}

.stat-number {
  font-size: clamp(2rem, 4vw, 3.2rem);
  font-weight: 800;
  letter-spacing: -.05em;
  line-height: 1;
  margin-bottom: 0.5rem;
  font-family: 'DM Mono', monospace;
}

.text-default {
  color: var(--bridge-cyan);
  text-shadow: 0 0 15px rgba(93, 204, 229, 0.4);
}

.text-comfura {
  color: #00d4ff;
  text-shadow: 0 0 15px rgba(0, 212, 255, 0.4);
}

.text-mesh {
  color: #ff6b35;
  text-shadow: 0 0 15px rgba(255, 107, 53, 0.4);
}

.text-bridge {
  color: var(--bridge-cyan);
  text-shadow: 0 0 18px rgba(93, 204, 229, 0.5);
}

.stat-label {
  color: var(--bridge-muted);
  font-size: 0.82rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
</style>
