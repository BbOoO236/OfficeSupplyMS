import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

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
    redirect: '/dashboard',
    component: () => import('@/layout/Index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '仪表盘', icon: 'el-icon-s-home' }
      },
      // 物资管理
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/material/Index.vue'),
        meta: { title: '物资管理', icon: 'el-icon-box', role: ['ADMIN'] }
      },
      {
        path: 'materials/add',
        name: 'MaterialAdd',
        component: () => import('@/views/material/Form.vue'),
        meta: { title: '新增物资', role: ['ADMIN'] }
      },
      {
        path: 'materials/edit/:id',
        name: 'MaterialEdit',
        component: () => import('@/views/material/Form.vue'),
        meta: { title: '编辑物资', role: ['ADMIN'] }
      },
      // 申领管理
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/application/Index.vue'),
        meta: { title: '申领管理', icon: 'el-icon-document' }
      },
      {
        path: 'applications/my',
        name: 'MyApplications',
        component: () => import('@/views/application/MyApplications.vue'),
        meta: { title: '我的申领', role: ['USER'] }
      },
      {
        path: 'applications/create',
        name: 'ApplicationCreate',
        component: () => import('@/views/application/Form.vue'),
        meta: { title: '发起申领', role: ['USER'] }
      },
      // 库存管理
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/Index.vue'),
        meta: { title: '库存管理', icon: 'el-icon-office-building', role: ['ADMIN'] }
      },
      {
        path: 'inventory/stock-in',
        name: 'StockIn',
        component: () => import('@/views/inventory/StockIn.vue'),
        meta: { title: '入库管理', role: ['ADMIN'] }
      },
      {
        path: 'inventory/stock-out',
        name: 'StockOut',
        component: () => import('@/views/inventory/StockOut.vue'),
        meta: { title: '出库管理', role: ['ADMIN'] }
      },
      {
        path: 'inventory/check',
        name: 'InventoryCheck',
        component: () => import('@/views/inventory/Check.vue'),
        meta: { title: '库存盘点', role: ['ADMIN'] }
      },
      // 用户管理
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', icon: 'el-icon-user', role: ['ADMIN'] }
      },
      {
        path: 'users/add',
        name: 'UserAdd',
        component: () => import('@/views/user/Form.vue'),
        meta: { title: '新增用户', role: ['ADMIN'] }
      },
      {
        path: 'users/edit/:id',
        name: 'UserEdit',
        component: () => import('@/views/user/Form.vue'),
        meta: { title: '编辑用户', role: ['ADMIN'] }
      },
      // 算法分析
      {
        path: 'algorithms',
        name: 'Algorithms',
        component: () => import('@/views/algorithm/Index.vue'),
        meta: { title: '算法分析', icon: 'el-icon-cpu', role: ['ADMIN'] }
      },
      {
        path: 'algorithms/abc',
        name: 'AlgorithmABC',
        component: () => import('@/views/algorithm/ABC.vue'),
        meta: { title: 'ABC分类分析', role: ['ADMIN'] }
      },
      {
        path: 'algorithms/forecast',
        name: 'AlgorithmForecast',
        component: () => import('@/views/algorithm/Forecast.vue'),
        meta: { title: '需求预测', role: ['ADMIN'] }
      },
      {
        path: 'algorithms/reorder',
        name: 'AlgorithmReorder',
        component: () => import('@/views/algorithm/Reorder.vue'),
        meta: { title: '补货点计算', role: ['ADMIN'] }
      },
      {
        path: 'algorithms/anomaly',
        name: 'AlgorithmAnomaly',
        component: () => import('@/views/algorithm/Anomaly.vue'),
        meta: { title: '异常检测', role: ['ADMIN'] }
      },
      // 数据报表
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/report/Index.vue'),
        meta: { title: '数据报表', icon: 'el-icon-data-analysis', role: ['ADMIN'] }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人中心', icon: 'el-icon-user' }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  },
  {
    path: '*',
    redirect: '/404'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - OfficeSupplyMS`
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (store.getters.token) {
      // 检查角色权限
      if (to.meta.role) {
        const userRole = store.getters.role
        if (to.meta.role.includes(userRole)) {
          next()
        } else {
          next('/404')
        }
      } else {
        next()
      }
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router