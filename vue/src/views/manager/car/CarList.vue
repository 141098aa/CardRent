<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-row :gutter="10">
        <el-col :span="5">
          <el-input
            v-model="searchForm.keyword"
            placeholder="车型/品牌/标签"
            :prefix-icon="Search"
            clearable
            @keyup.enter="load" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.brandId" placeholder="选择品牌" clearable filterable @change="load">
            <el-option v-for="item in brandOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable @change="load">
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.seats" placeholder="座位数" clearable @change="load">
            <el-option label="2座" :value="2" />
            <el-option label="4座" :value="4" />
            <el-option label="5座" :value="5" />
            <el-option label="7座" :value="7" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.energy" placeholder="能源类型" clearable @change="load">
            <el-option label="燃油" value="燃油" />
            <el-option label="纯电" value="纯电" />
            <el-option label="混动" value="混动" />
            <el-option label="增程式" value="增程式" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch" plain>重置</el-button>
        </el-col>
      </el-row>

      <el-row :gutter="10" style="margin-top: 10px">
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="车辆状态" clearable @change="load">
            <el-option label="可租" value="available" />
            <el-option label="已租" value="rented" />
            <el-option label="维修" value="maintenance" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <div style="display: flex; align-items: center">
            <span style="margin-right: 8px; color: #64748b">价格区间：</span>
            <el-input-number
              v-model="searchForm.minPrice"
              :min="0"
              placeholder="最低价"
              size="small"
              style="width: 100px" />
            <span style="margin: 0 8px">-</span>
            <el-input-number
              v-model="searchForm.maxPrice"
              :min="0"
              placeholder="最高价"
              size="small"
              style="width: 100px" />
            <el-button type="primary" link @click="load" style="margin-left: 8px">确定</el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 表格卡片 -->
    <div class="card">
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between">
        <div>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增车辆
          </el-button>
          <el-button type="danger" :disabled="!selectedIds.length" @click="batchDelete">
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
        <div>
          <el-button @click="loadBrands" :loading="loadingBrands">刷新品牌</el-button>
          <el-button @click="loadCategories" :loading="loadingCategories">刷新分类</el-button>
        </div>
      </div>

      <el-table
        :data="data.tableData"
        stripe
        style="width: 100%"
        :border="false"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="车辆信息" min-width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-image
                :src="scope.row.image"
                style="width: 60px; height: 40px; border-radius: 4px; object-fit: cover; margin-right: 10px"
                :preview-src-list="[scope.row.image]"
                fit="cover" />
              <div>
                <div style="font-weight: 600">{{ scope.row.brandName }} {{ scope.row.model }}</div>
                <div style="font-size: 12px; color: #64748b">
                  {{ scope.row.year }}款 | {{ scope.row.mileage || '新车' }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="品牌" prop="brandName" width="100" />

        <el-table-column label="规格" width="150">
          <template #default="scope">
            <div>{{ scope.row.seats }}座 | {{ scope.row.gear }} | {{ scope.row.energy }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="日租价" width="100">
          <template #default="scope">
            <span style="color: #409eff; font-weight: 600">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="80" align="center" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="tag" label="标签" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.tag" type="warning" size="small">{{ scope.row.tag }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="rating" label="评分" width="80">
          <template #default="scope">
            <span>{{ scope.row.rating }} ({{ scope.row.reviewCount }})</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-dropdown trigger="click" @command="(command) => handleStatusChange(scope.row, command)">
              <el-button size="small">
                状态 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="available">设为可租</el-dropdown-item>
                  <el-dropdown-item command="rented">设为已租</el-dropdown-item>
                  <el-dropdown-item command="maintenance">设为维修</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
      :title="data.dialogType === 'add' ? '新增车辆' : '编辑车辆'"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 20px">
        <el-row :gutter="20">
          <el-col :span="9">
            <el-form-item label="品牌" prop="brandId">
              <el-select v-model="data.form.brandId" placeholder="请选择品牌" style="width: 100%" filterable>
                <el-option v-for="item in brandOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="车型" prop="model">
              <el-input v-model="data.form.model" placeholder="请输入车型" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="9">
            <el-form-item label="年份" prop="year">
              <el-input-number v-model="data.form.year" :min="2000" :max="2030" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="日租价" prop="price">
              <el-input-number v-model="data.form.price" :min="0" :precision="2" style="width: 100%">
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="座位数" prop="seats">
              <el-select v-model="data.form.seats" placeholder="座位数">
                <el-option label="2座" :value="2" />
                <el-option label="4座" :value="4" />
                <el-option label="5座" :value="5" />
                <el-option label="7座" :value="7" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="变速箱" prop="gear">
              <el-select v-model="data.form.gear" placeholder="变速箱">
                <el-option label="自动" value="自动" />
                <el-option label="手动" value="手动" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="能源类型" prop="energy">
              <el-select v-model="data.form.energy" placeholder="能源类型">
                <el-option label="燃油" value="燃油" />
                <el-option label="纯电" value="纯电" />
                <el-option label="混动" value="混动" />
                <el-option label="增程式" value="增程式" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="9">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="data.form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="标签" prop="tag">
              <el-input v-model="data.form.tag" placeholder="如:省油优选" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="里程" prop="mileage">
              <el-input v-model="data.form.mileage" placeholder="如:2.3万公里" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="排量" prop="displacement">
              <el-input v-model="data.form.displacement" placeholder="如:2.0T" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分类" prop="categoryIds">
          <el-select v-model="data.form.categoryIds" multiple placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="特色配置" prop="features">
          <el-select
            v-model="data.form.features"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请输入特色配置，回车创建"
            style="width: 100%">
            <el-option v-for="item in commonFeatures" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="主图" prop="image">
          <el-upload
            class="image-uploader"
            :action="baseUrl + '/files/upload'"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload">
            <div class="image-upload-wrapper">
              <img v-if="data.form.image" :src="data.form.image" class="uploaded-image" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="取车地点" prop="pickupLocation">
          <el-input v-model="data.form.pickupLocation" placeholder="请输入取车地点" />
        </el-form-item>

        <el-form-item label="还车地点" prop="returnLocation">
          <el-input v-model="data.form.returnLocation" placeholder="请输入还车地点" />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input v-model="data.form.description" type="textarea" :rows="3" placeholder="请输入车辆详细描述" />
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
import { Search, Plus, Delete, ArrowDown } from '@element-plus/icons-vue'
import { carApi } from '@/utils/api'

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()
const selectedIds = ref([])

// 品牌选项
const brandOptions = ref([])
const loadingBrands = ref(false)

// 分类选项
const categoryOptions = ref([])
const loadingCategories = ref(false)

// 常用配置选项
const commonFeatures = ref([
  '智能互联',
  '倒车影像',
  '定速巡航',
  '无钥匙启动',
  '自动空调',
  '真皮座椅',
  '天窗',
  '座椅加热',
  '自适应巡航',
  '车道保持',
  '自动驾驶',
  '大屏幕',
  '全景天窗',
  '哈曼卡顿',
  '氛围灯',
  '电动尾门',
  '四驱',
  '运动模式',
  '抬头显示',
  '座椅按摩'
])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  dialogVisible: false,
  dialogType: 'add', // add 或 edit
  saving: false,
  form: {
    brandId: null,
    model: '',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: new Date().getFullYear(),
    price: null,
    stock: 1,
    status: 'available',
    tag: '',
    mileage: '',
    displacement: '',
    description: '',
    image: '',
    features: [],
    configs: [],
    categoryIds: [],
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '支持异地还车'
  },
  rules: {
    brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
    model: [{ required: true, message: '请输入车型', trigger: 'blur' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    seats: [{ required: true, message: '请选择座位数', trigger: 'change' }],
    energy: [{ required: true, message: '请选择能源类型', trigger: 'change' }]
  }
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  brandId: null,
  categoryId: null,
  seats: null,
  energy: '',
  status: '',
  minPrice: null,
  maxPrice: null
})

// 加载品牌列表
const loadBrands = async () => {
  loadingBrands.value = true
  try {
    const res = await carApi.listAllBrands()
    if (res.code === '200') {
      brandOptions.value = res.data || []
    }
  } finally {
    loadingBrands.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await carApi.listAllCategories()
    if (res.code === '200') {
      categoryOptions.value = res.data || []
    }
  } finally {
    loadingCategories.value = false
  }
}

// 加载车辆列表
const load = () => {
  carApi
    .selectPage({
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      keyword: searchForm.keyword,
      brandId: searchForm.brandId,
      categoryId: searchForm.categoryId,
      seats: searchForm.seats,
      energy: searchForm.energy,
      status: searchForm.status,
      minPrice: searchForm.minPrice,
      maxPrice: searchForm.maxPrice
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
  searchForm.brandId = null
  searchForm.categoryId = null
  searchForm.seats = null
  searchForm.energy = ''
  searchForm.status = ''
  searchForm.minPrice = null
  searchForm.maxPrice = null
  data.pageNum = 1
  load()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

// 获取状态样式
const getStatusType = (status) => {
  const map = {
    available: 'success',
    rented: 'danger',
    maintenance: 'warning'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    available: '可租',
    rented: '已租',
    maintenance: '维修'
  }
  return map[status] || status
}

// 新增
const handleAdd = () => {
  data.dialogType = 'add'
  data.form = {
    brandId: null,
    model: '',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: new Date().getFullYear(),
    price: null,
    stock: 1,
    status: 'available',
    tag: '',
    mileage: '',
    displacement: '',
    description: '',
    image: '',
    features: [],
    configs: [],
    categoryIds: [],
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '支持异地还车'
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
    const apiCall = data.dialogType === 'add' ? carApi.add : carApi.update

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
  ElMessageBox.confirm('确定要删除该车辆吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      carApi.deleteCar(id).then((res) => {
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
    ElMessage.warning('请选择要删除的车辆')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 辆车吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      carApi.batchDelete(selectedIds.value).then((res) => {
        if (res.code === '200') {
          ElMessage.success('批量删除成功')
          selectedIds.value = []
          if (data.tableData.length === selectedIds.value.length && data.pageNum > 1) {
            data.pageNum--
          }
          load()
        }
      })
    })
    .catch(() => {})
}

// 更改状态
const handleStatusChange = (row, status) => {
  carApi
    .updateStatus({
      id: row.id,
      status: status
    })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('状态更新成功')
        load()
      }
    })
}

// 图片上传成功
const handleImageSuccess = (res) => {
  if (res.code === '200') {
    data.form.image = res.data
    ElMessage.success('图片上传成功')
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

// 初始化
onMounted(() => {
  load()
  loadBrands()
  loadCategories()
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
</style>
