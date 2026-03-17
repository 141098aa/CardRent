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

          <!-- 时间选择 -->
          <el-form :model="rentForm" label-width="80px">
            <el-form-item label="取车时间">
              <el-date-picker
                v-model="rentForm.pickupDate"
                type="datetime"
                placeholder="选择取车时间"
                style="width: 30%"
                :disabled-date="disabledDate" />
            </el-form-item>
            <el-form-item label="还车时间">
              <el-date-picker
                v-model="rentForm.returnDate"
                type="datetime"
                placeholder="选择还车时间"
                style="width: 30%"
                :disabled-date="disabledDate" />
            </el-form-item>
          </el-form>

          <!-- 费用明细 - 显示动态调价 -->
          <div class="price-detail" v-if="rentDays > 0 && dynamicPrice">
            <!-- 基础租金 -->
            <div class="price-row">
              <span>基础租金 (¥{{ car?.price }} × {{ rentDays }}天)</span>
              <span>¥{{ dynamicPrice.baseRent }}</span>
            </div>

            <!-- 动态调价明细 -->
            <div v-if="dynamicPrice.priceAdjustments && parsedAdjustments.length > 0" class="adjustments-list">
              <div v-for="(adj, index) in parsedAdjustments" :key="index" class="adjustment-item">
                <div class="adjustment-info">
                  <span>{{ adj.name }}</span>
                  <el-tooltip :content="getAdjustmentTooltip(adj)" placement="top">
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

            <!-- 调整后租金 - 只有在有调价时才显示 -->
            <div v-if="hasAnyAdjustment" class="price-row adjusted-rent">
              <span>调整后租金</span>
              <span class="highlight-price">¥{{ dynamicPrice.dynamicRent }}</span>
            </div>

            <!-- 如果没有调价，直接显示基础租金作为最终租金 -->
            <!-- <div v-else class="price-row final-rent">
    <span>租金</span>
    <span class="normal-price">¥{{ dynamicPrice.baseRent }}</span>
  </div> -->

            <!-- 保险费用 -->
            <div class="price-row">
              <span>基础保险 (¥{{ car?.insurancePrice || 50 }}/天)</span>
              <span>¥{{ dynamicPrice.insuranceTotal }}</span>
            </div>

            <!-- 押金 -->
            <div class="price-row">
              <span>押金 (可退)</span>
              <span class="deposit">¥{{ dynamicPrice.deposit }}</span>
            </div>

            <!-- 总计 -->
            <div class="price-row total">
              <div class="total-label">
                <span>总计</span>
                <el-popover placement="top" :width="280" trigger="hover">
                  <template #reference>
                    <el-icon class="price-tip-icon"><QuestionFilled /></el-icon>
                  </template>
                  <div class="price-policy">
                    <h4>动态定价说明</h4>
                    <p>我们的价格会随市场情况动态调整，确保您在合适的时间获得最优价格：</p>
                    <ul>
                      <li v-if="hasWeekendAdjustment">周末溢价：周五至周日用车，价格上浮20%</li>
                      <li v-if="hasSeasonAdjustment">节假日溢价：国家法定节假日，价格上浮50%</li>
                      <li v-if="hasDiscountAdjustment">长租优惠：租期7天以上，享受9折优惠</li>
                    </ul>
                    <p v-if="!hasAnyAdjustment">·当前无任何动态调价</p>
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
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Star, StarFilled, Check, Location, Van, ShoppingCart, Warning } from '@element-plus/icons-vue'
import { getCarDetail, getHotRecommend, getRatingRecommend } from '@/utils/api/user/car'
import { createOrder, previewPrice } from '@/utils/api/user/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('detail')
const showRentDialog = ref(false)
const rentLoading = ref(false)

// 租车表单
const rentForm = reactive({
  pickupDate: '',
  returnDate: ''
})

// 计算租车天数
const rentDays = computed(() => {
  if (!rentForm.pickupDate || !rentForm.returnDate) return 0
  const diff = new Date(rentForm.returnDate) - new Date(rentForm.pickupDate)
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
})

// 租金小计
const rentalPrice = computed(() => {
  return (car.value?.price || 0) * rentDays.value
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
  return parsedAdjustments.value.some((adj) => adj.type === 'weekend')
})

const hasSeasonAdjustment = computed(() => {
  return parsedAdjustments.value.some((adj) => adj.type === 'season')
})

const hasDiscountAdjustment = computed(() => {
  return parsedAdjustments.value.some((adj) => adj.type === 'discount')
})

const hasAnyAdjustment = computed(() => {
  return parsedAdjustments.value.length > 0
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
      return '周五至周日用车，价格上浮20%'
    case 'season':
      return '国家法定节假日用车，价格上浮50%'
    case 'discount':
      return '长租优惠已自动应用'
    default:
      return adj.description
  }
}

// 获取调价项的样式类
const getAdjustmentBadgeClass = (adj) => {
  if (adj.type === 'discount') return 'discount-badge'
  return 'premium-badge'
}
// 总金额
const totalPrice = computed(() => {
  if (dynamicPrice.value) {
    return dynamicPrice.value.totalPrice
  }
  const rent = rentalPrice.value
  const insurance = insuranceTotal.value
  const deposit = car.value?.deposit || 0
  return rent + insurance + deposit
})

// 支付金额（不含押金）
const payAmount = computed(() => {
  return rentalPrice.value + insuranceTotal.value
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

      // 从localStorage获取收藏状态
      const favorites = JSON.parse(localStorage.getItem('user-favorites') || '[]')
      car.value.isFavorite = favorites.some((item) => item.id === car.value.id)
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载车辆详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
    // 滚动到顶部
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
const toggleFavorite = () => {
  if (!car.value) return

  car.value.isFavorite = !car.value.isFavorite

  // 更新收藏列表
  let favorites = JSON.parse(localStorage.getItem('user-favorites') || '[]')

  if (car.value.isFavorite) {
    if (!favorites.some((item) => item.id === car.value.id)) {
      favorites.push(car.value)
      ElMessage.success('已添加到收藏')
    }
  } else {
    favorites = favorites.filter((item) => item.id !== car.value.id)
    ElMessage.success('已取消收藏')
  }

  localStorage.setItem('user-favorites', JSON.stringify(favorites))
}

// 检查认证状态
const checkAuthStatus = () => {
  // 直接从 localStorage 获取最新数据
  const userInfo = JSON.parse(localStorage.getItem('system-user') || '{}')

  // 检查真实状态码：只有状态为1才算通过
  const realNamePassed = userInfo.realNameStatus === 1
  const driverLicensePassed = userInfo.driverLicenseStatus === 1

  return {
    realNameVerified: realNamePassed,
    driverLicenseVerified: driverLicensePassed,
    allVerified: realNamePassed && driverLicensePassed,
    // 添加状态码供前端显示
    realNameStatus: userInfo.realNameStatus,
    driverLicenseStatus: userInfo.driverLicenseStatus
  }
}

// 处理租车点击
const handleRent = () => {
  if (!checkLogin()) return

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

// 添加响应式变量存储动态价格
const dynamicPrice = ref(null)

// 价格预览监听
watch([() => rentForm.pickupDate, () => rentForm.returnDate], async () => {
  if (rentForm.pickupDate && rentForm.returnDate && rentDays.value >= 1 && car.value) {
    try {
      // 将日期转换为 ISO 字符串并去除时区信息
      const formatDate = (date) => {
        const d = new Date(date)
        // 格式化为 YYYY-MM-DDTHH:mm:ss
        return d.toISOString().split('.')[0] // 去掉毫秒和时区
      }

      const res = await previewPrice({
        carId: car.value.id,
        pickupTime: formatDate(rentForm.pickupDate),
        returnTime: formatDate(rentForm.returnDate)
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

    // 构建订单数据 - 后端会重新计算价格，所以不需要传价格字段
    const orderData = {
      userId: userInfo.id,
      carId: car.value.id,
      pickupTime: rentForm.pickupDate,
      returnTime: rentForm.returnDate,
      remark: '' // 可选的备注
    }

    const res = await createOrder(orderData)

    if (res.code === '200') {
      ElMessage.success('订单创建成功，请尽快支付')
      showRentDialog.value = false
      // 清空表单
      rentForm.pickupDate = ''
      rentForm.returnDate = ''
      // 跳转到订单列表
      router.push('/front/orders')
    } else {
      ElMessage.error(res.msg || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.message || '创建订单失败，请稍后重试')
  } finally {
    rentLoading.value = false
  }
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

.rent-days {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
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
</style>
