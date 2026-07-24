<template>
  <section
    class="panel-card"
    :class="{ 'panel-card--span': span }"
    :style="span ? { gridColumn: `span ${span}` } : {}"
  >
    <div class="panel-head" v-if="title">
      <h3 class="panel-title">{{ title }}</h3>
      <span v-if="sub" class="panel-sub">{{ sub }}</span>
    </div>
    <div class="panel-body">
      <div v-if="loading" class="state-loading">Loading··</div>
      <div v-else-if="empty" class="state-empty">No data</div>
      <slot v-else />
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  sub: { type: String, default: '' },
  span: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  empty: { type: Boolean, default: false },
})
</script>

<style scoped>
.panel-card {
  background: linear-gradient(135deg, rgba(35, 40, 68, 0.5) 0%, rgba(20, 22, 38, 0.35) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-lg);
  padding: 22px 24px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.panel-card:hover {
  border-color: rgba(255, 255, 255, 0.24);
  box-shadow: 
    0 16px 36px rgba(0, 0, 0, 0.45),
    inset 0 1px 2px rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
}

.panel-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(ellipse at 100% 0%, rgba(255,255,255,0.015) 0%, transparent 55%);
  pointer-events: none;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  gap: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.panel-sub {
  font-size: 11px;
  font-weight: 400;
  color: var(--text-tertiary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  white-space: nowrap;
}

.panel-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>
