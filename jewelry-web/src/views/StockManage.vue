<template>
  <div class="container-page">
    <div class="toolbar flex gap-2">
      <el-button type="primary" size="small" @click="handleIn"><el-icon><Plus /></el-icon>入库</el-button>
      <el-button type="warning" size="small" @click="handleOut"><el-icon><Minus /></el-icon>出库</el-button>
    </div>
    <el-alert v-if="lowStock.length" :title="'库存预警：' + lowStock.map(p=>p.name).join('、')" type="warning" show-icon class="mb-4" />
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" /><el-table-column prop="productName" label="商品" />
      <el-table-column prop="type" label="类型" width="80"><template #default="s"><el-tag :type="s.row.type==='in'?'success':'danger'" size="small">{{ s.row.type==='in'?'入库':'出库' }}</el-tag></template></el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" /><el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="时间" width="160" />
    </el-table>
    <div class="mt-5 text-right"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="loadData" small /></div>
    <el-dialog v-model="dialogVisible" :title="stockType==='in'?'入库':'出库'" width="450px">
      <el-form :model="sForm" ref="sFormRef" label-width="90px">
        <el-form-item label="选择商品"><el-select v-model="sForm.productId" placeholder="选择商品" filterable class="w-full"><el-option v-for="p in products" :key="p.id" :label="p.name+' (库存:'+p.stock+')'" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="sForm.quantity" :min="1" class="w-full" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="sForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitStock">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { getStockList, stockIn, stockOut, getAllProducts } from '@/api'

const tableData = ref([]); const total = ref(0); const loading = ref(false); const products = ref([]); const lowStock = ref([])
const dialogVisible = ref(false); const stockType = ref('in')
const search = reactive({ page: 1, limit: 10 })
const sForm = reactive({ productId: null, quantity: 1, remark: '' }); const sFormRef = ref(null)

onMounted(() => { loadData(); loadProducts() })
async function loadData() { loading.value = true; try { const r = await getStockList(search); tableData.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false } }
async function loadProducts() { try { const r = await getAllProducts(); products.value = r.data; lowStock.value = r.data.filter(p => p.stock < 10) } catch {} }
function handleIn() { stockType.value = 'in'; Object.assign(sForm, { productId: null, quantity: 1, remark: '' }); dialogVisible.value = true }
function handleOut() { stockType.value = 'out'; Object.assign(sForm, { productId: null, quantity: 1, remark: '' }); dialogVisible.value = true }
async function submitStock() { if (!sForm.productId) { ElMessage.warning('请选择商品'); return }; await (stockType.value === 'in' ? stockIn({ ...sForm }) : stockOut({ ...sForm })); ElMessage.success(stockType.value === 'in' ? '入库成功' : '出库成功'); dialogVisible.value = false; loadData(); loadProducts() }
</script>
