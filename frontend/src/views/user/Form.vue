<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="form-header">
        <span>{{ formTitle }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="普通用户" value="employee" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select
            ref="deptSelect"
            v-model="form.department"
            placeholder="请选择或输入部门"
            filterable
            allow-create
            default-first-option
            style="width: 100%;"
            :filter-method="departmentFilterMethod"
            @visible-change="fetchDepartments"
          >
            <el-option
              v-for="dept in filteredDepartmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
            <el-option
              v-if="showAddNewOption"
              label="+ 新增部门"
              value="__ADD_NEW__"
              style="color: #409EFF; font-weight: 500;"
            />
          </el-select>
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
import * as departmentApi from '@/api/department'

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
        role: 'employee',
        department: '',
        email: '',
        phone: '',
        status: 1
      },
      departmentList: [],
      filteredDepartmentList: [],
      showAddNewOption: false,
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
        department: [
          { required: true, message: '请选择部门', trigger: 'change' }
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
    },
    isAddMode() {
      return !this.$route.params.id
    }
  },
  watch: {
    'form.department'(val) {
      if (val === '__ADD_NEW__') {
        // 用户点击了"新增部门"，关闭下拉框，清空选择让用户输入
        this.$refs.deptSelect.blur()
        this.form.department = ''
        this.showAddNewOption = false
        // 等待 DOM 更新后聚焦输入框
        this.$nextTick(() => {
          this.$refs.deptSelect.focus()
        })
      }
    },
    // 监听路由变化，当从编辑页切回新增页时重置表单
    $route(to, from) {
      if (to.name === 'UserAdd' && from.name === 'UserEdit') {
        this.resetForm()
      }
    }
  },
  beforeRouteEnter(to, from, next) {
    // 根据路由决定是重置还是加载数据
    next(vm => {
      if (!to.params.id) {
        // 新增模式，重置表单
        vm.resetForm()
      }
      // 编辑模式的数据加载在 beforeMount 中进行
    })
  },
  created() {
    // 组件创建时根据模式决定是重置表单还是加载数据
    if (this.isAddMode) {
      console.log('UserForm created in add mode, resetting form...')
      this.resetForm()
      console.log('Form after reset:', JSON.stringify(this.form))
    } else {
      console.log('UserForm created in edit mode, will fetch data in beforeMount...')
    }
  },
  beforeMount() {
    // 根据模式决定是重置表单还是加载数据
    if (this.isAddMode) {
      console.log('UserForm beforeMount in add mode, resetting...')
      this.form = {
        id: undefined,
        username: '',
        password: '',
        realName: '',
        role: '',
        department: '',
        email: '',
        phone: '',
        status: 1
      }
      this.filteredDepartmentList = []
      this.showAddNewOption = false
    } else {
      // 编辑模式，加载用户数据
      console.log('UserForm beforeMount in edit mode, fetching data...')
      this.fetchData()
    }
  },
  mounted() {
    // 强制清空用户名和密码输入框（防止浏览器 autofill）
    if (this.isAddMode) {
      this.$nextTick(() => {
        setTimeout(() => {
          const form = this.$refs.form
          if (form && form.$el) {
            const inputs = form.$el.querySelectorAll('input')
            inputs.forEach(input => {
              input.value = ''
            })
          }
          // 同时清空 v-model 绑定
          this.form.username = ''
          this.form.password = ''
        }, 100)
      })
    }
  },
  methods: {
    resetForm() {
      this.form = {
        id: undefined,
        username: '',
        password: '',
        realName: '',
        role: '',
        department: '',
        email: '',
        phone: '',
        status: 1
      }
      this.filteredDepartmentList = []
      this.showAddNewOption = false
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    fetchData() {
      const id = this.$route.params.id
      userApi.getUserById(id).then(response => {
        this.form = response.data
        // 清除密码字段，编辑时不需要
        this.form.password = ''
      })
    },
    fetchDepartments(visible) {
      if (visible) {
        departmentApi.getDepartmentList().then(response => {
          this.departmentList = response.data || []
          this.filteredDepartmentList = [...this.departmentList]
          // 检查当前输入是否在列表中，如果不在则显示"新增部门"选项
          const currentVal = this.form.department
          this.showAddNewOption = !currentVal || !this.departmentList.some(d => d.name === currentVal)
        })
      }
    },
    departmentFilterMethod(query) {
      // 过滤部门列表
      if (query) {
        this.filteredDepartmentList = this.departmentList.filter(d =>
          d.name.toLowerCase().includes(query.toLowerCase())
        )
        // 如果过滤后没有匹配且输入不为空，显示新增选项
        this.showAddNewOption = this.filteredDepartmentList.length === 0 && query.trim() !== ''
      } else {
        this.filteredDepartmentList = [...this.departmentList]
        this.showAddNewOption = !this.form.department || !this.departmentList.some(d => d.name === this.form.department)
      }
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 如果输入了不在列表中的新部门，先创建部门
          const deptName = this.form.department
          const deptExists = this.departmentList.some(d => d.name === deptName)
          if (!deptExists && deptName) {
            departmentApi.createDepartment({ name: deptName, description: '用户自定义部门' }).then(() => {
              this.$message.success('新部门已创建')
              // 刷新部门列表
              this.fetchDepartments()
              this.doSubmit()
            }).catch(() => {
              // 部门可能已存在，忽略错误继续提交
              this.doSubmit()
            })
          } else {
            this.doSubmit()
          }
        }
      })
    },
    doSubmit() {
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
    },
    cancel() {
      this.$router.push('/users')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-container {
  padding: 0;
}

::v-deep .el-card {
  border-radius: $border-radius-xl;
  border: 1px solid $border-color;
}

.form-header {
  font-family: 'Lexend', sans-serif;
  font-weight: 600;
  font-size: 18px;
  color: $text-primary;
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: $text-primary;
}
</style>