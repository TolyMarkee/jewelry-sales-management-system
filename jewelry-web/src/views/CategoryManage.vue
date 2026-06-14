<template>
  <div class="max-w-3xl mx-auto space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:#DEE4EA">分类管理</h2>
      <button @click="handleAdd" class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:rgba(168,85,247,0.2); color:#c4b5fd; border:1px solid rgba(168,85,247,0.25)">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
        新建分类
      </button>
    </div>

    <!-- Folder tree cards -->
    <div class="space-y-2">
      <div
        v-for="(cat, idx) in tableData" :key="cat.id"
        class="group flex items-center gap-4 px-4 py-3 rounded-xl transition-all duration-300 hover:translate-x-1 relative overflow-hidden"
        style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.04)"
      >
        <div class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity" :style="{background:`linear-gradient(90deg, ${colors[idx%5]}15, transparent 40%)`}" />

        <!-- Folder icon -->
        <div class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 relative z-10" :style="{background:`${colors[idx%5]}18`}">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" :stroke="colors[idx%5]" stroke-width="1.5">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>

        <div class="flex-1 min-w-0 relative z-10">
          <div class="text-sm font-medium" style="color:#DEE4EA">{{ cat.name }}</div>
          <div class="text-xs mt-0.5 truncate" style="color:#596773">{{ cat.description || '暂无描述' }}</div>
        </div>

        <div class="text-xs flex-shrink-0 relative z-10" style="color:#596773">{{ cat.createTime?.substring(0,10) }}</div>

        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity relative z-10">
          <button @click="handleEdit(cat)" class="w-7 h-7 rounded-lg flex items-center justify-center transition-all hover:scale-110" style="color:#60a5fa" title="编辑">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
          </button>
          <button @click="handleDelete(cat)" class="w-7 h-7 rounded-lg flex items-center justify-center transition-all hover:scale-110" style="color:#ef4444" title="删除">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </div>
    </div>

    <div v-if="tableData.length===0" class="text-center py-16">
      <div class="text-4xl mb-3 opacity-30">📁</div>
      <div class="text-sm" style="color:#596773">暂无分类，点击上方按钮创建</div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" :close-on-click-modal="false">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：戒指" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="简单描述这个分类" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false" style="background:transparent; border-color:#2C333A; color:#94a3b8">取消</el-button>
        <el-button type="primary" @click="submitForm" style="background:linear-gradient(135deg,#a855f7,#6366f1); border:none">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, saveCategory, updateCategory, deleteCategory } from '@/api'

const tableData = ref([])
const dialogVisible = ref(false); const dialogTitle = ref('新建分类')
const form = reactive({ id:null, name:'', description:'' }); const formRef = ref(null)
const colors = ['#a855f7','#60a5fa','#34d399','#f59e0b','#f472b6']

onMounted(() => loadData())
async function loadData() { try { tableData.value = (await getCategoryList()).data } catch {} }
function handleAdd() { dialogTitle.value='新建分类'; Object.assign(form,{id:null,name:'',description:''}); dialogVisible.value=true }
function handleEdit(row) { dialogTitle.value='编辑分类'; Object.assign(form,{...row}); dialogVisible.value=true }
async function submitForm() { if(!form.name){ElMessage.warning('请输入名称');return}; await (form.id?updateCategory({...form}):saveCategory({...form})); ElMessage.success(form.id?'修改成功':'创建成功'); dialogVisible.value=false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm(`确定删除「${row.name}」？`,'提示',{type:'warning'}); await deleteCategory(row.id); ElMessage.success('已删除'); loadData() }
</script>
