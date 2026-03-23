<template>
  <div class="admin-scope">
    <!-- 统计卡片 -->
    <div class="stats-card" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-item" @click="switchStatus('all')">
            <div class="stat-label">全部订单</div>
            <div class="stat-value">{{ stats.all || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" @click="switchStatus('pending')">
            <div class="stat-label">待处理</div>
            <div class="stat-value warning">{{ stats.pending || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" @click="switchStatus('active')">
            <div class="stat-label">进行中</div>
            <div class="stat-value primary">{{ stats.active || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item" @click="switchStatus('completed')">
            <div class="stat-label">已完成</div>
            <div class="stat-value success">{{ stats.completed || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-row :gutter="10">
        <el-col :span="5">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单号/用户/手机号/车型"
            :prefix-icon="Search"
            clearable
            @keyup.enter="load" />
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.statusGroup" placeholder="订单状态" clearable @change="load">
            <el-option label="全部订单" value="all" />
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :span="7">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
            @change="load" />
        </el-col>
        <el-col :span="9">
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch" plain>重置</el-button>
          <el-button
            @click="loadStats"
            :loading="loadingStats"
            :icon="loadingStats ? 'Loading' : 'Refresh'"
            style="margin-left: 50px">
            {{ loadingStats ? '刷新中...' : '刷新统计' }}
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <el-table :data="data.tableData" stripe style="width: 100%" :border="false">
        <el-table-column prop="orderNo" label="订单号" width="150" />

        <el-table-column label="用户信息" width="150">
          <template #default="scope">
            <div>{{ scope.row.userName }}</div>
            <div style="font-size: 12px; color: #64748b">{{ scope.row.userPhone }}</div>
          </template>
        </el-table-column>

        <el-table-column label="车辆" min-width="160">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-image
                :src="scope.row.carImage"
                style="width: 50px; height: 35px; border-radius: 4px; object-fit: cover; margin-right: 8px"
                :preview-src-list="[scope.row.carImage]" />
              <div>{{ scope.row.carName }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="租期" width="160">
          <template #default="scope">
            <div>{{ scope.row.pickupTime }} 至</div>
            <div>{{ scope.row.returnTime }} ({{ scope.row.days }}天)</div>
          </template>
        </el-table-column>

        <el-table-column prop="totalPrice" label="金额" width="100">
          <template #default="scope">
            <span style="color: #409eff; font-weight: 600">¥{{ scope.row.totalPrice }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="下单时间" width="160" />

        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>

            <!-- 待审核订单 -->
            <template v-if="scope.row.status === 'pending_audit'">
              <el-button type="success" size="small" @click="handleAudit(scope.row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleAudit(scope.row, 'reject')">拒绝</el-button>
            </template>

            <!-- 待取车订单 -->
            <template v-else-if="scope.row.status === 'pending_pickup'">
              <el-button type="primary" size="small" @click="handlePickup(scope.row)">确认取车</el-button>
            </template>

            <!-- 进行中订单 -->
            <template v-else-if="scope.row.status === 'active' || scope.row.status === 'pending_return'">
              <el-button type="success" size="small" @click="handleReturn(scope.row)"> 确认还车 </el-button>
            </template>

            <!-- 可取消订单 -->
            <template v-if="['pending_pay', 'pending_audit', 'pending_pickup'].includes(scope.row.status)">
              <el-button type="danger" size="small" @click="handleCancel(scope.row)">取消</el-button>
            </template>
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

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)" size="small">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户姓名">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="车辆">{{ currentOrder.carName }}</el-descriptions-item>
          <el-descriptions-item label="日租金">¥{{ currentOrder.dailyPrice }}</el-descriptions-item>
          <el-descriptions-item label="取车时间">{{ currentOrder.pickupTime }}</el-descriptions-item>
          <el-descriptions-item label="还车时间">{{ currentOrder.returnTime }}</el-descriptions-item>
          <el-descriptions-item label="租车天数">{{ currentOrder.days }}天</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalPrice }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '--' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.paymentTime || '--' }}</el-descriptions-item>
          <el-descriptions-item label="取车地点">{{ currentOrder.pickupLocation }}</el-descriptions-item>
          <el-descriptions-item label="还车地点">{{ currentOrder.returnLocation }}</el-descriptions-item>
          <el-descriptions-item label="用户备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">{{ currentOrder.auditRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{
            currentOrder.cancelReason || '无'
          }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditAction === 'approve' ? '通过审核' : '拒绝审核'" width="400px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button
          :type="auditAction === 'approve' ? 'success' : 'danger'"
          @click="submitAudit"
          :loading="auditLoading">
          {{ auditAction === 'approve' ? '确 定 通 过' : '确 定 拒 绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { orderApi } from '@/utils/api'

const loadingStats = ref(false)
const detailVisible = ref(false)
const currentOrder = ref(null)
const auditVisible = ref(false)
const auditAction = ref('approve')
const auditLoading = ref(false)
const auditForm = reactive({
  id: null,
  remark: ''
})

const dateRange = ref([])

const stats = ref({
  all: 0,
  pending: 0,
  active: 0,
  completed: 0
})

const searchForm = reactive({
  keyword: '',
  statusGroup: '',
  startDate: '',
  endDate: ''
})

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: []
})

// 加载统计
const loadStats = () => {
  loadingStats.value = true
  orderApi
    .getStats()
    .then((res) => {
      if (res.code === '200') {
        // 比较新旧数据，看是否有变化
        const oldStats = JSON.stringify(stats.value)
        const newStats = JSON.stringify(res.data)

        stats.value = res.data

        if (oldStats !== newStats) {
          ElMessage.success('统计已更新')
        } else {
          ElMessage.info('统计暂无变化')
        }
      } else {
        // 处理非200的响应码
        ElMessage.error(res.msg || '获取统计失败')
      }
    })
    .catch((error) => {
      // 处理网络错误或异常
      console.error('加载统计失败:', error)
      ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
    })
    .finally(() => {
      loadingStats.value = false
    })
}

// 加载列表
const load = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = dateRange.value[0]
    searchForm.endDate = dateRange.value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }

  orderApi
    .list({
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      keyword: searchForm.keyword,
      statusGroup: searchForm.statusGroup,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    })
    .then((res) => {
      if (res.code === '200') {
        data.tableData = res.data.list || []
        data.total = res.data.total || 0
      } else {
        ElMessage.error(res.msg || '加载订单列表失败')
      }
    })
    .catch((error) => {
      console.error('加载订单列表失败:', error)
      ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
    })
}
// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending_pay: '待付款',
    pending_audit: '待审核',
    pending_pickup: '待取车',
    active: '进行中',
    pending_return: '待确认还车', // 添加这个
    completed: '已完成',
    cancelled: '已取消',
    refunding: '退款中',
    refunded: '已退款'
  }
  return map[status] || status
}
// 获取状态样式
const getStatusType = (status) => {
  const map = {
    pending_pay: 'warning',
    pending_audit: 'warning',
    pending_pickup: 'primary',
    active: 'primary',
    pending_return: 'info',
    completed: 'success',
    cancelled: 'info',
    refunding: 'danger',
    refunded: 'info'
  }
  return map[status] || 'info'
}
// 切换状态
const switchStatus = (status) => {
  const statusMap = {
    all: 'all',
    pending: 'pending', // 待处理：pending_pay + pending_audit
    active: 'active', // 进行中：pending_pickup + active + pending_return
    completed: 'completed' // 已完成：completed + cancelled
  }
  searchForm.statusGroup = statusMap[status] || status
  data.pageNum = 1
  load()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.statusGroup = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  data.pageNum = 1
  load()
}
// 禁用过去的日期
const disabledDate = (time) => {
  // 获取今天的开始时间（00:00:00）
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // 返回 true 表示禁用，false 表示可选
  return time.getTime() < today.getTime()
}

// 查看详情
const viewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

// 审核
const handleAudit = (row, action) => {
  auditAction.value = action
  auditForm.id = row.id
  auditForm.remark = ''
  auditVisible.value = true
}

// 提交审核
const submitAudit = () => {
  auditLoading.value = true
  orderApi
    .audit({
      id: auditForm.id,
      action: auditAction.value,
      remark: auditForm.remark
    })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('审核完成')
        auditVisible.value = false
        load()
        loadStats()
      } else {
        ElMessage.error(res.msg || '审核失败')
        auditVisible.value = false // 失败时也关闭对话框
      }
    })
    .catch((error) => {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
      auditVisible.value = false
    })
    .finally(() => {
      auditLoading.value = false
    })
}

// 确认取车
const handlePickup = (row) => {
  ElMessageBox.confirm('确认用户已取到车辆？', '提示', {
    type: 'info',
    confirmButtonText: '确认取车'
  })
    .then(() => {
      orderApi
        .confirmPickup({ id: row.id })
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success('取车确认成功')
            load()
            loadStats()
          } else {
            ElMessage.error(res.msg || '取车确认失败')
          }
        })
        .catch((error) => {
          console.error('取车确认失败:', error)
          ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
        })
    })
    .catch(() => {})
}

// 确认还车
const handleReturn = (row) => {
  // 根据状态显示不同的确认文案
  const confirmText = row.status === 'pending_return' ? '确认用户已归还车辆？' : '确认用户已归还车辆？'

  ElMessageBox.confirm(confirmText, '提示', {
    type: 'info',
    confirmButtonText: '确认还车'
  })
    .then(() => {
      orderApi
        .confirmReturn({ id: row.id })
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success('还车确认成功')
            load()
            loadStats()
          } else {
            ElMessage.error(res.msg || '还车确认失败')
          }
        })
        .catch((error) => {
          console.error('还车确认失败:', error)
          ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
        })
    })
    .catch(() => {})
}

// 取消订单
const handleCancel = (row) => {
  ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想',
    inputPlaceholder: '请输入取消原因'
  })
    .then(({ value }) => {
      if (!value) {
        ElMessage.warning('请输入取消原因')
        return
      }
      orderApi
        .cancel({ id: row.id, reason: value })
        .then((res) => {
          if (res.code === '200') {
            ElMessage.success('订单已取消')
            load()
            loadStats()
          } else {
            ElMessage.error(res.msg || '取消订单失败')
          }
        })
        .catch((error) => {
          console.error('取消订单失败:', error)
          ElMessage.error(error.response?.data?.msg || '网络错误，请稍后重试')
        })
    })
    .catch(() => {})
}

onMounted(() => {
  loadStats()
  load()
})
</script>

<style scoped>
.stats-card {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  border: 1px solid #edf2f7;
}

.stat-item {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background-color: #f8fafc;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1e2a3a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.order-detail {
  padding: 0 10px;
}
</style>
