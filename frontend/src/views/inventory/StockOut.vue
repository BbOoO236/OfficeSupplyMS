<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="出库单号">
          <el-input
            v-model="listQuery.stockOutNo"
            placeholder="请输入出库单号"
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
        <el-form-item label="出库类型">
          <el-select v-model="listQuery.type" placeholder="请选择" clearable>
            <el-option label="申领出库" value="APPLICATION" />
            <el-option label="调拨出库" value="TRANSFER" />
            <el-option label="损耗出库" value="LOSS" />
            <el-option label="其他出库" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库时间">
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
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建出库单</el-button>
      <el-button type="success" icon="el-icon-refresh" @click="getList">刷新</el-button>
      <el-button v-if="filterMaterialId" type="info" icon="el-icon-back" @click="goBack">返回物资列表</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="出库单号" prop="stockOutNo" align="center" width="150">
        <template slot-scope="{row}">
          <span>{{ row.stockOutNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物资编码" prop="materialCode" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.materialCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物资名称" prop="materialName" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.materialName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出库类型" prop="type" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.type === 'APPLICATION' ? 'success' : row.type === 'LOSS' ? 'danger' : ''">
            {{ typeText(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="出库数量" prop="quantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出库时间" prop="stockOutTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.stockOutTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作员" prop="operatorName" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.operatorName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.remark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="关联申领单" prop="applicationNo" align="center" width="150">
        <template slot-scope="{row}">
          <span>{{ row.applicationNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新建/编辑出库单对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="物资" prop="materialId">
          <el-select
            v-model="form.materialId"
            placeholder="请选择物资"
            filterable
            style="width: 100%;"
            @change="handleMaterialChange"
          >
            <el-option
              v-for="item in materialOptions"
              :key="item.id"
              :label="`${item.name} (${item.code})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出库类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择出库类型" style="width: 100%;">
            <el-option label="申领出库" value="APPLICATION" />
            <el-option label="调拨出库" value="TRANSFER" />
            <el-option label="损耗出库" value="LOSS" />
            <el-option label="其他出库" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="出库时间" prop="stockOutTime">
          <el-date-picker
            v-model="form.stockOutTime"
            type="datetime"
            placeholder="选择出库时间"
            style="width: 100%;"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item v-if="form.type === 'APPLICATION'" label="关联申领单" prop="applicationId">
          <el-select
            v-model="form.applicationId"
            placeholder="请选择申领单"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="item in applicationOptions"
              :key="item.id"
              :label="`${item.applicationNo} - ${item.materialName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="出库单详情" :visible.sync="detailVisible" width="750px">
      <div v-loading="detailLoading" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="出库单号">{{ currentStockOut.stockOutNo }}</el-descriptions-item>
          <el-descriptions-item label="出库类型">
            <el-tag :type="getTypeTagType(currentStockOut.type)" size="small">
              {{ typeText(currentStockOut.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="物资编码">{{ currentStockOut.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物资名称">{{ currentStockOut.materialName }}</el-descriptions-item>
          <el-descriptions-item label="物资单位">{{ currentStockOut.materialUnit }}</el-descriptions-item>
          <el-descriptions-item label="出库数量">{{ currentStockOut.quantity }}</el-descriptions-item>
          <el-descriptions-item label="出库时间">{{ currentStockOut.outTime }}</el-descriptions-item>
          <el-descriptions-item label="操作员">{{ currentStockOut.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="关联申领单">{{ currentStockOut.applicationNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出库原因">{{ currentStockOut.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentStockOut.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 操作历史 -->
        <el-card class="history-card" shadow="never" v-if="stockOutHistory.length > 0">
          <div slot="header" class="card-header">
            <span>操作历史</span>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in stockOutHistory"
              :key="index"
              :timestamp="item.createTime"
              type="primary"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ item.actionText || item.action }}</div>
                <div class="timeline-desc" v-if="item.operatorName">操作人：{{ item.operatorName }}</div>
                <div class="timeline-desc" v-if="item.remark">备注：{{ item.remark }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as stockOutApi from '@/api/stockOut'
import * as materialApi from '@/api/material'
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'
import dayjs from 'dayjs'

export default {
  name: 'StockOut',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        stockOutNo: '',
        materialName: '',
        type: '',
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      dialogVisible: false,
      dialogTitle: '新建出库单',
      form: {
        materialId: '',
        type: 'APPLICATION',
        quantity: 1,
        stockOutTime: '',
        remark: '',
        applicationId: ''
      },
      rules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'blur' }],
        type: [{ required: true, message: '请选择出库类型', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }]
      },
      materialOptions: [],
      applicationOptions: [],
      filterMaterialId: null,
      detailVisible: false,
      detailLoading: false,
      currentStockOut: {},
      stockOutHistory: []
    }
  },
  created() {
    if (this.$route.query.materialId) {
      this.filterMaterialId = parseInt(this.$route.query.materialId)
      this.listQuery.materialId = this.filterMaterialId
    }
    this.getList()
    this.loadMaterialOptions()
    this.loadApplicationOptions()
  },
  methods: {
    getList(pagination) {
      if (pagination) {
        this.listQuery.page = pagination.page
        this.listQuery.limit = pagination.limit
      }
      this.listLoading = true
      if (this.filterMaterialId) {
        stockOutApi.getStockOutsByMaterialId(this.filterMaterialId).then(response => {
          this.list = response.data || []
          this.total = this.list.length
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      } else {
        stockOutApi.getStockOutList(this.listQuery).then(response => {
          this.list = response.data.list || response.data
          this.total = response.data.total || this.list.length
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      }
    },
    handlePageChange(page) {
      this.listQuery.page = page
      this.getList({ page: page, limit: this.listQuery.limit })
    },
    loadMaterialOptions() {
      materialApi.getMaterialList({ limit: 1000 }).then(response => {
        this.materialOptions = response.data.list || response.data
      })
    },
    loadApplicationOptions() {
      applicationApi.getApplicationList({ limit: 1000 }).then(response => {
        this.applicationOptions = response.data.list || response.data
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
        stockOutNo: '',
        materialName: '',
        type: '',
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
    typeText(type) {
      const map = {
        APPLICATION: '申领出库',
        TRANSFER: '调拨出库',
        LOSS: '损耗出库',
        OTHER: '其他出库'
      }
      return map[type] || type
    },
    handleCreate() {
      this.dialogTitle = '新建出库单'
      this.form = {
        materialId: '',
        type: 'APPLICATION',
        quantity: 1,
        remark: '',
        applicationId: ''
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    handleMaterialChange(materialId) {
      // 可以检查库存数量
    },
    handleView(row) {
      this.detailLoading = true
      this.currentStockOut = {}
      this.stockOutHistory = []
      this.detailVisible = true
      stockOutApi.getStockOutById(row.id).then(response => {
        this.currentStockOut = response.data || {}
        this.stockOutHistory = response.data.historyList || []
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑出库单'
      this.form = {
        id: row.id,
        materialId: row.materialId,
        type: row.type,
        quantity: row.quantity,
        remark: row.remark || '',
        applicationId: row.applicationId
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    getTypeTagType(type) {
      const map = {
        'APPLICATION': 'success',
        'TRANSFER': 'primary',
        'LOSS': 'danger',
        'OTHER': 'warning'
      }
      return map[type] || 'info'
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id) {
            // 编辑模式
            stockOutApi.updateStockOut(this.form.id, this.form).then(() => {
              this.$message.success('出库单更新成功')
              this.dialogVisible = false
              this.getList()
            }).catch((error) => {
              this.$message.error(error.message || '更新失败')
            })
          } else {
            // 新建模式
            stockOutApi.createStockOut(this.form).then(() => {
              this.$message.success('出库单创建成功')
              this.dialogVisible = false
              this.getList()
            }).catch((error) => {
              this.$message.error(error.message || '创建失败')
            })
          }
        }
      })
    },
    goBack() {
      this.$router.push('/inventory')
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
::v-deep .el-table .el-table__cell {
  .el-button + .el-button {
    margin-left: 8px;
  }
}
.history-card {
  margin-top: 20px;
}
.timeline-content {
  .timeline-title {
    font-weight: 500;
    color: #303133;
  }
  .timeline-desc {
    color: #909399;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>