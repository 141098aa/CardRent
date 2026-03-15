<template>
  <div class="front-container">
    <!-- 顶部轮播图 -->
    <div class="hero">
      <div class="carousel-wrapper">
        <el-carousel :interval="4000" type="card" height="220px" class="hero-carousel">
          <el-carousel-item v-for="item in carouselItems" :key="item.id">
            <div class="carousel-content" :style="{ backgroundImage: `url(${item.image})` }">
              <div class="carousel-overlay">
                <div class="carousel-title">{{ item.title }}</div>
                <div class="carousel-desc">{{ item.desc }}</div>
                <el-button size="small" class="carousel-btn" @click.stop="quickSearch(item.keyword)"
                  >查看车型</el-button
                >
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <!-- 租车流程 + 热门分类 -->
    <div class="section two-col">
      <div class="panel">
        <div class="section__title">租车流程</div>
        <div class="steps">
          <div class="step">
            <div class="step__no">01</div>
            <div class="step__main">
              <div class="step__k">选择车型</div>
              <div class="step__v">按品牌/类型/价格筛选</div>
            </div>
          </div>
          <div class="step">
            <div class="step__no">02</div>
            <div class="step__main">
              <div class="step__k">确认信息</div>
              <div class="step__v">时间地点、证件信息</div>
            </div>
          </div>
          <div class="step">
            <div class="step__no">03</div>
            <div class="step__main">
              <div class="step__k">在线下单</div>
              <div class="step__v">价格透明，支持多种支付</div>
            </div>
          </div>
          <div class="step">
            <div class="step__no">04</div>
            <div class="step__main">
              <div class="step__k">取还车辆</div>
              <div class="step__v">流程简单，客服随时在线</div>
            </div>
          </div>
        </div>

        <el-button class="primary-cta" type="primary" @click="go('/front/rental')">去选车</el-button>
      </div>

      <div class="panel">
        <div class="section__title">热门分类</div>
        <div class="filters">
          <div class="filter" @click="goFilter('新能源')">
            <div class="filter__k">新能源</div>
            <div class="filter__v">省钱省心 · 城市通勤</div>
          </div>
          <div class="filter" @click="goFilter('SUV')">
            <div class="filter__k">SUV</div>
            <div class="filter__v">空间更大 · 轻越野</div>
          </div>
          <div class="filter" @click="goFilter('商务')">
            <div class="filter__k">商务轿车</div>
            <div class="filter__v">稳重舒适 · 接待出行</div>
          </div>
          <div class="filter" @click="goFilter('经济')">
            <div class="filter__k">经济实惠</div>
            <div class="filter__v">性价比高 · 新手友好</div>
          </div>
        </div>

        <div class="chips">
          <span class="chip" @click="goFilter('自动')">自动挡</span>
          <span class="chip" @click="goFilter('5座')">5座</span>
          <span class="chip" @click="goFilter('7座')">7座</span>
          <span class="chip" @click="goFilter('纯电')">纯电</span>
          <span class="chip" @click="goFilter('混动')">混动</span>
        </div>
      </div>
    </div>

    <!-- 热门推荐车辆 -->
    <div class="section">
      <div class="section__row">
        <div class="section__title">热门推荐</div>
        <el-button link type="primary" @click="go('/front/rental')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="loading" class="car-grid">
        <div v-for="car in hotCars" :key="car.id" class="car-card" @click="goRentalDetail(car)">
          <div class="car-card__img">
            <img :src="car.image" :alt="car.model" />
            <div class="car-card__badge">{{ car.tag }}</div>
          </div>

          <div class="car-card__body">
            <div class="car-card__name">{{ car.brandName }} · {{ car.model }}</div>
            <div class="car-card__meta">
              <span>{{ car.seats }}座</span>
              <span class="dot">·</span>
              <span>{{ car.gear }}</span>
              <span class="dot">·</span>
              <span>{{ car.energy }}</span>
            </div>

            <div class="car-card__footer">
              <div class="car-card__price">
                <span class="price">¥{{ car.price }}</span>
                <span class="unit">/天起</span>
              </div>
              <div class="car-card__cta">查看详情</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评分推荐车辆 -->
    <div class="section">
      <div class="section__row">
        <div class="section__title">高分好评</div>
        <el-button link type="primary" @click="go('/front/rental')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="ratingLoading" class="car-grid">
        <div v-for="car in ratingCars" :key="car.id" class="car-card" @click="goRentalDetail(car)">
          <div class="car-card__img">
            <img :src="car.image" :alt="car.model" />
            <div class="car-card__badge">评分 {{ car.rating }}</div>
          </div>

          <div class="car-card__body">
            <div class="car-card__name">{{ car.brandName }} · {{ car.model }}</div>
            <div class="car-card__meta">
              <span>{{ car.seats }}座</span>
              <span class="dot">·</span>
              <span>{{ car.gear }}</span>
              <span class="dot">·</span>
              <span>{{ car.energy }}</span>
            </div>

            <div class="car-card__footer">
              <div class="car-card__price">
                <span class="price">¥{{ car.price }}</span>
                <span class="unit">/天起</span>
              </div>
              <div class="car-card__cta">查看详情</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 租车须知/公告 -->
    <div class="section">
      <div class="section__row">
        <div class="section__title">租车须知</div>
        <el-button link type="primary" @click="go('/front/news')">
          更多资讯 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div class="notice-grid">
        <div class="notice">
          <div class="notice__k">证件要求</div>
          <div class="notice__v">需身份证 + 驾照，信息一致即可下单。</div>
        </div>
        <div class="notice">
          <div class="notice__k">押金说明</div>
          <div class="notice__v">根据车型收取押金，支持原路退回。</div>
        </div>
        <div class="notice">
          <div class="notice__k">保险保障</div>
          <div class="notice__v">基础保险已包含，可按需升级保障。</div>
        </div>
        <div class="notice">
          <div class="notice__k">取消规则</div>
          <div class="notice__v">按下单时间节点计费，规则透明可查。</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userCarApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const ratingLoading = ref(false)

// 轮播图数据
const carouselItems = ref([
  {
    id: 1,
    title: '新能源专场',
    desc: '比亚迪全系限时优惠',
    keyword: '新能源',
    image: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1200&q=80'
  },
  {
    id: 2,
    title: 'SUV 家族',
    desc: '空间更大，适合全家出行',
    keyword: 'SUV',
    image: 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1200&q=80'
  },
  {
    id: 3,
    title: '商务轿车',
    desc: '稳重舒适，接待首选',
    keyword: '商务',
    image: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=1200&q=80'
  },
  {
    id: 4,
    title: '经济实惠',
    desc: '性价比高，新手友好',
    keyword: '经济',
    image: 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=1200&q=80'
  }
])

// 热门推荐车辆
const hotCars = ref([])

// 评分推荐车辆
const ratingCars = ref([])

// 加载热门推荐（基于评价数量）
const loadHotRecommend = async () => {
  loading.value = true
  try {
    const res = await userCarApi.getHotRecommend(4)
    if (res.code === '200') {
      hotCars.value = res.data || []
    }
  } catch (error) {
    console.error('加载热门推荐失败:', error)
    ElMessage.error('加载热门推荐失败')
  } finally {
    loading.value = false
  }
}

// 加载评分推荐（基于评分）
const loadRatingRecommend = async () => {
  ratingLoading.value = true
  try {
    const res = await userCarApi.getRatingRecommend(4)
    if (res.code === '200') {
      ratingCars.value = res.data || []
    }
  } catch (error) {
    console.error('加载评分推荐失败:', error)
    ElMessage.error('加载评分推荐失败')
  } finally {
    ratingLoading.value = false
  }
}

// 跳转函数
const go = (path) => {
  router.push(path)
  setTimeout(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, 100)
}

// 快速搜索 - 跳转到租车页面并带搜索参数
const quickSearch = (kw) => {
  router.push({
    path: '/front/rental',
    query: { keyword: kw }
  })
}

// 筛选 - 跳转到租车页面并带筛选参数
const goFilter = (kw) => {
  router.push({
    path: '/front/rental',
    query: { keyword: kw }
  })
}

// 跳转到车辆详情
const goRentalDetail = (car) => {
  router.push(`/front/car/${car.id}`)
}

// 初始化加载
onMounted(() => {
  loadHotRecommend()
  loadRatingRecommend()
})
</script>

<style scoped>
.front-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 18px 14px 28px;
}

/* 顶部纯轮播图区域*/
.hero {
  margin-bottom: 24px;
  background: #ffffff;
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(237, 242, 247, 0.9);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.02);
}

.carousel-wrapper {
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
}

.hero-carousel {
  border-radius: 16px;
  overflow: hidden;
}

.carousel-content {
  height: 100%;
  width: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  border-radius: 12px;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
  border-radius: 12px;
}

.carousel-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.carousel-desc {
  font-size: 14px;
  margin-bottom: 12px;
  opacity: 0.9;
}

.carousel-btn {
  background: #c8a165;
  border: none;
  color: white;
  font-size: 13px;
  padding: 8px 16px;
  border-radius: 30px;
  font-weight: 500;
}

.carousel-btn:hover {
  background: #b28b4f;
}

/* 自定义轮播点样式 */
:deep(.el-carousel__indicators) {
  bottom: 10px;
}

:deep(.el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.6);
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: #c8a165;
  width: 24px;
  border-radius: 4px;
}

/* 通用 section */
.section {
  margin-top: 24px;
}

.section__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 16px;
}

.section__title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  position: relative;
  padding-left: 12px;
}

.section__title::before {
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

/* 两列面板（租车流程 + 热门分类） */
.two-col {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
}

.panel {
  background: #fff;
  border: 1px solid rgba(237, 242, 247, 1);
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.panel:hover {
  border-color: #c8a165;
  box-shadow: 0 15px 35px rgba(200, 161, 101, 0.1);
}

/* 租车流程 */
.steps {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.step {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(241, 245, 249, 0.7);
  transition: all 0.2s ease;
}

.step:hover {
  background: rgba(200, 161, 101, 0.08);
}

.step__no {
  width: 44px;
  height: 36px;
  border-radius: 10px;
  background: rgba(200, 161, 101, 0.18);
  color: #9a6b2f;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.step__k {
  font-weight: 800;
  color: #111827;
  font-size: 15px;
}

.step__v {
  margin-top: 2px;
  color: #64748b;
  font-size: 13px;
}

.primary-cta {
  width: 100%;
  height: 42px;
  border-radius: 12px;
  background: #c8a165;
  border: none;
  font-weight: 700;
  font-size: 15px;
}

.primary-cta:hover {
  background: #b28b4f;
}

/* 热门分类 */
.filters {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.filter {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(237, 242, 247, 1);
  background: rgba(64, 158, 255, 0.06);
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.filter:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.06);
  border-color: #c8a165;
}

.filter__k {
  font-weight: 900;
  color: #111827;
  font-size: 15px;
}

.filter__v {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(237, 242, 247, 1);
  color: #334155;
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  transition: all 0.2s ease;
}

.chip:hover {
  background: rgba(200, 161, 101, 0.1);
  color: #c8a165;
  border-color: #c8a165;
}

/* 热门车辆列表 */
.car-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.car-card {
  background: #fff;
  border: 1px solid rgba(237, 242, 247, 1);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.car-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 34px rgba(0, 0, 0, 0.07);
  border-color: #c8a165;
}

.car-card__img {
  position: relative;
  height: 140px;
  background: #f1f5f9;
}

.car-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.car-card__badge {
  position: absolute;
  left: 10px;
  top: 10px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.6);
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
}

.car-card__body {
  padding: 14px;
}

.car-card__name {
  font-size: 15px;
  font-weight: 800;
  color: #111827;
  margin-bottom: 6px;
}

.car-card__meta {
  color: #64748b;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.dot {
  opacity: 0.6;
}

.car-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.car-card__price .price {
  font-size: 18px;
  font-weight: 900;
  color: #c8a165;
}

.car-card__price .unit {
  margin-left: 4px;
  font-size: 12px;
  color: #64748b;
}

.car-card__cta {
  font-size: 13px;
  color: #409eff;
  background: rgba(64, 158, 255, 0.12);
  border-radius: 999px;
  padding: 6px 12px;
  transition: all 0.2s;
}

.car-card__cta:hover {
  background: #409eff;
  color: #fff;
}

/* 租车须知 */
.notice-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.notice {
  background: #fff;
  border: 1px solid rgba(237, 242, 247, 1);
  border-radius: 14px;
  padding: 16px;
  transition: all 0.2s ease;
}

.notice:hover {
  border-color: #c8a165;
  box-shadow: 0 4px 12px rgba(200, 161, 101, 0.1);
}

.notice__k {
  font-weight: 900;
  color: #111827;
  font-size: 15px;
  margin-bottom: 8px;
}

.notice__v {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}
</style>
