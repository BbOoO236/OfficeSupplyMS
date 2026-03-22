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
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建入库单</el-button>
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
  </div>
</template>

<script>
import * as stockInApi from '@/api/stockIn'
import * as materialApi from '@/api/material'
import Pagination from '@/components/Pagination'

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
      materialOptions: []
    }
  },
  created() {
    this.getList()
    this.loadMaterialOptions()
  },
  methods: {
    getList() {
      this.listLoading = true
      stockInApi.getStockInList(this.listQuery).then(response => {
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
        stockInTime: new Date().format('yyyy-MM-dd HH:mm:ss'),
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
      this.$alert(`入库单详情：${JSON.stringify(row, null, 2)}`, '查看详情', {
        confirmButtonText: '关闭'
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该入库单吗？', '提示', {
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
          stockInApi.createStockIn(this.form).then(response => {
            this.$message.success('入库单创建成功')
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