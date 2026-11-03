<template>
  <div class="application-detail-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb-container">
      <el-breadcrumb-item :to="{ path: backPath }">{{ isAdmin ? '申领管理' : '申领记录' }}</el-breadcrumb-item>
      <el-breadcrumb-item>申领详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading" class="detail-content">
      <!-- 高风险预警 -->
      <el-alert
        v-if="showStockWarning"
        :title="stockWarningTitle"
        :description="stockWarningDesc"
        type="warning"
        show-icon
        :closable="false"
        class="risk-alert">
        <template slot>
          <div class="risk-info">
            <span>当前库存：{{ application.materialStock }} {{ application.materialUnit }}</span>
            <span>申领数量：{{ application.quantity }} {{ application.materialUnit }}</span>
          </div>
        </template>
      </el-alert>

      <!-- 基础信息卡片 -->
      <el-card class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span>申领信息</span>
          <el-button v-if="(isAdmin || application.userId === currentUserId) && application.status === 'PENDING' && !editMode" type="primary" size="mini" @click="startEdit">编辑</el-button>
        </div>

        <!-- 显示模式 -->
        <div class="info-list" v-if="!editMode">
          <div class="info-item">
            <span class="info-label">申领单号</span>
            <span class="info-value">{{ application.applicationNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">申领人</span>
            <span class="info-value">{{ application.userName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所属部门</span>
            <span class="info-value">{{ application.userDepartment || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">物资名称</span>
            <span class="info-value">{{ application.materialName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">物资编码</span>
            <span class="info-value">{{ application.materialCode || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">申领数量</span>
            <span class="info-value">{{ application.quantity }} {{ application.materialUnit }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用途</span>
            <span class="info-value">{{ application.purpose || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">当前库存</span>
            <span class="info-value">{{ application.materialStock ?? '-' }} {{ application.materialUnit }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态</span>
            <span class="info-value">
              <el-tag :type="getStatusType(application.status)">
                {{ getStatusText(application.status) }}
              </el-tag>
            </span>
          </div>
          <div class="info-item" v-if="application.remark">
            <span class="info-label">备注</span>
            <span class="info-value">{{ application.remark }}</span>
          </div>
        </div>

        <!-- 编辑模式 -->
        <el-form v-else ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
          <el-form-item label="申领单号">
            <span>{{ application.applicationNo }}</span>
          </el-form-item>
          <el-form-item label="申领人">
            <span>{{ application.userName }}</span>
          </el-form-item>
          <el-form-item label="物资" prop="materialId">
            <el-select v-model="editForm.materialId" placeholder="请选择物资" filterable style="width: 100%;" @change="handleMaterialChange">
              <el-option
                v-for="item in materialOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="物资编码">
            <span>{{ editForm.materialCode || '-' }}</span>
          </el-form-item>
          <el-form-item label="申领数量" prop="quantity">
            <el-input-number v-model="editForm.quantity" :min="1" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="用途" prop="purpose">
            <el-input v-model="editForm.purpose" placeholder="请输入用途" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="editForm.remark" placeholder="请输入备注" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveEdit">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 审批历史时间线 -->
      <el-card class="info-card" shadow="never">
        <div slot="header" class="card-header">
          <span>审批历史</span>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in approvalTimeline"
            :key="index"
            :timestamp="item.time"
            :type="item.type"
            :icon="item.icon"
            :hollow="item.hollow"
          >
            <div class="timeline-content">
              <div class="timeline-title">{{ item.title }}</div>
              <div class="timeline-desc" v-if="item.operator">操作人：{{ item.operator }}</div>
              <div class="timeline-desc" v-if="item.remark">备注：{{ item.remark }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="application">
        <el-button v-if="isAdmin && application.status === 'PENDING'" type="success" size="medium" @click="handleApprove">
          审批
        </el-button>
        <el-button v-if="isAdmin && application.status === 'TO_BE_OUTBOUND'" type="primary" size="medium" @click="handleOutbound">
          出库
        </el-button>
        <el-button v-if="isAdmin && canRollback(application)" type="warning" size="medium" @click="handleRollback">
          回退状态
        </el-button>
        <el-button v-if="!isAdmin && application.status === 'PENDING'" type="danger" size="medium" @click="handleCancel">
          撤销
        </el-button>
        <el-button size="medium" @click="goBack">
          返回列表
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && !application.id"></el-empty>
  </div>
</template>

<script>
import * as applicationApi from '@/api/application'
import * as materialApi from '@/api/material'

export default {
  name: 'ApplicationDetail',
  data() {
    return {
      loading: false,
      application: {},
      editMode: false,
      editForm: {
        materialId: null,
        materialCode: '',
        quantity: 1,
        purpose: '',
        remark: ''
      },
      editRules: {
        materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入申领数量', trigger: 'blur' }],
        purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }]
      },
      materialOptions: []
    }
  },
  computed: {
    isAdmin() {
      return this.$store.getters.role === 'admin'
    },
    currentUserId() {
      return this.$store.getters.userInfo.userId
    },
    backPath() {
      return this.isAdmin ? '/applications' : '/applications/records'
    },
    showStockWarning() {
      if (!this.application.materialStock || !this.application.quantity) {
        return false
      }
      return this.application.materialStock < this.application.quantity
    },
    stockWarningTitle() {
      if (this.application.materialStock === 0) {
        return '库存已缺货'
      }
      return '库存不足'
    },
    stockWarningDesc() {
      if (this.application.materialStock === 0) {
        return '该物资当前库存为0，无法完成出库操作'
      }
      return `该物资当前库存（${this.application.materialStock}）小于申领数量（${this.application.quantity}），出库后将导致库存为负`
    },
    approvalTimeline() {
      // 如果后端返回了历史记录列表，使用它
      if (this.application.historyList && this.application.historyList.length > 0) {
        return this.application.historyList.map(history => {
          let type = 'primary'
          let icon = 'el-icon-edit'

          switch (history.action) {
            case 'SUBMIT':
              type = 'primary'
              icon = 'el-icon-edit'
              break
            case 'APPROVE':
              type = 'success'
              icon = 'el-icon-check'
              break
            case 'REJECT':
              type = 'danger'
              icon = 'el-icon-close'
              break
            case 'OUTBOUND':
              type = 'primary'
              icon = 'el-icon-truck'
              break
            case 'CANCEL':
              type = 'info'
              icon = 'el-icon-circle-close'
              break
            case 'ROLLBACK':
              type = 'warning'
              icon = 'el-icon-refresh'
              break
            case 'UPDATE':
              type = 'primary'
              icon = 'el-icon-edit'
              break
          }

          return {
            time: history.createTime || '-',
            title: history.actionText || history.action,
            type: type,
            icon: icon,
            operator: history.operatorName || '-',
            remark: history.remark || '-',
            hollow: false
          }
        })
      }

      // 兼容旧数据：手动构造时间线
      const timeline = []
      const app = this.application

      // 提交申请
      timeline.push({
        time: app.applyTime || '-',
        title: '提交申领',
        type: 'primary',
        icon: 'el-icon-edit',
        hollow: false
      })

      // 审批/拒绝
      if (app.approveTime) {
        if (app.status === 'REJECTED') {
          timeline.push({
            time: app.approveTime,
            title: '审批拒绝',
            type: 'danger',
            icon: 'el-icon-close',
            operator: app.approveUserName,
            remark: app.rejectReason || '未说明原因',
            hollow: false
          })
        } else {
          timeline.push({
            time: app.approveTime,
            title: '审批通过',
            type: 'success',
            icon: 'el-icon-check',
            operator: app.approveUserName,
            remark: app.approvalRemark,
            hollow: false
          })
        }
      }

      // 出库
      if (app.outboundTime) {
        timeline.push({
          time: app.outboundTime,
          title: '已出库',
          type: 'primary',
          icon: 'el-icon-truck',
          operator: app.outboundUserName,
          hollow: false
        })
      }

      // 已撤销
      if (app.status === 'CANCELLED') {
        timeline.push({
          time: '-',
          title: '已撤销',
          type: 'info',
          icon: 'el-icon-circle-close',
          hollow: false
        })
      }

      return timeline
    }
  },
  created() {
    this.loadApplicationDetail()
    this.loadMaterialOptions()
  },
  methods: {
    loadMaterialOptions() {
      materialApi.getMaterialList({ limit: 1000 }).then(response => {
        this.materialOptions = response.data.list || response.data || []
      })
    },
    startEdit() {
      this.editForm = {
        materialId: this.application.materialId,
        materialCode: this.application.materialCode,
        quantity: this.application.quantity,
        purpose: this.application.purpose,
        remark: this.application.remark || ''
      }
      this.editMode = true
    },
    cancelEdit() {
      this.editMode = false
    },
    handleMaterialChange(materialId) {
      const material = this.materialOptions.find(item => item.id === materialId)
      if (material) {
        this.editForm.materialCode = material.code
      }
    },
    saveEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          applicationApi.updateApplication(this.application.id, {
            materialId: this.editForm.materialId,
            quantity: this.editForm.quantity,
            purpose: this.editForm.purpose,
            remark: this.editForm.remark
          }).then(() => {
            this.$message.success('更新成功')
            this.editMode = false
            this.loadApplicationDetail()
          }).catch((error) => {
            this.$message.error(error.message || '更新失败')
          })
        }
      })
    },
    loadApplicationDetail() {
      const applicationId = this.$route.params.id || this.$route.query.id
      if (!applicationId) {
        this.$message.error('申领ID不存在')
        return
      }

      this.loading = true
      applicationApi.getApplicationById(applicationId).then(response => {
        this.application = response.data || {}
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getStatusType(status) {
      const map = {
        'PENDING': 'warning',
        'REJECTED': 'danger',
        'TO_BE_OUTBOUND': 'info',
        'OUTBOUND': 'success',
        'CANCELLED': 'info',
        // 兼容旧状态
        'APPROVED': 'info',
        'COMPLETED': 'success'
      }
      return map[status] || ''
    },
    getStatusText(status) {
      const map = {
        'PENDING': '待审批',
        'REJECTED': '已拒绝',
        'TO_BE_OUTBOUND': '待出库',
        'OUTBOUND': '已出库',
        'CANCELLED': '已撤销',
        // 兼容旧状态
        'APPROVED': '待出库',
        'COMPLETED': '已出库'
      }
      return map[status] || status
    },
    canRollback(application) {
      // 已出库不能回退
      return ['TO_BE_OUTBOUND', 'REJECTED'].includes(application.status)
    },
    handleRollback() {
      this.$prompt('请输入回退原因', '状态回退', {
        inputPlaceholder: '回退到待审批状态',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            applicationApi.rollbackApplication(this.application.id, 'PENDING').then(() => {
              this.$message.success('状态回退成功')
              this.loadApplicationDetail()
            })
          }
          done()
        }
      }).catch(() => {})
    },
    handleCancel() {
      this.$confirm('确认撤销该申领单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        applicationApi.cancelApplication(this.application.id).then(() => {
          this.$message.success('撤销成功')
          this.loadApplicationDetail()
        })
      }).catch(() => {
        // 用户取消操作，不做任何处理
      })
    },
    handleApprove() {
      this.$prompt('请输入审批意见', '审批', {
        confirmButtonText: '通过',
        cancelButtonText: '拒绝',
        inputType: 'textarea',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            applicationApi.approveApplication({
              applicationId: this.application.id,
              action: 'APPROVE',
              approvalRemark: instance.inputValue || ''
            }).then(() => {
              this.$message.success('审批通过')
              instance.confirmButtonLoading = false
              done()
              this.loadApplicationDetail()
            }).catch(() => {
              instance.confirmButtonLoading = false
            })
          } else if (action === 'cancel') {
            instance.cancelButtonLoading = true
            this.$prompt('请输入拒绝原因', '拒绝', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              inputPlaceholder: '请输入拒绝原因',
              beforeClose: (action2, instance2, done2) => {
                if (action2 === 'confirm') {
                  applicationApi.approveApplication({
                    applicationId: this.application.id,
                    action: 'REJECT',
                    approvalRemark: instance2.inputValue || ''
                  }).then(() => {
                    this.$message.success('已拒绝')
                    instance2.cancelButtonLoading = false
                    done2()
                    done()
                    this.loadApplicationDetail()
                  }).catch(() => {
                    instance2.cancelButtonLoading = false
                  })
                } else {
                  instance2.cancelButtonLoading = false
                  done2()
                }
              }
            }).catch(() => {
              instance.cancelButtonLoading = false
            })
          } else {
            done()
          }
        }
      }).catch(() => {})
    },
    handleOutbound() {
      this.$confirm(`确认对申领单 ${this.application.applicationNo} 进行出库操作?`, '出库确认', {
        confirmButtonText: '确认出库',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        applicationApi.outboundApplication(this.application.id).then(() => {
          this.$message.success('出库操作完成')
          this.loadApplicationDetail()
        })
      }).catch(() => {})
    },
    goBack() {
      this.$router.push(this.backPath)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.application-detail-container {
  padding: 0;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.detail-content {
  .risk-alert {
    margin-bottom: 20px;

    .risk-info {
      display: flex;
      gap: 24px;
      margin-top: 8px;
      font-size: 14px;
    }
  }

  .info-card {
    border-radius: $border-radius-xl;
    margin-bottom: 20px;

    .card-header {
      font-weight: 600;
      font-size: 16px;
    }

    .info-list {
      .info-item {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid #F0F0F0;

        &:last-child {
          border-bottom: none;
        }

        .info-label {
          color: $text-secondary;
          font-size: 14px;
        }

        .info-value {
          color: $text-primary;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
  }
}

.timeline-content {
  .timeline-title {
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .timeline-desc {
    font-size: 13px;
    color: $text-secondary;
  }
}

::v-deep .el-timeline-item__node {
  background-color: $primary-color;
}
</style>
