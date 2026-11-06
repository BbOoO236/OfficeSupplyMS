<template>
  <div class="sidebar-item">
    <!-- 简单的侧边栏项 -->
    <div v-if="!item.children">
      <el-menu-item :index="item.path">
        <i v-if="item.meta && item.meta.icon" :class="item.meta.icon"></i>
        <span>{{ item.meta && item.meta.title ? item.meta.title : item.name }}</span>
      </el-menu-item>
    </div>
    <el-submenu v-else :index="item.path">
      <template slot="title">
        <i v-if="item.meta && item.meta.icon" :class="item.meta.icon"></i>
        <span>{{ item.meta && item.meta.title ? item.meta.title : item.name }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="child.path"
      />
    </el-submenu>
  </div>
</template>

<script>
export default {
  name: 'SidebarItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    basePath: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.sidebar-item {
  /* 样式 */
}
</style>