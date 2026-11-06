<template>
  <div class="material-detail-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb-container">
      <el-breadcrumb-item :to="{ path: '/materials' }">物资管理</el-breadcrumb-item>
      <el-breadcrumb-item>物资详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading" class="detail-content">
      <!-- 物资基础信息 + 算法结果区域 -->
      <div class="info-grid" v-if="material">
        <!-- 基础信息卡片 -->
        <el-card class="info-card" shadow="never">
          <div slot="header" class="card-header">
            <span>物资基础信息</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">物资编码</span>
              <span class="info-value">{{ material.code }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">物资名称</span>
              <span class="info-value">{{ material.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">物资类别</span>
              <span class="info-value">{{ material.categoryName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">规格</span>
              <span class="info-value">{{ material.specification || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单价</span>
              <span class="info-value">¥{{ material.unitPrice }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单位</span>
              <span class="info-value">{{ material.unit || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">采购提前期</span>
              <span class="info-value">{{ material.leadTime || '-' }}天</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前库存</span>
              <span class="info-value">
                <el-tag :type="getStockStatusType(material.currentStock, material.safetyStock)">
                  {{ material.currentStock }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">库存状态</span>
              <span class="info-value">
                <el-tag :type="getStockStatusType(material.currentStock, material.safetyStock)">
                  {{ getStockStatusText(material.currentStock, material.safetyStock) }}
                </el-tag>
              </span>
            </div>
          </div>
        </el-card>

        <!-- 算法结果卡片 -->
        <el-card class="info-card" shadow="never">
          <div slot="header" class="card-header">
            <span>算法分析结果</span>
          </div>
          <div class="info-list">
            <!-- ABC分类 -->
            <div class="info-item">
              <span class="info-label">ABC分类</span>
              <span class="info-value">
                <el-tag v-if="material.abcClass === 'A'" type="danger" class="abc-tag">
                  A类
                </el-tag>
                <el-tag v-else-if="material.abcClass === 'B'" type="warning" class="abc-tag">
                  B类
                </el-tag>
                <el-tag v-else-if="material.abcClass === 'C'" type="success" class="abc-tag">
                  C类
                </el-tag>
                <span v-else class="no-data">未分类</span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">安全库存</span>
              <span class="info-value">{{ material.safetyStock ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">补货点</span>
              <span class="info-value">{{ material.reorderPoint ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">日均消耗量</span>
              <span class="info-value">{{ material.dailyConsumption ?? '-' }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="material">
        <el-button type="primary" size="medium" @click="handleApply">
          <i class="el-icon-plus"></i> 立即申领
        </el-button>
        <el-button size="medium" @click="goBack">
          返回列表
        </el-button>
      </div>

      <!-- 相关推荐 -->
      <el-card class="recommend-card" shadow="never" v-if="recommendations.length > 0">
        <div slot="header" class="card-header">
          <span>同类物资推荐</span>
        </div>
        <div class="recommend-list">
          <div
            v-for="item in recommendations"
            :key="item.id"
            class="recommend-item"
            @click="goToDetail(item.id)"
          >
            <div class="recommend-name">{{ item.name }}</div>
            <div class="recommend-spec">{{ item.specification || '-' }}</div>
            <div class="recommend-price">¥{{ item.unitPrice }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && !material" description="物资不存在或已删除"></el-empty>
  </div>
</template>

<script>
import * as materialApi from '@/api/material'

export default {
  name: 'MaterialDetail',
  data() {
    return {
      loading: false,
      material: null,
      recommendations: []
    }
  },
  created() {
    this.loadMaterialDetail()
  },
  methods: {
    loadMaterialDetail() {
      const materialId = this.$route.params.id || this.$route.query.id
      if (!materialId) {
        this.$message.error('物资ID不存在')
        return
      }

      this.loading = true
      materialApi.getMaterialById(materialId).then(response => {
        this.material = response.data || {}
        this.loading = false
        this.loadRecommendations()
      }).catch(() => {
        this.loading = false
      })
    },
    loadRecommendations() {
      if (!this.material.categoryId) return

      materialApi.getMaterialList({
        category: this.material.categoryId,
        limit: 10
      }).then(response => {
        const list = response.data.list || response.data || []
        // 排除当前物资
        this.recommendations = list
          .filter(item => item.id !== this.material.id)
          .slice(0, 6)
      }).catch(() => {
        // 推荐列表加载失败不影响主内容
      })
    },
    getStockStatusType(currentStock, safetyStock) {
      if (currentStock === 0) return 'danger'
      if (currentStock <= safetyStock) return 'warning'
      return 'success'
    },
    getStockStatusText(currentStock, safetyStock) {
      if (currentStock === 0) return '已缺货'
      if (currentStock <= safetyStock) return '库存紧张'
      return '正常'
    },
    handleApply() {
      if (this.material.status !== 1) {
        this.$message.warning('该物资已停用，无法申领')
        return
      }
      this.$router.push({
        path: '/applications/create',
        query: {
          materialId: this.material.id,
          materialName: this.material.name
        }
      })
    },
    goBack() {
      this.$router.push('/materials')
    },
    goToDetail(id) {
      this.$router.push(`/materials/${id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.material-detail-container {
  padding: 0;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.detail-content {
  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
  }

  .info-card {
    border-radius: $border-radius-xl;

    .card-header {
      font-weight: 600;
      font-size: 16px;
    }

    .info-list {
      .info-item {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid #F0F0F0;

        &:last-child {
          border-bottom: none;
        }

        .info-label {
          color: $text-secondary;
          font-size: 14px;
        }

        .info-value {
          color: $text-primary;
          font-size: 14px;
          font-weight: 500;

          .no-data {
            color: $text-muted;
          }
        }

        .abc-tag {
          font-weight: 600;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .recommend-card {
    border-radius: $border-radius-xl;

    .card-header {
      font-weight: 600;
      font-size: 16px;
    }

    .recommend-list {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;

      .recommend-item {
        padding: 16px;
        border: 1px solid #F0F0F0;
        border-radius: $border-radius-base;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: $primary-color;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .recommend-name {
          font-weight: 500;
          color: $text-primary;
          margin-bottom: 4px;
        }

        .recommend-spec {
          font-size: 12px;
          color: $text-secondary;
          margin-bottom: 8px;
        }

        .recommend-price {
          font-size: 14px;
          font-weight: 600;
          color: $primary-color;
        }
      }
    }
  }
}
</style>
