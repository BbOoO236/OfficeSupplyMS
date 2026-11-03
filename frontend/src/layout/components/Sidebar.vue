<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <!-- Logo 区域 -->
    <div class="sidebar-header">
      <div class="logo-wrapper" @click="goHome">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
        </div>
        <span v-if="!isCollapsed" class="logo-text">OfficeSupply</span>
      </div>
      <button class="collapse-btn" @click="toggleSidebar">
        <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
      </button>
    </div>

    <!-- 菜单 -->
    <nav class="sidebar-nav">
      <!-- 首页入口 -->
      <div class="nav-section">
        <div
          class="nav-item first-level-item"
          :class="{ active: isActivePath('/dashboard') }"
          @click="navigateTo('/dashboard')"
        >
          <i class="el-icon-s-home nav-icon"></i>
          <span v-if="!isCollapsed" class="nav-text">仪表盘</span>
        </div>
      </div>

      <!-- 一级菜单分组 -->
      <div
        v-for="group in visibleGroups"
        :key="group.id"
        class="nav-section"
      >
        <!-- 展开状态：显示一级+二级 -->
        <template v-if="!isCollapsed">
          <!-- 一级导航：点击展开/折叠 -->
          <div
            class="nav-item first-level-item"
            :class="{ active: isInGroup(group) }"
            @click="toggleExpand(group.id)"
          >
            <i :class="group.icon" class="nav-icon"></i>
            <span class="nav-text">{{ group.name }}</span>
            <i class="el-icon-arrow-down arrow-icon" :class="{ collapsed: !isExpanded(group.id) }"></i>
          </div>

          <!-- 二级菜单列表 -->
          <ul v-show="isExpanded(group.id)" class="nav-list second-level-list">
            <li
              v-for="item in group.children"
              :key="item.path"
              class="nav-item second-level-item"
              :class="{ active: isActivePath(item.path) }"
              @click="navigateTo(item.path)"
            >
              <span class="nav-text">{{ item.meta.title }}</span>
            </li>
          </ul>
        </template>

        <!-- 缩进状态：显示图标，点击弹出菜单 -->
        <el-dropdown v-if="isCollapsed" trigger="click" @command="navigateTo">
          <div
            class="nav-item first-level-item module-icon-item"
            :class="{ active: isInGroup(group) }"
          >
            <i :class="group.icon" class="nav-icon"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <div class="dropdown-title">{{ group.name }}</div>
            <el-dropdown-item
              v-for="item in group.children"
              :key="item.path"
              :command="item.path"
              :class="{ active: isActivePath(item.path) }"
            >
              {{ item.meta.title }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <!-- 个人中心 -->
      <div class="nav-section nav-section-bottom">
        <div
          class="nav-item first-level-item"
          :class="{ active: isActivePath('/profile') }"
          @click="navigateTo('/profile')"
        >
          <i class="el-icon-user nav-icon"></i>
          <span v-if="!isCollapsed" class="nav-text">个人信息</span>
        </div>
      </div>
    </nav>

    <!-- 用户信息 -->
    <div class="sidebar-footer">
      <div class="user-wrapper" @click="navigateTo('/profile')">
        <div class="user-avatar-wrapper">
          <el-avatar :size="40" :src="userAvatar" />
        </div>
        <div v-if="!isCollapsed" class="user-info">
          <span class="user-name">{{ realName || username }}</span>
          <span class="user-role">{{ role === 'admin' ? '管理员' : '员工' }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Sidebar',
  data() {
    return {
      userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      // 展开状态映射
      expandedMap: {}
    }
  },
  computed: {
    ...mapGetters(['sidebar', 'username', 'realName', 'role']),
    isCollapsed() {
      return !this.sidebar.opened
    },
    // 根据角色过滤后的菜单分组
    visibleGroups() {
      const userRole = this.role
      const allRoutes = this.$router.options.routes.find(r => r.path === '/').children || []

      const groups = [
        { id: 'materials', name: '物资管理', icon: 'el-icon-box', children: [] },
        { id: 'applications', name: '申领管理', icon: 'el-icon-document', children: [] },
        { id: 'inventory', name: '库存管理', icon: 'el-icon-office-building', children: [] },
        { id: 'users', name: '用户管理', icon: 'el-icon-user', children: [] },
        { id: 'algorithms', name: '算法分析', icon: 'el-icon-cpu', children: [] },
        { id: 'reports', name: '数据报表', icon: 'el-icon-data-analysis', children: [] }
      ]

      allRoutes.forEach(route => {
        const path = '/' + route.path
        if (path === '/dashboard' || path === '/profile') return
        if (route.meta && route.meta.role && !route.meta.role.includes(userRole)) return
        // 过滤掉不显示在侧边栏的路由
        if (route.meta && route.meta.hideInSidebar) return

        if (path.startsWith('/materials')) groups[0].children.push(route)
        else if (path.startsWith('/applications')) groups[1].children.push(route)
        else if (path.startsWith('/inventory')) groups[2].children.push(route)
        else if (path.startsWith('/users')) groups[3].children.push(route)
        else if (path.startsWith('/algorithms')) groups[4].children.push(route)
        else if (path.startsWith('/reports')) groups[5].children.push(route)
      })

      return groups.filter(g => g.children.length > 0)
    }
  },
  methods: {
    toggleSidebar() {
      this.$store.commit('TOGGLE_SIDEBAR')
    },
    goHome() {
      this.$router.push('/dashboard')
    },
    toggleExpand(groupId) {
      // 切换展开状态，创建新对象触发 Vue 2 响应式更新
      this.expandedMap = {
        ...this.expandedMap,
        [groupId]: !this.expandedMap[groupId]
      }
    },
    isExpanded(groupId) {
      // 默认为展开状态
      return this.expandedMap[groupId] !== false
    },
    isInGroup(group) {
      return group.children.some(item => this.isActivePath(item.path))
    },
    isActivePath(path) {
      const fullPath = path.startsWith('/') ? path : '/' + path
      return this.$route.path === fullPath
    },
    navigateTo(path) {
      const fullPath = path.startsWith('/') ? path : '/' + path
      // 如果当前路径相同，不重复导航
      if (this.$route.path === fullPath) {
        return
      }
      this.$router.push(fullPath)
    },
    // 初始化展开状态
    initExpanded() {
      this.visibleGroups.forEach(group => {
        if (this.expandedMap[group.id] === undefined) {
          // 默认缩起
          this.$set(this.expandedMap, group.id, false)
        }
      })
    }
  },
  mounted() {
    this.initExpanded()
  },
  watch: {
    '$route.path': {
      handler() {
        // 路由变化时，确保当前分组展开
        this.visibleGroups.forEach(group => {
          if (this.isInGroup(group)) {
            this.$set(this.expandedMap, group.id, true)
          }
        })
      },
      immediate: true
    },
    // 深度监听 expandedMap 变化，确保 v-show 触发更新
    expandedMap: {
      handler() {
        // 触发强制更新以确保 v-show 重新计算
        this.$forceUpdate()
      },
      deep: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: $surface-color;
  transition: width $transition-slow;
  overflow: hidden;
}

.sidebar-header {
  height: $navbar-height;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;

  .logo-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;

    .logo-icon {
      width: 32px;
      height: 32px;
      background: $primary-color;
      border-radius: $border-radius-lg;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      svg {
        width: 18px;
        height: 18px;
        color: white;
      }
    }

    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      white-space: nowrap;
    }
  }

  .collapse-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: transparent;
    border-radius: $border-radius-sm;
    color: $text-secondary;
    cursor: pointer;
    transition: all $transition-fast;
    flex-shrink: 0;

    &:hover {
      background: $background-color;
      color: $primary-color;
    }
  }
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 0;
}

.nav-section {
  margin-bottom: 4px;

  &.nav-section-bottom {
    margin-top: auto;
    border-top: 1px solid $border-color;
    padding-top: 12px;
  }
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 一级菜单 */
.first-level-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  margin: 2px 8px;
  border-radius: $border-radius-base;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-fast;
  border-left: 4px solid transparent;

  &:hover {
    background: $background-color;
    color: $text-primary;
  }

  &.active {
    background: rgba($primary-color, 0.08);
    color: $primary-color;
    border-left-color: $primary-color;

    .nav-icon {
      color: $primary-color;
    }
  }

  .nav-icon {
    font-size: 18px;
    width: 20px;
    text-align: center;
    flex-shrink: 0;
  }

  .nav-text {
    flex: 1;
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
  }

  .arrow-icon {
    font-size: 12px;
    color: $text-muted;
    transition: transform $transition-fast;

    &.collapsed {
      transform: rotate(-90deg);
    }
  }
}

/* 二级菜单 */
.second-level-list {
  margin: 0 8px;
  padding: 4px 0;
  background: $background-color;
  border-radius: 0 0 $border-radius-base $border-radius-base;
}

.second-level-item {
  display: flex;
  align-items: center;
  padding: 8px 16px 8px 44px;
  margin: 0;
  cursor: pointer;
  color: $text-secondary;
  font-size: 13px;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary-color, 0.05);
    color: $primary-color;
  }

  &.active {
    color: $primary-color;
    font-weight: 500;
    background: rgba($primary-color, 0.08);
  }

  .nav-text {
    white-space: nowrap;
  }
}

/* 缩进状态 */
.sidebar-collapsed {
  .sidebar-header {
    justify-content: center;
    padding: 0 8px;

    .logo-wrapper .logo-text {
      display: none;
    }
  }

  .sidebar-nav {
    padding: 12px 0;
  }

  .first-level-item {
    justify-content: center;
    padding: 10px;
    margin: 2px 4px;
    border-left: none;
    border-bottom: 2px solid transparent;

    &.active {
      border-left: none;
      border-bottom-color: $primary-color;
      background: rgba($primary-color, 0.08);
    }

    .nav-icon {
      margin: 0;
    }

    .nav-text,
    .arrow-icon {
      display: none;
    }
  }

  .second-level-list {
    display: none;
  }

  .nav-section-bottom .first-level-item {
    margin: 2px 4px;
  }

  .sidebar-footer {
    padding: 12px 8px;

    .user-wrapper {
      justify-content: center;

      .user-info {
        display: none;
      }
    }
  }
}

/* 下拉菜单 */
::v-deep .dropdown-title {
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: $text-muted;
  border-bottom: 1px solid $border-color;
}

::v-deep .el-dropdown-menu__item {
  padding: 8px 16px;
  font-size: 14px;

  &.active {
    color: $primary-color;
    background: rgba($primary-color, 0.08);
  }
}

/* 用户信息 */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid $border-color;
  flex-shrink: 0;
}

.user-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  margin: -8px;
  border-radius: $border-radius-base;
  transition: background $transition-fast;

  &:hover {
    background: $background-color;
  }

  .user-info {
    display: flex;
    flex-direction: column;

    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: $text-primary;
    }

    .user-role {
      font-size: 12px;
      color: $text-muted;
    }
  }
}
</style>
