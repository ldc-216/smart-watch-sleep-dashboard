<template>
  <div class="page-container">
    <!-- 来自画像页的数据提示 Banner -->
    <transition name="banner-slide">
      <div v-if="fromProfileBanner" class="profile-import-banner">
        <span class="pib-icon">🔗</span>
        <span class="pib-text">已自动载入 <strong>{{ fromProfileUsername }}</strong> 的睡眠画像均值数据并完成预填</span>
        <button class="pib-close" @click="fromProfileBanner = false">×</button>
      </div>
    </transition>
    <div class="grid row1">
      <div class="card card-form">
        <div class="card-head">
          <span class="card-title">睡眠得分预测</span>
          <span class="card-sub">参数调节</span>
        </div>
        <!-- 快捷场景预设 -->
        <div class="preset-chips">
          <span class="preset-label">场景预设:</span>
          <button 
            v-for="p in presetList" 
            :key="p.name" 
            class="preset-btn"
            :class="{ active: activePreset === p.name }"
            @click="applyPreset(p)"
          >
            {{ p.name }}
          </button>
        </div>
        <div class="form-grid">
          <div v-for="f in formFields" :key="f.key" class="fg-item">
            <div class="fg-top">
              <label class="fg-label" :for="'pred-' + f.key">{{ f.label }}</label>
              <span class="fg-val">{{ form[f.key] }}{{ f.unit }}</span>
            </div>
            <input :id="'pred-' + f.key" :name="'pred-' + f.key" type="range" class="slider" :min="f.min" :max="f.max" :step="f.step" v-model.number="form[f.key]" />
          </div>
        </div>
        <button class="btn-primary btn-predict" :disabled="predicting" @click="onPredict">
          <span v-if="predicting">计算中··</span>
          <span v-else>预测得分</span>
        </button>
      </div>

      <div class="card">
        <div class="card-head">
          <span class="card-title">预测结果</span>
          <span class="card-sub">Random Forest</span>
        </div>
        <div class="result-box-compact">
          <div class="score-main-row">
            <span class="score-num" :style="{ color: scoreColor }">
              {{ predictedScore !== null ? predictedScore.toFixed(1) : '--' }}
            </span>
            <div class="score-sub-col">
              <span class="score-sub">预测睡眠得分</span>
              <div class="status-badge" :style="{ backgroundColor: scoreStatus?.color + '15', color: scoreStatus?.color, borderColor: scoreStatus?.color + '30' }" v-if="scoreStatus">
                {{ scoreStatus.text }}
              </div>
            </div>
            <div class="score-metrics-col" v-if="modelMetrics">
              <span class="tag tag-teal">RMSE {{ modelMetrics.rmse?.toFixed(2) }}</span>
              <span class="tag tag-amber">R² {{ modelMetrics.r2?.toFixed(3) }}</span>
            </div>
          </div>
        </div>
        
        <!-- Tabs Header -->
        <div class="card-tabs-header">
          <button class="tab-btn" :class="{ active: activeRightTab === 'guidance' }" @click="activeRightTab = 'guidance'">💡 干预建议</button>
          <button class="tab-btn" :class="{ active: activeRightTab === 'seeker' }" @click="activeRightTab = 'seeker'">🎯 目标寻优</button>
        </div>

        <div v-if="activeRightTab === 'guidance'" class="pred-guidance-panel">
          <div class="pg-head">💡 模拟习惯分析与生活干预建议</div>
          <div class="pg-list" v-if="predictedScore !== null">
            <div v-for="(g, idx) in predGuidelines" :key="idx" class="pg-card" :class="'pg-card--' + g.type">
              <div class="pg-card-title">
                <span class="pg-dot"></span>
                {{ g.title }}
              </div>
              <div class="pg-card-desc">{{ g.desc }}</div>
            </div>
          </div>
          <div class="pg-empty" v-else>
            请在左侧调整各项生活行为因子的数值，并点击“预测得分”获取定制化的生活指导方案。
          </div>
        </div>

        <div v-else-if="activeRightTab === 'seeker'" class="goal-seeker-panel">
          <div class="seeker-container">
            <div class="seeker-input-row">
              <label class="seeker-label">设定期望得分:</label>
              <div class="seeker-control">
                <input type="range" min="50" max="98" step="1" v-model.number="targetScore" class="slider seeker-slider" />
                <span class="seeker-val" :style="{ color: targetScoreColor }">{{ targetScore }}分</span>
              </div>
            </div>
            
            <button class="btn-primary btn-seek" :disabled="seeking || predictedScore === null" @click="onSeek">
              <span v-if="seeking">寻优计算中...</span>
              <span v-else>🔍 一键求解生活建议</span>
            </button>

            <div class="seeker-results" v-if="seekResult">
              <div class="sr-header">
                <span>💡 达成目标（估值: ~{{ seekResult.predicted.toFixed(1) }}分）的推荐路径：</span>
              </div>
              <div class="sr-list">
                <div v-for="item in seekResult.adjustments" :key="item.key" class="sr-item">
                  <span class="sr-name">{{ item.label }}</span>
                  <span class="sr-from">{{ item.from }}{{ item.unit }}</span>
                  <span class="sr-arrow">➔</span>
                  <span class="sr-to" :class="{ 'sr-better': item.improved }">{{ item.to }}{{ item.unit }}</span>
                  <span class="sr-diff" :class="{ 'sr-better': item.improved }">
                    ({{ item.diff > 0 ? '+' : '' }}{{ item.diff }}{{ item.unit }})
                  </span>
                </div>
                <div class="sr-empty-msg" v-if="seekResult.adjustments.length === 0">
                  当前数值已完全满足或极其接近期望目标分，无需调整！
                </div>
              </div>
              <button class="btn-apply-seek" @click="applySeekResult">
                ⚡ 应用推荐数值并同步滑块
              </button>
            </div>
            <div class="seeker-empty" v-else>
              设定您的目标睡眠分数，系统将基于随机森林模型，自动计算出优化路径。
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid row2">
      <!-- Left Card: Importance & Sensitivity -->
      <div class="card">
        <div class="card-head" style="margin-bottom: 4px;">
          <div class="card-tabs-header-left">
            <button class="tab-btn" :class="{ active: activeLeftTab === 'importance' }" @click="activeLeftTab = 'importance'">特征重要性</button>
            <button class="tab-btn" :class="{ active: activeLeftTab === 'sensitivity' }" @click="activeLeftTab = 'sensitivity'">因子敏感度</button>
          </div>
          <span class="card-sub">分析</span>
        </div>
        <div class="chart-wrap" v-if="activeLeftTab === 'importance'">
          <EChart v-if="!loadingImportance" :option="importanceOption" height="100%" />
          <div v-else class="state-loading">Loading··</div>
        </div>
        <div class="chart-wrap" v-else-if="activeLeftTab === 'sensitivity'">
          <div class="sensitivity-container">
            <div class="sens-selector-row">
              <span class="sens-label">选择分析因子:</span>
              <select v-model="selectedSensFeature" class="sens-select" @change="calculateSensitivity">
                <option v-for="f in modifiableFields" :key="f.key" :value="f.key">{{ f.label }}</option>
              </select>
            </div>
            <div class="chart-wrap-sens">
              <EChart v-if="!calculatingSensitivity" :option="sensitivityOption" height="100%" />
              <div v-else class="state-loading">计算敏感度中...</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Card: Clustering & Sandbox -->
      <div class="card">
        <div class="card-head" style="margin-bottom: 4px;">
          <div class="card-tabs-header-left">
            <button class="tab-btn" :class="{ active: activeClusterTab === 'cluster' }" @click="activeClusterTab = 'cluster'">人群聚类 (3D)</button>
            <button class="tab-btn" :class="{ active: activeClusterTab === 'sandbox' }" @click="activeClusterTab = 'sandbox'">多方案沙箱</button>
          </div>
          <span class="card-sub">PCA 3D·K-Means</span>
        </div>
        
        <div class="chart-wrap-cluster" v-if="activeClusterTab === 'cluster'">
          <div v-if="loadingCluster" class="state-loading">Loading··</div>
          <div v-else class="cluster-content">
            <div class="chart-container-3d">
              <EChart :option="clusterOption" height="100%" />
            </div>
            <div class="cluster-tags">
              <div 
                v-for="p in clusterProfiles" 
                :key="p.clusterId" 
                class="ct-item-card clickable-card" 
                :class="{ 'card-active': activeClusterId === p.clusterId }"
                :style="{ 
                  borderColor: clusterColor(p.clusterId) + (activeClusterId === p.clusterId ? '80' : '25'), 
                  background: clusterColor(p.clusterId) + (activeClusterId === p.clusterId ? '15' : '05') 
                }"
                @click="applyCluster(p)"
              >
                <div class="ct-item-header">
                  <span class="ct-dot" :style="{ background: clusterColor(p.clusterId), color: clusterColor(p.clusterId) }"></span>
                  <span class="ct-name">{{ getClusterMeta(p.clusterId).name }}</span>
                  <span class="ct-meta">{{ p.userCnt }}人 · {{ p.avgSleepScore?.toFixed(1) }}分</span>
                </div>
                <div class="ct-desc">{{ getClusterMeta(p.clusterId).desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="sandbox-panel" v-else-if="activeClusterTab === 'sandbox'">
          <div class="sandbox-container">
            <div class="sb-header">
              <span class="sb-title">方案对比沙箱 (最多保存5组)</span>
              <button class="btn-save-sb" :disabled="predictedScore === null" @click="saveToSandbox">
                💾 保存当前方案
              </button>
            </div>
            <div class="sb-table-wrap" v-if="sandboxScenarios.length > 0">
              <table class="sb-table">
                <thead>
                  <tr>
                    <th>方案名称</th>
                    <th>得分</th>
                    <th>核心生活因子明细</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(sc, idx) in sandboxScenarios" :key="idx">
                    <td class="sb-td-name">
                      <input v-model="sc.name" class="sb-name-input" @change="saveSandboxToLocalStorage" />
                    </td>
                    <td class="sb-td-score" :style="{ color: getScoreColorByVal(sc.score) }">
                      {{ sc.score.toFixed(1) }}
                    </td>
                    <td class="sb-td-detail">
                      <span class="sb-detail-tag">👣 {{ sc.data.stepCountDay }}步</span>
                      <span class="sb-detail-tag">☕ {{ sc.data.caffeineMg }}mg</span>
                      <span class="sb-detail-tag">📱 {{ sc.data.screenTimeBeforeBedMin }}m</span>
                      <span class="sb-detail-tag">⚡ 压力{{ sc.data.stressScore }}</span>
                      <span class="sb-detail-tag">🌡️ {{ sc.data.roomTemperatureC }}℃</span>
                    </td>
                    <td class="sb-td-actions">
                      <button class="sb-act-btn load" @click="loadSandbox(sc)">载入</button>
                      <button class="sb-act-btn delete" @click="deleteSandbox(idx)">删除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="sb-empty" v-else>
              暂无保存的方案。您可以在上方调节好参数后，点击“保存当前方案”进行多情景横向对比。
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import EChart from '../components/EChart.vue'
import { predictSleepScore, getFeatureImportance, getModelMetrics, getClusterResult } from '../api/screen4'
import { CHART_COLORS, baseTooltip, baseGrid, categoryAxis, valueAxis } from '../styles/chartTheme'

const C = CHART_COLORS

const FEATURE_LABEL = {
  step_count_day: '步数', caffeine_mg: '咖啡因', alcohol_units: '酒精',
  screen_time_before_bed_min: '屏幕时间', stress_score: '压力',
  activity_before_bed_min: '睡前运动', room_temperature_c: '室温',
  ambient_noise_db: '噪音', bedtime_consistency_std_min: '就寝一致性',
  nap_duration_minutes: '小睡', age: '年龄', bmi: 'BMI'
}

const formFields = [
  { key: 'stepCountDay', label: '步数', min: 0, max: 20000, step: 100, unit: '' },
  { key: 'caffeineMg', label: '咖啡因', min: 0, max: 400, step: 10, unit: 'mg' },
  { key: 'alcoholUnits', label: '酒精', min: 0, max: 5, step: 0.1, unit: '单位' },
  { key: 'screenTimeBeforeBedMin', label: '屏幕时间', min: 0, max: 180, step: 5, unit: 'm' },
  { key: 'stressScore', label: '压力', min: 0, max: 100, step: 1, unit: '' },
  { key: 'activityBeforeBedMin', label: '运动', min: 0, max: 90, step: 5, unit: 'm' },
  { key: 'roomTemperatureC', label: '室温', min: 10, max: 30, step: 0.5, unit: '℃' },
  { key: 'ambientNoiseDb', label: '噪音', min: 20, max: 70, step: 1, unit: 'dB' },
  { key: 'bedtimeConsistencyStdMin', label: '就寝一致性', min: 0, max: 120, step: 5, unit: 'm' },
  { key: 'napDurationMinutes', label: '小睡', min: 0, max: 90, step: 5, unit: 'm' },
  { key: 'age', label: '年龄', min: 10, max: 90, step: 1, unit: '岁' },
  { key: 'bmi', label: 'BMI', min: 15, max: 40, step: 0.1, unit: '' },
]

const form = reactive({
  stepCountDay: 6000, caffeineMg: 100, alcoholUnits: 0, screenTimeBeforeBedMin: 60,
  stressScore: 40, activityBeforeBedMin: 20, roomTemperatureC: 21, ambientNoiseDb: 35,
  bedtimeConsistencyStdMin: 20, napDurationMinutes: 0, age: 35, bmi: 22.5
})

// 快捷场景数据集
const presetList = [
  {
    name: '🌙 完美睡眠',
    data: {
      stepCountDay: 10000, caffeineMg: 20, alcoholUnits: 0.0, screenTimeBeforeBedMin: 15,
      stressScore: 10, activityBeforeBedMin: 0, roomTemperatureC: 20.5, ambientNoiseDb: 25,
      bedtimeConsistencyStdMin: 10, napDurationMinutes: 0, age: 30, bmi: 21.0
    }
  },
  {
    name: '☕ 熬夜加班',
    data: {
      stepCountDay: 2500, caffeineMg: 280, alcoholUnits: 1.2, screenTimeBeforeBedMin: 150,
      stressScore: 85, activityBeforeBedMin: 10, roomTemperatureC: 24.5, ambientNoiseDb: 45,
      bedtimeConsistencyStdMin: 70, napDurationMinutes: 20, age: 30, bmi: 24.0
    }
  },
  {
    name: '🏃 运动日',
    data: {
      stepCountDay: 15000, caffeineMg: 50, alcoholUnits: 0.0, screenTimeBeforeBedMin: 40,
      stressScore: 20, activityBeforeBedMin: 60, roomTemperatureC: 20.0, ambientNoiseDb: 30,
      bedtimeConsistencyStdMin: 15, napDurationMinutes: 0, age: 30, bmi: 22.0
    }
  }
]

const activePreset = ref(null)
const activeClusterId = ref(null)

function applyPreset(preset) {
  activePreset.value = preset.name
  activeClusterId.value = null
  const d = preset.data
  Object.keys(d).forEach(k => {
    form[k] = d[k]
  })
  onPredict()
}

function applyCluster(p) {
  activePreset.value = null
  activeClusterId.value = p.clusterId
  
  const clamp = (v, min, max) => Math.min(max, Math.max(min, v))
  if (p.avgStressScore !== undefined && p.avgStressScore !== null) {
    form.stressScore = Math.round(clamp(p.avgStressScore, 0, 100))
  }
  if (p.avgCaffeineMg !== undefined && p.avgCaffeineMg !== null) {
    form.caffeineMg = Math.round(clamp(p.avgCaffeineMg, 0, 400))
  }
  if (p.avgAlcoholUnits !== undefined && p.avgAlcoholUnits !== null) {
    form.alcoholUnits = parseFloat(clamp(p.avgAlcoholUnits, 0, 5).toFixed(1))
  }
  if (p.avgScreenTimeBeforeBedMin !== undefined && p.avgScreenTimeBeforeBedMin !== null) {
    form.screenTimeBeforeBedMin = Math.round(clamp(p.avgScreenTimeBeforeBedMin, 0, 180))
  }
  
  if (p.clusterId === 0) {
    form.stepCountDay = 3200
    form.bedtimeConsistencyStdMin = 65
    form.napDurationMinutes = 15
    form.ambientNoiseDb = 40
    form.activityBeforeBedMin = 15
    form.roomTemperatureC = 23.5
  } else if (p.clusterId === 1) {
    form.stepCountDay = 11000
    form.bedtimeConsistencyStdMin = 12
    form.napDurationMinutes = 0
    form.ambientNoiseDb = 28
    form.activityBeforeBedMin = 20
    form.roomTemperatureC = 20.0
  } else if (p.clusterId === 2) {
    form.stepCountDay = 5800
    form.bedtimeConsistencyStdMin = 35
    form.bmi = 28.5
    form.age = 48
    form.ambientNoiseDb = 36
    form.alcoholUnits = Math.max(form.alcoholUnits, 1.8) 
  }

  onPredict()
}

// Tab states
const activeRightTab = ref('guidance')
const activeLeftTab = ref('importance')
const activeClusterTab = ref('cluster')

// Target score solver (Goal Seeker)
const targetScore = ref(85)
const seeking = ref(false)
const seekResult = ref(null)

const targetScoreColor = computed(() => {
  if (targetScore.value >= 80) return 'var(--accent-teal)'
  if (targetScore.value >= 70) return 'var(--accent-amber)'
  return 'var(--accent-rose)'
})

const PERFECT_SLEEP = {
  stepCountDay: 12000, caffeineMg: 0, alcoholUnits: 0.0, screenTimeBeforeBedMin: 0,
  stressScore: 0, activityBeforeBedMin: 0, roomTemperatureC: 20.5, ambientNoiseDb: 25,
  bedtimeConsistencyStdMin: 10, napDurationMinutes: 0
}

const WORST_SLEEP = {
  stepCountDay: 1000, caffeineMg: 350, alcoholUnits: 4.0, screenTimeBeforeBedMin: 150,
  stressScore: 90, activityBeforeBedMin: 70, roomTemperatureC: 28.0, ambientNoiseDb: 60,
  bedtimeConsistencyStdMin: 90, napDurationMinutes: 60
}

const modifiableFields = computed(() => {
  return formFields.filter(f => f.key !== 'age' && f.key !== 'bmi')
})

async function onSeek() {
  if (predictedScore.value === null) return
  seeking.value = true
  seekResult.value = null
  
  const currentScore = predictedScore.value
  const target = targetScore.value
  
  if (Math.abs(currentScore - target) < 1.0) {
    seekResult.value = {
      predicted: currentScore,
      adjustments: []
    }
    seeking.value = false
    return
  }

  const isUpward = target > currentScore
  const destination = isUpward ? PERFECT_SLEEP : WORST_SLEEP

  const stepsCount = 11
  const promises = []
  
  for (let i = 0; i < stepsCount; i++) {
    const t = i / (stepsCount - 1)
    const testForm = { ...form }
    
    Object.keys(destination).forEach(key => {
      const start = form[key]
      const end = destination[key]
      testForm[key] = start + t * (end - start)
      
      const field = formFields.find(f => f.key === key)
      if (field) {
        testForm[key] = Math.min(field.max, Math.max(field.min, testForm[key]))
        if (field.step >= 1) {
          testForm[key] = Math.round(testForm[key])
        } else {
          testForm[key] = parseFloat(testForm[key].toFixed(1))
        }
      }
    })
    
    promises.push((async () => {
      try {
        const res = await predictSleepScore(testForm)
        return { t, formState: testForm, score: res.predictedSleepScore }
      } catch (err) {
        return { t, formState: testForm, score: currentScore + t * (isUpward ? 95 - currentScore : 40 - currentScore) }
      }
    })())
  }
  
  try {
    const results = await Promise.all(promises)
    results.sort((a, b) => a.t - b.t)
    
    let bestMatch = results[0]
    let minDiff = Math.abs(bestMatch.score - target)
    
    for (let i = 1; i < results.length; i++) {
      const diff = Math.abs(results[i].score - target)
      if (diff < minDiff) {
        minDiff = diff
        bestMatch = results[i]
      }
    }
    
    const adjustments = []
    Object.keys(destination).forEach(key => {
      const field = formFields.find(f => f.key === key)
      if (!field) return
      
      const fromVal = form[key]
      const toVal = bestMatch.formState[key]
      const diff = parseFloat((toVal - fromVal).toFixed(1))
      
      if (Math.abs(diff) > 0.01) {
        adjustments.push({
          key,
          label: field.label,
          unit: field.unit,
          from: fromVal,
          to: toVal,
          diff,
          improved: isUpward
        })
      }
    })
    
    seekResult.value = {
      predicted: bestMatch.score,
      formState: bestMatch.formState,
      adjustments
    }
  } catch (e) {
    console.error(e)
  } finally {
    seeking.value = false
  }
}

function applySeekResult() {
  if (!seekResult.value || !seekResult.value.formState) return
  Object.keys(seekResult.value.formState).forEach(key => {
    form[key] = seekResult.value.formState[key]
  })
  seekResult.value = null
  activeRightTab.value = 'guidance'
  onPredict()
}

// Single-variable Sensitivity
const selectedSensFeature = ref('caffeineMg')
const calculatingSensitivity = ref(false)
const sensitivityPoints = ref([])

async function calculateSensitivity() {
  const field = formFields.find(f => f.key === selectedSensFeature.value)
  if (!field) return
  
  calculatingSensitivity.value = true
  const stepsCount = 10
  const stepVal = (field.max - field.min) / (stepsCount - 1)
  
  const promises = []
  for (let i = 0; i < stepsCount; i++) {
    const val = field.min + i * stepVal
    const testForm = { ...form }
    testForm[selectedSensFeature.value] = field.step >= 1 ? Math.round(val) : parseFloat(val.toFixed(1))
    
    promises.push((async () => {
      try {
        const res = await predictSleepScore(testForm)
        return { val: testForm[selectedSensFeature.value], score: res.predictedSleepScore }
      } catch (err) {
        return { val: testForm[selectedSensFeature.value], score: 70 }
      }
    })())
  }
  
  try {
    const results = await Promise.all(promises)
    results.sort((a, b) => a.val - b.val)
    sensitivityPoints.value = results
  } catch (e) {
    console.error(e)
  } finally {
    calculatingSensitivity.value = false
  }
}

const sensitivityOption = computed(() => {
  const xData = sensitivityPoints.value.map(p => p.val)
  const yData = sensitivityPoints.value.map(p => p.score)
  const field = formFields.find(f => f.key === selectedSensFeature.value)
  const unit = field ? field.unit : ''
  const name = field ? field.label : ''
  
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'axis' }),
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-size:11px"><b>${name}：${p.name}${unit}</b><br/>预测得分：<span style="color:var(--accent-teal);font-weight:700">${p.value}分</span></div>`
      }
    },
    grid: { left: 40, right: 20, top: 20, bottom: 35 },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 9.5 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
    },
    yAxis: {
      type: 'value',
      min: 40,
      max: 100,
      axisLabel: { color: 'rgba(255,255,255,0.4)', fontSize: 9.5 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [{
      name: '预测分数',
      type: 'line',
      smooth: true,
      data: yData,
      symbolSize: 6,
      itemStyle: { color: '#5aab9a' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(90, 171, 154, 0.25)' },
            { offset: 1, color: 'rgba(90, 171, 154, 0)' }
          ]
        }
      },
      lineStyle: {
        width: 3,
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#5aab9a' },
            { offset: 1, color: '#6ba8d9' }
          ]
        }
      }
    }]
  }
})

// Sandbox Comparison Scenarios
const sandboxScenarios = ref([])

function saveToSandbox() {
  if (predictedScore.value === null) return
  if (sandboxScenarios.value.length >= 5) {
    alert('方案沙箱最多保存5个方案，请先删除部分旧方案。')
    return
  }
  
  const name = `方案 ${sandboxScenarios.value.length + 1}`
  sandboxScenarios.value.push({
    name,
    score: predictedScore.value,
    data: { ...form }
  })
  saveSandboxToLocalStorage()
}

function loadSandbox(sc) {
  Object.keys(sc.data).forEach(key => {
    form[key] = sc.data[key]
  })
  onPredict()
}

function deleteSandbox(idx) {
  sandboxScenarios.value.splice(idx, 1)
  saveSandboxToLocalStorage()
}

function saveSandboxToLocalStorage() {
  localStorage.setItem('sleep_prediction_sandbox', JSON.stringify(sandboxScenarios.value))
}

function loadSandboxFromLocalStorage() {
  const saved = localStorage.getItem('sleep_prediction_sandbox')
  if (saved) {
    try {
      sandboxScenarios.value = JSON.parse(saved)
    } catch (e) {
      sandboxScenarios.value = []
    }
  }
}

function getScoreColorByVal(score) {
  if (score >= 80) return 'var(--accent-teal)'
  if (score >= 70) return 'var(--accent-amber)'
  return 'var(--accent-rose)'
}

watch([selectedSensFeature, activeLeftTab], () => {
  if (activeLeftTab.value === 'sensitivity') {
    calculateSensitivity()
  }
}, { immediate: true })

const predicting = ref(false)
const predictedScore = ref(null)
const modelMetrics = ref(null)
const loadingImportance = ref(true)
const loadingCluster = ref(true)
const featureImportance = ref([])
const clusterPoints = ref([])
const clusterProfiles = ref([])
const fromProfileBanner = ref(false)
const fromProfileUsername = ref('')

const CLUSTER_PALETTE = [C[4], C[1], C[0], C[2], C[3], C[6]]
const clusterColor = (id) => CLUSTER_PALETTE[id % CLUSTER_PALETTE.length]

// 聚类人群画像深度定义与诊疗指南
// 实际数据：cluster 0 (score=64.25, stress=68.2) cluster 1 (score=86.45, stress=18.5) cluster 2 (score=52.8, snore=28.5, spo2=91.2%)
const CLUSTER_METADATA = {
  0: {
    name: '高压焦虑失眠群体',
    desc: '特征：日常压力高（均值68.2），睡前屏幕暴露超1.8小时，睡眠得分偏低（64.3分）。建议：睡前30分钟强行推行数码静默隔离区，配合腹式呼吸解压，下午2点后禁食咖啡因。'
  },
  1: {
    name: '优质健眠型群体',
    desc: '特征：日间压力轻微（均值18.5），无明显睡前屏幕蓝光干扰，睡眠得分极高（86.5分），HRV健康。建议：属于极佳睡眠典范，继续保持现有生活规律，可设定为健康人群比对标杆。'
  },
  2: {
    name: '呼吸低氧高危群体',
    desc: '特征：打鼾频率偏高（均值28.5次/晚），最低血氧明显受压（均值91.2%），睡眠得分最低（52.8分）。建议：注意侧卧以释放气道，严控晚间酒精，超重人群需科学减重，必要时进行临床PSG监测。'
  }
}

function getClusterMeta(clusterId) {
  return CLUSTER_METADATA[clusterId] || { name: `未知聚类人群 ${clusterId}`, desc: '暂无该人群健康特征分析及医学指导建议。' }
}

const scoreColor = computed(() => {
  if (predictedScore.value === null) return 'var(--text-tertiary)'
  if (predictedScore.value >= 80) return 'var(--accent-teal)'
  if (predictedScore.value >= 70) return 'var(--accent-amber)'
  return 'var(--accent-rose)'
})

const scoreStatus = computed(() => {
  if (predictedScore.value === null) return null
  const score = predictedScore.value
  if (score >= 80) {
    return { text: '得分偏高', color: 'var(--accent-teal)' }
  } else if (score >= 70) {
    return { text: '得分中等', color: 'var(--accent-amber)' }
  } else {
    return { text: '得分偏低', color: 'var(--accent-rose)' }
  }
})

const predGuidelines = computed(() => {
  if (predictedScore.value === null) return []
  const items = []
  
  // 1. 睡眠总分诊断
  const score = predictedScore.value
  if (score >= 80) {
    items.push({
      type: 'success',
      title: '高品质睡眠模拟结果',
      desc: '当前生活参数预计能够带来极佳的体能与记忆修复。继续保持这一平衡的日程安排！'
    })
  } else if (score >= 70) {
    items.push({
      type: 'warning',
      title: '亚健康睡眠模拟结果',
      desc: '模拟睡眠分数处于中等区间，存在优化空间。可以通过减少睡前电子设备使用或降低日间压力来突破。'
    })
  } else {
    items.push({
      type: 'danger',
      title: '低效睡眠风险警告',
      desc: '当前参数组合预示着明显的疲劳堆积与浅睡眠占比过高。重点关注咖啡因、屏幕时间及就寝一致性。'
    })
  }

  // 2. 咖啡因分析
  if (form.caffeineMg > 150) {
    items.push({
      type: 'warning',
      title: '高咖啡因干扰 (Caffeine: ' + form.caffeineMg + 'mg)',
      desc: '咖啡因摄入量偏高。建议将饮用时间限制在清晨，并在睡前8小时内完全避免，以保证日落后腺苷能正常发挥促眠作用。'
    })
  }

  // 3. 酒精分析
  if (form.alcoholUnits > 1) {
    items.push({
      type: 'danger',
      title: '酒精脱水与快速动眼期(REM)剥夺 (Alcohol: ' + form.alcoholUnits + 'U)',
      desc: '模拟饮酒量过大。酒精虽能缩短入睡潜伏期，但会造成下半夜心率偏高、多梦易醒，使醒后大脑昏沉。'
    })
  }

  // 4. 睡前屏幕
  if (form.screenTimeBeforeBedMin > 45) {
    items.push({
      type: 'warning',
      title: '睡前蓝光负荷超纲 (Screen: ' + form.screenTimeBeforeBedMin + 'm)',
      desc: '睡前屏幕暴露过长，阻碍松果体分泌褪黑素。建议将手机调为暗色/夜间防蓝光模式，或在睡前30分钟停止浏览。'
    })
  }

  // 5. 压力状态
  if (form.stressScore > 50) {
    items.push({
      type: 'danger',
      title: '中重度精神压力 (Stress: ' + form.stressScore + ')',
      desc: '高压力引发交感神经兴奋。推荐睡前进行4-7-8呼吸法或收听白噪音，帮助大脑从工作状态平稳过渡到睡眠状态。'
    })
  }

  // 6. 日间步数与睡眠驱力
  if (form.stepCountDay < 5000) {
    items.push({
      type: 'warning',
      title: '日间活动量不足 (Steps: ' + form.stepCountDay + ')',
      desc: '步数偏低导致慢波深睡眠的“蓄水池”蓄水不足。建议日间累计步行6000-10000步，利用生物钟的累积效应快速深睡。'
    })
  }

  // 7. 作息一致性
  if (form.bedtimeConsistencyStdMin > 40) {
    items.push({
      type: 'warning',
      title: '就寝不规律 (Std Dev: ' + form.bedtimeConsistencyStdMin + 'm)',
      desc: '就寝时间波动过大（标准差超40分钟），会导致体内昼夜节律时钟错位。建议在周末也保持统一的起床时间。'
    })
  }

  // 8. 最佳卧室温度
  if (form.roomTemperatureC < 17 || form.roomTemperatureC > 23) {
    items.push({
      type: 'warning',
      title: '卧室温度不适宜 (Temp: ' + form.roomTemperatureC + '℃)',
      desc: '环境温度偏离了最佳生理区间(18℃~22℃)，这会妨碍身体核心体温的自然下降，导致入睡困难或燥热易醒。'
    })
  }

  return items
})

const importanceOption = computed(() => {
  const sorted = [...featureImportance.value].sort((a, b) => a.importance - b.importance)
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'item' }),
      formatter: (params) => {
        const name = params.name || ''
        const val = typeof params.value === 'number' ? params.value.toFixed(2) : params.value
        return `<div style="font-size:11px"><b>${name}</b><br/>重要性占比：<span style="color:${params.color || '#5aab9a'};font-weight:700">${val}%</span></div>`
      }
    },
    grid: { left: 90, right: 24, top: 4, bottom: 4, containLabel: true },
    xAxis: valueAxis({
      axisLabel: { fontSize: 10, formatter: (v) => v + '%' },
      splitLine: { show: false }
    }),
    yAxis: categoryAxis({
      data: sorted.map(d => FEATURE_LABEL[d.featureName] || d.featureName),
      axisLabel: { color: 'rgba(255,255,255,0.45)', fontSize: 10 }
    }),
    series: [{
      type: 'bar', barWidth: 8,
      data: sorted.map((d, index) => {
        // 计算插值系数 (从 0 到 1)，代表从小到大的特征重要性排名
        const N = sorted.length
        const factor = index / (N > 1 ? N - 1 : 1)
        
        // 起点色插值：深灰蓝 [19, 38, 59] 到 湖绿 [60, 168, 166]
        const startR = Math.round(19 + factor * (60 - 19))
        const startG = Math.round(38 + factor * (168 - 38))
        const startB = Math.round(59 + factor * (166 - 59))
        const colorStart = `rgb(${startR}, ${startG}, ${startB})`
        
        // 终点色插值：暗石蓝 [41, 92, 128] 到 霓虹青 [139, 227, 210]
        const endR = Math.round(41 + factor * (139 - 41))
        const endG = Math.round(92 + factor * (227 - 92))
        const endB = Math.round(128 + factor * (210 - 128))
        const colorEnd = `rgb(${endR}, ${endG}, ${endB})`
        
        return {
          value: d.importancePct,
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 1, y2: 0, // 从左至右的水平渐变
              colorStops: [
                { offset: 0, color: colorStart },
                { offset: 1, color: colorEnd }
              ]
            },
            borderRadius: [0, 4, 4, 0]
          }
        }
      })
    }]
  }
})

const clusterOption = computed(() => {
  const ids = [...new Set(clusterPoints.value.map(p => p.clusterId))].sort()
  const series = ids.map(id => ({
    type: 'scatter3D',
    name: getClusterMeta(id).name,
    symbolSize: 3.5,
    itemStyle: { color: clusterColor(id), opacity: 0.75, borderWidth: 0 },
    data: clusterPoints.value.filter(p => p.clusterId === id).map(p => [p.pca1, p.pca2, p.pca3])
  }))
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'item' }),
      formatter: (params) => {
        const meta = getClusterMeta(params.seriesIndex)
        return `<div style="font-size:11px"><b style="color:${clusterColor(params.seriesIndex)}">${meta.name}</b><br/>PC1: ${params.value[0]?.toFixed(2)}&nbsp;&nbsp;PC2: ${params.value[1]?.toFixed(2)}&nbsp;&nbsp;PC3: ${params.value[2]?.toFixed(2)}</div>`
      }
    },
    legend: {
      data: ids.map(id => getClusterMeta(id).name),
      textStyle: { color: 'rgba(255,255,255,0.4)', fontSize: 9.5 },
      bottom: 4, left: 'center',
      itemWidth: 10, itemHeight: 10
    },
    grid3D: {
      viewControl: { autoRotate: true, autoRotateSpeed: 5, distance: 180 },
      boxWidth: 85, boxHeight: 85, boxDepth: 85,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
      axisLabel: { color: 'rgba(255,255,255,0.18)', fontSize: 9 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.01)' } }
    },
    xAxis3D: { type: 'value', name: 'PC1', nameTextStyle: { color: 'rgba(255,255,255,0.15)', fontSize: 9 } },
    yAxis3D: { type: 'value', name: 'PC2', nameTextStyle: { color: 'rgba(255,255,255,0.15)', fontSize: 9 } },
    zAxis3D: { type: 'value', name: 'PC3', nameTextStyle: { color: 'rgba(255,255,255,0.15)', fontSize: 9 } },
    series
  }
})

async function onPredict() {
  predicting.value = true
  try {
    const res = await predictSleepScore(form)
    predictedScore.value = res.predictedSleepScore
    if (activeLeftTab.value === 'sensitivity') {
      calculateSensitivity()
    }
  } finally { predicting.value = false }
}

onMounted(() => {
  // 加载沙箱缓存
  loadSandboxFromLocalStorage()

  // 检查是否从画像页携带了数据过来
  const saved = localStorage.getItem('profile_to_predict')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      // 将画像数据填充到预测表单，使用 clamp 确保值在滑块范围内
      const clamp = (v, min, max) => Math.min(max, Math.max(min, v))
      form.stepCountDay = clamp(parsed.stepCountDay ?? form.stepCountDay, 0, 20000)
      form.caffeineMg = clamp(parsed.caffeineMg ?? form.caffeineMg, 0, 400)
      form.alcoholUnits = clamp(parsed.alcoholUnits ?? form.alcoholUnits, 0, 5)
      form.screenTimeBeforeBedMin = clamp(parsed.screenTimeBeforeBedMin ?? form.screenTimeBeforeBedMin, 0, 180)
      form.stressScore = clamp(parsed.stressScore ?? form.stressScore, 0, 100)
      form.activityBeforeBedMin = clamp(parsed.activityBeforeBedMin ?? form.activityBeforeBedMin, 0, 90)
      form.roomTemperatureC = clamp(parsed.roomTemperatureC ?? form.roomTemperatureC, 10, 30)
      form.ambientNoiseDb = clamp(parsed.ambientNoiseDb ?? form.ambientNoiseDb, 20, 70)
      form.bedtimeConsistencyStdMin = clamp(parsed.bedtimeConsistencyStdMin ?? form.bedtimeConsistencyStdMin, 0, 120)
      form.napDurationMinutes = clamp(parsed.napDurationMinutes ?? form.napDurationMinutes, 0, 90)
      form.age = clamp(parsed.age ?? form.age, 10, 90)
      form.bmi = clamp(parsed.bmi ?? form.bmi, 15, 40)
      // 显示提示 Banner
      fromProfileUsername.value = parsed._username || '画像用户'
      fromProfileBanner.value = true
      // 清除 localStorage，避免重复触发
      localStorage.removeItem('profile_to_predict')
      // 延迟自动触发预测，让页面渲染完成
      setTimeout(() => { onPredict() }, 300)
    } catch (e) {
      localStorage.removeItem('profile_to_predict')
    }
  }

  getModelMetrics().then(d => modelMetrics.value = d).catch(() => {})
  getFeatureImportance().then(d => { featureImportance.value = d; loadingImportance.value = false }).catch(() => { loadingImportance.value = false })
  getClusterResult().then(d => {
    clusterPoints.value = d.points || []
    clusterProfiles.value = d.profiles || []
    loadingCluster.value = false
  }).catch(() => { loadingCluster.value = false })
})
</script>

<style scoped>
/* 来自画像页的数据提示 Banner */
.profile-import-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 16px;
  background: linear-gradient(90deg, rgba(90, 171, 154, 0.12) 0%, rgba(107, 168, 217, 0.08) 100%);
  border: 1px solid rgba(90, 171, 154, 0.3);
  border-radius: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(90, 171, 154, 0.08);
}
.pib-icon {
  font-size: 14px;
  flex-shrink: 0;
}
.pib-text {
  flex: 1;
  line-height: 1.4;
}
.pib-text strong {
  color: var(--accent-teal);
  font-weight: 700;
}
.pib-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 16px;
  cursor: pointer;
  padding: 0 2px;
  line-height: 1;
  transition: color 0.2s;
  flex-shrink: 0;
}
.pib-close:hover { color: var(--accent-rose); }

/* Banner 过渡动画 */
.banner-slide-enter-active,
.banner-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 60px;
  overflow: hidden;
}
.banner-slide-enter-from,
.banner-slide-leave-to {
  opacity: 0;
  max-height: 0;
  margin-bottom: 0;
  transform: translateY(-8px);
}

.page-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  gap: 12px;
  box-sizing: border-box;
}

.row1 { grid-template-columns: 2fr 1fr; height: 44%; min-height: 260px; }
.row2 { grid-template-columns: 1fr 2fr; flex: 1; min-height: 280px; }

/* 场景预设 */
.preset-chips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.preset-label {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-right: 4px;
}
.preset-btn {
  padding: 3px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-secondary);
  font-size: 9.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.preset-btn:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.2);
  color: var(--text-primary);
}
.preset-btn.active {
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.2) 0%, rgba(107, 168, 217, 0.15) 100%);
  border-color: rgba(90, 171, 154, 0.6);
  color: #7deacb;
  box-shadow: 0 0 8px rgba(90, 171, 154, 0.1);
}

.card {
  background: linear-gradient(135deg, rgba(35, 40, 68, 0.5) 0%, rgba(20, 22, 38, 0.35) 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-lg);
  padding: 12px 18px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
.card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(ellipse at 100% 0%, rgba(255,255,255,0.012) 0%, transparent 55%);
  pointer-events: none;
}
.card-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 8px;
  flex-shrink: 0;
}
.card-title { font-family: var(--font-title); font-size: 14.5px; font-weight: 600; color: var(--text-primary); letter-spacing: 0.02em; }
.card-sub { font-size: 10px; color: var(--text-tertiary); letter-spacing: 0.05em; text-transform: uppercase; }

/* Form */
.card-form { display: flex; flex-direction: column; }
.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px 12px;
  margin-bottom: 8px;
  flex: 1;
  min-height: 0;
  align-content: space-around;
}
.fg-item { display: flex; flex-direction: column; gap: 1px; }
.fg-top { display: flex; justify-content: space-between; align-items: baseline; }
.fg-label { font-size: 10px; color: var(--text-tertiary); white-space: nowrap; }
.fg-val { font-family: var(--font-mono); font-size: 9.5px; color: var(--text-secondary); font-weight: 500; }

.slider {
  -webkit-appearance: none; appearance: none;
  width: 100%; height: 2px; border-radius: 1px;
  background: rgba(255,255,255,0.08); outline: none; margin: 1px 0;
}
.slider::-webkit-slider-thumb {
  -webkit-appearance: none; appearance: none;
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--text-primary); cursor: pointer;
}
.btn-predict { width: 100%; padding: 6px; font-size: 12px; flex-shrink: 0; }

/* Result */
.result-box {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 1px; flex: 1; min-height: 0;
}
.score-num {
  font-family: var(--font-mono); font-size: 52px; font-weight: 500;
  letter-spacing: -0.04em; line-height: 1; transition: color 0.5s ease;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.05);
}
.score-num-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  gap: 8px;
  margin-bottom: 6px;
}
.status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
  border: 1px solid transparent;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.4s ease-out;
  letter-spacing: 0.04em;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  text-shadow: 0 0 8px currentColor;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}
.score-sub { font-size: 11px; color: var(--text-tertiary); letter-spacing: 0.06em; text-transform: uppercase; }
.score-meta { display: flex; gap: 6px; margin-top: 6px; }

/* Charts */
.chart-wrap {
  flex: 1;
  min-height: 160px;
  width: 100%;
  overflow: hidden;
  position: relative;
}
.chart-wrap > div {
  height: 100% !important;
}
.chart-wrap-cluster {
  flex: 1;
  min-height: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.cluster-content {
  display: flex;
  flex-direction: row;
  flex: 1;
  min-height: 0;
  gap: 16px;
  overflow: hidden;
}
.chart-container-3d {
  flex: 3;
  min-height: 200px;
  height: 100%;
  overflow: hidden;
  position: relative;
}
.chart-container-3d > div {
  height: 100% !important;
}

/* Cluster Tags */
.cluster-tags {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: flex-start;
  padding-top: 4px;
  overflow-y: auto;
  flex-shrink: 0;
}
.ct-item-card.clickable-card {
  cursor: pointer;
  position: relative;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}
.ct-item-card.clickable-card::after {
  content: '🎯 点击代入拟合';
  position: absolute;
  right: 12px;
  bottom: 8px;
  font-size: 8px;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity 0.2s;
}
.ct-item-card.clickable-card:hover {
  transform: translateY(-1.5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border-color: rgba(255,255,255,0.06);
}
.ct-item-card.clickable-card:hover::after {
  opacity: 0.7;
}
.ct-item-card.clickable-card.card-active {
  transform: scale(1.015);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35);
  border-color: rgba(90, 171, 154, 0.4) !important;
}
.ct-item-card.clickable-card.card-active::after {
  content: '⚡ 已代入参数';
  color: var(--accent-teal);
  opacity: 0.9;
}
.ct-item-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
}
.ct-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  box-shadow: 0 0 5px currentColor;
}
.ct-name {
  font-weight: 600;
  color: var(--text-primary);
}
.ct-meta {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 9.5px;
  color: var(--text-secondary);
}
.ct-desc {
  font-size: 9.5px;
  color: var(--text-tertiary);
  line-height: 1.4;
  text-align: left;
}

/* Compact Score Header Layout */
.result-box-compact {
  padding: 4px 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 8px;
  flex-shrink: 0;
}
.score-main-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.score-num {
  font-family: var(--font-mono);
  font-size: 38px;
  font-weight: 600;
  line-height: 1;
  transition: color 0.5s ease;
  text-shadow: 0 0 12px rgba(255, 255, 255, 0.05);
}
.score-sub-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.score-sub {
  font-size: 9px;
  color: var(--text-tertiary);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.status-badge {
  font-size: 9.5px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  border: 1px solid transparent;
  backdrop-filter: blur(4px);
  text-align: center;
}
.score-metrics-col {
  margin-left: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.tag {
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: var(--font-mono);
}
.tag-teal {
  color: var(--accent-teal);
  background: rgba(90, 171, 154, 0.12);
}
.tag-amber {
  color: var(--accent-amber);
  background: rgba(201, 151, 78, 0.12);
}

/* Guidance list styling */
.pred-guidance-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.pg-head {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
  letter-spacing: 0.02em;
}
.pg-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-right: 4px;
}
.pg-card {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.04);
  background: rgba(255, 255, 255, 0.01);
  transition: all 0.25s ease;
}
.pg-card-title {
  font-size: 10px;
  font-weight: 600;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.pg-card-desc {
  font-size: 9px;
  color: var(--text-secondary);
  line-height: 1.4;
  text-align: left;
}
.pg-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
}
.pg-card--success {
  border-left: 2px solid var(--accent-teal);
  background: rgba(90, 171, 154, 0.02);
}
.pg-card--success .pg-card-title { color: var(--accent-teal); }
.pg-card--success .pg-dot { background: var(--accent-teal); box-shadow: 0 0 4px var(--accent-teal); }

.pg-card--warning {
  border-left: 2px solid var(--accent-amber);
  background: rgba(201, 151, 78, 0.02);
}
.pg-card--warning .pg-card-title { color: var(--accent-amber); }
.pg-card--warning .pg-dot { background: var(--accent-amber); box-shadow: 0 0 4px var(--accent-amber); }

.pg-card--danger {
  border-left: 2px solid var(--accent-rose);
  background: rgba(212, 133, 123, 0.02);
}
.pg-card--danger .pg-card-title { color: var(--accent-rose); }
.pg-card--danger .pg-dot { background: var(--accent-rose); box-shadow: 0 0 4px var(--accent-rose); }

.pg-empty {
  font-size: 10px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 24px 10px;
  line-height: 1.5;
  background: rgba(255,255,255,0.01);
  border: 1px dashed rgba(255,255,255,0.06);
  border-radius: 6px;
  margin-top: 10px;
  border-radius: 6px;
  margin-top: 10px;
}

/* Tabs & Custom elements for Sandbox/Solver/Sensitivity */
.card-tabs-header {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 8px;
  padding-bottom: 4px;
  flex-shrink: 0;
}
.card-tabs-header-left {
  display: flex;
  gap: 12px;
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
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--accent-teal);
  border-radius: 2px;
  box-shadow: 0 0 8px var(--accent-teal);
}

/* Goal Seeker UI */
.goal-seeker-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.seeker-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
}
.seeker-input-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: rgba(255, 255, 255, 0.02);
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.seeker-label {
  font-size: 9.5px;
  color: var(--text-secondary);
  font-weight: 500;
}
.seeker-control {
  display: flex;
  align-items: center;
  gap: 10px;
}
.seeker-slider {
  flex: 1;
}
.seeker-val {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  min-width: 40px;
  text-align: right;
}
.btn-seek {
  width: 100%;
  padding: 6px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
  flex-shrink: 0;
}
.seeker-results {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
}
.sr-header {
  font-size: 9px;
  color: var(--text-secondary);
  font-weight: 600;
}
.sr-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-right: 4px;
  background: rgba(0, 0, 0, 0.15);
  border-radius: 6px;
  padding: 6px;
  border: 1px solid rgba(255, 255, 255, 0.02);
}
.sr-item {
  display: flex;
  align-items: center;
  font-size: 9.5px;
  color: var(--text-secondary);
  padding: 3px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.01);
}
.sr-name {
  font-weight: 500;
  min-width: 65px;
}
.sr-from {
  font-family: var(--font-mono);
  color: var(--text-tertiary);
  min-width: 40px;
  text-align: right;
}
.sr-arrow {
  margin: 0 6px;
  color: var(--text-tertiary);
}
.sr-to {
  font-family: var(--font-mono);
  font-weight: 600;
  min-width: 40px;
  text-align: left;
}
.sr-diff {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 9px;
  font-weight: 500;
  color: var(--text-tertiary);
}
.sr-better {
  color: var(--accent-teal) !important;
}
.btn-apply-seek {
  width: 100%;
  padding: 5px;
  font-size: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.2) 0%, rgba(107, 168, 217, 0.15) 100%);
  border: 1px solid rgba(90, 171, 154, 0.4);
  color: #7deacb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  margin-top: 4px;
}
.btn-apply-seek:hover {
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.3) 0%, rgba(107, 168, 217, 0.25) 100%);
  border-color: rgba(90, 171, 154, 0.6);
  box-shadow: 0 0 8px rgba(90, 171, 154, 0.15);
}
.seeker-empty {
  font-size: 9.5px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 24px 10px;
  line-height: 1.5;
  background: rgba(255,255,255,0.01);
  border: 1px dashed rgba(255,255,255,0.06);
  border-radius: 6px;
  margin-top: 4px;
}
.sr-empty-msg {
  font-size: 9.5px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 12px 0;
}

/* Sensitivity analysis */
.sensitivity-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
.sens-selector-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-shrink: 0;
}
.sens-label {
  font-size: 10px;
  color: var(--text-secondary);
}
.sens-select {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  color: var(--text-primary);
  font-size: 10px;
  padding: 2px 8px;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s;
}
.sens-select:hover, .sens-select:focus {
  border-color: var(--accent-teal);
}
.chart-wrap-sens {
  flex: 1;
  min-height: 0;
  position: relative;
}

/* Sandbox Panel */
.sandbox-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.sandbox-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  gap: 6px;
}
.sb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 4px;
}
.sb-title {
  font-size: 10px;
  color: var(--text-secondary);
  font-weight: 600;
}
.btn-save-sb {
  padding: 2px 8px;
  font-size: 9px;
  background: rgba(90, 171, 154, 0.15);
  border: 1px solid rgba(90, 171, 154, 0.3);
  color: #7deacb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-save-sb:hover:not(:disabled) {
  background: rgba(90, 171, 154, 0.25);
  border-color: rgba(90, 171, 154, 0.5);
}
.sb-table-wrap {
  flex: 1;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.15);
}
.sb-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 9.5px;
  text-align: left;
}
.sb-table th {
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-tertiary);
  font-weight: 500;
  padding: 4px 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.sb-table td {
  padding: 4px 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  vertical-align: middle;
}
.sb-td-name {
  width: 75px;
}
.sb-name-input {
  background: transparent;
  border: none;
  border-bottom: 1px dashed rgba(255,255,255,0.2);
  color: var(--text-primary);
  font-size: 9.5px;
  width: 100%;
  outline: none;
  padding: 0 0 2px 0;
}
.sb-name-input:focus {
  border-bottom-style: solid;
  border-bottom-color: var(--accent-teal);
}
.sb-td-score {
  font-family: var(--font-mono);
  font-weight: 700;
  width: 35px;
}
.sb-td-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  align-items: center;
  min-height: 20px;
}
.sb-detail-tag {
  font-size: 8px;
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
}
.sb-td-actions {
  width: 75px;
  text-align: right;
}
.sb-act-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 9px;
  padding: 2px 4px;
  transition: color 0.2s;
}
.sb-act-btn.load {
  color: var(--accent-teal);
}
.sb-act-btn.load:hover {
  color: #92ffd9;
}
.sb-act-btn.delete {
  color: var(--accent-rose);
}
.sb-act-btn.delete:hover {
  color: #ff9da9;
}
.sb-empty {
  font-size: 9.5px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 24px 10px;
  line-height: 1.5;
  background: rgba(255,255,255,0.01);
  border: 1px dashed rgba(255,255,255,0.06);
  border-radius: 6px;
}
</style>
