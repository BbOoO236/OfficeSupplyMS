<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>{{ formTitle }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="物资编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入物资编码" />
        </el-form-item>
        <el-form-item label="物资名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物资名称" />
        </el-form-item>
        <el-form-item label="物资类别" prop="category">
          <el-input v-model="form.category" placeholder="请输入物资类别" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input v-model="form.unitPrice" placeholder="请输入单价">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="采购提前期" prop="leadTime">
          <el-input-number v-model="form.leadTime" :min="1" placeholder="请输入采购提前期" />
          <span style="margin-left: 10px; color: #909399;">天</span>
        </el-form-item>
        <el-form-item label="安全系数" prop="safetyFactor">
          <el-input-number v-model="form.safetyFactor" :min="0.1" :step="0.1" placeholder="请输入安全系数" />
        </el-form-item>
        <el-form-item label="当前库存" prop="currentStock">
          <el-input-number v-model="form.currentStock" :min="0" placeholder="请输入当前库存" />
        </el-form-item>
        <el-form-item label="最低库存" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" placeholder="请输入最低库存" />
        </el-form-item>
        <el-form-item label="最高库存" prop="maxStock">
          <el-input-number v-model="form.maxStock" :min="0" placeholder="请输入最高库存" />
        </el-form-item>
        <el-form-item label="ABC分类" prop="abcClass">
          <el-select v-model="form.abcClass" placeholder="请选择ABC分类">
            <el-option label="A类（重要物资）" value="A" />
            <el-option label="B类（一般物资）" value="B" />
            <el-option label="C类（次要物资）" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import * as materialApi from '@/api/material'

export default {
  name: 'MaterialForm',
  data() {
    return {
      form: {
        id: undefined,
        code: '',
        name: '',
        category: '',
        specification: '',
        unit: '',
        unitPrice: '',
        leadTime: 7,
        safetyFactor: 1.5,
        currentStock: 0,
        minStock: 10,
        maxStock: 100,
        abcClass: 'C',
        status: 1,
        remark: ''
      },
      rules: {
        code: [
          { required: true, message: '请输入物资编码', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入物资名称', trigger: 'blur' }
        ],
        unitPrice: [
          { required: true, message: '请输入单价', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    formTitle() {
      return this.$route.params.id ? '编辑物资' : '新增物资'
    }
  },
  created() {
    if (this.$route.params.id) {
      this.fetchData()
    }
  },
  methods: {
    fetchData() {
      const id = this.$route.params.id
      materialApi.getMaterialById(id).then(response => {
        this.form = response.data
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id) {
            // 编辑
            materialApi.updateMaterial(this.form.id, this.form).then(response => {
              this.$message.success('更新成功')
              this.$router.push('/materials')
            })
          } else {
            // 新增
            materialApi.createMaterial(this.form).then(response => {
              this.$message.success('创建成功')
              this.$router.push('/materials')
            })
          }
        }
      })
    },
    cancel() {
      this.$router.push('/materials')
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
</style>