<template>
  <div class="space-y-6">
    <!-- Welcome + Weather -->
    <div class="flex gap-4">
      <div class="flex-1 rounded-2xl p-6 relative overflow-hidden border" style="background:var(--surface); border-color:var(--border)">
        <div class="relative z-10">
          <h2 class="text-xl font-extrabold" style="color:var(--text-primary)">欢迎回来，{{ userStore.user?.realName || userStore.user?.username }}</h2>
          <p class="text-sm mt-1 font-medium" style="color:var(--text-secondary)">{{ today }} · {{ greeting }}</p>
        </div>
        <div class="absolute right-4 top-1/2 -translate-y-1/2 text-6xl opacity-5" style="color:var(--accent)">✦</div>
      </div>
      <div class="w-60 rounded-2xl p-5 flex items-center gap-4 border" style="background:var(--surface); border-color:var(--border)">
        <span class="text-3xl">{{ weatherIcon }}</span>
        <div>
          <div class="text-xs" style="color:var(--text-muted)">{{ weather.city || '获取天气...' }}</div>
          <div class="text-2xl font-bold" style="color:var(--text-primary)">{{ weather.temp || '--' }}°</div>
        </div>
      </div>
    </div>

    <!-- Stat Cards -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="s in statCards" :key="s.label"
        class="group rounded-2xl p-5 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl relative overflow-hidden border"
        style="background:var(--surface); border-color:var(--border); box-shadow:var(--card-shadow)"
        @click="s.link && router.push(s.link)"
      >
        <div class="flex items-start justify-between relative z-10">
          <div>
            <div class="text-xs mb-1 font-medium" style="color:var(--text-secondary)">{{ s.label }}</div>
            <div class="text-3xl font-extrabold tracking-tight" style="color:var(--text-primary)">{{ s.value }}</div>
          </div>
          <div class="w-11 h-11 rounded-xl flex items-center justify-center text-lg shadow-sm" :style="{background:`${s.color}18`, color:s.color}">
            <component :is="s.icon" class="w-5 h-5" />
          </div>
        </div>
      </div>
    </div>

    <!-- Charts + Stock -->
    <div class="flex gap-4">
      <div class="flex-[2] rounded-2xl p-5 border" style="background:var(--surface); border-color:var(--border); box-shadow:var(--card-shadow)">
        <div class="text-sm font-semibold mb-4" style="color:var(--text-primary)">近14天销售额趋势</div>
        <div ref="barChart" style="height:260px" />
      </div>
      <div class="flex-1 rounded-2xl p-5 border" style="background:var(--surface); border-color:var(--border); box-shadow:var(--card-shadow)">
        <div class="text-sm font-semibold mb-3" style="color:var(--text-primary)">库存预警</div>
        <div v-if="lowStock.length===0" class="text-center py-8"><div class="text-4xl mb-2">✅</div><div class="text-xs" style="color:var(--text-muted)">库存充足，无需补货</div></div>
        <div v-else class="space-y-2">
          <div v-for="p in lowStock" :key="p.id" class="flex items-center justify-between py-2 border-b border-white/[0.04]">
            <span class="text-sm truncate" style="color:var(--text-primary)">{{ p.name }}</span>
            <span class="text-xs px-2 py-0.5 rounded-full" :style="{background:p.stock===0?'rgba(239,68,68,0.2)':'rgba(250,204,21,0.2)', color:p.stock===0?'#ef4444':'#facc15'}">{{ p.stock }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'
import { Goods, Tickets, User, Coin } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const barChart = ref(null)

const today = new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' })
const greeting = computed(() => { const h=new Date().getHours(); if(h<6)return'夜深了'; if(h<9)return'早上好'; if(h<12)return'上午好'; if(h<14)return'中午好'; if(h<18)return'下午好'; return'晚上好' })

const stats = reactive({ products:0, orders:0, customers:0, sales:0 })
const lowStock = ref([])
const weather = reactive({ city:'', temp:'', desc:'' })

const statCards = computed(() => [
  { label:'商品总数', value:stats.products, icon:Goods, color:'#60a5fa', glow:'#60a5fa', link:'/product' },
  { label:'订单总数', value:stats.orders, icon:Tickets, color:'#f59e0b', glow:'#f59e0b', link:'/order' },
  { label:'客户总数', value:stats.customers, icon:User, color:'#34d399', glow:'#34d399', link:'/customer' },
  { label:'销售总额', value:'¥'+stats.sales, icon:Coin, color:'#f472b6', glow:'#f472b6', link:'' }
])

const weatherIcon = computed(() => { const d=weather.desc; if(!d)return'🌤️'; if(d.includes('晴'))return'☀️'; if(d.includes('云')||d.includes('阴'))return'⛅'; if(d.includes('雨'))return'🌧️'; return'🌤️' })

onMounted(async () => {
  try {
    const [p,c,o] = await Promise.all([getAllProducts(), getAllCustomers(), getOrderList({page:1,limit:1000})])
    stats.products = p.data.length; stats.customers = c.data.length
    lowStock.value = p.data.filter(x=>x.stock<10)
    const orders = o.data.list||[]; stats.orders = o.data.total||orders.length
    stats.sales = orders.filter(x=>x.status!=='cancelled').reduce((s,x)=>s+parseFloat(x.totalAmount||0),0).toFixed(2)
    await nextTick(); initChart(orders); fetchWeather()
  } catch {}
})

function initChart(orders) {
  if (!barChart.value) return; const c = echarts.init(barChart.value)
  const m = {}; orders.filter(o=>o.status!=='cancelled').forEach(o=>{const d=(o.createTime||'').substring(0,10); if(d) m[d]=(m[d]||0)+parseFloat(o.totalAmount||0)})
  const days = Object.keys(m).sort().slice(-14)
  c.setOption({
    tooltip:{trigger:'axis'}, grid:{left:50,right:10,top:10,bottom:20},
    xAxis:{type:'category',data:days,axisLabel:{color:'#596773',fontSize:10,rotate:30},axisLine:{lineStyle:{color:'#2C333A'}}},
    yAxis:{type:'value',name:'元',nameTextStyle:{color:'#596773'},axisLabel:{color:'#596773'},splitLine:{lineStyle:{color:'#2C333A'}}},
    series:[{type:'bar',data:days.map(d=>m[d]),itemStyle:{color:'#a855f7',borderRadius:[4,4,0,0]},barMaxWidth:30}]
  })
}

async function fetchWeather() {
  try { const r=await fetch('https://restapi.amap.com/v3/ip?key=YOUR_AMAP_KEY'); if(!r.ok)return; const d=await r.json(); const w=await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=YOUR_AMAP_KEY&city=${d.adcode||'110000'}&extensions=base`); const j=await w.json(); if(j.status==='1'&&j.lives){const l=j.lives[0]; Object.assign(weather,{city:l.city,temp:l.temperature,desc:l.weather})} } catch {}
}
</script>
