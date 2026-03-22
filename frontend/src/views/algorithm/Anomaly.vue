<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/algorithms')" content="异常检测" />
    <div class="description">
      <p>检测库存和消耗异常，及时发现异常波动。</p>
    </div>

    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>异常检测</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">刷新数据</el-button>
      </div>
      <div>
        <el-form :inline="true" :model="form" class="demo-form-inline">
          <el-form-item label="检测日期">
            <el-date-picker
              v-model="form.date"
              type="date"
              placeholder="选择日期"
              value-format="yyyy-MM-dd"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="detectAnomalies" :loading="loading">开始检测</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="showChart" style="margin-top: 20px;">
        <div id="anomalyChart" style="width: 100%; height: 400px;"></div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="tableData"
        border
        fit
        highlight-current-row
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column label="物资名称" prop="materialName" align="center" />
        <el-table-column label="物资编码" prop="materialCode" align="center" />
        <el-table-column label="异常类型" prop="anomalyType" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.anomalyType === 'HIGH' ? 'warning' : 'danger'">
              {{ row.anomalyType === 'HIGH' ? '消耗过高' : '消耗过低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="检测值" prop="detectedValue" align="center" />
        <el-table-column label="正常范围" prop="normalRange" align="center" />
        <el-table-column label="偏离程度" prop="deviation" align="center">
          <template slot-scope="{row}">
            <span>{{ row.deviation }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="检测时间" prop="detectedTime" align="center" />
        <el-table-column label="建议操作" prop="suggestedAction" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'

export default {
  name: 'AlgorithmAnomaly',
  data() {
    return {
      loading: false,
      tableLoading: false,
      showChart: true,
      form: {
        date: new Date().toISOString().split('T')[0] // 今天
      },
      tableData: [],
      chart: null
    }
  },
  mounted() {
    this.refreshData()
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    refreshData() {
      this.tableLoading = true
      algorithmApi.detectAnomalies(this.form.date).then(response => {
        this.tableData = response.data
        this.tableLoading = false
        this.updateChart()
      }).catch(() => {
        this.tableLoading = false
      })
    },
    detectAnomalies() {
      this.loading = true
      algorithmApi.detectAnomalies(this.form.date).then(response => {
        this.$message.success('异常检测完成')
        this.tableData = response.data
        this.updateChart()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    initChart() {
      if (!this.$echarts) return
      const chartDom = document.getElementById('anomalyChart')
      if (!chartDom) return
      this.chart = this.$echarts.init(chartDom)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart || this.tableData.length === 0) return
      const categories = this.tableData.map(item => item.materialName)
      const deviations = this.tableData.map(item => item.deviation)
      const anomalyTypes = this.tableData.map(item => item.anomalyType)
      const colors = anomalyTypes.map(type => type === 'HIGH' ? '#faad14' : '#ff4d4f')
      const option = {
        title: {
          text: '异常检测结果',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const item = this.tableData[dataIndex]
            return `物资: ${item.materialName}<br/>偏离程度: ${item.deviation}%<br/>异常类型: ${item.anomalyType === 'HIGH' ? '消耗过高' : '消耗过低'}`
          }.bind(this)
        },
        xAxis: {
          type: 'category',
          data: categories,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '偏离程度 (%)'
        },
        series: [
          {
            name: '偏离程度',
            type: 'bar',
            data: deviations,
            itemStyle: {
              color: function(params) {
                return colors[params.dataIndex]
              }
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      }
      this.chart.setOption(option)
      window.addEventListener('resize', this.resizeChart)
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize()
      }
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