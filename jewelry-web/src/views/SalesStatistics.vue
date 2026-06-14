<template>
  <div class="space-y-6">
    <h2 class="text-lg font-bold" style="color:#DEE4EA">销售统计</h2>

    <!-- Stat cards -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="(s,i) in statCards" :key="i" class="rounded-2xl p-5 flex items-center gap-4" style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.06)">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg" :style="{background:s.bg, color:s.color}">{{ s.icon }}</div>
        <div><div class="text-2xl font-bold" :style="{color:s.color}">{{ s.value }}</div><div class="text-xs" style="color:#596773">{{ s.label }}</div></div>
      </div>
    </div>

    <!-- Charts -->
    <div class="flex gap-4">
      <div class="flex-1 rounded-2xl p-5" style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.06)">
        <div class="text-sm font-semibold mb-4" style="color:#DEE4EA">订单状态分布</div>
        <div ref="pieChart" style="height:320px" />
      </div>
      <div class="flex-1 rounded-2xl p-5" style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.06)">
        <div class="text-sm font-semibold mb-4" style="color:#DEE4EA">近14天销售额趋势</div>
        <div ref="barChart" style="height:320px" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'
import * as echarts from 'echarts'

const pieChart = ref(null); const barChart = ref(null)
const statCards = reactive([
  { label:'商品总数', value:0, color:'#60a5fa', bg:'rgba(96,165,250,0.15)', icon:'📦' },
  { label:'订单总数', value:0, color:'#f59e0b', bg:'rgba(245,158,11,0.15)', icon:'📋' },
  { label:'客户总数', value:0, color:'#34d399', bg:'rgba(52,211,153,0.15)', icon:'👥' },
  { label:'销售总额', value:'¥0', color:'#f472b6', bg:'rgba(244,114,182,0.15)', icon:'💰' }
])

onMounted(async()=>{
  try{
    const [p,c,o]=await Promise.all([getAllProducts(),getAllCustomers(),getOrderList({page:1,limit:1000})])
    statCards[0].value=p.data.length;statCards[1].value=o.data.total;statCards[2].value=c.data.length
    const orders=o.data.list||[]
    statCards[3].value='¥'+orders.filter(x=>x.status!=='cancelled').reduce((s,x)=>s+parseFloat(x.totalAmount||0),0).toFixed(2)
    await nextTick();initPie(orders);initBar(orders)
  }catch{}
})

function initPie(orders) {
  if(!pieChart.value)return;const c=echarts.init(pieChart.value);const m={}
  orders.forEach(o=>{const l={pending:'待付款',paid:'已付款',shipped:'已发货',completed:'已完成',cancelled:'已取消'}[o.status]||o.status;m[l]=(m[l]||0)+1})
  c.setOption({
    tooltip:{trigger:'item'},legend:{bottom:0,textStyle:{color:'#94a3b8'}},
    series:[{type:'pie',radius:['45%','75%'],center:['50%','45%'],avoidLabelOverlap:false,
      itemStyle:{borderColor:'#101214',borderWidth:2},
      label:{color:'#94a3b8'},data:Object.entries(m).map(([n,v])=>({name:n,value:v}))}]
  })
}

function initBar(orders) {
  if(!barChart.value)return;const c=echarts.init(barChart.value);const m={}
  orders.filter(o=>o.status!=='cancelled').forEach(o=>{const d=(o.createTime||'').substring(0,10);if(d)m[d]=(m[d]||0)+parseFloat(o.totalAmount||0)})
  const days=Object.keys(m).sort().slice(-14)
  c.setOption({
    tooltip:{trigger:'axis'},grid:{left:50,right:10,top:10,bottom:20},
    xAxis:{type:'category',data:days,axisLabel:{color:'#596773',fontSize:10,rotate:30},axisLine:{lineStyle:{color:'#2C333A'}}},
    yAxis:{type:'value',name:'元',nameTextStyle:{color:'#596773'},axisLabel:{color:'#596773'},splitLine:{lineStyle:{color:'#2C333A'}}},
    series:[{type:'bar',data:days.map(d=>m[d]),itemStyle:{color:'#a855f7',borderRadius:[4,4,0,0]},barMaxWidth:30}]
  })
}
</script>
