import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/ProductManage.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/CustomerManage.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('@/views/StockManage.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/SalesStatistics.vue'),
        meta: { title: '销售统计' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理', requireAdmin: true }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else if (to.meta.requireAdmin && user && user.role !== 'admin') {
    next('/')
    // 可选：提示无权限
  } else {
    next()
  }
})

export default router
