<template>
  <div class="app-container">
    <h2>算法分析</h2>
    <p class="description">选择以下算法进行分析计算，提升物资管理效率。</p>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="algorithm-card" shadow="hover">
          <div slot="header" class="clearfix">
            <span class="algorithm-title">ABC分类分析</span>
          </div>
          <div class="algorithm-icon">
            <i class="el-icon-sort" />
          </div>
          <div class="algorithm-description">
            根据物资消耗金额进行ABC分类，识别重要物资。
          </div>
          <div class="algorithm-action">
            <el-button type="primary" @click="$router.push('/algorithms/abc')">开始分析</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="algorithm-card" shadow="hover">
          <div slot="header" class="clearfix">
            <span class="algorithm-title">需求预测</span>
          </div>
          <div class="algorithm-icon">
            <i class="el-icon-trend-charts" />
          </div>
          <div class="algorithm-description">
            基于历史消耗数据预测未来需求，辅助采购决策。
          </div>
          <div class="algorithm-action">
            <el-button type="primary" @click="$router.push('/algorithms/forecast')">开始分析</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="algorithm-card" shadow="hover">
          <div slot="header" class="clearfix">
            <span class="algorithm-title">补货点计算</span>
          </div>
          <div class="algorithm-icon">
            <i class="el-icon-shopping-cart-full" />
          </div>
          <div class="algorithm-description">
            计算物资的安全库存和补货点，避免缺货。
          </div>
          <div class="algorithm-action">
            <el-button type="primary" @click="$router.push('/algorithms/reorder')">开始分析</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="algorithm-card" shadow="hover">
          <div slot="header" class="clearfix">
            <span class="algorithm-title">异常检测</span>
          </div>
          <div class="algorithm-icon">
            <i class="el-icon-warning-outline" />
          </div>
          <div class="algorithm-description">
            检测库存和消耗异常，及时发现异常波动。
          </div>
          <div class="algorithm-action">
            <el-button type="primary" @click="$router.push('/algorithms/anomaly')">开始分析</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="schedule-section">
      <h3>批量计算任务</h3>
      <p class="description">您可以安排以下算法批量计算，系统将在后台执行并更新结果。</p>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="never">
            <div slot="header" class="clearfix">
              <span>ABC分类批量计算</span>
            </div>
            <div>对所有物资进行ABC分类计算。</div>
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="scheduleAbcClassification">安排计算</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div slot="header" class="clearfix">
              <span>补货点批量计算</span>
            </div>
            <div>计算所有物资的补货点。</div>
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="scheduleReorderPointCalculation">安排计算</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <div slot="header" class="clearfix">
              <span>需求预测批量计算</span>
            </div>
            <div>预测所有物资的未来需求。</div>
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="scheduleDemandForecast">安排计算</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'

export default {
  name: 'AlgorithmIndex',
  methods: {
    scheduleAbcClassification() {
      this.$confirm('确认安排ABC分类批量计算吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        algorithmApi.scheduleAbcClassification().then(response => {
          this.$message.success('已安排ABC分类批量计算')
        })
      })
    },
    scheduleReorderPointCalculation() {
      this.$confirm('确认安排补货点批量计算吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        algorithmApi.scheduleReorderPointCalculation().then(response => {
          this.$message.success('已安排补货点批量计算')
        })
      })
    },
    scheduleDemandForecast() {
      this.$confirm('确认安排需求预测批量计算吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        algorithmApi.scheduleDemandForecast().then(response => {
          this.$message.success('已安排需求预测批量计算')
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
h2 {
  margin-bottom: 10px;
}
.description {
  color: #606266;
  margin-bottom: 30px;
}
.algorithm-card {
  margin-bottom: 20px;
  text-align: center;
}
.algorithm-title {
  font-weight: bold;
  font-size: 16px;
}
.algorithm-icon {
  font-size: 48px;
  color: #409EFF;
  margin: 20px 0;
}
.algorithm-description {
  color: #606266;
  margin-bottom: 20px;
  min-height: 60px;
}
.algorithm-action {
  margin-top: 10px;
}
.schedule-section {
  margin-top: 40px;
}
</style>