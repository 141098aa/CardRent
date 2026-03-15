<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="searchForm.name"
        style="width: 200px; margin-right: 10px"
        placeholder="请输入品牌名称"
        :prefix-icon="Search"
        @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="resetSearch" plain>重置</el-button>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增品牌
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
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="品牌名称" min-width="70" />

        <el-table-column label="排序" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.sortOrder"
              :min="0"
              size="small"
              controls-position="right"
              style="width: 80px"
              @change="(val) => updateSort(scope.row)" />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="250" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center; gap: 8px">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="() => updateStatus(scope.row)" />
              <span :style="{ color: scope.row.status === 1 ? '#67c23a' : '#f56c6c', fontWeight: 500 }">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="120" />

        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <div style="display: flex; gap: 8px; justify-content: center">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </div>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="data.dialogVisible"
      :title="data.dialogType === 'add' ? '新增品牌' : '编辑品牌'"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="data.form.name" placeholder="请输入品牌名称" />
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="data.form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="data.form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="save" :loading="data.saving">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { carApi } from '@/utils/api'

const formRef = ref()
const selectedIds = ref([])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  dialogVisible: false,
  dialogType: 'add',
  saving: false,
  form: {
    name: '',
    sortOrder: 0,
    status: 1
  },
  rules: {
    name: [
      { required: true, message: '请输入品牌名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ]
  }
})

const searchForm = reactive({
  name: ''
})

// 加载数据
const load = () => {
  carApi
    .selectBrandPage({
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: searchForm.name
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
  searchForm.name = ''
  data.pageNum = 1
  load()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

// 新增
const handleAdd = () => {
  data.dialogType = 'add'
  data.form = {
    name: '',
    sortOrder: 0,
    status: 1
  }
  data.dialogVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.dialogType = 'edit'
  data.form = JSON.parse(JSON.stringify(row))
  data.dialogVisible = true
}

// 保存
const save = () => {
  formRef.value?.validate((valid) => {
    if (!valid) return

    data.saving = true
    const apiCall = data.dialogType === 'add' ? carApi.addBrand : carApi.updateBrand

    apiCall(data.form)
      .then((res) => {
        if (res.code === '200') {
          ElMessage.success(data.dialogType === 'add' ? '新增成功' : '修改成功')
          data.dialogVisible = false
          load()
        }
      })
      .finally(() => {
        data.saving = false
      })
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该品牌吗？删除后可能影响相关车辆', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      carApi.deleteBrand(id).then((res) => {
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
    ElMessage.warning('请选择要删除的品牌')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个品牌吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      // 批量删除需要后端支持，这里简化为循环删除
      Promise.all(selectedIds.value.map((id) => carApi.deleteBrand(id))).then(() => {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        load()
      })
    })
    .catch(() => {})
}

// 更新排序
const updateSort = (row) => {
  carApi.updateBrand(row).then((res) => {
    if (res.code === '200') {
      ElMessage.success('排序已更新')
    }
  })
}

// 更新状态
const updateStatus = (row) => {
  carApi.updateBrand(row).then((res) => {
    if (res.code === '200') {
      ElMessage.success('状态已更新')
    }
  })
}

// 初始化
onMounted(() => {
  load()
})
</script>
