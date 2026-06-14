<template>
  <div class="container">
    <div class="toolbar">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="role" label="角色" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.role==='admin'?'danger':'info'" size="small">
            {{ scope.row.role === 'admin' ? '管理员' : '销售员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="450px">
      <el-form :model="form" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :placeholder="form.id ? '留空则不修改密码' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="admin" />
            <el-option label="销售员" value="sales" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, saveUser, updateUser, deleteUser } from '@/api'

export default {
  name: 'UserManage',
  data() {
    return {
      tableData: [], loading: false,
      dialogVisible: false, dialogTitle: '新增用户',
      form: { id: null, username: '', password: '', realName: '', phone: '', role: 'sales' }
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getUserList()
        this.tableData = res.data
      } catch (e) { } finally { this.loading = false }
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.form = { id: null, username: '', password: '', realName: '', phone: '', role: 'sales' }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.form = { ...row, password: '' }
      this.dialogVisible = true
    },
    submitForm() {
      if (!this.form.username) { this.$message.warning('请输入用户名'); return }
      if (!this.form.id && !this.form.password) { this.$message.warning('请输入密码'); return }
      const action = this.form.id ? updateUser(this.form) : saveUser(this.form)
      action.then(() => {
        this.$message.success(this.form.id ? '修改成功' : '添加成功')
        this.dialogVisible = false; this.loadData()
      }).catch(() => { })
    },
    handleDelete(row) {
      this.$confirm('确定删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteUser(row.id); this.$message.success('删除成功'); this.loadData()
      }).catch(() => { })
    }
  }
}
</script>
