<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <sidebar class="sidebar-container" :class="{ 'sidebar-collapsed': !sidebar.opened }" />

    <div class="main-container" :class="{ 'main-container-collapsed': !sidebar.opened }">
      <!-- 导航栏 -->
      <navbar />

      <!-- 主内容区 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import Sidebar from './components/Sidebar'
import Navbar from './components/Navbar'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    Navbar
  },
  computed: {
    sidebar() {
      return this.$store.getters.sidebar
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  background-color: $background-color;
}

.sidebar-container {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: $sidebar-width;
  background: $surface-color;
  border-right: 1px solid $border-color;
  transition: width $transition-slow;
  z-index: 1001;
  display: flex;
  flex-direction: column;

  &.sidebar-collapsed {
    width: $sidebar-collapsed-width;
  }
}

.main-container {
  min-height: 100vh;
  transition: margin-left $transition-slow;
  margin-left: $sidebar-width;
  display: flex;
  flex-direction: column;

  &.main-container-collapsed {
    margin-left: $sidebar-collapsed-width;
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  background-color: $background-color;
  min-height: calc(100vh - #{$navbar-height});
}
</style>
