<template>
  <div class="error-page">
    <div class="error-code">404</div>
    <div class="error-message">页面不存在</div>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409EFF;
}
.error-message {
  font-size: 24px;
  margin: 20px 0;
  color: #666;
}
</style>