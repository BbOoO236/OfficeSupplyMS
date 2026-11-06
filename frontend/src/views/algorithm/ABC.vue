<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/algorithms')" content="ABC分类分析" />
    <div class="description">
      <p>根据物资消耗金额进行ABC分类，识别重要物资。</p>
    </div>

    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>ABC分类计算</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">刷新数据</el-button>
      </div>
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="物资名称">
            <el-input v-model="filterForm.materialName" placeholder="请输入物资名称" clearable />
          </el-form-item>
          <el-form-item label="物资编码">
            <el-input v-model="filterForm.materialCode" placeholder="请输入物资编码" clearable />
          </el-form-item>
          <el-form-item label="ABC分类">
            <el-select v-model="filterForm.abcClass" placeholder="请选择分类" clearable>
              <el-option label="A类" value="A" />
              <el-option label="B类" value="B" />
              <el-option label="C类" value="C" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div style="margin-top: 15px;">
        <el-button type="primary" @click="calculateAbcClassification" :loading="loading">开始计算</el-button>
        <el-button @click="showChart = !showChart">{{ showChart ? '隐藏图表' : '显示图表' }}</el-button>
      </div>

      <div v-if="showChart" style="margin-top: 20px;">
        <div id="abcChart" style="width: 100%; height: 400px;"></div>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="paginatedData"
        border
        fit
        highlight-current-row
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column label="物资名称" prop="materialName" align="center" />
        <el-table-column label="物资编码" prop="materialCode" align="center" />
        <el-table-column label="消耗金额" prop="consumptionAmount" align="center">
          <template slot-scope="{row}">
            <span>¥{{ row.consumptionAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计百分比" prop="cumulativePercentage" align="center">
          <template slot-scope="{row}">
            <span>{{ row.cumulativePercentage }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="ABC分类" prop="abcClass" align="center">
          <template slot-scope="{row}">
            <el-tag :type="row.abcClass === 'A' ? 'danger' : row.abcClass === 'B' ? 'warning' : ''">
              {{ row.abcClass }}类
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建议管理策略" prop="managementStrategy" align="center" />
      </el-table>
      <pagination
        v-show="filteredData.length > 0"
        :total="filteredData.length"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script>
import * as algorithmApi from '@/api/algorithm'
import Pagination from '@/components/Pagination'

export default {
  name: 'AlgorithmABC',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableLoading: false,
      showChart: true,
      tableData: [],
      filterForm: {
        materialName: '',
        materialCode: '',
        abcClass: ''
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      chart: null
    }
  },
  computed: {
    filteredData() {
      let data = this.tableData
      if (this.filterForm.materialName) {
        data = data.filter(item => item.materialName && item.materialName.toLowerCase().includes(this.filterForm.materialName.toLowerCase()))
      }
      if (this.filterForm.materialCode) {
        data = data.filter(item => item.materialCode && item.materialCode.toLowerCase().includes(this.filterForm.materialCode.toLowerCase()))
      }
      if (this.filterForm.abcClass) {
        data = data.filter(item => item.abcClass === this.filterForm.abcClass)
      }
      return data
    },
    paginatedData() {
      const start = (this.listQuery.page - 1) * this.listQuery.limit
      const end = start + this.listQuery.limit
      return this.filteredData.slice(start, end)
    }
  },
  mounted() {
    this.refreshData()
    this.$nextTick(() => {
      this.initChart()
    })
  },
  watch: {
    showChart(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.initChart()
        })
      }
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    refreshData() {
      this.tableLoading = true
      algorithmApi.calculateAbcClassification().then(response => {
        this.tableData = response.data
        this.tableLoading = false
        this.updateChart()
      }).catch(() => {
        this.tableLoading = false
      })
    },
    calculateAbcClassification() {
      this.loading = true
      algorithmApi.calculateAbcClassification().then(response => {
        this.$message.success('ABC分类计算完成')
        this.tableData = response.data
        this.listQuery.page = 1
        this.updateChart()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
    },
    resetFilter() {
      this.filterForm = {
        materialName: '',
        materialCode: '',
        abcClass: ''
      }
      this.listQuery.page = 1
    },
    handlePageChange() {
      // Just trigger re-render of paginatedData
    },
    initChart() {
      if (!this.$echarts) return
      const chartDom = document.getElementById('abcChart')
      if (!chartDom) return
      if (this.chart) {
        this.chart.dispose()
      }
      this.chart = this.$echarts.init(chartDom)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart || this.tableData.length === 0) return
      // 准备数据
      const categories = this.tableData.map(item => item.materialName)
      const amounts = this.tableData.map(item => item.consumptionAmount)
      const abcClasses = this.tableData.map(item => item.abcClass)
      // 颜色映射
      const colorMap = { A: '#ff4d4f', B: '#faad14', C: '#52c41a' }
      const colors = abcClasses.map(cls => colorMap[cls])
      const option = {
        title: {
          text: 'ABC分类分析',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const item = this.tableData[dataIndex]
            return `物资: ${item.materialName}<br/>消耗金额: ¥${item.consumptionAmount}<br/>分类: ${item.abcClass}类`
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
          name: '消耗金额 (元)'
        },
        series: [
          {
            name: '消耗金额',
            type: 'bar',
            data: amounts,
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
.filter-container {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}
</style>