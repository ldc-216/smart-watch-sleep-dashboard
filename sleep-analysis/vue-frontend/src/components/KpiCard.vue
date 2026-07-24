<template>
  <div 
    class="kpi-card" 
    :class="`kpi-card--${color}`"
    ref="cardRef"
    @mousemove="handleMouseMove"
    @mouseleave="handleMouseLeave"
    :style="cardStyle"
  >
    <!-- Background Dots Grid -->
    <div class="dots-grid"></div>

    <div class="kpi-top">
      <span class="kpi-label">{{ label }}</span>
    </div>
    
    <div class="kpi-bottom">
      <span class="kpi-value">{{ displayValue }}</span>
      <span class="kpi-unit" v-if="unit">{{ unit }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'

const props = defineProps({
  label: String,
  value: [String, Number],
  unit: String,
  color: { type: String, default: 'amber' }
})



const cardRef = ref(null)
const tilt = reactive({ x: 0, y: 0, active: false })

function handleMouseMove(e) {
  if (!cardRef.value) return
  const card = cardRef.value
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const xc = rect.width / 2
  const yc = rect.height / 2
  tilt.x = (yc - y) / 5
  tilt.y = (x - xc) / 5
  tilt.active = true
}

function handleMouseLeave() {
  tilt.x = 0
  tilt.y = 0
  tilt.active = false
}

const accentColor = computed(() => {
  if (props.color === 'amber') return 'var(--accent-amber)'
  if (props.color === 'sky') return 'var(--accent-sky)'
  if (props.color === 'rose') return 'var(--accent-rose)'
  if (props.color === 'teal') return 'var(--accent-teal)'
  return 'var(--accent-amber)'
})

const glowColor = computed(() => {
  if (props.color === 'amber') return 'rgba(201, 151, 78, 0.18)'
  if (props.color === 'sky') return 'rgba(107, 168, 217, 0.18)'
  if (props.color === 'rose') return 'rgba(212, 133, 123, 0.18)'
  if (props.color === 'teal') return 'rgba(90, 171, 154, 0.18)'
  return 'rgba(201, 151, 78, 0.18)'
})

const accentRgb = computed(() => {
  if (props.color === 'amber') return '201, 151, 78'
  if (props.color === 'sky') return '107, 168, 217'
  if (props.color === 'rose') return '212, 133, 123'
  if (props.color === 'teal') return '90, 171, 154'
  return '201, 151, 78'
})

const cardStyle = computed(() => {
  return {
    '--color-accent': accentColor.value,
    '--color-glow': glowColor.value,
    background: `linear-gradient(135deg, rgba(${accentRgb.value}, 0.12) 0%, rgba(35, 40, 68, 0.45) 50%, rgba(20, 22, 38, 0.3) 100%)`,
    transform: tilt.active 
      ? `perspective(1000px) rotateX(${tilt.x}deg) rotateY(${tilt.y}deg) scale3d(1.03, 1.03, 1.03) translateY(-4px)` 
      : 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)',
    transition: tilt.active ? 'none' : 'all 0.5s cubic-bezier(0.16, 1, 0.3, 1)'
  }
})

const displayValue = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '--') return '--'
  
  // 如果是类似 "7h 18m" 这种已经格式化好的字符串，直接返回
  if (typeof props.value === 'string' && (props.value.includes('h') || props.value.includes('m'))) {
    return props.value
  }

  const n = Number(props.value)
  if (isNaN(n)) return props.value

  // 如果有小数，保留一位小数并千分位格式化
  if (n % 1 !== 0) {
    return n.toLocaleString('en-US', { minimumFractionDigits: 1, maximumFractionDigits: 1 })
  }
  // 整数千分位格式化
  return n.toLocaleString('en-US')
})
</script>

<style scoped>
.kpi-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.03) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-left: 3px solid var(--color-accent);
  border-radius: var(--radius-lg);
  min-height: 114px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  cursor: pointer;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  border-color: var(--color-accent);
  border-left-width: 5px;
  box-shadow: 
    0 12px 28px rgba(0, 0, 0, 0.5),
    0 0 20px var(--color-glow),
    inset 0 1px 1px rgba(255, 255, 255, 0.1);
}

/* Background Dots Grid */
.dots-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 10px 10px;
  background-position: center;
  pointer-events: none;
  z-index: 1;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.kpi-card:hover .dots-grid {
  background-image: radial-gradient(var(--color-accent) 1px, transparent 1px);
  opacity: 0.4;
}

/* Sci-Fi Corner Brackets */
.kpi-card::before {
  content: '';
  position: absolute;
  top: 8px;
  left: 8px;
  width: 8px;
  height: 8px;
  border-top: 1.5px solid var(--color-accent);
  border-left: 1.5px solid var(--color-accent);
  opacity: 0.35;
  transition: opacity 0.3s, transform 0.3s;
  z-index: 2;
}

.kpi-card::after {
  content: '';
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  border-bottom: 1.5px solid var(--color-accent);
  border-right: 1.5px solid var(--color-accent);
  opacity: 0.35;
  transition: opacity 0.3s, transform 0.3s;
  z-index: 2;
}

.kpi-card:hover::before {
  opacity: 0.95;
  transform: translate(-1.5px, -1.5px) scale(1.1);
}

.kpi-card:hover::after {
  opacity: 0.95;
  transform: translate(1.5px, 1.5px) scale(1.1);
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.kpi-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  letter-spacing: 0.02em;
}



.kpi-bottom {
  display: flex;
  align-items: baseline;
  gap: 4px;
  position: relative;
  z-index: 2;
  transform: translateZ(20px);
}

.kpi-value {
  font-family: var(--font-mono);
  font-size: 36px;
  font-weight: 600;
  line-height: 1;
  color: var(--color-accent);
  letter-spacing: -0.03em;
  font-feature-settings: "tnum";
  font-variant-numeric: tabular-nums;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.05), 0 2px 10px rgba(0, 0, 0, 0.5);
}

.kpi-unit {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-tertiary);
  letter-spacing: 0.01em;
}
</style>
