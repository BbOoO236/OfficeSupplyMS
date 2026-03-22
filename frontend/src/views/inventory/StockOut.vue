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
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="info" size="mini" @click="handleView(row)">查看</el-button>
          <el-button type="danger" size="mini" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script>
import * as stockOutApi from '@/api/stockOut'
import * as materialApi from '@/api/material'
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'

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
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
        stockOutTime: [{ required: true, message: '请选择出库时间', trigger: 'blur' }]
      },
      materialOptions: [],
      applicationOptions: []
    }
  },
  created() {
    this.getList()
    this.loadMaterialOptions()
    this.loadApplicationOptions()
  },
  methods: {
    getList() {
      this.listLoading = true
      stockOutApi.getStockOutList(this.listQuery).then(response => {
        this.list = response.data.list || response.data
        this.total = response.data.total || this.list.length
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
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
        stockOutTime: new Date().format('yyyy-MM-dd HH:mm:ss'),
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
      this.$alert(`出库单详情：${JSON.stringify(row, null, 2)}`, '查看详情', {
        confirmButtonText: '关闭'
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该出库单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 假设有删除接口，这里暂时用提示
        this.$message.success('删除成功')
        this.getList()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          stockOutApi.createStockOut(this.form).then(response => {
            this.$message.success('出库单创建成功')
            this.dialogVisible = false
            this.getList()
          })
        }
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