<template>
  <div class="forget-password-container">
    <div class="password-card">
      <div class="password-header">
        <h2 class="password-title">忘记支付密码</h2>
        <p class="password-subtitle">验证身份后重新设置支付密码</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="password-form">
        <!-- 验证码区域 -->
        <div class="form-item">
          <div class="input-label">验证码</div>
          <div class="code-wrapper">
            <el-input v-model="form.resetCode" placeholder="请输入4位验证码" maxlength="4" class="code-input">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
            <el-button
              type="primary"
              class="code-btn"
              @click="sendResetCode"
              :loading="sendingCode"
              :disabled="codeSent"
              plain>
              {{ codeSent ? '已发送' : '获取验证码' }}
            </el-button>
          </div>

          <!-- 验证码显示 -->
          <div v-if="codeSent && resetCode" class="code-display">
            <span>验证码：</span>
            <span class="code-value">{{ resetCode }}</span>
            <el-tag size="small" type="warning" effect="light">10分钟有效</el-tag>
          </div>
        </div>

        <!-- 新密码 -->
        <div class="form-item">
          <div class="input-label">新支付密码</div>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6位数字密码"
            maxlength="6"
            show-password
            class="custom-input">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 确认密码 -->
        <div class="form-item">
          <div class="input-label">确认密码</div>
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入支付密码"
            maxlength="6"
            show-password
            class="custom-input">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 按钮区域 -->
        <div class="form-actions">
          <el-button type="primary" class="submit-btn" @click="handleReset" :loading="loading"> 确认重置 </el-button>
          <el-button class="cancel-btn" @click="goBack">取 消</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { paymentPasswordApi } from '@/utils/api'
import { Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const formRef = ref()
const resetCode = ref('')
const codeSent = ref(false)

const form = reactive({
  resetCode: '',
  password: '',
  confirmPassword: ''
})

// 验证规则
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入支付密码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('支付密码必须为6位数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入支付密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  resetCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur' }
  ],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// 发送重置验证码
const sendResetCode = async () => {
  sendingCode.value = true
  try {
    const res = await paymentPasswordApi.sendResetCode()
    if (res.code === '200') {
      resetCode.value = res.data.resetCode
      codeSent.value = true
      ElMessage.success('验证码已生成')
    } else {
      ElMessage.error(res.msg || '获取验证码失败')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || '获取验证码失败'
    ElMessage.error(errorMsg)
  } finally {
    sendingCode.value = false
  }
}

// 重置密码
const handleReset = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await paymentPasswordApi.resetPassword({
          resetCode: form.resetCode,
          newPassword: form.password
        })

        if (res.code === '200') {
          ElMessage.success('支付密码重置成功')
          setTimeout(() => {
            router.back()
          }, 1500)
        } else {
          ElMessage.error(res.msg || '重置失败')
        }
      } catch (error) {
        console.error('重置密码失败:', error)
        const errorMsg = error.response?.data?.msg || error.message || '重置失败'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}

// 返回
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.forget-password-container {
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
  max-width: 460px;
  background: white;
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
  font-size: 24px;
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
  margin: 8px 0 0;
}

.password-form {
  padding: 30px;
}

.form-item {
  margin-bottom: 24px;
}

.input-label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  margin-bottom: 8px;
}

.code-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s ease;
  height: 46px;
}

.code-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c8a165 inset;
}

.code-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px rgba(200, 161, 101, 0.2),
    0 0 0 1px #c8a165 inset;
}

.code-btn {
  width: 110px;
  height: 46px;
  border-radius: 12px;
  color: #c8a165;
  border-color: #c8a165;
  background: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.code-btn:hover {
  background: #fcf6ed;
  color: #b28b4f;
  border-color: #b28b4f;
}

.code-btn:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  border-color: #e2e8f0;
  opacity: 0.7;
}

.code-display {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #1e2a3a;
  border: 1px solid #edf2f7;
}

.code-value {
  font-size: 18px;
  font-weight: 700;
  color: #c8a165;
  letter-spacing: 2px;
  background: white;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px dashed #c8a165;
}

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

.custom-input :deep(.el-input__prefix) {
  color: #c8a165;
  margin-right: 8px;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
}

.submit-btn {
  flex: 1;
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

.cancel-btn {
  flex: 0.6;
  height: 48px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 15px;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #f8fafc;
  border-color: #c8a165;
  color: #c8a165;
}
</style>
