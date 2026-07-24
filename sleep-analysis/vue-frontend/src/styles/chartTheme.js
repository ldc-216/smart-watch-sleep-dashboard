export const CHART_COLORS = ['#c9974e', '#5aab9a', '#d4857b', '#9b8ec4', '#6ba8d9', '#e8c97a', '#73c7b3']

export const AXIS_TEXT_COLOR = 'rgba(230, 228, 222, 0.35)'
export const AXIS_LINE_COLOR = 'rgba(255, 255, 255, 0.06)'
export const SPLIT_LINE_COLOR = 'rgba(255, 255, 255, 0.03)'

export function createLinearGradient(echarts, colorStart, colorEnd) {
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: colorStart },
    { offset: 1, color: colorEnd }
  ])
}

export function baseGrid(overrides = {}) {
  return {
    left: 16,
    right: 16,
    top: 40,
    bottom: 24,
    containLabel: true,
    ...overrides
  }
}

export function baseTooltip(overrides = {}) {
  return {
    trigger: 'axis',
    backgroundColor: 'rgba(20, 20, 30, 0.92)',
    borderColor: 'rgba(255, 255, 255, 0.08)',
    textStyle: { color: '#eeede6', fontFamily: 'Inter, sans-serif', fontSize: 12, fontWeight: 400 },
    extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.45); border-radius: 12px; padding: 10px 14px;',
    ...overrides
  }
}

export function categoryAxis(overrides = {}) {
  return {
    type: 'category',
    axisLine: { lineStyle: { color: AXIS_LINE_COLOR } },
    axisLabel: { color: AXIS_TEXT_COLOR, fontFamily: 'Inter, sans-serif', fontSize: 10.5, margin: 14 },
    axisTick: { show: false },
    ...overrides
  }
}

export function valueAxis(overrides = {}) {
  return {
    type: 'value',
    axisLine: { show: false },
    axisLabel: { color: AXIS_TEXT_COLOR, fontFamily: 'Inter, sans-serif', fontSize: 10.5, margin: 14 },
    splitLine: { lineStyle: { color: SPLIT_LINE_COLOR, type: 'solid' } },
    ...overrides
  }
}

export function baseLegend(overrides = {}) {
  return {
    top: 0,
    right: 0,
    textStyle: { color: AXIS_TEXT_COLOR, fontFamily: 'Inter, sans-serif', fontSize: 11.5, fontWeight: 400 },
    itemWidth: 8,
    itemHeight: 8,
    icon: 'roundRect',
    itemGap: 18,
    ...overrides
  }
}
