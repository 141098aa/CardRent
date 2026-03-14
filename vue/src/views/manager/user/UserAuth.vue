<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-select
        v-model="data.authType"
        placeholder="认证类型"
        style="width: 150px; margin-right: 10px"
        clearable
        @change="load">
        <el-option label="全部" value="all" />
        <el-option label="实名认证" value="real_name" />
        <el-option label="驾驶证认证" value="driver_license" />
      </el-select>

      <el-select
        v-model="data.status"
        placeholder="审核状态"
        style="width: 150px; margin-right: 10px"
        clearable
        @change="load">
        <el-option label="待审核" :value="0" />
        <el-option label="已认证" :value="1" />
        <el-option label="审核失败" :value="2" />
      </el-select>

      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset" plain>重置</el-button>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <div style="margin-bottom: 16px">
        <el-button
          type="success"
          size="small"
          :disabled="!selectedIds || selectedIds.length === 0"
          @click="batchApprove">
          批量通过
        </el-button>
        <el-button type="danger" size="small" :disabled="!selectedIds || selectedIds.length === 0" @click="batchReject">
          批量拒绝
        </el-button>
      </div>

      <el-table
        :data="data.tableData"
        stripe
        style="width: 100%"
        :border="false"
        @selection-change="handleSelectionChange"
        row-key="uniqueId">
        <el-table-column type="selection" width="55" :reserve-selection="true" :selectable="isSelectable" />

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="认证类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.authType === 'real_name' ? 'primary' : 'success'" size="small">
              {{ scope.row.authType === 'real_name' ? '实名认证' : '驾驶证认证' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="userRealName" label="用户姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />

        <el-table-column prop="authName" label="认证姓名" min-width="100" />
        <el-table-column prop="authNumber" label="证件号码" min-width="180" />

        <el-table-column label="证件照片" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewImages(scope.row)">查看照片</el-button>
          </template>
        </el-table-column>

        <el-table-column v-if="data.authType === 'driver_license'" prop="vehicleType" label="准驾车型" width="100" />
        <el-table-column v-if="data.authType === 'driver_license'" label="有效期" width="200">
          <template #default="scope"> {{ scope.row.validStart }} 至 {{ scope.row.validEnd }} </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column prop="auditTime" label="审核时间" min-width="160" />
        <el-table-column prop="auditRemark" label="审核备注" min-width="150" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" type="success" size="small" @click="handleApprove(scope.row)">
              通过
            </el-button>
            <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="handleReject(scope.row)">
              拒绝
            </el-button>
            <el-button v-if="scope.row.status !== 0" type="primary" size="small" @click="viewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页卡片 -->
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
      v-model="data.auditDialogVisible"
      :title="data.auditAction === 'approve' ? '通过审核' : '拒绝审核'"
      width="450px"
      :close-on-click-modal="false">
      <el-form :model="data.auditForm" label-width="80px">
        <el-form-item label="审核备注" prop="remark">
          <el-input v-model="data.auditForm.remark" type="textarea" :rows="3" placeholder="请输入审核备注（可选）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.auditDialogVisible = false">取 消</el-button>
          <el-button
            :type="data.auditAction === 'approve' ? 'success' : 'danger'"
            @click="submitAudit"
            :loading="data.auditLoading">
            {{ data.auditAction === 'approve' ? '确 定 通 过' : '确 定 拒 绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看照片对话框 -->
    <el-dialog v-model="data.imageDialogVisible" :title="data.dialogTitle" width="700px">
      <div class="image-container" v-if="data.currentAuth">
        <!-- 根据认证类型显示不同的标题 -->
        <template v-if="data.currentAuth.authType === 'real_name'">
          <!-- 实名认证：身份证正反面 -->
          <div class="image-item">
            <div class="image-label">身份证正面</div>
            <div class="image-wrapper">
              <el-image
                :src="data.currentAuth.frontImage"
                fit="contain"
                class="auth-image"
                :preview-src-list="[data.currentAuth.frontImage, data.currentAuth.backImage]">
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>暂无照片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
          <div class="image-item">
            <div class="image-label">身份证背面</div>
            <div class="image-wrapper">
              <el-image
                :src="data.currentAuth.backImage"
                fit="contain"
                class="auth-image"
                :preview-src-list="[data.currentAuth.frontImage, data.currentAuth.backImage]">
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>暂无照片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </template>

        <template v-else-if="data.currentAuth.authType === 'driver_license'">
          <!-- 驾驶证认证：驾驶证正本和副页 -->
          <div class="image-item">
            <div class="image-label">驾驶证正本</div>
            <div class="image-wrapper">
              <el-image
                :src="data.currentAuth.frontImage"
                fit="contain"
                class="auth-image"
                :preview-src-list="[data.currentAuth.frontImage, data.currentAuth.backImage]">
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>暂无照片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
          <div class="image-item">
            <div class="image-label">驾驶证副页</div>
            <div class="image-wrapper">
              <el-image
                :src="data.currentAuth.backImage"
                fit="contain"
                class="auth-image"
                :preview-src-list="[data.currentAuth.frontImage, data.currentAuth.backImage]">
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>暂无照片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </template>
      </div>
    </el-dialog>

    <!-- 详情/编辑对话框 -->
    <el-dialog
      v-model="data.detailDialogVisible"
      :title="data.editMode ? '编辑认证' : '认证详情'"
      width="700px"
      :close-on-click-modal="false">
      <div v-if="data.currentDetail" class="detail-container">
        <!-- 查看模式 -->
        <template v-if="!data.editMode">
          <!-- 基本信息 -->
          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item label="认证类型">
              <el-tag :type="data.currentDetail.authType === 'real_name' ? 'primary' : 'success'">
                {{ data.currentDetail.authType === 'real_name' ? '实名认证' : '驾驶证认证' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(data.currentDetail.status)" effect="light">
                {{ getStatusText(data.currentDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户账号">{{ data.currentDetail.username }}</el-descriptions-item>
            <el-descriptions-item label="用户姓名">{{ data.currentDetail.userRealName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ data.currentDetail.phone }}</el-descriptions-item>
            <el-descriptions-item label="认证姓名">{{ data.currentDetail.authName }}</el-descriptions-item>
            <el-descriptions-item label="证件号码">{{ data.currentDetail.authNumber }}</el-descriptions-item>

            <!-- 驾驶证特有字段 -->
            <template v-if="data.currentDetail.authType === 'driver_license'">
              <el-descriptions-item label="准驾车型">{{ data.currentDetail.vehicleType }}</el-descriptions-item>
              <el-descriptions-item label="有效期"
                >{{ data.currentDetail.validStart }} 至 {{ data.currentDetail.validEnd }}</el-descriptions-item
              >
            </template>

            <el-descriptions-item label="申请时间">{{ data.currentDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ data.currentDetail.auditTime || '未审核' }}</el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2">{{
              data.currentDetail.auditRemark || '无'
            }}</el-descriptions-item>
          </el-descriptions>

          <!-- 证件照片 -->
          <div class="detail-images">
            <div class="detail-image-item">
              <div class="image-label">
                {{ data.currentDetail.authType === 'real_name' ? '身份证正面' : '驾驶证正本' }}
              </div>
              <el-image
                :src="data.currentDetail.frontImage"
                fit="contain"
                class="detail-image"
                :preview-src-list="[data.currentDetail.frontImage, data.currentDetail.backImage]">
                <template #error>
                  <div class="image-error">暂无照片</div>
                </template>
              </el-image>
            </div>
            <div class="detail-image-item">
              <div class="image-label">
                {{ data.currentDetail.authType === 'real_name' ? '身份证背面' : '驾驶证副页' }}
              </div>
              <el-image
                :src="data.currentDetail.backImage"
                fit="contain"
                class="detail-image"
                :preview-src-list="[data.currentDetail.frontImage, data.currentDetail.backImage]">
                <template #error>
                  <div class="image-error">暂无照片</div>
                </template>
              </el-image>
            </div>
          </div>

          <!-- 查看模式下的操作按钮 -->
          <div class="detail-actions">
            <el-button type="primary" @click="enterEditMode">重新审核</el-button>
            <el-button @click="data.detailDialogVisible = false">关 闭</el-button>
          </div>
        </template>

        <!-- 编辑模式 -->
        <template v-else>
          <el-form :model="data.editForm" label-width="100px">
            <el-form-item label="认证类型">
              <el-tag :type="data.editForm.authType === 'real_name' ? 'primary' : 'success'">
                {{ data.editForm.authType === 'real_name' ? '实名认证' : '驾驶证认证' }}
              </el-tag>
            </el-form-item>

            <el-form-item label="审核状态" required>
              <el-radio-group v-model="data.editForm.status">
                <el-radio :value="0" disabled>待审核</el-radio>
                <el-radio :value="1">已认证</el-radio>
                <el-radio :value="2">审核失败</el-radio>
              </el-radio-group>
              <div class="form-tip">注：待审核状态不可手动设置，只能通过通过/拒绝操作改变</div>
            </el-form-item>

            <el-form-item label="审核备注">
              <el-input v-model="data.editForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注" />
            </el-form-item>
          </el-form>

          <!-- 编辑模式下的操作按钮 -->
          <div class="detail-actions">
            <el-button type="primary" @click="submitEdit" :loading="data.auditLoading">确 定</el-button>
            <el-button @click="cancelEdit">取 消</el-button>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { authApi } from '@/utils/api'

// 使用 ref 定义 selectedIds
const selectedIds = ref([])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  authType: 'all',
  status: null,
  tableData: [],

  auditDialogVisible: false,
  auditLoading: false,
  auditAction: 'approve', // approve 或 reject
  auditForm: {
    id: null,
    authType: null,
    remark: ''
  },

  imageDialogVisible: false,
  currentAuth: null,
  dialogTitle: '', // 对话框标题

  // 详情对话框相关字段
  detailDialogVisible: false, // 详情对话框显示控制
  currentDetail: null, // 当前查看的详情数据
  editMode: false, // 是否为编辑模式
  editForm: {
    // 编辑表单
    id: null,
    authType: null,
    status: null,
    auditRemark: ''
  }
})

// 加载数据
const load = () => {
  authApi
    .selectPage({
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: data.status,
      authType: data.authType
    })
    .then((res) => {
      if (res.code === '200') {
        // 为每行数据添加唯一键
        const list = (res.data.list || []).map((item) => ({
          ...item,
          uniqueId: `${item.authType}_${item.id}` // 生成唯一键
        }))

        data.tableData = list
        data.total = res.data.total || 0
      }
    })
}

// 重置
const reset = () => {
  data.authType = 'all'
  data.status = null
  data.pageNum = 1
  load()
}
// 判断行是否可被选中（只有待审核的记录才能被选中）
const isSelectable = (row) => {
  return row.status === 0 // 只有 status=0（待审核）的记录可选
}

// 添加计算属性或方法设置对话框标题
const getDialogTitle = (authType) => {
  return authType === 'real_name' ? '身份证照片' : '驾驶证照片'
}

// 在打开对话框时设置标题
const viewImages = (row) => {
  data.currentAuth = row
  data.dialogTitle = getDialogTitle(row.authType)
  data.imageDialogVisible = true
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => ({
    id: item.id, // 使用原始ID
    authType: item.authType
  }))
}

// 获取状态样式
const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已认证',
    2: '审核失败'
  }
  return map[status] || '未知'
}

// 单个通过
const handleApprove = (row) => {
  data.auditAction = 'approve'
  data.auditForm.id = row.id
  data.auditForm.authType = row.authType
  data.auditForm.remark = ''
  data.auditDialogVisible = true
}

// 单个拒绝
const handleReject = (row) => {
  data.auditAction = 'reject'
  data.auditForm.id = row.id
  data.auditForm.authType = row.authType
  data.auditForm.remark = ''
  data.auditDialogVisible = true
}

// 批量通过
const batchApprove = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审核的记录')
    return
  }

  ElMessageBox.confirm(`确定要通过选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      batchAudit(1, '')
    })
    .catch(() => {})
}

// 批量拒绝
const batchReject = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要审核的记录')
    return
  }

  ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入拒绝原因'
  })
    .then(({ value }) => {
      if (!value) {
        ElMessage.warning('请输入拒绝原因')
        return
      }
      batchAudit(2, value)
    })
    .catch(() => {})
}

// 批量审核
const batchAudit = (status, remark) => {
  const authType = selectedIds.value[0]?.authType
  const sameType = selectedIds.value.every((item) => item.authType === authType)

  if (!sameType) {
    ElMessage.warning('不能同时审核不同类型的认证')
    return
  }

  authApi
    .batchAudit({
      ids: selectedIds.value.map((item) => item.id),
      authType: authType,
      status: status,
      remark: remark
    })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('审核完成')
        selectedIds.value = []
        load()
      }
    })
}

// 提交审核
const submitAudit = () => {
  if (!data.auditForm.id) return

  const status = data.auditAction === 'approve' ? 1 : 2
  const apiCall = data.auditForm.authType === 'real_name' ? authApi.auditRealName : authApi.auditDriverLicense

  data.auditLoading = true
  apiCall({
    id: data.auditForm.id,
    status: status,
    remark: data.auditForm.remark
  })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('审核完成')
        data.auditDialogVisible = false
        load()
      }
    })
    .finally(() => {
      data.auditLoading = false
    })
}

// 查看详情
const viewDetail = (row) => {
  data.currentDetail = row
  data.editMode = false // 默认为查看模式
  data.detailDialogVisible = true
}

// 进入编辑模式
const enterEditMode = () => {
  data.editMode = true
  // 初始化编辑表单
  data.editForm = {
    id: data.currentDetail.id,
    authType: data.currentDetail.authType,
    status: data.currentDetail.status,
    auditRemark: data.currentDetail.auditRemark || ''
  }
}

// 取消编辑
const cancelEdit = () => {
  data.editMode = false
  data.editForm = {
    id: null,
    authType: null,
    status: null,
    auditRemark: ''
  }
}

// 提交修改
const submitEdit = () => {
  if (!data.editForm.id) return

  const apiCall = data.editForm.authType === 'real_name' ? authApi.auditRealName : authApi.auditDriverLicense

  data.auditLoading = true
  apiCall({
    id: data.editForm.id,
    status: data.editForm.status,
    remark: data.editForm.auditRemark
  })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('修改成功')
        data.detailDialogVisible = false
        data.editMode = false
        load() // 重新加载列表
      }
    })
    .finally(() => {
      data.auditLoading = false
    })
}

// 初始化加载
load()
</script>

<style scoped>
.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);
  border: 1px solid #edf2f7;
}

.image-container {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.image-item {
  flex: 1;
  text-align: center;
}

.image-label {
  margin-bottom: 10px;
  font-weight: 500;
  color: #334155;
}
.image-wrapper {
  width: 100%;
  height: 250px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  height: 100%;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.image-error span {
  font-size: 14px;
}

.auth-image {
  width: 100%;
  height: 250px;
  border-radius: 8px;
  border: 1px solid #edf2f7;
  cursor: pointer;
}
.detail-container {
  padding: 10px 0;
}

.detail-descriptions {
  margin-bottom: 20px;
}

.detail-images {
  display: flex;
  gap: 20px;
  margin: 20px 0;
}

.detail-image-item {
  flex: 1;
  text-align: center;
}

.image-label {
  margin-bottom: 10px;
  font-weight: 500;
  color: #334155;
}

.detail-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  border: 1px solid #edf2f7;
  cursor: pointer;
}

.image-error {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.detail-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #edf2f7;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  background-color: #f8fafc;
  margin-right: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #edf2f7;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

:deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.el-dialog__footer) {
  padding: 16px 20px;
  border-top: 1px solid #edf2f7;
  background-color: #f8fafc;
}
:deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f8fafc;
  color: #1e2a3a;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid #e9ecef;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f0f2f5;
}
</style>
