<template>
  <div class="container-page">
    <div class="search-bar"><el-form :inline="true" :model="search" size="small">
      <el-form-item label="订单编号"><el-input v-model="search.orderNo" clearable /></el-form-item>
      <el-form-item label="状态"><el-select v-model="search.status" clearable><el-option v-for="o in statusOpts" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
    </el-form></div>
    <div class="toolbar"><el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon>创建订单</el-button></div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" /><el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="customerName" label="客户" width="100" /><el-table-column prop="userName" label="销售员" width="100" />
      <el-table-column prop="totalAmount" label="总金额" width="110"><template #default="s">¥{{ s.row.totalAmount }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="90"><template #default="s"><el-tag :type="statusType(s.row.status)" size="small">{{ statusLabel(s.row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right"><template #default="s">
        <el-button type="primary" link size="small" @click="showDetail(s.row)">详情</el-button>
        <el-button v-if="s.row.status==='pending'" link size="small" @click="updateStatus(s.row.id,'paid')">付款</el-button>
        <el-button v-if="s.row.status==='paid'" link size="small" @click="updateStatus(s.row.id,'shipped')">发货</el-button>
        <el-button v-if="s.row.status==='shipped'" link size="small" @click="updateStatus(s.row.id,'completed')">完成</el-button>
        <el-button type="danger" link size="small" @click="handleDelete(s.row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <div class="mt-5 text-right"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="loadData" small /></div>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="创建订单" width="750px" @closed="resetOrder">
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="客户"><el-select v-model="orderForm.customerId" filterable class="w-full"><el-option v-for="c in custList" :key="c.id" :label="c.name+' - '+c.phone" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="orderForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <h4 class="mb-3">订单明细</h4>
      <el-table :data="orderForm.items" border size="small">
        <el-table-column label="商品" width="200"><template #default="s">
          <el-select v-model="s.row.productId" filterable size="small" @change="onProdChange(s.row)"><el-option v-for="p in prodList" :key="p.id" :label="p.name+' (¥'+p.price+')'" :value="p.id" /></el-select>
        </template></el-table-column>
        <el-table-column label="单价" width="100"><template #default="s">¥{{ s.row.price }}</template></el-table-column>
        <el-table-column label="数量" width="100"><template #default="s"><el-input-number v-model="s.row.quantity" :min="1" size="small" style="width:80px" /></template></el-table-column>
        <el-table-column label="小计" width="120"><template #default="s">¥{{ (s.row.price*s.row.quantity).toFixed(2) }}</template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="s"><el-button type="danger" link size="small" @click="removeItem(s.$index)">删除</el-button></template></el-table-column>
      </el-table>
      <el-button type="primary" link class="mt-3" @click="orderForm.items.push({productId:null,price:0,quantity:1})"><el-icon><Plus /></el-icon>添加商品</el-button>
      <div class="text-right mt-3 text-lg">订单总额：<strong class="text-red-500">¥{{ totalPrice }}</strong></div>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitOrder">提交订单</el-button></template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="curOrder">
        <p><strong>订单编号：</strong>{{ curOrder.orderNo }}</p><p><strong>客户：</strong>{{ curOrder.customerName }}</p>
        <p><strong>销售员：</strong>{{ curOrder.userName }}</p><p><strong>状态：</strong>{{ statusLabel(curOrder.status) }}</p>
        <p><strong>备注：</strong>{{ curOrder.remark || '无' }}</p>
        <el-table :data="curOrder.items" border size="small" class="mt-4">
          <el-table-column prop="productName" label="商品" /><el-table-column prop="price" label="单价"><template #default="s">¥{{ s.row.price }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
        </el-table>
        <p class="text-right mt-3 text-lg"><strong>总金额：¥{{ curOrder.totalAmount }}</strong></p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getOrderList, saveOrder, updateOrderStatus, deleteOrder, getAllCustomers, getAllProducts } from '@/api'

const tableData = ref([]); const total = ref(0); const loading = ref(false); const dialogVisible = ref(false); const detailVisible = ref(false)
const custList = ref([]); const prodList = ref([]); const curOrder = ref(null)
const search = reactive({ orderNo: '', status: '', page: 1, limit: 10 })
const orderForm = reactive({ customerId: null, remark: '', items: [{productId:null,price:0,quantity:1}] })
const totalPrice = computed(() => orderForm.items.reduce((s,i) => s+i.price*i.quantity, 0).toFixed(2))
const statusOpts = [{label:'待付款',value:'pending'},{label:'已付款',value:'paid'},{label:'已发货',value:'shipped'},{label:'已完成',value:'completed'},{label:'已取消',value:'cancelled'}]
function statusLabel(s) { const m = {pending:'待付款',paid:'已付款',shipped:'已发货',completed:'已完成',cancelled:'已取消'}; return m[s]||s }
function statusType(s) { const m = {pending:'warning',paid:'info',shipped:'',completed:'success',cancelled:'danger'}; return m[s]||'info' }

onMounted(() => loadData())
async function loadData() { loading.value = true; try { const r = await getOrderList(search); tableData.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false } }
function resetSearch() { Object.assign(search, { orderNo: '', status: '', page: 1, limit: 10 }); loadData() }
async function handleAdd() { resetOrder(); const [c,p] = await Promise.all([getAllCustomers(), getAllProducts()]); custList.value = c.data; prodList.value = p.data; dialogVisible.value = true }
function resetOrder() { Object.assign(orderForm, { customerId: null, remark: '', items: [{productId:null,price:0,quantity:1}] }) }
function onProdChange(item) { const p = prodList.value.find(x => x.id===item.productId); if (p) item.price = p.price }
function removeItem(i) { if (orderForm.items.length>1) orderForm.items.splice(i,1) }
async function submitOrder() {
  if (!orderForm.customerId) { ElMessage.warning('请选择客户'); return }
  if (orderForm.items.some(i=>!i.productId)) { ElMessage.warning('请选择商品'); return }
  await saveOrder({ customerId:orderForm.customerId, remark:orderForm.remark, totalAmount:parseFloat(totalPrice.value), items:orderForm.items.map(i=>({productId:i.productId,quantity:i.quantity,price:i.price})) })
  ElMessage.success('下单成功'); dialogVisible.value = false; loadData()
}
async function updateStatus(id, status) { await updateOrderStatus({ id, status }); ElMessage.success('更新成功'); loadData() }
function showDetail(row) { curOrder.value = row; detailVisible.value = true }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteOrder(row.id); ElMessage.success('删除成功'); loadData() }
</script>
