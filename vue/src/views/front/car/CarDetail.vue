<template>
  <div class="car-detail-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="!car" class="error-state">
      <el-empty description="车辆不存在或已被下架" />
    </div>
    <div v-else class="car-detail">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/front/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/front/rental' }">在线租车</el-breadcrumb-item>
          <el-breadcrumb-item>{{ car.brand }} {{ car.model }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 车辆信息区域 -->
      <div class="detail-card">
        <div class="detail-header">
          <h1 class="car-title">{{ car.brand }} {{ car.model }} {{ car.year }}款</h1>
          <div class="car-tags">
            <el-tag v-if="car.tag" type="warning" size="small" effect="dark">{{ car.tag }}</el-tag>
            <el-tag v-if="car.status === 'available'" type="success" size="small" effect="plain">可租</el-tag>
            <el-tag v-else-if="car.status === 'rented'" type="danger" size="small" effect="plain">已租出</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">维修中</el-tag>
          </div>
        </div>

        <!-- 主要内容区：图片 + 基本信息 -->
        <div class="main-content">
          <!-- 左侧图片区 -->
          <div class="image-section">
            <el-image :src="car.image" :preview-src-list="[car.image]" fit="cover" class="main-image" />
          </div>
          <!-- 右侧信息区 -->
          <div class="info-section">
            <!-- 价格信息 -->
            <div class="price-box">
              <div class="price-info">
                <span class="price-label">日租价格</span>
                <span class="price-value">¥{{ car.price }}</span>
                <span class="price-unit">/天</span>
              </div>
              <div class="stock-info">
                <span class="stock-label">剩余可租</span>
                <span class="stock-value" :class="{ 'low-stock': car.stock <= 3 }">{{ car.stock }}辆</span>
              </div>
            </div>
            <!-- 车辆参数 -->
            <div class="params-grid">
              <div class="param-item">
                <span class="param-label">座位数</span>
                <span class="param-value">{{ car.seats }}座</span>
              </div>
              <div class="param-item">
                <span class="param-label">变速箱</span>
                <span class="param-value">{{ car.gear }}</span>
              </div>
              <div class="param-item">
                <span class="param-label">能源类型</span>
                <span class="param-value">{{ car.energy }}</span>
              </div>
              <div class="param-item">
                <span class="param-label">上牌年份</span>
                <span class="param-value">{{ car.year }}年</span>
              </div>
              <div class="param-item">
                <span class="param-label">里程</span>
                <span class="param-value">{{ car.mileage || '2.3' }}万公里</span>
              </div>
              <div class="param-item">
                <span class="param-label">排量</span>
                <span class="param-value">{{ car.displacement || '2.0T' }}</span>
              </div>
            </div>
            <!-- 特色配置 -->
            <div class="features-section">
              <h3 class="section-subtitle">特色配置</h3>
              <div class="features-list">
                <span v-for="feature in car.features" :key="feature" class="feature-item">
                  <el-icon><Check /></el-icon>
                  {{ feature }}
                </span>
              </div>
            </div>
            <!-- 取还车信息 -->
            <div class="location-info">
              <div class="info-row">
                <el-icon><Location /></el-icon>
                <span>取车地点：{{ car.pickupLocation || '杭州市西湖区文三路100号' }}</span>
              </div>
              <div class="info-row">
                <el-icon><Van /></el-icon>
                <span>还车地点：{{ car.returnLocation || '支持异地还车' }}</span>
              </div>
            </div>
            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button
                type="primary"
                class="rent-btn"
                :disabled="car.status !== 'available' || car.stock <= 0"
                @click="handleRent">
                <el-icon><ShoppingCart /></el-icon>
                立即租车
              </el-button>
              <el-button class="favorite-btn" :class="{ 'is-favorite': car.isFavorite }" @click="toggleFavorite">
                <el-icon><StarFilled v-if="car.isFavorite" /><Star v-else /></el-icon>
                {{ car.isFavorite ? '已收藏' : '收藏车辆' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 车辆详情和评价标签页 -->
      <div class="detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="车辆详情" name="detail">
            <div class="detail-content">
              <h3 class="tab-subtitle">车辆描述</h3>
              <p class="car-description">{{ car.description || '暂无描述' }}</p>

              <h3 class="tab-subtitle">车辆配置</h3>
              <div class="config-grid">
                <div v-for="config in car.configs || defaultConfigs" :key="config.name" class="config-item">
                  <span class="config-name">{{ config.name }}</span>
                  <span class="config-value">{{ config.value }}</span>
                </div>
              </div>

              <h3 class="tab-subtitle">租车须知</h3>
              <ul class="notice-list">
                <li v-for="(notice, index) in notices" :key="index">
                  <el-icon><Warning /></el-icon>
                  {{ notice }}
                </li>
              </ul>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`用户评价 (${car.reviewCount || 0})`" name="reviews">
            <div class="reviews-content">
              <!-- 评分概览 -->
              <div class="rating-overview">
                <div class="rating-score">
                  <span class="score-number">{{ car.rating || 5.0 }}</span>
                  <span class="score-max">/5.0</span>
                </div>
                <div class="rating-stats">
                  <div class="rating-stars">
                    <el-rate v-model="car.rating" disabled :colors="['#f59e0b', '#f59e0b', '#f59e0b']" />
                  </div>
                  <span class="rating-count">{{ car.reviewCount || 0 }}条评价</span>
                </div>
              </div>
              <!-- 评价列表（暂无接口，保留模拟数据） -->
              <div v-if="reviews.length > 0" class="reviews-list">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <img :src="review.avatar" class="review-avatar" />
                    <div class="review-user">
                      <span class="review-name">{{ review.userName }}</span>
                      <el-rate v-model="review.rating" disabled size="small" />
                    </div>
                    <span class="review-time">{{ review.time }}</span>
                  </div>
                  <p class="review-content">{{ review.content }}</p>
                </div>
              </div>
              <div v-else class="no-reviews">
                <el-empty description="暂无评价" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <!-- 推荐车型（使用真实接口） -->
      <div class="recommend-section" v-if="recommendCars.length > 0">
        <h2 class="section-title">推荐车型</h2>
        <div class="recommend-grid">
          <div v-for="item in recommendCars" :key="item.id" class="recommend-card" @click="goToCarDetail(item)">
            <div class="recommend-card__img">
              <img :src="item.image" :alt="item.model" />
            </div>
            <div class="recommend-card__body">
              <div class="recommend-card__name">{{ item.brand }} {{ item.model }}</div>
              <div class="recommend-card__price">¥{{ item.price }}/天</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 租车对话框 -  -->
    <el-dialog v-model="showRentDialog" title="确认租车" width="90%" max-width="500px" class="rent-dialog">
      <div class="rent-content">
        <div class="rent-summary">
          <!-- 车辆信息 -->
          <div class="rent-car-info">
            <img :src="car?.image" class="rent-car-image" />
            <div class="rent-car-detail">
              <span class="rent-car-name">{{ car?.brand }} {{ car?.model }}</span>
              <span class="rent-car-price">¥{{ car?.price }}/天</span>
            </div>
          </div>
          <!-- 在租车对话框的时间选择部分 -->
          <el-form :model="rentForm" label-width="80px">
            <el-form-item label="取车时间">
              <el-date-picker
                v-model="rentForm.pickupDate"
                type="datetime"
                placeholder="选择取车时间"
                style="width: 30%"
                :disabled-date="disabledPickupDate" />
            </el-form-item>
            <el-form-item label="还车时间">
              <el-date-picker
                v-model="rentForm.returnDate"
                type="datetime"
                placeholder="选择还车时间"
                style="width: 30%"
                :disabled-date="disabledReturnDate"
                :picker-options="returnDatePickerOptions" />
            </el-form-item>

            <!-- 添加错误提示 -->
            <div v-if="timeError" class="time-error">
              <el-icon><WarningFilled /></el-icon>
              <span>{{ timeErrorMessage }}</span>
            </div>
          </el-form>
          <!-- 费用明细 - 显示动态调价 -->
          <div class="price-detail" v-if="rentDays > 0 && dynamicPrice">
            <!-- 1. 先显示天数基础租金（原价，不含折扣） -->
            <div class="price-row">
              <span> {{ dynamicPrice.days }}天基础租金 (¥{{ car?.price }} × {{ dynamicPrice.days }}天) </span>
              <span>¥{{ daysBaseRent }}</span>
            </div>
            <!-- 2. 周末溢价（如果有） -->
            <div v-if="dynamicPrice.weekendPremium && dynamicPrice.weekendPremium > 0" class="price-row weekend">
              <div class="adjustment-info">
                <span>周末溢价</span>
                <el-tooltip content="周六至周日用车，价格上浮20%" placement="top">
                  <el-icon class="info-icon"><InfoFilled /></el-icon>
                </el-tooltip>
                <span class="adjustment-badge premium-badge">周末溢价20%</span>
              </div>
              <span class="price-up">+¥{{ dynamicPrice.weekendPremium }}</span>
            </div>
            <!-- 3. 节假日溢价（如果有） -->
            <div v-if="dynamicPrice.holidayPremium && dynamicPrice.holidayPremium > 0" class="price-row holiday">
              <div class="adjustment-info">
                <span>节假日溢价</span>
                <el-tooltip content="国家法定节假日用车，价格上浮30%~50%" placement="top">
                  <el-icon class="info-icon"><InfoFilled /></el-icon>
                </el-tooltip>
                <span class="adjustment-badge premium-badge">节假日溢价</span>
              </div>
              <span class="price-up">+¥{{ dynamicPrice.holidayPremium }}</span>
            </div>
            <!-- 4. 长租折扣（如果有） -->
            <div v-if="dynamicPrice.discountAmount && dynamicPrice.discountAmount < 0" class="price-row discount">
              <div class="adjustment-info">
                <span>长租折扣</span>
                <el-tooltip content="长租优惠已自动应用" placement="top">
                  <el-icon class="info-icon"><InfoFilled /></el-icon>
                </el-tooltip>
                <span class="adjustment-badge discount-badge">{{ getDiscountDescription(dynamicPrice) }}</span>
              </div>
              <span class="price-down">{{ dynamicPrice.discountAmount }}</span>
            </div>
            <!-- 5. 超时费用 -->
            <div v-if="dynamicPrice.hours > 0 && dynamicPrice.overtimeAmount > 0" class="adjustment-item overtime">
              <div class="adjustment-info">
                <span>超时费用</span>
                <el-tooltip
                  v-if="overtimeAdjustment"
                  :content="getAdjustmentTooltip(overtimeAdjustment)"
                  placement="top">
                  <el-icon class="info-icon"><InfoFilled /></el-icon>
                </el-tooltip>
                <span class="adjustment-badge premium-badge"
                  >超时{{ dynamicPrice.hours }}小时 (¥{{ dynamicPrice.overtimeInfo?.rate || hourlyRate }}/小时)</span
                >
              </div>
              <span class="price-up">+¥{{ dynamicPrice.overtimeAmount }}</span>
            </div>

            <!-- 6. 租金合计（直接显示最终结果） -->
            <div class="price-row total-rent">
              <span>租金合计</span>
              <span class="highlight-price">¥{{ dynamicPrice.dynamicRent }}</span>
            </div>
            <!-- 分隔线 -->
            <div class="divider"></div>

            <!-- 保险费用 -->
            <div class="price-row">
              <span>基础保险 (¥{{ car?.insurancePrice || 50 }}/天 × {{ dynamicPrice.days }}天)</span>
              <span>¥{{ dynamicPrice.insuranceTotal }}</span>
            </div>
            <!-- 押金 -->
            <div class="price-row">
              <span>押金 (可退)</span>
              <span class="deposit">¥{{ dynamicPrice.deposit }}</span>
            </div>
            <!-- 分隔线 -->
            <div class="divider"></div>

            <!-- 总计 -->
            <div class="price-row total">
              <div class="total-label">
                <span>应付总额</span>
                <el-popover placement="top" :width="280" trigger="hover">
                  <template #reference>
                    <el-icon class="price-tip-icon"><QuestionFilled /></el-icon>
                  </template>
                  <div class="price-policy">
                    <h4>动态定价说明</h4>
                    <p>我们的价格会随市场情况动态调整，确保您在合适的时间获得最优价格：</p>
                    <ul>
                      <li v-if="hasWeekendAdjustment">周末溢价：周六至周日用车，价格上浮20%</li>
                      <li v-if="hasSeasonAdjustment">节假日溢价：国家法定节假日，价格上浮30~50%</li>
                      <li v-if="hasDiscountAdjustment">长租优惠：租期7天以上，享受折扣优惠</li>
                      <li>超时费用：不足30分钟免费；30分钟-4小时按小时计费；超过4小时按1天计费</li>
                    </ul>
                    <p>所有价格调整都会在订单确认页明细展示，请您放心预订。</p>
                  </div>
                </el-popover>
              </div>
              <span class="total-price">¥{{ dynamicPrice.totalPrice }}</span>
            </div>
            <!-- 押金说明 -->
            <div class="price-note">
              <el-icon><InfoFilled /></el-icon>
              <span>押金将在还车后3-5个工作日内原路退回</span>
            </div>
            <!-- 价格提示 - 只在有调价时显示 -->
            <div class="price-note" v-if="hasAnyAdjustment">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ getPriceNoteText }}</span>
            </div>
          </div>
          <!-- 未选择时间时的提示 -->
          <div v-else-if="rentDays === 0" class="price-tip-message">请选择取车和还车时间查看费用明细</div>
          <!-- 备注信息 -->
          <div class="remark-section" v-if="showRentDialog">
            <div class="remark-header">
              <span class="remark-label">备注信息</span>
              <span class="remark-tip">（选填，可输入特殊要求或说明）</span>
            </div>
            <el-input
              v-model="rentForm.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注信息，例如：需要儿童安全座椅、接待服务等"
              maxlength="300"
              show-word-limit
              style="width: 40%"
              class="remark-input" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRentDialog = false">取 消</el-button>
          <el-button type="primary" @click="confirmRent" :loading="rentLoading">确认租车</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  <!-- 使用支付弹窗组件 -->
  <PaymentDialog
    v-model:visible="showPaymentDialog"
    :amount="dynamicPrice?.totalPrice || totalPrice"
    :order-id="currentOrderId"
    @pay="handlePayment"
    @success="handlePaymentSuccess"
    @cancel="handlePaymentCancel" />
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Star,
  StarFilled,
  Check,
  Location,
  Van,
  ShoppingCart,
  Warning,
  WarningFilled
} from '@element-plus/icons-vue'
import { getCarDetail, getHotRecommend, getRatingRecommend } from '@/utils/api/user/car'
import { createOrder, previewPrice } from '@/utils/api/user/order'
import { getUserById } from '@/utils/api/user/profile'
import { userFavoriteApi } from '@/utils/api'
import PaymentDialog from '@/components/PaymentDialog.vue'
import { userOrderApi } from '@/utils/api'

const emit = defineEmits(['updateUser'])
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('detail')
const showRentDialog = ref(false)
const rentLoading = ref(false)

// 支付弹窗控制
const showPaymentDialog = ref(false)
const currentOrderId = ref(null)

// 时间错误提示
const timeError = ref(false)
const timeErrorMessage = ref('')

// 租车表单
const rentForm = reactive({
  pickupDate: '',
  returnDate: '',
  remark: ''
})

// 计算租车天数
const rentDays = computed(() => {
  if (!rentForm.pickupDate || !rentForm.returnDate) return 0
  const diff = new Date(rentForm.returnDate) - new Date(rentForm.pickupDate)
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
})

// 计算小时费率
const hourlyRate = computed(() => {
  if (!dynamicPrice.value) return 0
  return (dynamicPrice.value.dailyPrice / 24).toFixed(2)
})
// 获取折扣描述
const getDiscountDescription = (price) => {
  if (!price.discountAmount || price.discountAmount >= 0) return ''

  // 计算折扣百分比
  const discountPercent = Math.abs(Math.round((price.discountAmount / (price.days * price.dailyPrice)) * 100))
  return `长租${discountPercent}%优惠`
}

// 计算天数基础租金（原价）
const daysBaseRent = computed(() => {
  if (!dynamicPrice.value) return 0
  return (dynamicPrice.value.days * dynamicPrice.value.dailyPrice).toFixed(2)
})

// 保险费用
const insuranceTotal = computed(() => {
  return (car.value?.insurancePrice || 0) * rentDays.value
})
// 解析调价明细
const parsedAdjustments = computed(() => {
  if (!dynamicPrice.value?.priceAdjustments) return []
  try {
    return JSON.parse(dynamicPrice.value.priceAdjustments)
  } catch (e) {
    console.error('解析调价明细失败', e)
    return []
  }
})

// 判断是否有各种调价
const hasWeekendAdjustment = computed(() => {
  return dynamicPrice.value?.weekendPremium && dynamicPrice.value.weekendPremium > 0
})

const hasSeasonAdjustment = computed(() => {
  return dynamicPrice.value?.holidayPremium && dynamicPrice.value.holidayPremium > 0
})

const hasDiscountAdjustment = computed(() => {
  return dynamicPrice.value?.discountAmount && dynamicPrice.value.discountAmount < 0
})

const hasAnyAdjustment = computed(() => {
  return (
    hasWeekendAdjustment.value ||
    hasSeasonAdjustment.value ||
    hasDiscountAdjustment.value ||
    (dynamicPrice.value?.overtimeAmount && dynamicPrice.value.overtimeAmount > 0)
  )
})

// 获取调价提示文本
const getPriceNoteText = computed(() => {
  const texts = []
  if (hasWeekendAdjustment.value) texts.push('周末用车价格上浮20%')
  if (hasSeasonAdjustment.value) texts.push('节假日用车价格上浮50%')
  if (hasDiscountAdjustment.value) texts.push('长租优惠已自动应用')

  if (texts.length === 0) return ''
  if (texts.length === 1) return `价格说明：${texts[0]}`
  return `价格说明：${texts.join('，')}`
})

// 获取调价项的提示信息
const getAdjustmentTooltip = (adj) => {
  switch (adj.type) {
    case 'weekend':
      return '周末溢价：周六至周日用车，价格上浮20%'
    case 'season':
      return '节假日溢价：国家法定节假日用车，价格上浮30%~50%'
    case 'discount':
      // 长租优惠详细说明
      if (adj.days) {
        return `长租优惠：租期 ${adj.days} 天，享受 ${adj.description}，已自动应用`
      } else if (adj.factor) {
        const discountPercent = Math.round((1 - adj.factor) * 100)
        return `长租优惠：租期越长优惠越多，当前享受 ${discountPercent}% 折扣`
      } else {
        return '长租优惠：租期7天以上享受9折优惠，14天以上85折，30天以上8折'
      }
    case 'overtime':
      // 根据超时信息动态生成说明
      if (adj.hours) {
        return `超时费用：超时 ${adj.hours} 小时，按小时计费 (¥${adj.rate}/小时)`
      } else if (adj.description && adj.description.includes('超过4小时')) {
        return '超时费用：超过4小时按1天计费，包含当天保险'
      } else {
        return '超时费用：不足30分钟免费；30分钟至4小时按小时计费(日租金/24×小时)；超过4小时按1天计费(含保险)'
      }
    default:
      return adj.description
  }
}
// 获取超时调价项
const overtimeAdjustment = computed(() => {
  return parsedAdjustments.value.find((adj) => adj.type === 'overtime')
})

// 总金额
const totalPrice = computed(() => {
  if (dynamicPrice.value) {
    return dynamicPrice.value.totalPrice
  }
  const rent = (car.value?.price || 0) * rentDays.value
  const insurance = insuranceTotal.value
  const deposit = car.value?.deposit || 0
  return rent + insurance + deposit
})

// 禁用过去的日期
const disabledDate = (time) => {
  // 获取今天的开始时间（00:00:00）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  // 返回 true 表示禁用，false 表示可选
  return time.getTime() < today.getTime()
}

// 车辆数据
const car = ref(null)

// 评价数据（暂无接口，保留模拟数据）
const reviews = ref([
  {
    id: 1,
    userName: '张先生',
    avatar: 'https://randomuser.me/api/portraits/men/1.jpg',
    rating: 5,
    content: '车况很好，取还车方便，服务态度很好，下次还会选择。',
    time: '2024-01-15'
  },
  {
    id: 2,
    userName: '李女士',
    avatar: 'https://randomuser.me/api/portraits/women/2.jpg',
    rating: 4.5,
    content: '车辆很新，驾驶体验不错，就是取车地点有点远。',
    time: '2024-01-10'
  },
  {
    id: 3,
    userName: '王先生',
    avatar: 'https://randomuser.me/api/portraits/men/3.jpg',
    rating: 5,
    content: '性价比很高，服务很周到，推荐！',
    time: '2024-01-05'
  }
])

// 默认配置（当car.configs为空时使用）
const defaultConfigs = [
  { name: '驱动方式', value: '前驱' },
  { name: '燃油标号', value: '95号' },
  { name: '油箱容量', value: '55L' },
  { name: '百公里油耗', value: '7.8L' },
  { name: '最大功率', value: '150kW' },
  { name: '轴距', value: '2870mm' }
]

// 租车须知
const notices = [
  '取车时请携带本人身份证和驾驶证',
  '车辆需在约定时间归还，超时将收取额外费用',
  '请保持车辆清洁，如有损坏需照价赔偿',
  '支持芝麻信用免押金租车',
  '24小时道路救援服务'
]

// 推荐车型（使用真实数据）
const recommendCars = ref([])

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))

// 加载车辆详情
const loadCarDetail = async (carId) => {
  loading.value = true
  try {
    const res = await getCarDetail(carId)
    if (res.code === '200') {
      car.value = res.data

      // 从后端检查收藏状态（如果用户已登录）
      if (user.value?.id) {
        const isFavorite = await checkFavoriteStatus(carId)
        car.value.isFavorite = isFavorite
      } else {
        car.value.isFavorite = false
      }
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载车辆详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 加载推荐车型
const loadRecommendCars = async () => {
  try {
    // 同时请求热门推荐和评分推荐，合并去重
    const [hotRes, ratingRes] = await Promise.all([getHotRecommend(4), getRatingRecommend(4)])

    const hotList = hotRes.code === '200' ? hotRes.data || [] : []
    const ratingList = ratingRes.code === '200' ? ratingRes.data || [] : []

    // 合并去重，最多显示4个
    const merged = [...hotList, ...ratingList]
    const unique = merged.filter((item, index, self) => index === self.findIndex((t) => t.id === item.id)).slice(0, 4)

    recommendCars.value = unique
  } catch (error) {
    console.error('加载推荐车型失败:', error)
    // 失败时不显示推荐
    recommendCars.value = []
  }
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadCarDetail(parseInt(newId))
      // 切换车辆时重新加载推荐
      loadRecommendCars()
    }
  },
  { immediate: true }
)

// 初始化
onMounted(() => {
  loadRecommendCars() // 加载推荐车型
})

// 切换收藏
const toggleFavorite = async () => {
  if (!car.value) return
  if (!checkLogin()) return

  try {
    if (car.value.isFavorite) {
      // 取消收藏
      const res = await userFavoriteApi.removeFavorite(car.value.id, 'car')
      if (res.code === '200') {
        car.value.isFavorite = false
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(res.msg || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const res = await userFavoriteApi.addFavorite({
        targetId: car.value.id,
        targetType: 'car'
      })
      if (res.code === '200') {
        car.value.isFavorite = true
        ElMessage.success('已添加到收藏')
      } else {
        ElMessage.error(res.msg || '添加收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error.response?.data?.msg || error.message || '操作失败，请稍后重试')
  }
}
// 检查车辆是否已被收藏
const checkFavoriteStatus = async (carId) => {
  if (!user.value?.id) return false

  try {
    const res = await userFavoriteApi.checkFavorite(carId, 'car')
    if (res.code === '200') {
      return res.data // 返回布尔值
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
  return false
}

// 检查认证状态
const checkAuthStatus = () => {
  // 直接从 localStorage 获取最新数据
  const userInfo = JSON.parse(localStorage.getItem('system-user') || '{}')

  // 直接使用 realNameVerified 和 driverLicenseVerified 字段
  // 值为 1 表示已认证通过
  const realNamePassed = userInfo.realNameVerified === 1
  const driverLicensePassed = userInfo.driverLicenseVerified === 1

  return {
    realNameVerified: realNamePassed,
    driverLicenseVerified: driverLicensePassed,
    allVerified: realNamePassed && driverLicensePassed,
    // 也保留原始值供其他用途
    realNameStatus: userInfo.realNameVerified,
    driverLicenseStatus: userInfo.driverLicenseVerified
  }
}

// 处理租车点击
const handleRent = async () => {
  if (!checkLogin()) return

  // 先刷新用户认证状态
  await refreshUserAuth()

  const auth = checkAuthStatus()

  if (!auth.allVerified) {
    let message = ''
    let highlight = ''

    if (!auth.realNameVerified && !auth.driverLicenseVerified) {
      message = '您还未完成实名认证和驾驶证认证，无法租车'
    } else if (!auth.realNameVerified) {
      message = '您还未完成实名认证，无法租车'
      highlight = 'realName'
    } else if (!auth.driverLicenseVerified) {
      message = '您还未完成驾驶证认证，无法租车'
      highlight = 'driverLicense'
    }

    ElMessageBox.confirm(`${message}，是否前往认证？`, '提示', {
      confirmButtonText: '去认证',
      cancelButtonText: '暂不',
      type: 'warning'
    })
      .then(() => {
        router.push({
          path: '/front/person',
          query: {
            tab: 'auth',
            highlight: highlight
          }
        })
      })
      .catch(() => {})

    return
  }

  // 认证通过，显示租车对话框
  showRentDialog.value = true
}
// 刷新用户认证状态
const refreshUserAuth = async () => {
  try {
    // 从后端获取最新用户信息
    const res = await getUserById(user.value.id)
    if (res.code === '200') {
      // 更新 localStorage 和 user 对象
      const updatedUser = res.data
      localStorage.setItem('system-user', JSON.stringify(updatedUser))
      user.value = updatedUser
      console.log('用户认证状态已刷新:', updatedUser.realNameVerified, updatedUser.driverLicenseVerified)
    }
  } catch (error) {
    console.error('刷新用户信息失败:', error)
  }
}
// 添加响应式变量存储动态价格
const dynamicPrice = ref(null)
// 监听时间变化，实时验证
watch(
  [() => rentForm.pickupDate, () => rentForm.returnDate],
  () => {
    if (rentForm.pickupDate && rentForm.returnDate) {
      if (new Date(rentForm.returnDate) < new Date(rentForm.pickupDate)) {
        timeError.value = true
        timeErrorMessage.value = '还车时间不能早于取车时间，请重新选择'
      } else {
        timeError.value = false
      }
    } else {
      timeError.value = false
    }
  },
  { immediate: true }
)

// 禁用取车时间：不能选过去的时间
const disabledPickupDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

// 禁用还车时间：不能选过去的时间 + 不能早于取车时间
const disabledReturnDate = (time) => {
  // 不能选过去的时间
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (time.getTime() < today.getTime()) return true

  // 如果已选择取车时间，则不能选早于取车时间
  if (rentForm.pickupDate) {
    const pickup = new Date(rentForm.pickupDate)
    pickup.setHours(0, 0, 0, 0)
    return time.getTime() < pickup.getTime()
  }

  return false
}

// 还车时间选择器选项（可选）
const returnDatePickerOptions = {
  firstDayOfWeek: 1,
  shortcuts: [
    {
      text: '当天',
      onClick(picker) {
        if (!rentForm.pickupDate) {
          ElMessage.warning('请先选择取车时间')
          return
        }
        const pickup = new Date(rentForm.pickupDate)
        picker.$emit('pick', pickup)
      }
    },
    {
      text: '次日',
      onClick(picker) {
        if (!rentForm.pickupDate) {
          ElMessage.warning('请先选择取车时间')
          return
        }
        const nextDay = new Date(rentForm.pickupDate)
        nextDay.setDate(nextDay.getDate() + 1)
        picker.$emit('pick', nextDay)
      }
    }
  ]
}
// 价格预览监听
watch([() => rentForm.pickupDate, () => rentForm.returnDate], async () => {
  if (rentForm.pickupDate && rentForm.returnDate && rentDays.value >= 1 && car.value) {
    try {
      // 将日期转换为本地时间字符串（不带时区）
      const formatLocalDate = (date) => {
        const d = new Date(date)
        // 解决时区偏移问题
        const timezoneOffset = d.getTimezoneOffset() * 60000
        const localDate = new Date(d.getTime() - timezoneOffset)
        return localDate.toISOString().slice(0, 19)
      }

      const res = await previewPrice({
        carId: car.value.id,
        pickupTime: formatLocalDate(rentForm.pickupDate),
        returnTime: formatLocalDate(rentForm.returnDate)
      })
      if (res.code === '200') {
        dynamicPrice.value = res.data
      }
    } catch (error) {
      console.error('获取价格预览失败:', error)
    }
  } else {
    dynamicPrice.value = null
  }
})

// 确认租车
const confirmRent = async () => {
  if (!rentForm.pickupDate || !rentForm.returnDate) {
    ElMessage.warning('请选择取车和还车时间')
    return
  }
  // 双重验证
  if (new Date(rentForm.returnDate) < new Date(rentForm.pickupDate)) {
    ElMessage.error('还车时间不能早于取车时间')
    return
  }

  if (rentDays.value < 1) {
    ElMessage.warning('租车天数不能少于1天')
    return
  }

  rentLoading.value = true

  try {
    // 从 localStorage 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('system-user') || '{}')
    if (!userInfo.id) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }

    // 使用和价格预览相同的格式化方法处理时间
    const formatLocalDate = (date) => {
      const d = new Date(date)
      // 解决时区偏移问题
      const timezoneOffset = d.getTimezoneOffset() * 60000
      const localDate = new Date(d.getTime() - timezoneOffset)
      return localDate.toISOString().slice(0, 19)
    }

    // 构建订单数据
    const orderData = {
      userId: userInfo.id,
      carId: car.value.id,
      pickupTime: formatLocalDate(rentForm.pickupDate), // 使用格式化后的时间
      returnTime: formatLocalDate(rentForm.returnDate), // 使用格式化后的时间
      remark: rentForm.remark || '' // 传递备注，默认为空字符串
    }

    const createRes = await createOrder(orderData)
    if (createRes.code !== '200') {
      ElMessage.error(createRes.msg || '创建订单失败')
      return
    }

    // 保存订单ID，打开支付弹窗
    currentOrderId.value = createRes.data.id
    //showRentDialog.value = false
    showPaymentDialog.value = true
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.message || '创建订单失败，请稍后重试')
  } finally {
    rentLoading.value = false
  }
}
// 支付处理
const handlePayment = async (params) => {
  try {
    const res = await userOrderApi.payOrder({
      id: params.orderId,
      paymentMethod: 'wallet',
      paymentPassword: params.paymentPassword
    })

    if (res.code === '200') {
      // 更新用户信息
      if (res.data) {
        localStorage.setItem('system-user', JSON.stringify(res.data))
        user.value = res.data
      }
    } else {
      ElMessage.error(res.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败')
  }
}
const refreshLockStatus = async () => {}
const handlePaymentSuccess = () => {
  // 支付成功后的处理
  showPaymentDialog.value = false
  showRentDialog.value = false // 关闭租车对话框

  // 刷新用户余额
  const userInfo = JSON.parse(localStorage.getItem('system-user') || '{}')
  getUserById(userInfo.id).then((res) => {
    if (res.code === '200') {
      localStorage.setItem('system-user', JSON.stringify(res.data))
      user.value = res.data
    }
  })

  // 清空表单
  rentForm.pickupDate = ''
  rentForm.returnDate = ''
  rentForm.remark = ''

  router.push('/front/orders')
}

const handlePaymentCancel = () => {
  // 支付取消后的处理
  showPaymentDialog.value = false
}
// 添加用户登录检查
const checkLogin = () => {
  if (!user.value?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 跳转到车辆详情
const goToCarDetail = (car) => {
  router.push(`/front/car/${car.id}`)
}
</script>

<style scoped>
.car-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
}

/* 面包屑 */
.breadcrumb {
  margin-bottom: 20px;
}

/* 加载和错误状态 */
.loading-state,
.error-state {
  padding: 60px 0;
}

/* 详情卡片 */
.detail-card {
  background: white;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #edf2f7;
  margin-bottom: 30px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.car-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0;
}

.car-tags {
  display: flex;
  gap: 8px;
}

/* 主要内容区 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

/* 图片区 */
.image-section {
  border-radius: 16px;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

/* 信息区 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.price-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #fef9e7, #fff7e0);
  border-radius: 16px;
  border: 1px solid #fde6c4;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-label {
  font-size: 14px;
  color: #92400e;
}

.price-value {
  font-size: 36px;
  font-weight: 800;
  color: #b45309;
}

.price-unit {
  font-size: 14px;
  color: #92400e;
}

.stock-info {
  text-align: right;
}

.stock-label {
  display: block;
  font-size: 13px;
  color: #92400e;
  margin-bottom: 4px;
}

.stock-value {
  font-size: 24px;
  font-weight: 700;
  color: #059669;
}

.stock-value.low-stock {
  color: #dc2626;
}

/* 参数网格 */
.params-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
}

.param-item {
  text-align: center;
}

.param-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.param-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

/* 特色配置 */
.features-section {
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
}

.section-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 12px;
}

.features-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.feature-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: white;
  border-radius: 30px;
  font-size: 13px;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.feature-item .el-icon {
  color: #c8a165;
  font-size: 14px;
}

/* 位置信息 */
.location-info {
  padding: 16px;
  background: #f1f5f9;
  border-radius: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 14px;
}

.info-row:first-child {
  margin-bottom: 8px;
}

.info-row .el-icon {
  color: #c8a165;
  font-size: 16px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.rent-btn {
  flex: 2;
  height: 56px;
  border-radius: 40px;
  background: linear-gradient(135deg, #c8a165, #b28b4f);
  border: none;
  font-size: 18px;
  font-weight: 600;
}

.rent-btn:hover {
  background: linear-gradient(135deg, #b28b4f, #9e7a45);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.3);
}

.favorite-btn {
  flex: 1;
  height: 56px;
  border-radius: 40px;
  border: 1px solid #e2e8f0;
  background: white;
  font-size: 16px;
  color: #64748b;
}

.favorite-btn:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.favorite-btn.is-favorite {
  background: #fef2f2;
  border-color: #f56c6c;
  color: #f56c6c;
}

/* 标签页 */
.detail-tabs {
  background: white;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #edf2f7;
  margin-bottom: 30px;
}

.tab-subtitle {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 24px 0 16px;
}

.tab-subtitle:first-child {
  margin-top: 0;
}

.car-description {
  font-size: 16px;
  color: #4a5568;
  line-height: 1.8;
  margin-bottom: 24px;
}

/* 配置网格 */
.config-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.config-name {
  font-size: 14px;
  color: #64748b;
}

.config-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
}

/* 须知列表 */
.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #475569;
}

.notice-list li .el-icon {
  color: #c8a165;
  font-size: 16px;
}

/* 评价区域 */
.rating-overview {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 16px;
  margin-bottom: 24px;
}

.rating-score {
  text-align: center;
}

.score-number {
  font-size: 48px;
  font-weight: 800;
  color: #f59e0b;
}

.score-max {
  font-size: 18px;
  color: #94a3b8;
}

.rating-stats {
  flex: 1;
}

.rating-stars {
  margin-bottom: 4px;
}

.rating-count {
  font-size: 14px;
  color: #64748b;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #edf2f7;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.review-user {
  flex: 1;
}

.review-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 2px;
}

.review-time {
  font-size: 12px;
  color: #94a3b8;
}

.review-content {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  margin: 0;
}

/* 推荐车型 */
.recommend-section {
  background: white;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #edf2f7;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 20px;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #c8a165;
  border-radius: 2px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.04);
  border-color: #c8a165;
}

.recommend-card__img {
  height: 120px;
  overflow: hidden;
}

.recommend-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.recommend-card:hover .recommend-card__img img {
  transform: scale(1.05);
}

.recommend-card__body {
  padding: 12px;
}

.recommend-card__name {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 4px;
}

.recommend-card__price {
  font-size: 16px;
  font-weight: 700;
  color: #c8a165;
}

/* 租车对话框 */
.rent-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.rent-car-info {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 20px;
}

.rent-car-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.rent-car-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.rent-car-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 4px;
}

.rent-car-price {
  font-size: 18px;
  font-weight: 700;
  color: #c8a165;
}

.total-price {
  font-size: 20px;
  font-weight: 800;
  color: #c8a165;
}

/* 费用明细样式 */
.price-detail {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
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

.price-row .deposit {
  color: #409eff;
  font-weight: 500;
}

.price-note {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 12px;
  color: #64748b;
}

.price-note .el-icon {
  color: #c8a165;
  font-size: 14px;
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

.price-up {
  color: #ef4444;
  font-weight: 500;
}

.price-down {
  color: #10b981;
  font-weight: 500;
}

.total-label {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-tip-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.price-tip-icon:hover {
  color: #409eff;
}

.price-tip-message {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-size: 14px;
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

.discounted-rent {
  font-weight: 600;
  color: #1e2a3a;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.subtotal-price {
  color: #409eff;
  font-size: 16px;
}

.overtime {
  color: #f56c6c;
}

.total-rent {
  font-weight: 600;
  font-size: 16px;
  border-top: 1px solid #e2e8f0;
  padding-top: 8px;
  margin-top: 4px;
}

.highlight-price {
  color: #c8a165;
  font-size: 18px;
}

.divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #e2e8f0, transparent);
  margin: 8px 0;
}
.weekend .price-up,
.holiday .price-up {
  color: #f56c6c;
  font-weight: 500;
}
.overtime .price-up {
  color: #f56c6c;
  font-weight: 500;
}

.overtime .adjustment-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.overtime .info-icon {
  font-size: 14px;
  color: #94a3b8;
  cursor: help;
}

.overtime .info-icon:hover {
  color: #409eff;
}
/* 错误提示样式 */
.time-error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 8px 0 0;
  padding: 8px 12px;
  background-color: #fef2f2;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
}

.time-error .el-icon {
  font-size: 16px;
}

.remark-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.remark-header {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.remark-label {
  font-weight: 600;
  color: #1e2a3a;
  font-size: 14px;
}

.remark-tip {
  font-size: 12px;
  color: #94a3b8;
}

.remark-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #e2e8f0;
  font-size: 14px;
}

.remark-input :deep(.el-textarea__inner:focus) {
  border-color: #c8a165;
  box-shadow: 0 0 0 2px rgba(200, 161, 101, 0.1);
}

.remark-input :deep(.el-input__count) {
  color: #94a3b8;
  font-size: 12px;
}
</style>
