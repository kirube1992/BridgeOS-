<template>
  <div class="clarity-score">
    <!-- Score Header -->
    <div class="score-header">
      <div class="score-label-group">
        <span class="score-label">Clarity Score</span>
        <span class="score-badge" :class="scoreClass">
          {{ score }} / 100
        </span>
      </div>
      <span v-if="score >= 80" class="status-text good">✅ Clear</span>
      <span v-else-if="score >= 60" class="status-text medium">⚠️ Needs improvement</span>
      <span v-else class="status-text poor">🔴 Vague</span>
    </div>

    <!-- Progress Bar -->
    <div class="score-bar">
      <div class="score-fill" :style="{ width: score + '%' }" :class="scoreClass"></div>
    </div>

    <!-- Score Breakdown (optional) -->
    <div v-if="showBreakdown && rules.length" class="score-breakdown">
      <div class="breakdown-header">
        <span class="breakdown-title">How the score is calculated</span>
        <span class="breakdown-total">{{ rules.filter(r => r.passed).length }} / {{ rules.length }} rules met</span>
      </div>
      <div v-for="rule in rules" :key="rule.name" class="rule-item">
        <span class="rule-check">{{ rule.passed ? '✅' : '❌' }}</span>
        <span class="rule-name">{{ rule.name }}</span>
        <span class="rule-points">+{{ rule.points }} pts</span>
        <span v-if="!rule.passed" class="rule-hint">(missing)</span>
      </div>
    </div>

    <!-- Fallback when no rules provided -->
    <div v-else-if="showBreakdown && !rules.length" class="score-breakdown">
      <div class="breakdown-header">
        <span class="breakdown-title">Score Details</span>
      </div>
      <div class="rule-item">
        <span class="rule-check">ℹ️</span>
        <span class="rule-name">No breakdown available</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface ClarityRule {
  name: string
  passed: boolean
  points: number
}

const props = withDefaults(defineProps<{
  score: number
  showBreakdown?: boolean
  rules?: ClarityRule[]
}>(), {
  showBreakdown: false,
  rules: () => []
})

const rules = computed(() => props.rules)

const scoreClass = computed(() => {
  const s = props.score
  if (s >= 80) return 'high'
  if (s >= 60) return 'medium'
  return 'low'
})
</script>

<style scoped>
.clarity-score {
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.25rem 1.5rem;
  border: 1px solid #e2e8f0;
}

/* ===== Header ===== */
.score-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.score-label-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.score-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
}

.score-badge {
  font-size: 1.1rem;
  font-weight: 700;
  padding: 0.1rem 0.75rem;
  border-radius: 9999px;
}
.score-badge.high { color: #16a34a; background: #dcfce7; }
.score-badge.medium { color: #d97706; background: #fef3c7; }
.score-badge.low { color: #dc2626; background: #fee2e2; }

.status-text {
  font-size: 0.8rem;
  font-weight: 500;
}
.status-text.good { color: #16a34a; }
.status-text.medium { color: #d97706; }
.status-text.poor { color: #dc2626; }

/* ===== Progress Bar ===== */
.score-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 9999px;
  overflow: hidden;
  margin: 0.25rem 0 0.5rem;
}

.score-fill {
  height: 100%;
  border-radius: 9999px;
  transition: width 0.6s ease;
}
.score-fill.high { background: linear-gradient(90deg, #4ade80, #22c55e); }
.score-fill.medium { background: linear-gradient(90deg, #fcd34d, #f59e0b); }
.score-fill.low { background: linear-gradient(90deg, #f87171, #ef4444); }

/* ===== Breakdown ===== */
.score-breakdown {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid #e2e8f0;
}

.breakdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.breakdown-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.breakdown-total {
  font-size: 0.75rem;
  font-weight: 500;
  color: #64748b;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.2rem 0;
  font-size: 0.85rem;
  color: #334155;
}

.rule-check {
  width: 1.5rem;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.rule-name {
  flex: 1;
}

.rule-points {
  font-weight: 500;
  color: #94a3b8;
}

.rule-hint {
  font-size: 0.7rem;
  color: #94a3b8;
  font-style: italic;
}
</style>