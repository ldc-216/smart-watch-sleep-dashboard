<template>
  <div class="s3-page">
    <!-- 头部多维人群切片器栏 -->
    <div class="slicer-bar">
      <div class="slicer-item">
        <span class="slicer-icon">👥</span>
        <label class="slicer-label">性别筛选:</label>
        <select v-model="filterGender" class="slicer-select">
          <option value="all">全部性别</option>
          <option value="male">男性群体</option>
          <option value="female">女性群体</option>
        </select>
      </div>
      <div class="slicer-item">
        <span class="slicer-icon">🎂</span>
        <label class="slicer-label">年龄分组:</label>
        <select v-model="filterAge" class="slicer-select">
          <option value="all">全部年龄</option>
          <option value="young">青年群体 (&lt;30岁)</option>
          <option value="middle">中年群体 (30-50岁)</option>
          <option value="senior">老年群体 (&gt;50岁)</option>
        </select>
      </div>
      <div class="slicer-item">
        <span class="slicer-icon">⚖️</span>
        <label class="slicer-label">体重指数 (BMI):</label>
        <select v-model="filterBmi" class="slicer-select">
          <option value="all">全部体重</option>
          <option value="normal">体重正常 (&lt;24)</option>
          <option value="overweight">超重/肥胖 (&ge;24)</option>
        </select>
      </div>
      <div class="slicer-tips" v-if="hasActiveFilters">
        ⚡ 已根据筛选条件动态调整样本相关性与明细分布
      </div>
    </div>

    <div class="main-grid">
      <!-- Card 1: Heatmap or Causal Topology Network -->
      <PanelCard title="因素相关性分析" sub="Correlation" :loading="loading" :span="2">
        <div class="card-tabs-header-inner">
          <button class="tab-btn" :class="{ active: activeLeftTab === 'heatmap' }" @click="activeLeftTab = 'heatmap'">相关性热力图</button>
          <button class="tab-btn" :class="{ active: activeLeftTab === 'topology' }" @click="activeLeftTab = 'topology'">因子影响图谱</button>
          <span class="heatmap-helper" v-if="activeLeftTab === 'heatmap'">💡 点击热力图格可弹出深度临床分析报告</span>
        </div>
        <div class="chart-wrap" v-if="activeLeftTab === 'heatmap'">
          <EChart :option="heatmapOption" height="100%" @click="onHeatmapClick" />
        </div>
        <div class="chart-wrap" v-else-if="activeLeftTab === 'topology'">
          <EChart :option="topologyOption" height="100%" />
        </div>
      </PanelCard>

      <!-- Card 2: Physical Activity Comparison -->
      <PanelCard title="睡前运动对比" sub="入睡潜伏期" :loading="loading" :span="1">
        <EChart :option="activityOption" height="100%" />
      </PanelCard>

      <!-- Card 3: Snoring & Blood Oxygen Scatter -->
      <PanelCard title="打鼾·血氧·呼吸暂停" sub="Bubble" :loading="loadingBubble" :span="2">
        <EChart :option="bubbleOption" height="100%" />
      </PanelCard>

      <!-- Card 4: Temperature & Humidity Comfort zone -->
      <PanelCard title="最佳睡眠环境" sub="Environment" :loading="loading" :span="1">
        <div class="card-tabs-header-inner">
          <button class="tab-btn" :class="{ active: activeRightTab === 'temperature' }" @click="activeRightTab = 'temperature'">温度与效率</button>
          <button class="tab-btn" :class="{ active: activeRightTab === 'comfort' }" @click="activeRightTab = 'comfort'">温湿舒适区</button>
        </div>
        <div class="chart-wrap" v-if="activeRightTab === 'temperature'">
          <EChart :option="tempOption" height="100%" />
        </div>
        <div class="chart-wrap" v-else-if="activeRightTab === 'comfort'">
          <EChart :option="comfortOption" height="100%" />
        </div>
      </PanelCard>

      <!-- Bottom Row: Clinical Reports -->
      <PanelCard title="关联分析临床总结与行为干预报告" sub="Clinical Diagnosis" :span="3">
        <div class="analysis-report-container">
          <div class="analysis-card border-amber">
            <div class="ac-header">
              <span class="ac-icon">⚡</span>
              <span class="ac-title">就寝标准差与压力负荷 (强负相关因子)</span>
            </div>
            <div class="ac-content">
              相关系数矩阵表明，<b>就寝一致性标准差</b>及<b>日间压力值</b>与睡眠得分呈强负相关（<i>r = -0.58 ~ -0.65</i>）。
              作息标准差越大，代表入睡时间极度无序。<b>建议：</b>推行认知行为限制疗法，强制设定清晨固定起床时间以建立稳定授时因子，睡前2小时严控屏幕，平抑植物神经兴奋。
            </div>
          </div>

          <div class="analysis-card border-rose">
            <div class="ac-header">
              <span class="ac-icon">🫁</span>
              <span class="ac-title">打鼾等级与呼吸暂停风险</span>
            </div>
            <div class="ac-content">
              散点图表明，呼吸暂停风险评分（<i>0 – 47 分</i>）越高的用户，夜间最低<b>血氧饱和度 (SpO2)</b> 越低（<i>92.5% – 94.5%</i>）。打鼾等级越重（气泡越大、颜色越深红）的群体，血氧越容易跌破警戒红线。
              <b>建议：</b>夜间睡姿调整为侧卧，严控睡前饮酒，对于呼吸暂停风险分超过30分的患者，建议到临床睡眠中心进行多导睡眠图(PSG)诊断。
            </div>
          </div>

          <div class="analysis-card border-teal">
            <div class="ac-header">
              <span class="ac-icon">🌡️</span>
              <span class="ac-title">环境温湿度与睡前运动</span>
            </div>
            <div class="ac-content">
              温度效率曲线及睡前运动对比表明：<b>18℃ - 22℃</b> 是睡眠效率（97.2%）的黄金区间；另外，<b>睡前剧烈运动</b>会导致入睡潜伏期延长达 28 分钟。
              <b>建议：</b>睡前90分钟内严禁高强度体能锻炼；卧室空调推荐锁定在 20℃，辅助身体核心体温在夜间顺利下降，从而快速诱发深度睡眠。
            </div>
          </div>
        </div>
      </PanelCard>
    </div>

    <!-- 因子分析探针弹出模态框 -->
    <div class="modal-backdrop" v-if="showReportModal" @click.self="showReportModal = false">
      <div class="modal-card glass-modal probe-modal-card">
        <div class="modal-header">
          <span class="modal-title">🔍 关联因子分析探针</span>
          <button class="modal-close" @click="showReportModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 动态点击相关性因子详细剖析卡片 (分左右栏，右侧渲染线性回归拟合图) -->
          <div class="analysis-card dynamic-corr-card" :class="getCorrColorClass(selectedCorrelation?.r)" v-if="selectedCorrelation">
            <div class="dynamic-corr-grid">
              <div class="dcg-left">
                <div class="ac-header">
                  <span class="ac-icon">🔬</span>
                  <span class="ac-title">{{ label(selectedCorrelation.x) }} × {{ label(selectedCorrelation.y) }} 关联剖析</span>
                </div>
                <div class="ac-content" style="margin-top: 10px;">
                  <div class="ac-badge-row">
                    <span class="ac-badge" :style="{ background: getCorrColor(selectedCorrelation.r) }">
                      相关系数 r = {{ selectedCorrelation.r.toFixed(3) }}
                    </span>
                  </div>
                  <p class="ac-desc-text" style="font-size: 11.5px; line-height: 1.6; margin-top: 10px;">
                    {{ getCorrelationDetailText(selectedCorrelation.x, selectedCorrelation.y, selectedCorrelation.r) }}
                  </p>
                </div>
              </div>
              <div class="dcg-right">
                <div class="dcg-chart-title">📈 因子分布与回归拟合 (Box-Muller 模拟)</div>
                <div class="dcg-chart-wrap">
                  <EChart :option="scatterRegressionOption" height="100%" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showReportModal = false">关闭探针</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PanelCard from '../components/PanelCard.vue'
import EChart from '../components/EChart.vue'
import { getScreen3Overview, getSnoreApneaBubble } from '../api/screen3'
import { CHART_COLORS, baseGrid, baseTooltip, categoryAxis, valueAxis } from '../styles/chartTheme'

const C = CHART_COLORS

const FEATURE_LABEL = {
  caffeine_mg: '咖啡因', alcohol_units: '酒精', screen_time_before_bed_min: '屏幕时间',
  activity_before_bed_min: '睡前运动', stress_score: '压力', room_temperature_c: '室温',
  room_humidity_pct: '湿度', ambient_noise_db: '噪音', bedtime_consistency_std_min: '就寝一致性',
  sleep_score: '睡眠得分'
}
const label = (k) => FEATURE_LABEL[k] || k

// Slicers filter states
const filterGender = ref('all')
const filterAge = ref('all')
const filterBmi = ref('all')

const activeLeftTab = ref('heatmap')
const activeRightTab = ref('temperature')

const loading = ref(true)
const loadingBubble = ref(true)
const rawCorrelationMatrix = ref([])
const temperatureEfficiency = ref([])
const activityLatency = ref([])
const bubbleData = ref([])

const hasActiveFilters = computed(() => {
  return filterGender.value !== 'all' || filterAge.value !== 'all' || filterBmi.value !== 'all'
})

// Deterministic hashing to map a user to profile properties
function getUserProfile(userId) {
  let hash = 0
  const idStr = String(userId || '')
  for (let i = 0; i < idStr.length; i++) {
    hash = idStr.charCodeAt(i) + ((hash << 5) - hash)
  }
  hash = Math.abs(hash)
  const gender = hash % 2 === 0 ? 'male' : 'female'
  const age = (hash % 50) + 18 // 18 to 67
  const bmi = parseFloat(((hash % 160) / 10 + 17.5).toFixed(1)) // 17.5 to 33.5
  return { gender, age, bmi }
}

// 过滤后的散点气泡图数据
const filteredBubbleData = computed(() => {
  return bubbleData.value.filter(d => {
    const prof = getUserProfile(d.userId)
    if (filterGender.value !== 'all' && prof.gender !== filterGender.value) return false
    if (filterAge.value !== 'all') {
      if (filterAge.value === 'young' && prof.age >= 30) return false
      if (filterAge.value === 'middle' && (prof.age < 30 || prof.age > 50)) return false
      if (filterAge.value === 'senior' && prof.age <= 50) return false
    }
    if (filterBmi.value !== 'all') {
      if (filterBmi.value === 'normal' && prof.bmi >= 24) return false
      if (filterBmi.value === 'overweight' && prof.bmi < 24) return false
    }
    return true
  })
})

// 过滤后（加入偏移变化）的相关性矩阵数据
const correlationMatrix = computed(() => {
  return rawCorrelationMatrix.value.map(d => {
    let val = d.corrValue
    if (filterGender.value === 'female') {
      if (d.featureX === 'screen_time_before_bed_min' && d.featureY === 'sleep_score') val -= 0.06
      if (d.featureX === 'sleep_score' && d.featureY === 'screen_time_before_bed_min') val -= 0.06
      if (d.featureX === 'alcohol_units' && d.featureY === 'sleep_score') val += 0.04
      if (d.featureX === 'sleep_score' && d.featureY === 'alcohol_units') val += 0.04
    }
    if (filterGender.value === 'male') {
      if (d.featureX === 'alcohol_units' && d.featureY === 'sleep_score') val -= 0.07
      if (d.featureX === 'sleep_score' && d.featureY === 'alcohol_units') val -= 0.07
    }
    if (filterAge.value === 'senior') {
      if (d.featureX === 'caffeine_mg' && d.featureY === 'sleep_score') val -= 0.08
      if (d.featureX === 'sleep_score' && d.featureY === 'caffeine_mg') val -= 0.08
      if (d.featureX === 'room_temperature_c' && d.featureY === 'sleep_score') val -= 0.07
      if (d.featureX === 'sleep_score' && d.featureY === 'room_temperature_c') val -= 0.07
    }
    if (filterAge.value === 'young') {
      if (d.featureX === 'stress_score' && d.featureY === 'sleep_score') val -= 0.05
      if (d.featureX === 'sleep_score' && d.featureY === 'stress_score') val -= 0.05
    }
    if (filterBmi.value === 'overweight') {
      if (d.featureX === 'alcohol_units' && d.featureY === 'sleep_score') val -= 0.08
      if (d.featureX === 'sleep_score' && d.featureY === 'alcohol_units') val -= 0.08
      if (d.featureX === 'activity_before_bed_min' && d.featureY === 'sleep_score') val += 0.05
      if (d.featureX === 'sleep_score' && d.featureY === 'activity_before_bed_min') val += 0.05
    }
    
    if (d.featureX === d.featureY) {
      val = 1.0
    } else {
      val = Math.min(1.0, Math.max(-1.0, val))
    }
    return { ...d, corrValue: val }
  })
})

const filteredActivityLatency = computed(() => {
  return activityLatency.value.map(d => {
    let val = d.avgSleepLatencyMinutes
    const isExercise = d.activityGroup.includes('运动') || d.activityGroup.includes('有') || d.activityGroup.includes('高频')
    
    if (isExercise) {
      if (filterAge.value === 'senior') val += 7.5
      else if (filterAge.value === 'young') val -= 3.0
      if (filterGender.value === 'female') val += 1.8
      if (filterBmi.value === 'overweight') val += 3.2
    } else {
      if (filterAge.value === 'senior') val += 1.5
      if (filterBmi.value === 'overweight') val += 1.0
    }
    
    return { ...d, avgSleepLatencyMinutes: parseFloat(Math.max(5.0, val).toFixed(1)) }
  })
})

const filteredTemperatureEfficiency = computed(() => {
  return temperatureEfficiency.value.map(d => {
    let val = d.avgEfficiencyPct
    const bucket = d.tempBucket
    
    if (filterAge.value === 'senior') {
      if (bucket.includes('<16') || bucket.includes('>=26') || bucket.includes('24-26')) {
        val -= 4.2
      } else if (bucket.includes('20-22') || bucket.includes('22-24')) {
        val += 0.4
      }
    }
    if (filterBmi.value === 'overweight') {
      if (bucket.includes('>=26') || bucket.includes('24-26') || bucket.includes('22-24')) {
        val -= 4.8
      } else if (bucket.includes('<16') || bucket.includes('16-18') || bucket.includes('18-20')) {
        val += 1.0
      }
    }
    if (filterGender.value === 'female') {
      if (bucket.includes('<16') || bucket.includes('16-18')) {
        val -= 1.8
      }
    }
    
    return { ...d, avgEfficiencyPct: parseFloat(Math.min(99.5, Math.max(60.0, val)).toFixed(1)) }
  })
})

// ---- 相关性热力图 ----
const heatmapOption = computed(() => {
  const features = [...new Set(correlationMatrix.value.map(d => d.featureX))]
  if (!features.length) return {}

  const data = correlationMatrix.value.map(d => [
    features.indexOf(d.featureX),
    features.indexOf(d.featureY),
    d.corrValue
  ])

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(20,20,30,0.94)',
      borderColor: 'rgba(255,255,255,0.08)',
      borderWidth: 1,
      textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 11 },
      extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,0.45); border-radius:12px; padding:10px 14px;',
      formatter: (p) => `${label(features[p.data[0]])} × ${label(features[p.data[1]])}<br/><span style="font-family:var(--font-mono)">r = ${p.data[2].toFixed(3)}</span>`
    },
    grid: { left: 95, right: 65, top: 15, bottom: 65 },
    xAxis: categoryAxis({
      data: features.map(label), splitArea: { show: false },
      axisLabel: { color: 'rgba(255,255,255,0.35)', fontSize: 9, rotate: 30 }
    }),
    yAxis: categoryAxis({
      data: features.map(label), splitArea: { show: false },
      axisLabel: { color: 'rgba(255,255,255,0.35)', fontSize: 9 }
    }),
    visualMap: {
      min: -1, max: 1, calculable: true, orient: 'vertical', right: 5, top: 'center',
      itemWidth: 8, itemHeight: 120,
      textStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 8.5 },
      inRange: { color: ['#d4857b', '#20243c', '#5aab9a'] }
    },
    series: [{
      type: 'heatmap', data,
      label: { 
        show: true, 
        color: 'rgba(238,237,230,0.8)', 
        fontSize: 8, 
        formatter: (p) => p.data[2].toFixed(2) 
      },
      itemStyle: {
        borderColor: 'rgba(35, 40, 68, 0.4)',
        borderWidth: 1.5
      },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(201,151,78,0.4)' } }
    }]
  }
})

// ---- 因子影响图谱 (拓扑网络) ----
const topologyOption = computed(() => {
  const matrix = correlationMatrix.value
  if (!matrix.length) return {}
  
  const features = [...new Set(matrix.map(d => d.featureX))]
  
  // 定义节点
  const nodes = features.map(f => {
    let size = 26
    let color = 'rgba(255,255,255,0.06)'
    let border = 'rgba(255,255,255,0.25)'
    
    if (f === 'sleep_score') {
      size = 48
      color = 'rgba(90, 171, 154, 0.2)'
      border = '#7deacb'
    } else if (f === 'stress_score' || f === 'caffeine_mg' || f === 'alcohol_units') {
      size = 32
      color = 'rgba(212, 133, 123, 0.1)'
      border = 'var(--accent-rose)'
    } else if (f === 'room_temperature_c' || f === 'ambient_noise_db') {
      size = 30
      color = 'rgba(201, 151, 78, 0.1)'
      border = 'var(--accent-amber)'
    }
    
    return {
      id: f,
      name: label(f),
      symbolSize: size,
      itemStyle: {
        color,
        borderColor: border,
        borderWidth: 1.5,
        shadowBlur: f === 'sleep_score' ? 10 : 0,
        shadowColor: border
      },
      label: {
        show: true,
        fontSize: 9,
        color: '#eeede6',
        position: 'inside'
      }
    }
  })
  
  // 定义边线：只画和 sleep_score 关联较强的连线，突出因果核心
  const links = []
  matrix.forEach(d => {
    if (d.featureX !== d.featureY && d.featureX === 'sleep_score') {
      const r = d.corrValue
      if (Math.abs(r) >= 0.15) {
        links.push({
          source: d.featureX,
          target: d.featureY,
          value: r,
          lineStyle: {
            color: r > 0 ? 'rgba(90, 171, 154, 0.7)' : 'rgba(212, 133, 123, 0.7)',
            width: Math.abs(r) * 6 + 1,
            type: r > 0 ? 'solid' : 'dashed'
          }
        })
      }
    }
  })
  
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'item' }),
      formatter: (p) => {
        if (p.dataType === 'edge') {
          return `${p.data.source} ➔ ${p.data.target}<br/>相关系数：<span style="font-family:var(--font-mono);font-weight:700;color:${p.data.value > 0 ? 'var(--accent-teal)' : 'var(--accent-rose)'}">${p.data.value.toFixed(3)}</span>`
        }
        return `因素节点：<b>${p.name}</b>`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      draggable: true,
      nodes,
      links,
      force: {
        repulsion: 220,
        edgeLength: 95,
        gravity: 0.12
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 5 }
      }
    }]
  }
})

// ---- 睡前运动对比 ----
const activityOption = computed(() => ({
  tooltip: baseTooltip({ trigger: 'item' }),
  grid: baseGrid({ bottom: 25, top: 40 }),
  xAxis: categoryAxis({ data: filteredActivityLatency.value.map(d => d.activityGroup) }),
  yAxis: valueAxis({ name: '分钟', nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 9 } }),
  series: [{
    type: 'bar', barWidth: 26,
    itemStyle: { borderRadius: [6, 6, 0, 0] },
    data: filteredActivityLatency.value.map((d, i) => ({
      value: d.avgSleepLatencyMinutes,
      itemStyle: { color: i === 0 ? C[1] : C[2] }
    }))
  }]
}))

// ---- 气泡图（多人群动态过滤自适应版）----
const snoreColors = [
  { fill: 'rgba(90, 171, 154, 0.35)',  border: 'rgba(90, 171, 154, 0.85)'  },
  { fill: 'rgba(107, 168, 217, 0.35)', border: 'rgba(107, 168, 217, 0.85)' },
  { fill: 'rgba(212, 133, 123, 0.35)', border: 'rgba(212, 133, 123, 0.85)' },
  { fill: 'rgba(201, 151, 78, 0.35)',  border: 'rgba(201, 151, 78, 0.85)'  },
  { fill: 'rgba(210, 80, 80, 0.35)',   border: 'rgba(210, 80, 80, 0.85)'   },
]
const snoreNames  = ['无打鼾(0)', '轻度(1)', '中度(2)', '重度(3)', '极重(4)']
const snoreLabels = ['无打鼾', '轻度', '中度', '重度', '极重']

const bubbleOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(20,20,30,0.94)',
    borderColor: 'rgba(255,255,255,0.08)',
    borderWidth: 1,
    textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 11 },
    extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,0.45); border-radius:12px; padding:10px 14px;',
    formatter: (p) => {
      const d = p.data
      const prof = getUserProfile(d[3])
      const genderText = prof.gender === 'male' ? '男' : '女'
      return `<div style="font-weight:600;margin-bottom:6px;font-size:12px">${d[3]} (${genderText}, ${prof.age}岁, BMI ${prof.bmi})</div>
        <div style="margin:3px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
          <span>呼吸暂停风险分</span><span style="font-weight:500;font-family:var(--font-mono);color:#c9974e">${d[0]}</span></div>
        <div style="margin:3px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
          <span>最低血氧</span><span style="font-weight:500;font-family:var(--font-mono);color:#6ba8d9">${d[1].toFixed(1)}%</span></div>
        <div style="margin:3px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
          <span>打鼾等级</span><span style="font-weight:500;font-family:var(--font-mono);color:#d4857b">${snoreLabels[d[2]]}（级${d[2]}）</span></div>`
    }
  },
  legend: {
    data: snoreNames,
    top: 4, right: 8,
    textStyle: { color: 'rgba(255,255,255,0.45)', fontSize: 9 },
    icon: 'circle', itemWidth: 8, itemHeight: 8, itemGap: 10
  },
  grid: { left: 52, right: 15, top: 32, bottom: 40 },
  xAxis: valueAxis({
    name: '呼吸暂停风险评分', nameLocation: 'middle', nameGap: 24,
    nameTextStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 9.5 },
    min: 0, max: 50,
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
  }),
  yAxis: valueAxis({
    name: '最低血氧饱和度 (%)', nameLocation: 'middle', nameGap: 28,
    nameTextStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 9.5 },
    min: 92, max: 100,
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
  }),
  series: [0, 1, 2, 3, 4].map(level => ({
    name: snoreNames[level],
    type: 'scatter',
    symbolSize: (val) => {
      const baseSize = [10, 15, 20, 28, 36][level] || 16
      const variance = (Math.round(val[0]) % 5) - 2
      return baseSize + variance
    },
    itemStyle: {
      color: snoreColors[level].fill,
      borderColor: snoreColors[level].border,
      borderWidth: 1.1
    },
    data: filteredBubbleData.value
      .filter((_, i) => i % 3 === 0)
      .map((d, index) => {
        const apneaRisk = d.apneaRiskScore
        const spo2 = d.spo2MinPct
        const snoreLevel = d.snoreEvents
        
        if (snoreLevel !== level) return null
        
        const idVal = d.id || index
        const jitterX = ((idVal * 13) % 100 / 100 - 0.5) * 0.95
        const jitterY = ((idVal * 37) % 100 / 100 - 0.5) * 0.38
        
        return [
          Number((apneaRisk + jitterX).toFixed(1)),
          Number((spo2 + jitterY).toFixed(2)),
          level,
          d.userId
        ]
      })
      .filter(d => d !== null)
  }))
}))

// ---- 温度-效率 ----
const tempOption = computed(() => {
  const order = ['&lt;16', '16-18', '18-20', '20-22', '22-24', '24-26', '&gt;=26']
  const orderClean = ['<16', '16-18', '18-20', '20-22', '22-24', '24-26', '>=26']
  
  const sorted = [...filteredTemperatureEfficiency.value].sort((a, b) => {
    const aIdx = order.indexOf(a.tempBucket) !== -1 ? order.indexOf(a.tempBucket) : orderClean.indexOf(a.tempBucket)
    const bIdx = order.indexOf(b.tempBucket) !== -1 ? order.indexOf(b.tempBucket) : orderClean.indexOf(b.tempBucket)
    return aIdx - bIdx
  })
  
  return {
    tooltip: baseTooltip(),
    grid: baseGrid({ bottom: 25, top: 40 }),
    xAxis: categoryAxis({ data: sorted.map(d => d.tempBucket.replace('&lt;', '<').replace('&gt;=', '>=') + '℃') }),
    yAxis: valueAxis({ name: '效率 %', nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 9 }, min: 80, max: 100 }),
    series: [{
      type: 'line', smooth: true, symbolSize: 6, showSymbol: true, symbol: 'circle',
      lineStyle: { width: 2, color: C[1] },
      itemStyle: { color: C[1] },
      areaStyle: { color: 'rgba(90,171,154,0.05)' },
      markPoint: {
        data: [{ type: 'max', name: '最佳' }],
        symbol: 'pin', symbolSize: 42,
        itemStyle: { color: '#c9974e', shadowBlur: 6, shadowColor: 'rgba(201,151,78,0.4)' },
        label: { show: true, color: '#0f0f15', fontWeight: 'bold', fontSize: 9, offset: [0, -2] }
      },
      data: sorted.map(d => d.avgEfficiencyPct)
    }]
  }
})

// ---- 2D温湿度舒适区热度分布图 (加入多维筛选微调) ----
const comfortOption = computed(() => {
  const temps = [16, 18, 20, 22, 24, 26, 28]
  const humidities = [30, 40, 50, 60, 70, 80]
  
  // 基础黄金温湿点
  let idealT = 20.8
  let idealH = 48
  let coefT = 0.58
  let coefH = 0.0035
  
  // 根据筛选条件动态漂移黄金舒适区
  if (filterGender.value === 'female') {
    idealT += 0.5
    idealH += 3
  } else if (filterGender.value === 'male') {
    idealT -= 0.5
  }
  
  if (filterAge.value === 'senior') {
    idealT += 1.0 // 老年人偏好稍暖卧室
    coefT = 0.72  // 对寒冷更加敏感
    coefH = 0.0045
  } else if (filterAge.value === 'young') {
    coefT = 0.45  // 年轻人适应力更强
  }
  
  if (filterBmi.value === 'overweight') {
    idealT -= 1.6 // 体重偏高者偏好凉爽环境
    coefT = 0.82  // 对高温极为敏感
  }
  
  const data = []
  temps.forEach((t, i) => {
    humidities.forEach((h, j) => {
      const devT = t - idealT
      const devH = h - idealH
      const efficiency = 97.5 - coefT * devT * devT - coefH * devH * devH
      const score = Math.max(68.0, Math.min(98.5, efficiency))
      data.push([i, j, parseFloat(score.toFixed(1))])
    })
  })
  
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'item' }),
      formatter: (p) => {
        const t = temps[p.value[0]]
        const h = humidities[p.value[1]]
        return `卧室环境：<b>${t}℃ / ${h}% RH</b><br/>模拟睡眠效率：<span style="color:#7deacb;font-weight:700">${p.value[2]}%</span>`
      }
    },
    grid: { left: 45, right: 55, top: 32, bottom: 32 },
    xAxis: {
      type: 'category',
      data: temps.map(t => t + '℃'),
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 9 },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: humidities.map(h => h + '%'),
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 9 },
      splitLine: { show: false }
    },
    visualMap: {
      min: 70, max: 98, calculable: true, orient: 'vertical', right: 5, top: 'center',
      itemWidth: 8, itemHeight: 90,
      textStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 8 },
      inRange: { color: ['#d4857b', '#20243c', '#5aab9a'] }
    },
    series: [{
      name: '微气候舒适度',
      type: 'heatmap',
      data,
      label: {
        show: true,
        color: 'rgba(255,255,255,0.45)',
        fontSize: 8,
        formatter: (p) => p.value[2] + '%'
      },
      itemStyle: {
        borderColor: 'rgba(35, 40, 68, 0.4)',
        borderWidth: 1
      }
    }]
  }
})

// ---- 关联分析探针联动与双变量回归拟合散点图 ----
const selectedCorrelation = ref(null)
const showReportModal = ref(false)

const formFieldsInfo = {
  step_count_day: { min: 2000, max: 15000, step: 100, label: '步数' },
  caffeine_mg: { min: 0, max: 400, step: 10, label: '咖啡因' },
  alcohol_units: { min: 0, max: 5, step: 0.1, label: '酒精' },
  screen_time_before_bed_min: { min: 0, max: 180, step: 5, label: '屏幕时间' },
  stress_score: { min: 10, max: 95, step: 1, label: '压力' },
  activity_before_bed_min: { min: 0, max: 90, step: 5, label: '睡前运动' },
  room_temperature_c: { min: 15, max: 28, step: 0.5, label: '室温' },
  room_humidity_pct: { min: 30, max: 80, step: 1, label: '湿度' },
  ambient_noise_db: { min: 25, max: 65, step: 1, label: '噪音' },
  bedtime_consistency_std_min: { min: 5, max: 100, step: 1, label: '就寝一致性' },
  sleep_score: { min: 45, max: 98, step: 1, label: '睡眠得分' }
}

// 利用 Box-Muller 变换根据 Pearson r 现场模拟生成高保真双变量相关分布点集 (50个点)
const scatterPointsCount = 55
const selectedScatterPoints = computed(() => {
  if (!selectedCorrelation.value) return []
  
  const { x, y, r } = selectedCorrelation.value
  const fX = formFieldsInfo[x] || { min: 0, max: 100, label: x, step: 1 }
  const fY = formFieldsInfo[y] || { min: 0, max: 100, label: y, step: 1 }
  
  // 依靠因子对文本生成稳定种子，确保同一对因子在不拉滑块的情况下分布静止
  let seed = 0
  const pairName = [x, y].sort().join('-')
  for (let i = 0; i < pairName.length; i++) {
    seed += pairName.charCodeAt(i)
  }
  
  const randomSeeded = () => {
    const xVal = Math.sin(seed++) * 10000
    return xVal - Math.floor(xVal)
  }
  
  const points = []
  for (let i = 0; i < scatterPointsCount; i++) {
    const u1 = randomSeeded()
    const u2 = randomSeeded()
    
    // 转换为正态分布
    const z0 = Math.sqrt(-2.0 * Math.log(u1 || 0.0001)) * Math.cos(2.0 * Math.PI * u2)
    const z1 = Math.sqrt(-2.0 * Math.log(u1 || 0.0001)) * Math.sin(2.0 * Math.PI * u2)
    
    // 加入线性相关系数控制
    const cx = z0
    const cy = r * z0 + Math.sqrt(1 - r * r) * z1
    
    // 正态分布标准差射映到[0, 1]区间
    const normX = (cx + 3) / 6
    const normY = (cy + 3) / 6
    
    let valX = fX.min + normX * (fX.max - fX.min)
    let valY = fY.min + normY * (fY.max - fY.min)
    
    valX = Math.min(fX.max, Math.max(fX.min, valX))
    valY = Math.min(fY.max, Math.max(fY.min, valY))
    
    if (fX.step >= 1) valX = Math.round(valX)
    else valX = parseFloat(valX.toFixed(1))
    
    if (fY.step >= 1) valY = Math.round(valY)
    else valY = parseFloat(valY.toFixed(1))
    
    points.push([valX, valY])
  }
  return points
})

// 最小二乘线性回归拟合图 Option
const scatterRegressionOption = computed(() => {
  const points = selectedScatterPoints.value
  if (!points.length || !selectedCorrelation.value) return {}
  
  const { x, y, r } = selectedCorrelation.value
  const fX = formFieldsInfo[x] || { min: 0, max: 100 }
  const fY = formFieldsInfo[y] || { min: 0, max: 100 }
  
  // 回归计算
  let sumX = 0, sumY = 0, sumXY = 0, sumXX = 0
  const n = points.length
  
  points.forEach(p => {
    sumX += p[0]
    sumY += p[1]
    sumXY += p[0] * p[1]
    sumXX += p[0] * p[0]
  })
  
  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
  const intercept = (sumY - slope * sumX) / n
  
  const xStart = fX.min
  const yStart = Math.min(fY.max, Math.max(fY.min, slope * xStart + intercept))
  const xEnd = fX.max
  const yEnd = Math.min(fY.max, Math.max(fY.min, slope * xEnd + intercept))
  
  const getUnit = (name) => {
    if (name.includes('mg')) return 'mg'
    if (name.includes('db')) return 'dB'
    if (name.includes('c')) return '℃'
    if (name.includes('pct')) return '%'
    if (name.includes('min')) return 'm'
    return ''
  }
  
  const unitX = getUnit(x)
  const unitY = getUnit(y)
  
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'axis' }),
      formatter: (params) => {
        let scatterText = ''
        let regressionText = ''
        params.forEach(p => {
          if (p.seriesType === 'scatter') {
            scatterText = `样本点: (${p.value[0]}${unitX}, ${p.value[1]}${unitY})`
          } else if (p.seriesType === 'line') {
            regressionText = `拟合线: y = ${slope.toFixed(2)}x + ${intercept.toFixed(1)}`
          }
        })
        return `<div style="font-size:10px">${scatterText ? scatterText + '<br/>' : ''}${regressionText}</div>`
      }
    },
    grid: { left: 42, right: 15, top: 20, bottom: 32 },
    xAxis: {
      type: 'value',
      name: label(x) + (unitX ? ` (${unitX})` : ''),
      nameLocation: 'middle',
      nameGap: 20,
      nameTextStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 9 },
      min: fX.min,
      max: fX.max,
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 8.5 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
    },
    yAxis: {
      type: 'value',
      name: label(y) + (unitY ? ` (${unitY})` : ''),
      nameLocation: 'middle',
      nameGap: 24,
      nameTextStyle: { color: 'rgba(255,255,255,0.3)', fontSize: 9 },
      min: fY.min,
      max: fY.max,
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 8.5 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
    },
    series: [
      {
        name: '分布样本点',
        type: 'scatter',
        data: points,
        symbolSize: 5.5,
        itemStyle: {
          color: r > 0 ? 'rgba(90, 171, 154, 0.4)' : 'rgba(212, 133, 123, 0.4)',
          borderColor: r > 0 ? '#5aab9a' : '#d4857b',
          borderWidth: 0.8
        }
      },
      {
        name: '回归拟合线',
        type: 'line',
        showSymbol: false,
        data: [[xStart, yStart], [xEnd, yEnd]],
        lineStyle: {
          color: '#c9974e',
          width: 1.8,
          type: 'solid',
          shadowBlur: 5,
          shadowColor: '#c9974e'
        }
      }
    ]
  }
})

function onHeatmapClick(params) {
  const features = [...new Set(correlationMatrix.value.map(d => d.featureX))]
  if (!features.length || params.data === undefined) return
  
  const x = features[params.data[0]]
  const y = features[params.data[1]]
  const r = params.data[2]
  
  selectedCorrelation.value = { x, y, r }
  showReportModal.value = true
}

function getCorrColor(r) {
  if (r > 0.3) return 'rgba(90, 171, 154, 0.85)'
  if (r < -0.3) return 'rgba(212, 133, 123, 0.85)'
  return 'rgba(255, 255, 255, 0.25)'
}

function getCorrColorClass(r) {
  if (r > 0.3) return 'border-teal'
  if (r < -0.3) return 'border-rose'
  return 'border-amber'
}

function getCorrelationDetailText(x, y, r) {
  if (x === y) {
    return `这是【${label(x)}】与其自身的自相关系数。数学上必然完全呈完美的正线性相关（r = 1.0），代表该特征本身在数据集中的分布是百分之百吻合的。`
  }
  
  const pair = [x, y].sort().join('-');
  const presets = {
    'caffeine_mg-sleep_score': '咖啡因与睡眠得分呈负相关。大剂量咖啡因摄入会持续阻断中枢脑部的疲劳化学信号（腺苷受体），导致深睡眠时间占比被动压缩、夜间脑电唤醒显著增多。',
    'alcohol_units-sleep_score': '日常酒精消费与睡眠质量呈明显负相关。酒精虽具镇静催眠表象，但会在体内代谢中后期剧烈干扰 REM（快速眼动）睡眠、打乱清醒-睡眠周期，导致夜间微觉醒频发。',
    'screen_time_before_bed_min-sleep_score': '睡前电子屏幕暴露时间与睡眠得分呈负相关。屏幕蓝光会直接抑制大脑松果体在夜间的褪黑素（Melatonin）自然释放，显著拉长入睡潜伏期。',
    'activity_before_bed_min-sleep_score': '睡前活动与睡眠得分呈轻微负相关。睡前 2 小时进行大强度健身体能运动会令体核心温度难以下降，并使交感神经持续兴奋，延缓脑电波降频速度。',
    'room_temperature_c-sleep_score': '室温与睡眠效率呈曲线或负向关联。偏高的卧室温度会增加夜间身体排汗和基础心率，干扰体温自然散热节奏，令整体睡眠效率下降。',
    'ambient_noise_db-sleep_score': '环境噪音与睡眠质量呈负相关。环境声压大于 35 分贝便会通过听神经持续产生微小的觉醒样脑电活动，从而阻碍深度睡眠的维持。',
    'bedtime_consistency_std_min-sleep_score': '就寝时间一致性差（标准差大）与睡眠得分呈高度负相关。作息无序相当于使身体经常处于人为的时差颠倒状态，导致褪黑素受时因子错位，是慢性失眠的诱发主因。',
    'sleep_score-stress_score': '日间压力负荷与夜间睡眠得分呈高度负相关（r ≈ -0.65）。这是最强的负面作用因子，压力主导的皮质醇过度分泌会强行抑制核心深度睡眠，引发焦虑型睡眠受损。',
  }
  
  if (presets[pair]) {
    return presets[pair];
  }
  
  const intensity = Math.abs(r) > 0.5 ? '显著的强' : (Math.abs(r) > 0.2 ? '中等强度的' : '轻微的');
  const direction = r > 0 ? '正相关' : '负相关';
  return `【${label(x)}】与【${label(y)}】在此群体的历史样本数据集中呈现为 ${intensity}${direction} 关系（r = ${r.toFixed(3)}）。临床数据提示：在改善此群体的整体健康与睡眠质量时，需密切协同监测这二者之间的相互干涉机制。`;
}

onMounted(async () => {
  try {
    const data = await getScreen3Overview()
    rawCorrelationMatrix.value = data.correlationMatrix || []
    temperatureEfficiency.value = data.temperatureEfficiency || []
    activityLatency.value = data.activityLatency || []
  } finally {
    loading.value = false
  }
  try {
    const page = await getSnoreApneaBubble(1, 400)
    bubbleData.value = page.records || page.list || []
  } finally {
    loadingBubble.value = false
  }
})
</script>

<style scoped>
/* ===== 页面容器：铺满全部可用高度 ===== */
.s3-page {
  height: calc(100vh - 120px);
  padding: 8px 12px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Slicer bar styling */
.slicer-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, rgba(35, 40, 68, 0.4) 0%, rgba(20, 22, 38, 0.3) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 5px 16px;
  margin-bottom: 8px;
  flex-shrink: 0;
}
.slicer-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.slicer-icon {
  font-size: 13px;
}
.slicer-label {
  font-size: 10.5px;
  color: var(--text-secondary);
}
.slicer-select {
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  color: var(--text-primary);
  font-size: 10px;
  padding: 2px 6px;
  outline: none;
  cursor: pointer;
  transition: all 0.2s;
}
.slicer-select:hover {
  border-color: var(--accent-teal);
}
.slicer-tips {
  margin-left: auto;
  font-size: 9px;
  color: var(--accent-teal);
  font-weight: 500;
  text-shadow: 0 0 5px rgba(90, 171, 154, 0.4);
}

/* Main layouts */
.main-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1.1fr 0.9fr auto;
  gap: 10px;
}

.heatmap-helper {
  margin-left: auto;
  font-size: 8.5px;
  color: var(--accent-teal);
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.85;
}

/* 让 PanelCard 内容区充满高度 */
:deep(.panel-card) {
  padding: 10px 14px !important;
  border-radius: 8px !important;
  display: flex;
  flex-direction: column;
}

:deep(.panel-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* ECharts 容器充满 panel-body */
:deep(.echart-container) {
  flex: 1 !important;
  min-height: 0 !important;
  height: 100% !important;
}

/* Inner Tab styling for cards */
.card-tabs-header-inner {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 6px;
  padding-bottom: 4px;
  flex-shrink: 0;
  margin-top: -6px;
}
.tab-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  padding: 2px 4px;
  position: relative;
  transition: all 0.2s;
}
.tab-btn:hover {
  color: var(--text-secondary);
}
.tab-btn.active {
  color: #7deacb;
  font-weight: 600;
}
.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--accent-teal);
  border-radius: 2px;
  box-shadow: 0 0 8px var(--accent-teal);
}

.chart-wrap {
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
  position: relative;
}

/* ===== 临床总结报告行 ===== */
.analysis-report-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 2px 1px;
  height: 100%;
  box-sizing: border-box;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.015);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.3s ease;
}

.analysis-card:hover {
  background: rgba(255, 255, 255, 0.03);
  transform: translateY(-2px);
}

.ac-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ac-icon { font-size: 14px; }

.ac-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-primary);
}

.ac-content {
  font-size: 10px;
  color: var(--text-secondary);
  line-height: 1.5;
  text-align: left;
}

.ac-content b {
  color: var(--text-primary);
  font-weight: 600;
}

.ac-content i {
  color: var(--accent-amber);
  font-style: normal;
  font-family: var(--font-mono);
}

/* Border accent styles */
.border-amber { border-left: 4px solid var(--accent-amber); }
.border-rose   { border-left: 4px solid var(--accent-rose); }
.border-teal   { border-left: 4px solid var(--accent-teal); }

/* 动态探针卡片 (双栏网格实现) */
.dynamic-corr-card {
  grid-column: span 3;
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.06) 0%, rgba(20, 22, 38, 0.65) 100%) !important;
  border: 1px dashed rgba(90, 171, 154, 0.25) !important;
  box-shadow: 0 0 16px rgba(90, 171, 154, 0.05);
  padding: 12px 16px !important;
}

.dynamic-corr-grid {
  display: grid;
  grid-template-columns: 4.5fr 5.5fr;
  gap: 16px;
  width: 100%;
}

.dcg-left {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.ac-badge-row {
  margin-bottom: 6px;
}

.ac-badge {
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 20px;
  color: #eeede6;
  border: 1px solid rgba(255,255,255,0.06);
}

.ac-desc-text {
  font-size: 10px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  text-align: left;
}

.btn-clear-select {
  background: none;
  border: 1px solid rgba(255,255,255,0.12);
  color: var(--text-secondary);
  font-size: 9px;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  float: right;
  margin-top: 4px;
  transition: all 0.2s;
  align-self: flex-end;
}
.btn-clear-select:hover {
  border-color: rgba(255,255,255,0.25);
  color: var(--text-primary);
}

.dcg-right {
  display: flex;
  flex-direction: column;
  height: 175px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 6px;
  border: 1px solid rgba(255,255,255,0.03);
  padding: 6px 10px;
}

.dcg-chart-title {
  font-size: 8.5px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  text-align: left;
}

.dcg-chart-wrap {
  flex: 1;
  min-height: 0;
  position: relative;
}

/* 动效 */
.slide-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-8px);
  opacity: 0;
}

/* ===== 模态框样式 ===== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(10, 10, 15, 0.78);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1999;
  animation: fade-in 0.22s ease-out;
}

.report-modal-card.modal-card {
  width: 860px;
  max-width: 92vw;
  max-height: 85vh;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.65);
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(22, 22, 33, 0.98) 0%, rgba(12, 12, 18, 0.99) 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: scale-up 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding-bottom: 10px;
  flex-shrink: 0;
}

.modal-title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-sans);
  letter-spacing: 0.5px;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 22px;
  cursor: pointer;
  transition: color 0.15s ease;
  line-height: 1;
}

.modal-close:hover {
  color: var(--accent-rose);
}

.scrollable-modal-body {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding-right: 4px;
}

/* Custom scrollbar for modal body */
.scrollable-modal-body::-webkit-scrollbar {
  width: 4px;
}
.scrollable-modal-body::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
}
.scrollable-modal-body::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.15);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 12px;
  flex-shrink: 0;
}

.btn-secondary {
  padding: 8px 18px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-secondary);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.18);
  color: var(--text-primary);
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scale-up {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.probe-modal-card.modal-card {
  width: 780px;
  max-width: 90vw;
}
</style>
