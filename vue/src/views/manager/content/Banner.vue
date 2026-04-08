<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="searchForm.keyword"
        placeholder="请输入标题或描述"
        style="width: 200px; margin-right: 10px"
        :prefix-icon="Search"
        clearable
        @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="resetSearch" plain>重置</el-button>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增轮播图
        </el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="batchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
      </div>

      <el-table
        :data="data.tableData"
        stripe
        style="width: 100%"
        :border="false"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="图片" width="100" align="center">
          <template #default="scope">
            <el-image
              :src="scope.row.image"
              style="width: 60px; height: 40px; border-radius: 4px; object-fit: cover"
              :preview-src-list="[scope.row.image]"
              fit="cover" />
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="150" />

        <el-table-column prop="description" label="副标题" min-width="150" show-overflow-tooltip />

        <el-table-column prop="keyword" label="关键词" width="100" />

        <el-table-column prop="link" label="跳转链接" min-width="150" show-overflow-tooltip />

        <el-table-column label="排序" min-width="120" align="center">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.sortOrder"
              :min="0"
              size="small"
              controls-position="right"
              style="width: 80px"
              @change="() => updateSort(scope.row)" />
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="150" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="() => updateStatus(scope.row)" />
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <div style="display: flex; gap: 6px; justify-content: center">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </div>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="副标题" prop="description">
          <el-input v-model="form.description" placeholder="请输入副标题" />
        </el-form-item>

        <el-form-item label="图片" prop="image" required>
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload">
            <div class="image-upload-wrapper">
              <img v-if="form.image" :src="form.image" class="uploaded-image" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </div>
          </el-upload>
          <div class="form-tip">建议尺寸：1920×500px，大小不超过5MB</div>
        </el-form-item>

        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="关联搜索关键词（可选）" />
        </el-form-item>

        <el-form-item label="跳转链接" prop="link">
          <el-input v-model="form.link" placeholder="点击后跳转的链接（可选）" />
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" placeholder="数字越小越靠前" style="width: 30%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="save" :loading="saving">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { bannerApi } from '@/utils/api'

const baseUrl = import.meta.env.VITE_BASE_URL
const uploadUrl = baseUrl + '/files/upload'

const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const saving = ref(false)
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: []
})

const form = reactive({
  id: null,
  title: '',
  description: '',
  image: '',
  keyword: '',
  link: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

// 加载列表
const load = () => {
  bannerApi
    .getBannerList({
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      keyword: searchForm.keyword
    })
    .then((res) => {
      if (res.code === '200') {
        data.tableData = res.data.list || []
        data.total = res.data.total || 0
      }
    })
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  data.pageNum = 1
  load()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

// 新增
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, {
    id: null,
    title: '',
    description: '',
    image: '',
    keyword: '',
    link: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 保存
const save = () => {
  formRef.value?.validate((valid) => {
    if (!valid) return

    saving.value = true
    const apiCall = dialogType.value === 'add' ? bannerApi.addBanner : bannerApi.updateBanner
    apiCall(form)
      .then((res) => {
        if (res.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '修改成功')
          dialogVisible.value = false
          load()
        }
      })
      .finally(() => {
        saving.value = false
      })
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      bannerApi.deleteBanner(id).then((res) => {
        if (res.code === '200') {
          ElMessage.success('删除成功')
          if (data.tableData.length === 1 && data.pageNum > 1) {
            data.pageNum--
          }
          load()
        }
      })
    })
    .catch(() => {})
}

// 批量删除
const batchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的轮播图')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个轮播图吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      bannerApi.batchDeleteBanner(selectedIds.value).then((res) => {
        if (res.code === '200') {
          ElMessage.success('批量删除成功')
          selectedIds.value = []
          load()
        }
      })
    })
    .catch(() => {})
}

// 更新排序
const updateSort = (row) => {
  bannerApi.updateBanner(row).then((res) => {
    if (res.code === '200') {
      ElMessage.success('排序已更新')
    }
  })
}

// 更新状态
const updateStatus = (row) => {
  bannerApi.updateBannerStatus({ id: row.id, status: row.status }).then((res) => {
    if (res.code === '200') {
      ElMessage.success('状态已更新')
    }
  })
}

// 图片上传成功
const handleImageSuccess = (res) => {
  if (res.code === '200') {
    form.image = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.image-upload-wrapper {
  width: 200px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-upload-wrapper:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  font-size: 12px;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 4px;
  color: #999;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
