<template>
  <div class="password-container">
    <div class="password-card">
      <!-- 头部 -->
      <div class="password-header">
        <h2 class="password-title">修改密码</h2>
        <p class="password-subtitle">为了账户安全，请定期更换密码</p>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="100px" class="password-form">
        <!-- 原密码 -->
        <el-form-item prop="password" label="原密码">
          <el-input
            v-model="data.user.password"
            show-password
            autocomplete="off"
            placeholder="请输入原密码"
            class="custom-input" />
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item prop="newPassword" label="新密码">
          <el-input
            v-model="data.user.newPassword"
            show-password
            autocomplete="off"
            placeholder="请输入新密码"
            class="custom-input" />
          <div class="password-tip">密码长度至少8位，包含字母和数字</div>
        </el-form-item>

        <!-- 确认新密码 -->
        <el-form-item prop="confimPassword" label="确认新密码">
          <el-input
            v-model="data.user.confimPassword"
            show-password
            autocomplete="off"
            placeholder="请再次输入新密码"
            class="custom-input" />
        </el-form-item>

        <!-- 安全提示 -->
        <div class="security-tips">
          <el-icon class="security-icon"><Warning /></el-icon>
          <span>密码强度：</span>
          <div class="strength-bar">
            <div class="strength-progress" :style="{ width: passwordStrength + '%' }"></div>
          </div>
          <span class="strength-text">{{ strengthText }}</span>
        </div>

        <!-- 按钮区域 -->
        <div class="form-actions">
          <el-button type="primary" class="submit-btn" @click="updatePassword" :loading="loading"> 确认修改 </el-button>
          <el-button class="reset-btn" @click="handleCancel"> 取 消 </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import router from '@/router'

const loading = ref(false)
const formRef = ref()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  newPassword: '',
  confimPassword: '',
  rules: {
    password: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 8, message: '密码长度至少8位', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          const hasLetter = /[a-zA-Z]/.test(value)
          const hasNumber = /[0-9]/.test(value)
          if (!hasLetter || !hasNumber) {
            callback(new Error('密码必须包含字母和数字'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    confimPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== data.user.newPassword) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: ['blur', 'change']
      }
    ]
  }
})

// 计算密码强度
const passwordStrength = computed(() => {
  const pwd = data.user.newPassword || ''
  if (!pwd) return 0

  let strength = 0
  if (pwd.length >= 8) strength += 20
  if (pwd.length >= 10) strength += 10
  if (/[a-z]/.test(pwd)) strength += 15
  if (/[A-Z]/.test(pwd)) strength += 15
  if (/[0-9]/.test(pwd)) strength += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) strength += 20

  return Math.min(strength, 100)
})

const strengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength < 30) return '弱'
  if (strength < 60) return '中'
  if (strength < 80) return '强'
  return '非常强'
})

// 更新密码
const updatePassword = () => {
  if (data.user.newPassword !== data.user.confimPassword) {
    ElMessage.warning('两次输入的新密码不同，请确认！')
    return
  }

  formRef.value?.validate((valid) => {
    if (valid) {
      loading.value = true
      request
        .put('/updatePassword', data.user)
        .then((res) => {
          loading.value = false
          if (res.code === '200') {
            ElMessage.success('密码修改成功，请重新登录')
            setTimeout(() => {
              logout()
            }, 1500)
          } else {
            ElMessage.error(res.msg)
          }
        })
        .catch(() => {
          loading.value = false
        })
    }
  })
}

// 取消
const handleCancel = () => {
  router.back()
}

// 退出登录
const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
}
</script>

<style scoped>
.password-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-card {
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
  transition: all 0.3s ease;
}

.password-card:hover {
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.04);
}

.password-header {
  padding: 30px 30px 20px;
  border-bottom: 1px solid #edf2f7;
  background: linear-gradient(135deg, #ffffff, #fafafa);
}

.password-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 8px;
  position: relative;
  display: inline-block;
}

.password-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 40px;
  height: 3px;
  background: #c8a165;
  border-radius: 2px;
}

.password-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.password-form {
  padding: 30px;
}

/* 自定义输入框 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
  height: 46px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c8a165 inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px rgba(200, 161, 101, 0.2),
    0 0 0 1px #c8a165 inset;
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #1e293b;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
  font-size: 14px;
}

/* 密码提示 */
.password-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  padding-left: 4px;
}

/* 安全提示 */
.security-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: -8px 0 20px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.security-icon {
  color: #c8a165;
  font-size: 16px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.strength-progress {
  height: 100%;
  background: linear-gradient(90deg, #f97316, #c8a165, #10b981);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.strength-text {
  font-size: 12px;
  font-weight: 600;
  min-width: 40px;
  color: #64748b;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.submit-btn {
  min-width: 140px;
  height: 48px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.reset-btn {
  min-width: 100px;
  height: 48px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 15px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: #f8fafc;
  border-color: #c8a165;
  color: #c8a165;
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  font-size: 15px;
  padding-right: 16px;
}

:deep(.el-form-item__error) {
  padding-left: 4px;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 640px) {
  .password-container {
    padding: 20px 16px;
  }

  .password-card {
    border-radius: 20px;
  }

  .password-header {
    padding: 24px 20px;
  }

  .password-title {
    font-size: 22px;
  }

  .password-form {
    padding: 24px 20px;
  }

  .form-actions {
    flex-direction: column;
    gap: 12px;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
  }

  .security-tips {
    flex-wrap: wrap;
  }
}
</style>
