<template>
  <div class="messages-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">站内信</h1>
      <!-- <p class="page-subtitle">系统通知 · 订单提醒 · 退款通知</p> -->
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="message-stats">
        共有 <span class="stats-number">{{ messages.length }}</span> 条消息
        <span v-if="unreadCount > 0" class="unread-tip"
          >，其中 <span class="unread-count">{{ unreadCount }}</span> 条未读</span
        >
      </div>
      <div class="action-buttons">
        <el-button v-if="unreadCount > 0" type="primary" plain size="small" @click="markAllAsRead">
          <el-icon><Check /></el-icon>
          全部标为已读
        </el-button>
        <el-button v-if="messages.length > 0" type="danger" plain size="small" @click="clearAllMessages">
          <el-icon><Delete /></el-icon>
          清空所有消息
        </el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="messages-list">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息">
          <template #extra>
            <el-button type="primary" class="go-home-btn" @click="goToHome">返回首页</el-button>
          </template>
        </el-empty>
      </div>

      <div v-else class="message-items">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-card"
          :class="{ unread: !message.isRead }"
          @click="handleMessageClick(message)">
          <!-- 消息图标 -->
          <div class="message-icon" :class="getIconClass(message.type)">
            <el-icon><component :is="getIcon(message.type)" /></el-icon>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            <div class="message-header">
              <div class="message-title">
                <span class="title-text">{{ message.title }}</span>
                <span v-if="!message.isRead" class="unread-badge">未读</span>
              </div>
              <div class="message-time">{{ formatDateTime(message.createTime) }}</div>
            </div>

            <div class="message-body">
              <p class="message-text">{{ message.content }}</p>
            </div>

            <div class="message-footer">
              <el-button v-if="message.link" type="primary" link size="small" @click.stop="goToLink(message.link)">
                查看详情
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="500px">
      <div v-if="currentMessage" class="message-detail">
        <div class="detail-header">
          <div class="detail-title">{{ currentMessage.title }}</div>
          <div class="detail-time">{{ formatDateTime(currentMessage.createTime) }}</div>
        </div>
        <div class="detail-content">{{ currentMessage.content }}</div>
        <div v-if="currentMessage.link" class="detail-link">
          <el-button type="primary" @click="goToLink(currentMessage.link)">查看详情</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Bell, ShoppingCart, CreditCard, Check, Delete, ArrowRight, Wallet } from '@element-plus/icons-vue'
import { messageApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const messages = ref([])
const unreadCount = ref(0)
const detailVisible = ref(false)
const currentMessage = ref(null)

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '--'
  return datetime.replace('T', ' ').substring(0, 19)
}

// 根据消息类型获取图标
const getIcon = (type) => {
  const iconMap = {
    system: 'Bell',
    refund: 'CreditCard',
    order: 'ShoppingCart',
    deposit: 'Wallet'
  }
  return iconMap[type] || 'Message'
}

// 获取图标样式类
const getIconClass = (type) => {
  const classMap = {
    system: 'system',
    refund: 'refund',
    order: 'order',
    deposit: 'deposit'
  }
  return classMap[type] || 'system'
}

// 加载消息列表
const loadMessages = async () => {
  loading.value = true
  try {
    const res = await messageApi.getMessages()
    if (res.code === '200') {
      messages.value = res.data || []
      // 统计未读数量
      unreadCount.value = messages.value.filter((m) => !m.isRead).length
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

// 标记单条消息为已读
const markAsRead = async (id) => {
  try {
    const res = await messageApi.markAsRead(id)
    if (res.code === '200') {
      // 更新本地状态
      const message = messages.value.find((m) => m.id === id)
      if (message && !message.isRead) {
        message.isRead = true
        unreadCount.value--
        // 触发导航栏更新未读数量
        await updateNavUnreadCount()
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    const res = await messageApi.markAllAsRead()
    if (res.code === '200') {
      messages.value.forEach((m) => {
        m.isRead = true
      })
      unreadCount.value = 0
      ElMessage.success('已全部标记为已读')
      // 触发导航栏更新未读数量
      await updateNavUnreadCount()
    }
  } catch (error) {
    console.error('全部标记已读失败:', error)
    ElMessage.error('操作失败')
  }
}
// 更新导航栏未读数量
const updateNavUnreadCount = async () => {
  try {
    // 触发父组件重新获取未读数量
    const event = new CustomEvent('updateUnreadCount')
    window.dispatchEvent(event)

    // 或者通过 props/emit 如果组件有通信
    // emit('updateUnreadCount')
  } catch (error) {
    console.error('更新未读数量失败:', error)
  }
}
// 清空所有消息
const clearAllMessages = () => {
  ElMessageBox.confirm('确定要清空所有消息吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await messageApi.clearAll()
        if (res.code === '200') {
          messages.value = []
          unreadCount.value = 0
          ElMessage.success('已清空所有消息')
        }
      } catch (error) {
        console.error('清空消息失败:', error)
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

// 点击消息处理
const handleMessageClick = async (message) => {
  currentMessage.value = message
  detailVisible.value = true

  // 如果未读，标记为已读
  if (!message.isRead) {
    await markAsRead(message.id)
  }
}

// 跳转链接
const goToLink = (link) => {
  detailVisible.value = false
  router.push(link)
}

// 返回首页
const goToHome = () => {
  router.push('/front/home')
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.messages-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
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
  margin-bottom: 20px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.message-stats {
  font-size: 14px;
  color: #475569;
}

.stats-number {
  font-size: 18px;
  font-weight: 700;
  color: #c8a165;
  margin: 0 4px;
}

.unread-tip {
  color: #64748b;
}

.unread-count {
  color: #f56c6c;
  font-weight: 600;
}

/* 消息列表 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.message-card:hover {
  border-color: #c8a165;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.message-card.unread {
  background: #fef9e7;
  border-left: 4px solid #c8a165;
}

/* 消息图标 */
.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.message-icon.system {
  background: #e6f0ff;
  color: #409eff;
}

.message-icon.refund {
  background: #ffe6e6;
  color: #f56c6c;
}

.message-icon.order {
  background: #e8f5e9;
  color: #67c23a;
}

.message-icon.deposit {
  background: #fff3e0;
  color: #e6a23c;
}

/* 消息内容 */
.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

.unread-badge {
  font-size: 11px;
  padding: 2px 8px;
  background: #f56c6c;
  color: white;
  border-radius: 20px;
}

.message-time {
  font-size: 12px;
  color: #94a3b8;
}

.message-body {
  margin-bottom: 8px;
}

.message-text {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-footer {
  display: flex;
  justify-content: flex-end;
}

/* 消息详情对话框 */
.message-detail {
  padding: 8px 0;
}

.detail-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf2f7;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 8px;
}

.detail-time {
  font-size: 12px;
  color: #94a3b8;
}

.detail-content {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.detail-link {
  text-align: center;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  padding: 60px 0;
}

.go-home-btn {
  background: #c8a165;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
}

.go-home-btn:hover {
  background: #b28b4f;
}
</style>
