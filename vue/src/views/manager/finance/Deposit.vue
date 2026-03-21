<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/订单号"
            :prefix-icon="Search"
            clearable
            @keyup.enter="load" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="押金状态" clearable @change="load">
            <el-option label="全部" value="" />
            <el-option label="已冻结" value="frozen" />
            <el-option label="已解冻" value="unfrozen" />
            <el-option label="已扣除" value="deducted" />
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
    <div class="stats-cards" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="stat-card frozen">
            <div class="stat-label">冻结押金总额</div>
            <div class="stat-value">¥{{ formatMoney(depositStats.totalFrozen) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card unfrozen">
            <div class="stat-label">已解冻押金总额</div>
            <div class="stat-value">¥{{ formatMoney(depositStats.totalUnfrozen) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card deducted">
            <div class="stat-label">已扣除押金总额</div>
            <div class="stat-value">¥{{ formatMoney(depositStats.totalDeducted) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <el-table
        :data="data.tableData"
        stripe
        style="width: 100%"
        :border="false">
        
        <el-table-column prop="orderNo" label="订单号" width="180" />
        
        <el-table-column prop="userName" label="用户" width="120" />
        
        <el-table-column prop="amount" label="押金金额" width="120">
          <template #default="scope">
            <span style="color: #409eff; font-weight: 600">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="deductAmount" label="扣除金额" width="120">
          <template #default="scope">
            <span v-if="scope.row.status === 'deducted'" style="color: #f56c6c">
              ¥{{ scope.row.deductAmount || 0 }}
            </span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="deductReason" label="扣除原因" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="createTime" label="创建时间" width="160" />
        
        <el-table-column prop="unfrozenTime" label="解冻时间" width="160" />
        
        <el-table-column prop="deductTime" label="扣除时间" width="160" />
        
        <el-table-column label="操作" align="center" width="140" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'frozen'">
              <el-button type="danger" size="small" @click="openDeductDialog(scope.row)">
                扣除押金
              </el-button>
            </template>
            <el-button v-else size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
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

    <!-- 扣除押金对话框 -->
    <el-dialog
      v-model="deductVisible"
      title="扣除押金"
      width="450px"
      :close-on-click-modal="false">
      
      <el-form :model="deductForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ currentDeposit?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="用户">
          <span>{{ currentDeposit?.userName }}</span>
        </el-form-item>
        <el-form-item label="押金金额">
          <span style="color: #409eff; font-weight: 600">¥{{ currentDeposit?.amount }}</span>
        </el-form-item>
        <el-form-item label="扣除金额" required>
          <el-input-number 
            v-model="deductForm.amount" 
            :min="0" 
            :max="currentDeposit?.amount"
            :precision="2"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="扣除原因" required>
          <el-input 
            v-model="deductForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入扣除原因，如：违章罚款、车辆损坏等" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deductVisible = false">取 消</el-button>
          <el-button type="danger" @click="submitDeduct" :loading="deductLoading">确 定 扣 除</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="押金详情" width="500px">
      <div v-if="currentDeposit" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentDeposit.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentDeposit.userName }}</el-descriptions-item>
          <el-descriptions-item label="押金金额">¥{{ currentDeposit.amount }}</el-descriptions-item>
          <el-descriptions-item label="扣除金额">¥{{ currentDeposit.deductAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="扣除原因">{{ currentDeposit.deductReason || '--' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentDeposit.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDeposit.createTime }}</el-descriptions-item>
          <el-descriptions-item label="解冻时间">{{ currentDeposit.unfrozenTime || '--' }}</el-descriptions-item>
          <el-descriptions-item label="扣除时间">{{ currentDeposit.deductTime || '--' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { financeApi } from '@/utils/api'

const dateRange = ref([])
const deductVisible = ref(false)
const deductLoading = ref(false)
const detailVisible = ref(false)
const currentDeposit = ref(null)

const deductForm = reactive({
  amount: 0,
  reason: ''
})

const searchForm = reactive({
  keyword: '',
  status: '',
  startDate: '',
  endDate: ''
})

const depositStats = reactive({
  totalFrozen: 0,
  totalUnfrozen: 0,
  totalDeducted: 0
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

// 获取状态标签样式
const getStatusTag = (status) => {
  const map = {
    frozen: 'warning',
    unfrozen: 'success',
    deducted: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    frozen: '已冻结',
    unfrozen: '已解冻',
    deducted: '已扣除'
  }
  return map[status] || status
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

// 加载押金列表
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
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    }
    const res = await financeApi.getDeposits(params)
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载押金列表失败:', error)
    ElMessage.error('加载失败')
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  data.pageNum = 1
  load()
  loadDepositStats()
}

// 打开扣除对话框
const openDeductDialog = (row) => {
  currentDeposit.value = row
  deductForm.amount = 0
  deductForm.reason = ''
  deductVisible.value = true
}

// 提交扣除
const submitDeduct = async () => {
  if (deductForm.amount <= 0) {
    ElMessage.warning('请输入扣除金额')
    return
  }
  if (deductForm.amount > currentDeposit.value.amount) {
    ElMessage.warning('扣除金额不能超过押金金额')
    return
  }
  if (!deductForm.reason) {
    ElMessage.warning('请输入扣除原因')
    return
  }

  ElMessageBox.confirm('确认扣除押金吗？此操作不可撤销，将通知用户并生成流水记录。', '提示', {
    type: 'warning',
    confirmButtonText: '确认扣除'
  }).then(async () => {
    deductLoading.value = true
    try {
      const res = await financeApi.deductDeposit({
        depositId: currentDeposit.value.id,
        deductAmount: deductForm.amount,
        reason: deductForm.reason
      })
      if (res.code === '200') {
        ElMessage.success('押金扣除成功')
        deductVisible.value = false
        load()
        loadDepositStats()
      }
    } catch (error) {
      console.error('扣除押金失败:', error)
      ElMessage.error('扣除失败')
    } finally {
      deductLoading.value = false
    }
  }).catch(() => {})
}

// 查看详情
const viewDetail = (row) => {
  currentDeposit.value = row
  detailVisible.value = true
}

onMounted(() => {
  load()
  loadDepositStats()
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

.stat-card.frozen .stat-value {
  color: #e6a23c;
}

.stat-card.unfrozen .stat-value {
  color: #67c23a;
}

.stat-card.deducted .stat-value {
  color: #f56c6c;
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

.detail-content {
  padding: 0 10px;
}
</style>