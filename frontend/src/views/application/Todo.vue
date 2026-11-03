<template>
  <div class="app-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb-container">
      <el-breadcrumb-item :to="{ path: '/applications/my' }">我的申领</el-breadcrumb-item>
      <el-breadcrumb-item>待批申领</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="申领单号">
          <el-input
            v-model="listQuery.applicationNo"
            placeholder="请输入申领单号"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="物资名称">
          <el-input
            v-model="listQuery.materialName"
            placeholder="请输入物资名称"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="待出库" value="TO_BE_OUTBOUND" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="success" icon="el-icon-refresh" @click="getList">刷新</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="申领单号" prop="applicationNo" align="center" width="150">
        <template slot-scope="{row}">
          <span>{{ row.applicationNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物资名称" prop="materialName" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.materialName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申领数量" prop="quantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.quantity }} {{ row.materialUnit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用途" prop="purpose" align="center" min-width="120">
        <template slot-scope="{row}">
          <span>{{ row.purpose || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申领时间" prop="applyTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.applyTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" align="center" width="120">
        <template slot-scope="{row}">
          <el-tag :type="statusTagType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" prop="approvalRemark" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.approvalRemark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="info" size="mini" @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script>
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'

export default {
  name: 'MyTodo',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        applicationNo: '',
        materialName: '',
        status: ''
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList(pagination) {
      if (pagination) {
        this.listQuery.page = pagination.page
        this.listQuery.limit = pagination.limit
      }
      this.listLoading = true
      // 只获取当前用户的待审批申领
      applicationApi.getMyApplications({ ...this.listQuery, status: 'PENDING' }).then(response => {
        this.list = response.data.list || response.data || []
        this.total = response.data.total || this.list.length
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handlePageChange(page) {
      this.listQuery.page = page
      this.getList({ page: page, limit: this.listQuery.limit })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetFilter() {
      this.listQuery = {
        page: 1,
        limit: 20,
        applicationNo: '',
        materialName: '',
        status: ''
      }
      this.getList()
    },
    statusTagType(status) {
      const map = {
        'PENDING': 'warning',
        'TO_BE_OUTBOUND': 'info'
      }
      return map[status] || ''
    },
    statusText(status) {
      const map = {
        'PENDING': '待审批',
        'TO_BE_OUTBOUND': '待出库',
        // 兼容旧状态
        'APPROVED': '待出库',
        'COMPLETED': '已出库'
      }
      return map[status] || status
    },
    handleView(row) {
      this.$router.push(`/applications/${row.id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.breadcrumb-container {
  margin-bottom: 20px;
}

.filter-container {
  background: $surface-color;
  border-radius: $border-radius-xl;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid $border-color;

  ::v-deep .el-form-item {
    margin-bottom: 0;
  }
}

.operation-container {
  margin-bottom: 16px;
}

::v-deep .el-table {
  border-radius: $border-radius-xl;
  overflow: hidden;

  th.el-table__cell {
    background-color: $background-color !important;
  }
}
</style>
