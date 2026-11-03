<template>
  <header class="navbar">
    <div class="navbar-left">
      <!-- 页面标题 -->
      <h1 class="page-title">{{ pageTitle }}</h1>
    </div>

    <div class="navbar-right">
      <!-- 用户菜单 -->
      <el-dropdown trigger="click" @command="handleUserCommand">
        <div class="user-dropdown">
          <el-avatar :size="32" :src="userAvatar" />
          <span class="user-name">{{ realName || username }}</span>
          <i class="el-icon-arrow-down"></i>
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="profile">
            <i class="el-icon-user"></i>个人信息
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <i class="el-icon-switch-button"></i>退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </header>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Navbar',
  data() {
    return {
      userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  computed: {
    ...mapGetters(['username', 'realName']),
    pageTitle() {
      const route = this.$route
      return route.meta?.title || '仪表盘'
    }
  },
  methods: {
    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'logout':
          this.$store.dispatch('logout').then(() => {
            this.$router.push('/login')
          })
          break
        default:
          this.$message.info('功能待开发')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.navbar {
  height: $navbar-height;
  background: $surface-color;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-left {
  .page-title {
    font-family: 'Lexend', sans-serif;
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    margin: 0;
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $background-color;
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
  }

  i {
    font-size: 12px;
    color: $text-muted;
  }
}
</style>
