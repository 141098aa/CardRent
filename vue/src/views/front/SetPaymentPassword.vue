<template>
  <div class="set-password-container">
    <div class="password-card">
      <div class="password-header">
        <h2 class="password-title">设置支付密码</h2>
        <p class="password-subtitle">为您的账户设置6位数字支付密码</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="password-form">
        <el-form-item label="支付密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入6位数字支付密码"
            maxlength="6"
            show-password
            class="password-input" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入支付密码"
            maxlength="6"
            show-password
            class="password-input" />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading"> 确认设置 </el-button>
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

const router = useRouter()
const loading = ref(false)
const formRef = ref()

const form = reactive({
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
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// 提交设置
const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true

      // 获取当前用户信息
      const user = JSON.parse(localStorage.getItem('system-user') || '{}')

      // 模拟API请求
      setTimeout(() => {
        // 保存支付密码
        user.paymentPassword = form.password
        localStorage.setItem('system-user', JSON.stringify(user))

        loading.value = false
        ElMessage.success('支付密码设置成功')

        // 返回上一页
        setTimeout(() => {
          router.back()
        }, 1500)
      }, 1000)
    }
  })
}

// 返回
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.set-password-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-card {
  max-width: 500px;
  width: 100%;
  background: white;
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.03);
  border: 1px solid #edf2f7;
  overflow: hidden;
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

.password-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 50px;
  font-size: 16px;
  letter-spacing: 2px;
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 30px;
}

.submit-btn {
  flex: 1;
  height: 50px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 16px;
  font-weight: 600;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.cancel-btn {
  flex: 1;
  height: 50px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 16px;
}

.cancel-btn:hover {
  background: #f8fafc;
  border-color: #c8a165;
  color: #c8a165;
}

@media (max-width: 600px) {
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
  }
}
</style>
