<template>
  <div class="sidebar">
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import SidebarItem from './SidebarItem'
import { mapGetters } from 'vuex'

export default {
  components: { SidebarItem },
  computed: {
    ...mapGetters(['sidebar']),
    routes() {
      // 根据用户角色过滤路由
      const role = this.$store.getters.role
      return this.$router.options.routes
        .find(route => route.path === '/')
        .children.filter(route => {
          if (!route.meta || !route.meta.role) {
            return true
          }
          return route.meta.role.includes(role)
        })
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: 100%;
  height: 100%;
  background-color: #304156;

  .scrollbar-wrapper {
    overflow-x: hidden !important;

    ::v-deep .el-scrollbar__view {
      height: 100%;
    }
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
  }
}
</style>