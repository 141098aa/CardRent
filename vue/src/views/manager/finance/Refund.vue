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
          <el-select v-model="searchForm.status" placeholder="退款状态" clearable @change="load">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已完成" value="completed" />
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

    <!-- 操作栏 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge">
            <span>待审核退款申请</span>
          </el-badge>
        </div>
        <div>
          <el-button type="success" size="small" :disabled="!selectedIds.length" @click="batchAudit('approve')">
            批量通过
          </el-button>
          <el-button type="danger" size="small" :disabled="!selectedIds.length" @click="batchAudit('reject')">
            批量拒绝
          </el-button>
        </div>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <el-table
        :data="data.tableData"
        stripe
        style="width: 100%"
        :border="false"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />

        <el-table-column prop="refundNo" label="退款单号" width="180" />

        <el-table-column prop="orderNo" label="订单号" width="180" />

        <el-table-column prop="userName" label="用户" width="120" />

        <el-table-column prop="amount" label="退款金额" width="120">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: 600">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="退款原因" min-width="180" show-overflow-tooltip />

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="160" />

        <el-table-column prop="auditTime" label="审核时间" width="160" />

        <el-table-column prop="auditRemark" label="审核备注" min-width="150" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending'">
              <el-button type="success" size="small" @click="handleAudit(scope.row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleAudit(scope.row, 'reject')">拒绝</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditVisible"
      :title="auditAction === 'approve' ? '通过退款申请' : '拒绝退款申请'"
      width="450px"
      :close-on-click-modal="false">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditVisible = false">取 消</el-button>
          <el-button
            :type="auditAction === 'approve' ? 'success' : 'danger'"
            @click="submitAudit"
            :loading="auditLoading">
            {{ auditAction === 'approve' ? '确 定 通 过' : '确 定 拒 绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="退款详情" width="500px">
      <div v-if="currentRefund" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentRefund.userName }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ currentRefund.amount }}</el-descriptions-item>
          <el-descriptions-item label="退款原因">{{ currentRefund.reason }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentRefund.createTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentRefund.auditTime || '--' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ currentRefund.auditRemark || '--' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentRefund.completeTime || '--' }}</el-descriptions-item>
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
const selectedIds = ref([])
const auditVisible = ref(false)
const auditLoading = ref(false)
const detailVisible = ref(false)
const auditAction = ref('approve')
const pendingCount = ref(0)

const auditForm = reactive({
  id: null,
  remark: ''
})

const currentRefund = ref(null)

const searchForm = reactive({
  keyword: '',
  status: '',
  startDate: '',
  endDate: ''
})

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: []
})

// 获取状态标签样式
const getStatusTag = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return map[status] || status
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const res = await financeApi.getPendingRefundCount()
    if (res.code === '200') {
      pendingCount.value = res.data
    }
  } catch (error) {
    console.error('加载待审核数量失败:', error)
  }
}

// 加载退款列表
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
    const res = await financeApi.getRefunds(params)
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载退款列表失败:', error)
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
  loadPendingCount()
}

// 单个审核
const handleAudit = (row, action) => {
  auditAction.value = action
  auditForm.id = row.id
  auditForm.remark = ''
  auditVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  auditLoading.value = true
  try {
    const res = await financeApi.auditRefund({
      id: auditForm.id,
      action: auditAction.value,
      remark: auditForm.remark
    })
    if (res.code === '200') {
      ElMessage.success(auditAction.value === 'approve' ? '审核通过' : '已拒绝')
      auditVisible.value = false
      load()
      loadPendingCount()
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 批量审核
const batchAudit = async (action) => {
  if (!selectedIds.value.length) return

  ElMessageBox.confirm(
    `确定要${action === 'approve' ? '通过' : '拒绝'}选中的 ${selectedIds.value.length} 条退款申请吗？`,
    '提示',
    {
      type: 'warning',
      confirmButtonText: '确定'
    }
  )
    .then(async () => {
      try {
        const res = await financeApi.batchAuditRefunds({
          ids: selectedIds.value,
          action: action,
          remark: ''
        })
        if (res.code === '200') {
          ElMessage.success('批量审核完成')
          selectedIds.value = []
          load()
          loadPendingCount()
        }
      } catch (error) {
        console.error('批量审核失败:', error)
        ElMessage.error('批量审核失败')
      }
    })
    .catch(() => {})
}

// 查看详情
const viewDetail = (row) => {
  currentRefund.value = row
  detailVisible.value = true
}

onMounted(() => {
  load()
  loadPendingCount()
})
</script>

<style scoped>
.badge {
  margin-right: 20px;
}

.detail-content {
  padding: 0 10px;
}
</style>
