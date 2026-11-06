import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'
import { getModuleByPath } from '@/constants/modules'

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
        meta: { title: '物资管理', icon: 'el-icon-box', role: ['admin', 'employee'] }
      },
      {
        path: 'materials/add',
        name: 'MaterialAdd',
        component: () => import('@/views/material/Form.vue'),
        meta: { title: '新增物资', role: ['admin'] }
      },
      {
        path: 'materials/edit/:id',
        name: 'MaterialEdit',
        component: () => import('@/views/material/Form.vue'),
        meta: { title: '编辑物资', role: ['admin'], hideInSidebar: true }
      },
      {
        path: 'materials/:id',
        name: 'MaterialDetail',
        component: () => import('@/views/material/Detail.vue'),
        meta: { title: '物资详情', role: ['admin', 'employee'], hideInSidebar: true }
      },
      // 申领管理
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/application/Index.vue'),
        meta: { title: '申领管理', icon: 'el-icon-document', role: ['admin'] }
      },
      {
        path: 'applications/my',
        name: 'MyApplications',
        component: () => import('@/views/application/MyApplications.vue'),
        meta: { title: '我的申领', role: ['employee'] }
      },
      {
        path: 'applications/records',
        name: 'MyRecords',
        component: () => import('@/views/application/MyRecords.vue'),
        meta: { title: '申领记录', role: ['employee'] }
      },
      {
        path: 'applications/todo',
        name: 'MyTodo',
        component: () => import('@/views/application/Todo.vue'),
        meta: { title: '待办事项', role: ['employee'] }
      },
      {
        path: 'applications/create',
        name: 'ApplicationCreate',
        component: () => import('@/views/application/Form.vue'),
        meta: { title: '发起申领', role: ['employee', 'admin'] }
      },
      {
        path: 'applications/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/application/Detail.vue'),
        meta: { title: '申领详情', role: ['admin', 'employee'], hideInSidebar: true }
      },
      // 库存管理
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/Index.vue'),
        meta: { title: '库存管理', icon: 'el-icon-office-building', role: ['admin'] }
      },
      {
        path: 'inventory/stock-in',
        name: 'StockIn',
        component: () => import('@/views/inventory/StockIn.vue'),
        meta: { title: '入库管理', role: ['admin'] }
      },
      {
        path: 'inventory/stock-out',
        name: 'StockOut',
        component: () => import('@/views/inventory/StockOut.vue'),
        meta: { title: '出库管理', role: ['admin'] }
      },
      {
        path: 'inventory/check',
        name: 'InventoryCheck',
        component: () => import('@/views/inventory/Check.vue'),
        meta: { title: '库存盘点', role: ['admin'] }
      },
      // 部门管理
      {
        path: 'departments',
        name: 'Departments',
        component: () => import('@/views/department/Index.vue'),
        meta: { title: '部门管理', icon: 'el-icon-office-building', role: ['admin'] }
      },
      // 用户管理
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', icon: 'el-icon-user', role: ['admin'] }
      },
      {
        path: 'users/add',
        name: 'UserAdd',
        component: () => import('@/views/user/Form.vue'),
        meta: { title: '新增用户', role: ['admin'] }
      },
      {
        path: 'users/edit/:id',
        name: 'UserEdit',
        component: () => import('@/views/user/Form.vue'),
        meta: { title: '编辑用户', role: ['admin'], hideInSidebar: true }
      },
      // 算法分析
      {
        path: 'algorithms',
        name: 'Algorithms',
        component: () => import('@/views/algorithm/Index.vue'),
        meta: { title: '算法分析', icon: 'el-icon-cpu', role: ['admin'] }
      },
      {
        path: 'algorithms/abc',
        name: 'AlgorithmABC',
        component: () => import('@/views/algorithm/ABC.vue'),
        meta: { title: 'ABC分类分析', role: ['admin'] }
      },
      {
        path: 'algorithms/forecast',
        name: 'AlgorithmForecast',
        component: () => import('@/views/algorithm/Forecast.vue'),
        meta: { title: '需求预测', role: ['admin'] }
      },
      {
        path: 'algorithms/reorder',
        name: 'AlgorithmReorder',
        component: () => import('@/views/algorithm/Reorder.vue'),
        meta: { title: '补货点计算', role: ['admin'] }
      },
      {
        path: 'algorithms/anomaly',
        name: 'AlgorithmAnomaly',
        component: () => import('@/views/algorithm/Anomaly.vue'),
        meta: { title: '异常检测', role: ['admin'] }
      },
      // 数据报表
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/report/Index.vue'),
        meta: { title: '数据报表', icon: 'el-icon-data-analysis', role: ['admin'] }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人信息', icon: 'el-icon-user' }
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
  //验证token
    // console.log('当前路径:', to.path)
    // console.log('requiresAuth:', to.meta.requiresAuth)
    // console.log('当前 token:', store.getters.token)
  // 设置页面标题
  if (to.meta.title) {
    document.title = `OfficeSupplyMS`
  }

  // 检查是否需要登录
// 检查当前路由及其所有父路由是否需要认证
const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

if (requiresAuth) {
    if (store.getters.token) {
      // 检查角色权限
      if (to.meta.role) {
        const userRole = store.getters.role
        //验证路由跳转
        console.log('路由路径:', to.path)
        console.log('路由meta.role:', to.meta.role)
        console.log('用户角色:', userRole)
        console.log('是否包含:', to.meta.role.includes(userRole))
        if (to.meta.role.includes(userRole)) {
          // 设置当前激活模块
          if (to.path !== '/') {
            const moduleId = getModuleByPath(to.path)
            // 只有当模块发生变化时才更新
            if (store.state.activeModule !== moduleId) {
              store.commit('SET_ACTIVE_MODULE', moduleId)
            }
          }
          next()
        } else {
          next('/404')
        }
      } else {
        // 设置当前激活模块
        if (to.path !== '/') {
          const moduleId = getModuleByPath(to.path)
          // 只有当模块发生变化时才更新
          if (store.state.activeModule !== moduleId) {
            store.commit('SET_ACTIVE_MODULE', moduleId)
          }
        }
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