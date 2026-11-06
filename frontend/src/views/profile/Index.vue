<template>
  <div class="profile-container">
    <h1>个人信息</h1>
    <el-card class="profile-card">
      <div class="profile-header">
        <el-avatar :size="100" :src="userAvatar" class="profile-avatar" />
        <div class="profile-info">
          <h2>{{ realName || username }}</h2>
          <p class="profile-role">{{ roleText }}</p>
          <p class="profile-department" v-if="department">{{ department }}</p>
        </div>
      </div>
      <el-divider />
      <div class="profile-details">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <label>用户名：</label>
              <span>{{ username }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>姓名：</label>
              <span>{{ realName || '未设置' }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>角色：</label>
              <span>{{ roleText }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>部门：</label>
              <span>{{ department || '未设置' }}</span>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="detail-item">
              <label>用户ID：</label>
              <span>{{ userId || '未设置' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 修改密码区域 -->
    <el-card class="section-card">
      <div slot="header" class="card-header">
        <span>修改密码</span>
      </div>
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="120px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPasswordForm" :loading="passwordLoading">修改密码</el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      passwordLoading: false
    }
  },
  computed: {
    ...mapGetters(['username', 'realName', 'role', 'department', 'userInfo']),
    userId() {
      return this.userInfo.userId || ''
    },
    roleText() {
      const roleMap = {
        'admin': '管理员',
        'employee': '员工'
      }
      return roleMap[this.role] || this.role || '未知'
    },
    userAvatar() {
      // 默认头像，可以根据实际需求修改
      return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  methods: {
    // 密码表单提交
    submitPasswordForm() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.passwordLoading = true
          changePassword(this.passwordForm.oldPassword, this.passwordForm.newPassword)
            .then(response => {
              this.$message.success('密码修改成功')
              this.resetPasswordForm()
            })
            .catch(error => {
              this.$message.error(error.message || '密码修改失败')
            })
            .finally(() => {
              this.passwordLoading = false
            })
        } else {
          return false
        }
      })
    },
    // 重置密码表单
    resetPasswordForm() {
      this.$refs.passwordForm.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.profile-container {
  padding: 0;
  max-width: 800px;
}

.profile-card, .section-card {
  border-radius: $border-radius-xl;
  border: 1px solid $border-color;
  margin-bottom: 20px;

  ::v-deep .el-divider--horizontal {
    margin: 20px 0;
  }
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.profile-avatar {
  margin-right: 30px;
  border: 3px solid $border-color;
}

.profile-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-family: 'Lexend', sans-serif;
  color: $text-primary;
}

.profile-role {
  margin: 0 0 5px 0;
  color: $primary-color;
  font-size: 16px;
  font-weight: 500;
}

.profile-department {
  margin: 0;
  color: $text-muted;
}

.profile-details {
  margin-top: 20px;
}

.detail-item {
  margin-bottom: 15px;
  padding: 12px 16px;
  background: $background-color;
  border-radius: $border-radius-base;
  display: flex;
  align-items: center;

  label {
    font-weight: 600;
    color: $text-primary;
    margin-right: 12px;
    min-width: 70px;
  }

  span {
    color: $text-secondary;
  }
}

.card-header {
  font-family: 'Lexend', sans-serif;
  font-weight: 600;
  font-size: 16px;
  color: $text-primary;
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: $text-primary;
}
</style>