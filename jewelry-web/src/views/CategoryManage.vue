<template>
  <div class="container-page">
    <div class="toolbar"><el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon>新增分类</el-button></div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="s">
          <el-button type="primary" link size="small" @click="handleEdit(s.row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryList, saveCategory, updateCategory, deleteCategory } from '@/api'

const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false); const dialogTitle = ref('新增分类')
const form = reactive({ id: null, name: '', description: '' }); const formRef = ref(null)

onMounted(() => loadData())
async function loadData() { loading.value = true; try { tableData.value = (await getCategoryList()).data } catch {} finally { loading.value = false } }
function handleAdd() { dialogTitle.value = '新增分类'; Object.assign(form, { id: null, name: '', description: '' }); dialogVisible.value = true }
function handleEdit(row) { dialogTitle.value = '编辑分类'; Object.assign(form, { ...row }); dialogVisible.value = true }
async function submitForm() { if (!form.name) { ElMessage.warning('请输入名称'); return }; await (form.id ? updateCategory({ ...form }) : saveCategory({ ...form })); ElMessage.success(form.id ? '修改成功' : '添加成功'); dialogVisible.value = false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteCategory(row.id); ElMessage.success('删除成功'); loadData() }
</script>
