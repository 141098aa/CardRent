<template>
  <div class="news-detail-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <el-empty description="资讯不存在或已删除" />
    </div>

    <!-- 资讯内容 -->
    <div v-else class="news-detail">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/front/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/front/news' }">租车资讯</el-breadcrumb-item>
          <el-breadcrumb-item>{{ news.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 文章卡片 -->
      <div class="article-card">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ news.title }}</h1>

          <div class="article-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ news.source }}
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ news.date }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ news.views }} 阅读
            </span>
            <span class="meta-item">
              <el-icon><ChatLineRound /></el-icon>
              {{ news.comments }} 评论
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ news.likes }} 点赞
            </span>
          </div>

          <div class="article-category">
            <el-tag
              :style="{ backgroundColor: news.category.color + '20', color: news.category.color, border: 'none' }">
              {{ news.category.name }}
            </el-tag>
            <el-tag v-if="news.isHot" type="danger" effect="light" size="small">热门</el-tag>
            <el-tag v-if="news.isTop" type="warning" effect="light" size="small">置顶</el-tag>
          </div>
        </div>

        <!-- 文章封面图 -->
        <div class="article-cover">
          <el-image :src="news.image" fit="cover" class="cover-image" />
        </div>

        <!-- 文章内容 -->
        <div class="article-content" v-html="news.content"></div>

        <!-- 文章标签 -->
        <div class="article-tags" v-if="news.tags && news.tags.length">
          <span class="tags-label">标签：</span>
          <el-tag v-for="tag in news.tags" :key="tag" size="small" class="article-tag">{{ tag }}</el-tag>
        </div>

        <!-- 文章操作 -->
        <div class="article-actions">
          <el-button :type="isLiked ? 'danger' : 'default'" @click="handleLike" :loading="likeLoading">
            <el-icon><Star /></el-icon>
            {{ isLiked ? '已点赞' : '点赞' }} ({{ news.likes }})
          </el-button>
          <el-button @click="handleShare">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
          <el-button @click="handleCollect" :type="isCollected ? 'warning' : 'default'">
            <el-icon><StarFilled v-if="isCollected" /><Star v-else /></el-icon>
            {{ isCollected ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <!-- 相关推荐 -->
        <div class="related-news" v-if="relatedNews.length">
          <h3 class="related-title">相关推荐</h3>
          <div class="related-grid">
            <div v-for="item in relatedNews" :key="item.id" class="related-card" @click="goToNewsDetail(item.id)">
              <div class="related-card__img">
                <img :src="item.image" :alt="item.title" />
              </div>
              <div class="related-card__content">
                <h4 class="related-card__title">{{ item.title }}</h4>
                <p class="related-card__summary">{{ item.summary }}</p>
                <div class="related-card__meta">
                  <span>{{ item.date }}</span>
                  <span>{{ item.views }}阅读</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h3 class="comment-title">评论 ({{ news.comments || 0 }})</h3>

          <!-- 评论输入框 -->
          <div class="comment-input">
            <el-avatar
              :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              size="default" />
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              class="comment-textarea"
              maxlength="200"
              show-word-limit />
          </div>
          <div class="comment-actions">
            <el-button type="primary" @click="submitComment" :loading="commentLoading">发表评论</el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list" v-if="comments.length">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :src="comment.avatar" size="default" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-name">{{ comment.userName }}</span>
                  <span class="comment-time">{{ comment.time }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
                <div class="comment-footer">
                  <span class="comment-reply" @click="replyTo(comment)">回复</span>
                  <span class="comment-like" @click="likeComment(comment)">
                    <el-icon><Star /></el-icon>
                    {{ comment.likes || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-comments">
            <el-empty description="暂无评论，快来抢沙发吧！" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, View, ChatLineRound, Star, StarFilled, Share } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref(false)
const likeLoading = ref(false)
const commentLoading = ref(false)
const isLiked = ref(false)
const isCollected = ref(false)
const commentText = ref('')

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))

// 评论列表
const comments = ref([
  {
    id: 1,
    userName: '张先生',
    avatar: 'https://randomuser.me/api/portraits/men/1.jpg',
    content: '这篇文章写得太好了，对我很有帮助！',
    time: '2024-03-15 14:30',
    likes: 5
  },
  {
    id: 2,
    userName: '李女士',
    avatar: 'https://randomuser.me/api/portraits/women/2.jpg',
    content: '请问文中提到的优惠政策现在还能享受吗？',
    time: '2024-03-15 10:20',
    likes: 2
  }
])

// 资讯数据
const news = ref(null)

// 相关推荐
const relatedNews = ref([])

// 加载资讯详情
const loadNewsDetail = (id) => {
  loading.value = true
  error.value = false

  // 模拟API请求
  setTimeout(() => {
    const newsData = {
      id: parseInt(id),
      title: '新能源汽车租赁补贴政策延至2025年',
      summary: '近日，国家发改委等三部门联合发布通知，将新能源汽车租赁补贴政策延长至2025年底...',
      content: `
        <p>近日，国家发展改革委、工业和信息化部、财政部等三部门联合发布《关于延续新能源汽车租赁补贴政策的通知》，明确将新能源汽车租赁补贴政策延长至2025年12月31日。</p>
        <p>通知指出，为进一步推动新能源汽车产业发展，促进绿色低碳出行，决定将新能源汽车租赁补贴政策实施期限延长至2025年底。补贴对象为在符合条件的租赁企业租赁新能源乘用车的个人用户。</p>
        <p><strong>补贴标准：</strong></p>
        <ul>
          <li>租赁期限在7天（含）以上的，按租赁费用的15%给予补贴，单笔最高不超过500元；</li>
          <li>租赁期限在30天（含）以上的，按租赁费用的20%给予补贴，单笔最高不超过1500元；</li>
          <li>租赁期限在90天（含）以上的，按租赁费用的25%给予补贴，单笔最高不超过3000元。</li>
        </ul>
        <p><strong>申请条件：</strong></p>
        <ol>
          <li>申请人需年满18周岁，持有有效身份证件；</li>
          <li>持有有效机动车驾驶证；</li>
          <li>在符合条件的租赁企业租赁新能源乘用车；</li>
          <li>租赁合同真实有效，且已实际履行。</li>
        </ol>
        <p>通知强调，各地要加强对补贴资金的监督管理，确保补贴资金专款专用，防止骗取、挪用补贴资金等行为。同时，鼓励各地结合实际情况，制定更加优惠的地方性补贴政策。</p>
        <p>业内专家表示，此次补贴政策的延续将进一步降低消费者租用新能源汽车的成本，促进新能源汽车的推广应用，对推动绿色出行、减少碳排放具有积极意义。</p>
      `,
      category: { id: 'policy', name: '政策法规', color: '#f59e0b' },
      image: 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=1200&q=80',
      date: '2024-03-15',
      views: 2345,
      comments: 56,
      likes: 128,
      isHot: true,
      isTop: true,
      source: '新华社',
      tags: ['新能源汽车', '补贴政策', '政策解读']
    }

    news.value = newsData

    // 加载相关推荐
    loadRelatedNews(newsData.category.id, newsData.id)

    loading.value = false
  }, 500)
}

// 加载相关推荐
const loadRelatedNews = (categoryId, currentId) => {
  // 模拟相关推荐数据
  relatedNews.value = [
    {
      id: 2,
      title: '比亚迪海豹06正式上市，首月可享8折租车优惠',
      summary: '比亚迪全新车型海豹06正式上市，易租车平台独家推出首月8折优惠活动...',
      image: 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=1200&q=80',
      date: '2024-03-14',
      views: 1876
    },
    {
      id: 3,
      title: '清明小长假租车预订量同比增长150%',
      summary: '随着清明假期临近，租车市场迎来预订高峰...',
      image: 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?auto=format&fit=crop&w=1200&q=80',
      date: '2024-03-13',
      views: 3210
    },
    {
      id: 6,
      title: '交通部：简化租车手续，推进电子证照互认',
      summary: '交通运输部近日发布通知，要求各地简化租车手续...',
      image: 'https://images.unsplash.com/photo-1485291571150-772bcfc10da5?auto=format&fit=crop&w=1200&q=80',
      date: '2024-03-10',
      views: 1987
    }
  ]
}

// 点赞处理
const handleLike = () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }

  likeLoading.value = true
  setTimeout(() => {
    isLiked.value = !isLiked.value
    if (isLiked.value) {
      news.value.likes++
      ElMessage.success('点赞成功')
    } else {
      news.value.likes--
      ElMessage.success('取消点赞')
    }
    likeLoading.value = false
  }, 300)
}

// 分享处理
const handleShare = () => {
  // 复制链接到剪贴板
  navigator.clipboard
    .writeText(window.location.href)
    .then(() => {
      ElMessage.success('链接已复制，可以分享给好友')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

// 收藏处理
const handleCollect = () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }

  isCollected.value = !isCollected.value
  ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
}

// 提交评论
const submitComment = () => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }

  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true

  setTimeout(() => {
    const newComment = {
      id: comments.value.length + 1,
      userName: user.value.name || '用户',
      avatar: user.value.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      content: commentText.value,
      time: new Date().toLocaleString('zh-CN', { hour12: false }),
      likes: 0
    }

    comments.value.unshift(newComment)
    news.value.comments++
    commentText.value = ''
    commentLoading.value = false
    ElMessage.success('评论发表成功')
  }, 500)
}

// 回复评论
const replyTo = (comment) => {
  commentText.value = `@${comment.userName} `
}

// 点赞评论
const likeComment = (comment) => {
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return
  }
  comment.likes = (comment.likes || 0) + 1
}

// 跳转到相关资讯
const goToNewsDetail = (id) => {
  router.push(`/front/news/${id}`)
}

// 监听路由参数
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadNewsDetail(newId)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.news-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
}

/* 面包屑 */
.breadcrumb {
  margin-bottom: 20px;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  border: 1px solid #edf2f7;
}

/* 文章头部 */
.article-header {
  margin-bottom: 30px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e2a3a;
  margin: 0 0 16px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf2f7;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #64748b;
}

.meta-item .el-icon {
  font-size: 16px;
  color: #c8a165;
}

.article-category {
  display: flex;
  gap: 8px;
}

/* 文章封面 */
.article-cover {
  margin-bottom: 30px;
  border-radius: 16px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

/* 文章内容 */
.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #4a5568;
  margin-bottom: 30px;
}

.article-content p {
  margin-bottom: 20px;
}

.article-content h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 30px 0 15px;
}

.article-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 25px 0 12px;
}

.article-content ul,
.article-content ol {
  margin-bottom: 20px;
  padding-left: 20px;
}

.article-content li {
  margin-bottom: 8px;
}

.article-content blockquote {
  margin: 20px 0;
  padding: 16px 20px;
  background: #f8fafc;
  border-left: 4px solid #c8a165;
  border-radius: 8px;
  font-style: italic;
  color: #475569;
}

.article-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
}

/* 文章标签 */
.article-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 0;
  border-top: 1px solid #edf2f7;
  border-bottom: 1px solid #edf2f7;
  margin-bottom: 20px;
}

.tags-label {
  font-size: 14px;
  color: #64748b;
}

.article-tag {
  cursor: pointer;
}

/* 文章操作 */
.article-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 40px;
}

/* 相关推荐 */
.related-news {
  margin-bottom: 40px;
}

.related-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 20px;
  position: relative;
  padding-left: 12px;
}

.related-title::before {
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

.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.related-card {
  background: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-card:hover {
  transform: translateY(-2px);
  border-color: #c8a165;
  box-shadow: 0 10px 20px rgba(200, 161, 101, 0.1);
}

.related-card__img {
  height: 120px;
  overflow: hidden;
}

.related-card__img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-card__content {
  padding: 12px;
}

.related-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-card__summary {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
}

/* 评论区 */
.comment-section {
  border-top: 2px solid #edf2f7;
  padding-top: 30px;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin: 0 0 20px;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-textarea {
  flex: 1;
}

.comment-actions {
  text-align: right;
  margin-bottom: 30px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
}

.comment-time {
  font-size: 12px;
  color: #94a3b8;
}

.comment-text {
  font-size: 14px;
  color: #4a5568;
  margin: 0 0 8px;
  line-height: 1.6;
}

.comment-footer {
  display: flex;
  gap: 16px;
}

.comment-reply,
.comment-like {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
}

.comment-reply:hover,
.comment-like:hover {
  color: #c8a165;
}

.no-comments {
  padding: 30px 0;
}

/* 加载和错误状态 */
.loading-state,
.error-state {
  padding: 60px 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .article-card {
    padding: 30px;
  }

  .article-title {
    font-size: 28px;
  }

  .article-meta {
    gap: 12px;
  }

  .related-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .article-card {
    padding: 20px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    flex-direction: column;
    gap: 8px;
  }

  .cover-image {
    height: 250px;
  }

  .article-actions {
    flex-direction: column;
  }

  .related-grid {
    grid-template-columns: 1fr;
  }

  .comment-input {
    flex-direction: column;
  }

  .comment-item {
    flex-direction: column;
  }
}
</style>
