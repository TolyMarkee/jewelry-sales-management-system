<template>
  <div class="container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalOrders }}</div>
          <div class="stat-label">订单总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalCustomers }}</div>
          <div class="stat-label">客户总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num" style="color:#f56c6c">¥{{ stats.totalSales }}</div>
          <div class="stat-label">销售总额</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <div slot="header"><span>订单状态分布</span></div>
          <div ref="pieChart" style="height:350px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header"><span>最近订单统计</span></div>
          <div ref="barChart" style="height:350px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'

export default {
  name: 'SalesStatistics',
  data() {
    return {
      stats: { totalProducts: 0, totalOrders: 0, totalCustomers: 0, totalSales: 0 }
    }
  },
  mounted() { this.loadStats() },
  methods: {
    async loadStats() {
      try {
        const [prodRes, custRes, orderRes] = await Promise.all([
          getAllProducts(),
          getAllCustomers(),
          getOrderList({ page: 1, limit: 1000 })
        ])
        this.stats.totalProducts = prodRes.data.length
        this.stats.totalCustomers = custRes.data.length
        this.stats.totalOrders = orderRes.data.total
        this.stats.totalSales = orderRes.data.list
          .filter(o => o.status !== 'cancelled')
          .reduce((sum, o) => sum + parseFloat(o.totalAmount), 0).toFixed(2)

        this.$nextTick(() => {
          this.initPieChart(orderRes.data.list)
          this.initBarChart(orderRes.data.list)
        })
      } catch (e) { }
    },
    initPieChart(orders) {
      if (!this.$refs.pieChart) return
      const chart = echarts.init(this.$refs.pieChart)
      // 按状态统计数量
      const statusMap = {}
      orders.forEach(o => {
        const label = { pending: '待付款', paid: '已付款', shipped: '已发货', completed: '已完成', cancelled: '已取消' }[o.status] || o.status
        statusMap[label] = (statusMap[label] || 0) + 1
      })
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['40%', '70%'],
          data: Object.entries(statusMap).map(([name, value]) => ({ name, value })),
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
        }]
      })
    },
    initBarChart(orders) {
      if (!this.$refs.barChart) return
      const chart = echarts.init(this.$refs.barChart)
      // 按日期统计销售额
      const dayMap = {}
      orders.filter(o => o.status !== 'cancelled').forEach(o => {
        const day = o.createTime ? o.createTime.substring(0, 10) : '未知'
        dayMap[day] = (dayMap[day] || 0) + parseFloat(o.totalAmount)
      })
      const days = Object.keys(dayMap).sort().slice(-14)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: days, axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', name: '销售额(元)' },
        series: [{ type: 'bar', data: days.map(d => dayMap[d]), itemStyle: { color: '#409EFF' } }]
      })
    }
  }
}
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-num { font-size: 32px; font-weight: bold; color: #409EFF; }
.stat-label { margin-top: 5px; color: #999; }
</style>
