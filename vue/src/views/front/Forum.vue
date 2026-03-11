<template>
  <div class="forum-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">租车论坛</h1>
      <p class="page-subtitle">交流经验 · 分享心得 · 避坑指南</p>
    </div>

    <!-- 论坛导航和发帖按钮 -->
    <div class="forum-nav">
      <div class="nav-tabs">
        <span class="nav-tab" :class="{ active: currentTab === 'latest' }" @click="switchTab('latest')">最新</span>
        <span class="nav-tab" :class="{ active: currentTab === 'hot' }" @click="switchTab('hot')">热门</span>
        <span class="nav-tab" :class="{ active: currentTab === 'recommend' }" @click="switchTab('recommend')"
          >推荐</span
        >
        <span class="nav-tab" :class="{ active: currentTab === 'following' }" @click="switchTab('following')"
          >关注</span
        >
      </div>
      <el-button type="primary" class="post-btn" @click="showPostDialog = true">
        <el-icon><Edit /></el-icon> 发布帖子
      </el-button>
    </div>

    <!-- 分类标签 -->
    <div class="category-tags">
      <span class="category-tag" :class="{ active: currentCategory === 'all' }" @click="currentCategory = 'all'"
        >全部</span
      >
      <span
        v-for="category in categories"
        :key="category.id"
        class="category-tag"
        :class="{ active: currentCategory === category.id }"
        @click="currentCategory = category.id">
        {{ category.name }}
      </span>
    </div>

    <!-- 论坛主体：两列布局 -->
    <div class="forum-main">
      <!-- 左侧：帖子列表 -->
      <div class="posts-section">
        <!-- 帖子列表 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="filteredPosts.length === 0" class="empty-state">
          <el-empty description="暂无帖子" />
        </div>

        <div v-else class="posts-list">
          <div v-for="post in paginatedPosts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
            <!-- 帖子头部 -->
            <div class="post-header">
              <div class="post-author">
                <img :src="post.author.avatar" class="author-avatar" />
                <div class="author-info">
                  <span class="author-name">{{ post.author.name }}</span>
                  <span class="post-time">{{ post.time }}</span>
                </div>
              </div>
              <div
                class="post-category"
                :style="{ backgroundColor: post.category.color + '20', color: post.category.color }">
                {{ post.category.name }}
              </div>
            </div>

            <!-- 帖子内容 -->
            <div class="post-content">
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ post.excerpt }}</p>

              <!-- 帖子图片预览 -->
              <div v-if="post.images && post.images.length > 0" class="post-images">
                <div
                  v-for="(img, index) in post.images.slice(0, 3)"
                  :key="index"
                  class="post-image"
                  :style="{ backgroundImage: `url(${img})` }"></div>
                <div v-if="post.images.length > 3" class="post-image-more">+{{ post.images.length - 3 }}</div>
              </div>
            </div>

            <!-- 帖子统计 -->
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ post.views }}
              </span>
              <span class="stat-item">
                <el-icon><ChatLineRound /></el-icon>
                {{ post.comments }}
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ post.likes }}
              </span>
              <span class="stat-item" v-if="post.isTop">
                <el-icon><Top /></el-icon>
                置顶
              </span>
              <span class="stat-item" v-if="post.isEssence">
                <el-icon><Medal /></el-icon>
                精华
              </span>
            </div>

            <!-- 最后回复信息 -->
            <div class="post-footer">
              <span class="last-reply"> 最后回复：{{ post.lastReply.time }} · {{ post.lastReply.user }} </span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredPosts.length"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
        </div>
      </div>

      <!-- 右侧：侧边栏 -->
      <div class="sidebar">
        <!-- 搜索框 -->
        <div class="sidebar-card">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索帖子、作者..."
              class="search-input"
              clearable
              @keyup.enter="handleSearch">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 热门标签 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">热门标签</h4>
          <div class="hot-tags">
            <span v-for="tag in hotTags" :key="tag" class="hot-tag" @click="handleTagClick(tag)">
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 活跃用户 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">活跃用户</h4>
          <div class="active-users">
            <div v-for="user in activeUsers" :key="user.id" class="active-user">
              <img :src="user.avatar" class="user-avatar-small" />
              <span class="user-name">{{ user.name }}</span>
              <span class="user-posts">{{ user.posts }} 帖</span>
            </div>
          </div>
        </div>

        <!-- 论坛规则 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">论坛规则</h4>
          <ul class="rule-list">
            <li>禁止发布广告和垃圾信息</li>
            <li>尊重他人，友善交流</li>
            <li>分享真实租车经历</li>
            <li>禁止恶意刷帖和灌水</li>
          </ul>
        </div>

        <!-- 友情链接 -->
        <div class="sidebar-card">
          <h4 class="sidebar-title">友情链接</h4>
          <div class="friend-links">
            <a href="#" class="friend-link">易租车官网</a>
            <a href="#" class="friend-link">租车攻略</a>
            <a href="#" class="friend-link">自驾游社区</a>
            <a href="#" class="friend-link">汽车之家</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布帖子弹窗 -->
    <el-dialog v-model="showPostDialog" title="发布新帖子" width="600px" class="post-dialog">
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="newPost.title" placeholder="请输入帖子标题" />
        </el-form-item>

        <el-form-item label="分类" required>
          <el-select v-model="newPost.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input v-model="newPost.content" type="textarea" :rows="6" placeholder="请输入帖子内容" />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload action="#" list-type="picture-card" :auto-upload="false" :limit="9" multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPostDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPost">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, View, ChatLineRound, Star, Top, Medal, Search, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const currentTab = ref('latest')
const currentCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const showPostDialog = ref(false)

// 分类数据
const categories = ref([
  { id: 'experience', name: '租车体验', color: '#409eff' },
  { id: 'question', name: '问题求助', color: '#f59e0b' },
  { id: 'guide', name: '攻略分享', color: '#10b981' },
  { id: 'avoid', name: '避坑指南', color: '#f56c6c' },
  { id: 'news', name: '行业资讯', color: '#8b5cf6' },
  { id: 'chat', name: '闲聊灌水', color: '#ec4899' }
])
// 处理标签点击
const handleTagClick = (tag) => {
  searchKeyword.value = tag
  handleSearch()
}
// 帖子数据
const allPosts = ref([
  {
    id: 1,
    title: '三亚租车避坑指南｜千万不要被坑了！',
    excerpt: '刚从三亚回来，分享这次租车的真实经历，希望大家都能避开这些坑...',
    content: '',
    category: { id: 'avoid', name: '避坑指南', color: '#f56c6c' },
    author: {
      id: 101,
      name: '自驾游小王子',
      avatar: 'https://randomuser.me/api/portraits/men/1.jpg'
    },
    time: '2小时前',
    views: 2345,
    comments: 56,
    likes: 128,
    isTop: true,
    isEssence: true,
    images: [
      'https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=800&q=80'
    ],
    lastReply: {
      time: '10分钟前',
      user: '老司机'
    }
  },
  {
    id: 2,
    title: '租车第一次上高速需要注意什么？新手求教',
    excerpt: '驾照刚拿不久，想租车出去玩，第一次上高速有点紧张，有什么需要注意的吗？',
    category: { id: 'question', name: '问题求助', color: '#f59e0b' },
    author: {
      id: 102,
      name: '新手上路',
      avatar: 'https://randomuser.me/api/portraits/women/2.jpg'
    },
    time: '昨天',
    views: 1234,
    comments: 89,
    likes: 45,
    isTop: false,
    isEssence: false,
    images: [],
    lastReply: {
      time: '30分钟前',
      user: '老司机'
    }
  },
  {
    id: 3,
    title: '特斯拉Model Y长途自驾体验｜3000公里真实感受',
    excerpt: '租了一周Model Y，从北京开到青岛，聊聊真实续航、充电体验和驾驶感受...',
    category: { id: 'experience', name: '租车体验', color: '#409eff' },
    author: {
      id: 103,
      name: '电车爱好者',
      avatar: 'https://randomuser.me/api/portraits/men/3.jpg'
    },
    time: '3天前',
    views: 3456,
    comments: 112,
    likes: 256,
    isTop: false,
    isEssence: true,
    images: [
      'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80'
    ],
    lastReply: {
      time: '1小时前',
      user: '特斯拉车主'
    }
  },
  {
    id: 4,
    title: '租车平台对比｜哪家最划算？',
    excerpt: '整理了几个主流租车平台的优缺点和价格对比，帮助大家选择最适合的平台...',
    category: { id: 'guide', name: '攻略分享', color: '#10b981' },
    author: {
      id: 104,
      name: '省钱小能手',
      avatar: 'https://randomuser.me/api/portraits/women/4.jpg'
    },
    time: '5天前',
    views: 5678,
    comments: 234,
    likes: 345,
    isTop: true,
    isEssence: true,
    images: [],
    lastReply: {
      time: '2小时前',
      user: '精打细算'
    }
  },
  {
    id: 5,
    title: '租车保险到底怎么选？一文看懂',
    excerpt: '很多新手不知道租车保险怎么选，今天详细讲解各种保险的覆盖范围和注意事项...',
    category: { id: 'guide', name: '攻略分享', color: '#10b981' },
    author: {
      id: 105,
      name: '保险专家',
      avatar: 'https://randomuser.me/api/portraits/men/5.jpg'
    },
    time: '1周前',
    views: 4321,
    comments: 67,
    likes: 189,
    isTop: false,
    isEssence: true,
    images: [],
    lastReply: {
      time: '昨天',
      user: '新手小白'
    }
  },
  {
    id: 6,
    title: '分享一个租车遇到的事故处理流程',
    excerpt: '上次租车出了小事故，记录一下整个处理流程，希望能帮到大家...',
    category: { id: 'avoid', name: '避坑指南', color: '#f56c6c' },
    author: {
      id: 106,
      name: '经历者',
      avatar: 'https://randomuser.me/api/portraits/women/6.jpg'
    },
    time: '2周前',
    views: 2109,
    comments: 45,
    likes: 78,
    isTop: false,
    isEssence: false,
    images: [],
    lastReply: {
      time: '3天前',
      user: '安全第一'
    }
  }
])

// 热门标签
const hotTags = ref([
  '避坑指南',
  '特斯拉',
  '新能源',
  '自驾游',
  '新手必看',
  '保险攻略',
  '长途驾驶',
  '平台对比',
  '违章处理'
])

// 活跃用户
const activeUsers = ref([
  { id: 1, name: '老司机', avatar: 'https://randomuser.me/api/portraits/men/1.jpg', posts: 156 },
  { id: 2, name: '自驾游达人', avatar: 'https://randomuser.me/api/portraits/men/2.jpg', posts: 89 },
  { id: 3, name: '车评人', avatar: 'https://randomuser.me/api/portraits/men/3.jpg', posts: 67 },
  { id: 4, name: '小白成长记', avatar: 'https://randomuser.me/api/portraits/women/4.jpg', posts: 45 },
  { id: 5, name: '资深玩家', avatar: 'https://randomuser.me/api/portraits/men/5.jpg', posts: 234 }
])

// 新帖子表单
const newPost = reactive({
  title: '',
  category: '',
  content: ''
})

// 过滤帖子
const filteredPosts = computed(() => {
  let result = [...allPosts.value]

  // 分类筛选
  if (currentCategory.value !== 'all') {
    result = result.filter((post) => post.category.id === currentCategory.value)
  }

  // 标签筛选
  if (currentTab.value === 'hot') {
    result = result.sort((a, b) => b.views - a.views)
  } else if (currentTab.value === 'recommend') {
    result = result.filter((post) => post.isEssence)
  } else if (currentTab.value === 'following') {
    // 模拟关注用户的帖子
    const followingIds = [101, 103, 105]
    result = result.filter((post) => followingIds.includes(post.author.id))
  }

  // 搜索关键词
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (post) =>
        post.title.toLowerCase().includes(keyword) ||
        post.excerpt.toLowerCase().includes(keyword) ||
        post.author.name.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 分页帖子
const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredPosts.value.slice(start, end)
})

// 切换标签
const switchTab = (tab) => {
  currentTab.value = tab
  currentPage.value = 1
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
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

// 查看帖子详情
const goToPostDetail = (id) => {
  router.push(`/front/forum/${id}`)
}

// 发布帖子
const submitPost = () => {
  if (!newPost.title || !newPost.category || !newPost.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  ElMessage.success('发布成功，等待审核')
  showPostDialog.value = false
  // 重置表单
  newPost.title = ''
  newPost.category = ''
  newPost.content = ''
}

// 监听筛选条件变化
watch([currentCategory, currentTab, searchKeyword], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.forum-container {
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

/* 论坛导航 */
.forum-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.nav-tabs {
  display: flex;
  gap: 8px;
  background: #f8fafc;
  padding: 4px;
  border-radius: 40px;
  border: 1px solid #edf2f7;
}

.nav-tab {
  padding: 8px 20px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-tab:hover {
  color: #c8a165;
}

.nav-tab.active {
  background: #c8a165;
  color: white;
}

.post-btn {
  height: 40px;
  border-radius: 40px;
  background: #c8a165;
  border: none;
  padding: 0 20px;
}

.post-btn:hover {
  background: #b28b4f;
}

/* 分类标签 */
.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.category-tag {
  padding: 6px 16px;
  border-radius: 30px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-tag:hover {
  border-color: #c8a165;
  color: #c8a165;
}

.category-tag.active {
  background: #c8a165;
  border-color: #c8a165;
  color: white;
}

/* 论坛主体 */
.forum-main {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

/* 帖子列表 */
.posts-section {
  min-width: 0;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.03);
  border-color: #c8a165;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
}

.post-time {
  font-size: 12px;
  color: #94a3b8;
}

.post-category {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 500;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 8px;
}

.post-excerpt {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.post-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: #f1f5f9;
}

.post-image-more {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #64748b;
}

.post-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #94a3b8;
}

.stat-item .el-icon {
  font-size: 16px;
}

.post-footer {
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.last-reply {
  font-size: 12px;
  color: #94a3b8;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: white;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 20px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 16px;
  position: relative;
  padding-left: 10px;
}

.sidebar-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: #c8a165;
  border-radius: 2px;
}

/* 搜索框 */
.search-box {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 40px;
  padding-left: 20px;
  height: 40px;
}

/* 热门标签 */
.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  padding: 4px 12px;
  border-radius: 30px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-tag:hover {
  background: #c8a165;
  color: white;
  border-color: #c8a165;
}

/* 活跃用户 */
.active-users {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.active-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  flex: 1;
  font-size: 14px;
  color: #1e2a3a;
  font-weight: 500;
}

.user-posts {
  font-size: 12px;
  color: #94a3b8;
}

/* 论坛规则 */
.rule-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rule-list li {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
  font-size: 13px;
  color: #64748b;
  border-bottom: 1px dashed #edf2f7;
}

.rule-list li:last-child {
  border-bottom: none;
}

.rule-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #c8a165;
  font-weight: bold;
}

/* 友情链接 */
.friend-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.friend-link {
  font-size: 13px;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s ease;
}

.friend-link:hover {
  color: #c8a165;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 弹窗样式 */
:deep(.post-dialog .el-dialog__header) {
  border-bottom: 1px solid #edf2f7;
  padding: 20px;
}

:deep(.post-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
}

:deep(.post-dialog .el-dialog__body) {
  padding: 20px;
}

:deep(.post-dialog .el-dialog__footer) {
  border-top: 1px solid #edf2f7;
  padding: 20px;
}

/* 空状态和加载 */
.loading-state,
.empty-state {
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .forum-main {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: -1;
  }
}

@media (max-width: 600px) {
  .forum-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .nav-tabs {
    width: 100%;
    justify-content: center;
  }

  .post-btn {
    width: 100%;
  }

  .post-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .post-stats {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
