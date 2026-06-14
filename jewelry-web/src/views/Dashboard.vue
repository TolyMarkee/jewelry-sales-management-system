<template>
  <div class="dashboard">
    <!-- 顶部：欢迎 + 天气 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="welcome-card">
          <div class="welcome">
            <h2>欢迎回来，{{ user.realName || user.username }}</h2>
            <p>{{ currentDate }} | {{ greeting }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="weather-card">
          <div v-if="weather.loading" style="text-align:center;padding:10px">
            <i class="el-icon-loading" /> 加载天气...
          </div>
          <div v-else-if="weather.city" class="weather-info">
            <div class="weather-left">
              <div class="weather-city">{{ weather.city }}</div>
              <div class="weather-temp">{{ weather.temperature }}°C</div>
              <div class="weather-desc">{{ weather.weather }}</div>
            </div>
            <div class="weather-right">
              <div class="weather-icon">{{ weatherIcon }}</div>
            </div>
          </div>
          <div v-else style="text-align:center;padding:10px;color:#999">
            请在代码中配置高德地图 API Key
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click.native="$router.push('/product')">
          <div class="stat-icon" style="background:#e6f7ff"><i class="el-icon-goods" style="color:#1890ff" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.totalProducts }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click.native="$router.push('/order')">
          <div class="stat-icon" style="background:#fff7e6"><i class="el-icon-s-order" style="color:#fa8c16" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.totalOrders }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click.native="$router.push('/customer')">
          <div class="stat-icon" style="background:#f6ffed"><i class="el-icon-user" style="color:#52c41a" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ stats.totalCustomers }}</div>
            <div class="stat-label">客户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background:#fff1f0"><i class="el-icon-money" style="color:#f5222d" /></div>
          <div class="stat-body">
            <div class="stat-num sales">¥{{ stats.totalSales }}</div>
            <div class="stat-label">销售总额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 + 低库存预警 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="14">
        <el-card>
          <div slot="header"><span>近14天销售额趋势</span></div>
          <div ref="barChart" style="height:300px" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <div slot="header"><span>库存预警</span></div>
          <el-table :data="lowStockProducts" size="small" style="width:100%" max-height="290">
            <el-table-column prop="name" label="商品" />
            <el-table-column prop="stock" label="库存" width="60">
              <template slot-scope="s">
                <el-tag :type="s.row.stock === 0 ? 'danger' : 'warning'" size="mini">{{ s.row.stock }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template slot-scope="s">
                <el-button type="text" size="mini" @click="$router.push('/stock')">补货</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="lowStockProducts.length === 0" style="text-align:center;padding:30px;color:#52c41a">
            <i class="el-icon-success" style="font-size:30px" /><p>库存充足，无需补货</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: { totalProducts: 0, totalOrders: 0, totalCustomers: 0, totalSales: 0 },
      lowStockProducts: [],
      weather: { loading: false, city: '', temperature: '', weather: '', humidity: '' }
    }
  },
  computed: {
    ...mapState(['user']),
    currentDate() {
      return new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了，注意休息'
      if (h < 9) return '早上好！新的一天开始了'
      if (h < 12) return '上午好！工作顺利'
      if (h < 14) return '中午好，别忘了休息'
      if (h < 18) return '下午好！'
      return '晚上好！'
    },
    weatherIcon() {
      const w = this.weather.weather
      if (w.includes('晴')) return '☀️'
      if (w.includes('云') || w.includes('阴')) return '⛅'
      if (w.includes('雨')) return '🌧️'
      if (w.includes('雪')) return '❄️'
      if (w.includes('风')) return '💨'
      return '🌤️'
    }
  },
  mounted() {
    this.loadStats()
    this.fetchWeather()
  },
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
        this.lowStockProducts = prodRes.data.filter(p => p.stock < 10)

        const orders = orderRes.data.list || []
        this.stats.totalOrders = orderRes.data.total || orders.length
        this.stats.totalSales = orders
          .filter(o => o.status !== 'cancelled')
          .reduce((sum, o) => sum + parseFloat(o.totalAmount || 0), 0).toFixed(2)

        this.$nextTick(() => this.initBarChart(orders))
      } catch (e) { /* 错误由拦截器处理 */ }
    },
    initBarChart(orders) {
      if (!this.$refs.barChart) return
      const chart = echarts.init(this.$refs.barChart)
      const dayMap = {}
      orders.filter(o => o.status !== 'cancelled').forEach(o => {
        const day = o.createTime ? o.createTime.substring(0, 10) : ''
        if (day) dayMap[day] = (dayMap[day] || 0) + parseFloat(o.totalAmount || 0)
      })
      const days = Object.keys(dayMap).sort().slice(-14)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 20, bottom: 30 },
        xAxis: { type: 'category', data: days, axisLabel: { rotate: 30, fontSize: 11 } },
        yAxis: { type: 'value', name: '元' },
        series: [{
          type: 'bar', data: days.map(d => dayMap[d]),
          itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
          barMaxWidth: 30
        }]
      })
    },
    async fetchWeather() {
      // 高德地图天气API - 请在下方填入你的 API Key
      const amapKey = 'YOUR_AMAP_KEY'  // <-- 改成你的高德地图 Key
      if (amapKey === 'YOUR_AMAP_KEY') return

      this.weather.loading = true
      try {
        // 先通过IP定位获取城市adcode
        const ipRes = await fetch(`https://restapi.amap.com/v3/ip?key=${amapKey}`)
        const ipData = await ipRes.json()
        const adcode = ipData.adcode || '110000'

        // 获取天气
        const weatherRes = await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=${amapKey}&city=${adcode}&extensions=base`)
        const weatherData = await weatherRes.json()
        if (weatherData.status === '1' && weatherData.lives) {
          const live = weatherData.lives[0]
          this.weather.city = live.city
          this.weather.temperature = live.temperature
          this.weather.weather = live.weather
          this.weather.humidity = live.humidity
        }
      } catch (e) {
        console.error('天气获取失败:', e)
      } finally {
        this.weather.loading = false
      }
    }
  }
}
</script>

<style scoped>
.dashboard { padding: 0; }

.welcome-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.welcome-card h2 { margin: 0 0 5px; font-size: 22px; }
.welcome-card p { margin: 0; opacity: 0.9; }

.weather-card { min-height: 90px; }
.weather-info { display: flex; align-items: center; justify-content: space-between; }
.weather-city { font-size: 14px; color: #666; }
.weather-temp { font-size: 28px; font-weight: bold; color: #333; }
.weather-desc { font-size: 13px; color: #999; margin-top: 2px; }
.weather-icon { font-size: 40px; }

.stat-card { display: flex; align-items: center; cursor: pointer; }
.stat-card:hover { transform: translateY(-2px); transition: .3s; }
.stat-icon { width: 55px; height: 55px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; margin-right: 15px; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-num { font-size: 26px; font-weight: bold; color: #333; }
.stat-num.sales { font-size: 22px; }
.stat-label { color: #999; font-size: 13px; margin-top: 2px; }
</style>
