<template>
  <div ref="chartRef" class="echart-container" :style="{ height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
// echarts-gl 通过副作用导入自动注册 3D 系列（scatter3D / bar3D / line3D 等）
// 必须在 echarts.init 之前导入，否则 3D 组件找不到
import 'echarts-gl'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' }
})

const emit = defineEmits(['click'])

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

function render() {
  if (!chartInstance || !props.option) return
  chartInstance.setOption(props.option, { notMerge: true })
}

onMounted(async () => {
  await nextTick()
  chartInstance = echarts.init(chartRef.value, null, { renderer: 'canvas' })
  
  // 绑定并转发图表点击事件
  chartInstance.on('click', (params) => {
    emit('click', params)
  })

  render()
  resizeObserver = new ResizeObserver(() => {
    chartInstance && chartInstance.resize()
  })
  resizeObserver.observe(chartRef.value)
})

// deep watch：option 里任意字段变化都触发重绘
watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  resizeObserver && resizeObserver.disconnect()
  chartInstance && chartInstance.dispose()
  chartInstance = null
})
</script>

<style scoped>
.echart-container { width: 100%; }
</style>
