<template>
  <div class="news-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">租车资讯</h1>
      <p class="page-subtitle">行业动态 · 政策法规 · 新车资讯 · 优惠活动</p>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <span
        v-for="category in categories"
        :key="category.id"
        class="category-tab"
        :class="{ active: currentCategory === category.id }"
        @click="currentCategory = category.id">
        {{ category.name }}
      </span>
    </div>

    <!-- 资讯列表 -->
    <div class="news-list">
      <!-- 置顶资讯 -->
      <div v-if="topNews" class="top-news" @click="goToNewsDetail(topNews.id)">
        <div class="top-news__img">
          <img :src="topNews.image" :alt="topNews.title" />
          <span class="top-news__badge">置顶</span>
        </div>
        <div class="top-news__content">
          <div class="top-news__meta">
            <span
              class="top-news__category"
              :style="{ backgroundColor: topNews.category.color + '20', color: topNews.category.color }">
              {{ topNews.category.name }}
            </span>
            <span class="top-news__date">{{ topNews.date }}</span>
          </div>
          <h2 class="top-news__title">{{ topNews.title }}</h2>
          <p class="top-news__summary">{{ topNews.summary }}</p>
          <div class="top-news__stats">
            <span
              ><el-icon><View /></el-icon> {{ topNews.views }}</span
            >
            <span
              ><el-icon><ChatLineRound /></el-icon> {{ topNews.comments }}</span
            >
            <span
              ><el-icon><Star /></el-icon> {{ topNews.likes }}</span
            >
          </div>
        </div>
      </div>

      <!-- 资讯网格 -->
      <div class="news-grid">
        <div v-for="news in paginatedNews" :key="news.id" class="news-card" @click="goToNewsDetail(news.id)">
          <div class="news-card__img">
            <img :src="news.image" :alt="news.title" />
            <span class="news-card__badge" v-if="news.isHot">热门</span>
          </div>
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

      <!-- 加载更多 -->
      <div class="load-more">
        <el-button v-if="hasMore" class="load-more-btn" @click="loadMore" :loading="loading"> 加载更多 </el-button>
        <div v-else class="no-more">没有更多资讯了</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatLineRound, Star } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const currentCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(8)

// 分类数据
const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'industry', name: '行业动态', color: '#409eff' },
  { id: 'policy', name: '政策法规', color: '#f59e0b' },
  { id: 'newcar', name: '新车资讯', color: '#10b981' },
  { id: 'promotion', name: '优惠活动', color: '#f56c6c' },
  { id: 'knowledge', name: '用车知识', color: '#8b5cf6' }
])

// 资讯数据
const allNews = ref([
  {
    id: 1,
    title: '新能源汽车租赁补贴政策延至2025年',
    summary: '近日，国家发改委等三部门联合发布通知，将新能源汽车租赁补贴政策延长至2025年底...',
    category: { id: 'policy', name: '政策法规', color: '#f59e0b' },
    image: 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-15',
    views: 2345,
    comments: 56,
    likes: 128,
    isHot: true,
    isTop: true,
    source: '新华社'
  },
  {
    id: 2,
    title: '比亚迪海豹06正式上市，首月可享8折租车优惠',
    summary: '比亚迪全新车型海豹06正式上市，易租车平台独家推出首月8折优惠活动...',
    category: { id: 'newcar', name: '新车资讯', color: '#10b981' },
    image: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-14',
    views: 1876,
    comments: 34,
    likes: 89,
    isHot: true,
    isTop: false,
    source: '易车网'
  },
  {
    id: 3,
    title: '清明小长假租车预订量同比增长150%',
    summary: '随着清明假期临近，租车市场迎来预订高峰。数据显示，今年清明假期租车预订量同比增长150%...',
    category: { id: 'industry', name: '行业动态', color: '#409eff' },
    image: 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-13',
    views: 3210,
    comments: 78,
    likes: 156,
    isHot: true,
    isTop: false,
    source: '36氪'
  },
  {
    id: 4,
    title: '五一出游季：长租7天以上享7折优惠',
    summary: '五一假期将至，易租车推出长租优惠活动，租车7天以上可享7折优惠，更有机会抽取免费租车券...',
    category: { id: 'promotion', name: '优惠活动', color: '#f56c6c' },
    image: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-12',
    views: 4567,
    comments: 123,
    likes: 234,
    isHot: true,
    isTop: false,
    source: '易租车官方'
  },
  {
    id: 5,
    title: '租车保险怎么选？这份指南请收好',
    summary: '很多新手租车时对保险选择很困惑，今天我们请来保险专家详细讲解各种保险的覆盖范围...',
    category: { id: 'knowledge', name: '用车知识', color: '#8b5cf6' },
    image: 'https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-11',
    views: 2890,
    comments: 67,
    likes: 145,
    isHot: false,
    isTop: false,
    source: '知乎专栏'
  },
  {
    id: 6,
    title: '交通部：简化租车手续，推进电子证照互认',
    summary: '交通运输部近日发布通知，要求各地简化租车手续，推进驾驶证电子证照跨地区互认...',
    category: { id: 'policy', name: '政策法规', color: '#f59e0b' },
    image: 'https://images.unsplash.com/photo-1485291571150-772bcfc10da5?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-10',
    views: 1987,
    comments: 45,
    likes: 89,
    isHot: false,
    isTop: false,
    source: '人民网'
  },
  {
    id: 7,
    title: '特斯拉Model Y成为最受欢迎租赁车型',
    summary: '根据易租车平台数据显示，特斯拉Model Y连续三个月成为最受欢迎租赁车型...',
    category: { id: 'industry', name: '行业动态', color: '#409eff' },
    image: 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-09',
    views: 2341,
    comments: 56,
    likes: 112,
    isHot: false,
    isTop: false,
    source: '汽车之家'
  },
  {
    id: 8,
    title: '新能源车租赁指南：续航、充电全解析',
    summary: '第一次租新能源车？这篇指南帮你了解续航里程、充电桩分布、注意事项等...',
    category: { id: 'knowledge', name: '用车知识', color: '#8b5cf6' },
    image: 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-08',
    views: 1654,
    comments: 34,
    likes: 67,
    isHot: false,
    isTop: false,
    source: '懂车帝'
  },
  {
    id: 9,
    title: '周末租车特惠：SUV车型88元/天起',
    summary: '周末出行好选择！多款热门SUV车型特惠，最低仅需88元/天，限时抢购...',
    category: { id: 'promotion', name: '优惠活动', color: '#f56c6c' },
    image: 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1200&q=80',
    date: '2024-03-07',
    views: 3876,
    comments: 98,
    likes: 187,
    isHot: true,
    isTop: false,
    source: '易租车官方'
  }
])

// 置顶资讯
const topNews = computed(() => {
  return allNews.value.find((item) => item.isTop) || null
})

// 过滤后的资讯
const filteredNews = computed(() => {
  let result = [...allNews.value]

  // 排除置顶资讯（置顶单独显示）
  result = result.filter((item) => !item.isTop)

  // 分类筛选
  if (currentCategory.value !== 'all') {
    result = result.filter((item) => item.category.id === currentCategory.value)
  }

  return result
})

// 分页资讯
const paginatedNews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredNews.value.slice(start, end)
})

// 是否有更多
const hasMore = computed(() => {
  return paginatedNews.value.length < filteredNews.value.length
})

// 加载更多
const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    currentPage.value++
    loading.value = false
  }, 500)
}

// 查看详情
const goToNewsDetail = (id) => {
  router.push(`/front/news/${id}`)
}

// 监听分类变化
watch(currentCategory, () => {
  currentPage.value = 1
})
</script>

<style scoped>
.news-container {
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

/* 分类标签 */
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 30px;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf2f7;
}

.category-tab {
  padding: 8px 20px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}

.category-tab:hover {
  color: #c8a165;
  border-color: #c8a165;
}

.category-tab.active {
  background: #c8a165;
  color: white;
  border-color: #c8a165;
}

/* 置顶资讯 */
.top-news {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.top-news:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.05);
  border-color: #c8a165;
}

.top-news__img {
  position: relative;
  height: 240px;
}

.top-news__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.top-news__badge {
  position: absolute;
  left: 16px;
  top: 16px;
  padding: 6px 16px;
  background: #c8a165;
  color: white;
  font-size: 13px;
  font-weight: 600;
  border-radius: 30px;
}

.top-news__content {
  padding: 24px 24px 24px 0;
  display: flex;
  flex-direction: column;
}

.top-news__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.top-news__category {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
}

.top-news__date {
  font-size: 13px;
  color: #94a3b8;
}

.top-news__title {
  font-size: 24px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 12px;
  line-height: 1.4;
}

.top-news__summary {
  font-size: 15px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
}

.top-news__stats {
  display: flex;
  gap: 24px;
  color: #94a3b8;
  font-size: 14px;
}

.top-news__stats span {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 资讯网格 */
.news-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.news-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.news-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.03);
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
  right: 12px;
  top: 12px;
  padding: 4px 12px;
  background: #f56c6c;
  color: white;
  font-size: 12px;
  font-weight: 500;
  border-radius: 30px;
}

.news-card__content {
  padding: 16px;
}

.news-card__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.news-card__category {
  padding: 4px 10px;
  border-radius: 30px;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card__summary {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.news-card__stats {
  display: flex;
  gap: 12px;
  color: #94a3b8;
  font-size: 12px;
}

.news-card__stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-card__source {
  font-size: 12px;
  color: #94a3b8;
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 30px;
}

.load-more-btn {
  padding: 12px 40px;
  border-radius: 30px;
  border: 1px solid #c8a165;
  background: transparent;
  color: #c8a165;
  font-weight: 500;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background: #c8a165;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(200, 161, 101, 0.2);
}

.no-more {
  color: #94a3b8;
  font-size: 14px;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 1100px) {
  .news-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .top-news {
    grid-template-columns: 1fr;
  }

  .top-news__content {
    padding: 20px;
  }

  .top-news__img {
    height: 200px;
  }

  .news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .news-grid {
    grid-template-columns: 1fr;
  }

  .category-tabs {
    gap: 8px;
  }

  .category-tab {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>
