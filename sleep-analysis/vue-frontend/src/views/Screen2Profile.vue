<template>
  <div class="profile-page">
    <div class="main-grid">
      <!-- 左栏：个人信息 + 睡眠阶段 -->
      <div class="left-column flex-col">
        <PanelCard title="个人健康档案" sub="Health Profile" class="profile-card">
          <div class="user-profile-hero">
            <!-- 管理员切换画像框 -->
            <div class="user-selector-wrapper" v-if="isRoot">
              <input 
                v-model="targetUserIdInput" 
                class="sf-input select-user-input" 
                placeholder="输入用户ID并回车..." 
                @keyup.enter="switchTargetUser"
              />
              <button class="btn-user-switch" @click="switchTargetUser">确认</button>
            </div>
            <div class="avatar-wrapper">
              <div class="avatar-glow" :style="{ background: avatarBgColor }"></div>
              <div class="avatar-circle" :style="{ background: avatarBgColor }">
                {{ profileData.userId ? (profileData.userId.startsWith('user_') ? profileData.userId.substring(5).toUpperCase() : profileData.userId.toUpperCase()) : 'U' }}
              </div>
            </div>
            <h2 class="profile-username">{{ profileData.userId || '未登录' }}</h2>
            <div class="profile-badges">
              <span class="badge-gender" :class="profileData.gender === 'female' ? 'female' : 'male'">
                {{ profileData.gender === 'female' ? '女性' : '男性' }}
              </span>
              <span class="badge-bmi">BMI {{ profileData.bmi }}</span>
            </div>
            
            <div class="info-table">
              <div class="info-row">
                <span class="row-label">年龄</span>
                <span class="row-value">{{ profileData.age }} 岁</span>
              </div>
              <div class="info-row">
                <span class="row-label">归属区域</span>
                <span class="row-value">{{ profileData.region || '未知' }}</span>
              </div>
              <div class="info-row">
                <span class="row-label">监测设备</span>
                <span class="row-value">{{ profileData.deviceModel || '未知' }}</span>
              </div>
            </div>
            
            <button class="btn-manage-profile" @click="openEditModal">
              <span class="btn-icon">⚙️</span>
              管理个人信息
            </button>
            <button class="btn-send-predict" @click="sendToPredict" :disabled="!metrics.avgSleepScore">
              <span class="btn-icon">🚀</span>
              发送至智能预测
            </button>
          </div>
        </PanelCard>

        <PanelCard title="睡眠阶段构成" sub="Sleep Stages" class="flex-1 stage-card">
          <div class="stages-container">
            <div class="stage-bar">
              <div class="stage-segment deep" :style="{ width: metrics.avgDeepSleepPct + '%' }" title="深睡眠"></div>
              <div class="stage-segment light" :style="{ width: metrics.avgLightSleepPct + '%' }" title="浅睡眠"></div>
              <div class="stage-segment rem" :style="{ width: metrics.avgRemSleepPct + '%' }" title="REM快速眼动"></div>
              <div class="stage-segment awake" :style="{ width: metrics.avgAwakePct + '%' }" title="清醒"></div>
            </div>
            
            <div class="stage-legend-list">
              <div class="stage-legend-item">
                <div class="legend-meta">
                  <span class="legend-dot deep"></span>
                  <span class="legend-name">深睡眠</span>
                </div>
                <span class="legend-val">{{ metrics.avgDeepSleepPct }}%</span>
              </div>
              <div class="stage-legend-item">
                <div class="legend-meta">
                  <span class="legend-dot light"></span>
                  <span class="legend-name">浅睡眠</span>
                </div>
                <span class="legend-val">{{ metrics.avgLightSleepPct }}%</span>
              </div>
              <div class="stage-legend-item">
                <div class="legend-meta">
                  <span class="legend-dot rem"></span>
                  <span class="legend-name">REM 睡眠</span>
                </div>
                <span class="legend-val">{{ metrics.avgRemSleepPct }}%</span>
              </div>
              <div class="stage-legend-item">
                <div class="legend-meta">
                  <span class="legend-dot awake"></span>
                  <span class="legend-name">清醒时间</span>
                </div>
                <span class="legend-val">{{ metrics.avgAwakePct }}%</span>
              </div>
            </div>
          </div>
        </PanelCard>
      </div>

      <!-- 中栏：核心指标 + 趋势图 + 处方 (Flex: 1.5) -->
      <div class="mid-column flex-col">
        <!-- 顶部核心指标八宫格 -->
        <div class="metrics-dashboard">
          <div class="metric-tile card-glow-amber">
            <div class="tile-header">
              <span class="tile-icon">🎯</span>
              <span class="tile-title">睡眠得分</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-amber">{{ metrics.avgSleepScore ?? '--' }}</span>
              <span class="tile-unit">分</span>
            </div>
          </div>
          <div class="metric-tile card-glow-teal">
            <div class="tile-header">
              <span class="tile-icon">⚡</span>
              <span class="tile-title">睡眠效率</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-teal">{{ metrics.avgSleepEfficiency ?? '--' }}</span>
              <span class="tile-unit">%</span>
            </div>
          </div>
          <div class="metric-tile card-glow-sky">
            <div class="tile-header">
              <span class="tile-icon">⏳</span>
              <span class="tile-title">实际时长</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-sky">{{ metrics.avgSleepHours ?? '--' }}</span>
              <span class="tile-unit">小时</span>
            </div>
          </div>
          <div class="metric-tile card-glow-rose">
            <div class="tile-header">
              <span class="tile-icon">🚪</span>
              <span class="tile-title">入睡延迟</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-rose">{{ metrics.avgSleepLatency ?? '--' }}</span>
              <span class="tile-unit">分钟</span>
            </div>
          </div>
          <div class="metric-tile card-glow-teal">
            <div class="tile-header">
              <span class="tile-icon">🫁</span>
              <span class="tile-title">最低血氧</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-teal">{{ metrics.avgSpo2Min ?? '--' }}</span>
              <span class="tile-unit">%</span>
            </div>
          </div>
          <div class="metric-tile card-glow-rose">
            <div class="tile-header">
              <span class="tile-icon">📢</span>
              <span class="tile-title">打鼾事件</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-rose">{{ metrics.avgSnoreEvents ?? '--' }}</span>
              <span class="tile-unit">次</span>
            </div>
          </div>
          <div class="metric-tile card-glow-amber">
            <div class="tile-header">
              <span class="tile-icon">🧠</span>
              <span class="tile-title">日间压力</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-amber">{{ metrics.avgStressScore ?? '--' }}</span>
              <span class="tile-unit">分</span>
            </div>
          </div>
          <div class="metric-tile card-glow-lavender">
            <div class="tile-header">
              <span class="tile-icon">💊</span>
              <span class="tile-title">用药频率</span>
            </div>
            <div class="tile-body">
              <span class="tile-value text-glow-lavender">{{ metrics.medicationRatioPct ?? '--' }}</span>
              <span class="tile-unit">%</span>
            </div>
          </div>
        </div>

        <!-- 中部：10日趋势图表卡片 (全宽) -->
        <PanelCard title="最近 10 天睡眠效率与得分趋势" sub="10-Day Health Trend" class="trend-card-full">
          <div class="trend-chart-container">
            <EChart :option="historyOption" height="130px" width="100%" />
          </div>
        </PanelCard>

        <!-- 底部：双卡片拉伸布局 -->
        <div class="bottom-split flex-1">
          <!-- 诊断与处方卡片 -->
          <PanelCard title="临床评估与行为处方" sub="CBT-I & Prescription" class="prescription-card flex-1">
            <div class="prescription-container">
              <div class="clinical-summary">
                <span class="summary-label">临床画像结论：</span>
                <span class="persona-badge" :class="personaBadgeClass">{{ profileData.personaTitle || '评估计算中' }}</span>
                <p class="summary-desc">{{ profileData.personaDesc || '正在加载临床诊断，请稍候...' }}</p>
              </div>
              
              <div class="guidance-list">
                <!-- 管理员账号或空数据友情提示 -->
                <div v-if="profileData.userId === 'root' && (!metrics.avgSleepScore || metrics.avgSleepScore === 0)" class="guide-card guide-card--warning">
                  <div class="gc-header">
                    <span class="gc-tag">系统提示</span>
                    <span class="gc-title">管理员数据限制</span>
                  </div>
                  <div class="gc-content">
                    您当前登录的是 <b>root (系统管理员)</b> 账号。系统管理员在睡眠监测数据库中无心率、血氧或打鼾日志记录。
                    <br/><br/>
                    <b>请按照以下步骤查看完整的个人画像：</b>
                    <br/>
                    1. 点击左下角账号区的“退出登录”按钮。
                    <br/>
                    2. 使用本平台检索出的睡眠用户 ID（如：<b>user_00001</b> 或 <b>user_01339</b>），输入密码 <b>123456</b> 重新登录。
                    <br/>
                    3. 再次进入本页面，系统将自动载入该用户的专属画像与 CBT-I 行为指导方案！
                  </div>
                </div>
                <div 
                  v-else
                  v-for="(adv, index) in profileData.advices" 
                  :key="index" 
                  class="guide-card" 
                  :class="'guide-card--' + adv.level"
                >
                  <div class="gc-header">
                    <span class="gc-tag">{{ adv.tag }}</span>
                    <span class="gc-title">{{ adv.title }}</span>
                  </div>
                  <div class="gc-content">{{ adv.content }}</div>
                </div>
              </div>
            </div>
          </PanelCard>

          <!-- 右下：生活行为与暴露评估卡片 -->
          <PanelCard title="生活行为与环境因素评估" sub="Behavior & Environment" class="exposure-card flex-1">
            <div class="exposure-container" v-if="profileData.userId !== 'root' || (metrics.avgSleepScore && metrics.avgSleepScore > 0)">
              <div class="exposure-item">
                <div class="ei-header">
                  <span class="ei-icon">🚶</span>
                  <span class="ei-name">日间身体活动</span>
                  <span class="ei-value">{{ metrics.avgStepCountDay || 0 }} 步</span>
                </div>
                <div class="ei-progress-wrap">
                  <div class="ei-progress-bar" :style="{ width: Math.min(100, ((metrics.avgStepCountDay || 0) / 10000) * 100) + '%', backgroundColor: 'var(--accent-teal)' }"></div>
                </div>
                <div class="ei-footer">
                  <span>合理目标: 10000 步</span>
                  <span>{{ Math.round(((metrics.avgStepCountDay || 0) / 10000) * 100) }}%</span>
                </div>
              </div>

              <div class="exposure-item">
                <div class="ei-header">
                  <span class="ei-icon">☕</span>
                  <span class="ei-name">咖啡因摄入</span>
                  <span class="ei-value">{{ metrics.avgCaffeineMg || 0 }} mg</span>
                </div>
                <div class="ei-progress-wrap">
                  <div class="ei-progress-bar" :style="{ width: Math.min(100, ((metrics.avgCaffeineMg || 0) / 300) * 100) + '%', backgroundColor: (metrics.avgCaffeineMg || 0) > 150 ? 'var(--accent-amber)' : 'var(--accent-teal)' }"></div>
                </div>
                <div class="ei-footer">
                  <span>日均上限: 300 mg</span>
                  <span>{{ Math.round(((metrics.avgCaffeineMg || 0) / 300) * 100) }}%</span>
                </div>
              </div>

              <div class="exposure-item">
                <div class="ei-header">
                  <span class="ei-icon">📱</span>
                  <span class="ei-name">睡前屏幕暴露</span>
                  <span class="ei-value">{{ metrics.avgScreenTimeBeforeBedMin || 0 }} m</span>
                </div>
                <div class="ei-progress-wrap">
                  <div class="ei-progress-bar" :style="{ width: Math.min(100, ((metrics.avgScreenTimeBeforeBedMin || 0) / 120) * 100) + '%', backgroundColor: (metrics.avgScreenTimeBeforeBedMin || 0) > 45 ? 'var(--accent-rose)' : 'var(--accent-teal)' }"></div>
                </div>
                <div class="ei-footer">
                  <span>合理区间: &lt; 30m</span>
                  <span>{{ Math.round(((metrics.avgScreenTimeBeforeBedMin || 0) / 120) * 100) }}%</span>
                </div>
              </div>

              <div class="exposure-item">
                <div class="ei-header">
                  <span class="ei-icon">🌡️</span>
                  <span class="ei-name">卧室平均温度</span>
                  <span class="ei-value">{{ metrics.avgRoomTemperatureC || 0 }} ℃</span>
                </div>
                <div class="ei-progress-wrap">
                  <div class="ei-progress-bar" :style="{ width: Math.max(20, Math.min(100, (((metrics.avgRoomTemperatureC || 20) - 10) / 20) * 100)) + '%', backgroundColor: ((metrics.avgRoomTemperatureC || 20) >= 18 && (metrics.avgRoomTemperatureC || 20) <= 22) ? 'var(--accent-teal)' : 'var(--accent-amber)' }"></div>
                </div>
                <div class="ei-footer">
                  <span>舒适区间: 18 - 22 ℃</span>
                  <span>{{ ((metrics.avgRoomTemperatureC || 20) >= 18 && (metrics.avgRoomTemperatureC || 20) <= 22) ? '舒适' : '偏离' }}</span>
                </div>
              </div>

              <div class="exposure-item">
                <div class="ei-header">
                  <span class="ei-icon">🔊</span>
                  <span class="ei-name">夜间环境噪音</span>
                  <span class="ei-value">{{ metrics.avgAmbientNoiseDb || 0 }} dB</span>
                </div>
                <div class="ei-progress-wrap">
                  <div class="ei-progress-bar" :style="{ width: Math.min(100, ((metrics.avgAmbientNoiseDb || 0) / 60) * 100) + '%', backgroundColor: (metrics.avgAmbientNoiseDb || 0) > 40 ? 'var(--accent-rose)' : 'var(--accent-teal)' }"></div>
                </div>
                <div class="ei-footer">
                  <span>安静水平: &lt; 35dB</span>
                  <span>{{ (metrics.avgAmbientNoiseDb || 0) > 40 ? '嘈杂' : '安静' }}</span>
                </div>
              </div>
            </div>
            <div class="exposure-empty" v-else>
              管理员账号暂无行为与环境监测数据。
            </div>
          </PanelCard>
        </div>
      </div>

      <!-- 右栏：健康指征雷达 + 睡眠稳定性评估 -->
      <div class="right-column flex-col">
        <!-- 雷达图 -->
        <PanelCard title="健康指征雷达" sub="Habit Radar" class="radar-card">
          <div class="radar-chart-container">
            <EChart :option="radarOption" height="220px" width="100%" />
          </div>
        </PanelCard>

        <!-- 睡眠稳定性与就寝规律度 -->
        <PanelCard title="睡眠稳定性与就寝规律度" sub="Sleep Stability" class="flex-1 stability-card">
          <div class="stability-container" v-if="stability.validDays >= 3">
            <!-- 稳定性总分 -->
            <div class="stability-score-hero">
              <div class="ss-ring-wrapper">
                <svg class="ss-ring" viewBox="0 0 100 100">
                  <circle class="ss-ring-bg" cx="50" cy="50" r="42" />
                  <circle class="ss-ring-fill" cx="50" cy="50" r="42"
                    :stroke-dasharray="2 * Math.PI * 42"
                    :stroke-dashoffset="2 * Math.PI * 42 * (1 - stability.score / 100)"
                    :stroke="stability.scoreColor"
                    :style="{ filter: 'drop-shadow(0 0 5px ' + stability.scoreColor + ')' }" />
                </svg>
                <div class="ss-ring-text">
                  <span class="ss-score" :style="{ textShadow: '0 0 10px ' + stability.scoreColor }">{{ stability.score }}</span>
                  <span class="ss-label">稳定性评分</span>
                </div>
              </div>
              <div class="ss-hero-meta">
                <div class="meta-row">
                  <span class="meta-label">规律评估</span>
                  <span class="ss-level-badge-glass" :style="{ color: stability.scoreColor, borderColor: stability.scoreColor, boxShadow: '0 0 8px ' + stability.scoreColor + '2b', background: stability.scoreColor + '0d' }">
                    {{ stability.levelLabel }}
                  </span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">评估样本</span>
                  <span class="meta-value">{{ stability.validDays }} 天数据</span>
                </div>
              </div>
            </div>

            <!-- 指标条 -->
            <div class="stability-metrics">
              <div class="sm-item">
                <div class="sm-header">
                  <span class="sm-bullet-glow" :style="{ backgroundColor: stability.scoreColor, boxShadow: '0 0 8px ' + stability.scoreColor }"></span>
                  <span class="sm-name">睡眠得分波动</span>
                  <span class="sm-value" :style="{ color: stability.scoreColor }">±{{ stability.scoreStddev }} <span class="sm-unit">分</span></span>
                </div>
                <div class="sm-bar-wrap">
                  <div class="sm-bar" :style="stabilityScoreBarStyle"></div>
                </div>
                <div class="sm-range"><span>稳定 (波动极小)</span><span>剧烈起伏</span></div>
              </div>
              <div class="sm-item">
                <div class="sm-header">
                  <span class="sm-bullet-glow" :style="{ backgroundColor: stability.effColor, boxShadow: '0 0 8px ' + stability.effColor }"></span>
                  <span class="sm-name">效率变化幅度</span>
                  <span class="sm-value" :style="{ color: stability.effColor }">±{{ stability.efficiencyStddev }} <span class="sm-unit">%</span></span>
                </div>
                <div class="sm-bar-wrap">
                  <div class="sm-bar" :style="stabilityEfficiencyBarStyle"></div>
                </div>
                <div class="sm-range"><span>规律 (变化平缓)</span><span>大幅起伏</span></div>
              </div>
              <div class="sm-item">
                <div class="sm-header">
                  <span class="sm-bullet-glow" :style="{ backgroundColor: stability.durColor, boxShadow: '0 0 8px ' + stability.durColor }"></span>
                  <span class="sm-name">就寝时长差异</span>
                  <span class="sm-value" :style="{ color: stability.durColor }">±{{ stability.durationStddev }} <span class="sm-unit">小时</span></span>
                </div>
                <div class="sm-bar-wrap">
                  <div class="sm-bar" :style="stabilityDurationBarStyle"></div>
                </div>
                <div class="sm-range"><span>规律 (作息固定)</span><span>大幅偏移</span></div>
              </div>
            </div>

            <!-- 结论描述 -->
            <div class="stability-conclusion" :style="{ borderLeftColor: stability.scoreColor, boxShadow: 'inset 3px 0 0 ' + stability.scoreColor }">
              <span class="conclusion-title">💡 评估报告结论</span>
              <p class="conclusion-text-content">{{ stability.conclusion }}</p>
            </div>
          </div>
          <div class="exposure-empty" v-else>
            近10天数据不足 (仅 {{ stability.validDays }} 天)，需要更多记录方可评估稳定性。
          </div>
        </PanelCard>
      </div>
    </div>
  </div>

  <!-- 玻态个人信息与密码修改模态框 -->
  <div class="modal-backdrop" v-if="showEditModal" @click.self="showEditModal = false">
    <div class="modal-card glass-modal">
      <div class="modal-header">
        <span class="modal-title">⚙&nbsp;&nbsp;管理个人信息</span>
        <button class="modal-close" @click="showEditModal = false">&times;</button>
      </div>

      <!-- 标签页头部切换 -->
      <div class="modal-tabs">
        <button class="tab-btn" :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">
          修改信息
        </button>
        <button class="tab-btn" :class="{ active: activeTab === 'password' }" @click="activeTab = 'password'">
          修改密码
        </button>
      </div>

      <!-- 消息反馈提示区 -->
      <div class="modal-alert-box" :class="msgType" v-if="modalMsg">
        {{ modalMsg }}
      </div>

      <div class="modal-body" v-if="activeTab === 'info'">
        <div class="form-group">
          <label class="form-label">生理性别</label>
          <div class="gender-radio-group">
            <label class="gender-radio-label" :class="{ active: editForm.gender === 'male' }">
              <input type="radio" v-model="editForm.gender" value="male" class="hidden-radio" />
              <span>👨 男性</span>
            </label>
            <label class="gender-radio-label" :class="{ active: editForm.gender === 'female' }">
              <input type="radio" v-model="editForm.gender" value="female" class="hidden-radio" />
              <span>👩 女性</span>
            </label>
          </div>
        </div>
        
        <div class="form-row-split">
          <div class="form-group flex-1">
            <label class="form-label">年龄 (岁)</label>
            <input type="number" v-model.number="editForm.age" min="1" max="120" class="glass-input" />
          </div>
          <div class="form-group flex-1" style="margin-left: 12px;">
            <label class="form-label">BMI 指数</label>
            <input type="number" v-model.number="editForm.bmi" step="0.1" min="10" max="50" class="glass-input" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">居住区域</label>
          <input type="text" v-model="editForm.region" placeholder="请输入常住省份/城市" class="glass-input" />
        </div>

        <div class="form-group">
          <label class="form-label">佩戴智能设备</label>
          <input type="text" v-model="editForm.deviceModel" placeholder="请输入健康手环/手表型号" class="glass-input" />
        </div>
      </div>

      <div class="modal-body" v-else>
        <div class="form-group">
          <label class="form-label">原密码</label>
          <input type="password" v-model="pwdForm.oldPassword" placeholder="请输入当前密码" class="glass-input" />
        </div>
        
        <div class="form-group">
          <label class="form-label">新密码</label>
          <input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码 (不少于6位)" class="glass-input" />
        </div>

        <div class="form-group">
          <label class="form-label">确认新密码</label>
          <input type="password" v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" class="glass-input" />
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-secondary" @click="showEditModal = false">取消</button>
        <button class="btn-primary" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : (activeTab === 'info' ? '提交修改' : '确认修改密码') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PanelCard from '../components/PanelCard.vue'
import EChart from '../components/EChart.vue'
import { getProfileDetail, updateProfileDetail, changePassword } from '../api/profile'

const router = useRouter()
const loading = ref(true)

const username = ref(localStorage.getItem('username') || '')
const isRoot = computed(() => username.value === 'root')
const targetUserIdInput = ref('')
const currentTargetUserId = ref('')

async function switchTargetUser() {
  const target = targetUserIdInput.value.trim()
  try {
    loading.value = true
    // 显式先拉一次确保目标用户存在，防错
    const data = await getProfileDetail(target)
    if (data) {
      currentTargetUserId.value = target
      profileData.value = data
      // 成功后再正式重新调用整体属性的加载
      await fetchProfileData()
    }
  } catch (e) {
    alert(`切换用户画像失败: ${e.response?.data?.message || e.message}`)
  } finally {
    loading.value = false
  }
}

const profileData = ref({
  userId: '',
  age: 30,
  gender: '',
  bmi: 22.0,
  region: '',
  deviceModel: '',
  personaTitle: '',
  personaDesc: '',
  advices: [],
  metrics: {},
  history: []
})

const metrics = computed(() => profileData.value.metrics || {})

// ---- 睡眠稳定性与就寝规律度评估 ----
const stability = computed(() => {
  const history = profileData.value.history || []
  const validDays = history.length

  if (validDays < 3) {
    return { validDays, score: 0, scoreColor: '#888', levelLabel: '数据不足', scoreStddev: '--', efficiencyStddev: '--', durationStddev: '--', effColor: '#888', durColor: '#888', conclusion: '', scoreBarPct: 0, effBarPct: 0, durBarPct: 0 }
  }

  const scores = history.map(d => d.sleep_score).filter(v => v != null)
  const effs = history.map(d => d.sleep_efficiency_pct).filter(v => v != null)
  const hours = history.map(d => d.sleepHours).filter(v => v != null)

  const stddev = (arr) => {
    const n = arr.length
    if (n < 2) return 0
    const mean = arr.reduce((s, v) => s + v, 0) / n
    return Math.sqrt(arr.reduce((s, v) => s + (v - mean) ** 2, 0) / n)
  }

  const scoreStddev = Math.round(stddev(scores) * 10) / 10
  const efficiencyStddev = Math.round(stddev(effs) * 10) / 10
  const durationStddev = Math.round(stddev(hours) * 100) / 100

  // 标准化：得分标准差 0-15+ 映射 0-100, 效率标准差 0-12+ 映射 0-100, 时长方差 0-1.5+ 映射 0-100
  const scoreRisk = Math.min(100, (scoreStddev / 15) * 100)
  const effRisk = Math.min(100, (efficiencyStddev / 12) * 100)
  const durRisk = Math.min(100, (durationStddev / 1.5) * 100)

  const overallRisk = Math.round((scoreRisk * 0.42 + effRisk * 0.33 + durRisk * 0.25))
  const finalScore = Math.max(10, Math.round(100 - overallRisk))

  let scoreColor, levelLabel, conclusion
  if (finalScore >= 80) {
    scoreColor = '#5aab9a'
    levelLabel = '优'
    conclusion = '您的睡眠模式非常稳定，近10天得分、效率、时长波动极小，已形成健康的睡眠节律，请继续保持。'
  } else if (finalScore >= 60) {
    scoreColor = '#c9974e'
    levelLabel = '良'
    conclusion = '您的睡眠整体较为规律，但偶有波动。建议注意周末补觉幅度，尽量固定每日就寝和起床时间。'
  } else if (finalScore >= 40) {
    scoreColor = '#e09145'
    levelLabel = '一般'
    conclusion = '您的睡眠稳定性偏低，得分和时长存在明显起伏。不规律的作息会削弱睡眠质量，建议制定并严格执行固定的作息时间表。'
  } else {
    scoreColor = '#d9594c'
    levelLabel = '差'
    conclusion = '您的睡眠模式极不稳定，得分和时长波动剧烈，存在熬夜-补觉恶性循环风险。强烈建议设定22:00-23:00固定就寝时间，避免周末作息大幅偏移。'
  }

  const scoreBarPct = scoreRisk
  const effBarPct = effRisk
  const durBarPct = durRisk

  const effColor = effRisk > 70 ? '#d9594c' : effRisk > 40 ? '#e09145' : '#5aab9a'
  const durColor = durRisk > 70 ? '#d9594c' : durRisk > 40 ? '#e09145' : '#5aab9a'

  return {
    validDays, score: finalScore, scoreColor, levelLabel,
    scoreStddev, efficiencyStddev, durationStddev,
    effColor, durColor,
    conclusion,
    scoreBarPct, effBarPct, durBarPct
  }
})

const stabilityScoreBarStyle = computed(() => ({
  width: Math.min(100, stability.value.scoreBarPct || 0) + '%',
  background: stability.value.scoreBarPct > 70
    ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber), var(--accent-rose))'
    : stability.value.scoreBarPct > 40
      ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber))'
      : 'var(--accent-teal)'
}))

const stabilityEfficiencyBarStyle = computed(() => ({
  width: Math.min(100, stability.value.effBarPct || 0) + '%',
  background: stability.value.effBarPct > 70
    ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber), var(--accent-rose))'
    : stability.value.effBarPct > 40
      ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber))'
      : 'var(--accent-teal)'
}))

const stabilityDurationBarStyle = computed(() => ({
  width: Math.min(100, stability.value.durBarPct || 0) + '%',
  background: stability.value.durBarPct > 70
    ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber), var(--accent-rose))'
    : stability.value.durBarPct > 40
      ? 'linear-gradient(90deg, var(--accent-teal), var(--accent-amber))'
      : 'var(--accent-teal)'
}))

const avatarBgColor = computed(() => {
  const username = profileData.value.userId || ''
  const colors = ['#c9974e', '#5aab9a', '#6ba8d9', '#a48cdb']
  let code = 0
  for (let i = 0; i < username.length; i++) {
    code += username.charCodeAt(i)
  }
  return colors[code % colors.length]
})

const personaBadgeClass = computed(() => {
  const title = profileData.value.personaTitle || ''
  if (title.includes('卓越')) return 'badge-success'
  if (title.includes('稳健')) return 'badge-warning'
  return 'badge-danger'
})

const radarOption = computed(() => {
  if (!metrics.value.avgSleepScore) return {}
  
  const durationScore = Math.min(100, (metrics.value.avgSleepHours || 0) * 12)
  const efficiencyScore = metrics.value.avgSleepEfficiency || 0
  const latencyScore = Math.max(0, 100 - (metrics.value.avgSleepLatency || 0) * 2.5)
  const respScore = Math.max(0, Math.min(100, ((metrics.value.avgSpo2Min || 95) - 80) * 6.6))
  const stressScore = Math.max(0, 100 - (metrics.value.avgStressScore || 0))
  
  return {
    radar: {
      indicator: [
        { name: '时长', max: 100 },
        { name: '效率', max: 100 },
        { name: '入睡速度', max: 100 },
        { name: '血氧', max: 100 },
        { name: '抗压', max: 100 }
      ],
      shape: 'polygon',
      radius: '36%',
      center: ['50%', '48%'],
      axisName: {
        color: 'rgba(255,255,255,0.72)',
        fontSize: 11,
        fontFamily: 'Inter, sans-serif',
        padding: [3, 4]
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(255, 255, 255, 0.005)', 'rgba(255, 255, 255, 0.015)']
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.05)'
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.05)'
        }
      }
    },
    series: [
      {
        name: '睡眠特征',
        type: 'radar',
        data: [
          {
            value: [durationScore, efficiencyScore, latencyScore, respScore, stressScore],
            name: '健康行为习惯画像',
            itemStyle: {
              color: '#5aab9a'
            },
            areaStyle: {
              color: 'rgba(90, 171, 154, 0.16)'
            },
            lineStyle: {
              width: 1.5
            }
          }
        ]
      }
    ]
  }
})

const historyOption = computed(() => {
  const data = [...(profileData.value.history || [])].reverse()
  const dates = data.map(d => d.date_recorded ? d.date_recorded.substring(5) : '')
  const scores = data.map(d => d.sleep_score)
  const efficiencies = data.map(d => d.sleep_efficiency_pct)
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(20,20,30,0.94)',
      borderColor: 'rgba(255,255,255,0.08)',
      borderWidth: 1,
      textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 12 },
      extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,0.45); border-radius:12px; padding:10px 14px;',
      formatter: (params) => {
        let res = `<div style="font-weight:600;margin-bottom:6px;font-size:12px">${params[0].name}</div>`
        params.forEach(p => {
          const color = p.seriesName === '睡眠得分' ? '#c9974e' : '#5aab9a'
          res += `<div style="margin:3px 0;display:flex;justify-content:space-between;gap:20px;color:rgba(255,255,255,0.6)">
            <span><span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:${color};margin-right:6px"></span>${p.seriesName}</span>
            <span style="font-weight:600;font-family:var(--font-mono)">${p.value}${p.seriesName === '睡眠得分' ? '分' : '%'}</span>
          </div>`
        })
        return res
      }
    },
    legend: {
      data: ['睡眠得分', '睡眠效率'],
      textStyle: { color: 'rgba(255,255,255,0.4)', fontSize: 11 },
      right: 10, top: 0
    },
    grid: { left: 35, right: 10, top: 35, bottom: 25 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
      axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      min: 40, max: 100,
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } },
      axisLabel: { color: 'rgba(255,255,255,0.3)', fontSize: 10 }
    },
    series: [
      {
        name: '睡眠效率', type: 'bar', barWidth: 8,
        itemStyle: {
          borderRadius: [3, 3, 0, 0],
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(90, 171, 154, 0.7)' },
              { offset: 1, color: 'rgba(90, 171, 154, 0.05)' }
            ]
          }
        },
        data: efficiencies
      },
      {
        name: '睡眠得分', type: 'line', smooth: true, symbolSize: 5,
        lineStyle: { width: 2.5, color: '#c9974e', shadowBlur: 8, shadowColor: 'rgba(201,151,78,0.35)' },
        itemStyle: { color: '#c9974e' },
        data: scores
      }
    ]
  }
})

// 发送至智能预测功能
function sendToPredict() {
  const m = metrics.value || {}
  const p = profileData.value || {}
  const payload = {
    stepCountDay: Math.round(m.avgStepCountDay || 6000),
    caffeineMg: Math.round(m.avgCaffeineMg || 100),
    alcoholUnits: parseFloat((m.avgAlcohol || 0).toFixed(1)),
    screenTimeBeforeBedMin: Math.round(m.avgScreenTimeBeforeBedMin || 60),
    stressScore: Math.round(m.avgStressScore || 40),
    activityBeforeBedMin: 20,
    roomTemperatureC: parseFloat((m.avgRoomTemperatureC || 21).toFixed(1)),
    ambientNoiseDb: Math.round(m.avgAmbientNoiseDb || 35),
    bedtimeConsistencyStdMin: 20,
    napDurationMinutes: 0,
    age: Math.round(p.age || 35),
    bmi: parseFloat((p.bmi || 22.5).toFixed(1)),
    _fromProfile: true,
    _username: p.userId || ''
  }
  localStorage.setItem('profile_to_predict', JSON.stringify(payload))
  router.push('/screen4')
}

const showEditModal = ref(false)
const saving = ref(false)
const activeTab = ref('info') // 'info' or 'password'
const modalMsg = ref('')
const msgType = ref('') // 'error' or 'success'

const editForm = ref({
  gender: 'male',
  age: 30,
  bmi: 22.0,
  region: '',
  deviceModel: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const openEditModal = () => {
  activeTab.value = 'info'
  modalMsg.value = ''
  msgType.value = ''
  editForm.value = {
    gender: profileData.value.gender || 'male',
    age: profileData.value.age || 30,
    bmi: profileData.value.bmi || 22.0,
    region: profileData.value.region || '',
    deviceModel: profileData.value.deviceModel || ''
  }
  pwdForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showEditModal.value = true
}

const fetchProfileData = async () => {
  try {
    const data = await getProfileDetail(currentTargetUserId.value)
    if (data) {
      profileData.value = data
    }
  } catch (e) {
    console.error('获取个人画像失败', e)
  }
}

const handleSave = async () => {
  modalMsg.value = ''
  msgType.value = ''
  
  if (activeTab.value === 'info') {
    try {
      saving.value = true
      const savePayload = {
        ...editForm.value,
        targetUserId: currentTargetUserId.value
      }
      await updateProfileDetail(savePayload)
      await fetchProfileData()
      showEditModal.value = false
    } catch (err) {
      modalMsg.value = err.message || '保存个人信息失败'
      msgType.value = 'error'
    } finally {
      saving.value = false
    }
  } else {
    // Validate passwords
    if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
      modalMsg.value = '密码不能为空'
      msgType.value = 'error'
      return
    }
    if (pwdForm.value.newPassword.length < 6) {
      modalMsg.value = '新密码长度不能小于 6 位'
      msgType.value = 'error'
      return
    }
    if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
      modalMsg.value = '两次输入的新密码不一致'
      msgType.value = 'error'
      return
    }

    try {
      saving.value = true
      await updateProfileDetail(editForm.value) // We can keep info updated
      await changePassword(pwdForm.value.oldPassword, pwdForm.value.newPassword)
      modalMsg.value = '密码修改成功！1.5秒后将自动登出重新登录...'
      msgType.value = 'success'
      
      setTimeout(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.hash = '/login'
      }, 1500)
    } catch (err) {
      modalMsg.value = err.message || '修改密码失败，请检查原密码'
      msgType.value = 'error'
    } finally {
      saving.value = false
    }
  }
}

onMounted(async () => {
  try {
    await fetchProfileData()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Page Layout */
.profile-page {
  height: calc(100vh - 110px);
  padding: 12px;
  box-sizing: border-box;
  overflow: hidden;
}

.main-grid {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 12px;
  height: 100%;
}

.flex-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.flex-1 {
  flex: 1;
  min-height: 0;
}

.left-column {
  width: 240px;
  min-width: 240px;
  height: 100%;
}

.mid-column {
  flex: 1;
  height: 100%;
  min-width: 0;
}

.right-column {
  width: 280px;
  min-width: 280px;
  height: 100%;
}

.profile-card {
  flex: 1.5;
  min-height: 0;
}

/* User Card */
.user-profile-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
  height: 100%;
  justify-content: flex-start;
  gap: 6px;
  width: 100%;
}

/* 用户选择器样式 */
.user-selector-wrapper {
  display: flex;
  width: 100%;
  padding: 0 10px;
  gap: 6px;
  margin-bottom: 12px;
  box-sizing: border-box;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}
.select-user-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  color: var(--text-primary) !important;
  font-size: 10px !important;
  padding: 4px 8px !important;
  border-radius: 6px !important;
  outline: none;
}
.select-user-input:focus {
  border-color: rgba(90, 171, 154, 0.5) !important;
}
.btn-user-switch {
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.15) 0%, rgba(107, 168, 217, 0.1) 100%);
  border: 1px solid rgba(90, 171, 154, 0.3);
  color: var(--accent-teal);
  font-size: 9.5px;
  padding: 0 8px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}
.btn-user-switch:hover {
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.25) 0%, rgba(107, 168, 217, 0.18) 100%);
  border-color: rgba(90, 171, 154, 0.6);
  color: #7deacb;
}

.avatar-wrapper {
  position: relative;
  width: 70px;
  height: 70px;
  margin-bottom: 4px;
}

.avatar-glow {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  filter: blur(10px);
  opacity: 0.4;
  animation: pulse-glow 3s infinite alternate;
}

.avatar-circle {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  color: #fff;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  border: 2.5px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.profile-username {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
  font-family: var(--font-mono);
  letter-spacing: -0.01em;
}

.profile-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.badge-gender {
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}
.badge-gender.female {
  color: var(--accent-rose);
  background: rgba(212, 133, 123, 0.12);
  border: 1px solid rgba(212, 133, 123, 0.25);
}
.badge-gender.male {
  color: var(--accent-sky);
  background: rgba(107, 168, 217, 0.12);
  border: 1px solid rgba(107, 168, 217, 0.25);
}
.badge-bmi {
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.info-table {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12.5px;
}

.row-label {
  color: var(--text-tertiary);
}

.row-value {
  color: var(--text-primary);
  font-weight: 600;
}

/* Sleep Stages Card */
.stage-card {
  flex: 1;
  min-height: 0;
}

.stages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  justify-content: center;
}

.stage-bar {
  display: flex;
  height: 12px;
  border-radius: 6px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.stage-segment {
  height: 100%;
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.stage-segment.deep { background: linear-gradient(90deg, #3d8f80, #5aab9a); }
.stage-segment.light { background: linear-gradient(90deg, #b8853b, #c9974e); }
.stage-segment.rem { background: linear-gradient(90deg, #a6655c, #d4857b); }
.stage-segment.awake { background: linear-gradient(90deg, #7c6fa6, #a48cdb); }

.stage-legend-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 12px;
}

.stage-legend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.legend-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.legend-dot.deep { background: var(--accent-teal); box-shadow: 0 0 6px var(--accent-teal); }
.legend-dot.light { background: var(--accent-amber); box-shadow: 0 0 6px var(--accent-amber); }
.legend-dot.rem { background: var(--accent-rose); box-shadow: 0 0 6px var(--accent-rose); }
.legend-dot.awake { background: var(--accent-lavender); box-shadow: 0 0 6px var(--accent-lavender); }

.legend-name {
  color: var(--text-secondary);
}

.legend-val {
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-weight: 600;
}

/* Metrics Dashboard */
.metrics-dashboard {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  flex: none;
}

.metric-tile {
  background: rgba(255, 255, 255, 0.012);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.metric-tile:hover {
  background: rgba(255, 255, 255, 0.025);
  transform: translateY(-2px);
}

.tile-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tile-icon {
  font-size: 14px;
}

.tile-title {
  font-size: 10.5px;
  font-weight: 500;
  color: var(--text-secondary);
}

.tile-body {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin-top: 2px;
}

.tile-value {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
}

.tile-unit {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-left: 2px;
}

/* Glow Border Accents for Tiles */
.card-glow-amber { border-bottom: 2.5px solid var(--accent-amber); }
.card-glow-teal { border-bottom: 2.5px solid var(--accent-teal); }
.card-glow-sky { border-bottom: 2.5px solid var(--accent-sky); }
.card-glow-rose { border-bottom: 2.5px solid var(--accent-rose); }
.card-glow-lavender { border-bottom: 2.5px solid var(--accent-lavender); }

.text-glow-amber { color: var(--accent-amber); text-shadow: 0 0 10px rgba(201, 151, 78, 0.15); }
.text-glow-teal { color: var(--accent-teal); text-shadow: 0 0 10px rgba(90, 171, 154, 0.15); }
.text-glow-sky { color: var(--accent-sky); text-shadow: 0 0 10px rgba(107, 168, 217, 0.15); }
.text-glow-rose { color: var(--accent-rose); text-shadow: 0 0 10px rgba(212, 133, 123, 0.15); }
.text-glow-lavender { color: var(--accent-lavender); text-shadow: 0 0 10px rgba(164, 140, 219, 0.15); }

/* Bottom Split Layout */
.bottom-split {
  display: flex;
  gap: 12px;
  min-height: 0;
}

.prescription-card, .exposure-card {
  flex: 1;
  min-height: 0;
}

.trend-card-full {
  height: 200px;
  flex-shrink: 0;
  margin-bottom: 0;
}

/* Lifestyle & Environment Exposure */
.exposure-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  justify-content: flex-start;
  overflow-y: auto;
  padding-right: 4px;
}

.exposure-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  background: rgba(255, 255, 255, 0.012);
  border: 1px solid rgba(255, 255, 255, 0.035);
  border-radius: 8px;
  padding: 6px 10px;
}

.ei-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
}

.ei-icon {
  font-size: 12px;
}

.ei-name {
  color: var(--text-secondary);
  font-weight: 500;
}

.ei-value {
  margin-left: auto;
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--text-primary);
}

.ei-progress-wrap {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
  overflow: hidden;
}

.ei-progress-bar {
  height: 100%;
  border-radius: 2px;
  transition: width 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.ei-footer {
  display: flex;
  justify-content: space-between;
  font-size: 9px;
  color: var(--text-tertiary);
}

.exposure-empty {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
  padding: 30px;
}

/* Prescription Card Container */
.prescription-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 12px;
  min-height: 0;
}

.clinical-summary {
  background: rgba(255, 255, 255, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  padding: 10px 12px;
  flex: none;
}

.summary-label {
  font-size: 11px;
  color: var(--text-tertiary);
  display: block;
  margin-bottom: 4px;
}

.persona-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12.5px;
  font-weight: 700;
  margin-bottom: 6px;
}
.persona-badge.badge-success {
  color: var(--accent-teal);
  background: rgba(90, 171, 154, 0.12);
  border: 1px solid rgba(90, 171, 154, 0.2);
  text-shadow: 0 0 6px rgba(90, 171, 154, 0.2);
}
.persona-badge.badge-warning {
  color: var(--accent-amber);
  background: rgba(201, 151, 78, 0.12);
  border: 1px solid rgba(201, 151, 78, 0.2);
  text-shadow: 0 0 6px rgba(201, 151, 78, 0.2);
}
.persona-badge.badge-danger {
  color: var(--accent-rose);
  background: rgba(212, 133, 123, 0.12);
  border: 1px solid rgba(212, 133, 123, 0.2);
  text-shadow: 0 0 6px rgba(212, 133, 123, 0.2);
}

.summary-desc {
  font-size: 11.5px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}

/* Guidance list scroll */
.guidance-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  padding-right: 4px;
}

.guide-card {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.04);
  background: rgba(255, 255, 255, 0.015);
  transition: all 0.2s ease;
}

.guide-card--warning {
  border-left: 3px solid var(--accent-amber);
  background: rgba(201, 151, 78, 0.03);
  border-color: rgba(201, 151, 78, 0.08) rgba(201, 151, 78, 0.08) rgba(201, 151, 78, 0.08) var(--accent-amber);
}
.guide-card--danger {
  border-left: 3px solid var(--accent-rose);
  background: rgba(212, 133, 123, 0.03);
  border-color: rgba(212, 133, 123, 0.08) rgba(212, 133, 123, 0.08) rgba(212, 133, 123, 0.08) var(--accent-rose);
}
.guide-card--info {
  border-left: 3px solid var(--accent-sky);
  background: rgba(107, 168, 217, 0.03);
  border-color: rgba(107, 168, 217, 0.08) rgba(107, 168, 217, 0.08) rgba(107, 168, 217, 0.08) var(--accent-sky);
}
.guide-card--success {
  border-left: 3px solid var(--accent-teal);
  background: rgba(90, 171, 154, 0.03);
  border-color: rgba(90, 171, 154, 0.08) rgba(90, 171, 154, 0.08) rgba(90, 171, 154, 0.08) var(--accent-teal);
}

.gc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.gc-tag {
  font-size: 8.5px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 3px;
}
.guide-card--warning .gc-tag { color: var(--accent-amber); background: rgba(201, 151, 78, 0.15); }
.guide-card--danger .gc-tag { color: var(--accent-rose); background: rgba(212, 133, 123, 0.15); }
.guide-card--info .gc-tag { color: var(--accent-sky); background: rgba(107, 168, 217, 0.15); }
.guide-card--success .gc-tag { color: var(--accent-teal); background: rgba(90, 171, 154, 0.15); }

.gc-title {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--text-primary);
}

.gc-content {
  font-size: 10.5px;
  color: var(--text-secondary);
  line-height: 1.45;
}

/* Trend Chart */
.trend-chart-container {
  height: 130px;
  width: 100%;
  min-height: 130px;
}

.radar-chart-container {
  width: 100%;
  height: 220px;
  min-height: 220px;
}

.radar-card {
  height: 300px;
  flex: none;
  overflow: visible;
}

/* 专门覆盖 PanelCard 在雷达图卡片上的 overflow:hidden */
:deep(.radar-panel-card.panel-card) {
  overflow: visible;
}

/* Animations */
@keyframes pulse-glow {
  0% {
    transform: scale(0.98);
    opacity: 0.3;
  }
  100% {
    transform: scale(1.03);
    opacity: 0.55;
  }
}

/* Edit button in Health Profile card */
.btn-manage-profile {
  width: 100%;
  padding: 6.5px 0;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-secondary);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 10px;
}
.btn-manage-profile:hover {
  background: rgba(201, 151, 78, 0.12);
  border-color: rgba(201, 151, 78, 0.35);
  color: var(--accent-amber);
  box-shadow: 0 4px 12px rgba(201, 151, 78, 0.15);
}

/* 发送至智能预测按钮 */
.btn-send-predict {
  width: 100%;
  padding: 7px 0;
  border-radius: 6px;
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.18) 0%, rgba(107, 168, 217, 0.14) 100%);
  border: 1px solid rgba(90, 171, 154, 0.35);
  color: var(--accent-teal);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 6px;
  letter-spacing: 0.01em;
  box-shadow: 0 0 0 0 rgba(90, 171, 154, 0);
  position: relative;
  overflow: hidden;
}
.btn-send-predict::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.0) 0%, rgba(90, 171, 154, 0.06) 100%);
  opacity: 0;
  transition: opacity 0.25s;
}
.btn-send-predict:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(90, 171, 154, 0.28) 0%, rgba(107, 168, 217, 0.22) 100%);
  border-color: rgba(90, 171, 154, 0.7);
  color: #7deacb;
  box-shadow: 0 4px 16px rgba(90, 171, 154, 0.22), 0 0 0 1px rgba(90, 171, 154, 0.15);
  transform: translateY(-1px);
}
.btn-send-predict:hover:not(:disabled)::before {
  opacity: 1;
}
.btn-send-predict:active:not(:disabled) {
  transform: translateY(0px);
  box-shadow: 0 2px 8px rgba(90, 171, 154, 0.15);
}
.btn-send-predict:disabled {
  opacity: 0.32;
  cursor: not-allowed;
}

/* Modal backdrop and card overlay */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(10, 10, 15, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1999;
  animation: fade-in 0.2s ease-out;
}

.modal-card {
  width: 380px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(25, 25, 35, 0.95) 0%, rgba(15, 15, 20, 0.98) 100%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  animation: scale-up 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding-bottom: 8px;
}

/* Modal Tabs */
.modal-tabs {
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.15);
  padding: 3px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.tab-btn {
  flex: 1;
  padding: 6px 0;
  border-radius: 4px;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  font-size: 11.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tab-btn.active {
  background: rgba(255, 255, 255, 0.06);
  color: var(--accent-amber);
  text-shadow: 0 0 6px rgba(201, 151, 78, 0.2);
}
.tab-btn:hover:not(.active) {
  color: var(--text-secondary);
}

/* Modal Alert Box */
.modal-alert-box {
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.4;
  animation: fade-in 0.2s ease-out;
}
.modal-alert-box.error {
  background: rgba(212, 133, 123, 0.1);
  border: 1px solid rgba(212, 133, 123, 0.25);
  color: var(--accent-rose);
}
.modal-alert-box.success {
  background: rgba(90, 171, 154, 0.1);
  border: 1px solid rgba(90, 171, 154, 0.25);
  color: var(--accent-teal);
}

.modal-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-sans);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 20px;
  cursor: pointer;
  transition: color 0.15s ease;
}
.modal-close:hover {
  color: var(--accent-rose);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
}

.form-row-split {
  display: flex;
  gap: 12px;
}

.glass-input {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 8px 10px;
  color: var(--text-primary);
  font-size: 12px;
  outline: none;
  transition: all 0.2s ease;
}
.glass-input:focus {
  border-color: var(--accent-teal);
  background: rgba(90, 171, 154, 0.06);
  box-shadow: 0 0 8px rgba(90, 171, 154, 0.25);
}

.gender-radio-group {
  display: flex;
  gap: 10px;
}

.gender-radio-label {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  font-size: 12px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}
.gender-radio-label.active {
  background: rgba(90, 171, 154, 0.12);
  border-color: var(--accent-teal);
  color: var(--accent-teal);
  font-weight: 600;
  box-shadow: 0 0 8px rgba(90, 171, 154, 0.2);
}

.hidden-radio {
  display: none;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 12px;
  margin-top: 4px;
}

.btn-secondary {
  padding: 7px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-secondary);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
}

.btn-primary {
  padding: 7px 14px;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--accent-teal) 0%, rgba(90, 171, 154, 0.7) 100%);
  border: none;
  color: #0f0f15;
  font-size: 11.5px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(90, 171, 154, 0.25);
  transition: all 0.2s ease;
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(90, 171, 154, 0.35);
  filter: brightness(1.1);
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scale-up {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* ========== 睡眠稳定性与就寝规律度卡片 ========== */
.stability-card {
  min-height: 0;
  background: rgba(20, 20, 30, 0.25) !important;
  backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.05) !important;
}

.stability-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  overflow: hidden;
}

/* 环形评分 */
.stability-score-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.015);
  border: 1px solid rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  padding: 10px 14px;
}

.ss-ring-wrapper {
  position: relative;
  width: 64px;
  height: 64px;
  flex-shrink: 0;
}

.ss-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ss-ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.04);
  stroke-width: 5;
}

.ss-ring-fill {
  fill: none;
  stroke-width: 6;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.8s cubic-bezier(0.4, 0, 0.2, 1), stroke 0.8s ease;
}

.ss-ring-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ss-score {
  font-family: var(--font-mono);
  font-size: 19px;
  font-weight: 700;
  color: #eeede6;
  line-height: 1;
}

.ss-label {
  font-size: 8px;
  color: rgba(255, 255, 255, 0.35);
  margin-top: 3px;
  letter-spacing: 0.5px;
}

.ss-hero-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
}

.meta-value {
  font-size: 10.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  font-family: var(--font-mono);
}

.ss-level-badge-glass {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 10.5px;
  font-weight: 700;
  border: 1px solid;
  letter-spacing: 0.5px;
  line-height: 1;
}

/* 指标条 */
.stability-metrics {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

.sm-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sm-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sm-bullet-glow {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  display: inline-block;
}

.sm-name {
  flex: 1;
  font-size: 10.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.5);
}

.sm-value {
  font-family: var(--font-mono);
  font-size: 11.5px;
  font-weight: 700;
}

.sm-unit {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.35);
  font-weight: normal;
  margin-left: 1px;
}

.sm-bar-wrap {
  height: 5px;
  background: rgba(255, 255, 255, 0.035);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.sm-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1), background 0.6s ease;
  position: relative;
  overflow: hidden;
}

.sm-bar::after {
  content: '';
  position: absolute;
  top: 0; right: 0; bottom: 0; left: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.18), transparent);
  animation: shine 2.5s infinite linear;
}

@keyframes shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.sm-range {
  display: flex;
  justify-content: space-between;
  font-size: 8.5px;
  color: rgba(255, 255, 255, 0.2);
}

/* 结论描述 */
.stability-conclusion {
  font-size: 10.5px;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.55);
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.015);
  border: 1px solid rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  flex-shrink: 0;
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conclusion-title {
  font-size: 10.5px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 0.3px;
}

.conclusion-text-content {
  margin: 0;
  color: rgba(255, 255, 255, 0.45);
}
</style>
