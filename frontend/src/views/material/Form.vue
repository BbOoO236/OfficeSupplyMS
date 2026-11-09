<template>
  <div class="app-container">
    <el-card class="form-card">
      <div slot="header" class="form-header">
        <span class="form-title">{{ formTitle }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="物资编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入物资编码" />
        </el-form-item>
        <el-form-item label="物资名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物资名称" />
        </el-form-item>
        <el-form-item label="物资类别" prop="category">
          <el-select
            v-model="form.category"
            placeholder="请选择或输入物资类别"
            filterable
            allow-create
            default-first-option
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
        <el-form-item label="当前库存" prop="currentStock">
          <el-input-number v-model="form.currentStock" :min="0" placeholder="请输入当前库存" />
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
        currentStock: 0,
        status: 1,
        remark: ''
      },
      categoryOptions: [],
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
    this.fetchOptions()
    if (this.$route.params.id) {
      this.fetchData()
    }
  },
  methods: {
    fetchOptions() {
      materialApi.getMaterialCategories().then(response => {
        this.categoryOptions = response.data || []
      })
    },
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
            }).catch(error => {
              this.$message.error('更新失败：' + (error.message || '未知错误'))
            })
          } else {
            // 新增
            materialApi.createMaterial(this.form).then(response => {
              this.$message.success('创建成功')
              this.$router.push('/materials')
            }).catch(error => {
              this.$message.error('创建失败：' + (error.message || '未知错误'))
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
@import '@/styles/variables.scss';

.app-container {
  padding: 0;
}

.form-card {
  background: $surface-color;
  border-radius: $border-radius-xl;
  border: 1px solid $border-color;
  padding: 24px;

  .form-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid $border-color;
  }
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: $text-primary;
}

::v-deep .el-input-number .el-input__inner {
  text-align: left;
}
</style>