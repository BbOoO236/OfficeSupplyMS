<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
          </div>
          <span class="brand-name">OfficeSupply</span>
        </div>
        <h1 class="brand-title">企业办公室物资管理系统</h1>
        <p class="brand-desc">高效、便捷的企业办公物资管理解决方案</p>

        <div class="brand-features">
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <span>简化申领流程</span>
          </div>
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <span>实时库存监控</span>
          </div>
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <span>智能补货提醒</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-form-wrapper">
        <div class="login-form-card">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请选择您的角色并登录</p>

          <!-- 角色选择 -->
          <div class="role-selector">
            <div
              class="role-radio"
              :class="{ active: loginForm.role === 'admin' }"
              @click="loginForm.role = 'admin'"
            >
              <i class="el-icon-user-solid"></i>
              <span>系统管理员</span>
            </div>
            <div
              class="role-radio"
              :class="{ active: loginForm.role === 'employee' }"
              @click="loginForm.role = 'employee'"
            >
              <i class="el-icon-s-custom"></i>
              <span>普通员工</span>
            </div>
          </div>

          <!-- 错误提示 -->
          <el-alert
            v-if="errorMsg"
            :title="errorMsg"
            type="error"
            :closable="false"
            show-icon
            class="login-error-alert"
          />

          <!-- 登录表单 -->
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                size="large"
                @keyup.enter.native="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                :loading="loading"
                type="primary"
                size="large"
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        role: 'admin'
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false,
      errorMsg: ''
    }
  },
  methods: {
    handleLogin() {
      // 清空错误信息
      this.errorMsg = ''
      this.$refs.loginFormRef.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('login', this.loginForm).then(() => {
            this.$router.push({ path: '/' })
            this.loading = false
          }).catch((error) => {
            this.loading = false
            const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
            this.errorMsg = errorMsg
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #E2E8F0 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.5;

    &.bg-shape-1 {
      width: 600px;
      height: 600px;
      background: linear-gradient(135deg, rgba(8, 145, 178, 0.1) 0%, rgba(34, 211, 238, 0.05) 100%);
      top: -200px;
      right: -100px;
    }

    &.bg-shape-2 {
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, rgba(34, 197, 94, 0.08) 0%, rgba(34, 211, 238, 0.03) 100%);
      bottom: -100px;
      left: -50px;
    }
  }
}

.login-container {
  display: flex;
  width: 1000px;
  max-width: 95vw;
  background: $surface-color;
  border-radius: $border-radius-xl;
  box-shadow: $shadow-md;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.login-brand {
  flex: 1;
  padding: 60px 48px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 32px;

    .logo-icon {
      width: 48px;
      height: 48px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: $border-radius-lg;
      display: flex;
      align-items: center;
      justify-content: center;

      svg {
        width: 28px;
        height: 28px;
      }
    }

    .brand-name {
      font-family: 'Lexend', sans-serif;
      font-size: 24px;
      font-weight: 600;
    }
  }

  .brand-title {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 16px;
    color: white;
  }

  .brand-desc {
    font-size: 15px;
    opacity: 0.9;
    margin-bottom: 48px;
    line-height: 1.6;
  }

  .brand-features {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 15px;

      i {
        width: 24px;
        height: 24px;
        background: rgba(34, 197, 94, 0.3);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
      }
    }
  }
}

.login-form-wrapper {
  width: 440px;
  padding: 60px 48px;
  display: flex;
  align-items: center;
}

.login-form-card {
  width: 100%;

  .form-title {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
    color: $text-primary;
  }

  .form-subtitle {
    color: $text-secondary;
    margin-bottom: 32px;
  }
}

.role-selector {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.role-radio {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 2px solid #E2E8F0;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: all 0.3s ease;
  color: $text-secondary;
  font-size: 14px;

  &:hover {
    border-color: rgba($primary-color, 0.3);
  }

  &.active {
    border-color: $primary-color;
    background: rgba($primary-color, 0.08);
    color: $primary-color;
  }

  i {
    font-size: 18px;
  }
}

.login-error-alert {
  margin-bottom: 20px;
}

.login-form {
  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: $border-radius-base;
    background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
    border: none;
    box-shadow: 0 4px 14px rgba(8, 145, 178, 0.3);

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(8, 145, 178, 0.4);
    }
  }
}
</style>
