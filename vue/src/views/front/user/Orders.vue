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
          <span class="stat-value">{{ orders.length }}</span>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('pending')">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">待取车</span>
          <span class="stat-value">{{ pendingCount }}</span>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('active')">
        <div class="stat-icon active">
          <el-icon><Van /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">进行中</span>
          <span class="stat-value">{{ activeCount }}</span>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('completed')">
        <div class="stat-icon completed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">已完成</span>
          <span class="stat-value">{{ completedCount }}</span>
        </div>
      </div>
    </div>

    <!-- 订单标签页 -->
    <div class="orders-content">
      <el-tabs v-model="activeTab" class="order-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="全部订单" name="all"></el-tab-pane>
        <el-tab-pane label="待取车" name="pending"></el-tab-pane>
        <el-tab-pane label="进行中" name="active"></el-tab-pane>
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
        <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
      </el-tabs>

      <!-- 订单列表 -->
      <div class="order-list">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else-if="filteredOrders.length === 0" class="empty-state">
          <el-empty description="暂无订单记录" />
        </div>

        <div v-else class="order-items">
          <div v-for="order in paginatedOrders" :key="order.id" class="order-card">
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderNo }}</span>
                <span class="order-time">下单时间：{{ order.createTime }}</span>
              </div>
              <el-tag :type="order.statusType" effect="light" size="small">
                {{ order.statusText }}
              </el-tag>
            </div>

            <!-- 订单内容 -->
            <div class="order-body">
              <div class="car-info">
                <img :src="order.carImage" :alt="order.carName" class="car-image" />
                <div class="car-details">
                  <h3 class="car-name">{{ order.carName }}</h3>
                  <div class="car-specs">
                    <span>{{ order.carSeats }}座</span>
                    <span>{{ order.carGear }}</span>
                    <span>{{ order.carEnergy }}</span>
                  </div>
                </div>
              </div>

              <div class="order-details">
                <div class="detail-row">
                  <span class="detail-label">取车时间：</span>
                  <span class="detail-value">{{ order.pickupTime }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">还车时间：</span>
                  <span class="detail-value">{{ order.returnTime }}</span>
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
                <el-button v-if="order.status === 'pending'" type="primary" size="small" @click="handlePickup(order)">
                  确认取车
                </el-button>
                <el-button v-if="order.status === 'active'" type="success" size="small" @click="handleReturn(order)">
                  申请还车
                </el-button>
                <el-button
                  v-if="order.status === 'pending' || order.status === 'active'"
                  size="small"
                  @click="handleCancel(order)">
                  取消订单
                </el-button>
                <el-button v-if="order.status === 'completed'" size="small" @click="handleReview(order)">
                  评价
                </el-button>
                <el-button size="small" @click="handleDetail(order)"> 查看详情 </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredOrders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredOrders.length"
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
              <el-tag :type="currentOrder.statusType" size="small">{{ currentOrder.statusText }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">下单时间：</span>
              <span class="info-value">{{ currentOrder.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">支付时间：</span>
              <span class="info-value">{{ currentOrder.payTime || '--' }}</span>
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
                <span>{{ currentOrder.carSeats }}座</span>
                <span>{{ currentOrder.carGear }}</span>
                <span>{{ currentOrder.carEnergy }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>租车信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">取车时间：</span>
              <span class="info-value">{{ currentOrder.pickupTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">还车时间：</span>
              <span class="info-value">{{ currentOrder.returnTime }}</span>
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

        <div class="detail-section">
          <h4>费用明细</h4>
          <div class="price-detail">
            <div class="price-row">
              <span>日租金 (¥{{ currentOrder.dailyPrice }} × {{ currentOrder.days }}天)</span>
              <span>¥{{ currentOrder.rentalPrice }}</span>
            </div>
            <div class="price-row">
              <span>基础保险</span>
              <span>¥{{ currentOrder.insurancePrice || 0 }}</span>
            </div>
            <div class="price-row">
              <span>手续费</span>
              <span>¥{{ currentOrder.servicePrice || 0 }}</span>
            </div>
            <div class="price-row total">
              <span>总计</span>
              <span>¥{{ currentOrder.totalPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Clock, Van, CircleCheck } from '@element-plus/icons-vue'

// 统计卡片点击处理
const handleStatClick = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
  // 可选：添加提示消息
  //ElMessage.success(`已切换到${getTabName(tabName)}`)
}

// 获取标签页中文名称
const getTabName = (tabName) => {
  const tabMap = {
    all: '全部订单',
    pending: '待取车',
    active: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return tabMap[tabName] || tabName
}
// 状态定义
const loading = ref(false)
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(5)
const showDetailDialog = ref(false)
const currentOrder = ref(null)

// 模拟订单数据
const orders = ref([
  {
    id: 1,
    orderNo: 'ORD202403150001',
    createTime: '2024-03-15 14:30:22',
    payTime: '2024-03-15 14:35:18',
    status: 'pending', // pending: 待取车, active: 进行中, completed: 已完成, cancelled: 已取消
    statusType: 'warning',
    statusText: '待取车',
    carName: '比亚迪 秦 PLUS DM-i',
    carImage: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80',
    carSeats: 5,
    carGear: '自动',
    carEnergy: '混动',
    dailyPrice: 168,
    pickupTime: '2024-03-16 09:00',
    returnTime: '2024-03-18 18:00',
    days: 3,
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '杭州市西湖区文三路100号',
    rentalPrice: 504,
    insurancePrice: 50,
    servicePrice: 20,
    totalPrice: 574
  },
  {
    id: 2,
    orderNo: 'ORD202403140002',
    createTime: '2024-03-14 10:15:33',
    payTime: '2024-03-14 10:20:45',
    status: 'active',
    statusType: 'primary',
    statusText: '进行中',
    carName: '丰田 凯美瑞',
    carImage: 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80',
    carSeats: 5,
    carGear: '自动',
    carEnergy: '燃油',
    dailyPrice: 198,
    pickupTime: '2024-03-15 10:00',
    returnTime: '2024-03-17 10:00',
    days: 2,
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '杭州市上城区解放路88号',
    rentalPrice: 396,
    insurancePrice: 40,
    servicePrice: 20,
    totalPrice: 456
  },
  {
    id: 3,
    orderNo: 'ORD202403100003',
    createTime: '2024-03-10 09:45:12',
    payTime: '2024-03-10 09:50:30',
    status: 'completed',
    statusType: 'success',
    statusText: '已完成',
    carName: '特斯拉 Model 3',
    carImage: 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80',
    carSeats: 5,
    carGear: '自动',
    carEnergy: '纯电',
    dailyPrice: 268,
    pickupTime: '2024-03-11 09:00',
    returnTime: '2024-03-13 09:00',
    days: 2,
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '杭州市西湖区文三路100号',
    rentalPrice: 536,
    insurancePrice: 50,
    servicePrice: 20,
    totalPrice: 606
  },
  {
    id: 4,
    orderNo: 'ORD202403050004',
    createTime: '2024-03-05 16:20:08',
    payTime: '2024-03-05 16:25:15',
    status: 'cancelled',
    statusType: 'danger',
    statusText: '已取消',
    carName: '宝马 3系',
    carImage: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80',
    carSeats: 5,
    carGear: '自动',
    carEnergy: '燃油',
    dailyPrice: 329,
    pickupTime: '2024-03-06 14:00',
    returnTime: '2024-03-08 14:00',
    days: 2,
    pickupLocation: '杭州市西湖区文三路100号',
    returnLocation: '杭州市西湖区文三路100号',
    rentalPrice: 658,
    insurancePrice: 60,
    servicePrice: 20,
    totalPrice: 738
  }
])

// 统计数据
const pendingCount = computed(() => orders.value.filter((o) => o.status === 'pending').length)
const activeCount = computed(() => orders.value.filter((o) => o.status === 'active').length)
const completedCount = computed(() => orders.value.filter((o) => o.status === 'completed').length)

// 过滤后的订单
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter((order) => order.status === activeTab.value)
})

// 分页订单
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 订单操作
const handlePickup = (order) => {
  ElMessageBox.confirm('确认已取到车辆吗？', '确认取车', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(() => {
      order.status = 'active'
      order.statusType = 'primary'
      order.statusText = '进行中'
      ElMessage.success('取车成功，祝您用车愉快！')
    })
    .catch(() => {})
}

const handleReturn = (order) => {
  ElMessageBox.confirm('确认已归还车辆吗？', '申请还车', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  })
    .then(() => {
      order.status = 'completed'
      order.statusType = 'success'
      order.statusText = '已完成'
      ElMessage.success('还车成功，感谢您的使用！')
    })
    .catch(() => {})
}

const handleCancel = (order) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '再想想',
    type: 'warning'
  })
    .then(() => {
      order.status = 'cancelled'
      order.statusType = 'danger'
      order.statusText = '已取消'
      ElMessage.success('订单已取消')
    })
    .catch(() => {})
}

const handleReview = (order) => {
  ElMessage.info('评价功能开发中...')
}

const handleDetail = (order) => {
  currentOrder.value = order
  showDetailDialog.value = true
}

// 加载数据
const loadOrders = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(() => {
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
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
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

/* 响应式 */
@media (max-width: 900px) {
  .order-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .order-body {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .order-price {
    text-align: left;
    flex-direction: row;
    align-items: center;
    gap: 8px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-info {
    flex-direction: column;
    gap: 4px;
  }

  .car-info {
    flex-direction: column;
  }

  .car-image {
    width: 100%;
    height: 120px;
  }

  .order-actions {
    flex-wrap: wrap;
  }

  .order-actions .el-button {
    flex: 1 1 auto;
  }

  .order-stats {
    grid-template-columns: 1fr;
  }
}
</style>
