<template>
  <div class="container-page space-y-5">
    <div class="grid grid-cols-4 gap-5">
      <div v-for="s in cards" :key="s.label" class="bg-white rounded-xl p-6 text-center"><div class="text-3xl font-bold" :style="{color:s.color}">{{ s.value }}</div><div class="text-gray-400 mt-1">{{ s.label }}</div></div>
    </div>
    <div class="flex gap-5">
      <div class="flex-1 bg-white rounded-xl p-5"><div class="font-semibold mb-3">订单状态分布</div><div ref="pieChart" style="height:320px" /></div>
      <div class="flex-1 bg-white rounded-xl p-5"><div class="font-semibold mb-3">最近订单统计</div><div ref="barChart" style="height:320px" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getOrderList, getAllProducts, getAllCustomers } from '@/api'
import * as echarts from 'echarts'

const pieChart = ref(null); const barChart = ref(null)
const cards = reactive([{ label: '商品总数', value: 0, color: '#409EFF' },{ label: '订单总数', value: 0, color: '#fa8c16' },{ label: '客户总数', value: 0, color: '#52c41a' },{ label: '销售总额', value: '¥0', color: '#f5222d' }])

onMounted(async () => {
  try {
    const [p,c,o] = await Promise.all([getAllProducts(), getAllCustomers(), getOrderList({page:1,limit:1000})])
    cards[0].value = p.data.length; cards[1].value = o.data.total; cards[2].value = c.data.length
    cards[3].value = '¥' + (o.data.list||[]).filter(x=>x.status!=='cancelled').reduce((s,x)=>s+parseFloat(x.totalAmount||0),0).toFixed(2)
    await nextTick()
    initPie(o.data.list||[]); initBar(o.data.list||[])
  } catch {}
})

function initPie(orders) {
  if (!pieChart.value) return; const c = echarts.init(pieChart.value); const m = {}
  orders.forEach(o => { const l = {pending:'待付款',paid:'已付款',shipped:'已发货',completed:'已完成',cancelled:'已取消'}[o.status]||o.status; m[l]=(m[l]||0)+1 })
  c.setOption({ tooltip:{trigger:'item'}, legend:{bottom:0}, series:[{ type:'pie', radius:['40%','70%'], data:Object.entries(m).map(([n,v])=>({name:n,value:v})) }] })
}
function initBar(orders) {
  if (!barChart.value) return; const c = echarts.init(barChart.value); const m = {}
  orders.filter(o=>o.status!=='cancelled').forEach(o => { const d=(o.createTime||'').substring(0,10); if(d) m[d]=(m[d]||0)+parseFloat(o.totalAmount||0) })
  const days = Object.keys(m).sort().slice(-14)
  c.setOption({ tooltip:{trigger:'axis'}, xAxis:{type:'category',data:days,axisLabel:{rotate:30}}, yAxis:{type:'value',name:'元'}, series:[{ type:'bar',data:days.map(d=>m[d]),itemStyle:{color:'#409EFF'} }] })
}
</script>
