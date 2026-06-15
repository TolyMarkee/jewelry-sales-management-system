<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:var(--text-primary)">用户管理</h2>
      <button @click="handleAdd" class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:linear-gradient(135deg,#a855f7,#6366f1); color:#fff">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>新增用户
      </button>
    </div>

    <div class="space-y-2">
      <div v-for="u in tableData" :key="u.id" class="group flex items-center gap-4 px-4 py-3 rounded-xl transition-all hover:translate-x-1" style="background:var(--surface); border:1px solid var(--border)">
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0" :style="{background:u.role==='admin'?'rgba(239,68,68,0.15)':'rgba(96,165,250,0.15)', color:u.role==='admin'?'#ef4444':'#60a5fa'}">{{ (u.realName||u.username)[0] }}</div>
        <div class="flex-1 min-w-0">
          <div class="text-sm font-medium" style="color:var(--text-primary)">{{ u.realName || u.username }}</div>
          <div class="text-xs mt-0.5" style="color:var(--text-muted)">@{{ u.username }} · {{ u.phone || '未填电话' }}</div>
        </div>
        <span class="text-[10px] px-2 py-0.5 rounded-full font-medium" :style="roleStyle(u.role)">{{ roleLabel(u.role) }}</span>
        <span class="text-xs flex-shrink-0" style="color:var(--text-muted)">{{ u.createTime?.substring(0,10) }}</span>
        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button @click="handleEdit(u)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#60a5fa"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg></button>
          <button @click="handleDelete(u)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#ef4444"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" :placeholder="form.id?'留空不修改':'请输入密码'" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role" class="w-full"><el-option label="管理员" value="admin" /><el-option label="销售主管" value="manager" /><el-option label="销售员" value="sales" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm" style="background:linear-gradient(135deg,#a855f7,#6366f1); border:none">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, saveUser, updateUser, deleteUser } from '@/api'

const tableData = ref([]); const dialogVisible = ref(false); const dialogTitle = ref('新增用户')
function roleLabel(r){ return {admin:'管理员',manager:'销售主管',sales:'销售员'}[r]||r }
function roleStyle(r){ const m={admin:{background:'rgba(239,68,68,0.15)',color:'#ef4444'},manager:{background:'rgba(168,85,247,0.15)',color:'#a855f7'},sales:{background:'rgba(96,165,250,0.15)',color:'#60a5fa'}}; return m[r]||m.sales }
const form = reactive({ id:null,username:'',password:'',realName:'',phone:'',role:'sales' }); const formRef = ref(null)

onMounted(()=>loadData())
async function loadData(){try{tableData.value=(await getUserList()).data}catch{}}
function handleAdd(){dialogTitle.value='新增用户';Object.assign(form,{id:null,username:'',password:'',realName:'',phone:'',role:'sales'});dialogVisible.value=true}
function handleEdit(row){dialogTitle.value='编辑用户';Object.assign(form,{...row,password:''});dialogVisible.value=true}
async function submitForm(){if(!form.username){ElMessage.warning('请输入用户名');return};if(!form.id&&!form.password){ElMessage.warning('请输入密码');return};await(form.id?updateUser({...form}):saveUser({...form}));ElMessage.success(form.id?'修改成功':'添加成功');dialogVisible.value=false;loadData()}
async function handleDelete(row){await ElMessageBox.confirm('确定删除？','提示',{type:'warning'});await deleteUser(row.id);ElMessage.success('已删除');loadData()}
</script>
