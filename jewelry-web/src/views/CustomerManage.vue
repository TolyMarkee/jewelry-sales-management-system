<template>
  <div class="container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" size="small">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/电话" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增客户</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:20px;text-align:right" :current-page="searchForm.page" :page-size="searchForm.limit"
      :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @size-change="v => { searchForm.limit = v; loadData() }"
      @current-change="v => { searchForm.page = v; loadData() }" />

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" />
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
import { getCustomerList, saveCustomer, updateCustomer, deleteCustomer } from '@/api'

export default {
  name: 'CustomerManage',
  data() {
    return {
      searchForm: { keyword: '', page: 1, limit: 10 },
      tableData: [], total: 0, loading: false,
      dialogVisible: false, dialogTitle: '新增客户',
      form: { id: null, name: '', phone: '', email: '', address: '' }
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getCustomerList(this.searchForm)
        this.tableData = res.data.records
        this.total = res.data.total
      } catch (e) { } finally { this.loading = false }
    },
    handleSearch() { this.searchForm.page = 1; this.loadData() },
    handleReset() { this.searchForm = { keyword: '', page: 1, limit: 10 }; this.loadData() },
    handleAdd() {
      this.dialogTitle = '新增客户'; this.form = { id: null, name: '', phone: '', email: '', address: '' }
      this.dialogVisible = true
    },
    handleEdit(row) { this.dialogTitle = '编辑客户'; this.form = { ...row }; this.dialogVisible = true },
    submitForm() {
      if (!this.form.name) { this.$message.warning('请输入客户姓名'); return }
      const action = this.form.id ? updateCustomer(this.form) : saveCustomer(this.form)
      action.then(() => {
        this.$message.success(this.form.id ? '修改成功' : '添加成功')
        this.dialogVisible = false; this.loadData()
      }).catch(() => { })
    },
    handleDelete(row) {
      this.$confirm('确定删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteCustomer(row.id); this.$message.success('删除成功'); this.loadData()
      }).catch(() => { })
    }
  }
}
</script>
