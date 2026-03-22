<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申领物资" prop="materialId">
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
              :label="`${item.name} (${item.code}) - 库存: ${item.currentStock}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存" prop="currentStock">
          <el-input v-model="form.currentStock" disabled style="width: 100%;" />
        </el-form-item>
        <el-form-item label="申领数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="申领用途" prop="purpose">
          <el-input
            v-model="form.purpose"
            type="textarea"
            :rows="3"
            placeholder="请输入申领用途"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="form.urgency">
            <el-radio label="NORMAL">一般</el-radio>
            <el-radio label="URGENT">紧急</el-radio>
            <el-radio label="VERY_URGENT">非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="期望领用时间" prop="expectedTime">
          <el-date-picker
            v-model="form.expectedTime"
            type="datetime"
            placeholder="选择期望领用时间"
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
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
      rules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入申领数量', trigger: 'blur' }],
        purpose: [{ required: true, message: '请输入申领用途', trigger: 'blur' }],
        urgency: [{ required: true, message: '请选择紧急程度', trigger: 'blur' }],
        expectedTime: [{ required: true, message: '请选择期望领用时间', trigger: 'blur' }]
      },
      materialOptions: [],
      currentMaterial: null
    }
  },
  created() {
    this.loadMaterialOptions()
    // 如果是编辑模式，获取申领数据
    if (this.$route.params.id) {
      this.title = '编辑申领'
      this.getApplication(this.$route.params.id)
    }
  },
  methods: {
    loadMaterialOptions() {
      materialApi.getMaterialList({ limit: 1000 }).then(response => {
        this.materialOptions = response.data.list || response.data
      })
    },
    getApplication(id) {
      applicationApi.getApplicationById(id).then(response => {
        const data = response.data
        this.form = {
          materialId: data.materialId,
          quantity: data.quantity,
          purpose: data.purpose,
          urgency: data.urgency,
          expectedTime: data.expectedTime,
          remark: data.remark
        }
        // 设置当前库存
        const material = this.materialOptions.find(item => item.id === data.materialId)
        if (material) {
          this.currentMaterial = material
          this.form.currentStock = material.currentStock
        }
      })
    },
    handleMaterialChange(materialId) {
      const material = this.materialOptions.find(item => item.id === materialId)
      if (material) {
        this.currentMaterial = material
        this.form.currentStock = material.currentStock
      }
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 检查库存是否足够
          if (this.currentMaterial && this.form.quantity > this.currentMaterial.currentStock) {
            this.$message.warning('申领数量超过当前库存，请调整数量')
            return
          }
          const apiCall = this.$route.params.id
            ? applicationApi.updateApplication(this.$route.params.id, this.form)
            : applicationApi.createApplication(this.form)
          apiCall.then(response => {
            this.$message.success(this.$route.params.id ? '编辑成功' : '申领提交成功')
            this.$router.push('/applications/my')
          })
        }
      })
    },
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>