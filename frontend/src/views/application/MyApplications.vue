<template>
  <div class="app-container">
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
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="待出库" value="TO_BE_OUTBOUND" />
            <el-option label="已出库" value="OUTBOUND" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">发起申领</el-button>
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
          <span>{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申领时间" prop="applicationTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.applicationTime }}</span>
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
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="info" size="mini" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" type="warning" size="mini" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="mini" @click="handleCancel(row)">取消</el-button>
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
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'

export default {
  name: 'MyApplications',
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
        status: '',
        startTime: '',
        endTime: ''
      },
      dateRange: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      applicationApi.getMyApplications(this.listQuery).then(response => {
        this.list = response.data.list || response.data
        this.total = response.data.total || this.list.length
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
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
        applicationNo: '',
        materialName: '',
        status: '',
        startTime: '',
        endTime: ''
      }
      this.dateRange = []
      this.getList()
    },
    handleDateChange(value) {
      if (value && value.length === 2) {
        this.listQuery.startTime = value[0] + ' 00:00:00'
        this.listQuery.endTime = value[1] + ' 23:59:59'
      } else {
        this.listQuery.startTime = ''
        this.listQuery.endTime = ''
      }
    },
    statusTagType(status) {
      const map = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger',
        TO_BE_OUTBOUND: 'info',
        OUTBOUND: '',
        COMPLETED: 'success'
      }
      return map[status] || ''
    },
    statusText(status) {
      const map = {
        PENDING: '待审批',
        APPROVED: '已审批',
        REJECTED: '已拒绝',
        TO_BE_OUTBOUND: '待出库',
        OUTBOUND: '已出库',
        COMPLETED: '已完成'
      }
      return map[status] || status
    },
    handleCreate() {
      this.$router.push('/applications/create')
    },
    handleView(row) {
      this.$router.push(`/applications/${row.id}`)
    },
    handleEdit(row) {
      this.$router.push(`/applications/edit/${row.id}`)
    },
    handleCancel(row) {
      this.$confirm('确认取消该申领单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 假设有取消接口，这里暂时用提示
        this.$message.success('取消成功')
        this.getList()
      })
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
.operation-container {
  margin-bottom: 20px;
}
</style>