<template>
  <div class="admin-scope">
    <div class="profile-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">个人信息</h1>
        <p class="page-subtitle">管理您的账户信息</p>
      </div>

      <!-- 个人信息卡片 -->
      <div class="profile-card card">
        <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" class="profile-form">
          <!-- 头像上传区域 -->
          <el-form-item prop="avatar" label="头像" class="avatar-item">
            <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
              :before-upload="beforeAvatarUpload">
              <div class="avatar-wrapper">
                <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar-preview" />
                <div v-else class="avatar-uploader-icon">
                  <el-icon><Plus /></el-icon>
                  <span>上传头像</span>
                </div>
              </div>
            </el-upload>
            <div class="avatar-tip">支持jpg、png格式，大小不超过2MB</div>
          </el-form-item>

          <!-- 账号（不可修改） -->
          <el-form-item prop="username" label="账号">
            <div class="info-text">{{ data.user.username }}</div>
          </el-form-item>

          <!-- 姓名 -->
          <el-form-item prop="name" label="姓名">
            <el-input v-model="data.user.name" placeholder="请输入您的姓名" class="custom-input" clearable />
          </el-form-item>

          <!-- 角色信息（只读） -->
          <el-form-item prop="role" label="角色">
            <el-tag type="primary" effect="light" class="role-tag">
              {{ data.user.role || '管理员' }}
            </el-tag>
          </el-form-item>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button type="primary" class="submit-btn" @click="update" :loading="loading">保存修改</el-button>
            <el-button class="reset-btn" @click="resetForm">取消</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import router from '@/router'
import { adminApi } from '@/utils/api'

const emit = defineEmits(['updateUser'])
const loading = ref(false)

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
  }
})

if (!data.user?.id) {
  ElMessage.error('请登录!')
  router.push('/login')
}

// 获取用户信息
const loadUser = async () => {
  try {
    const res = await adminApi.selectById(data.user.id)
    if (res.code === '200') {
      data.user = res.data
      localStorage.setItem('system-user', JSON.stringify(res.data))
      emit('updateUser')
    } else {
      ElMessage.error(res.msg || '获取用户信息失败')
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}
loadUser()

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

// 表单头像上传组件的回调函数
const handleFileUpload = (res) => {
  if (res.code === '200') {
    data.user.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// 更新数据
const update = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await adminApi.update(data.user)
        if (res.code === '200') {
          ElMessage.success('个人信息更新成功')
          await loadUser() // 重新加载用户信息
        } else {
          ElMessage.error(res.msg || '更新失败')
        }
      } catch (error) {
        ElMessage.error('更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  loadUser()
}
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
  position: relative;
  display: inline-block;
}

.page-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 40px;
  height: 3px;
  background: #409eff;
  border-radius: 2px;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 个人信息卡片 */
.profile-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.03);
  border: 1px solid #edf2f7;
}

.profile-form {
  max-width: 400px;
  margin: 0 auto;
}

/* 头像上传区域 */
.avatar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-wrapper:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: scale(1.02);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  font-size: 12px;
}

.avatar-uploader-icon .el-icon {
  font-size: 32px;
  margin-bottom: 4px;
  color: #999;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

/* 信息展示样式 */
.info-text {
  padding: 8px 0;
  color: #1e2a3a;
  font-weight: 500;
  background-color: #f8fafc;
  padding: 8px 12px;
  border-radius: 6px;
  width: 100%;
  border: 1px solid #edf2f7;
}

/* 输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #edf2f7 inset;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* 角色标签 */
.role-tag {
  padding: 6px 16px;
  font-size: 14px;
  border-radius: 30px;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.submit-btn {
  min-width: 120px;
  height: 40px;
  border-radius: 6px;
  font-weight: 500;
}

.reset-btn {
  min-width: 100px;
  height: 40px;
  border-radius: 6px;
}
</style>
