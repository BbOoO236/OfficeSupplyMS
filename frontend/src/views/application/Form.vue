<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>

      <!-- 已选物资列表 -->
      <div class="selected-materials" v-if="selectedMaterials.length > 0">
        <div class="section-header">
          <span>已选物资 ({{ selectedMaterials.length }})</span>
          <el-button type="text" @click="handleClearAll">清空全部</el-button>
        </div>
        <el-table :data="selectedMaterials" border style="width: 100%; margin-bottom: 16px;">
          <el-table-column prop="name" label="物资名称" min-width="150" />
          <el-table-column prop="code" label="物资编码" width="120" />
          <el-table-column prop="currentStock" label="当前库存" width="100" align="center" />
          <el-table-column label="申领数量" width="150" align="center">
            <template slot-scope="{row}">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.currentStock"
                size="small"
                @change="handleQuantityChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="{row}">
              <el-button type="text" size="small" @click="handleRemoveMaterial(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="selected-actions">
          <el-button type="primary" @click="handleAddMore">继续添加物资</el-button>
          <el-button type="success" @click="handlePreview">预览申领</el-button>
        </div>
      </div>

      <!-- 物资选择器 -->
      <div class="material-selector" v-if="showMaterialSelector">
        <div class="section-header">
          <span>选择物资</span>
          <el-button type="text" v-if="selectedMaterials.length > 0" @click="showMaterialSelector = false">返回已选列表</el-button>
        </div>
        <div class="filter-row">
          <el-input
            v-model="materialSearchKey"
            placeholder="搜索物资名称或编码"
            clearable
            @input="handleMaterialSearch"
          >
            <i slot="prefix" class="el-icon-search"></i>
          </el-input>
        </div>
        <el-table
          ref="materialTable"
          :data="filteredMaterialOptions"
          border
          highlight-current-row
          @current-change="handleCurrentChange"
          style="width: 100%; margin-top: 16px;"
          max-height="400"
        >
          <el-table-column property="name" label="物资名称" min-width="150" />
          <el-table-column property="code" label="物资编码" width="120" />
          <el-table-column property="categoryName" label="分类" width="100" />
          <el-table-column property="specification" label="规格" width="100" />
          <el-table-column property="currentStock" label="库存" width="80" align="center" />
          <el-table-column property="unitPrice" label="单价" width="80" align="center">
            <template slot-scope="{row}">
              ¥{{ row.unitPrice }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template slot-scope="{row}">
              <el-button
                :type="isMaterialSelected(row) ? 'info' : 'primary'"
                size="small"
                @click.stop="isMaterialSelected(row) ? handleRemoveMaterial(row) : handleSelectMaterial(row)"
                :disabled="!isMaterialSelected(row) && row.status !== 1"
              >
                {{ isMaterialSelected(row) ? '已选' : '选择' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 无已选物资时的引导 -->
      <div class="empty-state" v-if="selectedMaterials.length === 0 && !showMaterialSelector">
        <el-empty description="您还没有选择任何物资">
          <el-button type="primary" @click="showMaterialSelector = true">选择物资</el-button>
        </el-empty>
      </div>

      <!-- 申领信息表单 -->
      <el-form v-if="selectedMaterials.length > 0 && !showMaterialSelector" ref="infoForm" :model="infoForm" :rules="infoRules" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="申领用途" prop="purpose">
          <el-input
            v-model="infoForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请输入申领用途"
          />
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="infoForm.urgency">
            <el-radio label="NORMAL">一般</el-radio>
            <el-radio label="URGENT">紧急</el-radio>
            <el-radio label="VERY_URGENT">非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="期望领用时间" prop="expectedTime">
          <el-date-picker
            v-model="infoForm.expectedTime"
            type="datetime"
            placeholder="选择期望领用时间"
            style="width: 100%;"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="infoForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申领</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog title="申领预览" :visible.sync="previewDialogVisible" width="600px">
      <div class="preview-content">
        <div class="preview-section">
          <h4>申领物资清单</h4>
          <el-table :data="selectedMaterials" border size="small">
            <el-table-column prop="name" label="物资名称" />
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="unit" label="单位" width="60" align="center" />
            <el-table-column label="小计" width="80" align="center">
              <template slot-scope="{row}">
                ¥{{ (row.unitPrice * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="preview-total">
            <span>共 {{ selectedMaterials.length }} 种物资，合计 {{ totalQuantity }} 件</span>
          </div>
        </div>
        <div class="preview-section">
          <h4>申领信息</h4>
          <div class="info-row">
            <span class="label">紧急程度：</span>
            <span class="value">{{ getUrgencyText(infoForm.urgency) }}</span>
          </div>
          <div class="info-row" v-if="infoForm.expectedTime">
            <span class="label">期望领用时间：</span>
            <span class="value">{{ infoForm.expectedTime }}</span>
          </div>
          <div class="info-row" v-if="infoForm.purpose">
            <span class="label">申领用途：</span>
            <span class="value">{{ infoForm.purpose }}</span>
          </div>
          <div class="info-row" v-if="infoForm.remark">
            <span class="label">备注：</span>
            <span class="value">{{ infoForm.remark }}</span>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="previewDialogVisible = false">返回修改</el-button>
        <el-button type="primary" @click="confirmSubmit">确认提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as applicationApi from '@/api/application'
import * as materialApi from '@/api/material'

export default {
  name: 'ApplicationForm',
  data() {
    return {
      title: '发起申领',
      form: {
        materialId: '',
        quantity: 1,
        purpose: '',
        urgency: 'NORMAL',
        expectedTime: '',
        remark: ''
      },
      infoForm: {
        purpose: '',
        urgency: 'NORMAL',
        expectedTime: '',
        remark: ''
      },
      infoRules: {
        purpose: [{ required: true, message: '请输入申领用途', trigger: 'blur' }],
        urgency: [{ required: true, message: '请选择紧急程度', trigger: 'blur' }],
        expectedTime: [{ required: true, message: '请选择期望领用时间', trigger: 'blur' }]
      },
      materialOptions: [],
      filteredMaterialOptions: [],
      selectedMaterials: [],
      showMaterialSelector: false,
      materialSearchKey: '',
      previewDialogVisible: false,
      currentMaterial: null,
      presetMaterialName: '',
      showMaterialSelect: false,
      presetNameFromQuery: false,
      originalMaterialId: '',
      originalMaterialName: ''
    }
  },
  computed: {
    totalQuantity() {
      return this.selectedMaterials.reduce((sum, item) => sum + item.quantity, 0)
    },
    hasPresetMaterial() {
      if (this.$route.params.id) {
        return false
      }
      return !!this.$route.query.materialId
    }
  },
  created() {
    this.loadMaterialOptions()
    if (this.$route.params.id) {
      this.title = '编辑申领'
      this.loadApplicationData()
    }
  },
  methods: {
    loadApplicationData() {
      applicationApi.getApplicationById(this.$route.params.id).then(response => {
        const app = response.data
        if (app) {
          // 找到对应的物资并选中
          const material = this.materialOptions.find(item => item.id === app.materialId)
          if (material) {
            this.handleSelectMaterial({ ...material, quantity: app.quantity })
          }
          this.infoForm.purpose = app.purpose || ''
          this.infoForm.remark = app.remark || ''
        }
      })
    },
    loadMaterialOptions() {
      return materialApi.getMaterialList({ limit: 1000 }).then(response => {
        this.materialOptions = response.data.list || response.data
        this.filteredMaterialOptions = [...this.materialOptions]
        this.checkPresetMaterial()
      })
    },
    checkPresetMaterial() {
      if (this.$route.params.id) {
        return
      }
      const materialId = this.$route.query.materialId
      const materialName = this.$route.query.materialName

      if (materialId && this.materialOptions.length > 0) {
        const material = this.materialOptions.find(item => item.id == materialId)
        if (material && material.status === 1) {
          this.handleSelectMaterial(material)
        }
      }
    },
    handleMaterialSearch() {
      if (!this.materialSearchKey) {
        this.filteredMaterialOptions = [...this.materialOptions]
        return
      }
      const key = this.materialSearchKey.toLowerCase()
      this.filteredMaterialOptions = this.materialOptions.filter(item =>
        (item.name && item.name.toLowerCase().includes(key)) ||
        (item.code && item.code.toLowerCase().includes(key))
      )
    },
    handleCurrentChange(val) {
      this.currentMaterial = val
    },
    isMaterialSelected(material) {
      return this.selectedMaterials.some(item => item.id === material.id)
    },
    handleSelectMaterial(material) {
      if (this.isMaterialSelected(material)) {
        return
      }
      if (material.status !== 1) {
        this.$message.warning('该物资已停用，无法选择')
        return
      }
      this.selectedMaterials.push({
        ...material,
        quantity: 1
      })
    },
    handleRemoveMaterial(material) {
      const index = this.selectedMaterials.findIndex(item => item.id === material.id)
      if (index > -1) {
        this.selectedMaterials.splice(index, 1)
      }
    },
    handleQuantityChange(material) {
      // 数量变化时的处理
    },
    handleClearAll() {
      this.selectedMaterials = []
    },
    handleAddMore() {
      this.showMaterialSelector = true
      this.materialSearchKey = ''
      this.filteredMaterialOptions = [...this.materialOptions]
    },
    handlePreview() {
      this.$refs.infoForm.validate(valid => {
        if (valid) {
          this.previewDialogVisible = true
        }
      })
    },
    handleSubmit() {
      this.$refs.infoForm.validate(valid => {
        if (valid) {
          this.previewDialogVisible = true
        }
      })
    },
    confirmSubmit() {
      // 如果是编辑模式
      if (this.$route.params.id) {
        applicationApi.updateApplication(this.$route.params.id, {
          quantity: this.selectedMaterials.length > 0 ? this.selectedMaterials[0].quantity : null,
          purpose: this.infoForm.purpose,
          remark: this.infoForm.remark
        }).then(() => {
          this.$message.success('申领更新成功')
          this.$router.push('/applications/my')
        }).catch((error) => {
          this.$message.error(error.message || '更新失败')
        })
        return
      }

      // 分别为每个物资创建申领单
      const promises = this.selectedMaterials.map(material => {
        return applicationApi.createApplication({
          materialId: material.id,
          quantity: material.quantity,
          purpose: this.infoForm.purpose,
          urgency: this.infoForm.urgency,
          expectedTime: this.infoForm.expectedTime,
          remark: this.infoForm.remark
        })
      })

      Promise.all(promises).then(() => {
        this.$message.success('申领提交成功')
        this.$router.push('/applications/my')
      }).catch((error) => {
        this.$message.error(error.message || '提交失败')
      })
    },
    getUrgencyText(urgency) {
      const map = {
        'NORMAL': '一般',
        'URGENT': '紧急',
        'VERY_URGENT': '非常紧急'
      }
      return map[urgency] || urgency
    },
    goBack() {
      if (this.selectedMaterials.length > 0) {
        this.$confirm('您有未提交的申领，确定要返回吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.go(-1)
        })
      } else {
        this.$router.go(-1)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-container {
  padding: 0;
  max-width: 900px;
  margin: 0;
}

::v-deep .el-card {
  border-radius: $border-radius-xl;
  border: 1px solid $border-color;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: $text-primary;
}

.filter-row {
  margin-bottom: 8px;
}

.selected-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.preview-content {
  .preview-section {
    margin-bottom: 20px;

    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
    }

    &:last-child {
      margin-bottom: 0;
    }
  }

  .preview-total {
    margin-top: 12px;
    text-align: right;
    color: $text-secondary;
    font-size: 14px;
  }

  .info-row {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 100px;
      flex-shrink: 0;
      color: $text-secondary;
    }

    .value {
      flex: 1;
      color: $text-primary;
    }
  }
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>
