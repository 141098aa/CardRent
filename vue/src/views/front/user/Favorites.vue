<template>
  <div class="favorites-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <p class="page-subtitle">收藏您心仪的车型，随时查看</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="favorites-stats">
        共收藏 <span class="stats-number">{{ currentFavorites.length }}</span> 个车型
      </div>
      <div class="action-buttons">
        <el-button
          v-if="currentFavorites.length > 0"
          type="danger"
          plain
          size="small"
          class="clear-btn"
          @click="confirmClearCurrent">
          <el-icon><Delete /></el-icon>
          清空当前分类
        </el-button>
      </div>
    </div>

    <!-- 收藏列表 - 汽车 -->
    <div class="favorites-list">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="carFavorites.length === 0" class="empty-state">
        <el-empty description="暂无收藏车型">
          <template #extra>
            <el-button type="primary" class="go-rental-btn" @click="goToRental">去挑选车型</el-button>
          </template>
        </el-empty>
      </div>

      <div v-else class="favorites-grid">
        <div v-for="car in paginatedCarFavorites" :key="car.id" class="favorite-card" @click="goToCarDetail(car)">
          <!-- 车辆图片 -->
          <div class="favorite-card__img">
            <img :src="car.image" :alt="car.name" />
            <div class="favorite-card__badge" v-if="car.tag">{{ car.tag }}</div>
            <div class="favorite-card__remove" @click.stop="removeFromFavorites(car, 'cars')">
              <el-icon><Close /></el-icon>
            </div>
          </div>

          <!-- 车辆信息 -->
          <div class="favorite-card__body">
            <div class="favorite-card__header">
              <div class="favorite-card__name">{{ car.brand }} · {{ car.model }}</div>
              <div class="favorite-card__rating">
                <el-icon><StarFilled /></el-icon>
                <span>{{ car.rating }}</span>
              </div>
            </div>

            <div class="favorite-card__specs">
              <span class="spec-item">{{ car.seats }}座</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.gear }}</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.energy }}</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.year }}年</span>
            </div>

            <div class="favorite-card__features">
              <template v-if="car.features && car.features.length">
                <span v-for="feature in car.features.slice(0, 3)" :key="feature" class="feature-tag">
                  {{ feature }}
                </span>
              </template>
            </div>

            <div class="favorite-card__footer">
              <div class="favorite-card__price">
                <span class="price">¥{{ car.price }}</span>
                <span class="unit">/天起</span>
              </div>
              <div class="favorite-card__actions">
                <el-button size="small" class="rent-now-btn" @click.stop="quickRent(car)">立即租车</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[6, 9, 12]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 推荐车型 -->
    <div class="recommend-section" v-if="carFavorites.length > 0">
      <h2 class="section-title">猜你喜欢</h2>
      <div class="recommend-grid">
        <div v-for="car in recommendedCars" :key="car.id" class="recommend-card" @click="goToCarDetail(car)">
          <div class="recommend-card__img">
            <img :src="car.image" :alt="car.name" />
          </div>
          <div class="recommend-card__body">
            <div class="recommend-card__name">{{ car.brand }} {{ car.model }}</div>
            <div class="recommend-card__price">¥{{ car.price }}/天</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userFavoriteApi } from '@/utils/api'
import { userCarApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)

// 检查用户是否登录
const checkLogin = () => {
  const userStr = localStorage.getItem('system-user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 加载推荐车型（使用热门推荐接口）
const loadRecommendedCars = async () => {
  try {
    const res = await userCarApi.getHotRecommend(3)
    if (res.code === '200') {
      recommendedCars.value = res.data || []
    }
  } catch (error) {
    console.error('加载推荐车型失败:', error)
    // 失败时不显示推荐
    recommendedCars.value = []
  }
}
// 收藏列表
const carFavorites = ref([])

// 当前显示的收藏
const currentFavorites = computed(() => {
  return carFavorites.value
})

// 分页数据
const paginatedCarFavorites = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return carFavorites.value.slice(start, end)
})

// 推荐车型
const recommendedCars = ref([])

// 加载收藏数据
const loadFavorites = async () => {
  loading.value = true
  try {
    // 获取车辆收藏列表 - 使用当前页码和每页大小
    const carRes = await userFavoriteApi.getFavoriteList({
      targetType: 'car',
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })

    if (carRes.code === '200') {
      // 后端返回的是 PageInfo 格式，包含 list 和 total
      carFavorites.value = carRes.data?.list || []
      total.value = carRes.data?.total || 0
    } else {
      ElMessage.error(carRes.msg || '加载收藏失败')
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 从收藏中移除
const removeFromFavorites = async (item) => {
  try {
    const res = await userFavoriteApi.removeFavorite(item.id, 'car')
    if (res.code === '200') {
      carFavorites.value = carFavorites.value.filter((i) => i.id !== item.id)
      total.value = carFavorites.value.length // 更新总数
      ElMessage.success('已从收藏中移除')
    } else {
      ElMessage.error(res.msg || '移除收藏失败')
    }
  } catch (error) {
    console.error('移除收藏失败:', error)
    ElMessage.error(error.message || '移除收藏失败')
  }
}

// 清空当前分类收藏
const confirmClearCurrent = () => {
  ElMessageBox.confirm('确定要清空所有收藏吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const promises = carFavorites.value.map((item) => userFavoriteApi.removeFavorite(item.id, 'car'))
        await Promise.all(promises)
        carFavorites.value = []
        total.value = 0 // 更新总数
        ElMessage.success('已清空收藏')
      } catch (error) {
        console.error('清空收藏失败:', error)
        ElMessage.error(error.message || '清空收藏失败')
      }
    })
    .catch(() => {})
}

// 立即租车
const quickRent = (car) => {
  ElMessage.success(`正在预订 ${car.brand} ${car.model}`)
  router.push(`/front/car/${car.id}`)
}

// 查看详情
const goToCarDetail = (car) => {
  router.push(`/front/car/${car.id}`)
}

// 跳转页面
const goToRental = () => {
  router.push('/front/rental')
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 添加 watch 监听页码和标签页变化
watch(
  [currentPage, pageSize],
  () => {
    if (checkLogin()) {
      loadFavorites()
    }
  },
  { immediate: true }
)

// 处理首次加载
onMounted(() => {
  if (checkLogin()) {
    loadRecommendedCars()
  }
})
</script>

<style scoped>
.favorites-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  background: #f8fafc;
  min-height: calc(100vh - 70px);
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

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 16px;
  border: 1px solid #edf2f7;
}

.favorites-stats {
  font-size: 15px;
  color: #475569;
}

.stats-number {
  font-size: 20px;
  font-weight: 700;
  color: #c8a165;
  margin: 0 4px;
}

.clear-btn {
  border-radius: 30px;
  padding: 8px 16px;
  border-color: #fecaca;
  color: #ef4444;
}

.clear-btn:hover {
  background-color: #fee2e2;
  border-color: #fecaca;
}

/* 汽车收藏网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.07);
  border-color: #c8a165;
}

.favorite-card__img {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.favorite-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.favorite-card:hover .favorite-card__img img {
  transform: scale(1.05);
}

.favorite-card__badge {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 4px 12px;
  background: rgba(200, 161, 101, 0.9);
  color: white;
  font-size: 12px;
  border-radius: 30px;
  font-weight: 500;
  z-index: 1;
}

.favorite-card__remove {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;
  color: #94a3b8;
  border: 1px solid #edf2f7;
}

.favorite-card__remove:hover {
  background: #ef4444;
  color: white;
  transform: scale(1.1);
  border-color: #ef4444;
}

.favorite-card__body {
  padding: 16px;
}

.favorite-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.favorite-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #1e2a3a;
}

.favorite-card__rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
  font-size: 13px;
}

.favorite-card__specs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 13px;
  flex-wrap: wrap;
}

.spec-divider {
  color: #cbd5e0;
}

.favorite-card__features {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.feature-tag {
  padding: 4px 8px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 11px;
  color: #475569;
}

.favorite-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.favorite-card__price .price {
  font-size: 20px;
  font-weight: 800;
  color: #c8a165;
}

.favorite-card__price .unit {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 2px;
}

.rent-now-btn {
  background: #c8a165;
  color: white;
  border: none;
  border-radius: 30px;
  padding: 8px 16px;
  font-weight: 500;
}

.rent-now-btn:hover {
  background: #b28b4f;
}
/* 推荐区域 */
.recommend-section {
  margin-top: 48px;
  padding: 32px 0 0;
  border-top: 1px solid #edf2f7;
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
  grid-template-columns: repeat(3, 1fr);
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

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 空状态和加载 */
.loading-state,
.empty-state {
  padding: 60px 0;
  grid-column: 1 / -1;
}

.go-rental-btn {
  background: #c8a165;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
}

.go-rental-btn:hover {
  background: #b28b4f;
}
</style>
