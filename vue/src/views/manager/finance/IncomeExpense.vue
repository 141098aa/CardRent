<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/流水号"
            :prefix-icon="Search"
            clearable
            @keyup.enter="load" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.type" placeholder="交易类型" clearable @change="load">
            <el-option label="全部" value="" />
            <el-option label="充值" value="recharge" />
            <el-option label="支付" value="payment" />
            <el-option label="退款" value="refund" />
            <el-option label="押金冻结" value="deposit_freeze" />
            <el-option label="押金解冻" value="deposit_unfreeze" />
            <el-option label="押金扣除" value="deposit_deduct" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="load" />
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch" plain>重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 统计卡片 -->
    <!-- <div class="stats-cards" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="stat-card income">
            <div class="stat-label">总收入</div>
            <div class="stat-value">¥{{ formatMoney(statistics.totalIncome) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card expense">
            <div class="stat-label">总支出</div>
            <div class="stat-value">¥{{ formatMoney(statistics.totalExpense) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card profit">
            <div class="stat-label">净收益</div>
            <div class="stat-value">¥{{ formatMoney(statistics.totalProfit) }}</div>
          </div>
        </el-col>
      </el-row>
    </div> -->

    <!-- 表格卡片 -->
    <div class="card">
      <el-table :data="data.tableData" stripe style="width: 100%" :border="false">
        <el-table-column prop="transactionNo" label="流水号" width="180" />

        <el-table-column prop="userName" label="用户" width="120" />

        <el-table-column label="交易类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120">
          <template #default="scope">
            <span :class="scope.row.amount > 0 ? 'income-amount' : 'expense-amount'">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ formatMoney(scope.row.amount) }}
            </span>
          </template>
        </el-table-column>

        <!-- <el-table-column prop="balance" label="余额" width="120">
          <template #default="scope"> ¥{{ formatMoney(scope.row.balance) }} </template>
        </el-table-column> -->

        <el-table-column prop="relatedNo" label="关联单号" width="180" />

        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />

        <el-table-column prop="createTime" label="交易时间" width="160" />
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card" style="display: flex; justify-content: flex-end; margin-top: 5px">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :page-sizes="[10, 20, 50]"
        @size-change="load"
        @current-change="load"
        :total="data.total" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { financeApi } from '@/utils/api'

const dateRange = ref([])
const searchForm = reactive({
  keyword: '',
  type: '',
  startDate: '',
  endDate: ''
})

const statistics = reactive({
  totalIncome: 0,
  totalExpense: 0,
  totalProfit: 0
})

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: []
})

// 格式化金额
const formatMoney = (value) => {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

// 获取类型标签样式
const getTypeTag = (type) => {
  const map = {
    recharge: 'success',
    payment: 'danger',
    refund: 'warning',
    deposit_freeze: 'info',
    deposit_unfreeze: 'info',
    deposit_deduct: 'danger'
  }
  return map[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const map = {
    recharge: '充值',
    payment: '支付',
    refund: '退款',
    deposit_freeze: '押金冻结',
    deposit_unfreeze: '押金解冻',
    deposit_deduct: '押金扣除'
  }
  return map[type] || type
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const params = {
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    }
    const res = await financeApi.getStatistics(params)
    if (res.code === '200') {
      statistics.totalIncome = res.data.totalIncome || 0
      statistics.totalExpense = Math.abs(res.data.totalExpense || 0)
      statistics.totalProfit = res.data.totalProfit || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载流水列表
const load = async () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = dateRange.value[0]
    searchForm.endDate = dateRange.value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }

  try {
    const params = {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      keyword: searchForm.keyword,
      type: searchForm.type,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    }
    const res = await financeApi.getTransactions(params)
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载流水列表失败:', error)
    ElMessage.error('加载失败')
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  data.pageNum = 1
  load()
  loadStatistics()
}

onMounted(() => {
  load()
  loadStatistics()
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
  text-align: center;
}

.stat-card.income .stat-value {
  color: #67c23a;
}

.stat-card.expense .stat-value {
  color: #f56c6c;
}

.stat-card.profit .stat-value {
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.income-amount {
  color: #67c23a;
  font-weight: 600;
}

.expense-amount {
  color: #f56c6c;
  font-weight: 600;
}
</style>
