<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:var(--text-primary)">客户管理</h2>
      <button @click="handleAdd" class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:linear-gradient(135deg,#a855f7,#6366f1); color:#fff">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>新增客户
      </button>
    </div>

    <div class="flex items-center gap-3">
      <SuggestiveInput v-model="search.keyword" :hints="['搜索客户姓名...','按电话查找...','输入关键词搜索...']" @search="loadData" class="w-56" />
      <span class="text-xs" style="color:var(--text-muted)">共 {{ total }} 条</span>
    </div>

    <div class="space-y-2">
      <div v-for="c in tableData" :key="c.id" class="group flex items-center gap-4 px-4 py-3 rounded-xl transition-all hover:translate-x-1" style="background:var(--surface); border:1px solid var(--border)">
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0" :style="{background:'rgba(96,165,250,0.15)', color:'#60a5fa'}">{{ c.name[0] }}</div>
        <div class="flex-1 min-w-0">
          <div class="text-sm font-medium" style="color:var(--text-primary)">{{ c.name }}</div>
          <div class="text-xs mt-0.5" style="color:var(--text-muted)">{{ c.phone }} · {{ c.email || '未填邮箱' }}</div>
        </div>
        <span class="text-xs hidden md:block" style="color:var(--text-muted)">{{ c.address || '' }}</span>
        <span class="text-xs flex-shrink-0" style="color:var(--text-muted)">{{ c.createTime?.substring(0,10) }}</span>
        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button @click="handleEdit(c)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#60a5fa"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg></button>
          <button @click="handleDelete(c)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#ef4444"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
        </div>
      </div>
    </div>

    <div class="flex justify-center pt-4"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,prev,pager,next" small @change="loadData" /></div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" ref="formRef" label-width="70px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm" style="background:linear-gradient(135deg,#a855f7,#6366f1); border:none">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCustomerList, saveCustomer, updateCustomer, deleteCustomer } from '@/api'
import SuggestiveInput from '@/components/SuggestiveInput.vue'

const tableData = ref([]); const total = ref(0); const dialogVisible = ref(false); const dialogTitle = ref('新增客户')
const search = reactive({ keyword:'', page:1, limit:10 })
const form = reactive({ id:null,name:'',phone:'',email:'',address:'' }); const formRef = ref(null)

onMounted(()=>loadData())
async function loadData(){try{const r=await getCustomerList(search);tableData.value=r.data.records;total.value=r.data.total}catch{}}
function handleAdd(){dialogTitle.value='新增客户';Object.assign(form,{id:null,name:'',phone:'',email:'',address:''});dialogVisible.value=true}
function handleEdit(row){dialogTitle.value='编辑客户';Object.assign(form,{...row});dialogVisible.value=true}
async function submitForm(){if(!form.name){ElMessage.warning('请输入姓名');return};await(form.id?updateCustomer({...form}):saveCustomer({...form}));ElMessage.success(form.id?'修改成功':'添加成功');dialogVisible.value=false;loadData()}
async function handleDelete(row){await ElMessageBox.confirm('确定删除？','提示',{type:'warning'});await deleteCustomer(row.id);ElMessage.success('已删除');loadData()}
</script>
