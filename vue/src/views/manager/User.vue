<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" style="width: 200px" placeholder="请输入名称查询" :prefix-icon="Search" />
      <el-button type="primary" @click="load" style="margin-left: 10px">查询</el-button>
      <el-button @click="reset" type="info" plain> 重置 </el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <div>
        <el-table :data="data.tableData" stripe style="width: 100%">
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="avatar" label="头像">
            <template #default="scope">
              <el-image
                v-if="scope.row.avatar"
                :src="scope.row.avatar"
                style="width: 50px; height: 50px; display: block; border-radius: 50%"
                :preview-src-list="[scope.row.avatar]"
                preview-teleported></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" />
          <el-table-column prop="account" label="账户余额" />
          <el-table-column label="操作" width="180px" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="card">
      <el-pagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        @current-change="load"
        background
        layout="total,prev, pager, next, jumper"
        :total="data.total" />
    </div>

    <el-dialog v-model="data.FormVisible" title="用户信息" width="30%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 40px">
        <el-form-item prop="avatar" label="头像">
          <el-upload :action="baseUrl + '/files/upload'" list-type="picture" :on-success="handleFileUpload">
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item prop="username" label="账号">
          <el-input
            :disabled="data.form.id != undefined"
            v-model="data.form.username"
            placeholder="请输入账号"
            autocomplete="off" />
        </el-form-item>

        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.form.name" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.FormVisible = false">取消</el-button>
          <el-button type="primary" @click="save"> 确定 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Search, Edit, Delete } from '@element-plus/icons-vue'
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
  form: {},
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }]
  }
})
//分页查询数据
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
        data.tableData = res.data.list || [] // 取 list 数组
        data.total = res.data.total || 0 // 保存总记录数
      } else {
        ElMessage.error(res.msg)
      }
    })
}
load()
//重置
const reset = () => {
  data.name = null
  load()
}
//删除用户
const del = (id) => {
  ElMessageBox.confirm('您确定删除吗？', '删除确认', { type: 'warning' })
    .then((res) => {
      request.delete('/user/delete/' + id).then((res) => {
        if (res.code === '200') {
          ElMessage.success('操作成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch((err) => {})
}
//新增用户
const handleAdd = () => {
  data.form = {}
  data.FormVisible = true
}
const add = () => {
  request.post('/user/add', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.FormVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const update = () => {
  request.put('/user/update', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.FormVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
//新增保存
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      //表单校验通过
      data.form.id ? update() : add()
    }
  })
}
//编辑数据
const handleEdit = (row) => {
  //先深拷贝
  data.form = JSON.parse(JSON.stringify(row))
  data.FormVisible = true
}

//表单头像上传组件的回调函数 res.data就是头像的url
const handleFileUpload = (res) => {
  //console.log(res)
  data.form.avatar = res.data
  //console.log(data.form)
}
</script>
