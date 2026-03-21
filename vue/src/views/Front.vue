<template>
  <div>
    <!-- 导航栏  -->
    <div class="navbar">
      <!-- Logo区域 -->
      <div class="logo-wrapper">
        <div class="logo-container">
          <img src="@/assets/imgs/logo.png" alt="" class="logo" />
          <div class="logo-text">易租车<span class="separator">/</span><span class="subtitle">轻松出行</span></div>
        </div>
      </div>

      <!-- 导航菜单和搜索框 -->
      <div class="nav-wrapper">
        <div class="menu-container">
          <el-menu
            router
            :default-active="router.currentRoute.value.path"
            :ellipsis="false"
            class="custom-menu"
            mode="horizontal">
            <el-menu-item index="/front/home"> 首页 </el-menu-item>
            <el-menu-item index="/front/rental">在线租车</el-menu-item>
            <!-- <el-menu-item index="/front/forum">租车论坛</el-menu-item> -->
            <el-menu-item index="/front/news">租车资讯</el-menu-item>
            <el-menu-item index="/front/guide">服务指南</el-menu-item>
            <el-menu-item index="/front/about">关于我们</el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 用户区域 -->
      <div class="user-wrapper">
        <el-dropdown>
          <div class="user-container">
            <img
              class="user-avatar"
              :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              alt="" />
            <span class="user-name">{{ data.user.name || '新用户' }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="dropdown-menu">
              <el-dropdown-item @click="router.push('/front/person')">
                <el-icon class="menu-icon"><User /></el-icon>个人信息
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/recharge')">
                <el-icon class="menu-icon"><Money /></el-icon>我的充值
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/orders')">
                <el-icon class="menu-icon"><List /></el-icon>我的订单
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/favorites')">
                <el-icon class="menu-icon"><Star /></el-icon>我的收藏
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/password')">
                <el-icon class="menu-icon"><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout" class="logout-item">
                <el-icon class="menu-icon"><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 内容区域 -->
    <div>
      <div class="content-bg">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'
import Footer from '@/components/Footer.vue'
import { Search, ArrowDown, User, Lock, SwitchButton, Star, Money, List } from '@element-plus/icons-vue'

const searchText = ref('')

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success('退出成功')
}

// const search = () => {
//   if (searchText.value.trim()) {
//     ElMessage.success(`搜索: ${searchText.value}`)
//   }
// }

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>

<style scoped>
/* 导航栏容器 */
.navbar {
  height: 70px;
  background: #ffffff;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.03);
  border-bottom: 1px solid rgba(200, 161, 101, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  overflow: hidden;
}

/* Logo区域 */
.logo-wrapper {
  flex: 0 0 260px;
  min-width: 220px;
}

.logo-container {
  padding-left: 40px;
  display: flex;
  align-items: center;
  height: 70px;
}

.logo {
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.logo-text {
  font-weight: 500;
  font-size: 20px;
  margin-left: 10px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.separator {
  display: inline-block;
  margin: 0 8px;
  color: #c8a165;
  font-weight: 300;
}

.subtitle {
  font-weight: 300;
  font-size: 16px;
  color: #7f8c8d;
}

/* 导航菜单区域 */
.nav-wrapper {
  flex: 1 1 auto;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
}

.menu-container {
  width: 100%;
  display: flex;
  justify-content: center;
  min-width: 0;
}

:deep(.custom-menu.el-menu--horizontal) {
  border: none !important;
  background: transparent !important;
  height: 70px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  max-width: 100%;
  padding: 0 6px;
  scrollbar-width: none;
}
:deep(.custom-menu.el-menu--horizontal::-webkit-scrollbar) {
  display: none;
}

/* 菜单项 */
:deep(.custom-menu.el-menu--horizontal .el-menu-item) {
  color: #4a5568;
  background: transparent !important;
  border: none !important;

  height: 70px !important;
  line-height: 70px !important;

  padding: 0 16px !important;
  font-size: 15px;
  font-weight: 400;

  border-radius: 8px;
  display: inline-flex;
  align-items: center;

  position: relative;
  z-index: 0;

  transition: color 0.18s ease;
}

:deep(.custom-menu.el-menu--horizontal .el-menu-item:hover) {
  color: #409eff !important;
  background-color: transparent !important;
}
:deep(.custom-menu.el-menu--horizontal .el-menu-item:hover)::before {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  height: 36px;
  border-radius: 10px;
  background: #e6f0ff;
  z-index: -1;
}

:deep(.custom-menu.el-menu--horizontal .el-menu-item.is-active) {
  color: #409eff !important;
  background-color: transparent !important;
  font-weight: 600;
}
:deep(.custom-menu.el-menu--horizontal .el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  height: 36px;
  border-radius: 10px;
  background: #e6f0ff;
  z-index: -1;
}

:deep(.custom-menu.el-menu--horizontal .el-menu-item::after),
:deep(.custom-menu.el-menu--horizontal::after) {
  display: none !important;
  content: none !important;
}

/* 用户区域 */
.user-wrapper {
  flex: 0 0 240px;
  min-width: 200px;
  text-align: right;
  padding-right: 40px;
}

.user-container {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  cursor: pointer;
  height: 44px;
  padding: 4px 12px 4px 8px;
  border-radius: 999px;
  background: #f8f9fa;
  transition:
    background-color 0.18s ease,
    border-color 0.18s ease;
  border: 1px solid #edf2f7;
}

.user-container:hover {
  background: #e6f0ff;
  border-color: #409eff;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #fff;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex: 0 0 auto;
}

.user-name {
  color: #2c3e50;
  margin-left: 8px;
  font-weight: 500;
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  color: #a0aec0;
  margin-left: 6px;
  font-size: 14px;
  flex: 0 0 auto;
}

/* 下拉菜单样式 */
:deep(.dropdown-menu) {
  border-radius: 12px;
  padding: 8px 0;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid #edf2f7;
}

:deep(.dropdown-menu .el-dropdown-menu__item) {
  padding: 8px 20px;
  font-size: 14px;
  color: #4a5568;
  display: flex;
  align-items: center;
}

:deep(.dropdown-menu .el-dropdown-menu__item:hover) {
  background-color: #e6f0ff !important;
  color: #409eff;
}

.logout-item {
  color: #e53e3e !important;
  border-top: 1px solid #edf2f7;
  margin-top: 4px;
}

.logout-item:hover {
  background-color: #ffeded !important;
  color: #e53e3e !important;
}

.menu-icon {
  margin-right: 8px;
  color: #c8a165;
  font-size: 16px;
}

/* 内容区域 */
.content-bg {
  background-color: #f0f2ff;
}

/* 其他 */
:deep(.el-tooltip__trigger) {
  cursor: pointer;
  outline: none !important;
}

.el-dropdown:deep(.el-tooltip__trigger) {
  outline: none !important;
}

/* 小屏优化 */
@media (max-width: 1100px) {
  .logo-container {
    padding-left: 16px;
  }
  .user-wrapper {
    padding-right: 16px;
  }
  .logo-wrapper {
    flex-basis: 220px;
  }
  .user-wrapper {
    flex-basis: 210px;
  }
}
</style>
