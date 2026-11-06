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
        <el-form-item label="申领人">
          <el-input
            v-model="listQuery.userName"
            placeholder="请输入申领人"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="待出库" value="TO_BE_OUTBOUND" />
            <el-option label="已出库" value="OUTBOUND" />
            <el-option label="已撤销" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="申领时间">
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
      <el-button-group>
        <el-button type="primary" icon="el-icon-check" @click="handlePending">待审批</el-button>
        <el-button type="warning" icon="el-icon-truck" @click="handleToBeOutbound">待出库</el-button>
        <el-button type="danger" icon="el-icon-warning" @click="handleAbnormal">异常申领</el-button>
      </el-button-group>
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
      <el-table-column label="申领人" prop="userName" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.userName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部门" prop="userDepartment" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.userDepartment }}</span>
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
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button v-if="row.status === 'PENDING'" type="success" size="mini" @click="handleApprove(row)">审批</el-button>
          <el-button v-if="row.status === 'TO_BE_OUTBOUND'" type="primary" size="mini" @click="handleOutbound(row)">出库</el-button>
          <el-button type="info" size="mini" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 'PENDING'" type="warning" size="mini" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="mini" @click="handleReject(row)">拒绝</el-button>
          <el-button v-if="row.status === 'PENDING'" type="text" size="mini" @click="handleCancel(row)">撤销</el-button>
          <el-button v-if="canRollback(row)" type="text" size="mini" @click="handleRollback(row)">回退</el-button>
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

    <!-- 审批对话框 -->
    <el-dialog :title="approveTitle" :visible.sync="approveDialogVisible" width="500px">
      <el-form ref="approveForm" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="审批结果" prop="action">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="APPROVE">通过</el-radio>
            <el-radio label="REJECT">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="rejectReason">
          <el-input
            v-model="approveForm.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove">确认</el-button>
      </div>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog title="出库操作" :visible.sync="outboundDialogVisible" width="400px">
      <el-form>
        <el-form-item label="申领单号" label-width="100px">
          <span>{{ currentApplication.applicationNo }}</span>
        </el-form-item>
        <el-form-item label="申领人" label-width="100px">
          <span>{{ currentApplication.userName }}</span>
        </el-form-item>
        <el-form-item label="物资名称" label-width="100px">
          <span>{{ currentApplication.materialName }}</span>
        </el-form-item>
        <el-form-item label="申领数量" label-width="100px">
          <span>{{ currentApplication.quantity }}</span>
        </el-form-item>
        <el-form-item label="当前库存" label-width="100px">
          <span>{{ currentApplication.materialStock || '未知' }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOutbound">确认出库</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'

export default {
  name: 'ApplicationList',
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
        userName: '',
        status: '',
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      approveDialogVisible: false,
      outboundDialogVisible: false,
      approveTitle: '',
      currentApplication: {},
      approveForm: {
        applicationId: '',
        action: 'APPROVE',
        rejectReason: ''
      },
      approveRules: {
        action: [
          { required: true, message: '请选择审批结果', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList(pagination) {
      console.log('getList called, pagination:', pagination, 'listQuery:', this.listQuery)
      if (pagination) {
        this.listQuery.page = pagination.page
        this.listQuery.limit = pagination.limit
      }
      console.log('API call with:', this.listQuery)
      this.listLoading = true
      applicationApi.getApplicationList(this.listQuery).then(response => {
        console.log('API response:', response.data)
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
        userName: '',
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
    handlePageChange(page) {
      console.log('handlePageChange called with page:', page)
      this.listQuery.page = page
      this.getList({ page: page, limit: this.listQuery.limit })
    },
    statusTagType(status) {
      const map = {
        PENDING: 'warning',
        REJECTED: 'danger',
        TO_BE_OUTBOUND: 'info',
        OUTBOUND: 'success',
        CANCELLED: 'info',
        // 兼容旧状态
        APPROVED: 'info',
        COMPLETED: 'success'
      }
      return map[status] || ''
    },
    statusText(status) {
      const map = {
        PENDING: '待审批',
        REJECTED: '已拒绝',
        TO_BE_OUTBOUND: '待出库',
        OUTBOUND: '已出库',
        CANCELLED: '已撤销',
        // 兼容旧状态
        APPROVED: '待出库',
        COMPLETED: '已出库'
      }
      return map[status] || status
    },
    canRollback(row) {
      // 只有管理员可以将状态回退到待审批，已出库不能回退
      return ['TO_BE_OUTBOUND', 'REJECTED'].includes(row.status)
    },
    handlePending() {
      this.listQuery.status = 'PENDING'
      this.handleFilter()
    },
    handleToBeOutbound() {
      this.listQuery.status = 'TO_BE_OUTBOUND'
      this.handleFilter()
    },
    handleAbnormal() {
      applicationApi.getAbnormalApplications().then(response => {
        this.list = response.data
        this.total = this.list.length
        this.listQuery.status = ''
      })
    },
    handleApprove(row) {
      this.currentApplication = row
      this.approveTitle = `审批申领单 - ${row.applicationNo}`
      this.approveForm = {
        applicationId: row.id,
        action: 'APPROVE',
        rejectReason: ''
      }
      this.approveDialogVisible = true
    },
    handleReject(row) {
      this.currentApplication = row
      this.approveTitle = `拒绝申领单 - ${row.applicationNo}`
      this.approveForm = {
        applicationId: row.id,
        action: 'REJECT',
        rejectReason: ''
      }
      this.approveDialogVisible = true
    },
    handleCancel(row) {
      this.$confirm(`确认撤销申领单 ${row.applicationNo}？撤销后不可恢复。`, '撤销申领单', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        applicationApi.cancelApplication(row.id).then(() => {
          this.$message.success('撤销成功')
          this.getList()
        }).catch((error) => {
          this.$message.error(error.message || '撤销失败')
        })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    },
    handleOutbound(row) {
      this.currentApplication = row
      this.outboundDialogVisible = true
    },
    handleView(row) {
      this.$router.push(`/applications/${row.id}`)
    },
    handleEdit(row) {
      this.$router.push(`/applications/${row.id}`)
    },
    handleRollback(row) {
      this.$confirm(`确认将申领单 ${row.applicationNo} 回退到待审批状态？`, '状态回退', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        applicationApi.rollbackApplication(row.id, 'PENDING').then(() => {
          this.$message.success('状态回退成功')
          this.getList()
        })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    },
    confirmApprove() {
      this.$refs.approveForm.validate(valid => {
        if (valid) {
          applicationApi.approveApplication(this.approveForm).then(response => {
            this.$message.success('审批操作完成')
            this.approveDialogVisible = false
            this.getList()
          })
        }
      })
    },
    confirmOutbound() {
      applicationApi.outboundApplication(this.currentApplication.id).then(response => {
        this.$message.success('出库操作完成')
        this.outboundDialogVisible = false
        this.getList()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

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