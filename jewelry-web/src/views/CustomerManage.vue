<template>
  <div class="container-page">
    <div class="search-bar"><el-form :inline="true" :model="search" size="small">
      <el-form-item label="关键词"><el-input v-model="search.keyword" placeholder="姓名/电话" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">搜索</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
    </el-form></div>
    <div class="toolbar"><el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon>新增客户</el-button></div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" /><el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" /><el-table-column prop="email" label="邮箱" />
      <el-table-column prop="address" label="地址" /><el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="160"><template #default="s">
        <el-button type="primary" link size="small" @click="handleEdit(s.row)">编辑</el-button>
        <el-button type="danger" link size="small" @click="handleDelete(s.row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <div class="mt-5 text-right"><el-pagination v-model:current-page="search.page" v-model:page-size="search.limit" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="loadData" small /></div>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCustomerList, saveCustomer, updateCustomer, deleteCustomer } from '@/api'

const tableData = ref([]); const total = ref(0); const loading = ref(false)
const dialogVisible = ref(false); const dialogTitle = ref('新增客户')
const search = reactive({ keyword: '', page: 1, limit: 10 })
const form = reactive({ id: null, name: '', phone: '', email: '', address: '' }); const formRef = ref(null)

onMounted(() => loadData())
async function loadData() { loading.value = true; try { const r = await getCustomerList(search); tableData.value = r.data.records; total.value = r.data.total } catch {} finally { loading.value = false } }
function resetSearch() { Object.assign(search, { keyword: '', page: 1, limit: 10 }); loadData() }
function handleAdd() { dialogTitle.value = '新增客户'; Object.assign(form, { id: null, name: '', phone: '', email: '', address: '' }); dialogVisible.value = true }
function handleEdit(row) { dialogTitle.value = '编辑客户'; Object.assign(form, { ...row }); dialogVisible.value = true }
async function submitForm() { if (!form.name) { ElMessage.warning('请输入姓名'); return }; await (form.id ? updateCustomer({ ...form }) : saveCustomer({ ...form })); ElMessage.success(form.id ? '修改成功' : '添加成功'); dialogVisible.value = false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteCustomer(row.id); ElMessage.success('删除成功'); loadData() }
</script>
