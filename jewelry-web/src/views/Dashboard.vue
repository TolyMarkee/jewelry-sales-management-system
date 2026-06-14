<template>
  <div class="p-5 space-y-5">
    <!-- Top Row -->
    <div class="flex gap-5">
      <div class="flex-1 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl p-6 text-white">
        <h2 class="text-xl font-bold">欢迎回来，{{ userStore.user?.realName || userStore.user?.username }}</h2>
        <p class="text-white/80 text-sm mt-1">{{ today }} | {{ greeting }}</p>
      </div>
      <div class="w-64 bg-white rounded-xl p-4 flex items-center gap-3">
        <span class="text-3xl">{{ weatherIcon }}</span>
        <div>
          <div class="text-sm text-gray-500">{{ weather.city || '加载中...' }}</div>
          <div class="text-2xl font-bold">{{ weather.temp || '--' }}°C</div>
          <div class="text-xs text-gray-400">{{ weather.desc || '' }}</div>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-4 gap-5">
      <div v-for="s in statsCards" :key="s.label" class="bg-white rounded-xl p-5 flex items-center gap-4 cursor-pointer hover:-translate-y-0.5 transition-shadow hover:shadow-lg" @click="router.push(s.link)">
        <div class="w-14 h-14 rounded-xl flex items-center justify-center text-2xl" :style="{ background: s.bg, color: s.color }"><el-icon :size="26"><component :is="s.icon" /></el-icon></div>
        <div><div class="text-2xl font-bold">{{ s.value }}</div><div class="text-gray-400 text-sm">{{ s.label }}</div></div>
      </div>
    </div>

    <!-- Charts + Stock -->
    <div class="flex gap-5">
      <div class="flex-[2] bg-white rounded-xl p-5"><div class="font-semibold mb-3">近14天销售额趋势</div><div ref="barChart" style="height:280px" /></div>
      <div class="flex-1 bg-white rounded-xl p-5">
        <div class="font-semibold mb-3">库存预警</div>
        <div v-if="lowStock.length === 0" class="text-center py-10 text-green-500"><el-icon :size="40"><SuccessFilled /></el-icon><p class="mt-2">库存充足</p></div>
        <el-table v-else :data="lowStock" size="small" max-height="270">
          <el-table-column prop="name" label="商品" />
          <el-table-column prop="stock" label="库存" width="60">
            <template #default="s"><el-tag :type="s.row.stock === 0 ? 'danger' : 'warning'" size="small">{{ s.row.stock }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'
import { Goods, Tickets, User, Coin, SuccessFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'; if (h < 9) return '早上好'; if (h < 12) return '上午好'
  if (h < 14) return '中午好'; if (h < 18) return '下午好'; return '晚上好'
})

const stats = reactive({ products: 0, orders: 0, customers: 0, sales: 0 })
const lowStock = ref([])
const weather = reactive({ city: '', temp: '', desc: '' })

const statsCards = computed(() => [
  { label: '商品总数', value: stats.products, icon: Goods, bg: '#e6f7ff', color: '#1890ff', link: '/product' },
  { label: '订单总数', value: stats.orders, icon: Tickets, bg: '#fff7e6', color: '#fa8c16', link: '/order' },
  { label: '客户总数', value: stats.customers, icon: User, bg: '#f6ffed', color: '#52c41a', link: '/customer' },
  { label: '销售总额', value: '¥' + stats.sales, icon: Coin, bg: '#fff1f0', color: '#f5222d', link: '' }
])

const weatherIcon = computed(() => {
  const d = weather.desc; if (!d) return '🌤️'
  if (d.includes('晴')) return '☀️'; if (d.includes('云')||d.includes('阴')) return '⛅'
  if (d.includes('雨')) return '🌧️'; if (d.includes('雪')) return '❄️'; return '🌤️'
})

const barChart = ref(null)

onMounted(async () => {
  try {
    const [p, c, o] = await Promise.all([getAllProducts(), getAllCustomers(), getOrderList({ page: 1, limit: 1000 })])
    stats.products = p.data.length
    stats.customers = c.data.length
    lowStock.value = p.data.filter(x => x.stock < 10)
    const orders = o.data.list || []
    stats.orders = o.data.total || orders.length
    stats.sales = orders.filter(x => x.status !== 'cancelled').reduce((s, x) => s + parseFloat(x.totalAmount || 0), 0).toFixed(2)
    await nextTick()
    initChart(orders)
    fetchWeather()
  } catch {}
})

function initChart(orders) {
  if (!barChart.value) return
  const chart = echarts.init(barChart.value)
  const map = {}
  orders.filter(o => o.status !== 'cancelled').forEach(o => {
    const d = (o.createTime || '').substring(0, 10)
    if (d) map[d] = (map[d] || 0) + parseFloat(o.totalAmount || 0)
  })
  const days = Object.keys(map).sort().slice(-14)
  chart.setOption({
    tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 10, bottom: 20 },
    xAxis: { type: 'category', data: days, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: '元' },
    series: [{ type: 'bar', data: days.map(d => map[d]), itemStyle: { color: '#409EFF', borderRadius: [4,4,0,0] }, barMaxWidth: 30 }]
  })
}

async function fetchWeather() {
  try {
    const ipRes = await fetch('https://restapi.amap.com/v3/ip?key=YOUR_AMAP_KEY')
    if (!ipRes.ok) return
    const ipData = await ipRes.json()
    const wxRes = await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=YOUR_AMAP_KEY&city=${ipData.adcode || '110000'}&extensions=base`)
    const wxData = await wxRes.json()
    if (wxData.status === '1' && wxData.lives) {
      const l = wxData.lives[0]
      Object.assign(weather, { city: l.city, temp: l.temperature, desc: l.weather })
    }
  } catch {}
}
</script>
