<template>
  <div class="admin-scope">
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

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>

      <!-- 无边框表格 -->
      <el-table :data="data.tableData" stripe style="width: 100%" :border="false">
        <el-table-column label="用户名" prop="username" min-width="120"></el-table-column>

        <el-table-column label="名称" prop="name" min-width="120"></el-table-column>

        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="40" :fit="'cover'">
              <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column label="角色" prop="role" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === '超级管理员' ? 'danger' : 'primary'" size="small" effect="light">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(scope.row)" plain> 编辑 </el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(scope.row.id)" plain>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

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
      :title="data.form.id ? '编辑管理员' : '新增管理员'"
      width="450px"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding-right: 20px">
        <!-- 头像上传 -->
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImgSuccess"
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

        <el-form-item label="账号" prop="username">
          <el-input
            :disabled="data.form.id ? true : false"
            v-model="data.form.username"
            placeholder="请输入账号"
            clearable />
        </el-form-item>

        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" placeholder="请输入名称" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password" v-if="!data.form.id">
          <el-input v-model="data.form.password" type="password" placeholder="请输入密码" show-password clearable />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save" :loading="data.saving">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Search, Edit, Delete, Plus } from '@element-plus/icons-vue'

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  saving: false,
  form: {},
  tableData: [],
  name: null
})

// 分页查询
const load = () => {
  request
    .get('/admin/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name
      }
    })
    .then((res) => {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    })
}

// 新增
const handleAdd = () => {
  data.form = {
    role: '普通管理员' // 默认角色
  }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  data.saving = true
  request
    .post('/admin/add', data.form)
    .then((res) => {
      if (res.code === '200') {
        load()
        ElMessage.success('新增成功')
        data.formVisible = false
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
    .put('/admin/update', data.form)
    .then((res) => {
      if (res.code === '200') {
        load()
        ElMessage.success('修改成功')
        data.formVisible = false
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    })
    .finally(() => {
      data.saving = false
    })
}

// 弹窗保存
const save = () => {
  // 表单验证
  if (!data.form.username) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!data.form.name) {
    ElMessage.warning('请输入名称')
    return
  }
  if (!data.form.id && !data.form.password) {
    ElMessage.warning('请输入密码')
    return
  }

  // data.form有id就是更新，没有就是新增
  data.form.id ? update() : add()
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '再想想'
  })
    .then(() => {
      request.delete('/admin/delete/' + id).then((res) => {
        if (res.code === '200') {
          // 如果当前页只有一条数据且不是第一页，则返回上一页
          if (data.tableData.length === 1 && data.pageNum > 1) {
            data.pageNum--
          }
          load()
          ElMessage.success('删除成功')
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      })
    })
    .catch(() => {})
}

// 重置
const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

// 处理文件上传的钩子
const handleImgSuccess = (res) => {
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

<style scoped>
/* 所有通用样式都已移到全局src\assets\css\admin-theme.css，这里只保留特定于此页面的样式 */
</style>
