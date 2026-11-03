<template>
  <div class="app-main">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'AppMain'
}
</script>

<style scoped>
.app-main {
  min-height: calc(100vh - 60px);
}
</style>
