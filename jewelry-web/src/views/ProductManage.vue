<template>
  <div class="container-page">
    <div class="search-bar"><el-form :inline="true" :model="search" size="small">
      <el-form-item label="关键词"><el-input v-model="search.keyword" placeholder="商品名称" clearable /></el-form-item>
      <el-form-item label="分类"><el-select v-model="search.categoryId" placeholder="全部" clearable><el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
    </el-form></div>
    <div class="toolbar"><el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon>新增商品</el-button></div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" /><el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" width="100" /><el-table-column prop="material" label="材质" width="100" />
      <el-table-column prop="image" label="图片" width="80">
        <template #default="s"><el-image v-if="s.row.image" :src="'/images/'+s.row.image" :preview-src-list="['/images/'+s.row.image]" style="width:40px;height:40px" fit="cover" /><span v-else class="text-gray-300">无</span></template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="110"><template #default="s">¥{{ s.row.price }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right"><template #default="s">
        <el-button type="primary" link size="small" @click="handleEdit(s.row)">编辑</el-button>
        <el-button type="danger" link size="small" @click="handleDelete(s.row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <div class="mt-5 text-right"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="loadData" small /></div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" ref="formRef" label-width="90px">
        <el-form-item label="商品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类" prop="categoryId"><el-select v-model="form.categoryId" class="w-full"><el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="材质" prop="material"><el-input v-model="form.material" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" /></el-form-item>
        <el-form-item label="图片">
          <el-upload action="/api/upload/product" :headers="uploadHeaders" :show-file-list="false" :on-success="onImgSuccess" :before-upload="onImgBefore">
            <img v-if="form.image" :src="'/images/'+form.image" class="w-[120px] h-[120px] object-cover rounded-lg" />
            <div v-else class="w-[120px] h-[120px] border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center text-gray-400 cursor-pointer hover:border-blue-400"><el-icon :size="28"><Plus /></el-icon></div>
          </el-upload>
          <span class="text-xs text-gray-400 ml-2">{{ form.image || '点击上传' }}</span>
        </el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" class="w-full" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductList, getCategoryList, saveProduct, updateProduct, deleteProduct } from '@/api'

const tableData = ref([]); const total = ref(0); const loading = ref(false); const cats = ref([])
const dialogVisible = ref(false); const dialogTitle = ref('新增商品')
const search = reactive({ keyword: '', categoryId: null, page: 1, limit: 10 })
const form = reactive({ id: null, name: '', categoryId: null, material: '', price: 0, stock: 0, image: '', description: '' })
const formRef = ref(null)

const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }))

onMounted(() => { getCategoryList().then(r => cats.value = r.data); loadData() })
async function loadData() { loading.value = true; try { const r = await getProductList(search); tableData.value = r.data.list; total.value = r.data.total } catch {} finally { loading.value = false } }
function resetSearch() { Object.assign(search, { keyword: '', categoryId: null, page: 1, limit: 10 }); loadData() }
function handleAdd() { dialogTitle.value = '新增商品'; Object.assign(form, { id: null, name: '', categoryId: null, material: '', price: 0, stock: 0, image: '', description: '' }); dialogVisible.value = true }
function handleEdit(row) { dialogTitle.value = '编辑商品'; Object.assign(form, { ...row }); dialogVisible.value = true }
function onImgSuccess(res) { if (res.code === 0) form.image = res.data; else ElMessage.error('上传失败') }
function onImgBefore(file) { const ok = file.type.startsWith('image/') && file.size/1024/1024 < 5; if (!ok) ElMessage.error('仅图片且<5MB'); return ok }
async function submitForm() { if (!form.name) { ElMessage.warning('请输入商品名称'); return }; await (form.id ? updateProduct({ ...form }) : saveProduct({ ...form })); ElMessage.success(form.id ? '修改成功' : '添加成功'); dialogVisible.value = false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteProduct(row.id); ElMessage.success('删除成功'); loadData() }
</script>
