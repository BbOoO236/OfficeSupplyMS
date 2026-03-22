<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>{{ formTitle }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import * as userApi from '@/api/user'

export default {
  name: 'UserForm',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (!this.form.id && !value) {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }
    return {
      form: {
        id: undefined,
        username: '',
        password: '',
        realName: '',
        role: 'USER',
        department: '',
        email: '',
        phone: '',
        status: 1
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { validator: validatePassword, trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    formTitle() {
      return this.$route.params.id ? '编辑用户' : '新增用户'
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
      userApi.getUserById(id).then(response => {
        this.form = response.data
        // 清除密码字段，编辑时不需要
        this.form.password = ''
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 编辑时不需要提交密码
          const submitData = { ...this.form }
          if (!submitData.id && !submitData.password) {
            this.$message.error('请填写密码')
            return
          }
          if (submitData.id && !submitData.password) {
            // 编辑时删除password字段，避免更新空密码
            delete submitData.password
          }
          if (submitData.id) {
            // 编辑
            userApi.updateUser(submitData.id, submitData).then(response => {
              this.$message.success('更新成功')
              this.$router.push('/users')
            })
          } else {
            // 新增
            userApi.createUser(submitData).then(response => {
              this.$message.success('创建成功')
              this.$router.push('/users')
            })
          }
        }
      })
    },
    cancel() {
      this.$router.push('/users')
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
</style>