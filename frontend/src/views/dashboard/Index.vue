<template>
  <div class="app-container">
    <h1>仪表盘</h1>
    <p>欢迎使用物资申领管理系统</p>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header">快捷操作</div>
          <div>
            <el-button type="primary" @click="$router.push('/applications/create')">发起申领</el-button>
            <el-button type="success" @click="$router.push('/materials')">物资管理</el-button>
            <el-button type="warning" @click="$router.push('/inventory')">库存管理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header">系统概览</div>
          <div>
            <p>物资种类: {{ statistics.totalMaterials }}</p>
            <p>总库存数量: {{ statistics.totalStockQuantity }}</p>
            <p>低库存物资: {{ statistics.lowStockMaterials }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header">最近申领</div>
          <div>
            <p v-if="recentApplications.length === 0">暂无最近申领</p>
            <ul v-else>
              <li v-for="app in recentApplications" :key="app.id">{{ app.materialName }} - {{ app.quantity }}</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as materialApi from '@/api/material'
import * as applicationApi from '@/api/application'

export default {
  name: 'Dashboard',
  data() {
    return {
      statistics: {
        totalMaterials: 0,
        totalStockQuantity: 0,
        lowStockMaterials: 0
      },
      recentApplications: []
    }
  },
  created() {
    this.loadStatistics()
    this.loadRecentApplications()
  },
  methods: {
    loadStatistics() {
      materialApi.getMaterialList({ limit: 1000 }).then(response => {
        const materials = response.data.list || response.data
        this.statistics.totalMaterials = materials.length
        this.statistics.totalStockQuantity = materials.reduce((sum, item) => sum + (item.currentStock || 0), 0)
        this.statistics.lowStockMaterials = materials.filter(item => item.stockStatus === 'LOW').length
      })
    },
    loadRecentApplications() {
      applicationApi.getMyApplications({ limit: 5 }).then(response => {
        this.recentApplications = response.data.list || response.data
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  h1 {
    margin-bottom: 10px;
  }
  p {
    color: #666;
  }
}
</style>