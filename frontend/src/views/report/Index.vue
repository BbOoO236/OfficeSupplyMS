<template>
  <div class="app-container">
    <h2>数据报表</h2>
    <p class="description">系统数据统计与分析报表，帮助您了解物资管理情况。</p>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="clearfix">
            <span>库存周转率</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshInventoryTurnover">刷新</el-button>
          </div>
          <div id="inventoryTurnoverChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="clearfix">
            <span>ABC分类分布</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshAbcClassificationChart">刷新</el-button>
          </div>
          <div id="abcClassificationChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header" class="clearfix">
            <span>月度申领趋势</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshMonthlyApplicationTrend">刷新</el-button>
          </div>
          <div id="monthlyApplicationTrendChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="clearfix">
            <span>库存状态分布</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshStockStatusChart">刷新</el-button>
          </div>
          <div id="stockStatusChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="clearfix">
            <span>部门申领统计</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshDepartmentApplicationChart">刷新</el-button>
          </div>
          <div id="departmentApplicationChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'

export default {
  name: 'ReportIndex',
  data() {
    return {
      inventoryTurnoverChart: null,
      abcClassificationChart: null,
      monthlyApplicationTrendChart: null,
      stockStatusChart: null,
      departmentApplicationChart: null
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      this.refreshAllCharts()
    })
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    initCharts() {
      if (!this.$echarts) return
      // 初始化各个图表实例
      const inventoryTurnoverDom = document.getElementById('inventoryTurnoverChart')
      if (inventoryTurnoverDom) {
        this.inventoryTurnoverChart = this.$echarts.init(inventoryTurnoverDom)
      }
      const abcClassificationDom = document.getElementById('abcClassificationChart')
      if (abcClassificationDom) {
        this.abcClassificationChart = this.$echarts.init(abcClassificationDom)
      }
      const monthlyApplicationTrendDom = document.getElementById('monthlyApplicationTrendChart')
      if (monthlyApplicationTrendDom) {
        this.monthlyApplicationTrendChart = this.$echarts.init(monthlyApplicationTrendDom)
      }
      const stockStatusDom = document.getElementById('stockStatusChart')
      if (stockStatusDom) {
        this.stockStatusChart = this.$echarts.init(stockStatusDom)
      }
      const departmentApplicationDom = document.getElementById('departmentApplicationChart')
      if (departmentApplicationDom) {
        this.departmentApplicationChart = this.$echarts.init(departmentApplicationDom)
      }
      window.addEventListener('resize', this.resizeCharts)
    },
    disposeCharts() {
      if (this.inventoryTurnoverChart) {
        this.inventoryTurnoverChart.dispose()
      }
      if (this.abcClassificationChart) {
        this.abcClassificationChart.dispose()
      }
      if (this.monthlyApplicationTrendChart) {
        this.monthlyApplicationTrendChart.dispose()
      }
      if (this.stockStatusChart) {
        this.stockStatusChart.dispose()
      }
      if (this.departmentApplicationChart) {
        this.departmentApplicationChart.dispose()
      }
      window.removeEventListener('resize', this.resizeCharts)
    },
    resizeCharts() {
      const charts = [
        this.inventoryTurnoverChart,
        this.abcClassificationChart,
        this.monthlyApplicationTrendChart,
        this.stockStatusChart,
        this.departmentApplicationChart
      ]
      charts.forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    },
    refreshAllCharts() {
      this.refreshInventoryTurnover()
      this.refreshAbcClassificationChart()
      this.refreshMonthlyApplicationTrend()
      this.refreshStockStatusChart()
      this.refreshDepartmentApplicationChart()
    },
    refreshInventoryTurnover() {
      if (!this.inventoryTurnoverChart) return
      algorithmApi.getInventoryTurnoverRate().then(response => {
        const data = response.data
        const option = {
          title: {
            text: '库存周转率',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: data.categories || ['Q1', 'Q2', 'Q3', 'Q4']
          },
          yAxis: {
            type: 'value',
            name: '周转率'
          },
          series: [
            {
              name: '库存周转率',
              type: 'line',
              data: data.values || [2.5, 2.8, 3.1, 2.9],
              smooth: true,
              lineStyle: {
                width: 3
              },
              itemStyle: {
                color: '#1890ff'
              }
            }
          ]
        }
        this.inventoryTurnoverChart.setOption(option)
      }).catch(() => {
        // 使用模拟数据
        const option = {
          title: {
            text: '库存周转率',
            left: 'center'
          },
          xAxis: {
            type: 'category',
            data: ['Q1', 'Q2', 'Q3', 'Q4']
          },
          yAxis: {
            type: 'value',
            name: '周转率'
          },
          series: [
            {
              name: '库存周转率',
              type: 'line',
              data: [2.5, 2.8, 3.1, 2.9],
              smooth: true
            }
          ]
        }
        this.inventoryTurnoverChart.setOption(option)
      })
    },
    refreshAbcClassificationChart() {
      if (!this.abcClassificationChart) return
      algorithmApi.getAbcClassificationChartData().then(response => {
        const data = response.data
        const option = {
          title: {
            text: 'ABC分类分布',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              name: 'ABC分类',
              type: 'pie',
              radius: '50%',
              data: data || [
                { value: 10, name: 'A类' },
                { value: 20, name: 'B类' },
                { value: 70, name: 'C类' }
              ],
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        }
        this.abcClassificationChart.setOption(option)
      }).catch(() => {
        const option = {
          title: {
            text: 'ABC分类分布',
            left: 'center'
          },
          series: [
            {
              type: 'pie',
              radius: '50%',
              data: [
                { value: 10, name: 'A类' },
                { value: 20, name: 'B类' },
                { value: 70, name: 'C类' }
              ]
            }
          ]
        }
        this.abcClassificationChart.setOption(option)
      })
    },
    refreshMonthlyApplicationTrend() {
      if (!this.monthlyApplicationTrendChart) return
      algorithmApi.getMonthlyApplicationTrend().then(response => {
        const data = response.data
        const option = {
          title: {
            text: '月度申领趋势',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          xAxis: {
            type: 'category',
            data: data.categories || ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          },
          yAxis: {
            type: 'value',
            name: '申领数量'
          },
          series: [
            {
              name: '申领数量',
              type: 'bar',
              data: data.values || [120, 200, 150, 80, 70, 110, 130, 180, 160, 140, 190, 220],
              itemStyle: {
                color: '#52c41a'
              }
            }
          ]
        }
        this.monthlyApplicationTrendChart.setOption(option)
      }).catch(() => {
        const option = {
          title: {
            text: '月度申领趋势',
            left: 'center'
          },
          xAxis: {
            type: 'category',
            data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          },
          yAxis: {
            type: 'value',
            name: '申领数量'
          },
          series: [
            {
              name: '申领数量',
              type: 'bar',
              data: [120, 200, 150, 80, 70, 110, 130, 180, 160, 140, 190, 220]
            }
          ]
        }
        this.monthlyApplicationTrendChart.setOption(option)
      })
    },
    refreshStockStatusChart() {
      if (!this.stockStatusChart) return
      // 模拟库存状态数据
      const option = {
        title: {
          text: '库存状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '库存状态',
            type: 'pie',
            radius: '50%',
            data: [
              { value: 320, name: '正常' },
              { value: 45, name: '库存过低' },
              { value: 28, name: '库存过高' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      this.stockStatusChart.setOption(option)
    },
    refreshDepartmentApplicationChart() {
      if (!this.departmentApplicationChart) return
      // 模拟部门申领数据
      const option = {
        title: {
          text: '部门申领统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['技术部', '市场部', '财务部', '人事部', '行政部', '研发部']
        },
        yAxis: {
          type: 'value',
          name: '申领数量'
        },
        series: [
          {
            name: '申领数量',
            type: 'bar',
            data: [150, 230, 180, 120, 90, 200],
            itemStyle: {
              color: function(params) {
                const colorList = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
                return colorList[params.dataIndex]
              }
            }
          }
        ]
      }
      this.departmentApplicationChart.setOption(option)
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
  margin-bottom: 30px;
}
.chart-card {
  margin-bottom: 20px;
}
</style>