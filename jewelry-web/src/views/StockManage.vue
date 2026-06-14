<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:#DEE4EA">库存管理</h2>
      <div class="flex gap-2">
        <button @click="handleIn" class="px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:rgba(52,211,153,0.15); color:#34d399">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="inline mr-1"><path d="M12 5v14"/><path d="M5 12h14"/></svg>入库
        </button>
        <button @click="handleOut" class="px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:rgba(245,158,11,0.15); color:#f59e0b">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="inline mr-1"><path d="M5 12h14"/></svg>出库
        </button>
      </div>
    </div>

    <!-- Low stock warning -->
    <div v-if="lowStock.length" class="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm" style="background:rgba(245,158,11,0.1); border:1px solid rgba(245,158,11,0.2); color:#f59e0b">
      ⚠️ 库存预警：{{ lowStock.map(p=>p.name+'('+p.stock+')').join('、') }}
    </div>

    <div class="space-y-2">
      <div v-for="r in tableData" :key="r.id" class="flex items-center gap-4 px-4 py-3 rounded-xl" style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.04)">
        <span class="text-[10px] px-2 py-0.5 rounded-full font-medium w-12 text-center flex-shrink-0" :style="r.type==='in'?{background:'rgba(52,211,153,0.15)',color:'#34d399'}:{background:'rgba(245,158,11,0.15)',color:'#f59e0b'}">{{ r.type==='in'?'入库':'出库' }}</span>
        <div class="flex-1 min-w-0"><span class="text-sm" style="color:#DEE4EA">{{ r.productName }}</span><span class="text-xs ml-2" style="color:#596773">{{ r.remark || '' }}</span></div>
        <span class="text-sm font-bold" :style="{color:r.type==='in'?'#34d399':'#f59e0b'}">{{ r.type==='in'?'+':'-' }}{{ r.quantity }}</span>
        <span class="text-xs flex-shrink-0" style="color:#596773">{{ r.createTime?.substring(0,16) }}</span>
      </div>
    </div>

    <div class="flex justify-center pt-4"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,prev,pager,next" small @change="loadData" /></div>

    <el-dialog v-model="dialogVisible" :title="stockType==='in'?'入库':'出库'" width="420px">
      <el-form :model="sForm" ref="sFormRef" label-width="80px">
        <el-form-item label="商品"><el-select v-model="sForm.productId" filterable class="w-full"><el-option v-for="p in products" :key="p.id" :label="p.name+' (库存:'+p.stock+')'" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="sForm.quantity" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="sForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitStock" :style="{background:stockType==='in'?'linear-gradient(135deg,#34d399,#10b981)':'linear-gradient(135deg,#f59e0b,#ef4444)', border:'none'}">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStockList, stockIn, stockOut, getAllProducts } from '@/api'

const tableData = ref([]); const total = ref(0); const products = ref([]); const lowStock = ref([])
const dialogVisible = ref(false); const stockType = ref('in')
const search = reactive({ page:1, limit:10 })
const sForm = reactive({ productId:null, quantity:1, remark:'' }); const sFormRef = ref(null)

onMounted(()=>{loadData();loadProducts()})
async function loadData(){try{const r=await getStockList(search);tableData.value=r.data.list;total.value=r.data.total}catch{}}
async function loadProducts(){try{const r=await getAllProducts();products.value=r.data;lowStock.value=r.data.filter(p=>p.stock<10)}catch{}}
function handleIn(){stockType.value='in';Object.assign(sForm,{productId:null,quantity:1,remark:''});dialogVisible.value=true}
function handleOut(){stockType.value='out';Object.assign(sForm,{productId:null,quantity:1,remark:''});dialogVisible.value=true}
async function submitStock(){if(!sForm.productId){ElMessage.warning('请选择商品');return};await(stockType.value==='in'?stockIn({...sForm}):stockOut({...sForm}));ElMessage.success(stockType.value==='in'?'入库成功':'出库成功');dialogVisible.value=false;loadData();loadProducts()}
</script>
