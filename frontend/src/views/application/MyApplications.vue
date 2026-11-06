<template>
  <div class="app-container">
    <!-- 功能卡片区域 -->
    <div class="card-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="function-card" shadow="hover" @click.native="handleCreate">
            <div class="card-content">
              <i class="el-icon-plus card-icon"></i>
              <h3>发起申领</h3>
              <p>填写申领表单，申请所需物资</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="function-card" shadow="hover" @click.native="goToRecords">
            <div class="card-content">
              <i class="el-icon-tickets card-icon"></i>
              <h3>查看我的申领记录</h3>
              <p>查看历史申领记录及审批状态</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyApplications',
  methods: {
    handleCreate() {
      this.$router.push('/applications/create')
    },
    goToRecords() {
      this.$router.push('/applications/records')
    }
  }
}
</script>

<style lang="scss" scoped>
.card-container {
  margin-bottom: 30px;
}

.function-card {
  cursor: pointer;
  text-align: center;
  padding: 30px 20px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
}

.card-content {
  .card-icon {
    font-size: 48px;
    color: #409EFF;
    margin-bottom: 15px;
  }

  h3 {
    margin: 0 0 10px 0;
    font-size: 20px;
    color: #303133;
  }

  p {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}
</style>