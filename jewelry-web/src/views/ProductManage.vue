<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-bold" style="color:#DEE4EA">商品管理</h2>
      <button @click="handleAdd" class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all hover:scale-105" style="background:linear-gradient(135deg,#a855f7,#6366f1); color:#fff">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14"/><path d="M5 12h14"/></svg>新增商品
      </button>
    </div>

    <!-- Search -->
    <div class="flex gap-3 flex-wrap">
      <input v-model="search.keyword" placeholder="搜索商品..." class="h-9 px-3 rounded-lg text-sm outline-none w-48" style="background:#101214; border:1px solid #2C333A; color:#DEE4EA" @keyup.enter="loadData" />
      <el-select v-model="search.categoryId" placeholder="全部分类" clearable size="small" class="w-36" @change="loadData">
        <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <!-- Product grid -->
    <div class="grid grid-cols-1 gap-2">
      <div
        v-for="p in tableData" :key="p.id"
        class="group flex items-center gap-4 px-4 py-3 rounded-xl transition-all hover:translate-x-1"
        style="background:rgba(22,26,29,0.5); border:1px solid rgba(255,255,255,0.04)"
      >
        <!-- Image -->
        <div class="w-12 h-12 rounded-lg overflow-hidden flex-shrink-0" style="background:#101214">
          <img v-if="p.image" :src="'/images/'+p.image" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full flex items-center justify-center" style="color:#2C333A"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></div>
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium" style="color:#DEE4EA">{{ p.name }}</span>
            <span class="text-[10px] px-1.5 py-0.5 rounded" style="background:rgba(96,165,250,0.1); color:#60a5fa">{{ p.categoryName }}</span>
          </div>
          <div class="flex items-center gap-4 mt-1">
            <span class="text-xs" style="color:#596773">{{ p.material || '未标材质' }}</span>
            <span class="text-xs font-mono" style="color:#34d399">¥{{ p.price }}</span>
          </div>
        </div>

        <!-- Stock bar -->
        <div class="hidden md:flex items-center gap-2 w-24 flex-shrink-0">
          <div class="flex-1 h-1.5 rounded-full overflow-hidden" style="background:#2C333A">
            <div class="h-full rounded-full transition-all" :style="{width:Math.min(p.stock/50*100,100)+'%', background:p.stock<10?'#facc15':p.stock===0?'#ef4444':'#34d399'}" />
          </div>
          <span class="text-xs w-7 text-right" :style="{color:p.stock<10?'#facc15':'#596773'}">{{ p.stock }}</span>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button @click="handleEdit(p)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#60a5fa">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
          </button>
          <button @click="handleDelete(p)" class="w-7 h-7 rounded-lg flex items-center justify-center hover:scale-110 transition-all" style="color:#ef4444">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="flex justify-center pt-4">
      <el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,prev,pager,next" small @change="loadData" />
    </div>

    <!-- Dialog (unchanged logic, dark styled) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" ref="formRef" label-width="90px">
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.categoryId" class="w-full"><el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="材质"><el-input v-model="form.material" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" /></el-form-item>
        <el-form-item label="图片">
          <el-upload action="/api/upload/product" :headers="uploadHeaders" :show-file-list="false" :on-success="onImgSuccess" :before-upload="onImgBefore">
            <div v-if="form.image" class="w-20 h-20 rounded-lg overflow-hidden"><img :src="'/images/'+form.image" class="w-full h-full object-cover" /></div>
            <div v-else class="w-20 h-20 rounded-lg border-2 border-dashed flex items-center justify-center cursor-pointer hover:border-purple-500 transition-colors" style="border-color:#2C333A; color:#596773">+</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" class="w-full" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, getCategoryList, saveProduct, updateProduct, deleteProduct } from '@/api'

const tableData = ref([]); const total = ref(0); const cats = ref([])
const dialogVisible = ref(false); const dialogTitle = ref('新增商品')
const search = reactive({ keyword:'', categoryId:null, page:1, limit:10 })
const form = reactive({ id:null,name:'',categoryId:null,material:'',price:0,stock:0,image:'',description:'' }); const formRef = ref(null)
const uploadHeaders = computed(()=>({Authorization:'Bearer '+(localStorage.getItem('token')||'')}))

onMounted(()=>{getCategoryList().then(r=>cats.value=r.data);loadData()})
async function loadData(){try{const r=await getProductList(search);tableData.value=r.data.list;total.value=r.data.total}catch{}}
function handleAdd(){dialogTitle.value='新增商品';Object.assign(form,{id:null,name:'',categoryId:null,material:'',price:0,stock:0,image:'',description:''});dialogVisible.value=true}
function handleEdit(row){dialogTitle.value='编辑商品';Object.assign(form,{...row});dialogVisible.value=true}
function onImgSuccess(res){if(res.code===0)form.image=res.data;else ElMessage.error('上传失败')}
function onImgBefore(f){const ok=f.type.startsWith('image/')&&f.size/1024/1024<5;if(!ok)ElMessage.error('仅图片且<5MB');return ok}
async function submitForm(){if(!form.name){ElMessage.warning('请输入名称');return};await(form.id?updateProduct({...form}):saveProduct({...form}));ElMessage.success(form.id?'修改成功':'添加成功');dialogVisible.value=false;loadData()}
async function handleDelete(row){await ElMessageBox.confirm('确定删除？','提示',{type:'warning'});await deleteProduct(row.id);ElMessage.success('已删除');loadData()}
</script>
