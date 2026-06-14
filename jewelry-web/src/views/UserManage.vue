<template>
  <div class="container-page">
    <div class="toolbar"><el-button type="primary" size="small" @click="handleAdd"><el-icon><Plus /></el-icon>新增用户</el-button></div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" /><el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" /><el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="role" label="角色" width="100"><template #default="s"><el-tag :type="s.row.role==='admin'?'danger':'info'" size="small">{{ s.row.role==='admin'?'管理员':'销售员' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="160"><template #default="s">
        <el-button type="primary" link size="small" @click="handleEdit(s.row)">编辑</el-button>
        <el-button type="danger" link size="small" @click="handleDelete(s.row)">删除</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="form" ref="formRef" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" :placeholder="form.id?'留空不修改':'请输入密码'" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role" class="w-full"><el-option label="管理员" value="admin" /><el-option label="销售员" value="sales" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, saveUser, updateUser, deleteUser } from '@/api'

const tableData = ref([]); const loading = ref(false)
const dialogVisible = ref(false); const dialogTitle = ref('新增用户')
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', role: 'sales' }); const formRef = ref(null)

onMounted(() => loadData())
async function loadData() { loading.value = true; try { tableData.value = (await getUserList()).data } catch {} finally { loading.value = false } }
function handleAdd() { dialogTitle.value = '新增用户'; Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 'sales' }); dialogVisible.value = true }
function handleEdit(row) { dialogTitle.value = '编辑用户'; Object.assign(form, { ...row, password: '' }); dialogVisible.value = true }
async function submitForm() { if (!form.username) { ElMessage.warning('请输入用户名'); return }; if (!form.id && !form.password) { ElMessage.warning('请输入密码'); return }; await (form.id ? updateUser({ ...form }) : saveUser({ ...form })); ElMessage.success(form.id ? '修改成功' : '添加成功'); dialogVisible.value = false; loadData() }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await deleteUser(row.id); ElMessage.success('删除成功'); loadData() }
</script>
