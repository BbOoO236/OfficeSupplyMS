<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="物资名称">
          <el-input
            v-model="listQuery.name"
            placeholder="请输入物资名称"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input
            v-model="listQuery.code"
            placeholder="请输入物资编码"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="物资类别">
          <el-input
            v-model="listQuery.category"
            placeholder="请输入物资类别"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="ABC分类">
          <el-select v-model="listQuery.abcClass" placeholder="请选择" clearable>
            <el-option label="A类" value="A" />
            <el-option label="B类" value="B" />
            <el-option label="C类" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="listQuery.stockStatus" placeholder="请选择" clearable>
            <el-option label="库存充足" value="NORMAL" />
            <el-option label="库存过低" value="LOW" />
            <el-option label="库存过高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新增物资</el-button>
      <el-button type="warning" icon="el-icon-warning" @click="handleLowStock">低库存物资</el-button>
      <el-button type="danger" icon="el-icon-shopping-cart-full" @click="handleNeedReorder">需补货物资</el-button>
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
      <el-table-column label="单价" prop="unitPrice" align="center" width="100">
        <template slot-scope="{row}">
          <span>¥{{ row.unitPrice }}</span>
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
      <el-table-column label="状态" prop="status" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">编辑</el-button>
          <el-button v-if="row.status===1" size="mini" type="danger" @click="handleDelete(row)">停用</el-button>
          <el-button v-else size="mini" type="success" @click="handleEnable(row)">启用</el-button>
          <el-button size="mini" type="info" @click="handleStockIn(row)">入库</el-button>
          <el-button size="mini" type="info" @click="handleStockOut(row)">出库</el-button>
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

    <!-- 入库对话框 -->
    <el-dialog :title="stockInTitle" :visible.sync="stockInDialogVisible" width="400px">
      <el-form ref="stockInForm" :model="stockInForm" :rules="stockInRules" label-width="100px">
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="入库单价" prop="unitPrice">
          <el-input v-model="stockInForm.unitPrice" placeholder="请输入入库单价" style="width: 100%;">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStockIn">确认</el-button>
      </div>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog :title="stockOutTitle" :visible.sync="stockOutDialogVisible" width="400px">
      <el-form ref="stockOutForm" :model="stockOutForm" :rules="stockOutRules" label-width="100px">
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" :max="currentMaterial.currentStock" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="stockOutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStockOut">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as materialApi from '@/api/material'
import Pagination from '@/components/Pagination'

export default {
  name: 'MaterialList',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        name: '',
        code: '',
        category: '',
        abcClass: '',
        stockStatus: ''
      },
      stockInDialogVisible: false,
      stockOutDialogVisible: false,
      stockInTitle: '',
      stockOutTitle: '',
      currentMaterial: {},
      stockInForm: {
        quantity: 1,
        unitPrice: ''
      },
      stockOutForm: {
        quantity: 1
      },
      stockInRules: {
        quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
        unitPrice: [{ required: true, message: '请输入入库单价', trigger: 'blur' }]
      },
      stockOutRules: {
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
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
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetFilter() {
      this.listQuery = {
        page: 1,
        limit: 20,
        name: '',
        code: '',
        category: '',
        abcClass: '',
        stockStatus: ''
      }
      this.getList()
    },
    handleCreate() {
      this.$router.push('/materials/add')
    },
    handleUpdate(row) {
      this.$router.push(`/materials/edit/${row.id}`)
    },
    handleDelete(row) {
      this.$confirm('确认停用该物资吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        materialApi.deleteMaterial(row.id).then(response => {
          this.$message.success('停用成功')
          this.getList()
        })
      })
    },
    handleEnable(row) {
      // 启用物资，这里需要调用更新状态的API
      this.$confirm('确认启用该物资吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用更新API，将status改为1
        materialApi.updateMaterial(row.id, { status: 1 }).then(response => {
          this.$message.success('启用成功')
          this.getList()
        })
      })
    },
    handleLowStock() {
      this.listQuery.stockStatus = 'LOW'
      this.handleFilter()
    },
    handleNeedReorder() {
      materialApi.getNeedReorderMaterials().then(response => {
        this.list = response.data
        this.total = this.list.length
        this.listQuery.stockStatus = ''
      })
    },
    handleStockIn(row) {
      this.currentMaterial = row
      this.stockInTitle = `入库 - ${row.name}`
      this.stockInForm = {
        quantity: 1,
        unitPrice: row.unitPrice || ''
      }
      this.stockInDialogVisible = true
    },
    handleStockOut(row) {
      this.currentMaterial = row
      this.stockOutTitle = `出库 - ${row.name}`
      this.stockOutForm = {
        quantity: 1
      }
      this.stockOutDialogVisible = true
    },
    confirmStockIn() {
      this.$refs.stockInForm.validate(valid => {
        if (valid) {
          materialApi.stockIn(this.currentMaterial.id, {
            quantity: this.stockInForm.quantity,
            unitPrice: this.stockInForm.unitPrice
          }).then(response => {
            this.$message.success('入库成功')
            this.stockInDialogVisible = false
            this.getList()
          })
        }
      })
    },
    confirmStockOut() {
      this.$refs.stockOutForm.validate(valid => {
        if (valid) {
          if (this.stockOutForm.quantity > this.currentMaterial.currentStock) {
            this.$message.error('出库数量不能超过当前库存')
            return
          }
          materialApi.stockOut(this.currentMaterial.id, {
            quantity: this.stockOutForm.quantity
          }).then(response => {
            this.$message.success('出库成功')
            this.stockOutDialogVisible = false
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