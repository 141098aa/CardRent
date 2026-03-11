import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue') },
        { path: 'admin', component: () => import('@/views/manager/Admin.vue') },
        { path: 'user', component: () => import('@/views/manager/User.vue') },
        { path: 'person', component: () => import('@/views/manager/Person.vue') },
        { path: 'password', component: () => import('@/views/manager/Password.vue') }
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue') },
        { path: 'person', component: () => import('@/views/front/Person.vue') },
        { path: 'password', component: () => import('@/views/front/Password.vue') },
        { path: 'rental', component: () => import('@/views/front/Rental.vue') },
        { path: 'forum', component: () => import('@/views/front/Forum.vue') },
        { path: 'news', component: () => import('@/views/front/News.vue') },
        { path: 'guide', component: () => import('@/views/front/Guide.vue') },
        { path: 'about', component: () => import('@/views/front/About.vue') },
        { path: 'favorites', component: () => import('@/views/front/Favorites.vue') },
        { path: 'recharge', component: () => import('@/views/front/Recharge.vue') },
        { path: 'set-payment-password', component: () => import('@/views/front/SetPaymentPassword.vue') },
        { path: 'car/:id', component: () => import('@/views/front/CarDetail.vue') },
        { path: 'orders', component: () => import('@/views/front/Orders.vue'), meta: { requiresAuth: true } },
        { path: 'news/:id', component: () => import('@/views/front/NewsDetail.vue'), meta: { title: '资讯详情' } }
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') }
  ]
})

export default router
