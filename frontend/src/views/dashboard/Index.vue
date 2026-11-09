<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">早上好，{{ realName || username }}</h1>
        <p class="welcome-desc">
          今天是 {{ currentDate }}，当前已完成 <span class="highlight">{{ statistics.completedApplications }}</span> 项物资申请
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" size="large" @click="$router.push('/applications/create')">
          <i class="el-icon-plus"></i> 新建申请
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" :class="{ 'employee-grid': !isAdmin }">
      <div class="stat-card">
        <div class="stat-icon blue">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-info">
          <span class="stat-label">今日申请</span>
          <span class="stat-value">{{ statistics.todayApplications }}</span>
          <span class="stat-label">条</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon amber">
          <i class="el-icon-time"></i>
        </div>
        <div class="stat-info">
          <span class="stat-label">待审批</span>
          <span class="stat-value">{{ statistics.pendingApprovals }}</span>
          <span class="stat-change">待处理</span>
        </div>
      </div>

      <div class="stat-card" v-if="isAdmin">
        <div class="stat-icon green">
          <i class="el-icon-box"></i>
        </div>
        <div class="stat-info">
          <span class="stat-label">本月消耗</span>
          <span class="stat-value">{{ statistics.monthlyUsage }}</span>
          <span class="stat-change">件物资</span>
        </div>
      </div>

      <div class="stat-card" v-if="isAdmin">
        <div class="stat-icon" :class="statistics.needReorderCount > 0 ? 'red' : 'blue'">
          <i class="el-icon-box"></i>
        </div>
        <div class="stat-info">
          <span class="stat-label">需补货物资</span>
          <span class="stat-value">{{ statistics.needReorderCount }}</span>
          <span class="stat-change">件物资</span>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="dashboard-content">
      <!-- 最近申请 -->
      <div class="content-card">
        <div class="card-header">
          <h3>{{ isAdmin ? '待审批申请' : '我的申领' }}</h3>
          <el-button type="text" @click="$router.push(isAdmin ? '/applications' : '/applications/records')">查看全部</el-button>
        </div>
        <div class="card-body">
          <el-table :data="recentApplications" style="width: 100%" v-loading="listLoading">
            <el-table-column prop="applicationNo" label="单号" width="140" />
            <el-table-column prop="materialName" label="物资名称" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="applicationTime" label="时间" width="160" />
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="{row}">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="recentApplications.length === 0 && !listLoading" class="empty-state">
            <p>暂无申请记录</p>
          </div>
        </div>
      </div>

      <!-- 右侧卡片 -->
      <div class="sidebar-cards">
        <!-- 快捷入口 -->
        <div class="content-card">
          <div class="card-header">
            <h3>快捷入口</h3>
          </div>
          <div class="card-body">
            <div class="quick-actions">
              <div class="quick-action-item" @click="$router.push('/applications/create')">
                <div class="action-icon primary">
                  <i class="el-icon-plus"></i>
                </div>
                <span>新建申请</span>
              </div>
              <template v-if="isAdmin">
                <div class="quick-action-item" @click="$router.push('/materials')">
                  <div class="action-icon success">
                    <i class="el-icon-goods"></i>
                  </div>
                  <span>物资管理</span>
                </div>
                <div class="quick-action-item" @click="$router.push('/inventory')">
                  <div class="action-icon warning">
                    <i class="el-icon-box"></i>
                  </div>
                  <span>库存管理</span>
                </div>
                <div class="quick-action-item" @click="$router.push('/reports')">
                  <div class="action-icon info">
                    <i class="el-icon-data-line"></i>
                  </div>
                  <span>数据报表</span>
                </div>
              </template>
              <template v-else>
                <div class="quick-action-item" @click="$router.push('/applications/my')">
                  <div class="action-icon success">
                    <i class="el-icon-document"></i>
                  </div>
                  <span>我的申领</span>
                </div>
                <div class="quick-action-item" @click="$router.push('/applications/records')">
                  <div class="action-icon warning">
                    <i class="el-icon-tickets"></i>
                  </div>
                  <span>申领记录</span>
                </div>
                <div class="quick-action-item" @click="$router.push('/materials')">
                  <div class="action-icon info">
                    <i class="el-icon-goods"></i>
                  </div>
                  <span>物资查询</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 库存预警 -->
    <div v-if="lowStockMaterials.length > 0" class="content-card alert-card">
      <div class="card-header warning">
        <div class="header-left">
          <i class="el-icon-warning"></i>
          <h3>库存预警</h3>
        </div>
        <el-button type="text">查看全部</el-button>
      </div>
      <div class="card-body">
        <div class="alert-list">
          <div v-for="item in lowStockMaterials" :key="item.id" class="alert-item">
            <div class="alert-icon">
              <i class="el-icon-box"></i>
            </div>
            <div class="alert-info">
              <span class="alert-name">{{ item.name }}</span>
              <span class="alert-stock">剩余 {{ item.currentStock }} {{ item.unit }} / 最低 {{ item.minStock }} {{ item.unit }}</span>
            </div>
            <el-button size="small" type="primary" @click="handleRestock(item)">补货</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import * as dashboardApi from '@/api/dashboard'
import moment from 'moment'

export default {
  name: 'Dashboard',
  data() {
    return {
      statistics: {
        todayApplications: 0,
        pendingApprovals: 0,
        monthlyUsage: 0,
        needReorderCount: 0,
        completedApplications: 0
      },
      recentApplications: [],
      lowStockMaterials: [],
      listLoading: false,
      pendingApprovalList: [],
      pendingOutboundList: [],
      warnings: [],
      consumptionTrend: []
    }
  },
  computed: {
    ...mapGetters(['username', 'realName', 'role']),
    isAdmin() {
      return this.role === 'admin'
    },
    currentDate() {
      return moment().format('YYYY年M月D日 dddd')
    }
  },
  created() {
    this.loadDashboardData()
  },
  methods: {
    loadDashboardData() {
      this.listLoading = true
      if (this.isAdmin) {
        this.loadAdminDashboard()
      } else {
        this.loadEmployeeDashboard()
      }
    },
    loadAdminDashboard() {
      dashboardApi.getAdminDashboard().then(response => {
        const data = response.data || {}
        const stats = data.stats || {}

        // 映射统计数据
        this.statistics.todayApplications = stats.todayApplications || 0
        this.statistics.pendingApprovals = stats.pendingApprovals || stats.pendingApproval || 0
        this.statistics.monthlyUsage = stats.monthlyUsage || 0
        this.statistics.needReorderCount = stats.needReorderCount || 0
        this.statistics.completedApplications = stats.completedApplications || 0

        // 待审批列表作为最近申请
        this.recentApplications = (data.pendingApprovalList || []).map(item => ({
          applicationNo: item.applicationNo,
          materialName: item.materialName,
          quantity: item.quantity,
          applicationTime: item.applicationTime,
          status: item.status
        }))

        // 待出库列表
        this.pendingOutboundList = data.pendingOutboundList || []

        // 库存预警列表
        this.warnings = data.warnings || []

        // 消耗趋势
        this.consumptionTrend = data.consumptionTrend || []

        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    loadEmployeeDashboard() {
      dashboardApi.getEmployeeDashboard().then(response => {
        const data = response.data || {}
        const stats = data.stats || {}

        // 映射统计数据
        this.statistics.todayApplications = stats.todayApplications || 0
        this.statistics.pendingApprovals = stats.pendingApprovals || stats.pendingApproval || 0
        this.statistics.monthlyUsage = stats.monthlyUsage || 0
        this.statistics.completedApplications = stats.completedApplications || 0

        // 最近申领记录
        this.recentApplications = (data.recentApplications || []).map(item => ({
          applicationNo: item.applicationNo,
          materialName: item.materialName,
          quantity: item.quantity,
          applicationTime: item.applicationTime,
          status: item.status
        }))

        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    getStatusType(status) {
      const map = {
        'PENDING': 'warning',
        'REJECTED': 'danger',
        'TO_BE_OUTBOUND': 'info',
        'OUTBOUND': 'success',
        'CANCELLED': 'info'
      }
      return map[status] || ''
    },
    getStatusText(status) {
      const map = {
        'PENDING': '待审批',
        'REJECTED': '已拒绝',
        'TO_BE_OUTBOUND': '待出库',
        'OUTBOUND': '已出库',
        'CANCELLED': '已撤销'
      }
      return map[status] || status
    },
    handleRestock(item) {
      this.$router.push(`/materials/edit/${item.id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.dashboard {
  max-width: 1400px;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .welcome-title {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
    color: $text-primary;
  }

  .welcome-desc {
    color: $text-secondary;
    font-size: 15px;

    .highlight {
      color: $primary-color;
      font-weight: 600;
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  &.employee-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  background: $surface-color;
  border-radius: $border-radius-xl;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  border: 1px solid $border-color;
  transition: all $transition-base;

  &:hover {
    box-shadow: $shadow-base;
    transform: translateY(-2px);
  }

  &.warning .stat-icon {
    background: rgba(239, 68, 68, 0.1);
    color: $danger-color;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: $border-radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    i {
      font-size: 22px;
    }

    &.blue {
      background: rgba(8, 145, 178, 0.1);
      color: $primary-color;
    }

    &.amber {
      background: rgba(245, 158, 11, 0.1);
      color: $warning-color;
    }

    &.green {
      background: rgba(34, 197, 94, 0.1);
      color: $success-color;
    }

    &.red {
      background: rgba(239, 68, 68, 0.1);
      color: $danger-color;
    }
  }

  .stat-info {
    display: flex;
    flex-direction: column;

    .stat-label {
      font-size: 13px;
      color: $text-muted;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      font-family: 'Lexend', sans-serif;
      color: $text-primary;
      line-height: 1.2;
    }

    .stat-change {
      font-size: 12px;
      margin-top: 4px;
      color: $text-muted;

      &.positive {
        color: $success-color;
      }

      &.negative {
        color: $danger-color;
      }
    }
  }
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  margin-bottom: 24px;
}

.content-card {
  background: $surface-color;
  border-radius: $border-radius-xl;
  border: 1px solid $border-color;
  overflow: hidden;

  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid $border-color;
    display: flex;
    align-items: center;
    justify-content: space-between;

    h3 {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
    }

    &.warning {
      background: rgba(239, 68, 68, 0.03);

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          color: $danger-color;
        }
      }
    }
  }

  .card-body {
    padding: 20px;
  }
}

.sidebar-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;

  .quick-action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px 12px;
    border-radius: $border-radius-lg;
    border: 1px solid $border-color;
    cursor: pointer;
    transition: all $transition-fast;

    &:hover {
      border-color: $primary-color;
      background: $primary-bg;

      .action-icon {
        transform: scale(1.1);
      }
    }

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: $border-radius-base;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform $transition-fast;

      i {
        font-size: 20px;
      }

      &.primary {
        background: $primary-bg;
        color: $primary-color;
      }

      &.success {
        background: rgba(34, 197, 94, 0.1);
        color: $success-color;
      }

      &.warning {
        background: rgba(245, 158, 11, 0.1);
        color: $warning-color;
      }

      &.info {
        background: rgba(100, 116, 139, 0.1);
        color: $info-color;
      }
    }

    span {
      font-size: 13px;
      font-weight: 500;
      color: $text-secondary;
    }
  }
}

.alert-card {
  .alert-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .alert-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: rgba(239, 68, 68, 0.03);
    border: 1px solid rgba(239, 68, 68, 0.1);
    border-radius: $border-radius-lg;

    .alert-icon {
      width: 40px;
      height: 40px;
      background: rgba(239, 68, 68, 0.1);
      border-radius: $border-radius-base;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $danger-color;
    }

    .alert-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .alert-name {
        font-weight: 500;
        color: $text-primary;
      }

      .alert-stock {
        font-size: 12px;
        color: $danger-color;
        margin-top: 2px;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: $text-muted;
}
</style>
