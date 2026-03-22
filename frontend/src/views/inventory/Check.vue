<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="盘点单号">
          <el-input
            v-model="listQuery.checkNo"
            placeholder="请输入盘点单号"
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
        <el-form-item label="盘点状态">
          <el-select v-model="listQuery.adjusted" placeholder="请选择" clearable>
            <el-option label="未调整" value="false" />
            <el-option label="已调整" value="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有差异">
          <el-select v-model="listQuery.withDifference" placeholder="请选择" clearable>
            <el-option label="有差异" value="true" />
            <el-option label="无差异" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点时间">
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
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建盘点</el-button>
      <el-button type="warning" icon="el-icon-warning" @click="handleUnadjusted">未调整记录</el-button>
      <el-button type="danger" icon="el-icon-warning-outline" @click="handleWithDifference">有差异记录</el-button>
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
      <el-table-column label="盘点单号" prop="checkNo" align="center" width="150">
        <template slot-scope="{row}">
          <span>{{ row.checkNo }}</span>
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
      <el-table-column label="账面数量" prop="bookQuantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.bookQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际数量" prop="actualQuantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.actualQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column label="差异数量" prop="differenceQuantity" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.differenceQuantity > 0 ? 'success' : row.differenceQuantity < 0 ? 'danger' : 'info'">
            {{ row.differenceQuantity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="盘点时间" prop="checkTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.checkTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="盘点人" prop="checkerName" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.checkerName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="adjusted" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.adjusted ? 'success' : 'warning'">
            {{ row.adjusted ? '已调整' : '未调整' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" align="center" min-width="150">
        <template slot-scope="{row}">
          <span>{{ row.remark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button v-if="!row.adjusted" type="primary" size="mini" @click="handleAdjust(row)">调整库存</el-button>
          <el-button v-if="!row.adjusted" type="success" size="mini" @click="handleMarkAdjusted(row)">标记已调整</el-button>
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
    />

    <!-- 新建盘点对话框 -->
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
        <el-form-item label="账面数量" prop="bookQuantity">
          <el-input v-model="form.bookQuantity" disabled style="width: 100%;" />
        </el-form-item>
        <el-form-item label="实际数量" prop="actualQuantity">
          <el-input-number v-model="form.actualQuantity" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="盘点时间" prop="checkTime">
          <el-date-picker
            v-model="form.checkTime"
            type="datetime"
            placeholder="选择盘点时间"
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
import * as inventoryCheckApi from '@/api/inventoryCheck'
import * as materialApi from '@/api/material'
import Pagination from '@/components/Pagination'

export default {
  name: 'InventoryCheck',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        checkNo: '',
        materialName: '',
        adjusted: '',
        withDifference: '',
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      dialogVisible: false,
      dialogTitle: '新建盘点',
      form: {
        materialId: '',
        bookQuantity: 0,
        actualQuantity: 0,
        checkTime: '',
        remark: ''
      },
      rules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'blur' }],
        actualQuantity: [{ required: true, message: '请输入实际数量', trigger: 'blur' }],
        checkTime: [{ required: true, message: '请选择盘点时间', trigger: 'blur' }]
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
      inventoryCheckApi.getInventoryCheckList(this.listQuery).then(response => {
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
        checkNo: '',
        materialName: '',
        adjusted: '',
        withDifference: '',
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
      this.dialogTitle = '新建盘点'
      this.form = {
        materialId: '',
        bookQuantity: 0,
        actualQuantity: 0,
        checkTime: new Date().format('yyyy-MM-dd HH:mm:ss'),
        remark: ''
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    handleMaterialChange(materialId) {
      const material = this.materialOptions.find(item => item.id === materialId)
      if (material) {
        this.form.bookQuantity = material.currentStock || 0
      }
    },
    handleUnadjusted() {
      this.listQuery.adjusted = 'false'
      this.handleFilter()
    },
    handleWithDifference() {
      this.listQuery.withDifference = 'true'
      this.handleFilter()
    },
    handleAdjust(row) {
      this.$confirm('确认根据盘点差异调整库存吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        inventoryCheckApi.adjustStockByCheck(row.id).then(response => {
          this.$message.success('库存调整成功')
          this.getList()
        })
      })
    },
    handleMarkAdjusted(row) {
      inventoryCheckApi.markAsAdjusted(row.id).then(response => {
        this.$message.success('标记已调整成功')
        this.getList()
      })
    },
    handleView(row) {
      this.$alert(`盘点记录详情：${JSON.stringify(row, null, 2)}`, '查看详情', {
        confirmButtonText: '关闭'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          inventoryCheckApi.createInventoryCheck(this.form).then(response => {
            this.$message.success('盘点记录创建成功')
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