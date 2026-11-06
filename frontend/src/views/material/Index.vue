<template>
  <div class="material-container">
    <div class="material-layout">
      <!-- 右侧主内容 -->
      <div class="material-main">
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
            <el-form-item label="物资类别" v-if="!showCategorySidebar">
              <el-select
                v-model="listQuery.category"
                placeholder="请选择物资类别"
                clearable
                filterable
                allow-create
                style="width: 100%;"
              >
                <el-option
                  v-for="cat in categoryOptions"
                  :key="cat"
                  :label="cat"
                  :value="cat"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="isAdmin" label="ABC分类">
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

        <div class="operation-container">
          <div class="operation-left">
            <el-button type="primary" icon="el-icon-plus" @click="handleCreate" v-if="isAdmin">新增物资</el-button>
            <el-button type="warning" icon="el-icon-warning" @click="handleLowStock" v-if="isAdmin">低库存物资</el-button>
            <el-button type="danger" icon="el-icon-shopping-cart-full" @click="handleNeedReorder" v-if="isAdmin">需补货物资</el-button>
          </div>
          <div class="operation-right">
            <el-button-group>
              <el-button :type="viewMode === 'table' ? 'primary' : ''" icon="el-icon-s-grid" @click="viewMode = 'table'">列表</el-button>
              <el-button :type="viewMode === 'card' ? 'primary' : ''" icon="el-icon-menu" @click="viewMode = 'card'">卡片</el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 表格视图 -->
        <el-table
          v-loading="listLoading"
          v-show="viewMode === 'table'"
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
              <span>{{ row.specification || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" align="center" width="80">
            <template slot-scope="{row}">
              <span>{{ row.unit || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="unitPrice" align="center" width="100">
            <template slot-scope="{row}">
              <span>¥{{ row.unitPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="当前库存" prop="currentStock" align="center" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.stockStatus === 'LOW' ? 'danger' : row.stockStatus === 'HIGH' ? 'warning' : 'success'">
                {{ row.currentStock }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="安全库存" prop="safetyStock" align="center" width="100">
            <template slot-scope="{row}">
              <span>{{ row.safetyStock ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="ABC分类" prop="abcClass" align="center" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.abcClass === 'A' ? 'danger' : row.abcClass === 'B' ? 'warning' : ''">
                {{ row.abcClass ? row.abcClass + '类' : '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="状态" prop="status" align="center" width="100">
            <template slot-scope="{row}">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template slot-scope="{row}">
              <template v-if="isAdmin">
                <el-button type="primary" size="mini" @click="handleUpdate(row)">编辑</el-button>
                <el-button v-if="row.status===1" size="mini" type="danger" @click="handleDelete(row)">停用</el-button>
                <el-button v-else size="mini" type="success" @click="handleEnable(row)">启用</el-button>
              </template>
              <template v-else>
                <el-button type="primary" size="mini" @click="handleApply(row)" :disabled="row.status !== 1">申领</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <!-- 卡片视图 -->
        <div v-show="viewMode === 'card'" class="card-container">
          <div
            v-for="item in list"
            :key="item.id"
            class="material-card"
            @click="handleCardClick(item)"
          >
            <div class="card-header">
              <span class="card-name">{{ item.name }}</span>
              <el-tag v-if="isAdmin" :type="item.status === 1 ? 'success' : 'info'" size="small">
                {{ item.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </div>
            <div class="card-body">
              <div class="card-row">
                <span class="card-label">编码</span>
                <span class="card-value">{{ item.code }}</span>
              </div>
              <div class="card-row" v-if="item.categoryName">
                <span class="card-label">分类</span>
                <span class="card-value">{{ item.categoryName }}</span>
              </div>
              <div class="card-row">
                <span class="card-label">规格</span>
                <span class="card-value">{{ item.specification || '-' }}</span>
              </div>
              <div class="card-row">
                <span class="card-label">单价</span>
                <span class="card-value price">¥{{ item.unitPrice }}</span>
              </div>
              <div class="card-row" v-if="isAdmin">
                <span class="card-label">库存</span>
                <span class="card-value">
                  <el-tag :type="item.stockStatus === 'LOW' ? 'danger' : item.stockStatus === 'HIGH' ? 'warning' : 'success'" size="small">
                    {{ item.currentStock }} {{ item.unit }}
                  </el-tag>
                </span>
              </div>
            </div>
            <div class="card-footer">
              <template v-if="isAdmin">
                <el-button type="text" @click.stop="handleUpdate(item)">编辑</el-button>
                <el-button type="text" @click.stop="handleApply(item)" v-if="item.status === 1">申领</el-button>
              </template>
              <template v-else>
                <el-button type="primary" size="small" @click.stop="handleApply(item)" :disabled="item.status !== 1">申领物资</el-button>
              </template>
              <el-button type="text" @click.stop="goToDetail(item)">详情</el-button>
            </div>
          </div>
        </div>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="listQuery.page"
          :limit.sync="listQuery.limit"
          @pagination="getList"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 入库对话框 -->
    <el-dialog v-if="isAdmin" :title="stockInTitle" :visible.sync="stockInDialogVisible" width="400px">
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
    <el-dialog v-if="isAdmin" :title="stockOutTitle" :visible.sync="stockOutDialogVisible" width="400px">
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
        categoryId: null,
        abcClass: '',
        stockStatus: ''
      },
      viewMode: 'table',
      showCategorySidebar: false,
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
      },
      categoryOptions: []
    }
  },
  created() {
    this.getList()
    this.fetchCategoryOptions()
  },
  computed: {
    userRole() {
      return this.$store.getters.role
    },
    isAdmin() {
      return this.userRole === 'admin'
    },
    isEmployee() {
      return this.userRole === 'employee'
    }
  },
  methods: {
    getList(pagination) {
      if (pagination) {
        this.listQuery.page = pagination.page
        this.listQuery.limit = pagination.limit
      }
      this.listLoading = true
      materialApi.getMaterialList(this.listQuery).then(response => {
        this.list = response.data.list || response.data
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
    fetchCategoryOptions() {
      materialApi.getMaterialCategories().then(response => {
        this.categoryOptions = response.data || []
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
        categoryId: null,
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
        materialApi.deleteMaterial(row.id).then(() => {
          this.$message.success('停用成功')
          this.getList()
        })
      })
    },
    handleEnable(row) {
      this.$confirm('确认启用该物资吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        materialApi.updateMaterialStatus(row.id, 1).then(() => {
          this.$message.success('启用成功')
          this.getList()
        }).catch(error => {
          this.$message.error('启用失败：' + (error.message || '未知错误'))
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
    handleCardClick(item) {
      this.goToDetail(item)
    },
    goToDetail(item) {
      this.$router.push(`/materials/${item.id}`)
    },
    handleApply(row) {
      if (row.status !== 1) {
        this.$message.warning('该物资已停用，无法申领')
        return
      }
      this.$router.push({
        path: '/applications/create',
        query: {
          materialId: row.id,
          materialName: row.name
        }
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
          }).then(() => {
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
          }).then(() => {
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
@import '@/styles/variables.scss';

.material-container {
  padding: 0;
}

.material-layout {
  display: flex;
  gap: 20px;
}

.material-main {
  flex: 1;
  min-width: 0;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

::v-deep .el-table {
  border-radius: $border-radius-xl;
  overflow: hidden;

  th.el-table__cell {
    background-color: $background-color !important;
  }
}

.card-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.material-card {
  background: $surface-color;
  border: 1px solid $border-color;
  border-radius: $border-radius-xl;
  padding: 16px;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .card-name {
      font-weight: 600;
      font-size: 15px;
      color: $text-primary;
    }
  }

  .card-body {
    .card-row {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      border-bottom: 1px solid $border-light;

      &:last-child {
        border-bottom: none;
      }

      .card-label {
        color: $text-secondary;
        font-size: 13px;
      }

      .card-value {
        color: $text-primary;
        font-size: 13px;
        font-weight: 500;

        &.price {
          color: $primary-color;
          font-weight: 600;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid $border-light;
  }
}
</style>
