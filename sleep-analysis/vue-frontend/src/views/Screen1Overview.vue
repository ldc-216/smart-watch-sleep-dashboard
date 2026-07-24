<template>
  <div class="overview-page">
    <div class="grid kpi-row">
      <KpiCard label="平均睡眠得分" :value="kpi.avgSleepScore" unit="分" color="amber" />
      <KpiCard label="平均睡眠时长" :value="durationDisplay" unit="" color="sky" />
      <KpiCard label="失眠人群比例" :value="kpi.insomniaRatioPct" unit="%" color="rose" />
      <KpiCard label="总数据量" :value="kpi.totalRecordCnt" unit="条" color="teal" />
    </div>

    <div class="grid main-grid">
      <PanelCard title="睡眠阶段结构" sub="按年龄段" :loading="loading" :span="2">
        <EChart :option="ageStageOption" height="100%" />
      </PanelCard>

      <PanelCard title="月度趋势" sub="得分·效率" :loading="loading" :span="1">
        <EChart :option="monthlyTrendOption" height="100%" />
      </PanelCard>

      <PanelCard title="年度风险趋势" sub="呼吸暂停·失眠率" :loading="loading" :span="1">
        <EChart :option="riskTrendOption" height="100%" />
      </PanelCard>

      <PanelCard title="地域分布" sub="全球" :loading="loading" :span="1">
        <EChart :option="regionOption" height="100%" />
      </PanelCard>

      <!-- 新增：设备份额与异常预警中心 -->
      <PanelCard title="监测设备占比" sub="手环及可穿戴设备" :loading="loading" :span="1">
        <EChart :option="deviceShareOption" height="100%" />
      </PanelCard>

      <PanelCard title="健康状态异常与数据概览中心" sub="Health Alerts & Page Insights Summary" :loading="loading" :span="3">
        <div class="alerts-bulletin-container">
          <!-- 预警 1：生理指标与健康异常预警 -->
          <div class="alert-item alert-danger">
            <span class="alert-badge danger">🚨 健康异常预警</span>
            <div class="alert-text">
              本周期内累计筛查出 <strong class="highlight-rose">{{ kpi.highApneaRiskCnt || 0 }}</strong> 人存在高危睡眠呼吸暂停风险（评分 &ge; 30）。同时，失眠人群占比已达 <strong class="highlight-rose">{{ kpi.insomniaRatioPct || 0 }}%</strong>，表明当前群体存在一定的睡眠健康隐患，建议结合可穿戴设备数据提供个性化行为干预。
            </div>
          </div>
          <!-- 预警 2：年龄阶段总结（动态分析睡眠结构） -->
          <div class="alert-item alert-warning">
            <span class="alert-badge warning">👥 年龄段深睡分析</span>
            <div class="alert-text" v-html="ageStageSummaryText"></div>
          </div>
          <!-- 预警 3：地域与设备总结（动态分析地图与饼图） -->
          <div class="alert-item alert-info">
            <span class="alert-badge info">🌐 地域与设备分布</span>
            <div class="alert-text" v-html="regionDeviceSummaryText"></div>
          </div>
        </div>
      </PanelCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import PanelCard from '../components/PanelCard.vue'
import KpiCard from '../components/KpiCard.vue'
import EChart from '../components/EChart.vue'
import { getScreen1Overview } from '../api/screen1'
import { CHART_COLORS, baseGrid, baseTooltip, categoryAxis, valueAxis, baseLegend } from '../styles/chartTheme'
import * as echarts from 'echarts'
import worldJson from '../assets/world.json'

echarts.registerMap('world', worldJson)

const REGION_COORDS = {
  '亚洲': [100, 35],
  '欧洲': [15, 50],
  '北美洲': [-100, 40],
  '大洋洲': [135, -25],
  '其他': [20, 0]
}

const C = CHART_COLORS

const loading = ref(true)
const kpi = reactive({})
const yearlyTrend = ref([])
const regionSummary = ref([])
const monthlyTrend = ref([])
const ageStageBreakdown = ref([])
const deviceShares = ref([])

// ---- 动态页面概览数据总结计算 ----
const ageStageSummaryText = computed(() => {
  if (!ageStageBreakdown.value || !ageStageBreakdown.value.length) return '正在加载群体结构分析数据...'
  let worstAgeGroup = null
  let minDeep = 100
  ageStageBreakdown.value.forEach(d => {
    if (d.avgDeepPct < minDeep) {
      minDeep = d.avgDeepPct
      worstAgeGroup = d.ageBucket
    }
  })
  return `睡眠阶段结构指出：年龄段在 <b>${worstAgeGroup}</b> 的群体深睡占比最低（平均仅约 <b>${minDeep.toFixed(1)}%</b>），清醒比例相对偏高。这也印证了随年龄增加群体深睡和REM占比呈缓慢退化趋势。`
})

const regionDeviceSummaryText = computed(() => {
  if (!regionSummary.value.length || !deviceShares.value.length) return '正在加载地域及设备监测数据...'
  let lowestRegion = ''
  let lowestScore = 100
  regionSummary.value.forEach(r => {
    if (r.avgSleepScore < lowestScore) {
      lowestScore = r.avgSleepScore
      lowestRegion = r.region
    }
  })
  let topDevice = ''
  let maxCnt = 0
  let totalDev = 0
  deviceShares.value.forEach(d => {
    totalDev += d.cnt
    if (d.cnt > maxCnt) {
      maxCnt = d.cnt
      topDevice = d.deviceModel
    }
  })
  const pct = totalDev > 0 ? ((maxCnt / totalDev) * 100).toFixed(1) : '0'
  return `地域分布分析中，<b>${lowestRegion}</b> 的平均睡眠得分最低（仅 <b>${lowestScore.toFixed(1)}</b> 分）。设备占比方面，主流穿戴工具是 <b>${topDevice}</b>（占 <b>${pct}%</b>），为本次大数据分析的样本主力。`
})



const durationDisplay = computed(() => {
  if (!kpi.avgDurationMinutes && kpi.avgDurationMinutes !== 0) return '--'
  const h = Math.floor(kpi.avgDurationMinutes / 60)
  const m = Math.round(kpi.avgDurationMinutes % 60)
  return `${h}h ${m}m`
})

const tipBg = 'rgba(20,20,30,0.94)'
const tipBorder = 'rgba(255,255,255,0.08)'
const tipCss = 'box-shadow: 0 8px 24px rgba(0,0,0,0.45); border-radius: 12px; padding: 10px 14px;'

function tooltip(titleFn) {
  return {
    trigger: 'axis',
    backgroundColor: tipBg,
    borderColor: tipBorder,
    borderWidth: 1,
    textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 12, fontWeight: 400 },
    extraCssText: tipCss,
    formatter: titleFn
  }
}

// ---- 睡眠阶段堆叠柱状图 ----
const ageStageOption = computed(() => {
  const ages = ageStageBreakdown.value.map(d => d.ageBucket)
  const deepData  = ageStageBreakdown.value.map(d => d.avgDeepPct)
  const remData   = ageStageBreakdown.value.map(d => d.avgRemPct)
  const lightData = ageStageBreakdown.value.map(d => d.avgLightPct)
  const awakeData = ageStageBreakdown.value.map(d => d.avgAwakePct)

  const bar = (colorStops) => ({
    borderRadius: 4,
    color: {
      type: 'linear',
      x: 0, y: 0, x2: 1, y2: 0,
      colorStops
    },
    borderWidth: 0
  })

  return {
    tooltip: tooltip((params) => {
      let s = `<div style="font-weight:600;color:#eeede6;margin-bottom:6px;font-size:13px">${params[0].axisValue}</div>`
      params.forEach(p => {
        s += `<div style="margin:3px 0;display:flex;justify-content:space-between;gap:24px;">
          <span style="color:rgba(255,255,255,0.5)">${p.marker}${p.seriesName}</span>
          <span style="font-weight:500;font-family:var(--font-mono);">${p.value.toFixed(1)}%</span>
        </div>`
      })
      return s
    }),
    legend: { ...baseLegend(), top: 'auto', bottom: 0, left: 'center', right: 'auto', data: ['深度睡眠', 'REM', '浅度睡眠', '清醒'], itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 9.5 } },
    grid: { ...baseGrid(), left: '4%', right: '4%', top: '16%', bottom: '16%', containLabel: true },
    xAxis: valueAxis({ name: '%', max: 100, nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 9 } }),
    yAxis: {
      type: 'category',
      data: ages,
      axisLabel: { color: 'rgba(255,255,255,0.35)', fontSize: 10, fontFamily: 'Inter, sans-serif' },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
      axisTick: { show: false }
    },
    series: [
      { name: '深度睡眠', type: 'bar', stack: 'stage', data: deepData, itemStyle: bar([{ offset: 0, color: '#3f82be' }, { offset: 1, color: '#77b2e3' }]), barWidth: 16 },
      { name: 'REM', type: 'bar', stack: 'stage', data: remData, itemStyle: bar([{ offset: 0, color: '#398677' }, { offset: 1, color: '#63bbae' }]) },
      { name: '浅度睡眠', type: 'bar', stack: 'stage', data: lightData, itemStyle: bar([{ offset: 0, color: '#a77c38' }, { offset: 1, color: '#dfab5f' }]) },
      { name: '清醒', type: 'bar', stack: 'stage', data: awakeData, itemStyle: bar([{ offset: 0, color: '#bf6c61' }, { offset: 1, color: '#e59b92' }]) }
    ]
  }
})

// ---- 月度趋势 ----
const monthlyTrendOption = computed(() => {
  const sorted = [...monthlyTrend.value].sort((a, b) =>
    a.yearRecorded !== b.yearRecorded ? a.yearRecorded - b.yearRecorded : a.monthRecorded - b.monthRecorded
  )
  const recent = sorted.slice(-24)
  const xData = recent.map(d => `${d.yearRecorded}-${String(d.monthRecorded).padStart(2, '0')}`)
  const scoreData = recent.map(d => d.avgSleepScore)
  const effData = recent.map(d => d.avgEfficiencyPct)

  return {
    tooltip: tooltip((params) => {
      let s = `<div style="font-weight:600;color:#eeede6;margin-bottom:6px;font-size:13px">${params[0].axisValue}</div>`
      params.forEach(p => {
        const u = p.seriesName.includes('效率') ? '%' : ''
        s += `<div style="margin:3px 0;display:flex;justify-content:space-between;gap:24px;">
          <span style="color:rgba(255,255,255,0.5)">${p.marker}${p.seriesName}</span>
          <span style="font-weight:500;font-family:var(--font-mono);">${p.value.toFixed(1)}${u}</span>
        </div>`
      })
      return s
    }),
    legend: { ...baseLegend(), top: 'auto', bottom: 0, left: 'center', right: 'auto', data: ['得分', '效率'], itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 9.5 } },
    grid: { ...baseGrid(), left: '4%', right: '4%', top: '18%', bottom: '18%', containLabel: true },
    xAxis: categoryAxis({
      data: xData,
      axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 8.5, interval: 5, rotate: 0 }
    }),
    yAxis: [
      {
        type: 'value', name: '得分', nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 10 },
        axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 10 },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } },
        min: 62, max: 77
      },
      {
        type: 'value', name: '效率%', nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 10 },
        axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 10, formatter: '{value}%' },
        splitLine: { show: false },
        min: 86, max: 96
      }
    ],
    series: [
      {
        name: '得分', type: 'line', yAxisIndex: 0, data: scoreData,
        smooth: true, showSymbol: false, 
        itemStyle: { color: C[0] },
        lineStyle: { color: C[0], width: 3, shadowBlur: 8, shadowColor: 'rgba(201,151,78,0.25)' },
        areaStyle: { color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(201,151,78,0.18)' }, { offset: 1, color: 'rgba(201,151,78,0)' }]
        }}
      },
      {
        name: '效率', type: 'line', yAxisIndex: 1, data: effData,
        smooth: true, showSymbol: false, 
        itemStyle: { color: C[1] },
        lineStyle: { color: C[1], width: 3, shadowBlur: 8, shadowColor: 'rgba(90,171,154,0.25)' },
        areaStyle: { color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(90,171,154,0.15)' }, { offset: 1, color: 'rgba(90,171,154,0)' }]
        }}
      }
    ]
  }
})

// ---- 年度风险趋势 ----
const riskTrendOption = computed(() => {
  const years = yearlyTrend.value.map(d => d.yearRecorded)
  const apnea = yearlyTrend.value.map(d => d.highApneaRiskCnt)
  const insomnia = yearlyTrend.value.map(d => d.insomniaRatioPct)

  return {
    tooltip: tooltip((params) => {
      let s = `<div style="font-weight:600;color:#eeede6;margin-bottom:6px;font-size:13px">${params[0].axisValue}年</div>`
      params.forEach(p => {
        const u = p.seriesName.includes('失眠') ? '%' : ''
        s += `<div style="margin:3px 0;display:flex;justify-content:space-between;gap:24px;">
          <span style="color:rgba(255,255,255,0.5)">${p.marker}${p.seriesName}</span>
          <span style="font-weight:500;font-family:var(--font-mono);">${p.value}${u}</span>
        </div>`
      })
      return s
    }),
    legend: { ...baseLegend(), top: 'auto', bottom: 0, left: 'center', right: 'auto', data: ['高危人数', '失眠率'], itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 9.5 } },
    grid: { ...baseGrid(), left: '4%', right: '4%', top: '18%', bottom: '18%', containLabel: true },
    xAxis: categoryAxis({ data: years }),
    yAxis: [
      {
        type: 'value', name: '人', nameTextStyle: { color: 'rgba(255,255,255,0.2)', fontSize: 9 },
        axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 9.5 },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
      },
      {
        type: 'value', axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 9.5, formatter: '{value}%' },
        splitLine: { show: false }, min: 2.5, max: 5.0, interval: 0.5
      }
    ],
    series: [
      {
        name: '高危人数', type: 'bar', yAxisIndex: 0, data: apnea, barMaxWidth: 12,
        itemStyle: { 
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{ offset: 0, color: '#e0968d' }, { offset: 1, color: 'rgba(212,133,123,0.1)' }]
          }, 
          borderRadius: [5, 5, 0, 0] 
        },
        label: { show: false }
      },
      {
        name: '失眠率', type: 'line', yAxisIndex: 1, data: insomnia,
        smooth: true, symbol: 'circle', symbolSize: 5,
        lineStyle: { color: '#c9974e', width: 3, shadowBlur: 6, shadowColor: 'rgba(201,151,78,0.25)' },
        itemStyle: { color: '#c9974e', borderColor: 'rgba(20,20,30,1)', borderWidth: 2 }
      }
    ]
  }
})

// ---- 地域分布地图 ----
const regionOption = computed(() => {
  const mapData = regionSummary.value.map(item => {
    const coords = REGION_COORDS[item.region] || [0, 0]
    return {
      name: item.region,
      value: [...coords, item.avgSleepScore],
      avgDurationMinutes: item.avgDurationMinutes,
      userCnt: item.userCnt
    }
  })

  // 动态计算用户数的最大最小值以映射圈的大小，得分最大最小值映射颜色深浅
  const counts = regionSummary.value.map(d => d.userCnt || 0)
  const maxCnt = counts.length ? Math.max(...counts) : 1000
  const minCnt = counts.length ? Math.min(...counts) : 100

  const scores = regionSummary.value.map(d => d.avgSleepScore || 70)
  const minScore = scores.length ? Math.min(...scores) : 70
  const maxScore = scores.length ? Math.max(...scores) : 80

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: tipBg,
      borderColor: tipBorder,
      borderWidth: 1,
      textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 12, fontWeight: 400 },
      extraCssText: tipCss,
      formatter: (p) => {
        if (!p.data) return ''
        const d = p.data
        const hh = Math.floor(d.avgDurationMinutes / 60)
        const mm = Math.round(d.avgDurationMinutes % 60)
        return `<div style="font-weight:600;margin-bottom:8px;font-size:13px">${d.name}</div>
          <div style="margin:4px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
            <span>平均得分</span><span style="color:#5aab9a;font-weight:500;font-family:var(--font-mono)">${d.value[2].toFixed(1)}</span></div>
          <div style="margin:4px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
            <span>平均时长</span><span style="color:#6ba8d9;font-weight:500;font-family:var(--font-mono)">${hh}h ${mm}m</span></div>
          <div style="margin:4px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.5)">
            <span>监测用户</span><span style="color:#eeede6;font-weight:500;font-family:var(--font-mono)">${d.userCnt} 人</span></div>`
      }
    },
    visualMap: {
      show: true,
      min: minScore - 0.2,
      max: maxScore + 0.2,
      dimension: 2,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      itemWidth: 10,
      itemHeight: 100,
      text: ['高得分', '低得分'],
      calculable: true,
      inverse: true,
      inRange: {
        color: ['rgba(212,133,123,0.85)', 'rgba(201,151,78,0.85)', 'rgba(90,171,154,0.85)']
      },
      textStyle: {
        color: 'rgba(255,255,255,0.4)',
        fontFamily: 'var(--font-sans)',
        fontSize: 9.5
      }
    },
    geo: {
      map: 'world',
      roam: true,
      zoom: 1.25,
      center: [10, 20],
      label: { show: false },
      itemStyle: {
        areaColor: '#0f0f15',
        borderColor: 'rgba(255,255,255,0.03)',
        borderWidth: 0.6
      },
      emphasis: {
        itemStyle: { areaColor: '#181822' },
        label: { show: false }
      }
    },
    series: [{
      name: '睡眠状况',
      type: 'effectScatter',
      coordinateSystem: 'geo',
      data: mapData,
      showEffectOn: 'render',
      rippleEffect: { brushType: 'stroke', scale: 2.2 },
      symbolSize: (val) => {
        const score = val[2] || 70
        const range = maxScore - minScore
        if (range === 0) return 14
        const ratio = (score - minScore) / range
        return 8 + ratio * 12
      },
      label: {
        show: true,
        formatter: '{b}',
        position: 'right',
        color: 'rgba(238,237,230,0.6)',
        fontFamily: 'var(--font-sans)',
        fontSize: 10,
        distance: 8
      },
      itemStyle: {
        shadowBlur: 15,
        shadowColor: 'rgba(90,171,154,0.45)'
      }
    }]
  }
})

const deviceShareOption = computed(() => {
  const chartData = deviceShares.value.map(d => ({
    name: d.deviceModel,
    value: d.cnt
  }))

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: tipBg,
      borderColor: tipBorder,
      borderWidth: 1,
      textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 11 },
      extraCssText: tipCss,
      formatter: '{b} : {c}台 ({d}%)'
    },
    legend: {
      ...baseLegend(),
      orient: 'vertical',
      right: '6%',
      top: 'center',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: 'rgba(255,255,255,0.45)', fontSize: 10, fontFamily: 'Inter, sans-serif' }
    },
    series: [
      {
        name: '监测设备占比',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: 'rgba(20,20,30,0.5)',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: { label: { show: false } },
        labelLine: { show: false },
        data: chartData.length ? chartData : [{ name: '无数据', value: 0 }],
        color: ['#5aab9a', '#c9974e', '#6ba8d9', '#a48cdb', '#d4857b']
      }
    ]
  }
})

onMounted(async () => {
  try {
    const data = await getScreen1Overview()
    Object.assign(kpi, data.kpi)
    yearlyTrend.value = data.yearlyTrend || []
    regionSummary.value = (data.regionSummary || []).filter(item => {
      const name = (item.region || '').trim().toLowerCase()
      return !name.includes('其他') && !name.includes('other')
    })
    monthlyTrend.value = data.monthlyTrend || []
    ageStageBreakdown.value = data.ageStageBreakdown || []
    deviceShares.value = (data.deviceShare || []).filter(item => {
      const name = (item.deviceModel || '').trim().toLowerCase()
      return !name.includes('其他') && !name.includes('other')
    })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.overview-page {
  height: calc(100vh - 110px);
  padding: 8px 12px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-row {
  grid-template-columns: repeat(4, 1fr);
  flex-shrink: 0;
}

.main-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1.15fr 1fr 0.95fr;
  gap: 8px;
}

/* Deep override to shrink PanelCard default large padding & margins */
:deep(.panel-card) {
  padding: 10px 14px !important;
  border-radius: 8px !important;
}

:deep(.panel-head) {
  margin-bottom: 6px !important;
}

:deep(.panel-title) {
  font-size: 13px !important;
}

:deep(.panel-sub) {
  font-size: 9px !important;
}

/* Alerts Bulletin Board */
.alerts-bulletin-container {
  display: flex;
  flex-direction: row;
  gap: 10px;
  height: 100%;
  justify-content: space-between;
  padding: 2px;
}

.alert-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.012);
  border: 1px solid rgba(255, 255, 255, 0.035);
  transition: all 0.25s ease;
}
.alert-item:hover {
  background: rgba(255, 255, 255, 0.025);
  transform: translateY(-2px);
}

.alert-item.alert-danger {
  border-left: 3px solid var(--accent-rose);
  background: linear-gradient(90deg, rgba(212, 133, 123, 0.06) 0%, rgba(20, 20, 30, 0.2) 100%);
}
.alert-item.alert-warning {
  border-left: 3px solid var(--accent-amber);
  background: linear-gradient(90deg, rgba(201, 151, 78, 0.06) 0%, rgba(20, 20, 30, 0.2) 100%);
}
.alert-item.alert-info {
  border-left: 3px solid var(--accent-teal);
  background: linear-gradient(90deg, rgba(90, 171, 154, 0.06) 0%, rgba(20, 20, 30, 0.2) 100%);
}

.alert-badge {
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
}
.alert-badge.danger {
  color: var(--accent-rose);
  background: rgba(212, 133, 123, 0.15);
}
.alert-badge.warning {
  color: var(--accent-amber);
  background: rgba(201, 151, 78, 0.15);
}
.alert-badge.info {
  color: var(--accent-teal);
  background: rgba(90, 171, 154, 0.15);
}

.alert-text {
  font-size: 11.5px;
  line-height: 1.5;
  color: var(--text-secondary);
}

.highlight-rose {
  color: var(--accent-rose);
  font-weight: 700;
  text-shadow: 0 0 6px rgba(212, 133, 123, 0.2);
}

.highlight-amber {
  color: var(--accent-amber);
  font-weight: 700;
  text-shadow: 0 0 6px rgba(201, 151, 78, 0.2);
}
</style>

