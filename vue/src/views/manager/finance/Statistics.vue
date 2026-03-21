<template>
  <div class="admin-scope">
    <!-- 时间筛选 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="display: flex; align-items: center; gap: 16px">
        <span>时间范围：</span>
        <el-radio-group v-model="dateType" @change="handleDateTypeChange">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="year">今年</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>

        <template v-if="dateType === 'custom'">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="load" />
        </template>

        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <!-- 核心数据卡片 -->
    <div class="stats-cards" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card recharge">
            <div class="stat-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户充值总额</div>
              <div class="stat-value">¥{{ formatMoney(statistics.userRecharge) }}</div>
              <div class="stat-sub">平台资金流入</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card income">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平台收入</div>
              <div class="stat-value">¥{{ formatMoney(statistics.platformIncome) }}</div>
              <div class="stat-sub">租车支付 + 押金扣除</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card expense">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">退款支出</div>
              <div class="stat-value">¥{{ formatMoney(Math.abs(statistics.refundExpense)) }}</div>
              <div class="stat-sub">用户退款金额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card profit">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平台净收益</div>
              <div class="stat-value">¥{{ formatMoney(statistics.netProfit) }}</div>
              <div class="stat-sub">平台收入 - 退款支出</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 额外统计卡片 -->
    <div class="stats-cards" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="stat-card total-flow">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总流水</div>
              <div class="stat-value">¥{{ formatMoney(statistics.totalFlow) }}</div>
              <div class="stat-sub">所有资金变动总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card orders">
            <div class="stat-icon">
              <el-icon><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">订单数量</div>
              <div class="stat-value">{{ orderStats.total || 0 }}</div>
              <div class="stat-sub">期间内订单总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card balance">
            <div class="stat-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平台总余额</div>
              <div class="stat-value">¥{{ formatMoney(statistics.totalBalance) }}</div>
              <div class="stat-sub">用户充值总额 - 租车支付 - 退款</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="card" style="margin-bottom: 5px">
      <div class="chart-header">
        <span class="chart-title">收支趋势</span>
        <el-select v-model="chartType" size="small" style="width: 100px" @change="loadChart">
          <el-option label="近7天" value="week" />
          <el-option label="近30天" value="month" />
          <el-option label="近12个月" value="year" />
        </el-select>
      </div>
      <div class="chart-container" ref="trendChartRef"></div>
    </div>

    <!-- 类型统计 -->
    <div class="card" style="margin-bottom: 5px">
      <div class="chart-header">
        <span class="chart-title">收支类型统计</span>
      </div>
      <div class="type-stats">
        <el-table :data="typeStats" stripe style="width: 100%" :border="false">
          <el-table-column prop="typeText" label="类型" width="120" />
          <el-table-column label="收入" width="150">
            <template #default="scope">
              <span class="income-amount">¥{{ formatMoney(scope.row.income) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="支出" width="150">
            <template #default="scope">
              <span class="expense-amount">¥{{ formatMoney(scope.row.expense) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="净额" width="150">
            <template #default="scope">
              <span :class="scope.row.net > 0 ? 'income-amount' : 'expense-amount'">
                ¥{{ formatMoney(scope.row.net) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="笔数" width="100" />
          <el-table-column label="占比" min-width="150">
            <template #default="scope">
              <div class="progress-bar">
                <div class="progress" :style="{ width: scope.row.percent + '%' }"></div>
                <span class="progress-label">{{ scope.row.percent }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 押金统计 -->
    <div class="card">
      <div class="chart-header">
        <span class="chart-title">押金统计</span>
      </div>
      <div class="deposit-stats">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="deposit-item frozen">
              <div class="deposit-label">冻结押金</div>
              <div class="deposit-value">¥{{ formatMoney(depositStats.totalFrozen) }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="deposit-item unfrozen">
              <div class="deposit-label">已解冻押金</div>
              <div class="deposit-value">¥{{ formatMoney(depositStats.totalUnfrozen) }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="deposit-item deducted">
              <div class="deposit-label">已扣除押金</div>
              <div class="deposit-value">¥{{ formatMoney(depositStats.totalDeducted) }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { TrendCharts, List, Wallet, Money } from '@element-plus/icons-vue'
import { financeApi } from '@/utils/api'

const trendChartRef = ref(null)
let trendChart = null

const dateType = ref('today')
const dateRange = ref([])
const chartType = ref('week')

const statistics = reactive({
  userRecharge: 0, // 用户充值总额
  platformIncome: 0, // 平台实际收入（租车+押金扣除）
  refundExpense: 0, // 退款支出
  netProfit: 0, // 平台净收益
  totalFlow: 0, // 总流水
  totalBalance: 0 // 平台总余额
})

const orderStats = reactive({
  total: 0
})

const typeStats = ref([])
const depositStats = reactive({
  totalFrozen: 0,
  totalUnfrozen: 0,
  totalDeducted: 0
})

// 格式化金额
const formatMoney = (value) => {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

// 获取日期范围
const getDateRange = () => {
  const now = new Date()
  let start = ''
  let end = ''

  // 格式化日期为 YYYY-MM-DD（使用本地时间）
  const formatLocalDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  switch (dateType.value) {
    case 'today':
      start = formatLocalDate(now)
      end = start
      break
    case 'week':
      const day = now.getDay()
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - (day === 0 ? 6 : day - 1))
      start = formatLocalDate(weekStart)
      end = formatLocalDate(now)
      break
    case 'month':
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      start = formatLocalDate(monthStart)
      end = formatLocalDate(now)
      break
    case 'year':
      const yearStart = new Date(now.getFullYear(), 0, 1)
      start = formatLocalDate(yearStart)
      end = formatLocalDate(now)
      break
    case 'custom':
      if (dateRange.value && dateRange.value.length === 2) {
        start = dateRange.value[0]
        end = dateRange.value[1]
      }
      break
  }

  return { startDate: start, endDate: end }
}

// 加载统计数据
const load = async () => {
  const { startDate, endDate } = getDateRange()
  if (!startDate || !endDate) return

  try {
    const res = await financeApi.getStatistics({ startDate, endDate })
    if (res.code === '200') {
      statistics.userRecharge = res.data.userRecharge || 0
      statistics.platformIncome = res.data.platformIncome || 0
      statistics.refundExpense = Math.abs(res.data.refundExpense || 0)
      statistics.netProfit = res.data.netProfit || 0
      statistics.totalFlow = res.data.totalFlow || 0
      statistics.totalBalance = res.data.totalBalance || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }

  loadChart()
  loadTypeStats()
  loadDepositStats()
  loadOrderStats()
}

// 加载趋势图表
const loadChart = async () => {
  const data = await getChartData()

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'] },
    xAxis: { type: 'category', data: data.dates },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      {
        name: '收入',
        type: 'line',
        data: data.incomes,
        smooth: true,
        lineStyle: { color: '#67c23a' },
        areaStyle: { opacity: 0.1, color: '#67c23a' }
      },
      {
        name: '支出',
        type: 'line',
        data: data.expenses,
        smooth: true,
        lineStyle: { color: '#f56c6c' },
        areaStyle: { opacity: 0.1, color: '#f56c6c' }
      }
    ],
    grid: { top: 30, left: 50, right: 30, bottom: 30 }
  }

  trendChart.setOption(option)
}

// 获取图表数据（需要后端接口）
const getChartData = async () => {
  // TODO: 调用后端接口获取图表数据
  return {
    dates: ['03-15', '03-16', '03-17', '03-18', '03-19', '03-20', '03-21'],
    incomes: [1200, 1500, 1800, 2100, 1900, 2300, 2600],
    expenses: [800, 900, 1000, 1100, 1050, 1200, 1300]
  }
}

// 加载类型统计
const loadTypeStats = async () => {
  const { startDate, endDate } = getDateRange()
  try {
    const res = await financeApi.getStatisticsByType({ startDate, endDate })
    if (res.code === '200') {
      const typeMap = {
        recharge: '充值',
        payment: '支付',
        refund: '退款',
        deposit_freeze: '押金冻结',
        deposit_unfreeze: '押金解冻',
        deposit_deduct: '押金扣除'
      }
      const totalIncome = statistics.platformIncome
      typeStats.value = (res.data || []).map((item) => ({
        ...item,
        typeText: typeMap[item.type] || item.type,
        net: (item.income || 0) - (item.expense || 0),
        percent: totalIncome > 0 ? Math.round((item.income / totalIncome) * 100) : 0
      }))
    }
  } catch (error) {
    console.error('加载类型统计失败:', error)
  }
}

// 加载订单统计
const loadOrderStats = async () => {
  const { startDate, endDate } = getDateRange()
  try {
    // TODO: 调用订单统计接口
    // const res = await orderApi.getOrderStats({ startDate, endDate })
    // orderStats.total = res.data.total || 0
  } catch (error) {
    console.error('加载订单统计失败:', error)
  }
}

// 加载押金统计
const loadDepositStats = async () => {
  try {
    const res = await financeApi.getDepositStats()
    if (res.code === '200') {
      depositStats.totalFrozen = res.data.totalFrozen || 0
      depositStats.totalUnfrozen = res.data.totalUnfrozen || 0
      depositStats.totalDeducted = res.data.totalDeducted || 0
    }
  } catch (error) {
    console.error('加载押金统计失败:', error)
  }
}

// 日期类型变化
const handleDateTypeChange = () => {
  if (dateType.value !== 'custom') {
    load()
  }
}

// 重置
const reset = () => {
  dateType.value = 'today'
  dateRange.value = []
  load()
}

onMounted(() => {
  load()
  window.addEventListener('resize', () => {
    if (trendChart) trendChart.resize()
  })
})
</script>

<style scoped>
.stats-cards {
  margin-bottom: 16px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #edf2f7;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card.recharge .stat-icon {
  color: #409eff;
}
.stat-card.income .stat-icon {
  color: #67c23a;
}
.stat-card.expense .stat-icon {
  color: #f56c6c;
}
.stat-card.profit .stat-icon {
  color: #c8a165;
}
.stat-card.total-flow .stat-icon {
  color: #e6a23c;
}
.stat-card.orders .stat-icon {
  color: #8b5cf6;
}
.stat-card.balance .stat-icon {
  color: #10b981;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e2a3a;
}

.stat-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  position: relative;
  padding-left: 12px;
}

.chart-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: #c8a165;
  border-radius: 2px;
}

.chart-container {
  height: 350px;
}

.progress-bar {
  position: relative;
  height: 20px;
  background: #edf2f7;
  border-radius: 10px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #c8a165, #b28b4f);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.progress-label {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: #1e2a3a;
}

.income-amount {
  color: #67c23a;
  font-weight: 600;
}

.expense-amount {
  color: #f56c6c;
  font-weight: 600;
}

.deposit-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border: 1px solid #edf2f7;
}

.deposit-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.deposit-value {
  font-size: 24px;
  font-weight: 700;
}

.deposit-item.frozen .deposit-value {
  color: #e6a23c;
}

.deposit-item.unfrozen .deposit-value {
  color: #67c23a;
}

.deposit-item.deducted .deposit-value {
  color: #f56c6c;
}
</style>
