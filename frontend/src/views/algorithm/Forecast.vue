<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/algorithms')" content="需求预测" />
    <div class="description">
      <p>基于历史消耗数据预测未来需求，辅助采购决策。</p>
    </div>

    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>需求预测</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">刷新数据</el-button>
      </div>
      <div>
        <el-form :inline="true" :model="form" class="demo-form-inline">
          <el-form-item label="预测周期">
            <el-select v-model="form.period" placeholder="请选择预测周期">
              <el-option label="1个月" :value="1" />
              <el-option label="3个月" :value="3" />
              <el-option label="6个月" :value="6" />
              <el-option label="12个月" :value="12" />
            </el-select>
          </el-form-item>
          <el-form-item label="物资">
            <el-select v-model="form.materialId" placeholder="请选择物资" clearable filterable>
              <el-option
                v-for="item in materialOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="calculateForecast" :loading="loading">开始预测</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="showChart" style="margin-top: 20px;">
        <div id="forecastChart" style="width: 100%; height: 400px;"></div>
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
        <el-table-column label="历史平均月消耗" prop="historicalAvg" align="center" />
        <el-table-column label="预测月消耗" prop="forecastAvg" align="center" />
        <el-table-column label="预测总需求" prop="forecastTotal" align="center" />
        <el-table-column label="置信区间" prop="confidenceInterval" align="center" />
        <el-table-column label="建议采购量" prop="recommendedPurchase" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'
import * as materialApi from '@/api/material'

export default {
  name: 'AlgorithmForecast',
  data() {
    return {
      loading: false,
      tableLoading: false,
      showChart: true,
      form: {
        period: 3,
        materialId: ''
      },
      materialOptions: [],
      tableData: [],
      chart: null
    }
  },
  mounted() {
    this.fetchMaterials()
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
    fetchMaterials() {
      materialApi.getMaterialList({ page: 1, limit: 1000 }).then(response => {
        this.materialOptions = response.data.list || response.data
      })
    },
    refreshData() {
      // 可以加载最近一次的预测结果
      this.tableLoading = true
      algorithmApi.forecastDemand(this.form.period).then(response => {
        this.tableData = response.data
        this.tableLoading = false
        this.updateChart()
      }).catch(() => {
        this.tableLoading = false
      })
    },
    calculateForecast() {
      this.loading = true
      if (this.form.materialId) {
        algorithmApi.forecastMaterialDemand(this.form.materialId, this.form.period).then(response => {
          this.$message.success('需求预测计算完成')
          this.tableData = [response.data]
          this.updateChart()
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        algorithmApi.forecastDemand(this.form.period).then(response => {
          this.$message.success('需求预测计算完成')
          this.tableData = response.data
          this.updateChart()
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    },
    initChart() {
      if (!this.$echarts) return
      const chartDom = document.getElementById('forecastChart')
      if (!chartDom) return
      this.chart = this.$echarts.init(chartDom)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart || this.tableData.length === 0) return
      // 简单示例：显示预测值与历史值
      const categories = this.tableData.map(item => item.materialName)
      const historical = this.tableData.map(item => item.historicalAvg)
      const forecast = this.tableData.map(item => item.forecastAvg)
      const option = {
        title: {
          text: '需求预测',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['历史平均', '预测值'],
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
          name: '消耗量'
        },
        series: [
          {
            name: '历史平均',
            type: 'bar',
            data: historical
          },
          {
            name: '预测值',
            type: 'bar',
            data: forecast
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