<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="物资名称">
          <el-input
            v-model="listQuery.materialName"
            placeholder="请输入物资名称"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input
            v-model="listQuery.materialCode"
            placeholder="请输入物资编码"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="listQuery.stockStatus" placeholder="请选择" clearable>
            <el-option label="库存充足" value="NORMAL" />
            <el-option label="库存过低" value="LOW" />
            <el-option label="库存过高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="ABC分类">
          <el-select v-model="listQuery.abcClass" placeholder="请选择" clearable>
            <el-option label="A类" value="A" />
            <el-option label="B类" value="B" />
            <el-option label="C类" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="statistics-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #409EFF;">
                <i class="el-icon-box" />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalMaterials }}</div>
                <div class="stat-label">物资种类</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #67C23A;">
                <i class="el-icon-shopping-cart-full" />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalStockQuantity }}</div>
                <div class="stat-label">总库存数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #E6A23C;">
                <i class="el-icon-warning-outline" />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.lowStockMaterials }}</div>
                <div class="stat-label">低库存物资</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #F56C6C;">
                <i class="el-icon-shopping-cart-full" />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.needReorderMaterials }}</div>
                <div class="stat-label">需补货物资</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-circle-plus" @click="handleStockIn">入库操作</el-button>
      <el-button type="success" icon="el-icon-circle-minus" @click="handleStockOut">出库操作</el-button>
      <el-button type="warning" icon="el-icon-check" @click="handleInventoryCheck">库存盘点</el-button>
      <el-button type="danger" icon="el-icon-warning" @click="handleLowStock">查看低库存</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="物资编码" prop="code" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.code }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物资名称" prop="name" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="规格" prop="specification" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.specification }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单位" prop="unit" align="center" width="80">
        <template slot-scope="{row}">
          <span>{{ row.unit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前库存" prop="currentStock" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.stockStatus === 'LOW' ? 'danger' : row.stockStatus === 'HIGH' ? 'warning' : 'success'">
            {{ row.currentStock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="安全库存" prop="safetyStock" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.safetyStock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="补货点" prop="reorderPoint" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.reorderPoint }}</span>
        </template>
      </el-table-column>
      <el-table-column label="是否需要补货" prop="needReorder" align="center" width="120">
        <template slot-scope="{row}">
          <el-tag :type="row.needReorder ? 'danger' : 'success'">
            {{ row.needReorder ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="ABC分类" prop="abcClass" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.abcClass === 'A' ? 'danger' : row.abcClass === 'B' ? 'warning' : ''">
            {{ row.abcClass }}类
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button size="mini" type="info" @click="handleStockInRecord(row)">入库记录</el-button>
          <el-button size="mini" type="info" @click="handleStockOutRecord(row)">出库记录</el-button>
          <el-button size="mini" type="warning" @click="handleCheckRecord(row)">盘点记录</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />
  </div>
</template>

<script>
import * as materialApi from '@/api/material'
import Pagination from '@/components/Pagination'

export default {
  name: 'Inventory',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        materialName: '',
        materialCode: '',
        stockStatus: '',
        abcClass: ''
      },
      statistics: {
        totalMaterials: 0,
        totalStockQuantity: 0,
        lowStockMaterials: 0,
        needReorderMaterials: 0
      }
    }
  },
  created() {
    this.getList()
    this.getStatistics()
  },
  methods: {
    getList() {
      this.listLoading = true
      materialApi.getMaterialList(this.listQuery).then(response => {
        this.list = response.data.list || response.data
        this.total = response.data.total || this.list.length
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    getStatistics() {
      // 这里可以调用专门的统计接口，暂时用现有数据计算
      materialApi.getMaterialList({ limit: 1000 }).then(response => {
        const materials = response.data.list || response.data
        this.statistics.totalMaterials = materials.length
        this.statistics.totalStockQuantity = materials.reduce((sum, item) => sum + (item.currentStock || 0), 0)
        this.statistics.lowStockMaterials = materials.filter(item => item.stockStatus === 'LOW').length
        this.statistics.needReorderMaterials = materials.filter(item => item.needReorder).length
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetFilter() {
      this.listQuery = {
        page: 1,
        limit: 20,
        materialName: '',
        materialCode: '',
        stockStatus: '',
        abcClass: ''
      }
      this.getList()
    },
    handleStockIn() {
      this.$router.push('/inventory/stock-in')
    },
    handleStockOut() {
      this.$router.push('/inventory/stock-out')
    },
    handleInventoryCheck() {
      this.$router.push('/inventory/check')
    },
    handleLowStock() {
      this.listQuery.stockStatus = 'LOW'
      this.handleFilter()
    },
    handleStockInRecord(row) {
      this.$router.push(`/inventory/stock-in?materialId=${row.id}`)
    },
    handleStockOutRecord(row) {
      this.$router.push(`/inventory/stock-out?materialId=${row.id}`)
    },
    handleCheckRecord(row) {
      this.$router.push(`/inventory/check?materialId=${row.id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.filter-container {
  padding-bottom: 20px;
  .el-form-item {
    margin-bottom: 0;
  }
}
.statistics-container {
  margin-bottom: 20px;
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 24px;
        margin-right: 15px;
      }
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          line-height: 1;
        }
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
}
.operation-container {
  margin-bottom: 20px;
}
</style>