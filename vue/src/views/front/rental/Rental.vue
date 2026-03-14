<template>
  <div class="rental-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">在线租车</h1>
      <p class="page-subtitle">海量车型，透明价格，一键下单</p>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <div class="search-card">
        <!-- 关键词搜索 -->
        <div class="search-row">
          <div class="search-box">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索车型、品牌（例如：比亚迪 凯美瑞 SUV）"
              class="search-input"
              clearable
              @keyup.enter="handleSearch">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" class="search-btn" @click="handleSearch">搜索</el-button>
          </div>
        </div>

        <!-- 筛选条件 -->
        <div class="filter-row">
          <!-- 品牌筛选 -->
          <div class="filter-item">
            <span class="filter-label">品牌：</span>
            <div class="filter-tags">
              <span
                v-for="brand in brandOptions"
                :key="brand"
                class="filter-tag"
                :class="{ active: searchForm.brand === brand }"
                @click="searchForm.brand = searchForm.brand === brand ? '' : brand">
                {{ brand }}
              </span>
            </div>
          </div>

          <!-- 座位数筛选 -->
          <div class="filter-item">
            <span class="filter-label">座位：</span>
            <div class="filter-tags">
              <span
                v-for="seat in seatOptions"
                :key="seat"
                class="filter-tag"
                :class="{ active: searchForm.seats === seat }"
                @click="searchForm.seats = searchForm.seats === seat ? '' : seat">
                {{ seat }}座
              </span>
            </div>
          </div>

          <!-- 能源类型筛选 -->
          <div class="filter-item">
            <span class="filter-label">能源：</span>
            <div class="filter-tags">
              <span
                v-for="energy in energyOptions"
                :key="energy"
                class="filter-tag"
                :class="{ active: searchForm.energy === energy }"
                @click="searchForm.energy = searchForm.energy === energy ? '' : energy">
                {{ energy }}
              </span>
            </div>
          </div>

          <!-- 价格区间 -->
          <div class="filter-item">
            <span class="filter-label">价格：</span>
            <div class="price-range">
              <el-input-number v-model="searchForm.minPrice" :min="0" :max="10000" placeholder="最低价" size="small" />
              <span class="price-separator">-</span>
              <el-input-number v-model="searchForm.maxPrice" :min="0" :max="10000" placeholder="最高价" size="small" />
            </div>
          </div>
        </div>

        <!-- 常用筛选标签 -->
        <div class="quick-filters">
          <span class="quick-label">常用筛选：</span>
          <span class="quick-tag" @click="quickFilter('新能源')">新能源</span>
          <span class="quick-tag" @click="quickFilter('SUV')">SUV</span>
          <span class="quick-tag" @click="quickFilter('商务')">商务</span>
          <span class="quick-tag" @click="quickFilter('自动挡')">自动挡</span>
          <span class="quick-tag" @click="quickFilter('5座')">5座</span>
          <span class="quick-tag" @click="quickFilter('7座')">7座</span>
        </div>
      </div>
    </div>

    <!-- 排序和结果统计 -->
    <div class="result-bar">
      <div class="result-count">
        找到 <span class="count-num">{{ filteredCars.length }}</span> 辆车型
      </div>
      <div class="sort-options">
        <span class="sort-label">排序：</span>
        <span class="sort-item" :class="{ active: sortBy === 'default' }" @click="sortBy = 'default'">默认</span>
        <span class="sort-item" :class="{ active: sortBy === 'priceAsc' }" @click="sortBy = 'priceAsc'"
          >价格从低到高</span
        >
        <span class="sort-item" :class="{ active: sortBy === 'priceDesc' }" @click="sortBy = 'priceDesc'"
          >价格从高到低</span
        >
      </div>
    </div>

    <!-- 车辆列表 -->
    <div class="car-list">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="filteredCars.length === 0" class="empty-state">
        <el-empty description="暂无符合条件的车型" />
      </div>

      <div v-else class="car-grid">
        <div v-for="car in paginatedCars" :key="car.id" class="car-card" @click="goToCarDetail(car)">
          <!-- 车辆图片 -->
          <div class="car-card__img">
            <img :src="car.image" :alt="car.name" />
            <div class="car-card__badge" v-if="car.tag">{{ car.tag }}</div>
            <div class="car-card__favorite" @click.stop="toggleFavorite(car)">
              <el-icon :color="car.isFavorite ? '#f56c6c' : '#fff'">
                <StarFilled v-if="car.isFavorite" />
                <Star v-else />
              </el-icon>
            </div>
          </div>

          <!-- 车辆信息 -->
          <div class="car-card__body">
            <div class="car-card__header">
              <div class="car-card__name">{{ car.brand }} · {{ car.model }}</div>
              <div class="car-card__rating">
                <el-icon><StarFilled /></el-icon>
                <span>{{ car.rating }}</span>
                <span class="review-count">({{ car.reviewCount }}评价)</span>
              </div>
            </div>

            <div class="car-card__specs">
              <span class="spec-item">{{ car.seats }}座</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.gear }}</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.energy }}</span>
              <span class="spec-divider">|</span>
              <span class="spec-item">{{ car.year }}年</span>
            </div>

            <div class="car-card__features">
              <span v-for="feature in car.features.slice(0, 3)" :key="feature" class="feature-tag">
                {{ feature }}
              </span>
            </div>

            <div class="car-card__footer">
              <div class="car-card__price">
                <span class="price">¥{{ car.price }}</span>
                <span class="unit">/天起</span>
              </div>
              <div class="car-card__actions">
                <el-button size="small" class="rent-now-btn" @click.stop="quickRent(car)">立即租车</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredCars.length"
        :page-sizes="[8, 12, 16, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Star, StarFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute() // 创建 route 实例
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const sortBy = ref('default')

// 搜索表单
const searchForm = reactive({
  keyword: '',
  brand: '',
  seats: '',
  energy: '',
  minPrice: null,
  maxPrice: null
})
// 筛选选项
const brandOptions = ['全部', '特斯拉', '比亚迪', '宝马', '奔驰', '奥迪', '大众', '丰田', '本田']
const seatOptions = [2, 4, 5, 7]
const energyOptions = ['燃油', '纯电', '混动', '增程式']

// 车辆数据
const allCars = ref([
  {
    id: 1,
    brand: '比亚迪',
    model: '秦 PLUS DM-i',
    seats: 5,
    gear: '自动',
    energy: '混动',
    year: 2023,
    price: 168,
    rating: 4.8,
    reviewCount: 328,
    tag: '省油优选',
    isFavorite: false,
    features: ['智能互联', '倒车影像', '定速巡航'],
    image: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 2,
    brand: '丰田',
    model: '凯美瑞',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: 2023,
    price: 198,
    rating: 4.7,
    reviewCount: 256,
    tag: '商务舒适',
    isFavorite: true,
    features: ['真皮座椅', '天窗', '座椅加热'],
    image: 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 3,
    brand: '特斯拉',
    model: 'Model 3',
    seats: 5,
    gear: '自动',
    energy: '纯电',
    year: 2024,
    price: 268,
    rating: 4.9,
    reviewCount: 412,
    tag: '科技驾控',
    isFavorite: false,
    features: ['自动驾驶', '大屏幕', '全景天窗'],
    image: 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 4,
    brand: '大众',
    model: '途观 L',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: 2023,
    price: 228,
    rating: 4.6,
    reviewCount: 189,
    tag: '空间充足',
    isFavorite: false,
    features: ['全景天窗', '电动尾门', '四驱'],
    image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 5,
    brand: '宝马',
    model: '3系',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: 2023,
    price: 329,
    rating: 4.8,
    reviewCount: 276,
    tag: '运动操控',
    isFavorite: false,
    features: ['运动模式', '哈曼卡顿', '抬头显示'],
    image: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 6,
    brand: '奔驰',
    model: 'C级',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: 2024,
    price: 359,
    rating: 4.9,
    reviewCount: 345,
    tag: '豪华舒适',
    isFavorite: false,
    features: ['氛围灯', '柏林之声', '座椅按摩'],
    image: 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 7,
    brand: '奥迪',
    model: 'A4L',
    seats: 5,
    gear: '自动',
    energy: '燃油',
    year: 2023,
    price: 299,
    rating: 4.7,
    reviewCount: 234,
    tag: '科技感',
    isFavorite: false,
    features: ['虚拟座舱', 'quattro', '矩阵大灯'],
    image: 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=800&q=80'
  },
  {
    id: 8,
    brand: '理想',
    model: 'L7',
    seats: 5,
    gear: '自动',
    energy: '增程式',
    year: 2024,
    price: 399,
    rating: 4.8,
    reviewCount: 167,
    tag: '家庭首选',
    isFavorite: false,
    features: ['大彩电', '冰箱', '皇后座'],
    image: 'https://images.pexels.com/photos/3349460/pexels-photo-3349460.jpeg'
  },
  {
    id: 9,
    brand: '蔚来',
    model: 'ET5',
    seats: 5,
    gear: '自动',
    energy: '纯电',
    year: 2023,
    price: 329,
    rating: 4.7,
    reviewCount: 198,
    tag: '智能电动',
    isFavorite: false,
    features: ['换电', 'NOMI', '全景天窗'],
    image:
      'https://tse1-mm.cn.bing.net/th/id/OIP-C.YpR4PRpe3veIt1n-_8REtQHaE8?w=306&h=192&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3'
  },
  {
    id: 10,
    brand: '小鹏',
    model: 'P7',
    seats: 7,
    gear: '自动',
    energy: '纯电',
    year: 2023,
    price: 279,
    rating: 4.6,
    reviewCount: 156,
    tag: '运动轿跑',
    isFavorite: false,
    features: ['鹏翼门', '智能驾驶', '丹拿音响'],
    image:
      'https://tse4-mm.cn.bing.net/th/id/OIP-C.mA3jMP3rwQ653qRdA3J7dAHaFj?w=244&h=184&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3'
  }
])

// 过滤逻辑
const filteredCars = computed(() => {
  let result = [...allCars.value]

  // 关键词搜索 - 智能搜索
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()

    // 解析搜索关键词
    const seatsMatch = keyword.match(/^(\d+)座$/)
    const priceMatch = keyword.match(/^(\d+)-(\d+)$/) // 支持"100-200"格式

    result = result.filter((car) => {
      // 座位数精确匹配
      if (seatsMatch) {
        return car.seats === parseInt(seatsMatch[1])
      }

      // 价格区间匹配
      if (priceMatch) {
        const min = parseInt(priceMatch[1])
        const max = parseInt(priceMatch[2])
        return car.price >= min && car.price <= max
      }

      // 普通文本搜索
      return (
        car.brand.toLowerCase().includes(keyword) ||
        car.model.toLowerCase().includes(keyword) ||
        car.energy.toLowerCase().includes(keyword) ||
        car.tag?.toLowerCase().includes(keyword) ||
        car.gear.toLowerCase().includes(keyword) ||
        `${car.seats}座`.includes(keyword) // 支持搜索"5座"文本
      )
    })
  }

  // 品牌筛选
  if (searchForm.brand && searchForm.brand !== '全部') {
    result = result.filter((car) => car.brand === searchForm.brand)
  }

  // 座位筛选
  if (searchForm.seats) {
    result = result.filter((car) => car.seats === searchForm.seats)
  }

  // 能源筛选
  if (searchForm.energy) {
    result = result.filter((car) => car.energy === searchForm.energy)
  }

  // 价格区间
  if (searchForm.minPrice !== null) {
    result = result.filter((car) => car.price >= searchForm.minPrice)
  }
  if (searchForm.maxPrice !== null) {
    result = result.filter((car) => car.price <= searchForm.maxPrice)
  }

  // 排序
  if (sortBy.value === 'priceAsc') {
    result.sort((a, b) => a.price - b.price)
  } else if (sortBy.value === 'priceDesc') {
    result.sort((a, b) => b.price - a.price)
  }

  return result
})

// 分页数据
const paginatedCars = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCars.value.slice(start, end)
})

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 快速筛选映射 - 针对当前数据结构
const filterMap = {
  // 座位筛选
  '5座': { type: 'seats', value: 5 },
  '7座': { type: 'seats', value: 7 },

  // 能源类型筛选
  新能源: { type: 'energy', value: '纯电' }, // 注意：你的数据中新能源对应的是"纯电"
  纯电: { type: 'energy', value: '纯电' },
  混动: { type: 'energy', value: '混动' },

  // 车型筛选 - 作为关键词搜索
  SUV: { type: 'keyword', value: 'SUV' },
  商务: { type: 'keyword', value: '商务' },

  // 品牌筛选
  特斯拉: { type: 'brand', value: '特斯拉' },
  比亚迪: { type: 'brand', value: '比亚迪' },
  宝马: { type: 'brand', value: '宝马' },
  奔驰: { type: 'brand', value: '奔驰' },
  奥迪: { type: 'brand', value: '奥迪' },
  大众: { type: 'brand', value: '大众' },
  丰田: { type: 'brand', value: '丰田' },
  本田: { type: 'brand', value: '本田' }
}

// 快速筛选
const quickFilter = (filter) => {
  const mapping = filterMap[filter]

  if (mapping) {
    switch (mapping.type) {
      case 'seats':
        searchForm.seats = mapping.value
        searchForm.keyword = ''
        searchForm.brand = '' // 清空其他筛选条件
        searchForm.energy = ''
        break
      case 'energy':
        searchForm.energy = mapping.value
        searchForm.keyword = ''
        searchForm.seats = ''
        searchForm.brand = ''
        break
      case 'brand':
        searchForm.brand = mapping.value
        searchForm.keyword = ''
        searchForm.seats = ''
        searchForm.energy = ''
        break
      case 'keyword':
        searchForm.keyword = mapping.value
        searchForm.seats = ''
        searchForm.energy = ''
        searchForm.brand = ''
        break
      default:
        searchForm.keyword = filter
    }
  } else {
    // 默认作为关键词搜索
    searchForm.keyword = filter
  }

  // 重置到第一页
  currentPage.value = 1
}

// 收藏切换
const toggleFavorite = (car) => {
  car.isFavorite = !car.isFavorite

  // 获取当前收藏列表
  let favorites = JSON.parse(localStorage.getItem('user-favorites') || '[]')

  if (car.isFavorite) {
    // 添加到收藏
    if (!favorites.some((item) => item.id === car.id)) {
      favorites.push(car)
      ElMessage.success('已添加到收藏')
    }
  } else {
    // 从收藏中移除
    favorites = favorites.filter((item) => item.id !== car.id)
    ElMessage.success('已取消收藏')
  }

  // 保存到 localStorage
  localStorage.setItem('user-favorites', JSON.stringify(favorites))
}

// 立即租车
const quickRent = (car) => {
  ElMessage.success(`正在查看 ${car.brand} ${car.model} 详情`)
  // 跳转到详情页
  router.push(`/front/car/${car.id}`)
}

// 查看详情
const goToCarDetail = (car) => {
  router.push(`/front/car/${car.id}`)
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

// 监听搜索条件变化，重置页码
watch([searchForm, sortBy], () => {
  currentPage.value = 1
})
// 页面加载时读取 URL 参数
onMounted(() => {
  const keyword = route.query.keyword
  if (keyword) {
    searchForm.keyword = keyword
    handleSearch()
  }
})

// 监听路由参数变化（当在租车页面内点击其他分类时）
watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword) {
      searchForm.keyword = newKeyword
      handleSearch()
    }
  }
)
</script>

<style scoped>
.rental-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
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

/* 搜索卡片 */
.search-section {
  margin-bottom: 24px;
}

.search-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #edf2f7;
}

.search-row {
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 40px;
  padding-left: 20px;
  height: 46px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c8a165 inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 2px rgba(200, 161, 101, 0.2),
    0 0 0 1px #c8a165 inset;
}

.search-btn {
  width: 100px;
  height: 46px;
  border-radius: 40px;
  background: #c8a165;
  border: none;
  font-weight: 500;
}

.search-btn:hover {
  background: #b28b4f;
}

/* 筛选条件 */
.filter-row {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.filter-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.filter-label {
  min-width: 60px;
  font-size: 14px;
  color: #64748b;
  line-height: 32px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.filter-tag {
  padding: 6px 16px;
  border-radius: 30px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tag:hover {
  border-color: #c8a165;
  color: #c8a165;
}

.filter-tag.active {
  background: #c8a165;
  border-color: #c8a165;
  color: white;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-range :deep(.el-input-number) {
  width: 120px;
}

.price-separator {
  color: #94a3b8;
}

/* 常用筛选 */
.quick-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}

.quick-label {
  font-size: 13px;
  color: #94a3b8;
}

.quick-tag {
  padding: 4px 12px;
  border-radius: 30px;
  background: #f1f5f9;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-tag:hover {
  background: #c8a165;
  color: white;
}

/* 结果统计和排序 */
.result-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-count {
  font-size: 14px;
  color: #64748b;
}

.count-num {
  font-size: 18px;
  font-weight: 700;
  color: #c8a165;
  margin: 0 4px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sort-label {
  font-size: 14px;
  color: #94a3b8;
}

.sort-item {
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.2s ease;
}

.sort-item:hover {
  color: #c8a165;
}

.sort-item.active {
  color: #c8a165;
  font-weight: 500;
}

/* 车辆网格 */
.car-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.car-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
  cursor: pointer;
}

.car-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.07);
  border-color: #c8a165;
}

.car-card__img {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.car-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.car-card:hover .car-card__img img {
  transform: scale(1.05);
}

.car-card__badge {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 4px 12px;
  background: rgba(200, 161, 101, 0.9);
  color: white;
  font-size: 12px;
  border-radius: 30px;
  font-weight: 500;
}

.car-card__favorite {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.car-card__favorite:hover {
  background: rgba(0, 0, 0, 0.5);
  transform: scale(1.1);
}

.car-card__body {
  padding: 16px;
}

.car-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.car-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #1e2a3a;
}

.car-card__rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
  font-size: 13px;
}

.review-count {
  color: #94a3b8;
  font-size: 12px;
}

.car-card__specs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 13px;
}

.spec-divider {
  color: #cbd5e0;
}

.car-card__features {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.feature-tag {
  padding: 4px 8px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 11px;
  color: #475569;
}

.car-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.car-card__price .price {
  font-size: 20px;
  font-weight: 800;
  color: #c8a165;
}

.car-card__price .unit {
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

/* 分页 */
.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

/* 空状态和加载 */
.loading-state,
.empty-state {
  padding: 60px 0;
  grid-column: 1 / -1;
}

/* 响应式 */
@media (max-width: 1100px) {
  .car-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .car-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-item {
    flex-direction: column;
    gap: 8px;
  }

  .filter-label {
    line-height: 1.5;
  }

  .result-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

@media (max-width: 600px) {
  .car-grid {
    grid-template-columns: 1fr;
  }

  .search-box {
    flex-direction: column;
  }

  .search-btn {
    width: 100%;
  }

  .price-range {
    flex-wrap: wrap;
  }

  .quick-filters {
    flex-wrap: wrap;
  }
}
</style>
