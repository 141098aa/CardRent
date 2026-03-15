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

    <!-- 租车对话框（保持不变） -->
    <el-dialog v-model="showRentDialog" title="确认租车" width="90%" max-width="400px" class="rent-dialog">
      <!-- ... 对话框内容保持不变 ... -->
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Star, StarFilled, Check, Location, Van, ShoppingCart, Warning } from '@element-plus/icons-vue'
import { getCarDetail, getHotRecommend, getRatingRecommend } from '@/utils/api/user/car'

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

// 计算总价
const totalPrice = computed(() => {
  return (car.value?.price || 0) * rentDays.value
})

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
  const realNameVerified = user.value.realNameVerified || false
  const driverLicenseVerified = user.value.driverLicenseVerified || false

  return {
    realNameVerified,
    driverLicenseVerified,
    allVerified: realNameVerified && driverLicenseVerified
  }
}

// 处理租车点击
const handleRent = () => {
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

// 确认租车
const confirmRent = () => {
  if (!rentForm.pickupDate || !rentForm.returnDate) {
    ElMessage.warning('请选择取车和还车时间')
    return
  }

  if (rentDays.value < 1) {
    ElMessage.warning('租车天数不能少于1天')
    return
  }

  rentLoading.value = true

  // 模拟租车成功（订单接口暂无）
  setTimeout(() => {
    rentLoading.value = false
    showRentDialog.value = false
    ElMessage.success('租车成功！请到"我的订单"查看')
    router.push('/front/orders')
  }, 1500)
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

/* 响应式 */
@media (max-width: 900px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .params-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .config-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .car-title {
    font-size: 24px;
  }

  .price-box {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .stock-info {
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .params-grid {
    grid-template-columns: 1fr;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .rating-overview {
    flex-direction: column;
    gap: 16px;
  }

  .review-header {
    flex-wrap: wrap;
  }
}
</style>
