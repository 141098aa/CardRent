<template>
  <div>
    <div style="height: 60px; background-color: #1e3a5f; display: flex; align-items: center">
      <div style="width: 20%">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px; height: 40px; object-fit: contain" />
          <div
            style="
              font-weight: bold;
              font-size: 20px;
              margin-left: 8px;
              color: #fff;
              display: flex;
              align-items: center;
            ">
            易租车<span style="display: inline-block; margin: 0 4px; transform: translateY(2px); font-size: 28px"
              >·</span
            >轻松出行
          </div>
        </div>
      </div>
      <div style="width: 60%; height: 60px; display: flex; align-items: center">
        <div style="flex: 1">
          <el-menu
            :default-active="router.currentRoute.value.path"
            style="background-color: #1e3a5f"
            ellipsis
            mode="horizontal">
            <el-menu-item index="/front/home">首页</el-menu-item>
            <el-menu-item index="/front/goods">精选</el-menu-item>
          </el-menu>
        </div>
        <div style="width: fit-content" v-if="router.currentRoute.value.path !== ''">
          <el-input
            @keyup.enter="search"
            prefix-icon="Search"
            style="width: 300px; height: 40px"
            placeholder="请输入名称查询"></el-input>
          <el-button type="primary" style="height: 40px; margin-left: 5px">搜索</el-button>
        </div>
      </div>
      <div style="width: 20%; text-align: right; padding-right: 10px">
        <el-dropdown>
          <div style="display: flex; align-items: center">
            <img
              style="width: 40px; height: 40px; border-radius: 50%"
              :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              alt="" />
            <span style="color: #fff; margin-left: 5px">{{ data.user.name || '代码小白' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div>
      <div style="background-color: #f0f2ff">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'
import Footer from '@/components/Footer.vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success('退出成功')
}

//回车搜索
const search = () => {}
//更新front里面的user对象为最新值
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>

<style>
.el-tooltip__trigger {
  cursor: pointer;
  outline: none !important;
}
.el-menu--horizontal {
  border: none !important;
}
.el-menu--horizontal .el-menu-item {
  color: white;
  border: none;
  height: 60px;
}
.el-menu--horizontal .el-menu-item.is-active {
  border: none;
  color: white !important;
  background-color: #0c9c7a !important;
}
.el-menu--horizontal .el-menu-item:not(.is-active):hover {
  color: white;
  background-color: #8fcbbd !important;
}
</style>
