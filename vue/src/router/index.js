import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/front/home' },
    {
      path: '/manager',
      component: () => import('@/views/Layout.vue'),
      redirect: '/manager/home',
      children: [
        // 仪表盘
        { path: 'home', component: () => import('@/views/manager/dashboard/Home.vue') },

        // 用户管理
        { path: 'user', component: () => import('@/views/manager/user/User.vue') },
        { path: 'admin', component: () => import('@/views/manager/user/Admin.vue') },
        { path: 'user-auth', component: () => import('@/views/manager/user/UserAuth.vue') },

        // 车辆管理
        { path: 'car/list', component: () => import('@/views/manager/car/CarList.vue') },
        { path: 'car/category', component: () => import('@/views/manager/car/CarCategory.vue') },
        { path: 'car/brand', component: () => import('@/views/manager/car/CarBrand.vue') },

        // 订单管理
        { path: 'order', component: () => import('@/views/manager/order/OrderList.vue') },

        // 财务管理
        { path: 'income-expense', component: () => import('@/views/manager/finance/IncomeExpense.vue') },
        { path: 'refund', component: () => import('@/views/manager/finance/Refund.vue') },
        { path: 'deposit', component: () => import('@/views/manager/finance/Deposit.vue') },
        { path: 'statistics', component: () => import('@/views/manager/finance/Statistics.vue') },

        // 个人中心
        { path: 'person', component: () => import('@/views/manager/profile/Person.vue') },
        { path: 'password', component: () => import('@/views/manager/profile/Password.vue') },

        //内容管理
        { path: 'banner', component: () => import('@/views/manager/content/Banner.vue') }
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        // 公共页面
        { path: 'home', component: () => import('@/views/front/home/Home.vue') },
        { path: 'guide', component: () => import('@/views/front/guide/Guide.vue') },
        { path: 'about', component: () => import('@/views/front/about/About.vue') },

        // 租车相关
        { path: 'rental', component: () => import('@/views/front/rental/Rental.vue') },
        { path: 'car/:id', component: () => import('@/views/front/car/CarDetail.vue') },

        //  社区互动
        { path: 'news', component: () => import('@/views/front/news/News.vue') },
        { path: 'news/:id', component: () => import('@/views/front/news/NewsDetail.vue'), meta: { title: '资讯详情' } },

        //  个人中心
        { path: 'person', component: () => import('@/views/front/user/Person.vue'), meta: { requiresAuth: true } },
        { path: 'password', component: () => import('@/views/front/user/Password.vue'), meta: { requiresAuth: true } },
        {
          path: 'favorites',
          component: () => import('@/views/front/user/Favorites.vue'),
          meta: { requiresAuth: true }
        },
        { path: 'orders', component: () => import('@/views/front/user/Orders.vue'), meta: { requiresAuth: true } },
        { path: 'recharge', component: () => import('@/views/front/user/Recharge.vue'), meta: { requiresAuth: true } },
        {
          path: 'set-payment-password',
          component: () => import('@/views/front/user/SetPaymentPassword.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/front/forget-payment-password',
          component: () => import('@/views/front/user/ForgetPaymentPassword.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/front/messages',
          component: () => import('@/views/front/user/Messages.vue'),
          meta: { requiresAuth: true }
        }
      ]
    },
    { path: '/login', component: () => import('@/views/auth/Login.vue') },
    { path: '/register', component: () => import('@/views/auth/Register.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const user = JSON.parse(localStorage.getItem('system-user') || 'null')
    if (!user) {
      // 未登录，跳转到登录页，并携带目标路径
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
