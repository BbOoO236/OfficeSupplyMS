<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input
            v-model="listQuery.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input
            v-model="listQuery.realName"
            placeholder="请输入真实姓名"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="listQuery.role" placeholder="请选择" clearable>
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input
            v-model="listQuery.department"
            placeholder="请输入部门"
            clearable
            @keyup.enter.native="handleFilter"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="operation-container">
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新增用户</el-button>
      <el-button type="warning" icon="el-icon-refresh" @click="handleResetPasswordDialog">批量重置密码</el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="用户名" prop="username" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column label="真实姓名" prop="realName" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.realName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="角色" prop="role" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部门" prop="department" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.department }}</span>
        </template>
      </el-table-column>
      <el-table-column label="邮箱" prop="email" align="center" width="180">
        <template slot-scope="{row}">
          <span>{{ row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="电话" prop="phone" align="center" width="120">
        <template slot-scope="{row}">
          <span>{{ row.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" width="160">
        <template slot-scope="{row}">
          <span>{{ row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleUpdate(row)">编辑</el-button>
          <el-button v-if="row.status===1" size="mini" type="danger" @click="handleDisable(row)">禁用</el-button>
          <el-button v-else size="mini" type="success" @click="handleEnable(row)">启用</el-button>
          <el-button size="mini" type="warning" @click="handleResetPassword(row)">重置密码</el-button>
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

    <!-- 重置密码对话框 -->
    <el-dialog :title="resetPasswordTitle" :visible.sync="resetPasswordDialogVisible" width="400px">
      <el-form ref="resetPasswordForm" :model="resetPasswordForm" :rules="resetPasswordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请再次输入新密码" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as userApi from '@/api/user'
import Pagination from '@/components/Pagination'

export default {
  name: 'UserList',
  components: { Pagination },
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.resetPasswordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        username: '',
        realName: '',
        role: '',
        department: '',
        status: ''
      },
      resetPasswordDialogVisible: false,
      resetPasswordTitle: '',
      currentUser: {},
      resetPasswordForm: {
        newPassword: '',
        confirmPassword: ''
      },
      resetPasswordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      userApi.getUserList(this.listQuery).then(response => {
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
        username: '',
        realName: '',
        role: '',
        department: '',
        status: ''
      }
      this.getList()
    },
    handleCreate() {
      this.$router.push('/users/add')
    },
    handleUpdate(row) {
      this.$router.push(`/users/edit/${row.id}`)
    },
    handleDisable(row) {
      this.$confirm('确认禁用该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userApi.updateUser(row.id, { status: 0 }).then(response => {
          this.$message.success('禁用成功')
          this.getList()
        })
      })
    },
    handleEnable(row) {
      this.$confirm('确认启用该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userApi.updateUser(row.id, { status: 1 }).then(response => {
          this.$message.success('启用成功')
          this.getList()
        })
      })
    },
    handleResetPassword(row) {
      this.currentUser = row
      this.resetPasswordTitle = `重置密码 - ${row.realName}`
      this.resetPasswordForm = {
        newPassword: '',
        confirmPassword: ''
      }
      this.resetPasswordDialogVisible = true
    },
    handleResetPasswordDialog() {
      // 批量重置密码，暂时实现为单个重置，可扩展
      if (!this.list || this.list.length === 0) {
        this.$message.warning('暂无用户数据')
        return
      }
      // 这里简单起见，重置第一个用户密码，实际可做成多选
      this.handleResetPassword(this.list[0])
    },
    confirmResetPassword() {
      this.$refs.resetPasswordForm.validate(valid => {
        if (valid) {
          userApi.resetPassword(this.currentUser.id, this.resetPasswordForm.newPassword).then(response => {
            this.$message.success('重置密码成功')
            this.resetPasswordDialogVisible = false
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