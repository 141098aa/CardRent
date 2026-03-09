<template>
  <div class="card" style="width: 40%">
    <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="100px" style="padding-right: 40px">
      <el-form-item prop="password" label="原密码">
        <el-input v-model="data.user.password" show-password autocomplete="off" />
      </el-form-item>

      <el-form-item prop="newPassword" label="新密码">
        <el-input v-model="data.user.newPassword" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item prop="confimPassword" label="确认新密码">
        <el-input v-model="data.user.confimPassword" show-password autocomplete="off" />
      </el-form-item>

      <div style="text-align: center">
        <el-button type="primary" size="large" @click="updatePassword">保存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import router from '@/router'

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    password: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    confimPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
  }
})

//更新数据
const updatePassword = () => {
  if (data.user.newPassword !== data.user.confimPassword) {
    ElMessage.warning('两次输入的新密码不同，请确认！')
    return
  }
  request.put('/updatePassword', data.user).then((res) => {
    if (res.code === '200') {
      ElMessage.success('修改密码成功')
      logout()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const logout = () => {
  router.push('/login')
  ElMessage.success('请登录')
  localStorage.removeItem('system-user')
}
</script>
