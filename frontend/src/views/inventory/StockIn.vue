<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="入库单号">
          <el-input
            v-model="listQuery.stockInNo"
            placeholder="请输入入库单号"
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
        <el-form-item label="入库时间">
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
      <el-button v-if="!filterMaterialId" type="primary" icon="el-icon-plus" @click="handleCreate">新建入库单</el-button>
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
      <el-table-column label="入库单号" prop="stockInNo" align="center" width="150">
        <template slot-scope="{row}">
          <span>{{ row.stockInNo }}</span>
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
      <el-table-column label="入库数量" prop="quantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库单价" prop="unitPrice" align="center" width="100">
        <template slot-scope="{row}">
          <span>¥{{ row.unitPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库金额" prop="amount" align="center" width="100">
        <template slot-scope="{row}">
          <span>¥{{ row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库时间" prop="stockInTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.stockInTime }}</span>
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

    <!-- 新建/编辑入库单对话框 -->
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
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="入库单价" prop="unitPrice">
          <el-input v-model="form.unitPrice" placeholder="请输入入库单价" style="width: 100%;">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="入库时间" prop="stockInTime">
          <el-date-picker
            v-model="form.stockInTime"
            type="datetime"
            placeholder="选择入库时间"
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
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="入库单详情" :visible.sync="detailVisible" width="750px">
      <div v-loading="detailLoading" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入库单号">{{ currentStockIn.stockInNo }}</el-descriptions-item>
          <el-descriptions-item label="物资编码">{{ currentStockIn.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物资名称">{{ currentStockIn.materialName }}</el-descriptions-item>
          <el-descriptions-item label="物资单位">{{ currentStockIn.materialUnit }}</el-descriptions-item>
          <el-descriptions-item label="入库数量">{{ currentStockIn.quantity }}</el-descriptions-item>
          <el-descriptions-item label="入库单价">¥{{ currentStockIn.unitPrice }}</el-descriptions-item>
          <el-descriptions-item label="入库金额">¥{{ currentStockIn.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ currentStockIn.inTime }}</el-descriptions-item>
          <el-descriptions-item label="操作员">{{ currentStockIn.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentStockIn.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 操作历史 -->
        <el-card class="history-card" shadow="never" v-if="stockInHistory.length > 0">
          <div slot="header" class="card-header">
            <span>操作历史</span>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in stockInHistory"
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
import * as stockInApi from '@/api/stockIn'
import * as materialApi from '@/api/material'
import Pagination from '@/components/Pagination'
import dayjs from 'dayjs' //format时间格式

export default {
  name: 'StockIn',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        stockInNo: '',
        materialName: '',
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      dialogVisible: false,
      dialogTitle: '新建入库单',
      form: {
        materialId: '',
        quantity: 1,
        unitPrice: '',
        stockInTime: '',
        remark: ''
      },
      rules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
        unitPrice: [{ required: true, message: '请输入入库单价', trigger: 'blur' }],
        stockInTime: [{ required: true, message: '请选择入库时间', trigger: 'blur' }]
      },
      materialOptions: [],
      filterMaterialId: null,
      detailVisible: false,
      detailLoading: false,
      currentStockIn: {},
      stockInHistory: []
    }
  },
  created() {
    if (this.$route.query.materialId) {
      this.filterMaterialId = parseInt(this.$route.query.materialId)
      this.listQuery.materialId = this.filterMaterialId
    }
    this.getList()
    this.loadMaterialOptions()
  },
  methods: {
    getList(pagination) {
      if (pagination) {
        this.listQuery.page = pagination.page
        this.listQuery.limit = pagination.limit
      }
      this.listLoading = true
      if (this.filterMaterialId) {
        stockInApi.getStockInsByMaterialId(this.filterMaterialId).then(response => {
          this.list = response.data || []
          this.total = this.list.length
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      } else {
        stockInApi.getStockInList(this.listQuery).then(response => {
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
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetFilter() {
      this.listQuery = {
        page: 1,
        limit: 20,
        stockInNo: '',
        materialName: '',
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
    handleCreate() {
      this.dialogTitle = '新建入库单'
      this.form = {
        materialId: '',
        quantity: 1,
        unitPrice: '',
        stockInTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        remark: ''
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    handleMaterialChange(materialId) {
      const material = this.materialOptions.find(item => item.id === materialId)
      if (material && !this.form.unitPrice) {
        this.form.unitPrice = material.unitPrice
      }
    },
    handleView(row) {
      this.detailLoading = true
      this.currentStockIn = {}
      this.stockInHistory = []
      this.detailVisible = true
      stockInApi.getStockInById(row.id).then(response => {
        this.currentStockIn = response.data || {}
        this.stockInHistory = response.data.historyList || []
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑入库单'
      this.form = {
        id: row.id,
        materialId: row.materialId,
        quantity: row.quantity,
        unitPrice: row.unitPrice,
        stockInTime: row.inTime,
        supplier: row.supplier,
        remark: row.remark || ''
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id) {
            // 更新
            stockInApi.updateStockIn(this.form.id, this.form).then(() => {
              this.$message.success('入库单更新成功')
              this.dialogVisible = false
              this.getList()
            })
          } else {
            // 创建
            stockInApi.createStockIn(this.form).then(() => {
              this.$message.success('入库单创建成功')
              this.dialogVisible = false
              this.getList()
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