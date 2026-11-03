<template>
  <div class="app-container">
    <h2>数据报表</h2>
    <p class="description">系统数据统计与分析报表，帮助您了解物资管理情况。</p>

    <!-- 报表类型切换按钮 -->
    <div class="report-tabs">
      <el-radio-group v-model="activeReport" @change="switchReport">
        <el-radio-button label="turnover">库存周转率</el-radio-button>
        <el-radio-button label="abc">ABC分类分布</el-radio-button>
        <el-radio-button label="monthly">月度申领趋势</el-radio-button>
        <el-radio-button label="stock">库存状态</el-radio-button>
        <el-radio-button label="department">部门申领统计</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 报表内容区域 -->
    <div class="report-content">
      <!-- 库存周转率 -->
      <el-card v-show="activeReport === 'turnover'" class="chart-card">
        <div slot="header" class="clearfix">
          <span>库存周转率</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshInventoryTurnover" :loading="loading">刷新</el-button>
        </div>
        <div v-if="loading" style="text-align: center; padding: 50px;">
          <i class="el-icon-loading" style="font-size: 30px;"></i>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
        <div v-else-if="!turnoverData.labels || turnoverData.labels.length === 0" style="text-align: center; padding: 50px; color: #909399;">
          暂无数据
        </div>
        <div v-else id="turnoverChart" style="width: 100%; height: 400px;"></div>
      </el-card>

      <!-- ABC分类分布 -->
      <el-card v-show="activeReport === 'abc'" class="chart-card">
        <div slot="header" class="clearfix">
          <span>ABC分类分布</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshAbcClassification" :loading="loading">刷新</el-button>
        </div>
        <div v-if="loading" style="text-align: center; padding: 50px;">
          <i class="el-icon-loading" style="font-size: 30px;"></i>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
        <div v-else-if="!abcData.labels || abcData.labels.length === 0" style="text-align: center; padding: 50px; color: #909399;">
          暂无数据，请先执行ABC分类计算
        </div>
        <div v-else id="abcChart" style="width: 100%; height: 400px;"></div>
      </el-card>

      <!-- 月度申领趋势 -->
      <el-card v-show="activeReport === 'monthly'" class="chart-card">
        <div slot="header" class="clearfix">
          <span>月度申领趋势</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshMonthlyTrend" :loading="loading">刷新</el-button>
        </div>
        <div v-if="loading" style="text-align: center; padding: 50px;">
          <i class="el-icon-loading" style="font-size: 30px;"></i>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
        <div v-else-if="!monthlyData.labels || monthlyData.labels.length === 0" style="text-align: center; padding: 50px; color: #909399;">
          暂无数据
        </div>
        <div v-else id="monthlyChart" style="width: 100%; height: 400px;"></div>
      </el-card>

      <!-- 库存状态 -->
      <el-card v-show="activeReport === 'stock'" class="chart-card">
        <div slot="header" class="clearfix">
          <span>库存状态分布</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshStockStatus" :loading="loading">刷新</el-button>
        </div>
        <div v-if="loading" style="text-align: center; padding: 50px;">
          <i class="el-icon-loading" style="font-size: 30px;"></i>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
        <div v-else-if="!stockData.labels || stockData.labels.length === 0" style="text-align: center; padding: 50px; color: #909399;">
          暂无数据
        </div>
        <div v-else id="stockChart" style="width: 100%; height: 400px;"></div>
      </el-card>

      <!-- 部门申领统计 -->
      <el-card v-show="activeReport === 'department'" class="chart-card">
        <div slot="header" class="clearfix">
          <span>部门申领统计</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshDepartment" :loading="loading">刷新</el-button>
        </div>
        <div v-if="loading" style="text-align: center; padding: 50px;">
          <i class="el-icon-loading" style="font-size: 30px;"></i>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
        <div v-else-if="!departmentData.labels || departmentData.labels.length === 0" style="text-align: center; padding: 50px; color: #909399;">
          暂无数据
        </div>
        <div v-else id="departmentChart" style="width: 100%; height: 400px;"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'

export default {
  name: 'ReportIndex',
  data() {
    return {
      activeReport: 'turnover',
      loading: false,
      charts: {},
      turnoverData: {},
      abcData: {},
      monthlyData: {},
      stockData: {},
      departmentData: {}
    }
  },
  mounted() {
    this.switchReport('turnover')
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    switchReport(report) {
      this.activeReport = report  // 确保activeReport立即更新
      this.loading = true
      this.clearCharts()
      setTimeout(() => {
        this.loadReportData(report)
      }, 300)
    },
    loadReportData(report) {
      switch (report) {
        case 'turnover':
          this.refreshInventoryTurnover()
          break
        case 'abc':
          this.refreshAbcClassification()
          break
        case 'monthly':
          this.refreshMonthlyTrend()
          break
        case 'stock':
          this.refreshStockStatus()
          break
        case 'department':
          this.refreshDepartment()
          break
      }
    },
    clearCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].dispose()
          delete this.charts[key]
        }
      })
    },
    getChartInstance(domId) {
      if (!this.$echarts) return null
      const dom = document.getElementById(domId)
      if (!dom) {
        console.warn('DOM element not found:', domId)
        return null
      }
      if (!this.charts[domId]) {
        this.charts[domId] = this.$echarts.init(dom)
      }
      return this.charts[domId]
    },
    disposeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].dispose()
        }
      })
      this.charts = {}
    },
    initChart(domId, data, setOptionFunc) {
      // 等待DOM元素渲染，最多等待3秒
      const waitForDom = (retries = 30) => {
        const chart = this.getChartInstance(domId)
        if (chart) {
          setOptionFunc(chart, data)
        } else if (retries > 0) {
          setTimeout(() => waitForDom(retries - 1), 100)
        } else {
          console.error('DOM element not found after retries:', domId)
        }
      }
      waitForDom()
    },
    // 库存周转率
    refreshInventoryTurnover() {
      this.loading = true
      algorithmApi.getInventoryTurnoverRate().then(response => {
        console.log('Inventory turnover response:', response)
        const data = response.data
        if (data && data.length > 0 && data[0].labels && data[0].labels.length > 0) {
          this.turnoverData = data[0]
          this.$nextTick(() => {
            this.initChart('turnoverChart', data[0], this.setTurnoverChartOption.bind(this))
          })
        } else {
          this.turnoverData = { labels: [], values: [] }
        }
        this.loading = false
      }).catch((error) => {
        console.error('Inventory turnover error:', error)
        this.turnoverData = { labels: [], values: [] }
        this.loading = false
      })
    },
    setTurnoverChartOption(chart, data) {
      const option = {
        title: {
          text: data.title || '库存周转率',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} ' + (data.unit || '次')
        },
        xAxis: {
          type: 'category',
          data: data.labels || []
        },
        yAxis: {
          type: 'value',
          name: data.unit || '次'
        },
        series: [{
          name: '库存周转率',
          type: 'line',
          data: data.values || [],
          smooth: true,
          lineStyle: { width: 3 },
          itemStyle: { color: '#1890ff' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
              ]
            }
          }
        }],
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
      }
      chart.setOption(option)
      setTimeout(() => chart.resize(), 100)
    },
    // ABC分类分布
    refreshAbcClassification() {
      this.loading = true
      algorithmApi.getAbcClassificationChartData().then(response => {
        console.log('ABC classification response:', response)
        const data = response.data
        if (data && data.labels && data.labels.length > 0) {
          this.abcData = data
          this.$nextTick(() => {
            this.initChart('abcChart', data, this.setAbcChartOption.bind(this))
          })
        } else {
          this.abcData = { labels: [], values: [], counts: [] }
        }
        this.loading = false
      }).catch((error) => {
        console.error('ABC classification error:', error)
        this.abcData = { labels: [], values: [], counts: [] }
        this.loading = false
      })
    },
    setAbcChartOption(chart, data) {
      // 后端返回的values是百分比，counts是实际数量
      const pieData = data.labels.map((label, i) => ({
        name: label,
        value: data.values[i],
        count: data.counts ? data.counts[i] : 0
      }))
      const option = {
        title: {
          text: data.title || 'ABC分类占比',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            return `${params.name}<br/>百分比: ${params.value}%<br/>数量: ${params.data.count} 个`
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          formatter: function(name) {
            const item = pieData.find(p => p.name === name)
            if (item) {
              return `${name} (${item.count}个, ${item.value}%)`
            }
            return name
          }
        },
        series: [{
          name: 'ABC分类',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: function(params) {
              return `${params.name}\n${params.value}%\n(${params.data.count}个)`
            }
          },
          data: pieData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          color: ['#ff6b6b', '#feca57', '#48dbfb']
        }]
      }
      chart.setOption(option)
      setTimeout(() => chart.resize(), 100)
    },
    // 月度申领趋势
    refreshMonthlyTrend() {
      this.loading = true
      algorithmApi.getMonthlyApplicationTrend().then(response => {
        console.log('Monthly trend response:', response)
        const data = response.data
        console.log('Monthly trend data:', data)
        if (data && data.labels && data.labels.length > 0) {
          this.monthlyData = data
          this.$nextTick(() => {
            this.initChart('monthlyChart', data, this.setMonthlyChartOption.bind(this))
          })
        } else {
          this.monthlyData = { labels: [], values: [] }
        }
        this.loading = false
      }).catch((error) => {
        console.error('Monthly trend error:', error)
        this.monthlyData = { labels: [], values: [] }
        this.loading = false
      })
    },
    setMonthlyChartOption(chart, data) {
      const option = {
        title: {
          text: data.title || '月度申领趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c} ' + (data.unit || '个')
        },
        xAxis: {
          type: 'category',
          data: data.labels || []
        },
        yAxis: {
          type: 'value',
          name: data.unit || '个'
        },
        series: [{
          name: '申领数量',
          type: 'bar',
          data: data.values || [],
          itemStyle: {
            color: '#52c41a'
          },
          barWidth: '60%'
        }],
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
      }
      chart.setOption(option)
      setTimeout(() => chart.resize(), 100)
    },
    // 库存状态
    refreshStockStatus() {
      this.loading = true
      algorithmApi.getStockStatusDistribution().then(response => {
        console.log('Stock status response:', response)
        const data = response.data
        if (data && data.labels && data.labels.length > 0) {
          this.stockData = data
          this.$nextTick(() => {
            this.initChart('stockChart', data, this.setStockChartOption.bind(this))
          })
        } else {
          this.stockData = { labels: [], values: [] }
        }
        this.loading = false
      }).catch((error) => {
        console.error('Stock status error:', error)
        this.stockData = { labels: [], values: [] }
        this.loading = false
      })
    },
    setStockChartOption(chart, data) {
      const pieData = (data.labels || []).map((label, i) => ({
        name: label,
        value: data.values[i] || 0
      }))
      const option = {
        title: {
          text: data.title || '库存状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}个 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          name: '库存状态',
          type: 'pie',
          radius: '60%',
          center: ['50%', '50%'],
          data: pieData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          color: ['#52c41a', '#ff4d4f', '#faad14']
        }]
      }
      chart.setOption(option)
      setTimeout(() => chart.resize(), 100)
    },
    // 部门申领统计
    refreshDepartment() {
      this.loading = true
      algorithmApi.getDepartmentApplicationStats().then(response => {
        console.log('Department stats response:', response)
        const data = response.data
        console.log('Department stats data:', data)
        if (data && data.labels && data.labels.length > 0) {
          this.departmentData = data
          this.$nextTick(() => {
            this.initChart('departmentChart', data, this.setDepartmentChartOption.bind(this))
          })
        } else {
          this.departmentData = { labels: [], values: [] }
        }
        this.loading = false
      }).catch((error) => {
        console.error('Department stats error:', error)
        this.departmentData = { labels: [], values: [] }
        this.loading = false
      })
    },
    setDepartmentChartOption(chart, data) {
      const option = {
        title: {
          text: data.title || '部门申领统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}' + (data.unit || '个')
        },
        xAxis: {
          type: 'category',
          data: data.labels || [],
          axisLabel: { rotate: 45 }
        },
        yAxis: {
          type: 'value',
          name: data.unit || '个'
        },
        series: [{
          name: '申领数量',
          type: 'bar',
          data: data.values || [],
          itemStyle: {
            color: '#5470c6'
          },
          barWidth: '50%'
        }],
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true }
      }
      chart.setOption(option)
      setTimeout(() => chart.resize(), 100)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}
h2 {
  margin-bottom: 10px;
}
.description {
  color: #606266;
  margin-bottom: 20px;
}
.report-tabs {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}
.report-content {
  min-height: 500px;
}
.chart-card {
  margin-bottom: 20px;
}
</style>
