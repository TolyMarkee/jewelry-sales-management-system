<template>
  <div class="container">
    <div class="toolbar">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增分类</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="450px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { getCategoryList, saveCategory, updateCategory, deleteCategory } from '@/api'

export default {
  name: 'CategoryManage',
  data() {
    return {
      tableData: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '新增分类',
      form: { id: null, name: '', description: '' }
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getCategoryList()
        this.tableData = res.data
      } catch (e) { } finally { this.loading = false }
    },
    handleAdd() {
      this.dialogTitle = '新增分类'
      this.form = { id: null, name: '', description: '' }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑分类'
      this.form = { ...row }
      this.dialogVisible = true
    },
    submitForm() {
      if (!this.form.name) { this.$message.warning('请输入分类名称'); return }
      const action = this.form.id ? updateCategory(this.form) : saveCategory(this.form)
      action.then(() => {
        this.$message.success(this.form.id ? '修改成功' : '添加成功')
        this.dialogVisible = false
        this.loadData()
      }).catch(() => { })
    },
    handleDelete(row) {
      this.$confirm('确定删除该分类吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteCategory(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => { })
    }
  }
}
</script>
