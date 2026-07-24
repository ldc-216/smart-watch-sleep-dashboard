<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <!-- AI 语义检索输入框 -->
      <div class="ai-nlp-search-group">
        <span class="nlp-icon">🤖 AI 语义检索:</span>
        <input v-model="nlpQueryText" class="nlp-input" placeholder="输入大白话，如：'查找欧洲地区、BMI大于25、得分小于70的女性用户'..." @keyup.enter="parseNlpQuery" />
        <button class="btn-nlp-apply" @click="parseNlpQuery">智能解析并检索</button>
      </div>

      <div class="sb-grid">
        <div class="sf-group">
          <label class="sf-label">用户ID</label>
          <input v-model="searchForm.userId" class="sf-input" placeholder="精确匹配" />
        </div>
        <div class="sf-group">
          <label class="sf-label">性别</label>
          <select v-model="searchForm.gender" class="sf-input">
            <option value="">全部</option>
            <option value="female">女</option>
            <option value="male">男</option>
          </select>
        </div>
        <div class="sf-group">
          <label class="sf-label">服药</label>
          <select v-model="searchForm.medicationFlag" class="sf-input">
            <option :value="null">全部</option>
            <option :value="1">是</option>
            <option :value="0">否</option>
          </select>
        </div>
        <div class="sf-group">
          <label class="sf-label">失眠</label>
          <select v-model="searchForm.insomniaFlag" class="sf-input">
            <option :value="null">全部</option>
            <option :value="1">是</option>
            <option :value="0">否</option>
          </select>
        </div>
        <div class="sf-group">
          <label class="sf-label">区域</label>
          <select v-model="searchForm.region" class="sf-input">
            <option value="">全部</option>
            <option value="亚洲">亚洲</option>
            <option value="欧洲">欧洲</option>
            <option value="北美洲">北美洲</option>
            <option value="大洋洲">大洋洲</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="sf-group">
          <label class="sf-label">设备</label>
          <select v-model="searchForm.deviceModel" class="sf-input">
            <option value="">全部</option>
            <option value="AlphaWatch X1">AlphaWatch X1</option>
            <option value="OpenSmart v2">OpenSmart v2</option>
            <option value="PulsePro 3">PulsePro 3</option>
            <option value="SleepSense S2">SleepSense S2</option>
            <option value="WristFit Z">WristFit Z</option>
          </select>
        </div>
        <div class="sf-group">
          <label class="sf-label">年龄<span class="limit-span" v-if="limits.minAge !== null">({{ limits.minAge }}~{{ limits.maxAge }})</span></label>
          <div class="sf-range">
            <input v-model.number="searchForm.ageMin" type="number" class="sf-input" :placeholder="limits.minAge !== null ? limits.minAge : 'Min'" :min="limits.minAge" :max="limits.maxAge" />
            <span class="sf-sep">–</span>
            <input v-model.number="searchForm.ageMax" type="number" class="sf-input" :placeholder="limits.maxAge !== null ? limits.maxAge : 'Max'" :min="limits.minAge" :max="limits.maxAge" />
          </div>
        </div>
        <div class="sf-group">
          <label class="sf-label">得分<span class="limit-span" v-if="limits.minSleepScore !== null">({{ limits.minSleepScore }}~{{ limits.maxSleepScore }})</span></label>
          <div class="sf-range">
            <input v-model.number="searchForm.sleepScoreMin" type="number" class="sf-input" :placeholder="limits.minSleepScore !== null ? limits.minSleepScore : 'Min'" :min="limits.minSleepScore" :max="limits.maxSleepScore" />
            <span class="sf-sep">–</span>
            <input v-model.number="searchForm.sleepScoreMax" type="number" class="sf-input" :placeholder="limits.maxSleepScore !== null ? limits.maxSleepScore : 'Max'" :min="limits.minSleepScore" :max="limits.maxSleepScore" />
          </div>
        </div>
        <div class="sf-group">
          <label class="sf-label">打鼾<span class="limit-span" v-if="limits.minSnoreEvents !== null">({{ limits.minSnoreEvents }}~{{ limits.maxSnoreEvents }})</span></label>
          <div class="sf-range">
            <input v-model.number="searchForm.snoreEventsMin" type="number" class="sf-input" :placeholder="limits.minSnoreEvents !== null ? limits.minSnoreEvents : 'Min'" :min="limits.minSnoreEvents" :max="limits.maxSnoreEvents" />
            <span class="sf-sep">–</span>
            <input v-model.number="searchForm.snoreEventsMax" type="number" class="sf-input" :placeholder="limits.maxSnoreEvents !== null ? limits.maxSnoreEvents : 'Max'" :min="limits.minSnoreEvents" :max="limits.maxSnoreEvents" />
          </div>
        </div>
        <div class="sf-group">
          <label class="sf-label">BMI<span class="limit-span" v-if="limits.minBmi !== null">({{ limits.minBmi?.toFixed(1) }}~{{ limits.maxBmi?.toFixed(1) }})</span></label>
          <div class="sf-range">
            <input v-model.number="searchForm.bmiMin" type="number" class="sf-input" :placeholder="limits.minBmi !== null ? limits.minBmi?.toFixed(1) : 'Min'" :min="limits.minBmi" :max="limits.maxBmi" />
            <span class="sf-sep">–</span>
            <input v-model.number="searchForm.bmiMax" type="number" class="sf-input" :placeholder="limits.maxBmi !== null ? limits.maxBmi?.toFixed(1) : 'Max'" :min="limits.minBmi" :max="limits.maxBmi" />
          </div>
        </div>
      </div>
      <div class="sb-actions">
        <button v-if="isRoot" class="btn-secondary btn-sm" @click="openImport">导入数据</button>
        <button class="btn-secondary btn-sm" @click="exportCsv">导出数据</button>
        <button class="btn-primary btn-sm" :disabled="searching" @click="onSearch(true)">
          {{ searching ? '检索中··' : '检索' }}
        </button>
      </div>
    </div>

    <!-- 结果行 -->
    <div class="grid content-row">
      <div class="card">
        <div class="card-head">
          <div class="ch-left">
            <span class="card-title">检索结果</span>
            <span class="card-sub" v-if="searched">{{ searchResult.total }} 条</span>
            <!-- 批量删除按钮 -->
            <button 
              v-if="isRoot && selectedRecords.length > 0" 
              class="btn-danger btn-xs btn-batch-del" 
              @click="onBatchDelete"
            >
              🗑️ 批量删除 ({{ selectedRecords.length }})
            </button>
          </div>
          
          <!-- 列配置下拉菜单 -->
          <div class="col-selector-container" ref="colSelectorRef">
            <button class="btn-col-config" @click="showColSelector = !showColSelector">
              ⚙️ 列显示
            </button>
            <transition name="fade">
              <div v-if="showColSelector" class="col-selector-dropdown">
                <div class="csd-title">自定义展示字段</div>
                <div class="csd-grid">
                  <label v-for="(label, col) in colOptions" :key="col" class="csd-item">
                    <input type="checkbox" v-model="colVisible[col]" />
                    <span>{{ label }}</span>
                  </label>
                </div>
              </div>
            </transition>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <!-- 批量选择列 -->
                <th v-if="isRoot" class="th-check">
                  <input 
                    type="checkbox" 
                    :checked="isAllSelected" 
                    :indeterminate="isSomeSelected" 
                    @change="toggleSelectAll" 
                  />
                </th>
                <th>用户ID</th>
                <th class="th-sort" @click="toggleSort('dateRecorded')">日期 <span class="sort-arrow">{{ getSortIcon('dateRecorded') }}</span></th>
                <th v-if="colVisible.region">地区</th>
                <th v-if="colVisible.gender">性别</th>
                <th v-if="colVisible.age" class="th-sort" @click="toggleSort('age')">年龄 <span class="sort-arrow">{{ getSortIcon('age') }}</span></th>
                <th v-if="colVisible.bmi" class="th-sort" @click="toggleSort('bmi')">BMI <span class="sort-arrow">{{ getSortIcon('bmi') }}</span></th>
                <th v-if="colVisible.device">设备</th>
                <th class="th-sort" @click="toggleSort('sleepScore')">得分 <span class="sort-arrow">{{ getSortIcon('sleepScore') }}</span></th>
                <th v-if="colVisible.efficiency">效率</th>
                <th v-if="colVisible.latency">延迟</th>
                <th v-if="colVisible.heartRate">心率</th>
                <th v-if="colVisible.spo2">血氧</th>
                <th v-if="colVisible.snore">打鼾</th>
                <th v-if="colVisible.apnea" class="th-sort" @click="toggleSort('apneaRiskScore')">呼吸暂停 <span class="sort-arrow">{{ getSortIcon('apneaRiskScore') }}</span></th>
                <th v-if="colVisible.stress" class="th-sort" @click="toggleSort('stressScore')">压力 <span class="sort-arrow">{{ getSortIcon('stressScore') }}</span></th>
                <th v-if="colVisible.medication">服药</th>
                <th v-if="isRoot">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(r, i) in searchResult.records" :key="i" :class="{ 'row-selected': isRowSelected(r), 'row-warning': isWarningRecord(r) }">
                <!-- 行选择复选框 -->
                <td v-if="isRoot" class="td-check">
                  <input 
                    type="checkbox" 
                    :checked="isRowSelected(r)" 
                    @change="toggleSelectRow(r)" 
                  />
                </td>
                <td class="mono">{{ r.user_id }}</td>
                <td class="mono">{{ r.date_recorded }}</td>
                <td v-if="colVisible.region">{{ r.region }}</td>
                <td v-if="colVisible.gender">
                  <span :class="r.gender === 'female' ? 'badge-gender female' : 'badge-gender male'">
                    {{ r.gender === 'female' ? '女' : '男' }}
                  </span>
                </td>
                <td v-if="colVisible.age">{{ r.age }}</td>
                <td v-if="colVisible.bmi" class="mono">{{ r.bmi }}</td>
                <td v-if="colVisible.device">{{ r.device_model }}</td>
                <td>
                  <span class="score-badge" :class="scoreClass(r.sleep_score)">
                    <span v-if="isWarningRecord(r)" class="warning-blink">🚨</span>
                    {{ r.sleep_score }}
                  </span>
                </td>
                <td v-if="colVisible.efficiency" class="mono">{{ r.sleep_efficiency_pct }}%</td>
                <td v-if="colVisible.latency" class="mono">{{ r.sleep_latency_minutes }}m</td>
                <td v-if="colVisible.heartRate" class="mono">{{ r.heart_rate_mean_bpm }}</td>
                <td v-if="colVisible.spo2" :class="{ 'text-warn': r.spo2_min_pct <= 92 }">{{ r.spo2_min_pct }}%</td>
                <td v-if="colVisible.snore" class="mono">{{ r.snore_events }}</td>
                <td v-if="colVisible.apnea" :class="{ 'text-warn': r.apnea_risk_score >= 30 }">{{ r.apnea_risk_score }}</td>
                <td v-if="colVisible.stress" class="mono">{{ r.stress_score }}</td>
                <td v-if="colVisible.medication">
                  <span :class="r.medication_flag === 1 ? 'badge-med active' : 'badge-med'">
                    {{ r.medication_flag === 1 ? '是' : '否' }}
                  </span>
                </td>
                <td v-if="isRoot">
                  <button class="act-edit" @click="onEdit(r)">编辑</button>
                  <button class="act-dossier" @click="openDossier(r)">档案</button>
                  <button class="act-del" @click="onDelete(r)">删除</button>
                </td>
              </tr>
              <tr v-if="searched && !searchResult.records.length">
                <td :colspan="visibleColsCount" class="empty-cell">无匹配记录</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination" v-if="searched && searchResult.total > 0">
          <button class="page-btn" :disabled="searchForm.page <= 1" @click="changePage(searchForm.page - 1)">← 上一页</button>
          <span class="page-info">{{ searchForm.page }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="searchForm.page >= totalPages" @click="changePage(searchForm.page + 1)">下一页 →</button>
        </div>
      </div>

      <div class="card">
        <!-- Tab selector for right panel -->
        <div class="card-tabs-header">
          <button class="tab-btn" :class="{ active: activeTabRight === 'stats' }" @click="activeTabRight = 'stats'">📈 统计特征</button>
          <button class="tab-btn" :class="{ active: activeTabRight === 'charts' }" @click="activeTabRight = 'charts'">📊 地域分布</button>
          <button class="tab-btn" :class="{ active: activeTabRight === 'alerts' }" @click="activeTabRight = 'alerts'">🚨 离线告警</button>
          <button class="tab-btn" :class="{ active: activeTabRight === 'sandbox' }" @click="activeTabRight = 'sandbox'">👥 沙箱对比</button>
        </div>

        <!-- Tab 1: Stats & Clinical Report -->
        <div class="stats-panel-content" v-if="activeTabRight === 'stats'">
          <div class="stats-wrap" v-if="searched && searchResult.total > 0 && searchResult.stats">
            <div class="stat-block">
              <div class="stat-head">群体睡眠指征</div>
              <div class="stat-grid-3">
                <div class="stat-metric">
                  <span class="sm-label">平均得分</span>
                  <span class="sm-value color-amber">{{ searchResult.stats.avgSleepScore }}</span>
                </div>
                <div class="stat-metric">
                  <span class="sm-label">平均效率</span>
                  <span class="sm-value color-teal">{{ searchResult.stats.avgEfficiency }}%</span>
                </div>
                <div class="stat-metric">
                  <span class="sm-label">静息心率</span>
                  <span class="sm-value color-sky">{{ searchResult.stats.avgHeartRate }} bpm</span>
                </div>
              </div>
              
              <div class="stat-divider"></div>
              
              <div class="stat-grid-row">
                <div class="sgr-col">
                  <span class="sgr-label">日常压力</span>
                  <span class="sgr-val">{{ searchResult.stats.avgStress }}</span>
                </div>
                <div class="sgr-col">
                  <span class="sgr-label">睡前屏幕</span>
                  <span class="sgr-val">{{ searchResult.stats.avgScreenTime }}m</span>
                </div>
                <div class="sgr-col">
                  <span class="sgr-label">咖啡因</span>
                  <span class="sgr-val">{{ searchResult.stats.avgCaffeine }}mg</span>
                </div>
                <div class="sgr-col">
                  <span class="sgr-label">日常酒精</span>
                  <span class="sgr-val">{{ searchResult.stats.avgAlcohol }}U</span>
                </div>
              </div>
            </div>
            
            <div class="stat-block" v-if="searchResult.stats.genderRatio">
              <div class="stat-head">人口学分布</div>
              <div class="gender-bar">
                <div class="gender-labels">
                  <span>女 {{ searchResult.stats.genderRatio.female || 0 }}人 ({{ getGenderPct('female') }}%)</span>
                  <span>男 {{ searchResult.stats.genderRatio.male || 0 }}人 ({{ getGenderPct('male') }}%)</span>
                </div>
                <div class="gender-track">
                  <div class="gender-fill-f" :style="{ width: getGenderPct('female') + '%' }"></div>
                  <div class="gender-fill-m" :style="{ width: getGenderPct('male') + '%' }"></div>
                </div>
              </div>
            </div>

            <div class="guidance-section">
              <div class="stat-head">群体健康干预指导方案</div>
              <div class="guidance-list" v-if="diagnosticReport && diagnosticReport.length">
                <div v-for="(g, idx) in diagnosticReport" :key="idx" class="guide-card" :class="`guide-card--${g.type}`">
                  <div class="guide-title">
                    <span class="guide-status-dot"></span>
                    {{ g.title }}
                  </div>
                  <div class="guide-desc">{{ g.desc }}</div>
                </div>
              </div>
              <div class="guide-empty" v-else>
                🎉 该筛选群体的各项睡眠与行为指标均在良好状态，请继续保持健康的生活习惯！
              </div>
            </div>
          </div>
          <div v-else-if="searched && searchResult.total === 0" class="state-empty">
            ⚠️ 暂无匹配的用户记录，无法生成群体画像
          </div>
          <div v-else class="state-empty">执行检索后生成实时画像</div>
        </div>

        <!-- Tab 2: Region Distribution Pie -->
        <div class="charts-panel-content" v-else-if="activeTabRight === 'charts'">
          <div class="charts-wrap" v-if="searched && searchResult.total > 0">
            <div class="charts-title-sub">检索群体地域分布 (动态计算)</div>
            <div class="mini-chart-container">
              <EChart :option="regionChartOption" height="100%" />
            </div>
            <div class="charts-summary-box">
              根据当前条件检索到的数据样本，展现该群体的地域分布结构，有助于分析地域气候环境对睡眠的潜在诱发机制。
            </div>
          </div>
          <div v-else class="state-empty">检索后生成地域分布图</div>
        </div>

        <!-- Tab 3: Alerts Stream & Config -->
        <div class="alerts-panel-content" v-else-if="activeTabRight === 'alerts'">
          <div class="alerts-config-box">
            <div class="ac-head-sub">⚙️ 离线高危筛选拦截门槛</div>
            <div class="ac-form">
              <div class="acf-item">
                <label>得分 &lt;</label>
                <input v-model.number="alertConfig.sleepScoreMax" type="number" class="acf-input" />
              </div>
              <div class="acf-item">
                <label>血氧 &lt;</label>
                <input v-model.number="alertConfig.spo2Min" type="number" class="acf-input" />
              </div>
              <div class="acf-item">
                <label>呼吸暂停 &ge;</label>
                <input v-model.number="alertConfig.apneaRiskMin" type="number" class="acf-input" />
              </div>
            </div>
          </div>

          <div class="alerts-stream-box">
            <div class="asb-head">📂 批处理离线高危检测日志 (当前检索结果集)</div>
            <div class="asb-list">
              <div v-for="(item, idx) in batchAlerts" :key="idx" class="asb-item" :class="`asb-item--${item.type}`">
                <span class="asbi-time">[{{ item.date }}]</span>
                <span class="asbi-text">{{ item.text }}</span>
              </div>
              <div v-if="batchAlerts.length === 0" class="guide-empty" style="padding:15px 10px; font-size: 8.5px;">
                当前检索结果中没有符合高危阈值的异常数据
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 4: Sandbox Cohort Comparison -->
        <div class="sandbox-panel-content" v-else-if="activeTabRight === 'sandbox'">
          <div class="sandbox-cohort-controls">
            <button class="btn-save-sb" @click="saveCohort('A')">💾 保存为对比组 A</button>
            <button class="btn-save-sb" style="background: rgba(212,133,123,0.15); border-color: rgba(212,133,123,0.3); color: #ff9da9;" @click="saveCohort('B')">💾 保存为对比组 B</button>
          </div>
          <div class="sandbox-cohort-indicator">
            <span class="sci-badge sci-badge-a" v-if="cohortA">A: 已保存 ({{ cohortA.total }}人)</span>
            <span class="sci-badge sci-badge-empty" v-else>A: 未保存</span>
            <span class="sci-badge sci-badge-b" v-if="cohortB">B: 已保存 ({{ cohortB.total }}人)</span>
            <span class="sci-badge sci-badge-empty" v-else>B: 未保存</span>
          </div>

          <div class="radar-chart-wrap" v-if="cohortA || cohortB">
            <div class="radar-chart-container">
              <EChart :option="sandboxRadarOption" height="100%" />
            </div>
            <!-- Side-by-side comparison stats table -->
            <div class="sandbox-comparison-table">
              <table class="sb-table">
                <thead>
                  <tr>
                    <th>指征均值</th>
                    <th style="color: #7deacb">A组: {{ cohortA?.name || '--' }}</th>
                    <th style="color: #ff9da9">B组: {{ cohortB?.name || '--' }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>样本总数</td>
                    <td>{{ cohortA ? cohortA.total + ' 例' : '--' }}</td>
                    <td>{{ cohortB ? cohortB.total + ' 例' : '--' }}</td>
                  </tr>
                  <tr>
                    <td>睡眠得分</td>
                    <td>{{ cohortA ? cohortA.stats.avgSleepScore : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgSleepScore : '--' }}</td>
                  </tr>
                  <tr>
                    <td>睡眠效率</td>
                    <td>{{ cohortA ? cohortA.stats.avgEfficiency + '%' : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgEfficiency + '%' : '--' }}</td>
                  </tr>
                  <tr>
                    <td>静息心率</td>
                    <td>{{ cohortA ? cohortA.stats.avgHeartRate + ' bpm' : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgHeartRate + ' bpm' : '--' }}</td>
                  </tr>
                  <tr>
                    <td>压力负荷</td>
                    <td>{{ cohortA ? cohortA.stats.avgStress : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgStress : '--' }}</td>
                  </tr>
                  <tr>
                    <td>屏幕暴露</td>
                    <td>{{ cohortA ? cohortA.stats.avgScreenTime + ' m' : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgScreenTime + ' m' : '--' }}</td>
                  </tr>
                  <tr>
                    <td>咖啡因</td>
                    <td>{{ cohortA ? cohortA.stats.avgCaffeine + ' mg' : '--' }}</td>
                    <td>{{ cohortB ? cohortB.stats.avgCaffeine + ' mg' : '--' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="sb-radar-clear">
              <button class="btn-clear-select" style="float:none;" @click="cohortA = null; cohortB = null">清空对比沙箱</button>
            </div>
          </div>
          <div class="state-empty" v-else>
            请配置筛选条件并分别点击保存，可以在雷达图上进行亚健康指征的多维度并列对比。
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal for Import/Edit -->
  <div class="modal-backdrop" v-if="showImportDialog">
    <div class="modal-dialog">
      <div class="modal-head">
        <h3>{{ isEdit ? '编辑记录' : '导入数据' }}</h3>
        <button class="modal-close" @click="showImportDialog = false">&times;</button>
      </div>
      <div class="modal-tabs" v-if="!isEdit">
        <button class="tab-btn" :class="{ active: importTab === 'form' }" @click="importTab = 'form'">单条录入</button>
        <button class="tab-btn" :class="{ active: importTab === 'json' }" @click="importTab = 'json'">JSON批量</button>
      </div>
      <div class="modal-body">
        <div v-if="importTab === 'form' || isEdit" class="import-form">
          <div class="form-row">
            <div class="form-col">
              <label>用户ID *</label>
              <div class="autocomplete-container">
                <input 
                  v-model="userSearchQuery" 
                  class="sf-input" 
                  :disabled="isEdit"
                  placeholder="输入搜索/选择用户ID..." 
                  @focus="showUserDropdown = true"
                  @blur="showUserDropdown = false"
                />
                <div class="autocomplete-dropdown" v-if="showUserDropdown && filteredUserIds.length > 0">
                  <div 
                    v-for="uid in filteredUserIds" 
                    :key="uid" 
                    class="autocomplete-item"
                    @mousedown="selectUser(uid)"
                  >
                    {{ uid }}
                  </div>
                </div>
              </div>
            </div>
            <div class="form-col">
              <label>日期 *</label>
              <input v-model="importForm.date_recorded" class="sf-input" :disabled="isEdit" placeholder="YYYY-MM-DD" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-col">
              <label>性别</label>
              <select v-model="importForm.gender" class="sf-input" :disabled="true">
                <option value="male">男</option>
                <option value="female">女</option>
                <option value="other">其他</option>
              </select>
            </div>
            <div class="form-col">
              <label>年龄</label>
              <input v-model.number="importForm.age" type="number" class="sf-input" :disabled="true" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-col">
              <label>区域</label>
              <select v-model="importForm.region" class="sf-input" :disabled="true">
                <option value="亚洲">亚洲</option>
                <option value="欧洲">欧洲</option>
                <option value="北美洲">北美洲</option>
                <option value="大洋洲">大洋洲</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-col">
              <label>设备</label>
              <select v-model="importForm.device_model" class="sf-input" :disabled="true">
                <option value="AlphaWatch X1">AlphaWatch X1</option>
                <option value="OpenSmart v2">OpenSmart v2</option>
                <option value="PulsePro 3">PulsePro 3</option>
                <option value="SleepSense S2">SleepSense S2</option>
                <option value="WristFit Z">WristFit Z</option>
              </select>
            </div>
          </div>
          <div class="form-row"><div class="form-col"><label>得分</label><input v-model.number="importForm.sleep_score" type="number" class="sf-input" /></div><div class="form-col"><label>效率 (%)</label><input v-model.number="importForm.sleep_efficiency_pct" type="number" class="sf-input" /></div></div>
          <div class="form-row"><div class="form-col"><label>入睡延迟</label><input v-model.number="importForm.sleep_latency_minutes" type="number" class="sf-input" /></div><div class="form-col"><label>心率</label><input v-model.number="importForm.heart_rate_mean_bpm" type="number" class="sf-input" /></div></div>
          <div class="form-row"><div class="form-col"><label>最低血氧 (%)</label><input v-model.number="importForm.spo2_min_pct" type="number" class="sf-input" /></div><div class="form-col"><label>打鼾</label><input v-model.number="importForm.snore_events" type="number" class="sf-input" /></div></div>
          <div class="form-row"><div class="form-col"><label>服药</label><select v-model.number="importForm.medication_flag" class="sf-input"><option :value="0">否</option><option :value="1">是</option></select></div><div class="form-col"><label>呼吸暂停风险</label><input v-model.number="importForm.apnea_risk_score" type="number" class="sf-input" /></div></div>
          <div class="form-row"><div class="form-col" style="width: 100%;"><label>压力</label><input v-model.number="importForm.stress_score" type="number" class="sf-input" /></div></div>
        </div>
        <div v-if="importTab === 'json' && !isEdit" class="import-json">
          <label class="json-label">粘贴 JSON 数组：</label>
          <textarea v-model="jsonContent" class="textarea" placeholder='[{"user_id":"user_99999","date_recorded":"2026-07-06",...}]'></textarea>
        </div>
      </div>
      <div class="modal-foot">
        <button class="btn-secondary" @click="showImportDialog = false">取消</button>
        <button class="btn-primary" :disabled="importing" @click="submitImport">
          {{ importing ? '处理中··' : (isEdit ? '保存' : '导入') }}
        </button>
      </div>
    </div>
  </div>

  <!-- User Sleep Dossier Modal (Printable PDF) -->
  <div class="modal-backdrop" v-if="showDossierDialog">
    <div class="modal-dialog dossier-dialog">
      <div class="modal-head no-print">
        <h3>📄 用户睡眠健康电子档案</h3>
        <button class="modal-close" @click="showDossierDialog = false">&times;</button>
      </div>
      <div class="modal-body print-area" v-if="selectedUserDossier">
        <div class="dossier-header">
          <div class="dh-title">SMARTWATCH SLEEP HEALTH RECORD</div>
          <div class="dh-subtitle">智能可穿戴睡眠健康电子诊疗档案</div>
        </div>
        <div class="dossier-meta-grid">
          <div class="dmg-item"><strong>档案编号 (用户ID):</strong> {{ selectedUserDossier.user_id }}</div>
          <div class="dmg-item"><strong>数据日期:</strong> {{ selectedUserDossier.date_recorded }}</div>
          <div class="dmg-item"><strong>性别:</strong> {{ selectedUserDossier.gender === 'female' ? '女' : '男' }}</div>
          <div class="dmg-item"><strong>年龄:</strong> {{ selectedUserDossier.age }} 岁</div>
          <div class="dmg-item"><strong>BMI指数:</strong> {{ selectedUserDossier.bmi || '--' }}</div>
          <div class="dmg-item"><strong>监测设备:</strong> {{ selectedUserDossier.device_model || '--' }}</div>
          <div class="dmg-item"><strong>监测区域:</strong> {{ selectedUserDossier.region || '--' }}</div>
          <div class="dmg-item"><strong>服药标记:</strong> {{ selectedUserDossier.medication_flag === 1 ? '服药监测状态' : '无服药' }}</div>
        </div>
        
        <div class="dossier-section-title">📊 核心睡眠生理体征诊断</div>
        <div class="dossier-metrics-grid">
          <div class="dmg-card score-card">
            <span class="dmc-label">睡眠综合得分</span>
            <span class="dmc-val">{{ selectedUserDossier.sleep_score }}</span>
            <span class="dmc-status" :class="scoreClass(selectedUserDossier.sleep_score)">
              {{ selectedUserDossier.sleep_score >= 80 ? '优秀' : (selectedUserDossier.sleep_score >= 70 ? '一般' : '异常警告') }}
            </span>
          </div>
          <div class="dmg-card">
            <span class="dmc-label">睡眠效率</span>
            <span class="dmc-val">{{ selectedUserDossier.sleep_efficiency_pct }}%</span>
            <span class="dmc-desc">入睡率与维持性分析</span>
          </div>
          <div class="dmg-card">
            <span class="dmc-label">静息均值心率</span>
            <span class="dmc-val">{{ selectedUserDossier.heart_rate_mean_bpm }} bpm</span>
            <span class="dmc-desc">植物神经静息平衡度</span>
          </div>
          <div class="dmg-card" :class="{ 'warning-border': selectedUserDossier.spo2_min_pct <= 92 }">
            <span class="dmc-label">夜间最低血氧</span>
            <span class="dmc-val" :style="{ color: selectedUserDossier.spo2_min_pct <= 92 ? 'var(--accent-rose)' : 'inherit' }">
              {{ selectedUserDossier.spo2_min_pct }}%
            </span>
            <span class="dmc-status text-warn" v-if="selectedUserDossier.spo2_min_pct <= 92">🚨 低氧低氧</span>
            <span class="dmc-status text-success" v-else>安全</span>
          </div>
          <div class="dmg-card" :class="{ 'warning-border': selectedUserDossier.apnea_risk_score >= 25 }">
            <span class="dmc-label">呼吸暂停风险分</span>
            <span class="dmc-val">{{ selectedUserDossier.apnea_risk_score }}</span>
            <span class="dmc-status text-warn" v-if="selectedUserDossier.apnea_risk_score >= 25">🚨 高风险</span>
            <span class="dmc-status text-success" v-else>正常</span>
          </div>
        </div>

        <div class="dossier-section-title">🏥 医生临床意见与干预建议</div>
        <div class="dossier-clinical-notes">
          <p v-if="selectedUserDossier.sleep_score < 70 || selectedUserDossier.spo2_min_pct <= 92 || selectedUserDossier.apnea_risk_score >= 25">
            <strong>指征评估：</strong>受检用户在夜间监测到明显的睡眠深度不足及气道阻力上升症状。夜间最低血氧饱和度跌落至 <strong>{{ selectedUserDossier.spo2_min_pct }}%</strong>，伴随打鼾事件达 <strong>{{ selectedUserDossier.snore_events }}</strong> 次，存在明显的呼吸暂停高危隐患（阻塞风险评级: {{ selectedUserDossier.apnea_risk_score }}）。
            <br/><br/>
            <strong>临床干预指导方案：</strong>
            <br/>1. 睡姿诱导：夜间严禁仰卧，建议侧卧以防止重力塌陷气道。
            <br/>2. 微气候干预：卧室内噪声严格维持在 35dB 以下，室温调至 20.5℃。
            <br/>3. 生活干预：睡前 4 小时限制摄入酒精和咖啡，推行规律昼夜起床规律。
            <br/>4. PSG建议：若临床症状无缓解，建议及时到门诊进行多导睡眠图（PSG）检查。
          </p>
          <p v-else>
            <strong>指征评估：</strong>受检用户各项指标健康且在标准阈值范围内。睡眠得分达 {{ selectedUserDossier.sleep_score }} 分，自主神经心率 {{ selectedUserDossier.heart_rate_mean_bpm }} bpm 保持平稳，夜间最低血氧充足。
            <br/><br/>
            <strong>日常维护方案：</strong>继续保持现有健康的昼夜节律安排，午后控制咖啡因摄入，坚持规律中轻度有氧运动。
          </p>
        </div>
      </div>
      <div class="modal-foot no-print">
        <button class="btn-secondary" @click="showDossierDialog = false">关闭</button>
        <button class="btn-primary" @click="printDossier">🖨️ 打印电子病历档案 (PDF)</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import EChart from '../components/EChart.vue'
import { searchRecords, importRecords, deleteRecord, batchDeleteRecords, updateRecord, getRangeLimits } from '../api/screen5'
import { CHART_COLORS, baseTooltip } from '../styles/chartTheme'
import { getProfileDetail } from '../api/profile'

const username = ref(localStorage.getItem('username') || '')
const isRoot = computed(() => username.value === 'root')

const limits = ref({
  minAge: null, maxAge: null,
  minSleepScore: null, maxSleepScore: null,
  minSnoreEvents: null, maxSnoreEvents: null,
  minBmi: null, maxBmi: null
})

const searchForm = reactive({
  userId: '', gender: '', ageMin: null, ageMax: null, medicationFlag: null,
  region: '', deviceModel: '', insomniaFlag: null,
  sleepScoreMin: null, sleepScoreMax: null, snoreEventsMin: null, snoreEventsMax: null,
  bmiMin: null, bmiMax: null,
  sortField: 'dateRecorded', sortOrder: 'desc', page: 1, size: 22
})

const searching = ref(false)
const searched = ref(false)
const searchResult = ref({ total: 0, records: [], stats: { regionRatio: {} } })
const totalPages = computed(() => Math.max(1, Math.ceil(searchResult.value.total / searchForm.size)))

// ---- 列选择器状态 ----
const showColSelector = ref(false)
const colSelectorRef = ref(null)
const colVisible = reactive({
  region: true,
  gender: true,
  age: true,
  bmi: true,
  device: true,
  efficiency: true,
  latency: true,
  heartRate: true,
  spo2: true,
  snore: true,
  apnea: true,
  stress: true,
  medication: true
})
const colOptions = {
  region: '地区', gender: '性别', age: '年龄', bmi: 'BMI', device: '设备',
  efficiency: '效率', latency: '延迟', heartRate: '心率', spo2: '血氧',
  snore: '打鼾', apnea: '呼吸暂停', stress: '压力', medication: '服药'
}

// 动态计算列数
const visibleColsCount = computed(() => {
  let count = 3
  if (isRoot.value) count += 2
  Object.keys(colVisible).forEach(k => {
    if (colVisible[k]) count++
  })
  return count
})

// ---- 批量选择与批量删除状态 ----
const selectedRecords = ref([])

function isRowSelected(row) {
  return selectedRecords.value.some(r => r.userId === row.user_id && r.dateRecorded === row.date_recorded)
}

function toggleSelectRow(row) {
  const idx = selectedRecords.value.findIndex(r => r.userId === row.user_id && r.dateRecorded === row.date_recorded)
  if (idx > -1) {
    selectedRecords.value.splice(idx, 1)
  } else {
    selectedRecords.value.push({ userId: row.user_id, dateRecorded: row.date_recorded })
  }
}

const isAllSelected = computed(() => {
  const records = searchResult.value.records || []
  if (records.length === 0) return false
  return records.every(r => isRowSelected(r))
})

const isSomeSelected = computed(() => {
  const records = searchResult.value.records || []
  if (records.length === 0) return false
  const some = records.some(r => isRowSelected(r))
  return some && !isAllSelected.value
})

function toggleSelectAll() {
  const records = searchResult.value.records || []
  if (isAllSelected.value) {
    records.forEach(row => {
      const idx = selectedRecords.value.findIndex(r => r.userId === row.user_id && r.dateRecorded === row.date_recorded)
      if (idx > -1) {
        selectedRecords.value.splice(idx, 1)
      }
    })
  } else {
    records.forEach(row => {
      if (!isRowSelected(row)) {
        selectedRecords.value.push({ userId: row.user_id, dateRecorded: row.date_recorded })
      }
    })
  }
}

// 点击外部关闭列配置下拉菜单
function handleDocumentClick(e) {
  if (colSelectorRef.value && !colSelectorRef.value.contains(e.target)) {
    showColSelector.value = false
  }
}

// AI 智能语义检索 NLP 解析器
const nlpQueryText = ref('')
function parseNlpQuery() {
  const query = nlpQueryText.value.trim()
  if (!query) return
  
  // 重置条件
  searchForm.userId = ''
  searchForm.gender = ''
  searchForm.medicationFlag = null
  searchForm.insomniaFlag = null
  searchForm.region = ''
  searchForm.deviceModel = ''
  searchForm.ageMin = null
  searchForm.ageMax = null
  searchForm.sleepScoreMin = null
  searchForm.sleepScoreMax = null
  searchForm.snoreEventsMin = null
  searchForm.snoreEventsMax = null
  searchForm.bmiMin = null
  searchForm.bmiMax = null

  // 1. 地域解析
  if (query.includes('亚洲')) searchForm.region = '亚洲'
  else if (query.includes('欧洲')) searchForm.region = '欧洲'
  else if (query.includes('北美')) searchForm.region = '北美洲'
  else if (query.includes('大洋洲')) searchForm.region = '大洋洲'
  
  // 2. 性别解析
  if (query.includes('女') || query.includes('女性')) searchForm.gender = 'female'
  else if (query.includes('男') || query.includes('男性')) searchForm.gender = 'male'
  
  // 3. 服药状态
  if (query.includes('服药') || query.includes('吃药')) searchForm.medicationFlag = 1
  else if (query.includes('未服药') || query.includes('不服药') || query.includes('没有服药')) searchForm.medicationFlag = 0
  
  // 4. 失眠状态
  if (query.includes('失眠')) searchForm.insomniaFlag = 1
  else if (query.includes('无失眠') || query.includes('不失眠')) searchForm.insomniaFlag = 0

  // 5. 设备匹配
  const devices = ['AlphaWatch X1', 'OpenSmart v2', 'PulsePro 3', 'SleepSense S2', 'WristFit Z']
  devices.forEach(d => {
    if (query.toLowerCase().includes(d.toLowerCase()) || query.includes(d.replace(' ', ''))) {
      searchForm.deviceModel = d
    }
  })

  // 6. 年龄正则匹配 (例如：年龄大于50岁)
  const ageGtMatch = query.match(/(年龄|岁数)(大于|超过|>&ge;|ge|gt|above)\s*(\d+)/i) || query.match(/(\d+)\s*(岁以上|岁以后的)/)
  if (ageGtMatch) { searchForm.ageMin = parseInt(ageGtMatch[3] || ageGtMatch[1]) }
  const ageLtMatch = query.match(/(年龄|岁数)(小于|低于|<|&le;|le|lt|below)\s*(\d+)/i) || query.match(/(\d+)\s*(岁以下|岁之前的)/)
  if (ageLtMatch) { searchForm.ageMax = parseInt(ageLtMatch[3] || ageLtMatch[1]) }
  const ageBetweenMatch = query.match(/(\d+)\s*[-~至到]\s*(\d+)\s*岁/)
  if (ageBetweenMatch) {
    searchForm.ageMin = parseInt(ageBetweenMatch[1])
    searchForm.ageMax = parseInt(ageBetweenMatch[2])
  }

  // 7. 得分正则匹配
  const scoreGtMatch = query.match(/(得分|分数|质量)(大于|高于|超过|>)\s*(\d+)/) || query.match(/(\d+)\s*分以上/)
  if (scoreGtMatch) { searchForm.sleepScoreMin = parseInt(scoreGtMatch[3] || scoreGtMatch[1]) }
  const scoreLtMatch = query.match(/(得分|分数|质量)(小于|低于|<)\s*(\d+)/) || query.match(/(\d+)\s*分以下/)
  if (scoreLtMatch) { searchForm.sleepScoreMax = parseInt(scoreLtMatch[3] || scoreLtMatch[1]) }

  // 8. BMI 匹配
  const bmiGtMatch = query.match(/(bmi|体重指数)(大于|超过|>)\s*(\d+(\.\d+)?)/i)
  if (bmiGtMatch) { searchForm.bmiMin = parseFloat(bmiGtMatch[3]) }
  const bmiLtMatch = query.match(/(bmi|体重指数)(小于|低于|<)\s*(\d+(\.\d+)?)/i)
  if (bmiLtMatch) { searchForm.bmiMax = parseFloat(bmiLtMatch[3]) }

  onSearch(true)
}

// 导出 CSV 文件 (导出当前筛选下的全量大样本集，防止被分页阶段限制)
async function exportCsv() {
  const total = searchResult.value.total
  if (total === 0) {
    alert('当前无可供导出的检索记录')
    return
  }
  
  try {
    // 限制单次最大导出量为 30000 条，保护内存和性能（我们设置的 max_result_window 是 50000）
    const exportSize = Math.min(total, 30000)
    const exportForm = { ...searchForm, page: 1, size: exportSize }
    
    const res = await searchRecords(exportForm)
    const records = res.records || []
    
    if (records.length === 0) {
      alert('未获取到可导出的数据记录')
      return
    }

    const headers = ['用户ID', '日期', '地区', '性别', '年龄', 'BMI', '设备', '得分', '效率(%)', '入睡延迟(m)', '心率(bpm)', '最低血氧(%)', '打鼾次数', '呼吸暂停风险分', '压力值', '是否服药']
    const keys = ['user_id', 'date_recorded', 'region', 'gender', 'age', 'bmi', 'device_model', 'sleep_score', 'sleep_efficiency_pct', 'sleep_latency_minutes', 'heart_rate_mean_bpm', 'spo2_min_pct', 'snore_events', 'apnea_risk_score', 'stress_score', 'medication_flag']
    
    let csvContent = '\uFEFF' // Excel UTF-8 BOM
    csvContent += headers.join(',') + '\n'
    
    records.forEach(r => {
      const row = keys.map(k => {
        let val = r[k]
        if (k === 'gender') val = val === 'female' ? '女' : '男'
        if (k === 'medication_flag') val = val === 1 ? '是' : '否'
        if (typeof val === 'string' && val.includes(',')) {
          val = `"${val}"`
        }
        return val ?? ''
      })
      csvContent += row.join(',') + '\n'
    })
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.setAttribute('href', url)
    link.setAttribute('download', `睡眠监测数据表_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('获取数据失败，无法完成导出：' + (err.message || err))
  }
}

// 实时高危健康警报配置
const alertConfig = reactive({
  sleepScoreMax: 65,
  spo2Min: 90,
  apneaRiskMin: 28
})
function isWarningRecord(row) {
  return (
    (row.sleep_score !== null && row.sleep_score < alertConfig.sleepScoreMax) ||
    (row.spo2_min_pct !== null && row.spo2_min_pct < alertConfig.spo2Min) ||
    (row.apnea_risk_score !== null && row.apnea_risk_score >= alertConfig.apneaRiskMin)
  )
}

// 离线高危事件批处理检测日志 (扫描整个检索集，限制前 200 条大样本)
const allSearchRecordsForAlerts = ref([])
const batchAlerts = computed(() => {
  const records = allSearchRecordsForAlerts.value
  const list = []
  records.forEach(r => {
    if (isWarningRecord(r)) {
      const reasons = []
      if (r.sleep_score !== null && r.sleep_score < alertConfig.sleepScoreMax) {
        reasons.push(`睡眠得分偏低(${r.sleep_score}分)`)
      }
      if (r.spo2_min_pct !== null && r.spo2_min_pct < alertConfig.spo2Min) {
        reasons.push(`最低血氧低至(${r.spo2_min_pct}%)`)
      }
      if (r.apnea_risk_score !== null && r.apnea_risk_score >= alertConfig.apneaRiskMin) {
        reasons.push(`呼吸暂停高危(${r.apnea_risk_score}分)`)
      }
      
      list.push({
        date: r.date_recorded,
        userId: r.user_id,
        text: `用户 ${r.user_id} 触发高危指征: ${reasons.join('、')}`,
        type: (r.spo2_min_pct < 90 || r.apnea_risk_score >= 30) ? 'danger' : 'warning'
      })
    }
  })
  return list
})

// 睡眠电子档案查看与打印
const showDossierDialog = ref(false)
const selectedUserDossier = ref(null)
function openDossier(row) {
  selectedUserDossier.value = { ...row }
  showDossierDialog.value = true
}
function printDossier() {
  window.print()
}

// 右侧标签页状态
const activeTabRight = ref('stats')

// 动态地域分布 ECharts Pie Option (直接使用 ES 后端对整个匹配结果集做 Terms 聚合的真实比率数据，100% 精确覆盖全量文档)
const regionChartOption = computed(() => {
  const stats = searchResult.value.stats
  if (!stats || !stats.regionRatio) return {}
  const regionRatio = stats.regionRatio
  const keys = Object.keys(regionRatio)
  if (keys.length === 0) return {}
  const data = keys.map(key => ({ name: key, value: regionRatio[key] }))
  return {
    tooltip: {
      ...baseTooltip({ trigger: 'item' }),
      formatter: (params) => `${params.name}<br/>记录数：${params.value} 条 (${params.percent}%)`
    },
    legend: {
      orient: 'horizontal',
      bottom: 4,
      textStyle: { color: 'rgba(255,255,255,0.4)', fontSize: 8 },
      itemWidth: 10, itemHeight: 8
    },
    series: [{
      name: '地域占比',
      type: 'pie',
      radius: ['30%', '58%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: 'rgba(20,20,30,0.5)', borderWidth: 1 },
      label: { show: true, fontSize: 8, color: 'rgba(255,255,255,0.55)',
        formatter: '{b}\n{d}%' },
      emphasis: { label: { fontSize: 10, fontWeight: 'bold' } },
      data
    }]
  }
})

// 对比沙箱
const cohortA = ref(null)
const cohortB = ref(null)
function saveCohort(group) {
  const stats = searchResult.value.stats
  if (!stats) return
  
  const currentFilters = JSON.stringify({
    userId: searchForm.userId,
    gender: searchForm.gender,
    medicationFlag: searchForm.medicationFlag,
    insomniaFlag: searchForm.insomniaFlag,
    region: searchForm.region,
    deviceModel: searchForm.deviceModel,
    ageMin: searchForm.ageMin,
    ageMax: searchForm.ageMax,
    sleepScoreMin: searchForm.sleepScoreMin,
    sleepScoreMax: searchForm.sleepScoreMax,
    snoreEventsMin: searchForm.snoreEventsMin,
    snoreEventsMax: searchForm.snoreEventsMax,
    bmiMin: searchForm.bmiMin,
    bmiMax: searchForm.bmiMax
  })
  
  if (currentFilters !== lastSearchedFilters.value) {
    // 发现当前筛选配置发生改变但未点击“检索”，自动帮用户先检索对齐，再保存
    onSearch(true).then(() => {
      executeSaveCohort(group)
    }).catch(err => {
      alert('自动更新检索数据失败: ' + err.message)
    })
  } else {
    executeSaveCohort(group)
  }
}

function executeSaveCohort(group) {
  const stats = searchResult.value.stats
  if (!stats) return
  const payload = {
    name: group === 'A' ? '对比组 A' : '对比组 B',
    total: searchResult.value.total,
    stats: JSON.parse(JSON.stringify(stats))
  }
  if (group === 'A') cohortA.value = payload
  else cohortB.value = payload
  alert(`已将当前筛选出的 ${searchResult.value.total} 条数据保存为【${payload.name}】！`)
}

// 对比沙箱图形数值自适应放大器（在雷达图上稍微拉开极小差异的折线，方便视觉感知；原数据在下方表格和悬停提示中保持精确）
function getPlottedValues(valA, valB, maxVal) {
  const avg = (valA + valB) / 2
  const diff = Math.abs(valA - valB)
  const diffPct = diff / maxVal
  
  // 目标最小视觉差异为量程的 7%
  const targetPct = 0.07
  if (diffPct > 0 && diffPct < targetPct) {
    const factor = targetPct / diffPct
    const plottedA = avg + (valA - avg) * factor
    const plottedB = avg + (valB - avg) * factor
    return [
      Math.min(maxVal, Math.max(0, plottedA)),
      Math.min(maxVal, Math.max(0, plottedB))
    ]
  }
  return [valA, valB]
}

const sandboxRadarOption = computed(() => {
  if (!cohortA.value && !cohortB.value) return {}
  const radarIndicator = [
    { name: '睡眠得分', max: 100 },
    { name: '睡眠效率', max: 100 },
    { name: '压力负荷', max: 100 },
    { name: '屏幕时间(m)', max: 180 },
    { name: '咖啡因(mg)', max: 300 }
  ]
  
  const seriesData = []
  
  if (cohortA.value && cohortB.value) {
    const sA = cohortA.value.stats
    const sB = cohortB.value.stats
    
    // 应用视觉放大
    const [scoreA, scoreB] = getPlottedValues(parseFloat(sA.avgSleepScore), parseFloat(sB.avgSleepScore), 100)
    const [effA, effB] = getPlottedValues(parseFloat(sA.avgEfficiency), parseFloat(sB.avgEfficiency), 100)
    const [stressA, stressB] = getPlottedValues(parseFloat(sA.avgStress), parseFloat(sB.avgStress), 100)
    const [screenA, screenB] = getPlottedValues(parseFloat(sA.avgScreenTime), parseFloat(sB.avgScreenTime), 180)
    const [caffeineA, caffeineB] = getPlottedValues(parseFloat(sA.avgCaffeine), parseFloat(sB.avgCaffeine), 300)
    
    seriesData.push({
      value: [scoreA, effA, stressA, screenA, caffeineA],
      name: cohortA.value.name,
      itemStyle: { color: 'rgba(90, 171, 154, 0.85)' },
      areaStyle: { color: 'rgba(90, 171, 154, 0.15)' }
    })
    
    seriesData.push({
      value: [scoreB, effB, stressB, screenB, caffeineB],
      name: cohortB.value.name,
      itemStyle: { color: 'rgba(212, 133, 123, 0.85)' },
      areaStyle: { color: 'rgba(212, 133, 123, 0.15)' }
    })
  } else {
    // 只有单侧数据时，使用原值绘制
    if (cohortA.value) {
      const s = cohortA.value.stats
      seriesData.push({
        value: [
          parseFloat(s.avgSleepScore), parseFloat(s.avgEfficiency),
          parseFloat(s.avgStress), parseFloat(s.avgScreenTime), parseFloat(s.avgCaffeine)
        ],
        name: cohortA.value.name,
        itemStyle: { color: 'rgba(90, 171, 154, 0.85)' },
        areaStyle: { color: 'rgba(90, 171, 154, 0.15)' }
      })
    }
    if (cohortB.value) {
      const s = cohortB.value.stats
      seriesData.push({
        value: [
          parseFloat(s.avgSleepScore), parseFloat(s.avgEfficiency),
          parseFloat(s.avgStress), parseFloat(s.avgScreenTime), parseFloat(s.avgCaffeine)
        ],
        name: cohortB.value.name,
        itemStyle: { color: 'rgba(212, 133, 123, 0.85)' },
        areaStyle: { color: 'rgba(212, 133, 123, 0.15)' }
      })
    }
  }
  
  return {
    tooltip: {
      show: true,
      trigger: 'item',
      backgroundColor: 'rgba(20,20,30,0.95)',
      borderColor: 'rgba(255,255,255,0.08)',
      borderWidth: 1,
      textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 10 },
      extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,0.5); border-radius:8px; padding:8px 12px;',
      formatter: () => {
        let html = `<div style="font-weight:600;margin-bottom:6px;font-size:11px">📊 对比组特征明细 (原始值)</div>`
        if (cohortA.value) {
          const s = cohortA.value.stats
          html += `<div style="margin:2px 0;color:#7deacb">
            <b>${cohortA.value.name}</b> (样本: ${cohortA.value.total}人):<br/>
            得分: ${s.avgSleepScore} | 效率: ${s.avgEfficiency}% | 压力: ${s.avgStress}<br/>
            屏幕: ${s.avgScreenTime}m | 咖啡因: ${s.avgCaffeine}mg
          </div>`
        }
        if (cohortB.value) {
          const s = cohortB.value.stats
          html += `<div style="margin:6px 0 2px 0;color:#ff9da9">
            <b>${cohortB.value.name}</b> (样本: ${cohortB.value.total}人):<br/>
            得分: ${s.avgSleepScore} | 效率: ${s.avgEfficiency}% | 压力: ${s.avgStress}<br/>
            屏幕: ${s.avgScreenTime}m | 咖啡因: ${s.avgCaffeine}mg
          </div>`
        }
        return html
      }
    },
    legend: {
      data: seriesData.map(d => d.name),
      textStyle: { color: 'rgba(255,255,255,0.4)', fontSize: 8.5 },
      top: 5
    },
    radar: {
      indicator: radarIndicator,
      shape: 'circle',
      center: ['50%', '55%'],
      radius: '62%',
      axisName: { color: 'rgba(255,255,255,0.3)', fontSize: 8.5 },
      splitArea: { show: false },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [{
      type: 'radar', data: seriesData
    }]
  }
})

// ---- 临床总结生成 ----
const diagnosticReport = computed(() => {
  const stats = searchResult.value.stats
  if (!stats || searchResult.value.total === 0) return null

  const items = []
  
  const score = parseFloat(stats.avgSleepScore)
  if (!isNaN(score)) {
    if (score >= 80) {
      items.push({
        type: 'success', title: '睡眠质量优异',
        desc: `群体平均睡眠得分达到 ${score.toFixed(1)} 分，整体睡眠状况极佳，脑力与体能恢复效率高。`
      })
    } else if (score >= 70) {
      items.push({
        type: 'warning', title: '睡眠质量中等',
        desc: `人群睡眠分数（${score.toFixed(1)} 分）尚可，但存在一定改善空间。建议排查睡眠环境的温湿度及寝具舒适度。`
      })
    } else {
      items.push({
        type: 'danger', title: '睡眠质量偏低',
        desc: `群体平均睡眠得分仅有 ${score.toFixed(1)} 分，存在明显的睡眠不足或低效情况，处于疲劳亚健康状态。`
      })
    }
  }

  const eff = parseFloat(stats.avgEfficiency)
  if (!isNaN(eff)) {
    if (eff >= 92) {
      items.push({
        type: 'success', title: `高效睡眠通道 (Efficiency: ${eff.toFixed(1)}%)`,
        desc: `群体平均睡眠效率达 ${eff.toFixed(1)}%，入睡后实际睡眠比例高，夜间清醒或辗转频次合理。`
      })
    } else if (eff >= 85) {
      items.push({
        type: 'warning', title: `睡眠维持度不足 (Efficiency: ${eff.toFixed(1)}%)`,
        desc: `平均睡眠效率降至 ${eff.toFixed(1)}%，表明在床清醒时间略长。建议睡前30分钟停止工作，建立入睡前置仪式。`
      })
    } else {
      items.push({
        type: 'danger', title: `睡眠重度碎片化 (Efficiency: ${eff.toFixed(1)}%)`,
        desc: `睡眠效率低于 85% 的警戒线（仅 ${eff.toFixed(1)}%），表明夜间微觉醒频发或清醒时间过长，深度睡眠严重受损。`
      })
    }
  }

  const stress = parseFloat(stats.avgStress)
  if (!isNaN(stress)) {
    if (stress > 45) {
      items.push({
        type: 'danger', title: `压力负荷超标 (Stress: ${stress.toFixed(1)})`,
        desc: `该人群平均压力值偏高（${stress.toFixed(1)}），易导致交感神经过度兴奋，阻碍深度睡眠。建议睡前进行正念冥想。`
      })
    } else if (stress > 30) {
      items.push({
        type: 'warning', title: `压力值温和偏高 (Stress: ${stress.toFixed(1)})`,
        desc: `平均压力值为 ${stress.toFixed(1)}，处于黄色警戒区。建议工作间隙进行深呼吸调节，避免将紧张情绪带入卧室。`
      })
    } else {
      items.push({
        type: 'success', title: `情绪防御状态优良 (Stress: ${stress.toFixed(1)})`,
        desc: `压力均值仅有 ${stress.toFixed(1)}，表明群体情绪放松，植物神经处于平衡状态，利于深睡眠。`
      })
    }
  }

  const screen = parseFloat(stats.avgScreenTime)
  if (!isNaN(screen)) {
    if (screen > 45) {
      items.push({
        type: 'warning', title: `睡前蓝光负荷偏高 (Screen: ${screen.toFixed(1)}m)`,
        desc: `睡前屏幕平均使用时长达 ${screen.toFixed(1)} 分钟。蓝光会压抑褪黑素自然分泌，强烈建议睡前使用防蓝光眼镜或提前断电。`
      })
    } else if (screen > 20) {
      items.push({
        type: 'success', title: `睡前数码接触温和 (Screen: ${screen.toFixed(1)}m)`,
        desc: `睡前屏幕时间为 ${screen.toFixed(1)} 分钟，处于合理范围。继续保持，避免浏览过于刺激的内容。`
      })
    }
  }

  const caffeine = parseFloat(stats.avgCaffeine)
  if (!isNaN(caffeine)) {
    if (caffeine > 100) {
      items.push({
        type: 'warning', title: `日间咖啡因堆积 (Caffeine: ${caffeine.toFixed(1)}mg)`,
        desc: `日均咖啡因摄入达 ${caffeine.toFixed(1)}mg。下午 2 点后建议禁止摄入咖啡、茶或可乐，给腺苷受体腾出通道。`
      })
    }
  }

  const alcohol = parseFloat(stats.avgAlcohol)
  if (!isNaN(alcohol)) {
    if (alcohol > 0.8) {
      items.push({
        type: 'danger', title: `酒精破坏深睡眠 (Alcohol: ${alcohol.toFixed(1)}U)`,
        desc: `均饮酒超标（${alcohol.toFixed(1)} 单位）。酒精会严重缩短 REM（快速眼动）睡眠并引发后半夜频繁微觉醒。`
      })
    } else if (alcohol > 0.3) {
      items.push({
        type: 'warning', title: `轻度酒精干扰 (Alcohol: ${alcohol.toFixed(1)}U)`,
        desc: `日均饮酒量为 ${alcohol.toFixed(1)} 单位。建议将饮酒时间推前至睡前 3 小时以上，给肝脏预留代谢解毒时间。`
      })
    }
  }

  const recordsList = allSearchRecordsForAlerts.value
  if (recordsList.length > 0) {
    const highApneaCount = recordsList.filter(r => r.apnea_risk_score >= 20).length
    const highApneaPct = (highApneaCount / recordsList.length) * 100
    if (highApneaPct > 10) {
      items.push({
        type: 'danger', title: `气道塌陷与缺氧风险 (高危者 ${highApneaPct.toFixed(1)}%)`,
        desc: `筛选子集内呼吸暂停分值 >= 20 的人群比例达 ${highApneaPct.toFixed(1)}%。建议严重打鼾者侧卧入睡或使用助眠枕。`
      })
    }
  }

  return items
})

const lastSearchedFilters = ref('')

async function onSearch(resetPage = false) {
  if (resetPage) searchForm.page = 1
  selectedRecords.value = []
  searching.value = true
  try {
    // 1. 发起当前页的分页查询
    const res = await searchRecords(searchForm)
    searchResult.value = res
    searched.value = true
    
    // 2. 发起大样本量查询，用于扫描整个结果集中的高危指征和计算地域/诊断分布 (仅在筛选条件改变即 resetPage 为 true 时更新，避免翻页重复请求)
    if (resetPage) {
      const alertSearchForm = { ...searchForm, page: 1, size: 200 }
      const alertRes = await searchRecords(alertSearchForm)
      allSearchRecordsForAlerts.value = alertRes.records || []
    }
    
    lastSearchedFilters.value = JSON.stringify({
      userId: searchForm.userId,
      gender: searchForm.gender,
      medicationFlag: searchForm.medicationFlag,
      insomniaFlag: searchForm.insomniaFlag,
      region: searchForm.region,
      deviceModel: searchForm.deviceModel,
      ageMin: searchForm.ageMin,
      ageMax: searchForm.ageMax,
      sleepScoreMin: searchForm.sleepScoreMin,
      sleepScoreMax: searchForm.sleepScoreMax,
      snoreEventsMin: searchForm.snoreEventsMin,
      snoreEventsMax: searchForm.snoreEventsMax,
      bmiMin: searchForm.bmiMin,
      bmiMax: searchForm.bmiMax
    })
  } finally {
    searching.value = false
  }
}

function toggleSort(field) {
  if (searchForm.sortField === field) { searchForm.sortOrder = searchForm.sortOrder === 'asc' ? 'desc' : 'asc' }
  else { searchForm.sortField = field; searchForm.sortOrder = 'desc' }
  onSearch(true)
}
function getSortIcon(field) {
  if (searchForm.sortField !== field) return '↕'
  return searchForm.sortOrder === 'asc' ? '▲' : '▼'
}
function changePage(page) {
  if (page < 1 || page > totalPages.value) return
  searchForm.page = page; onSearch(false)
}

const showImportDialog = ref(false)
const importTab = ref('form')
const jsonContent = ref('')
const importing = ref(false)
const isEdit = ref(false)

// 缓存 2000 个现有的用户 ID
const existingUserIds = computed(() => {
  return Array.from({ length: 2000 }, (_, i) => 'user_' + String(i + 1).padStart(5, '0'))
})
const existingDatesForUser = ref([])

const userSearchQuery = ref('')
const showUserDropdown = ref(false)

const filteredUserIds = computed(() => {
  const query = userSearchQuery.value.trim().toLowerCase()
  if (!query) {
    return existingUserIds.value.slice(0, 50) // 默认只展示前50个，支持滚动选择
  }
  return existingUserIds.value.filter(uid => uid.toLowerCase().includes(query)).slice(0, 50)
})

function selectUser(uid) {
  importForm.user_id = uid
  userSearchQuery.value = uid
  showUserDropdown.value = false
  onUserSelected()
}

const importForm = reactive({
  user_id: '', date_recorded: '',
  gender: 'male', age: '', bmi: '', region: '亚洲', device_model: 'AlphaWatch X1',
  sleep_score: '', sleep_efficiency_pct: '', sleep_latency_minutes: '',
  heart_rate_mean_bpm: '', spo2_min_pct: '', snore_events: '', medication_flag: 0,
  apnea_risk_score: '', stress_score: ''
})

// 当选择了用户时，自动读取并锁定其基本生物特征，并拉取该用户所有已有的日期记录
async function onUserSelected() {
  const userId = importForm.user_id
  if (!userId) {
    existingDatesForUser.value = []
    return
  }
  try {
    // 1. 从后端读取该用户的基本健康特征
    const res = await getProfileDetail(userId)
    if (res) {
      importForm.gender = res.gender || 'male'
      importForm.age = res.age || ''
      importForm.region = res.region || '亚洲'
      importForm.device_model = res.deviceModel || 'AlphaWatch X1'
      importForm.bmi = res.bmi || ''
    }
    
    // 2. 从 ES 中查询该用户已有的所有检测日期，用于日期冲突校验
    const esRes = await searchRecords({ userId: userId, page: 1, size: 1000 })
    const records = esRes.records || []
    existingDatesForUser.value = records.map(r => r.date_recorded)
  } catch (err) {
    console.error('获取用户基础特征失败', err)
  }
}

async function submitImport() {
  let list = []
  if (importTab.value === 'form' || isEdit.value) {
    if (!importForm.user_id || !importForm.date_recorded) { alert('请填写用户ID和日期'); return }
    
    // 新增时限制不能录入已经存在的日期记录
    if (!isEdit.value && existingDatesForUser.value.includes(importForm.date_recorded.trim())) {
      alert(`用户 ${importForm.user_id} 在日期 ${importForm.date_recorded} 已存在健康数据记录，不能重复录入！`)
      return
    }

    if (isEdit.value) {
      importing.value = true
      try { await updateRecord(importForm); alert('保存成功'); showImportDialog.value = false; onSearch(false) }
      catch (e) { alert('保存失败: ' + (e.response?.data?.message || e.message)) }
      finally { importing.value = false }
      return
    }
    list.push({ ...importForm })
  } else {
    try { list = JSON.parse(jsonContent.value); if (!Array.isArray(list)) { alert('请输入JSON数组'); return } }
    catch (e) { alert('JSON格式错误: ' + e.message); return }
  }
  if (list.length === 0) { alert('无有效数据'); return }
  importing.value = true
  try { await importRecords(list); alert('导入成功'); showImportDialog.value = false; onSearch(true) }
  catch (e) { alert('导入失败: ' + (e.response?.data?.message || e.message)) }
  finally { importing.value = false }
}

function openImport() {
  isEdit.value = false; showImportDialog.value = true
  existingDatesForUser.value = []
  userSearchQuery.value = ''
  showUserDropdown.value = false
  Object.assign(importForm, {
    user_id: '', date_recorded: '',
    gender: 'male', age: '', bmi: '', region: '亚洲', device_model: 'AlphaWatch X1',
    sleep_score: '', sleep_efficiency_pct: '', sleep_latency_minutes: '',
    heart_rate_mean_bpm: '', spo2_min_pct: '', snore_events: '', medication_flag: 0,
    apnea_risk_score: '', stress_score: ''
  })
}
function onEdit(row) {
  isEdit.value = true; showImportDialog.value = true; importTab.value = 'form'
  existingDatesForUser.value = []
  userSearchQuery.value = row.user_id
  showUserDropdown.value = false
  Object.assign(importForm, {
    user_id: row.user_id, date_recorded: row.date_recorded,
    gender: row.gender || 'male', age: row.age, bmi: row.bmi,
    region: row.region || '亚洲', device_model: row.device_model || 'AlphaWatch X1',
    sleep_score: row.sleep_score, sleep_efficiency_pct: row.sleep_efficiency_pct,
    sleep_latency_minutes: row.sleep_latency_minutes, heart_rate_mean_bpm: row.heart_rate_mean_bpm,
    spo2_min_pct: row.spo2_min_pct || 94.0, snore_events: row.snore_events, medication_flag: row.medication_flag || 0,
    apnea_risk_score: row.apnea_risk_score, stress_score: row.stress_score
  })
}
async function onDelete(row) {
  if (!confirm(`确认删除 ${row.user_id} (${row.date_recorded})?`)) return
  try { await deleteRecord(row.user_id, row.date_recorded); alert('已删除'); onSearch(false) }
  catch (e) { alert('删除失败: ' + (e.response?.data?.message || e.message)) }
}
async function onBatchDelete() {
  if (!selectedRecords.value.length) return
  if (!confirm(`确认批量删除已选中的 ${selectedRecords.value.length} 条睡眠记录?`)) return
  try {
    await batchDeleteRecords(selectedRecords.value)
    alert('批量删除成功')
    selectedRecords.value = []
    onSearch(false)
  } catch (e) {
    alert('批量删除失败: ' + (e.response?.data?.message || e.message))
  }
}
function getGenderPct(gender) {
  if (!searchResult.value.stats?.genderRatio) return 0
  const r = searchResult.value.stats.genderRatio
  const femaleCnt = r.female || 0
  const maleCnt = r.male || 0
  const total = femaleCnt + maleCnt
  if (total === 0) return 0
  return gender === 'female' ? Math.round((femaleCnt / total) * 100) : Math.round((maleCnt / total) * 100)
}

function scoreClass(score) {
  if (score >= 80) return 'score-high'
  if (score >= 70) return 'score-mid'
  return 'score-low'
}

onMounted(async () => {
  document.addEventListener('click', handleDocumentClick)
  try {
    const res = await getRangeLimits()
    if (res) { limits.value = res }
  } catch (e) {
    console.error('获取检索范围极值失败:', e)
  }
  onSearch(true)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  gap: 12px;
  box-sizing: border-box;
}

/* ---- 搜索栏 ---- */
.search-bar {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  margin-bottom: 8px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-nlp-search-group {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(90, 171, 154, 0.06);
  border: 1px solid rgba(90, 171, 154, 0.3);
  padding: 4px 10px;
  border-radius: 8px;
}
.nlp-icon {
  font-size: 11px;
  font-weight: 600;
  color: #7deacb;
  white-space: nowrap;
}
.nlp-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 11px;
  outline: none;
}
.btn-nlp-apply {
  background: rgba(90, 171, 154, 0.2);
  border: 1px solid rgba(90, 171, 154, 0.5);
  color: #7deacb;
  font-size: 9.5px;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-nlp-apply:hover {
  background: rgba(90, 171, 154, 0.3);
  border-color: #7deacb;
}

.sb-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 6px 8px; }
.sb-actions { display: flex; justify-content: flex-end; gap: 8px; }
.sf-group { display: flex; flex-direction: column; gap: 2px; }
.sf-label { font-size: 10px; color: var(--text-tertiary); font-weight: 500; }
.limit-span { font-size: 9px; color: var(--accent-teal); margin-left: 4px; font-family: var(--font-mono); opacity: 0.8; }
.sf-input {
  background: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-subtle);
  border-radius: 7px; padding: 5px 8px; font-size: 11px; font-family: var(--font-sans);
  width: 100%; transition: border-color 0.2s; outline: none;
}
.sf-input option {
  background-color: #131525;
  color: #eeede6;
}
.sf-input:focus { border-color: var(--border-focus); }
.sf-range { display: flex; align-items: center; gap: 4px; }
.sf-range .sf-input { flex: 1; min-width: 0; }
.sf-sep { color: var(--text-tertiary); font-size: 11px; flex-shrink: 0; }
.btn-sm { padding: 6px 14px; font-size: 12px; }

/* ---- 内容行 ---- */
.content-row {
  display: grid;
  grid-template-columns: 2.5fr 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
  height: 100%;
}

/* ---- 卡片 ---- */
.card {
  background: linear-gradient(135deg, rgba(35, 40, 68, 0.5) 0%, rgba(20, 22, 38, 0.35) 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-lg);
  padding: 14px 18px;
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
  margin-bottom: 10px;
  flex-shrink: 0;
}
.card-title { font-family: var(--font-title); font-size: 14px; font-weight: 600; color: var(--text-primary); letter-spacing: 0.02em; }
.card-sub { font-size: 10px; color: var(--text-tertiary); letter-spacing: 0.05em; text-transform: uppercase; }

/* Tabs Header for Right Card */
.card-tabs-header {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 10px;
  padding-bottom: 4px;
  flex-shrink: 0;
}
.tab-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 10.5px;
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

.stats-panel-content, .charts-panel-content, .alerts-panel-content, .sandbox-panel-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

/* Region pie chart */
.charts-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}
.charts-title-sub {
  font-size: 10px;
  color: var(--text-secondary);
  font-weight: 600;
  margin-bottom: 6px;
  text-align: left;
}
.mini-chart-container {
  flex: 1;
  min-height: 180px;
  position: relative;
}
.charts-summary-box {
  background: rgba(255, 255, 255, 0.015);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 9.5px;
  color: var(--text-secondary);
  line-height: 1.4;
  margin-top: 6px;
  text-align: left;
}

/* Warnings row highlighter */
.row-warning {
  background: rgba(212, 133, 123, 0.06) !important;
}
.row-warning:hover {
  background: rgba(212, 133, 123, 0.1) !important;
}
.warning-blink {
  animation: blink-red 1s infinite alternate;
  margin-right: 2px;
}
@keyframes blink-red {
  from { opacity: 0.3; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1.1); }
}

/* Live warnings ticker */
.alerts-config-box {
  background: rgba(255,255,255,0.015);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 6px;
  padding: 6px 10px;
  margin-bottom: 8px;
}
.ac-head-sub {
  font-size: 9.5px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 4px;
  text-align: left;
}
.ac-form {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
.acf-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.acf-item label {
  font-size: 9.5px;
  color: var(--text-tertiary);
  white-space: nowrap;
}
.acf-input {
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(255,255,255,0.1);
  color: var(--text-primary);
  width: 32px;
  font-size: 9.5px;
  padding: 2px 4px;
  border-radius: 3px;
  text-align: center;
  outline: none;
}
.acf-input:focus {
  border-color: var(--accent-teal);
}
.alerts-stream-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.asb-head {
  font-size: 9.5px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 4px;
  text-align: left;
}
.asb-list {
  flex: 1;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 6px;
  padding: 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.asb-item {
  font-size: 9.5px;
  line-height: 1.4;
  text-align: left;
  border-left: 2px solid transparent;
  padding-left: 6px;
}
.asbi-time {
  font-family: var(--font-mono);
  color: var(--text-tertiary);
  margin-right: 6px;
}
.asbi-text {
  color: var(--text-secondary);
}
.asb-item--danger { border-left-color: var(--accent-rose); }
.asb-item--danger .asbi-text { color: #ff9da9; }
.asb-item--warning { border-left-color: var(--accent-amber); }
.asb-item--warning .asbi-text { color: #ffc27d; }
.asb-item--success { border-left-color: var(--accent-teal); }
.asb-item--success .asbi-text { color: #87ffd7; }

/* Sandbox panel */
.sandbox-cohort-controls {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
  flex-shrink: 0;
}
.btn-save-sb {
  flex: 1;
  padding: 3px 6px;
  font-size: 9.5px;
  background: rgba(90, 171, 154, 0.15);
  border: 1px solid rgba(90, 171, 154, 0.3);
  color: #7deacb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}
.btn-save-sb:hover {
  background: rgba(90, 171, 154, 0.25);
  border-color: #7deacb;
}
.sandbox-cohort-indicator {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
  flex-shrink: 0;
}
.sci-badge {
  flex: 1;
  text-align: center;
  font-size: 9px;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: var(--font-mono);
}
.sci-badge-a { background: rgba(90, 171, 154, 0.1); color: var(--accent-teal); border: 1px solid rgba(90, 171, 154, 0.2); }
.sci-badge-b { background: rgba(212, 133, 123, 0.1); color: var(--accent-rose); border: 1px solid rgba(212, 133, 123, 0.2); }
.sci-badge-empty { background: rgba(255,255,255,0.02); color: var(--text-tertiary); border: 1px solid rgba(255,255,255,0.05); }

.radar-chart-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.radar-chart-container {
  flex: 1;
  min-height: 180px;
  position: relative;
}
.sb-radar-clear {
  display: flex;
  justify-content: center;
  margin-top: 4px;
  flex-shrink: 0;
}

/* Dossier Dialog & Printing styles */
.dossier-dialog {
  max-width: 580px !important;
  border: 1px solid rgba(255,255,255,0.2) !important;
  background: #151829 !important;
}
.dossier-header {
  border-bottom: 2px solid var(--accent-teal);
  padding-bottom: 6px;
  margin-bottom: 12px;
  text-align: center;
}
.dh-title {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 700;
  color: #7deacb;
  letter-spacing: 0.08em;
}
.dh-subtitle {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.dossier-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px 12px;
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 12px;
}
.dmg-item {
  font-size: 10.5px;
  color: var(--text-secondary);
  text-align: left;
}
.dmg-item strong {
  color: var(--text-primary);
}
.dossier-section-title {
  font-size: 11px;
  font-weight: 600;
  color: #eeede6;
  border-left: 3px solid var(--accent-teal);
  padding-left: 6px;
  margin-bottom: 8px;
  text-align: left;
}
.dossier-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}
.dmg-card {
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 6px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  min-height: 60px;
}
.dmc-label {
  font-size: 9px;
  color: var(--text-tertiary);
}
.dmc-val {
  font-family: var(--font-mono);
  font-size: 14.5px;
  font-weight: 700;
  color: var(--text-primary);
}
.dmc-status {
  font-size: 8px;
  padding: 0.5px 4px;
  border-radius: 3px;
  font-weight: 600;
}
.dmc-desc {
  font-size: 8px;
  color: var(--text-secondary);
}
.score-card {
  background: rgba(90, 171, 154, 0.08);
  border-color: rgba(90, 171, 154, 0.2);
}
.score-card .dmc-val {
  color: #7deacb;
  font-size: 18px;
}
.warning-border {
  border-color: rgba(212, 133, 123, 0.4) !important;
  background: rgba(212, 133, 123, 0.06) !important;
}
.dossier-clinical-notes {
  background: rgba(255, 255, 255, 0.01);
  border: 1px dashed rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 10.5px;
  line-height: 1.5;
  color: var(--text-secondary);
  text-align: left;
}
.dossier-clinical-notes strong {
  color: var(--text-primary);
}

/* Print CSS */
@media print {
  body * {
    visibility: hidden;
  }
  .print-area, .print-area * {
    visibility: visible;
  }
  .print-area {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    background: white !important;
    color: #111 !important;
    padding: 20px;
    border-radius: 0;
  }
  .dh-title { color: #008080 !important; }
  .dossier-header { border-bottom-color: #008080 !important; }
  .dossier-meta-grid {
    background: #f5f5f5 !important;
    border-color: #ddd !important;
    color: #222 !important;
  }
  .dmg-item { color: #333 !important; }
  .dmg-item strong { color: #000 !important; }
  .dossier-section-title {
    color: #000 !important;
    border-left-color: #008080 !important;
  }
  .dmg-card {
    background: #fafafa !important;
    border-color: #ccc !important;
  }
  .dmc-label { color: #555 !important; }
  .dmc-val { color: #000 !important; }
  .score-card {
    background: #e6f2f2 !important;
    border-color: #b3d9d9 !important;
  }
  .score-card .dmc-val { color: #008080 !important; }
  .warning-border {
    border-color: #ffb3b3 !important;
    background: #ffe6e6 !important;
  }
  .dossier-clinical-notes {
    background: #fff !important;
    border-color: #ccc !important;
    color: #333 !important;
  }
  .dossier-clinical-notes strong { color: #000 !important; }
  .no-print { display: none !important; }
}

/* ---- 表格/其他 ---- */
.ch-left { display: flex; align-items: center; gap: 10px; }
.btn-batch-del { padding: 2px 8px; font-size: 9.5px; border-radius: 4px; cursor: pointer; }
.btn-col-config {
  background: none; border: 1px solid rgba(255, 255, 255, 0.12); color: var(--text-secondary);
  font-size: 10px; padding: 3px 10px; border-radius: 4px; cursor: pointer; transition: all 0.2s;
}
.btn-col-config:hover { border-color: rgba(255, 255, 255, 0.25); color: var(--text-primary); }
.col-selector-container { position: relative; }
.col-selector-dropdown {
  position: absolute; right: 0; top: 22px; z-index: 10; width: 220px;
  background: #141729; border: 1px solid rgba(255,255,255,0.15); border-radius: 6px;
  padding: 8px 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.5);
}
.csd-title { font-size: 10px; color: var(--text-tertiary); margin-bottom: 6px; font-weight: 600; text-align: left; }
.csd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 4px; }
.csd-item { display: flex; align-items: center; gap: 4px; font-size: 9.5px; color: var(--text-secondary); cursor: pointer; }
.csd-item input { cursor: pointer; }

.table-wrap { flex: 1; overflow-y: auto; margin-bottom: 8px; border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 6px; background: rgba(0, 0, 0, 0.15); }
.data-table { width: 100%; border-collapse: collapse; font-size: 10px; text-align: left; }
.data-table th {
  background: rgba(255, 255, 255, 0.02); color: var(--text-tertiary); font-weight: 500;
  padding: 6px 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.th-check, .td-check { width: 30px; text-align: center; }
.th-sort { cursor: pointer; }
.th-sort:hover { color: var(--text-primary); }
.sort-arrow { font-size: 8px; margin-left: 2px; }
.data-table td { padding: 6px 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.04); vertical-align: middle; }
.data-table tr:hover { background: rgba(255, 255, 255, 0.02); }
.data-table tr.row-selected { background: rgba(90, 171, 154, 0.1) !important; }
.mono { font-family: var(--font-mono); }

.badge-gender { font-size: 8.5px; padding: 1px 4px; border-radius: 3px; font-weight: 600; }
.badge-gender.female { background: rgba(212, 133, 123, 0.15); color: var(--accent-rose); }
.badge-gender.male { background: rgba(107, 168, 217, 0.15); color: var(--accent-sky); }
.score-badge { font-family: var(--font-mono); font-size: 9.5px; font-weight: 700; padding: 1px 6px; border-radius: 3px; }
.score-high { background: rgba(90, 171, 154, 0.15); color: var(--accent-teal); }
.score-mid { background: rgba(201, 151, 78, 0.15); color: var(--accent-amber); }
.score-low { background: rgba(212, 133, 123, 0.15); color: var(--accent-rose); }
.text-warn { color: var(--accent-rose); font-weight: 600; }
.badge-med { font-size: 8.5px; padding: 1px 4px; border-radius: 3px; color: var(--text-tertiary); background: rgba(255,255,255,0.04); }
.badge-med.active { background: rgba(90, 171, 154, 0.15); color: var(--accent-teal); }

.act-edit, .act-del, .act-dossier { background: none; border: none; cursor: pointer; font-size: 9.5px; padding: 2px 4px; transition: color 0.2s; }
.act-edit { color: var(--accent-teal); }
.act-edit:hover { color: #92ffd9; }
.act-dossier { color: var(--accent-sky); }
.act-dossier:hover { color: #a2d6ff; }
.act-del { color: var(--accent-rose); }
.act-del:hover { color: #ff9da9; }

.empty-cell { text-align: center; padding: 32px 0; color: var(--text-tertiary); font-size: 11px; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 10px; margin-top: 4px; flex-shrink: 0; }
.page-btn {
  background: none; border: 1px solid rgba(255, 255, 255, 0.12); color: var(--text-secondary);
  font-size: 10.5px; padding: 4px 10px; border-radius: 4px; cursor: pointer; transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { border-color: rgba(255, 255, 255, 0.25); color: var(--text-primary); }
.page-btn:disabled { color: var(--text-tertiary); opacity: 0.4; cursor: not-allowed; }
.page-info { font-family: var(--font-mono); font-size: 10.5px; color: var(--text-secondary); }

/* ---- 核心统计指标 ---- */
.stats-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
  padding-right: 4px;
}
.stat-block {
  background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-md); padding: 8px 12px; display: flex; flex-direction: column; gap: 6px;
}
.stat-head { font-size: 10px; font-weight: 600; color: var(--text-secondary); letter-spacing: 0.02em; text-align: left; }
.stat-grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.stat-metric { display: flex; flex-direction: column; gap: 2px; text-align: center; }
.sm-label { font-size: 8.5px; color: var(--text-tertiary); }
.sm-value { font-family: var(--font-mono); font-size: 16px; font-weight: 700; }
.color-amber { color: var(--accent-amber); }
.color-teal { color: var(--accent-teal); }
.color-sky { color: var(--accent-sky); }
.stat-divider { height: 1px; background: rgba(255, 255, 255, 0.05); }
.stat-grid-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; }
.sgr-col { display: flex; flex-direction: column; gap: 1px; text-align: center; }
.sgr-label { font-size: 8.5px; color: var(--text-tertiary); }
.sgr-val { font-family: var(--font-mono); font-size: 11px; color: var(--text-secondary); font-weight: 600; }

.gender-bar { display: flex; flex-direction: column; gap: 4px; }
.gender-labels { display: flex; justify-content: space-between; font-size: 9px; color: var(--text-secondary); }
.gender-track { height: 6px; border-radius: 3px; overflow: hidden; display: flex; background: rgba(255, 255, 255, 0.04); }
.gender-fill-f { height: 100%; background: linear-gradient(90deg, #d4857b 0%, #ee7f7f 100%); transition: width 0.5s ease; }
.gender-fill-m { height: 100%; background: linear-gradient(90deg, #6ba8d9 0%, #4ea8de 100%); transition: width 0.5s ease; }

.guidance-section { display: flex; flex-direction: column; gap: 6px; }
.guidance-list { display: flex; flex-direction: column; gap: 6px; }
.guide-card { padding: 6px 10px; border-radius: 6px; border: 1px solid rgba(255, 255, 255, 0.04); background: rgba(255, 255, 255, 0.01); display: flex; flex-direction: column; gap: 2px; }
.guide-title { font-size: 10px; font-weight: 600; display: flex; align-items: center; gap: 6px; text-align: left; }
.guide-status-dot { width: 4px; height: 4px; border-radius: 50%; }
.guide-desc { font-size: 9px; color: var(--text-secondary); line-height: 1.4; text-align: left; }

.guide-card--success { border-left: 2px solid var(--accent-teal); background: rgba(90, 171, 154, 0.02); }
.guide-card--success .guide-title { color: var(--accent-teal); }
.guide-card--success .guide-status-dot { background: var(--accent-teal); box-shadow: 0 0 4px var(--accent-teal); }

.guide-card--warning { border-left: 2px solid var(--accent-amber); background: rgba(201, 151, 78, 0.02); }
.guide-card--warning .guide-title { color: var(--accent-amber); }
.guide-card--warning .guide-status-dot { background: var(--accent-amber); box-shadow: 0 0 4px var(--accent-amber); }

.guide-card--danger { border-left: 2px solid var(--accent-rose); background: rgba(212, 133, 123, 0.02); }
.guide-card--danger .guide-title { color: var(--accent-rose); }
.guide-card--danger .guide-status-dot { background: var(--accent-rose); box-shadow: 0 0 4px var(--accent-rose); }

.guide-empty { font-size: 9.5px; color: var(--text-tertiary); text-align: center; padding: 24px 10px; line-height: 1.5; background: rgba(255,255,255,0.015); border: 1px dashed rgba(255,255,255,0.06); border-radius: 6px; }

/* ---- Modal ---- */
.modal-backdrop { position: fixed; inset: 0; z-index: 100; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.65); backdrop-filter: blur(4px); }
.modal-dialog { background: #131526; border: 1px solid rgba(255, 255, 255, 0.12); border-radius: var(--radius-lg); width: 480px; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 16px 48px rgba(0,0,0,0.5); }
.modal-head { display: flex; align-items: center; justify-content: space-between; padding: 12px 18px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.modal-head h3 { font-size: 13.5px; font-weight: 600; color: var(--text-primary); margin: 0; }
.modal-close { background: none; border: none; color: var(--text-tertiary); font-size: 18px; cursor: pointer; }
.modal-close:hover { color: var(--accent-rose); }
.modal-tabs { display: flex; border-bottom: 1px solid rgba(255,255,255,0.05); }
.modal-tabs .tab-btn { flex: 1; text-align: center; padding: 8px; border-radius: 0; border: none; border-bottom: 2px solid transparent; background: none; color: var(--text-tertiary); font-size: 11px; cursor: pointer; transition: all 0.2s; }
.modal-tabs .tab-btn.active { color: var(--accent-teal); border-bottom-color: var(--accent-teal); font-weight: 600; }
.modal-body { padding: 16px 20px; overflow-y: auto; max-height: 400px; }
.modal-foot { display: flex; justify-content: flex-end; gap: 10px; padding: 10px 18px; border-top: 1px solid rgba(255,255,255,0.08); }
.import-form { display: flex; flex-direction: column; gap: 8px; }
.form-row { display: flex; gap: 12px; }
.form-col { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.form-col label { font-size: 9.5px; color: var(--text-tertiary); text-align: left; }
.import-json { display: flex; flex-direction: column; gap: 6px; }
.json-label { font-size: 10px; color: var(--text-tertiary); text-align: left; }
.textarea { background: var(--bg-input); color: var(--text-primary); border: 1px solid var(--border-subtle); border-radius: 6px; font-family: var(--font-mono); font-size: 10.5px; padding: 8px; height: 160px; outline: none; resize: none; }
.textarea:focus { border-color: var(--border-focus); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.sandbox-comparison-table {
  margin-top: 8px;
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  padding: 6px 10px;
  flex-shrink: 0;
}
.sb-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 9px;
  text-align: left;
}
.sb-table th, .sb-table td {
  padding: 4px 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}
.sb-table th {
  color: var(--text-tertiary);
  font-weight: 600;
}
.sb-table td {
  color: var(--text-secondary);
}

/* ---- 搜索自动提示联想组件样式 ---- */
.autocomplete-container {
  position: relative;
  width: 100%;
}
.autocomplete-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 180px;
  overflow-y: auto;
  background-color: #1a1c2e;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 7px;
  z-index: 1050;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.6);
  margin-top: 2px;
}
.autocomplete-item {
  padding: 7px 10px;
  color: #eeede6;
  cursor: pointer;
  font-size: 11px;
  text-align: left;
  transition: background 0.15s, color 0.15s;
}
.autocomplete-item:hover {
  background-color: rgba(90, 171, 154, 0.15);
  color: #5aab9a;
}
</style>
