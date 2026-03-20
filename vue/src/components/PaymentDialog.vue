<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(val) => emit('update:visible', val)"
    title="支付确认"
    width="400px"
    class="payment-dialog"
    :close-on-click-modal="false"
    @close="handleClose">
    <div class="dialog-content">
      <!-- 支付金额显示 -->
      <div class="amount-display">
        <span class="amount-label">支付金额</span>
        <div class="amount-value-large">¥{{ amount }}</div>
      </div>

      <!-- 余额信息 -->
      <div class="balance-info">
        <span>当前余额：</span>
        <span class="balance-amount">¥{{ formatBalance(userBalance) }}</span>
        <span v-if="userBalance < amount" class="balance-warning">
          余额不足，
          <el-link type="primary" @click="goToRecharge" :underline="'never'">去充值</el-link>
        </span>
      </div>

      <!-- 密码输入区域 -->
      <div class="password-section">
        <div class="password-label">
          <el-icon><Lock /></el-icon>
          <span>支付密码</span>
        </div>
        <el-input
          v-model="paymentPassword"
          type="password"
          placeholder="6位数字密码"
          maxlength="6"
          show-password
          class="password-input"
          @keyup.enter="handleConfirm" />
      </div>

      <!-- 错误次数提示 -->
      <div v-if="!lockStatus.locked && lockStatus.errorCount > 0" class="error-count-tip">
        <el-alert
          :title="`支付密码错误，还剩${5 - lockStatus.errorCount}次机会，连续错误5次将锁定20分钟`"
          type="warning"
          :closable="false"
          show-icon />
      </div>

      <!-- 锁定提示 -->
      <div v-if="lockStatus.locked" class="lock-tip">
        <el-alert :title="lockMessage" type="error" :closable="false" show-icon />
      </div>

      <!-- 密码相关操作 -->
      <div class="password-actions">
        <template v-if="!hasPaymentPassword">
          <span class="no-password-tip">您还没有设置支付密码</span>
          <el-link type="primary" @click="goToSetPassword" :underline="'never'">立即设置</el-link>
        </template>
        <el-link type="primary" @click="goToForgetPassword" :underline="'never'">忘记密码？</el-link>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="default" @click="handleCancel" class="dialog-btn">取 消</el-button>
        <el-button
          size="default"
          type="primary"
          @click="handleConfirm"
          :loading="loading"
          :disabled="!hasPaymentPassword || lockStatus.locked || userBalance < amount"
          class="dialog-btn confirm-btn">
          确认支付
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { paymentPasswordApi } from '@/utils/api' // 用于密码验证和锁定状态
import { userOrderApi } from '@/utils/api' // 用于实际支付

const router = useRouter()
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  amount: {
    type: Number,
    default: 0
  },
  orderId: {
    type: Number,
    default: null
  },
  orderNo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'success', 'cancel'])

const loading = ref(false)
const paymentPassword = ref('')
const lockStatus = ref({ locked: false, errorCount: 0 })
const lockMessage = ref('')

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))
const hasPaymentPassword = computed(() => {
  return user.value.paymentPassword && user.value.paymentPassword.length > 0
})
const userBalance = computed(() => user.value.account || 0)

// 格式化余额
const formatBalance = (balance) => {
  if (!balance && balance !== 0) return '0.00'
  return Number(balance).toFixed(2)
}
// 跳转到充值页面
const goToRecharge = () => {
  emit('update:visible', false)
  router.push('/front/recharge')
}
// 检查锁定状态
const checkLockStatus = async () => {
  try {
    const res = await paymentPasswordApi.getLockStatus()
    if (res.code === '200') {
      lockStatus.value = res.data
      if (res.data.locked) {
        lockMessage.value = `支付密码错误次数过多，请在${res.data.remainingMinutes}分钟后重试`
      }
    }
  } catch (error) {
    console.error('获取锁定状态失败:', error)
  }
}

// 验证支付密码格式
const validatePaymentPassword = () => {
  if (!paymentPassword.value) {
    ElMessage.warning('请输入支付密码')
    return false
  }
  if (paymentPassword.value.length !== 6 || !/^\d+$/.test(paymentPassword.value)) {
    ElMessage.warning('支付密码必须为6位数字')
    return false
  }
  return true
}

// 确认支付
const handleConfirm = async () => {
  if (!validatePaymentPassword()) return

  loading.value = true

  try {
    const res = await userOrderApi.payOrder({
      id: props.orderId,
      paymentMethod: 'wallet',
      paymentPassword: paymentPassword.value
    })

    if (res.code === '200') {
      ElMessage.success('支付成功！')

      // 更新后的用户信息
      if (res.data) {
        localStorage.setItem('system-user', JSON.stringify(res.data))
        user.value = res.data
      }

      emit('success', res.data)
      reset()
    } else {
      ElMessage.error(res.msg || '支付失败')
      await checkLockStatus()
      if (lockStatus.value.locked) {
        ElMessage.warning(lockMessage.value)
      } else if (lockStatus.value.errorCount > 0) {
        const remainingAttempts = 5 - lockStatus.value.errorCount
        ElMessage.warning(`支付密码错误，还剩${remainingAttempts}次机会`)
      }
    }
  } catch (error) {
    console.error('支付失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || '支付失败'
    ElMessage.error(errorMsg)
    await checkLockStatus()
    if (lockStatus.value.locked) {
      ElMessage.warning(lockMessage.value)
    } else if (lockStatus.value.errorCount > 0) {
      const remainingAttempts = 5 - lockStatus.value.errorCount
      ElMessage.warning(`支付密码错误，还剩${remainingAttempts}次机会`)
    }
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
  reset()
}

// 关闭
const handleClose = () => {
  emit('update:visible', false)
  reset()
}

// 重置
const reset = () => {
  paymentPassword.value = ''
  lockStatus.value = { locked: false, errorCount: 0 }
  lockMessage.value = ''
}

// 跳转到设置支付密码页面
const goToSetPassword = () => {
  emit('update:visible', false)
  router.push('/front/set-payment-password')
}

// 跳转到忘记密码页面
const goToForgetPassword = () => {
  emit('update:visible', false)
  router.push('/front/forget-payment-password')
}

// 监听弹窗打开，刷新锁定状态和用户信息
watch(
  () => props.visible,
  async (newVal) => {
    if (newVal) {
      // 刷新用户信息
      const userStr = localStorage.getItem('system-user')
      if (userStr) {
        user.value = JSON.parse(userStr)
      }
      await checkLockStatus()
      paymentPassword.value = ''
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.payment-dialog :deep(.el-dialog__header) {
  padding: 12px 16px;
  margin-right: 0;
  border-bottom: 1px solid #edf2f7;
}

.payment-dialog :deep(.el-dialog__body) {
  padding: 0 16px 12px;
}

.dialog-content {
  text-align: center;
}

.amount-display {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.amount-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 2px;
}

.amount-value-large {
  font-size: 28px;
  font-weight: 700;
  color: #c8a165;
}

.balance-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f1f5f9;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
}

.balance-info .balance-amount {
  color: #409eff;
  font-weight: 600;
}

.balance-warning {
  color: #f56c6c;
  font-size: 12px;
  margin-left: 8px;
}

.password-section {
  margin-bottom: 8px;
}

.password-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 6px;
  color: #1e2a3a;
  font-size: 13px;
}

.password-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  height: 40px;
  font-size: 15px;
  text-align: center;
  letter-spacing: 2px;
}

.password-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  min-height: 24px;
  margin-top: 8px;
}

.no-password-tip {
  color: #e6a23c;
  font-size: 12px;
}

.error-count-tip,
.lock-tip {
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 16px 16px;
}

.dialog-btn {
  min-width: 90px;
  height: 38px;
  font-size: 15px;
  border-radius: 40px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.confirm-btn {
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-weight: 500;
  box-shadow: 0 4px 10px rgba(200, 161, 101, 0.2);
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(200, 161, 101, 0.3);
}

.confirm-btn:disabled {
  background: #cbd5e0;
  opacity: 0.7;
  transform: none;
  box-shadow: none;
}

.dialog-btn:not(.confirm-btn) {
  border: 1px solid #e2e8f0;
  background: white;
  color: #64748b;
}

.dialog-btn:not(.confirm-btn):hover {
  background: #f8fafc;
  border-color: #c8a165;
  color: #c8a165;
}
</style>
