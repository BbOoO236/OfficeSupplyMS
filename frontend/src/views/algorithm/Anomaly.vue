<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/algorithms')" content="异常申领检测" />
    <div class="description">
      <p>基于3σ原则检测申领数量是否异常（超过历史平均值的3倍标准差）</p>
    </div>

    <!-- 申领单号查询 -->
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>按申领单号查询</span>
      </div>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="申领单号">
          <el-input v-model="searchForm.applicationNo" placeholder="输入申领单号查询" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchByNo" :loading="searchLoading">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 查询结果 -->
      <div v-if="searchResult" style="margin-top: 20px;">
        <el-alert
          :title="searchResult.isAnomaly ? '异常申领' : '正常申领'"
          :type="searchResult.isAnomaly ? 'warning' : 'success'"
          :description="searchResult.message"
          show-icon>
        </el-alert>

        <el-table
          :data="[searchResult]"
          border
          style="width: 100%; margin-top: 15px;">
          <el-table-column label="申领单号" prop="applicationNo" align="center" />
          <el-table-column label="用户姓名" prop="userName" align="center" />
          <el-table-column label="部门" prop="userDepartment" align="center" />
          <el-table-column label="物资名称" prop="materialName" align="center" />
          <el-table-column label="申领数量" prop="currentQuantity" align="center" />
          <el-table-column label="历史均值" align="center">
            <template slot-scope="{row}">
              <span v-if="row.historicalMean !== null && row.historicalMean !== undefined">{{ formatNumber(row.historicalMean) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="历史标准差" align="center">
            <template slot-scope="{row}">
              <span v-if="row.historicalStdDev !== null && row.historicalStdDev !== undefined">{{ formatNumber(row.historicalStdDev) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="异常阈值" align="center">
            <template slot-scope="{row}">
              <span v-if="row.threshold !== null && row.threshold !== undefined">{{ formatNumber(row.threshold) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="偏离程度" align="center">
            <template slot-scope="{row}">
              <span v-if="row.deviation !== null && row.deviation !== undefined" style="color: #ff4d4f; font-weight: bold;">{{ formatNumber(row.deviation) }}%</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="isAnomaly" align="center">
            <template slot-scope="{row}">
              <el-tag :type="row.isAnomaly ? 'warning' : 'success'">
                {{ row.isAnomaly ? '异常' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="searchResult.isAbnormal" style="margin-top: 15px; padding: 15px; background: #fff2f0; border-radius: 4px;">
          <h4 style="color: #ff4d4f; margin-top: 0;">建议操作</h4>
          <p>{{ searchResult.suggestedAction }}</p>
        </div>
      </div>

      <el-empty v-if="!searchLoading && searched && !searchResult" description="未找到该申领单" style="margin-top: 20px;" />
    </el-card>

    <!-- 异常申领记录列表 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>异常申领记录</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadAnomalies" :loading="tableLoading">刷新</el-button>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="paginatedData"
        border
        fit
        highlight-current-row
        style="width: 100%;">
        <el-table-column label="申领单号" prop="applicationNo" align="center" width="150" />
        <el-table-column label="用户姓名" prop="userName" align="center" width="100" />
        <el-table-column label="部门" prop="userDepartment" align="center" width="100" />
        <el-table-column label="物资名称" prop="materialName" align="center" />
        <el-table-column label="物资编码" prop="materialCode" align="center" width="120" />
        <el-table-column label="申领数量" prop="currentQuantity" align="center" width="100" />
        <el-table-column label="历史均值" prop="historicalMean" align="center" width="100">
          <template slot-scope="{row}">
            <span v-if="row.historicalMean !== null && row.historicalMean !== undefined">{{ formatNumber(row.historicalMean) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="历史标准差" prop="historicalStdDev" align="center" width="120">
          <template slot-scope="{row}">
            <span v-if="row.historicalStdDev !== null && row.historicalStdDev !== undefined">{{ formatNumber(row.historicalStdDev) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="异常阈值" prop="threshold" align="center" width="100">
          <template slot-scope="{row}">
            <span v-if="row.threshold !== null && row.threshold !== undefined">{{ formatNumber(row.threshold) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="偏离程度" prop="deviation" align="center" width="100">
          <template slot-scope="{row}">
            <span v-if="row.deviation !== null && row.deviation !== undefined" style="color: #ff4d4f; font-weight: bold;">{{ formatNumber(row.deviation) }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="异常原因" prop="abnormalReason" align="center" />
        <el-table-column label="申领时间" prop="applyTime" align="center" width="160" />
      </el-table>

      <pagination
        v-show="tableData.length > 0"
        :total="tableData.length"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="handlePageChange"
      />

      <el-empty v-if="!tableLoading && tableData.length === 0" description="暂无异常申领记录" style="margin-top: 40px;" />
    </el-card>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'
import * as applicationApi from '@/api/application'
import Pagination from '@/components/Pagination'

export default {
  name: 'AlgorithmAnomaly',
  components: { Pagination },
  data() {
    return {
      tableLoading: false,
      searchLoading: false,
      searched: false,
      tableData: [],
      searchForm: {
        applicationNo: ''
      },
      searchResult: null,
      listQuery: {
        page: 1,
        limit: 10
      }
    }
  },
  computed: {
    paginatedData() {
      const start = (this.listQuery.page - 1) * this.listQuery.limit
      const end = start + this.listQuery.limit
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadAnomalies()
  },
  methods: {
    loadAnomalies() {
      this.tableLoading = true
      this.listQuery.page = 1
      algorithmApi.getAnomalousApplications().then(response => {
        this.tableData = response.data || []
        this.tableLoading = false
      }).catch(() => {
        this.tableData = []
        this.tableLoading = false
      })
    },
    handlePageChange() {
      // Just trigger re-render of paginatedData
    },
    searchByNo() {
      if (!this.searchForm.applicationNo) {
        this.$message.warning('请输入申领单号')
        return
      }
      this.searchLoading = true
      this.searched = true
      applicationApi.getApplicationByNo(this.searchForm.applicationNo).then(response => {
        const app = response.data
        if (app) {
          // 使用数据库中 isAbnormal 字段的值（这是创建/审批时标记的）
          const isAbnormal = app.isAbnormal === 1

          if (isAbnormal) {
            // 如果是异常申领，获取统计信息进行显示
            algorithmApi.checkApplicationAnomaly(app.userId, app.materialId, app.quantity).then(res => {
              const data = res.data
              this.searchResult = {
                applicationNo: app.applicationNo,
                userName: app.userName,
                userDepartment: app.userDepartment,
                materialName: app.materialName,
                materialCode: app.materialCode,
                currentQuantity: app.quantity,
                historicalMean: data ? data.historicalMean : null,
                historicalStdDev: data ? data.historicalStdDev : null,
                threshold: data ? data.threshold : null,
                deviation: data ? data.deviation : null,
                isAnomaly: true,
                isAbnormal: true,
                abnormalReason: app.abnormalReason,
                status: app.status,
                applyTime: app.applyTime,
                message: app.abnormalReason || `申领数量 ${app.quantity} 超过历史平均值的3倍标准差，属于异常申领`,
                suggestedAction: '建议审批人仔细核实该申领，可能存在异常或浪费情况'
              }
              this.searchLoading = false
            }).catch(() => {
              this.buildSearchResult(app, true)
              this.searchLoading = false
            })
          } else {
            this.buildSearchResult(app, false)
            this.searchLoading = false
          }
        } else {
          this.$message.error('未找到该申领单')
          this.searchResult = null
          this.searchLoading = false
        }
      }).catch(() => {
        this.$message.error('未找到该申领单')
        this.searchResult = null
        this.searchLoading = false
      })
    },
    buildSearchResult(app, isAbnormal) {
      this.searchResult = {
        applicationNo: app.applicationNo,
        userName: app.userName,
        userDepartment: app.userDepartment,
        materialName: app.materialName,
        materialCode: app.materialCode,
        currentQuantity: app.quantity,
        historicalMean: null,
        historicalStdDev: null,
        threshold: null,
        deviation: null,
        isAnomaly: isAbnormal,
        isAbnormal: isAbnormal,
        abnormalReason: app.abnormalReason,
        status: app.status,
        applyTime: app.applyTime,
        message: isAbnormal ?
          (app.abnormalReason || '异常申领') :
          `申领数量 ${app.quantity} 在正常范围内`,
        suggestedAction: ''
      }
    },
    resetSearch() {
      this.searchForm.applicationNo = ''
      this.searchResult = null
      this.searched = false
    },
    formatNumber(value) {
      if (value === null || value === undefined) return '-'
      // Handle BigDecimal or number
      const num = typeof value === 'object' ? parseFloat(value.toString()) : value
      if (isNaN(num)) return '-'
      return num.toFixed(2)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
.description {
  margin: 20px 0;
  color: #606266;
}
.box-card {
  margin-top: 20px;
}
</style>
