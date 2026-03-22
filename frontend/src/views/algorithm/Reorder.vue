<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/algorithms')" content="补货点计算" />
    <div class="description">
      <p>计算物资的安全库存和补货点，避免缺货。</p>
    </div>

    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>补货点计算</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">刷新数据</el-button>
      </div>
      <div>
        <el-button type="primary" @click="calculateReorderPoints" :loading="loading">开始计算</el-button>
        <el-button @click="showChart = !showChart">{{ showChart ? '隐藏图表' : '显示图表' }}</el-button>
      </div>

      <div v-if="showChart" style="margin-top: 20px;">
        <div id="reorderChart" style="width: 100%; height: 400px;"></div>
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
        <el-table-column label="当前库存" prop="currentStock" align="center" />
        <el-table-column label="安全库存" prop="safetyStock" align="center" />
        <el-table-column label="补货点" prop="reorderPoint" align="center" />
        <el-table-column label="日均消耗" prop="dailyConsumption" align="center" />
        <el-table-column label="采购提前期" prop="leadTime" align="center">
          <template slot-scope="{row}">
            <span>{{ row.leadTime }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="安全系数" prop="safetyFactor" align="center" />
        <el-table-column label="库存状态" prop="stockStatus" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.stockStatus === 'LOW' ? 'danger' : row.stockStatus === 'HIGH' ? 'warning' : 'success'">
              {{ row.stockStatus === 'LOW' ? '库存过低' : row.stockStatus === 'HIGH' ? '库存过高' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否需要补货" prop="needReorder" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.needReorder ? 'danger' : 'success'">
              {{ row.needReorder ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'

export default {
  name: 'AlgorithmReorder',
  data() {
    return {
      loading: false,
      tableLoading: false,
      showChart: true,
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
      algorithmApi.calculateReorderPoints().then(response => {
        this.tableData = response.data
        this.tableLoading = false
        this.updateChart()
      }).catch(() => {
        this.tableLoading = false
      })
    },
    calculateReorderPoints() {
      this.loading = true
      algorithmApi.calculateReorderPoints().then(response => {
        this.$message.success('补货点计算完成')
        this.tableData = response.data
        this.updateChart()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    initChart() {
      if (!this.$echarts) return
      const chartDom = document.getElementById('reorderChart')
      if (!chartDom) return
      this.chart = this.$echarts.init(chartDom)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart || this.tableData.length === 0) return
      const categories = this.tableData.map(item => item.materialName)
      const currentStock = this.tableData.map(item => item.currentStock)
      const safetyStock = this.tableData.map(item => item.safetyStock)
      const reorderPoint = this.tableData.map(item => item.reorderPoint)
      const option = {
        title: {
          text: '补货点分析',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const item = this.tableData[dataIndex]
            return `物资: ${item.materialName}<br/>当前库存: ${item.currentStock}<br/>安全库存: ${item.safetyStock}<br/>补货点: ${item.reorderPoint}`
          }.bind(this)
        },
        legend: {
          data: ['当前库存', '安全库存', '补货点'],
          top: 30
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
          name: '数量'
        },
        series: [
          {
            name: '当前库存',
            type: 'bar',
            data: currentStock
          },
          {
            name: '安全库存',
            type: 'bar',
            data: safetyStock
          },
          {
            name: '补货点',
            type: 'line',
            data: reorderPoint,
            itemStyle: {
              color: '#ff4d4f'
            },
            lineStyle: {
              width: 3
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