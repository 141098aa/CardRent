<template>
  <div class="admin-container">
    <!-- 顶部导航栏 -->
    <div class="admin-header">
      <div class="logo-area">
        <img src="@/assets/imgs/logo.png" alt="" class="logo" />
        <span class="logo-text">汽车租赁管理</span>
      </div>
      <div class="user-area">
        <img
          class="user-avatar"
          :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
          alt="" />
        <span class="user-name">{{ data.user.name || '系统管理员' }}</span>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="admin-main">
      <!-- 侧边菜单栏 - 简化版 -->
      <div class="admin-sidebar">
        <el-menu
          router
          class="admin-menu"
          :default-active="router.currentRoute.value.path"
          :default-openeds="['user', 'content', 'system']"
          background-color="#ffffff"
          text-color="#4a5568"
          active-text-color="#409eff">
          <!-- 系统首页 -->
          <el-menu-item index="/manager/home" class="menu-item">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-sub-menu index="user" class="menu-submenu">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/user" class="submenu-item">
              <el-icon><User /></el-icon>
              <span>普通用户</span>
            </el-menu-item>
            <el-menu-item index="/manager/admin" class="submenu-item">
              <el-icon><UserFilled /></el-icon>
              <span>管理员信息</span>
            </el-menu-item>
            <el-menu-item index="/manager/user-auth" class="submenu-item">
              <el-icon><CircleCheck /></el-icon>
              <span>认证审核</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 车辆管理 -->
          <el-sub-menu index="car" class="menu-submenu">
            <template #title>
              <el-icon><Van /></el-icon>
              <span>车辆管理</span>
            </template>
            <el-menu-item index="/manager/car/list" class="submenu-item">
              <el-icon><List /></el-icon>
              <span>车辆列表</span
              ><!-- 列表页包含添加车辆功能 -->
            </el-menu-item>
            <el-menu-item index="/manager/car/category" class="submenu-item">
              <el-icon><Collection /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="/manager/car/brand" class="submenu-item">
              <el-icon><ShoppingBag /></el-icon>
              <span>品牌管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 订单管理  -->
          <el-menu-item index="/manager/order" class="menu-item">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>

          <!-- 内容管理 -->
          <el-sub-menu index="content" class="menu-submenu">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/manager/forum" class="submenu-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>论坛管理</span>
            </el-menu-item>
            <el-menu-item index="/manager/news" class="submenu-item">
              <el-icon><Reading /></el-icon>
              <span>资讯管理</span>
            </el-menu-item>
            <el-menu-item index="/manager/guide" class="submenu-item">
              <el-icon><InfoFilled /></el-icon>
              <span>服务指南管理</span>
            </el-menu-item>
            <el-menu-item index="/manager/banner" class="submenu-item">
              <el-icon><Picture /></el-icon>
              <span>轮播图管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 财务管理 -->
          <el-sub-menu index="finance" class="menu-submenu">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item index="/manager/recharge" class="submenu-item">
              <el-icon><Wallet /></el-icon>
              <span>充值记录</span>
            </el-menu-item>
            <el-menu-item index="/manager/payment" class="submenu-item">
              <el-icon><CreditCard /></el-icon>
              <span>支付记录</span>
            </el-menu-item>
            <el-menu-item index="/manager/refund" class="submenu-item">
              <el-icon><Refresh /></el-icon>
              <span>退款管理</span>
            </el-menu-item>
            <el-menu-item index="/manager/statistics" class="submenu-item">
              <el-icon><DataLine /></el-icon>
              <span>财务统计</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 系统管理 -->
          <el-sub-menu index="system" class="menu-submenu">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/manager/settings" class="submenu-item">
              <el-icon><Tools /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
            <el-menu-item index="/manager/log" class="submenu-item">
              <el-icon><Histogram /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/manager/backup" class="submenu-item">
              <el-icon><Folder /></el-icon>
              <span>数据备份</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 个人信息 -->
          <el-menu-item index="/manager/person" class="menu-item">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>

          <!-- 修改密码 -->
          <el-menu-item index="/manager/password" class="menu-item">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>

          <!-- 退出系统 -->
          <el-menu-item index="/login" class="menu-item logout-item" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { HomeFilled, User, Lock, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()

// 滚动整个窗口
watch(
  () => route.fullPath,
  () => {
    // 直接滚动窗口到顶部
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })

    // 同时也尝试滚动 html 和 body
    // document.documentElement.scrollTo({
    //   top: 0,
    //   behavior: 'smooth'
    // })
    // document.body.scrollTo({
    //   top: 0,
    //   behavior: 'smooth'
    // })
  }
)

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const logout = () => {
  router.push('/login')
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 顶部导航栏 */
.admin-header {
  height: 60px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.logo-text {
  font-weight: 600;
  font-size: 20px;
  color: #1e2a3a;
  letter-spacing: 0.5px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  /* border-radius: 30px; */
  /* background-color: #f8f9fa; */
  /* border: 1px solid #e9ecef; */
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-right: 4px;
}

/* 主体区域 */
.admin-main {
  display: flex;
  min-height: calc(100vh - 60px);
}

/* 侧边菜单栏 */
.admin-sidebar {
  width: 220px;
  background-color: #ffffff;
  border-right: 1px solid #e9ecef;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.admin-menu {
  border: none !important;
  background-color: #ffffff !important;
}

/* 菜单项样式 */
:deep(.el-menu) {
  border-right: none !important;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  padding: 0 16px !important;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
  color: #64748b;
  transition: color 0.2s ease;
}

/* 鼠标悬停效果 */
:deep(.el-menu-item:hover) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

:deep(.el-menu-item:hover .el-icon) {
  color: #409eff !important;
}

/* 激活状态 */
:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
  font-weight: 500;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #409eff !important;
}

/* 子菜单样式 */
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  padding: 0 16px !important;
  border-radius: 8px;
  font-size: 14px;
  color: #4a5568 !important;
  transition: all 0.2s ease;
}

:deep(.el-sub-menu__title .el-icon) {
  font-size: 18px;
  margin-right: 10px;
  color: #64748b;
  transition: color 0.2s ease;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

:deep(.el-sub-menu__title:hover .el-icon) {
  color: #409eff !important;
}

:deep(.el-sub-menu.is-opened .el-sub-menu__title) {
  background-color: #ffffff;
  color: #409eff !important;
  font-weight: 500;
}

:deep(.el-sub-menu.is-opened .el-sub-menu__title .el-icon) {
  color: #409eff !important;
}

/* 子菜单项 */
:deep(.el-menu--inline .el-menu-item) {
  padding-left: 46px !important;
  background-color: #ffffff !important;
}

:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

:deep(.el-menu--inline .el-menu-item.is-active) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
  font-weight: 500;
}

/* 退出系统特殊样式 */
:deep(.logout-item) {
  margin-top: 20px;
}

:deep(.logout-item:hover) {
  background-color: #ffeded !important;
  color: #f56c6c !important;
}

:deep(.logout-item:hover .el-icon) {
  color: #f56c6c !important;
}

/* 内容区域 */
.admin-content {
  flex: 1;
  width: 0;
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 180px;
  }

  .logo-text {
    font-size: 16px;
  }

  .user-name {
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
