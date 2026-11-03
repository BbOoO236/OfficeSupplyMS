<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
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
            <el-option label="正常" value="NORMAL" />
            <el-option label="未调整" value="UNADJUSTED" />
            <el-option label="已调整" value="ADJUSTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新建盘点</el-button>
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
          <span>{{ row.bookQuantity ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际数量" prop="actualQuantity" align="center" width="100">
        <template slot-scope="{row}">
          <span>{{ row.actualQuantity ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="差异数量" prop="difference" align="center" width="100">
        <template slot-scope="{row}">
          <span v-if="row.difference === null">-</span>
          <span v-else-if="row.difference > 0" style="color: #67C23A;">+{{ row.difference }}</span>
          <span v-else-if="row.difference < 0" style="color: #F56C6C;">{{ row.difference }}</span>
          <span v-else style="color: #67C23A;">0</span>
        </template>
      </el-table-column>
      <el-table-column label="盘点时间" prop="checkTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.checkTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag v-if="row.difference === 0" type="success" size="small">正常</el-tag>
          <el-tag v-else-if="row.adjustment === 1" type="warning" size="small">已调整</el-tag>
          <el-tag v-else type="danger" size="small">未调整</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleView(row)">查看详情</el-button>
          <el-button v-if="row.difference !== 0" type="warning" size="mini" @click="handleAdjust(row)">调整差异</el-button>
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

    <!-- 新建/调整盘点对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="物资" prop="materialId">
          <el-select
            v-model="form.materialId"
            placeholder="请选择物资"
            filterable
            style="width: 100%;"
            @change="handleMaterialChange"
            :disabled="formType === 'adjust'"
          >
            <el-option
              v-for="item in materialOptions"
              :key="item.id"
              :label="`${item.name} (${item.code}) - 当前库存: ${item.currentStock}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="账面数量">
          <el-input :value="form.bookQuantity" disabled style="width: 100%;">
            <template slot="append">（当前库存）</template>
          </el-input>
        </el-form-item>
        <el-form-item label="实际数量" prop="actualQuantity">
          <el-input-number v-model="form.actualQuantity" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="差异数量">
          <el-input :value="form.actualQuantity - form.bookQuantity" disabled style="width: 100%;">
            <template slot="append">
              <span :style="{ color: getDifferenceColor(form.actualQuantity - form.bookQuantity) }">
                {{ getDifferenceText(form.actualQuantity - form.bookQuantity) }}
              </span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="formType === 'adjust'" label="调整备注" prop="adjustmentRemark">
          <el-input
            v-model="form.adjustmentRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入调整备注（必填）"
          />
        </el-form-item>
        <el-form-item v-else label="备注">
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

    <!-- 盘点详情对话框 -->
    <el-dialog title="盘点详情" :visible.sync="detailVisible" width="800px">
      <div v-loading="detailLoading" class="detail-content">
        <!-- 最新盘点信息 -->
        <el-divider content-position="left">最新盘点</el-divider>
        <el-descriptions :column="2" border v-if="currentSummary">
          <el-descriptions-item label="物资编码">{{ currentSummary.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="物资名称">{{ currentSummary.materialName }}</el-descriptions-item>
          <el-descriptions-item label="账面数量">{{ currentSummary.bookQuantity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际数量">{{ currentSummary.actualQuantity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="差异数量">
            <span v-if="currentSummary.difference === null">-</span>
            <span v-else-if="currentSummary.difference > 0" style="color: #67C23A;">+{{ currentSummary.difference }}（盘盈）</span>
            <span v-else-if="currentSummary.difference < 0" style="color: #F56C6C;">{{ currentSummary.difference }}（盘亏）</span>
            <span v-else style="color: #67C23A;">无差异</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentSummary.difference === 0" type="success" size="small">正常</el-tag>
            <el-tag v-else-if="currentSummary.adjustment === 1" type="warning" size="small">已调整</el-tag>
            <el-tag v-else type="danger" size="small">未调整</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="盘点时间">{{ currentSummary.checkTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentSummary.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 历史记录 -->
        <el-divider content-position="left" v-if="currentSummary && currentSummary.historyList && currentSummary.historyList.length > 0">历史盘点记录</el-divider>
        <el-table
          v-if="currentSummary && currentSummary.historyList && currentSummary.historyList.length > 0"
          :data="currentSummary.historyList"
          border
          size="small"
          style="margin-top: 10px;"
        >
          <el-table-column label="盘点单号" prop="checkNo" align="center" width="150" />
          <el-table-column label="账面数量" prop="bookQuantity" align="center" width="80" />
          <el-table-column label="实际数量" prop="actualQuantity" align="center" width="80" />
          <el-table-column label="差异数量" prop="difference" align="center" width="80">
            <template slot-scope="{row}">
              <span :style="{ color: getDifferenceColor(row.difference) }">
                {{ row.difference > 0 ? '+' : '' }}{{ row.difference }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="盘点时间" prop="checkTime" align="center" width="160" />
          <el-table-column label="状态" prop="adjustment" align="center" width="80">
            <template slot-scope="{row}">
              <el-tag v-if="row.difference === 0" type="success" size="small">正常</el-tag>
              <el-tag v-else-if="row.adjustment === 1" type="warning" size="small">已调整</el-tag>
              <el-tag v-else type="danger" size="small">未调整</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" align="center" min-width="100">
            <template slot-scope="{row}">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!currentSummary || !currentSummary.historyList || currentSummary.historyList.length === 0" description="暂无历史记录"></el-empty>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
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
        materialName: '',
        status: ''
      },
      dialogVisible: false,
      dialogTitle: '新建盘点',
      formType: 'create', // 'create' or 'adjust'
      form: {
        materialId: '',
        bookQuantity: 0,
        actualQuantity: 0,
        remark: '',
        adjustmentRemark: ''
      },
      rules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
        actualQuantity: [{ required: true, message: '请输入实际数量', trigger: 'blur' }]
      },
      materialOptions: [],
      detailVisible: false,
      detailLoading: false,
      currentSummary: null,
      currentCheck: null
    }
  },
  created() {
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
      inventoryCheckApi.getMaterialCheckSummaryList().then(response => {
        let data = response.data || []

        // 按物资名称过滤
        if (this.listQuery.materialName) {
          data = data.filter(item =>
            item.materialName && item.materialName.includes(this.listQuery.materialName)
          )
        }

        // 按状态过滤
        if (this.listQuery.status) {
          data = data.filter(item => {
            if (this.listQuery.status === 'NORMAL') {
              return item.difference === 0
            } else if (this.listQuery.status === 'UNADJUSTED') {
              return item.difference !== 0 && item.adjustment !== 1
            } else if (this.listQuery.status === 'ADJUSTED') {
              return item.difference !== 0 && item.adjustment === 1
            }
            return true
          })
        }

        this.total = data.length

        // 客户端分页
        if (this.listQuery.page && this.listQuery.limit) {
          const start = (this.listQuery.page - 1) * this.listQuery.limit
          const end = start + this.listQuery.limit
          this.list = data.slice(start, end)
        } else {
          this.list = data
        }

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
        materialName: '',
        status: ''
      }
      this.getList()
    },
    handlePageChange(page) {
      console.log('handlePageChange called with page:', page)
      this.listQuery.page = page
      this.getList({ page: page, limit: this.listQuery.limit })
    },
    handleCreate() {
      this.dialogTitle = '新建盘点'
      this.formType = 'create'
      this.form = {
        materialId: '',
        bookQuantity: 0,
        actualQuantity: 0,
        remark: '',
        adjustmentRemark: ''
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
        if (this.formType === 'create') {
          this.form.actualQuantity = material.currentStock || 0
        }
      }
    },
    getDifferenceColor(diff) {
      if (diff === null) return '#909399'
      if (diff > 0) return '#67C23A'
      if (diff < 0) return '#F56C6C'
      return '#67C23A'
    },
    getDifferenceText(diff) {
      if (diff === null) return '-'
      if (diff > 0) return '盘盈'
      if (diff < 0) return '盘亏'
      return '无差异'
    },
    handleView(row) {
      this.detailLoading = true
      this.currentSummary = null
      this.detailVisible = true
      inventoryCheckApi.getMaterialCheckSummary(row.materialId).then(response => {
        this.currentSummary = response.data || {}
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },
    handleAdjust(row) {
      this.currentCheck = row
      this.dialogTitle = '调整差异'
      this.formType = 'adjust'
      this.form = {
        materialId: row.materialId,
        bookQuantity: row.bookQuantity,
        actualQuantity: row.actualQuantity,
        remark: '',
        adjustmentRemark: ''
      }
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.formType === 'adjust') {
            // 调整差异
            if (!this.form.adjustmentRemark) {
              this.$message.error('请输入调整备注')
              return
            }
            // 获取当前盘点记录的ID
            inventoryCheckApi.getMaterialCheckSummary(this.form.materialId).then(response => {
              const summary = response.data
              if (summary && summary.lastCheckId) {
                inventoryCheckApi.adjustStockByCheck(summary.lastCheckId, this.form.actualQuantity, this.form.adjustmentRemark).then(() => {
                  this.$message.success('调整成功')
                  this.dialogVisible = false
                  this.getList()
                }).catch((error) => {
                  this.$message.error(error.message || '调整失败')
                })
              }
            })
          } else {
            // 新建盘点
            inventoryCheckApi.createInventoryCheck({
              materialId: this.form.materialId,
              actualQuantity: this.form.actualQuantity,
              remark: this.form.remark
            }).then(() => {
              this.$message.success('盘点成功')
              this.dialogVisible = false
              this.getList()
            }).catch((error) => {
              this.$message.error(error.message || '盘点失败')
            })
          }
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
