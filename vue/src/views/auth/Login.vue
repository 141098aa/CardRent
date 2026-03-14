<template>
  <div class="login-container">
    <!-- 背景图片层  -->
    <div class="background-layer"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <div class="brand-icon">
          <span class="car-icon">🚗</span>
        </div>
        <div class="brand-title">易租车</div>
        <div class="brand-subtitle">轻松出行 · 随心所驭</div>
      </div>

      <!-- 欢迎文字 -->
      <div class="welcome-text">
        <h3>欢迎回来</h3>
      </div>

      <!-- 登录表单 -->
      <el-form :model="data.form" ref="formRef" :rules="data.rules" class="login-form">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" v-model="data.form.username" placeholder="账号" class="custom-input" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            :prefix-icon="Lock"
            v-model="data.form.password"
            placeholder="密码"
            show-password
            class="custom-input" />
        </el-form-item>

        <el-form-item prop="role">
          <el-select style="width: 100%" v-model="data.form.role" class="custom-select">
            <el-option value="普通用户" label="普通用户"></el-option>
            <el-option value="管理员" label="管理员"></el-option>
          </el-select>
        </el-form-item>

        <div class="forgot-password">
          <a href="#">忘记密码？</a>
        </div>

        <!-- 协议勾选 -->
        <div class="agreement-checkbox">
          <el-checkbox v-model="data.agree" class="custom-checkbox">
            <span class="agree-text">
              我已阅读并同意
              <a href="javascript:void(0);" @click="openAgreement('service')" class="agree-link">《用户服务协议》</a>
              和
              <a href="javascript:void(0);" @click="openAgreement('privacy')" class="agree-link">《隐私政策》</a>
            </span>
          </el-checkbox>
        </div>

        <el-form-item>
          <el-button type="primary" class="login-button" @click="login" :loading="loading" :disabled="!data.agree">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册引导 -->
      <div class="register-guide">没有账号？ <a href="/register" class="register-link">注册</a></div>
    </div>

    <!-- 协议弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      class="agreement-dialog"
      :close-on-click-modal="false"
      @close="resetCountdown">
      <div class="agreement-content">
        <div v-html="agreementContent"></div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">关 闭</el-button>
          <el-button type="primary" @click="handleAgree" :disabled="countdown > 0">
            {{ countdown > 0 ? `阅读协议 (${countdown}s)` : '已阅读并同意' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onUnmounted } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import router from '@/router'

const loading = ref(false)
const dialogVisible = ref(false)
const agreementType = ref('service')
const countdown = ref(0)
let timer = null

const data = reactive({
  form: { role: '管理员' },
  agree: false,
  rules: {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
})

const formRef = ref()

// 开始倒计时
const startCountdown = () => {
  countdown.value = 5 // 5秒倒计时
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

// 重置倒计时
const resetCountdown = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  countdown.value = 0
}

// 处理关闭
const handleClose = () => {
  dialogVisible.value = false
  resetCountdown()
}

// 处理同意
const handleAgree = () => {
  if (countdown.value > 0) return
  dialogVisible.value = false
  data.agree = true
  resetCountdown()
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

// 弹窗标题
const dialogTitle = computed(() => {
  return agreementType.value === 'service' ? '用户服务协议' : '隐私政策'
})

// 弹窗内容
const agreementContent = computed(() => {
  if (agreementType.value === 'service') {
    return `
      <h3>用户服务协议</h3>
      <p>欢迎使用易租车服务！</p>
      <h4>1. 服务内容</h4>
      <p>易租车提供汽车租赁信息服务，包括但不限于车辆查询、预订、支付等功能。</p>
      <h4>2. 用户责任</h4>
      <p>用户应保证提供的个人信息真实有效，遵守交通法规，安全驾驶。</p>
      <h4>3. 隐私保护</h4>
      <p>我们将严格保护您的个人信息，详细内容请查看《隐私政策》。</p>
      <h4>4. 违约责任</h4>
      <p>如用户违反本协议，我们有权暂停或终止服务。</p>
      <p style="text-align: right; margin-top: 20px;">易租车团队<br>2024年1月1日</p>
    `
  } else {
    return `
      <h3>隐私政策</h3>
      <p>我们高度重视您的隐私保护。</p>
      <h4>1. 信息收集</h4>
      <p>我们收集的信息包括：账号信息、身份信息、车辆使用记录等。</p>
      <h4>2. 信息使用</h4>
      <p>您的信息仅用于提供服务、改善体验、安全验证等目的。</p>
      <h4>3. 信息共享</h4>
      <p>未经您的同意，我们不会向第三方共享您的个人信息，法律法规另有规定的除外。</p>
      <h4>4. 信息安全</h4>
      <p>我们采用行业标准的安全技术保护您的信息。</p>
      <p style="text-align: right; margin-top: 20px;">易租车团队<br>2024年1月1日</p>
    `
  }
})

// 打开协议弹窗
const openAgreement = (type) => {
  agreementType.value = type
  dialogVisible.value = true
  startCountdown() // 打开弹窗时开始倒计时
}

const login = () => {
  if (!data.agree) {
    ElMessage.warning('请先同意用户服务协议和隐私政策')
    return
  }

  formRef.value
    .validate((valid) => {
      if (valid) {
        loading.value = true
        request
          .post('/login', data.form)
          .then((res) => {
            loading.value = false
            if (res.code === '200') {
              ElMessage.success('登录成功')
              if (res.data.role === '管理员') {
                router.push('/manager/home')
              } else {
                router.push('/front/home')
              }
              localStorage.setItem('system-user', JSON.stringify(res.data))
            } else {
              ElMessage.error(res.msg)
            }
          })
          .catch(() => {
            loading.value = false
          })
      }
    })
    .catch((error) => {
      console.error(error)
    })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background-color: #000;
}

/* 汽车背景图 */
.background-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/imgs/bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: brightness(0.6) contrast(1.1);
  z-index: 1;
}

/*登录卡片 */
.login-card {
  width: 360px;
  padding: 30px 28px;
  border-radius: 16px;
  background-color: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(8px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 品牌区域  */
.brand-area {
  text-align: center;
  margin-bottom: 20px;
}
.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e3a5f;
  letter-spacing: 2px;
  margin-bottom: 5px;
}
.brand-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #1e3a5f, #2b4c7c);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 10px;
  box-shadow: 0 8px 16px rgba(30, 58, 95, 0.25);
}

.car-icon {
  font-size: 28px;
}
.brand-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  letter-spacing: 1px;
}
/* 欢迎文字 - */
.welcome-text {
  margin-bottom: 20px;
  text-align: center;
}

.welcome-text h3 {
  font-size: 18px;
  font-weight: 500;
  color: #4a5568;
  margin: 0;
  position: relative;
  display: inline-block;
}

.welcome-text h3::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 2px;
  background: #c8a165;
  border-radius: 2px;
}

/* 登录表单  */
.login-form {
  margin-top: 5px;
}

:deep(.el-form-item) {
  margin-bottom: 18px !important;
}

/* 自定义输入框样式 - */
:deep(.custom-input .el-input__wrapper) {
  border-radius: 10px;
  padding: 2px 12px;
  background-color: #f8fafc;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
  height: 42px;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: #c8a165;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #c8a165;
  box-shadow: 0 0 0 3px rgba(200, 161, 101, 0.1);
}

:deep(.custom-input .el-input__inner) {
  height: 40px;
  font-size: 14px;
}

:deep(.custom-input .el-input__prefix) {
  margin-right: 8px;
}

:deep(.custom-input .el-input__prefix-inner) {
  color: #c8a165;
  font-size: 16px;
}

/* 自定义选择框样式*/
:deep(.custom-select .el-input__wrapper) {
  border-radius: 10px;
  padding: 2px 12px;
  background-color: #f8fafc;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  height: 42px;
}

:deep(.custom-select .el-input__inner) {
  height: 40px;
  font-size: 14px;
}

/* 忘记密码 */
.forgot-password {
  text-align: right;
  margin: -8px 0 8px;
}

.forgot-password a {
  color: #94a3b8;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password a:hover {
  color: #c8a165;
}

/*  协议勾选样式 */
.agreement-checkbox {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

/* 自定义复选框样式 */
:deep(.custom-checkbox .el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #c8a165;
  border-color: #c8a165;
}

:deep(.custom-checkbox .el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #c8a165;
}

:deep(.custom-checkbox .el-checkbox__input:hover .el-checkbox__inner) {
  border-color: #c8a165;
}

:deep(.custom-checkbox .el-checkbox__label) {
  font-size: 13px;
  color: #4a5568;
}

.agree-text {
  font-size: 13px;
  color: #4a5568;
}

.agree-link {
  color: #c8a165;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
  cursor: pointer;
}

.agree-link:hover {
  color: #b28b4f;
  text-decoration: underline;
}

/* 登录按钮  */
.login-button {
  width: 100%;
  height: 44px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 16px rgba(200, 161, 101, 0.25);
  color: white;
}

.login-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(200, 161, 101, 0.35);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 4px 8px rgba(200, 161, 101, 0.2);
}

.login-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
}

.register-guide {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #7f8c8d;
}

.register-link {
  color: #c8a165;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}

.register-link:hover {
  color: #b28b4f;
  text-decoration: underline;
}

/* 协议弹窗样式 */
:deep(.agreement-dialog .el-dialog__header) {
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 24px;
}

:deep(.agreement-dialog .el-dialog__title) {
  color: #1e3a5f;
  font-weight: 600;
}

:deep(.agreement-dialog .el-dialog__body) {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.agreement-content {
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
}

.agreement-content h3 {
  color: #1e3a5f;
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 16px;
  text-align: center;
}

.agreement-content h4 {
  color: #2c3e50;
  font-size: 15px;
  margin: 16px 0 8px;
}

.agreement-content p {
  margin: 8px 0;
}

.agreement-content p:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button--primary {
  background: #c8a165;
  border-color: #c8a165;
  min-width: 140px;
}

.dialog-footer .el-button--primary:hover:not(:disabled) {
  background: #b28b4f;
  border-color: #b28b4f;
}

.dialog-footer .el-button--primary:disabled {
  background: #e9d5b5;
  border-color: #e9d5b5;
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
