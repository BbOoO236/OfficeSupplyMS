/**
 * 系统模块定义
 * 每个模块对应顶部导航栏的一个图标
 */
export const modules = [
  {
    id: 'dashboard',
    name: '仪表盘',
    icon: 'el-icon-s-home',
    pathPrefix: '/dashboard',
    description: '系统概览和统计信息'
  },
  {
    id: 'materials',
    name: '物资管理',
    icon: 'el-icon-box',
    pathPrefix: '/materials',
    description: '管理物资信息，包括新增、编辑、查询物资'
  },
  {
    id: 'applications',
    name: '申领管理',
    icon: 'el-icon-document',
    pathPrefix: '/applications',
    description: '处理物资申领申请，包括我的申领和申领审批'
  },
  {
    id: 'inventory',
    name: '库存管理',
    icon: 'el-icon-office-building',
    pathPrefix: '/inventory',
    description: '管理库存操作，包括入库、出库、盘点'
  },
  {
    id: 'users',
    name: '用户管理',
    icon: 'el-icon-user',
    pathPrefix: '/users',
    description: '管理系统用户，包括新增、编辑、删除用户'
  },
  {
    id: 'algorithms',
    name: '算法分析',
    icon: 'el-icon-cpu',
    pathPrefix: '/algorithms',
    description: '使用算法进行数据分析，包括ABC分类、需求预测等'
  },
  {
    id: 'reports',
    name: '数据报表',
    icon: 'el-icon-data-analysis',
    pathPrefix: '/reports',
    description: '查看系统数据报表和统计图表'
  }
]

/**
 * 根据路由路径获取对应的模块ID
 * @param {string} path 路由路径
 * @returns {string} 模块ID
 */
export function getModuleByPath(path) {
  if (!path) return 'dashboard'

  // 按路径前缀匹配
  for (const module of modules) {
    if (path.startsWith(module.pathPrefix)) {
      return module.id
    }
  }

  // 默认返回仪表盘
  return 'dashboard'
}

/**
 * 根据模块ID获取模块信息
 * @param {string} moduleId 模块ID
 * @returns {Object} 模块对象
 */
export function getModuleById(moduleId) {
  return modules.find(m => m.id === moduleId) || modules[0]
}