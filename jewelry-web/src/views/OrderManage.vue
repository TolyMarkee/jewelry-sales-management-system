<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:#DEE4EA">订单管理</h2>
      <button @click="handleAdd" class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:linear-gradient(135deg,#a855f7,#6366f1); color:#fff">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>创建订单
      </button>
    </div>

    <!-- Search -->
    <div class="flex gap-3 flex-wrap">
      <input v-model="search.orderNo" placeholder="订单编号..." class="h-9 px-3 rounded-lg text-sm outline-none w-48" style="background:#101214; border:1px solid #2C333A; color:#DEE4EA" @keyup.enter="loadData" />
      <select v-model="search.status" @change="loadData" class="h-9 px-3 rounded-lg text-sm outline-none cursor-pointer" style="background:#101214; border:1px solid #2C333A; color:#94a3b8">
        <option value="">全部状态</option><option v-for="o in statusOpts" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
    </div>

    <!-- Order cards -->
    <div class="space-y-2">
      <div v-for="o in tableData" :key="o.id" class="group rounded-xl p-4 transition-all hover:translate-x-1" style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.04)">
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-3">
            <span class="text-sm font-mono" style="color:#DEE4EA">{{ o.orderNo }}</span>
            <span class="text-[10px] px-2 py-0.5 rounded-full" :style="{background:statusColor(o.status)+'20', color:statusColor(o.status)}">{{ statusLabel(o.status) }}</span>
          </div>
          <span class="text-sm font-bold" style="color:#34d399">¥{{ o.totalAmount }}</span>
        </div>
        <div class="flex items-center gap-4 text-xs" style="color:#596773">
          <span>👤 {{ o.customerName }}</span><span>💼 {{ o.userName }}</span><span>🕐 {{ o.createTime?.substring(0,16) }}</span>
        </div>
        <!-- Status flow -->
        <div class="flex items-center gap-2 mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
          <button v-if="o.status==='pending'" @click="updateStatus(o.id,'paid')" class="text-[10px] px-2 py-1 rounded-lg transition-all hover:scale-105" style="background:rgba(96,165,250,0.15); color:#60a5fa">标记付款</button>
          <button v-if="o.status==='paid'" @click="updateStatus(o.id,'shipped')" class="text-[10px] px-2 py-1 rounded-lg transition-all hover:scale-105" style="background:rgba(168,85,247,0.15); color:#a855f7">标记发货</button>
          <button v-if="o.status==='shipped'" @click="updateStatus(o.id,'completed')" class="text-[10px] px-2 py-1 rounded-lg transition-all hover:scale-105" style="background:rgba(52,211,153,0.15); color:#34d399">标记完成</button>
          <button @click="showDetail(o)" class="text-[10px] px-2 py-1 rounded-lg transition-all hover:scale-105" style="background:rgba(148,163,184,0.1); color:#94a3b8">详情</button>
          <button @click="handleDelete(o)" class="text-[10px] px-2 py-1 rounded-lg transition-all hover:scale-105 ml-auto" style="color:#ef4444">删除</button>
        </div>
      </div>
    </div>

    <div class="flex justify-center pt-4">
      <el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,prev,pager,next" small @change="loadData" />
    </div>

    <!-- Create dialog -->
    <el-dialog v-model="dialogVisible" title="创建订单" width="700px" @closed="resetOrder">
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="客户"><el-select v-model="orderForm.customerId" filterable class="w-full"><el-option v-for="c in custList" :key="c.id" :label="c.name+' - '+c.phone" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="orderForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div class="text-sm font-semibold mb-2" style="color:#DEE4EA">订单明细</div>
      <div v-for="(item,i) in orderForm.items" :key="i" class="flex items-center gap-2 mb-2">
        <el-select v-model="item.productId" filterable size="small" class="flex-1" @change="onProdChange(item)"><el-option v-for="p in prodList" :key="p.id" :label="p.name+' (¥'+p.price+')'" :value="p.id" /></el-select>
        <span class="text-xs w-16" style="color:#34d399">¥{{ item.price }}</span>
        <el-input-number v-model="item.quantity" :min="1" size="small" class="w-20" />
        <span class="text-xs w-20" style="color:#f59e0b">小计 ¥{{ (item.price*item.quantity).toFixed(2) }}</span>
        <button @click="removeItem(i)" class="text-xs" style="color:#ef4444">✕</button>
      </div>
      <button @click="orderForm.items.push({productId:null,price:0,quantity:1})" class="text-xs mt-2 flex items-center gap-1 transition-all hover:scale-105" style="color:#a855f7">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>添加商品
      </button>
      <div class="text-right mt-4 text-lg font-bold" style="color:#34d399">总额：¥{{ totalPrice }}</div>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitOrder" style="background:linear-gradient(135deg,#a855f7,#6366f1); border:none">提交订单</el-button></template>
    </el-dialog>

    <!-- Detail dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <template v-if="curOrder">
        <div class="space-y-2 text-sm" style="color:#94a3b8">
          <p>订单编号：<span style="color:#DEE4EA">{{ curOrder.orderNo }}</span></p>
          <p>客户：<span style="color:#DEE4EA">{{ curOrder.customerName }}</span></p>
          <p>销售员：<span style="color:#DEE4EA">{{ curOrder.userName }}</span></p>
          <p>状态：<span :style="{color:statusColor(curOrder.status)}">{{ statusLabel(curOrder.status) }}</span></p>
        </div>
        <div class="mt-4 space-y-1">
          <div v-for="item in curOrder.items" :key="item.id" class="flex justify-between text-sm py-1 border-b" style="border-color:rgba(255,255,255,0.04)">
            <span style="color:#DEE4EA">{{ item.productName }}</span>
            <span style="color:#596773">x{{ item.quantity }} · ¥{{ item.price }}</span>
          </div>
        </div>
        <div class="text-right mt-3 text-lg font-bold" style="color:#34d399">¥{{ curOrder.totalAmount }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, saveOrder, updateOrderStatus, deleteOrder, getAllCustomers, getAllProducts } from '@/api'

const tableData = ref([]); const total = ref(0); const dialogVisible = ref(false); const detailVisible = ref(false)
const custList = ref([]); const prodList = ref([]); const curOrder = ref(null)
const search = reactive({ orderNo:'', status:'', page:1, limit:10 })
const orderForm = reactive({ customerId:null, remark:'', items:[{productId:null,price:0,quantity:1}] })
const totalPrice = computed(()=>orderForm.items.reduce((s,i)=>s+i.price*i.quantity,0).toFixed(2))
const statusOpts = [{label:'待付款',value:'pending'},{label:'已付款',value:'paid'},{label:'已发货',value:'shipped'},{label:'已完成',value:'completed'}]
function statusLabel(s){const m={pending:'待付款',paid:'已付款',shipped:'已发货',completed:'已完成',cancelled:'已取消'};return m[s]||s}
function statusColor(s){const m={pending:'#f59e0b',paid:'#60a5fa',shipped:'#a855f7',completed:'#34d399',cancelled:'#ef4444'};return m[s]||'#94a3b8'}

onMounted(()=>loadData())
async function loadData(){try{const r=await getOrderList(search);tableData.value=r.data.list;total.value=r.data.total}catch{}}
async function handleAdd(){resetOrder();const[c,p]=await Promise.all([getAllCustomers(),getAllProducts()]);custList.value=c.data;prodList.value=p.data;dialogVisible.value=true}
function resetOrder(){Object.assign(orderForm,{customerId:null,remark:'',items:[{productId:null,price:0,quantity:1}]})}
function onProdChange(item){const p=prodList.value.find(x=>x.id===item.productId);if(p)item.price=p.price}
function removeItem(i){if(orderForm.items.length>1)orderForm.items.splice(i,1)}
async function submitOrder(){if(!orderForm.customerId){ElMessage.warning('请选择客户');return};if(orderForm.items.some(i=>!i.productId)){ElMessage.warning('请选择商品');return};const data={customerId:orderForm.customerId,remark:orderForm.remark,totalAmount:parseFloat(totalPrice.value),items:orderForm.items.map(i=>({productId:i.productId,quantity:i.quantity,price:i.price}))};await saveOrder(data);ElMessage.success('下单成功');dialogVisible.value=false;loadData()}
async function updateStatus(id,status){await updateOrderStatus({id,status});ElMessage.success('更新成功');loadData()}
function showDetail(row){curOrder.value=row;detailVisible.value=true}
async function handleDelete(row){await ElMessageBox.confirm('确定删除？','提示',{type:'warning'});await deleteOrder(row.id);ElMessage.success('已删除');loadData()}
</script>
