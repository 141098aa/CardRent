<template>
  <div class="orders-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">查看和管理您的租车订单</p>
    </div>

    <!-- 订单统计卡片 -->
    <div class="order-stats">
      <div class="stat-card" @click="handleStatClick('all')">
        <div class="stat-icon all">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">全部订单</span>
          <span class="stat-value">{{ orderStats.all || 0 }}</span>
        </div>
      </div>

      <div class="stat-card" @click="handleStatClick('pending')">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">待处理</span>
          <span class="stat-value">{{ orderStats.pending || 0 }}</span>
        </div>
      </div>

      <div class="stat-card" @click="handleStatClick('ongoing')">
        <div class="stat-icon active">
          <el-icon><Van /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">进行中</span>
          <span class="stat-value">{{ orderStats.ongoing || 0 }}</span>
        </div>
      </div>

      <div class="stat-card" @click="handleStatClick('completed')">
        <div class="stat-icon completed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">已完成</span>
          <span class="stat-value">{{ orderStats.completed || 0 }}</span>
        </div>
      </div>

      <div class="stat-card" @click="handleStatClick('cancelled')">
        <div class="stat-icon cancelled">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">已取消</span>
          <span class="stat-value">{{ orderStats.cancelled || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 订单标签页 -->
    <div class="orders-content">
      <el-tabs v-model="activeTab" class="order-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="全部订单" name="all"></el-tab-pane>
        <el-tab-pane label="待处理" name="pending"></el-tab-pane>
        <!-- 待付款+待审核 -->
        <el-tab-pane label="进行中" name="ongoing"></el-tab-pane>
        <!-- 待取车+进行中 -->
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
        <!-- 已完成 -->
        <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
        <!-- 已取消+已退款 -->
      </el-tabs>

      <!-- 订单列表 -->
      <div class="order-list">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="orders.length === 0" class="empty-state">
          <el-empty description="暂无订单记录" />
        </div>

        <div v-else class="order-items">
          <div v-for="order in paginatedOrders" :key="order.id" class="order-card">
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderNo }}</span>
                <span class="order-time">下单时间：{{ formatDateTime(order.createTime) }}</span>
              </div>
              <el-tag :type="getStatusType(order.status)" effect="light" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>

            <!-- 订单内容 -->
            <div class="order-body">
              <div class="car-info">
                <img :src="order.carImage" :alt="order.carName" class="car-image" />
                <div class="car-details">
                  <h3 class="car-name">{{ order.carName }}</h3>
                  <div class="car-specs">
                    <span>{{ order.carSeats || '5' }}座</span>
                    <span>{{ order.carGear || '自动' }}</span>
                    <span>{{ order.carEnergy || '燃油' }}</span>
                  </div>
                </div>
              </div>

              <div class="order-details">
                <div class="detail-row">
                  <span class="detail-label">取车时间：</span>
                  <span class="detail-value">{{ formatDateTime(order.pickupTime) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">还车时间：</span>
                  <span class="detail-value">{{ formatDateTime(order.returnTime) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">租车天数：</span>
                  <span class="detail-value">{{ order.days }}天</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">取车地点：</span>
                  <span class="detail-value">{{ order.pickupLocation }}</span>
                </div>
              </div>

              <div class="order-price">
                <span class="price-label">订单金额</span>
                <span class="price-value">¥{{ order.totalPrice }}</span>
                <span class="price-unit">(含保险)</span>
              </div>
            </div>

            <!-- 订单操作 -->
            <div class="order-footer">
              <div class="order-actions">
                <!-- 待付款 - 显示支付按钮 -->
                <el-button v-if="order.status === 'pending_pay'" type="primary" size="small" @click="handlePay(order)">
                  立即支付
                </el-button>
                <!-- 待取车 - 显示确认取车按钮 -->
                <el-button
                  v-if="order.status === 'pending_pickup'"
                  type="primary"
                  size="small"
                  @click="handlePickup(order)">
                  确认取车
                </el-button>

                <!-- 进行中 - 显示申请还车按钮 -->
                <el-button v-if="order.status === 'active'" type="success" size="small" @click="handleReturn(order)">
                  申请还车
                </el-button>

                <!-- 待付款和待审核的订单可以取消 -->
                <el-button
                  v-if="['pending_pay', 'pending_audit'].includes(order.status)"
                  size="small"
                  @click="handleCancel(order)">
                  取消订单
                </el-button>

                <!-- 已完成订单可以评价 -->
                <el-button v-if="order.status === 'completed'" size="small" @click="handleReview(order)">
                  评价
                </el-button>

                <!-- 查看详情按钮始终显示 -->
                <el-button size="small" @click="handleDetail(order)">查看详情</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="orders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="600px" class="order-detail-dialog">
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-section">
          <h4>订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单号：</span>
              <span class="info-value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单状态：</span>
              <el-tag :type="getStatusType(currentOrder.status)" size="small">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">支付方式：</span>
              <span class="info-value">{{ getPaymentMethodText(currentOrder.paymentMethod) || '--' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">下单时间：</span>
              <span class="info-value">{{ formatDateTime(currentOrder.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">支付时间：</span>
              <span class="info-value">{{ formatDateTime(currentOrder.paymentTime) || '--' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>车辆信息</h4>
          <div class="car-detail-info">
            <img :src="currentOrder.carImage" class="detail-car-image" />
            <div class="detail-car-info">
              <div class="detail-car-name">{{ currentOrder.carName }}</div>
              <div class="detail-car-specs">
                <span>{{ currentOrder.carSeats || '5' }}座</span>
                <span>{{ currentOrder.carGear || '自动' }}</span>
                <span>{{ currentOrder.carEnergy || '燃油' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>租车信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">取车时间：</span>
              <span class="info-value">{{ formatDateTime(currentOrder.pickupTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">还车时间：</span>
              <span class="info-value">{{ formatDateTime(currentOrder.returnTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">租车天数：</span>
              <span class="info-value">{{ currentOrder.days }}天</span>
            </div>
            <div class="info-item">
              <span class="info-label">取车地点：</span>
              <span class="info-value">{{ currentOrder.pickupLocation }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">还车地点：</span>
              <span class="info-value">{{ currentOrder.returnLocation }}</span>
            </div>
          </div>
        </div>

        <!-- 订单详情对话框  -->
        <div class="detail-section">
          <h4>费用明细</h4>
          <div class="price-detail">
            <!-- 基础租金 -->
            <div class="price-row">
              <span>基础租金 (¥{{ currentOrder.dailyPrice }} × {{ currentOrder.days }}天)</span>
              <span>¥{{ currentOrder.dailyPrice * currentOrder.days }}</span>
            </div>

            <!-- 动态调价明细 - 从 priceAdjustments 解析 -->
            <div v-if="parsedAdjustments.length > 0" class="adjustments-list">
              <div v-for="(adj, index) in parsedAdjustments" :key="index" class="adjustment-item">
                <div class="adjustment-info">
                  <span>{{ adj.name }}</span>
                  <el-tooltip :content="getAdjustmentDescription(adj)" placement="top">
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                  <span class="adjustment-badge" :class="getAdjustmentBadgeClass(adj)">
                    {{ adj.description }}
                  </span>
                </div>
                <span :class="adj.amount > 0 ? 'price-up' : 'price-down'">
                  {{ adj.amount > 0 ? '+' : '' }}{{ adj.amount }}
                </span>
              </div>
            </div>

            <!-- 调整后租金 - 只在有调价时显示 -->
            <div v-if="parsedAdjustments.length > 0" class="price-row adjusted-rent">
              <span>调整后租金</span>
              <span class="highlight-price">¥{{ adjustedRent }}</span>
            </div>

            <!-- 保险 -->
            <div class="price-row">
              <span>基础保险 (¥{{ currentOrder.insurancePrice || 50 }}/天 × {{ currentOrder.days }}天)</span>
              <span>¥{{ (currentOrder.insurancePrice || 50) * currentOrder.days }}</span>
            </div>

            <!-- 押金 -->
            <div class="price-row">
              <span>押金 (可退)</span>
              <span>¥{{ currentOrder.deposit || 0 }}</span>
            </div>

            <!-- 总计 -->
            <div class="price-row total">
              <span>总计</span>
              <span class="total-price">¥{{ currentOrder.totalPrice }}</span>
            </div>

            <!-- 价格说明悬浮提示 -->
            <div class="price-note">
              <el-popover placement="top" :width="300" trigger="hover">
                <template #reference>
                  <span class="price-tip">
                    <el-icon><QuestionFilled /></el-icon>
                    价格说明
                  </span>
                </template>
                <div class="price-policy">
                  <h4>动态定价说明</h4>
                  <p>我们的价格会随市场情况动态调整，确保您在合适的时间获得最优价格：</p>
                  <ul>
                    <li>周末溢价：周六至周日用车，价格上浮20%</li>
                    <li>节假日溢价：国家法定节假日，价格上浮30~50%</li>
                    <li>长租优惠：租期7天以上，享受折扣优惠</li>
                    <li>超时费用：不足30分钟免费；30分钟-4小时按小时计费；超过4小时按1天计费</li>
                  </ul>
                  <p>所有价格调整都会在订单确认页明细展示，请您放心预订。</p>
                </div>
              </el-popover>
            </div>
          </div>
        </div>
        <div v-if="currentOrder.remark" class="detail-section">
          <h4>备注信息</h4>
          <div class="remark-content">
            {{ currentOrder.remark }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
  <!-- 支付弹窗 -->
  <PaymentDialog
    v-model:visible="showPaymentDialog"
    :amount="currentOrder?.totalPrice"
    :order-id="currentOrder?.id"
    @pay="handlePayment"
    @success="handlePaymentSuccess" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Clock, Van, CircleCheck, CircleClose, InfoFilled, QuestionFilled } from '@element-plus/icons-vue'
import { getMyOrders, getOrderStats, cancelOrder, payOrder } from '@/utils/api/user/order'
import PaymentDialog from '@/components/PaymentDialog.vue'
import { getUserById } from '@/utils/api/user/profile'

const emit = defineEmits(['updateUser'])
// 支付弹窗控制
const showPaymentDialog = ref(false)
// 添加用户信息变量
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))

// 统计卡片点击处理
const handleStatClick = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
  loadOrders() // 重新加载订单
}

// 状态定义
const loading = ref(false)
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const showDetailDialog = ref(false)
const currentOrder = ref(null)
const orders = ref([])
const orderStats = ref({
  all: 0,
  pending: 0,
  ongoing: 0,
  completed: 0,
  cancelled: 0
})

// 获取状态样式
const getStatusType = (status) => {
  const map = {
    pending_pay: 'warning',
    pending_audit: 'warning',
    pending_pickup: 'primary',
    active: 'primary',
    completed: 'success',
    cancelled: 'info',
    refunding: 'danger',
    refunded: 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending_pay: '待付款',
    pending_audit: '待审核',
    pending_pickup: '待取车',
    active: '进行中',
    completed: '已完成',
    cancelled: '已取消',
    refunding: '退款中',
    refunded: '已退款'
  }
  return map[status] || status
}
// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const map = {
    wechat: '微信支付',
    alipay: '支付宝',
    unionpay: '银联支付',
    wallet: '钱包余额'
  }
  return map[method] || method
}
// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '--'
  return datetime.replace('T', ' ').substring(0, 19)
}
// 解析调价明细
const parsedAdjustments = computed(() => {
  if (!currentOrder.value?.priceAdjustments) {
    console.log('priceAdjustments 为空:', currentOrder.value?.priceAdjustments)
    return []
  }
  try {
    console.log('原始 priceAdjustments:', currentOrder.value.priceAdjustments)
    const parsed = JSON.parse(currentOrder.value.priceAdjustments)
    console.log('解析后的调价明细:', parsed)
    return parsed
  } catch (e) {
    console.error('解析调价明细失败', e, '原始数据:', currentOrder.value.priceAdjustments)
    return []
  }
})

// 获取调价项的徽章样式
const getAdjustmentBadgeClass = (adj) => {
  if (adj.type === 'discount') return 'discount-badge'
  if (adj.type === 'overtime') return 'overtime-badge'
  return 'premium-badge'
}

// 获取调价项的描述
const getAdjustmentDescription = (adj) => {
  switch (adj.type) {
    case 'weekend':
      return '周末用车价格上浮20%'
    case 'season':
      return '节假日用车价格上浮30%~50%'
    case 'discount':
      return adj.description || '长租优惠已自动应用'
    case 'overtime':
      if (adj.hours) {
        return `超时费用：超时 ${adj.hours} 小时，按小时计费`
      } else if (adj.description && adj.description.includes('超过4小时')) {
        return '超时费用：超过4小时按1天计费，包含当天保险'
      } else {
        return '超时费用：超时按小时或天数计费'
      }
    default:
      return adj.description || ''
  }
}
// 计算调整后租金
const adjustedRent = computed(() => {
  if (!currentOrder.value) return 0
  if (currentOrder.value.dynamicRent) return currentOrder.value.dynamicRent
  return currentOrder.value.dailyPrice * currentOrder.value.days
})
// 加载订单统计
const loadOrderStats = async () => {
  try {
    const res = await getOrderStats()
    if (res.code === '200') {
      orderStats.value = res.data
    } else {
      ElMessage.error(res.msg || '加载订单统计失败')
    }
  } catch (error) {
    console.error('加载订单统计失败:', error)
    ElMessage.error(error.response?.data?.msg || error.message || '加载订单统计失败')
  }
}

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    // 根据 activeTab 映射对应的状态
    if (activeTab.value !== 'all') {
      switch (activeTab.value) {
        case 'pending':
          params.status = 'pending_pay,pending_audit'
          break
        case 'ongoing':
          params.status = 'pending_pickup,active'
          break
        case 'completed':
          params.status = 'completed'
          break
        case 'cancelled':
          params.status = 'cancelled,refunded'
          break
        default:
          params.status = activeTab.value
      }
    }

    const res = await getMyOrders(params)
    if (res.code === '200') {
      orders.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '加载订单列表失败')
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error(error.response?.data?.msg || error.message || '加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 分页订单
const paginatedOrders = computed(() => {
  return orders.value
})

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  loadOrders()
  loadOrderStats() // 重新加载统计
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadOrders()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
const handlePayAgain = (order) => {
  currentOrder.value = order
  showPaymentDialog.value = true
}
// 支付处理
const handlePayment = async (params) => {
  try {
    const res = await payOrder({
      id: params.orderId,
      paymentMethod: 'wallet',
      paymentPassword: params.paymentPassword
    })

    if (res.code === '200') {
    } else {
      ElMessage.error(res.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败')
  }
}
// 支付成功后的处理
const handlePaymentSuccess = () => {
  showPaymentDialog.value = false
  loadOrders() // 刷新订单列表
  loadOrderStats() // 刷新统计
  // 刷新用户余额
  const userInfo = JSON.parse(localStorage.getItem('system-user') || '{}')
  getUserById(userInfo.id).then((res) => {
    if (res.code === '200') {
      localStorage.setItem('system-user', JSON.stringify(res.data))
      user.value = res.data
    }
  })
}
// 确认取车（实际应该是用户确认取车，通知后台）
const handlePickup = (order) => {
  ElMessageBox.confirm('确认已取到车辆吗？', '确认取车', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(() => {
      // TODO: 调用确认取车API
      ElMessage.success('取车成功，祝您用车愉快！')
      loadOrders()
      loadOrderStats()
    })
    .catch(() => {})
}

// 申请还车
const handleReturn = (order) => {
  ElMessageBox.confirm('确认已归还车辆吗？', '申请还车', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(() => {
      // TODO: 调用申请还车API
      ElMessage.success('还车申请已提交，等待确认')
      loadOrders()
      loadOrderStats()
    })
    .catch(() => {})
}

// 取消订单
const handleCancel = (order) => {
  ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    inputPlaceholder: '请输入取消原因',
    type: 'warning'
  })
    .then(async ({ value }) => {
      if (!value) {
        ElMessage.warning('请输入取消原因')
        return
      }

      try {
        const res = await cancelOrder({
          id: order.id,
          reason: value
        })
        if (res.code === '200') {
          ElMessage.success('订单已取消')
          loadOrders()
          loadOrderStats()
        } else {
          ElMessage.error(res.msg || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error(error.response?.data?.msg || error.message || '取消订单失败')
      }
    })
    .catch(() => {})
}

// 支付订单 - 打开支付弹窗
const handlePay = (order) => {
  currentOrder.value = order
  showPaymentDialog.value = true
}

// 评价
const handleReview = (order) => {
  ElMessage.info('评价功能开发中...')
}

// 查看详情
const handleDetail = (order) => {
  currentOrder.value = order
  showDetailDialog.value = true
}

// 初始化
onMounted(() => {
  loadOrderStats()
  loadOrders()
})
</script>

<style scoped>
.orders-container {
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

/* 订单统计卡片 */
.order-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.04);
  border-color: #c8a165;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.all {
  background: #e6f0ff;
  color: #3182ce;
}

.stat-icon.pending {
  background: #fff3e0;
  color: #ed6c02;
}

.stat-icon.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.stat-icon.completed {
  background: #f3e5f5;
  color: #7b1fa2;
}
.stat-icon.cancelled {
  background: #fef2f2;
  color: #ef4444;
}
.stat-info {
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e2a3a;
}

/* 订单列表 */
.orders-content {
  background: white;
  border-radius: 24px;
  padding: 24px;
  border: 1px solid #edf2f7;
}

.order-tabs {
  margin-bottom: 20px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-card:hover {
  border-color: #c8a165;
  box-shadow: 0 5px 15px rgba(200, 161, 101, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #edf2f7;
}

.order-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

.order-number {
  font-weight: 600;
  color: #1e2a3a;
}

.order-body {
  padding: 16px;
  display: grid;
  grid-template-columns: 200px 1fr 150px;
  gap: 20px;
}

.car-info {
  display: flex;
  gap: 12px;
}

.car-image {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.car-details {
  flex: 1;
}

.car-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.car-specs {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.detail-label {
  width: 80px;
  color: #64748b;
}

.detail-value {
  color: #1e2a3a;
  font-weight: 500;
}

.order-price {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.price-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.price-value {
  font-size: 22px;
  font-weight: 700;
  color: #c8a165;
  line-height: 1.2;
}

.price-unit {
  font-size: 11px;
  color: #94a3b8;
}

.order-footer {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #edf2f7;
}

.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
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

/* 订单详情对话框 */
.order-detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #edf2f7;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: baseline;
}

.info-label {
  width: 80px;
  font-size: 13px;
  color: #64748b;
}

.info-value {
  font-size: 13px;
  color: #1e2a3a;
  font-weight: 500;
}

.car-detail-info {
  display: flex;
  gap: 16px;
}

.detail-car-image {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.detail-car-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.detail-car-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 8px;
}

.detail-car-specs {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
}

.price-detail {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #4a5568;
}

.price-row.total {
  border-top: 1px solid #edf2f7;
  margin-top: 8px;
  padding-top: 16px;
  font-weight: 700;
  color: #c8a165;
  font-size: 16px;
}
/* 在样式表中添加 */
.adjustments-list {
  margin: 8px 0;
  padding-left: 16px;
  border-left: 2px solid #e2e8f0;
}

.adjustment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
}

.adjustment-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.info-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.price-up {
  color: #f56c6c;
  font-weight: 500;
}

.price-down {
  color: #67c23a;
  font-weight: 500;
}

.adjusted-rent {
  font-weight: 600;
  color: #409eff;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.price-note {
  margin-top: 16px;
  text-align: right;
}

.price-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
}

.price-tip:hover {
  color: #409eff;
}

.price-policy {
  padding: 8px;
}

.price-policy h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #1e2a3a;
}

.price-policy p {
  margin: 4px 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.price-policy ul {
  margin: 8px 0;
  padding-left: 16px;
  color: #64748b;
  font-size: 12px;
}

.price-policy li {
  margin: 4px 0;
}
.adjustments-list {
  margin: 8px 0;
  padding-left: 12px;
  border-left: 2px solid #e2e8f0;
}

.adjustment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.adjustment-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.info-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.info-icon:hover {
  color: #409eff;
}

.price-up {
  color: #ef4444;
  font-weight: 500;
}

.price-down {
  color: #10b981;
  font-weight: 500;
}

.adjusted-rent {
  padding-top: 6px;
  margin-top: 6px;
  border-top: 1px dashed #e2e8f0;
  font-weight: 600;
}

.final-rent {
  font-weight: 500;
  color: #1e2a3a;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.normal-price {
  color: #1e2a3a;
  font-size: 16px;
  font-weight: 600;
}

.adjustments-list {
  margin: 8px 0;
  padding-left: 12px;
  border-left: 2px solid #e2e8f0;
}

.adjustment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.adjustment-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.info-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.info-icon:hover {
  color: #409eff;
}

.adjustment-badge {
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.premium-badge {
  background: #fee2e2;
  color: #ef4444;
}

.discount-badge {
  background: #dcfce7;
  color: #10b981;
}

.overtime-badge {
  background: #fff3e0;
  color: #ed6c02;
}

.price-up {
  color: #ef4444;
  font-weight: 500;
}

.price-down {
  color: #10b981;
  font-weight: 500;
}

.adjusted-rent {
  padding-top: 6px;
  margin-top: 6px;
  border-top: 1px dashed #e2e8f0;
  font-weight: 600;
}

.highlight-price {
  color: #409eff;
  font-size: 16px;
}

.total-price {
  color: #c8a165;
  font-size: 18px;
  font-weight: 700;
}
.remark-content {
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #c8a165;
  color: #4a5568;
  font-size: 14px;
  line-height: 1.6;
}
</style>
