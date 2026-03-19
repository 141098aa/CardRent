<template>
  <div class="favorites-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
      <p class="page-subtitle">收藏您心仪的车型和资讯，随时查看</p>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <el-tabs v-model="activeTab" class="favorite-tabs" @tab-change="handleTabChange">
        <el-tab-pane :label="`收藏的汽车 (${carFavorites.length})`" name="cars">
          <template #label>
            <span class="tab-label">
              <el-icon><Van /></el-icon>
              收藏的汽车
              <span class="tab-count">{{ carFavorites.length }}</span>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane :label="`收藏的资讯 (${newsFavorites.length})`" name="news">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              收藏的资讯
              <span class="tab-count">{{ newsFavorites.length }}</span>
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="favorites-stats">
        共收藏 <span class="stats-number">{{ currentFavorites.length }}</span> 个{{
          activeTab === 'cars' ? '车型' : '资讯'
        }}
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
    <div v-if="activeTab === 'cars'" class="favorites-list">
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

    <!-- 收藏列表 - 资讯 -->
    <div v-if="activeTab === 'news'" class="favorites-list">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="newsFavorites.length === 0" class="empty-state">
        <el-empty description="暂无收藏资讯">
          <template #extra>
            <el-button type="primary" class="go-rental-btn" @click="goToNews">去浏览资讯</el-button>
          </template>
        </el-empty>
      </div>

      <div v-else class="news-grid">
        <div v-for="news in paginatedNewsFavorites" :key="news.id" class="news-card" @click="goToNewsDetail(news)">
          <!-- 资讯图片 -->
          <div class="news-card__img">
            <img :src="news.image" :alt="news.title" />
            <div class="news-card__badge" v-if="news.isHot">热门</div>
            <div class="news-card__remove" @click.stop="removeFromFavorites(news, 'news')">
              <el-icon><Close /></el-icon>
            </div>
          </div>

          <!-- 资讯内容 -->
          <div class="news-card__content">
            <div class="news-card__meta">
              <span
                class="news-card__category"
                :style="{ backgroundColor: news.category.color + '20', color: news.category.color }">
                {{ news.category.name }}
              </span>
              <span class="news-card__date">{{ news.date }}</span>
            </div>
            <h3 class="news-card__title">{{ news.title }}</h3>
            <p class="news-card__summary">{{ news.summary }}</p>
            <div class="news-card__footer">
              <div class="news-card__stats">
                <span
                  ><el-icon><View /></el-icon> {{ news.views }}</span
                >
                <span
                  ><el-icon><ChatLineRound /></el-icon> {{ news.comments }}</span
                >
              </div>
              <span class="news-card__source">{{ news.source }}</span>
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
        :page-sizes="activeTab === 'cars' ? [6, 9, 12] : [5, 10, 15]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 推荐车型 -->
    <div class="recommend-section" v-if="activeTab === 'cars' && carFavorites.length > 0">
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

    <!-- 推荐资讯 -->
    <div class="recommend-section" v-if="activeTab === 'news' && newsFavorites.length > 0">
      <h2 class="section-title">热门资讯推荐</h2>
      <div class="recommend-news-grid">
        <div v-for="news in recommendedNews" :key="news.id" class="recommend-news-card" @click="goToNewsDetail(news)">
          <div class="recommend-news-card__img">
            <img :src="news.image" :alt="news.title" />
          </div>
          <div class="recommend-news-card__content">
            <h4 class="recommend-news-card__title">{{ news.title }}</h4>
            <div class="recommend-news-card__meta">
              <span>{{ news.date }}</span>
              <span>{{ news.views }}阅读</span>
            </div>
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
import { StarFilled, Delete, Close, Van, Document, View, ChatLineRound } from '@element-plus/icons-vue'
import { userFavoriteApi } from '@/utils/api'
import { userCarApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('cars')
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

// 加载推荐车型
const loadRecommendedCars = async () => {
  try {
    const res = await userCarApi.getHotRecommend(3)
    if (res.code === '200') {
      recommendedCars.value = res.data || []
    }
  } catch (error) {
    console.error('加载推荐车型失败:', error)
  }
}
// 收藏列表
const carFavorites = ref([])
const newsFavorites = ref([])

// 当前显示的收藏
const currentFavorites = computed(() => {
  return activeTab.value === 'cars' ? carFavorites.value : newsFavorites.value
})

// 分页数据
const paginatedCarFavorites = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return carFavorites.value.slice(start, end)
})

const paginatedNewsFavorites = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return newsFavorites.value.slice(start, end)
})

// 推荐车型（模拟数据）
const recommendedCars = ref([
  {
    id: 101,
    brand: '比亚迪',
    model: '汉 EV',
    price: 299,
    image: 'https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 102,
    brand: '特斯拉',
    model: 'Model Y',
    price: 329,
    image: 'https://images.unsplash.com/photo-1617704548623-340376564e68?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 103,
    brand: '理想',
    model: 'L9',
    price: 459,
    image: 'https://images.unsplash.com/photo-1698392384527-91a8a4b9b8c0?auto=format&fit=crop&w=800&q=80'
  }
])

// 推荐资讯（模拟数据）
const recommendedNews = ref([
  {
    id: 201,
    title: '新能源汽车租赁补贴政策延至2025年',
    image: 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80',
    date: '2024-03-15',
    views: 2345
  },
  {
    id: 202,
    title: '清明小长假租车预订量同比增长150%',
    image: 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?auto=format&fit=crop&w=800&q=80',
    date: '2024-03-13',
    views: 3210
  },
  {
    id: 203,
    title: '五一出游季：长租7天以上享7折优惠',
    image: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80',
    date: '2024-03-12',
    views: 4567
  }
])

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

    // 获取资讯收藏列表（如果有资讯接口）
    // const newsRes = await userFavoriteApi.getFavoriteList({
    //   targetType: 'news',
    //   pageNum: 1,
    //   pageSize: 999
    // })
    // if (newsRes.code === '200') {
    //   newsFavorites.value = newsRes.data?.list || []
    // } else if (newsRes.code !== '200') {
    //   ElMessage.error(newsRes.msg || '加载资讯收藏失败')
    // }
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 从收藏中移除
const removeFromFavorites = async (item, type) => {
  try {
    const targetType = type === 'cars' ? 'car' : 'news'
    const res = await userFavoriteApi.removeFavorite(item.id, targetType)

    if (res.code === '200') {
      if (type === 'cars') {
        carFavorites.value = carFavorites.value.filter((i) => i.id !== item.id)
      } else {
        newsFavorites.value = newsFavorites.value.filter((i) => i.id !== item.id)
      }
      ElMessage.success('已从收藏中移除')
    } else {
      ElMessage.error(res.msg || '移除收藏失败')
    }
  } catch (error) {
    console.error('移除收藏失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || '移除收藏失败'
    ElMessage.error(errorMsg)
  }
}

// 清空当前分类收藏
const confirmClearCurrent = () => {
  const typeText = activeTab.value === 'cars' ? '汽车收藏' : '资讯收藏'
  ElMessageBox.confirm(`确定要清空所有${typeText}吗？此操作不可恢复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const targetType = activeTab.value === 'cars' ? 'car' : 'news'
        // 逐个删除所有收藏（或者后端提供批量删除接口）
        const promises = currentFavorites.value.map((item) => userFavoriteApi.removeFavorite(item.id, targetType))
        const results = await Promise.all(promises)

        // 检查是否有失败
        const failed = results.filter((r) => r.code !== '200')
        if (failed.length > 0) {
          ElMessage.warning(`部分删除失败，${failed.length}个未成功`)
        } else {
          if (activeTab.value === 'cars') {
            carFavorites.value = []
          } else {
            newsFavorites.value = []
          }
          ElMessage.success(`已清空${typeText}`)
        }
      } catch (error) {
        console.error('清空收藏失败:', error)
        const errorMsg = error.response?.data?.msg || error.message || '清空收藏失败'
        ElMessage.error(errorMsg)
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

const goToNewsDetail = (news) => {
  router.push(`/front/news/${news.id}`)
}

// 跳转页面
const goToRental = () => {
  router.push('/front/rental')
}

const goToNews = () => {
  router.push('/front/news')
}

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  if (activeTab.value === 'cars') {
    pageSize.value = 6
  } else {
    pageSize.value = 5
  }
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

// 监听标签页变化重置分页
// watch(activeTab, () => {
//   currentPage.value = 1
// })
// 添加 watch 监听页码和标签页变化
watch(
  [activeTab, currentPage, pageSize],
  () => {
    // 确保用户已登录
    if (checkLogin()) {
      loadFavorites()
    }
  },
  { immediate: false } // 不要立即执行，让 onMounted 处理首次加载
)

// 处理首次加载
onMounted(() => {
  if (checkLogin()) {
    loadFavorites()
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

/* 分类标签 */
.category-tabs {
  margin-bottom: 20px;
  background: white;
  border-radius: 16px;
  padding: 10px 20px;
  border: 1px solid #edf2f7;
}

.favorite-tabs :deep(.el-tabs__nav) {
  border: none;
}

.favorite-tabs :deep(.el-tabs__item) {
  height: 50px;
  font-size: 15px;
  color: #64748b;
}

.favorite-tabs :deep(.el-tabs__item.is-active) {
  color: #c8a165;
  font-weight: 500;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-label .el-icon {
  font-size: 18px;
}

.tab-count {
  margin-left: 4px;
  padding: 2px 6px;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 11px;
  color: #475569;
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

/* 资讯收藏网格 */
.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.news-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: column;
}

.news-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.04);
  border-color: #c8a165;
}

.news-card__img {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.news-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.news-card:hover .news-card__img img {
  transform: scale(1.05);
}

.news-card__badge {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 4px 12px;
  background: rgba(245, 158, 11, 0.9);
  color: white;
  font-size: 12px;
  border-radius: 30px;
  font-weight: 500;
  z-index: 1;
}

.news-card__remove {
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

.news-card__remove:hover {
  background: #ef4444;
  color: white;
  transform: scale(1.1);
  border-color: #ef4444;
}

.news-card__content {
  padding: 16px;
  flex: 1;
}

.news-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.news-card__category {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.news-card__date {
  font-size: 11px;
  color: #94a3b8;
}

.news-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 8px;
  line-height: 1.4;
}

.news-card__summary {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 12px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.news-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #94a3b8;
}

.news-card__stats {
  display: flex;
  gap: 12px;
}

.news-card__stats span {
  display: flex;
  align-items: center;
  gap: 2px;
}

.news-card__stats .el-icon {
  font-size: 12px;
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

.recommend-news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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

.recommend-news-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-news-card:hover {
  transform: translateY(-2px);
  border-color: #c8a165;
  box-shadow: 0 5px 15px rgba(200, 161, 101, 0.05);
}

.recommend-news-card__img {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.recommend-news-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-news-card__content {
  flex: 1;
  min-width: 0;
}

.recommend-news-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-news-card__meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #94a3b8;
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
