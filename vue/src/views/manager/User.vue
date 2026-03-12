<template>
  <div class="admin-scope">
    <!-- 查询卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.name"
        style="width: 200px; margin-right: 10px"
        placeholder="请输入名称查询"
        :prefix-icon="Search"
        @keyup.enter="load"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset" plain>重置</el-button>
    </div>

    <!-- 表格卡片 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>

      <!-- 无边框表格 -->
      <el-table :data="data.tableData" stripe style="width: 100%" :border="false">
        <el-table-column prop="username" label="账号" min-width="120" />

        <el-table-column prop="name" label="姓名" min-width="120" />

        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="40" :fit="'cover'">
              <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" min-width="140" />

        <el-table-column prop="email" label="邮箱" min-width="180" />

        <el-table-column prop="account" label="账户余额" min-width="120">
          <template #default="scope">
            <span style="color: #409eff; font-weight: 600">¥{{ scope.row.account || '0.00' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="realNameVerified" label="实名认证" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.realNameVerified ? 'success' : 'info'" size="small" effect="light">
              {{ scope.row.realNameVerified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="driverLicenseVerified" label="驾驶证认证" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.driverLicenseVerified ? 'success' : 'info'" size="small" effect="light">
              {{ scope.row.driverLicenseVerified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(scope.row)" plain> 编辑 </el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="del(scope.row.id)" plain> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页卡片 -->
    <div class="card" style="display: flex; justify-content: flex-end">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :page-sizes="[5, 10, 20, 50]"
        @size-change="load"
        @current-change="load"
        :total="data.total" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="data.form.id ? '编辑用户' : '新增用户'"
      width="450px"
      v-model="data.FormVisible"
      :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 20px">
        <!-- 头像上传 -->
        <el-form-item prop="avatar" label="头像">
          <el-upload
            class="avatar-uploader"
            :action="baseUrl + '/files/upload'"
            :show-file-list="false"
            :on-success="handleFileUpload"
            :before-upload="beforeAvatarUpload">
            <div class="avatar-wrapper">
              <img v-if="data.form.avatar" :src="data.form.avatar" class="avatar-preview" />
              <div v-else class="avatar-uploader-icon">
                <el-icon><Plus /></el-icon>
                <span>上传头像</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item prop="username" label="账号">
          <el-input
            :disabled="data.form.id != undefined"
            v-model="data.form.username"
            placeholder="请输入账号"
            clearable />
        </el-form-item>

        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.form.name" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item prop="phone" label="手机号">
          <el-input v-model="data.form.phone" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item prop="email" label="邮箱">
          <el-input v-model="data.form.email" placeholder="请输入邮箱" clearable />
        </el-form-item>

        <!-- 密码字段（仅新增时显示） -->
        <el-form-item prop="password" label="密码" v-if="!data.form.id">
          <el-input v-model="data.form.password" type="password" placeholder="请输入密码" show-password clearable />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.FormVisible = false">取 消</el-button>
          <el-button type="primary" @click="save" :loading="data.saving">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Search, Edit, Delete, Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()

const data = reactive({
  name: null,
  tableData: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  FormVisible: false,
  saving: false,
  form: {},
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 3, max: 20, message: '账号长度在3-20个字符之间', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '姓名长度在2-20个字符之间', trigger: 'blur' }
    ],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
    ]
  }
})

// 分页查询数据
const load = () => {
  request
    .get('/user/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name
      }
    })
    .then((res) => {
      if (res.code === '200') {
        data.tableData = res.data.list || []
        data.total = res.data.total || 0
      } else {
        ElMessage.error(res.msg)
      }
    })
}

// 重置
const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

// 删除用户
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '再想想'
  })
    .then(() => {
      request.delete('/user/delete/' + id).then((res) => {
        if (res.code === '200') {
          // 如果当前页只有一条数据且不是第一页，则返回上一页
          if (data.tableData.length === 1 && data.pageNum > 1) {
            data.pageNum--
          }
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

// 新增用户
const handleAdd = () => {
  data.form = {} // 清空表单，不需要设置默认角色
  data.FormVisible = true
}

// 新增保存
const add = () => {
  data.saving = true
  request
    .post('/user/add', data.form)
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('新增成功')
        data.FormVisible = false
        load()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .finally(() => {
      data.saving = false
    })
}

// 编辑保存
const update = () => {
  data.saving = true
  request
    .put('/user/update', data.form)
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('修改成功')
        data.FormVisible = false
        load()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .finally(() => {
      data.saving = false
    })
}

// 新增/编辑保存
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 如果是新增，设置默认角色为普通用户
      if (!data.form.id) {
        data.form.role = '普通用户'
      }
      data.form.id ? update() : add()
    }
  })
}

// 编辑数据
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.FormVisible = true
}

// 头像上传成功回调
const handleFileUpload = (res) => {
  if (res.code === '200') {
    data.form.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 初始化加载
load()
</script>

<style scoped></style>
