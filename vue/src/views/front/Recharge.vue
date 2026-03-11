<template>
  <div class="recharge-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我的充值</h1>
      <!-- <p class="page-subtitle">账户充值 · 充值记录 · 余额管理</p> -->
    </div>

    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-card__header">
        <div class="balance-icon">
          <el-icon><Wallet /></el-icon>
        </div>
        <div class="balance-info">
          <span class="balance-label">当前账户余额</span>
          <div class="balance-amount">
            ¥ <span class="amount-number">{{ formatBalance(user.account) }}</span>
          </div>
        </div>
      </div>
      <div class="balance-card__actions">
        <el-button type="primary" class="recharge-btn" @click="handleRecharge">
          <el-icon><Plus /></el-icon>
          立即充值
        </el-button>
        <el-button class="history-btn" @click="scrollToHistory">
          <el-icon><Histogram /></el-icon>
          查看记录
        </el-button>
      </div>
    </div>

    <!-- 快速充值选项 -->
    <div class="quick-recharge">
      <h3 class="section-subtitle">快速充值</h3>
      <div class="amount-options">
        <div
          v-for="amount in quickAmounts"
          :key="amount"
          class="amount-option"
          :class="{ active: selectedAmount === amount }"
          @click="selectedAmount = amount">
          <span class="amount-value">¥{{ amount }}</span>
        </div>
        <div class="amount-option custom-amount" :class="{ active: isCustomAmount }" @click="openCustomAmount">
          <span class="amount-value">自定义</span>
        </div>
      </div>
    </div>

    <!-- 充值方式 -->
    <div class="payment-methods">
      <h3 class="section-subtitle">选择支付方式</h3>
      <div class="methods-grid">
        <div
          v-for="method in paymentMethods"
          :key="method.id"
          class="method-card"
          :class="{ active: selectedMethod === method.id }"
          @click="selectedMethod = method.id">
          <div class="method-icon" :class="method.iconClass">
            <el-icon v-if="method.id === 'wechat'"><ChatDotRound /></el-icon>
            <el-icon v-else-if="method.id === 'alipay'"><Platform /></el-icon>
            <el-icon v-else><CreditCard /></el-icon>
          </div>
          <div class="method-info">
            <span class="method-name">{{ method.name }}</span>
            <span class="method-desc">{{ method.desc }}</span>
          </div>
          <div class="method-check">
            <el-icon v-if="selectedMethod === method.id"><CircleCheckFilled /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 充值按钮 -->
    <div class="recharge-action">
      <el-button
        type="primary"
        class="submit-recharge-btn"
        :loading="rechargeLoading"
        :disabled="!selectedAmount || !selectedMethod"
        @click="handleRecharge">
        确认充值 ¥{{ selectedAmount || 0 }}
      </el-button>
    </div>

    <!-- 充值记录 -->
    <div class="recharge-history" ref="historyRef">
      <h3 class="section-subtitle">充值记录</h3>
      <el-tabs v-model="activeHistoryTab" class="history-tabs">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="成功" name="success"></el-tab-pane>
        <el-tab-pane label="处理中" name="pending"></el-tab-pane>
      </el-tabs>

      <div class="history-list">
        <div v-if="loadingHistory" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="filteredHistory.length === 0" class="empty-state">
          <el-empty description="暂无充值记录" />
        </div>

        <div v-else class="history-items">
          <div v-for="record in paginatedHistory" :key="record.id" class="history-item">
            <div class="history-item__icon" :class="record.status">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="history-item__info">
              <div class="history-item__header">
                <span class="history-item__amount">+¥{{ record.amount }}</span>
                <span class="history-item__status" :class="record.status">
                  {{ record.status === 'success' ? '成功' : record.status === 'pending' ? '处理中' : '失败' }}
                </span>
              </div>
              <div class="history-item__meta">
                <span class="history-item__method">{{ record.method }}</span>
                <span class="history-item__time">{{ record.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredHistory.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredHistory.length"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </div>

    <!-- 充值确认对话框 -->
    <el-dialog
      v-model="showRechargeDialog"
      title="支付确认"
      width="400px"
      class="recharge-dialog"
      :close-on-click-modal="false">
      <div class="dialog-content">
        <!-- 充值金额显示 -->
        <div class="amount-display">
          <span class="amount-label">充值金额</span>
          <div class="amount-value-large">¥{{ selectedAmount || 0 }}</div>
        </div>

        <!-- 支付方式显示 -->
        <div class="payment-info">
          <span class="payment-label">支付方式</span>
          <span class="payment-value">{{ selectedMethodName }}</span>
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
            @keyup.enter="confirmRecharge" />
        </div>

        <!-- 密码相关操作 -->
        <div class="password-actions">
          <template v-if="!hasPaymentPassword">
            <span class="no-password-tip">您还没有设置支付密码</span>
            <el-link type="primary" @click="goToSetPassword" underline="never">立即设置</el-link>
          </template>
          <el-link v-else type="primary" @click="goToSetPassword" underline="never">忘记密码？</el-link>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="default" @click="cancelRecharge" class="dialog-btn"> 取 消 </el-button>
          <el-button
            size="default"
            type="primary"
            @click="confirmRecharge"
            :loading="confirmLoading"
            :disabled="!hasPaymentPassword"
            class="dialog-btn confirm-btn">
            确认支付
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 自定义金额对话框 -->
    <el-dialog v-model="showCustomDialog" title="输入金额" width="90%" max-width="360px">
      <el-input
        v-model.number="customAmount"
        type="number"
        placeholder="请输入充值金额"
        :min="1"
        :max="10000"
        class="custom-amount-input">
        <template #prefix>¥</template>
      </el-input>

      <template #footer>
        <span class="dialog-footer">
          <el-button size="small" @click="showCustomDialog = false">取 消</el-button>
          <el-button size="small" type="primary" @click="confirmCustomAmount">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Wallet,
  Plus,
  Histogram,
  ChatDotRound,
  Platform,
  CreditCard,
  CircleCheckFilled,
  Lock
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const historyRef = ref(null)
const loadingHistory = ref(false)
const rechargeLoading = ref(false)
const confirmLoading = ref(false)
const showRechargeDialog = ref(false)
const showCustomDialog = ref(false)
const activeHistoryTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(5)
const paymentPassword = ref('')
const passwordError = ref('')

// 监听路由变化，确保每次进入页面都滚动到顶部
watch(
  () => route.path,
  () => {
    nextTick(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    })
  },
  { immediate: true }
)

onMounted(() => {
  loadRechargeHistory()
  nextTick(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  })
})

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))

// 检查是否有支付密码
const hasPaymentPassword = computed(() => {
  return user.value.paymentPassword && user.value.paymentPassword.length > 0
})

// 快速充值金额选项
const quickAmounts = [50, 100, 200, 500, 1000]
const selectedAmount = ref(100)
const customAmount = ref(null)
const isCustomAmount = computed(() => customAmount.value !== null && !quickAmounts.includes(selectedAmount.value))

// 支付方式
const paymentMethods = ref([
  { id: 'wechat', name: '微信支付', desc: '推荐微信用户', iconClass: 'wechat' },
  { id: 'alipay', name: '支付宝', desc: '推荐支付宝用户', iconClass: 'alipay' },
  { id: 'unionpay', name: '银联支付', desc: '支持所有银联卡', iconClass: 'unionpay' }
])
const selectedMethod = ref('wechat')

const selectedMethodName = computed(() => {
  const method = paymentMethods.value.find((m) => m.id === selectedMethod.value)
  return method ? method.name : ''
})

// 充值记录
const rechargeHistory = ref([
  {
    id: 1,
    amount: 100,
    method: '微信支付',
    status: 'success',
    time: '2024-01-15 14:30:22'
  },
  {
    id: 2,
    amount: 200,
    method: '支付宝',
    status: 'success',
    time: '2024-01-10 09:15:33'
  },
  {
    id: 3,
    amount: 50,
    method: '银联支付',
    status: 'pending',
    time: '2024-01-05 16:45:18'
  },
  {
    id: 4,
    amount: 500,
    method: '微信支付',
    status: 'success',
    time: '2023-12-28 11:20:45'
  },
  {
    id: 5,
    amount: 1000,
    method: '支付宝',
    status: 'failed',
    time: '2023-12-20 19:50:12'
  }
])

// 过滤后的记录
const filteredHistory = computed(() => {
  if (activeHistoryTab.value === 'all') return rechargeHistory.value
  return rechargeHistory.value.filter((item) => item.status === activeHistoryTab.value)
})

// 分页数据
const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredHistory.value.slice(start, end)
})

// 格式化余额
const formatBalance = (balance) => {
  if (!balance && balance !== 0) return '0.00'
  return Number(balance).toFixed(2)
}

// 滚动到历史记录
const scrollToHistory = () => {
  if (historyRef.value) {
    historyRef.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// 打开自定义金额
const openCustomAmount = () => {
  showCustomDialog.value = true
  customAmount.value = null
}

// 确认自定义金额
const confirmCustomAmount = () => {
  if (!customAmount.value || customAmount.value < 1) {
    ElMessage.warning('请输入有效的金额')
    return
  }
  if (customAmount.value > 10000) {
    ElMessage.warning('单次充值金额不能超过10000元')
    return
  }
  selectedAmount.value = customAmount.value
  showCustomDialog.value = false
}

// 跳转到设置支付密码页面
const goToSetPassword = () => {
  showRechargeDialog.value = false
  router.push('/front/set-payment-password')
}

// 处理充值
const handleRecharge = () => {
  // 先检查是否有支付密码
  if (!hasPaymentPassword.value) {
    ElMessageBox.confirm('您还没有设置支付密码，是否立即设置？', '提示', {
      confirmButtonText: '去设置',
      cancelButtonText: '暂不',
      type: 'info'
    })
      .then(() => {
        router.push('/front/set-payment-password')
      })
      .catch(() => {})
    return
  }

  showRechargeDialog.value = true
  paymentPassword.value = ''
  passwordError.value = ''
}

// 取消充值
const cancelRecharge = () => {
  showRechargeDialog.value = false
  paymentPassword.value = ''
  passwordError.value = ''
}

// 验证支付密码
const validatePaymentPassword = () => {
  if (!paymentPassword.value) {
    ElMessage.warning('请输入支付密码')
    return false
  }
  if (paymentPassword.value.length !== 6 || !/^\d+$/.test(paymentPassword.value)) {
    ElMessage.warning('支付密码必须为6位数字')
    return false
  }
  if (paymentPassword.value !== user.value.paymentPassword) {
    ElMessage.error('支付密码错误')
    return false
  }
  return true
}

// 确认充值
const confirmRecharge = async () => {
  if (!validatePaymentPassword()) {
    return
  }

  confirmLoading.value = true

  // 模拟支付成功
  setTimeout(() => {
    // 更新余额
    const newBalance = (user.value.account || 0) + selectedAmount.value
    user.value.account = newBalance

    // 保存到localStorage
    localStorage.setItem('system-user', JSON.stringify(user.value))

    // 添加充值记录
    const newRecord = {
      id: rechargeHistory.value.length + 1,
      amount: selectedAmount.value,
      method: selectedMethodName.value,
      status: 'success',
      time: new Date().toLocaleString('zh-CN', { hour12: false })
    }
    rechargeHistory.value.unshift(newRecord)

    // 重置状态
    confirmLoading.value = false
    showRechargeDialog.value = false
    paymentPassword.value = ''

    ElMessage.success(`充值成功！+¥${selectedAmount.value}`)
  }, 1500)
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  if (historyRef.value) {
    historyRef.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// 监听对话框关闭，清除密码
watch(showRechargeDialog, (newVal) => {
  if (!newVal) {
    paymentPassword.value = ''
    passwordError.value = ''
  }
})

// 加载充值记录
const loadRechargeHistory = () => {
  loadingHistory.value = true
  // 模拟加载
  setTimeout(() => {
    loadingHistory.value = false
  }, 500)
}
</script>

<style scoped>
.recharge-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
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
  background: #c8a165;
  border-radius: 2px;
}

.page-subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* 余额卡片 */
.balance-card {
  background: linear-gradient(135deg, #1e2a3a, #2d3a4a);
  border-radius: 24px;
  padding: 30px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.balance-card__header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.balance-icon {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #c8a165;
}

.balance-info {
  display: flex;
  flex-direction: column;
}

.balance-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.balance-amount {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.amount-number {
  font-size: 48px;
  margin-left: 4px;
}

.balance-card__actions {
  display: flex;
  gap: 12px;
}

.recharge-btn {
  background: #c8a165;
  border: none;
  border-radius: 40px;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: 500;
  height: 50px;
}

.recharge-btn:hover {
  background: #b28b4f;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.history-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 40px;
  padding: 12px 30px;
  font-size: 16px;
  color: white;
  height: 50px;
}

.history-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

/* 章节副标题 */
.section-subtitle {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 16px;
}

/* 金额选项 */
.amount-options {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 30px;
}

.amount-option {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.amount-option:hover {
  border-color: #c8a165;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(200, 161, 101, 0.1);
}

.amount-option.active {
  background: #c8a165;
  border-color: #c8a165;
}

.amount-option.active .amount-value {
  color: white;
  font-weight: 600;
}

.amount-value {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
}

.custom-amount {
  background: #f8fafc;
}

/* 支付方式 */
.methods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.method-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.method-card:hover {
  border-color: #c8a165;
  box-shadow: 0 5px 15px rgba(200, 161, 101, 0.05);
}

.method-card.active {
  border-color: #c8a165;
  background: rgba(200, 161, 101, 0.02);
}

.method-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.method-icon.wechat {
  background: #e8f5e9;
  color: #07c160;
}

.method-icon.alipay {
  background: #e8f0fe;
  color: #1677ff;
}

.method-icon.unionpay {
  background: #fce8e8;
  color: #e60012;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.method-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 12px;
  color: #94a3b8;
}

.method-check {
  color: #c8a165;
  font-size: 20px;
}

/* 充值按钮 */
.recharge-action {
  text-align: center;
  margin-bottom: 40px;
}

.submit-recharge-btn {
  min-width: 300px;
  height: 56px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.submit-recharge-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(200, 161, 101, 0.3);
}

.submit-recharge-btn:disabled {
  background: #cbd5e0;
  opacity: 0.7;
}

/* 充值记录 */
.recharge-history {
  background: white;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #edf2f7;
}

.history-tabs {
  margin-bottom: 20px;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.history-item:hover {
  border-color: #c8a165;
  background: white;
}

.history-item__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.history-item__icon.success {
  background: #e8f5e9;
  color: #2e7d32;
}

.history-item__icon.pending {
  background: #fff3e0;
  color: #ed6c02;
}

.history-item__icon.failed {
  background: #ffebee;
  color: #c62828;
}

.history-item__info {
  flex: 1;
}

.history-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-item__amount {
  font-size: 18px;
  font-weight: 700;
  color: #1e2a3a;
}

.history-item__status {
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 30px;
  font-weight: 500;
}

.history-item__status.success {
  background: #e8f5e9;
  color: #2e7d32;
}

.history-item__status.pending {
  background: #fff3e0;
  color: #ed6c02;
}

.history-item__status.failed {
  background: #ffebee;
  color: #c62828;
}

.history-item__meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #94a3b8;
}

/* 对话框样式  */
.recharge-dialog :deep(.el-dialog__header) {
  padding: 12px 16px;
  margin-right: 0;
}

.recharge-dialog :deep(.el-dialog__body) {
  padding: 0 16px 12px;
}

.dialog-content {
  text-align: center;
}

.amount-display {
  margin-bottom: 12px;
  padding: 8px;
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
  font-size: 24px;
  font-weight: 700;
  color: #c8a165;
}

.payment-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #edf2f7;
  margin-bottom: 12px;
  font-size: 13px;
}

.payment-label {
  font-size: 13px;
  color: #64748b;
}

.payment-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
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

.password-label .el-icon {
  font-size: 14px;
}

.password-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  height: 40px;
  font-size: 15px;
  text-align: center;
  letter-spacing: 2px;
}

.password-input :deep(.el-input__inner) {
  font-size: 15px;
}

.password-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  min-height: 24px;
}

.no-password-tip {
  color: #e6a23c;
  font-size: 12px;
}

/* 对话框底部按钮区域 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 16px 16px;
}

/* 对话框按钮样式 - 调大按钮 */
.dialog-btn {
  min-width: 90px;
  height: 38px;
  font-size: 15px;
  border-radius: 40px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

/* 确认按钮特殊样式 */
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

/* 取消按钮样式 */
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

/* 自定义金额输入框 */
.custom-amount-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 50px;
  font-size: 18px;
}

.custom-amount-input :deep(.el-input__prefix) {
  font-size: 18px;
  font-weight: 600;
  color: #c8a165;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .balance-card {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .balance-card__header {
    flex-direction: column;
    text-align: center;
  }

  .amount-options {
    grid-template-columns: repeat(3, 1fr);
  }

  .methods-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .balance-amount {
    font-size: 28px;
  }

  .amount-number {
    font-size: 36px;
  }

  .amount-options {
    grid-template-columns: repeat(2, 1fr);
  }

  .balance-card__actions {
    flex-direction: column;
    width: 100%;
  }

  .balance-card__actions .el-button {
    width: 100%;
    margin: 0;
  }

  .submit-recharge-btn {
    min-width: 100%;
  }

  .recharge-history {
    padding: 20px;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .history-item__icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  /* 移动端对话框优化 */
  .recharge-dialog :deep(.el-dialog) {
    width: 90%;
    margin: 0 auto;
  }

  .dialog-btn {
    min-width: 80px;
    height: 36px;
    font-size: 14px;
    padding: 0 16px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 380px) {
  .amount-options {
    grid-template-columns: 1fr;
  }

  .password-actions {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .dialog-footer {
    flex-direction: column-reverse;
    gap: 8px;
  }

  .dialog-btn {
    width: 100%;
  }
}
</style>
