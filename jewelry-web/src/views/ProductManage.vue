<template>
  <div class="container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" size="small">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部" clearable>
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增商品</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="material" label="材质" width="100" />
      <el-table-column prop="image" label="图片" width="80">
        <template slot-scope="scope">
          <el-image v-if="scope.row.image" :src="'/images/' + scope.row.image" :preview-src-list="['/images/' + scope.row.image]" style="width:40px;height:40px" fit="cover" />
          <span v-else style="color:#ccc">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格(元)" width="120">
        <template slot-scope="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination style="margin-top:20px;text-align:right" :current-page="searchForm.page" :page-size="searchForm.limit"
      :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange"
      @current-change="handlePageChange" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="550px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width:100%">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="材质" prop="material">
          <el-input v-model="form.material" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="product-upload"
            action="/api/upload/product"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload">
            <img v-if="form.image" :src="'/images/' + form.image" class="product-upload-preview" />
            <i v-else class="el-icon-plus product-upload-icon" />
          </el-upload>
          <span style="font-size:12px;color:#999;margin-left:10px">{{ form.image || '点击上传商品图片' }}</span>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { getProductList, getCategoryList, saveProduct, updateProduct, deleteProduct } from '@/api'

export default {
  name: 'ProductManage',
  data() {
    return {
      searchForm: { keyword: '', categoryId: null, page: 1, limit: 10 },
      tableData: [],
      total: 0,
      loading: false,
      categoryList: [],
      dialogVisible: false,
      dialogTitle: '新增商品',
      form: { id: null, name: '', categoryId: null, material: '', price: 0, stock: 0, image: '', description: '' },
      formRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadCategories()
    this.loadData()
  },
  computed: {
    uploadHeaders() {
      return { Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
    }
  },
  methods: {
    handleImageSuccess(res) {
      if (res.code === 0 && res.data) {
        this.form.image = res.data
      } else {
        this.$message.error('图片上传失败')
      }
    },
    beforeImageUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      if (!isLt5M) { this.$message.error('图片大小不能超过 5MB'); return false }
      return true
    },
    async loadCategories() {
      try {
        const res = await getCategoryList()
        this.categoryList = res.data
      } catch (e) { }
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getProductList(this.searchForm)
        this.tableData = res.data.list
        this.total = res.data.total
      } catch (e) { } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.searchForm.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { keyword: '', categoryId: null, page: 1, limit: 10 }
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增商品'
      this.form = { id: null, name: '', categoryId: null, material: '', price: 0, stock: 0, image: '', description: '' }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑商品'
      this.form = { ...row }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        try {
          if (this.form.id) {
            await updateProduct(this.form)
          } else {
            await saveProduct(this.form)
          }
          this.$message.success(this.form.id ? '修改成功' : '添加成功')
          this.dialogVisible = false
          this.loadData()
        } catch (e) { }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await deleteProduct(row.id)
          this.$message.success('删除成功')
          this.loadData()
        } catch (e) { }
      }).catch(() => { })
    },
    handleSizeChange(val) {
      this.searchForm.limit = val
      this.loadData()
    },
    handlePageChange(val) {
      this.searchForm.page = val
      this.loadData()
    }
  }
}
</script>

<style scoped>
.product-upload { display: inline-block; }
.product-upload >>> .el-upload {
  border: 2px dashed #d9d9d9; border-radius: 8px; cursor: pointer;
  width: 120px; height: 120px; overflow: hidden;
}
.product-upload >>> .el-upload:hover { border-color: #409EFF; }
.product-upload-preview { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; }
.product-upload-icon {
  font-size: 28px; color: #8c939d; width: 120px; height: 120px;
  line-height: 120px; text-align: center;
}
</style>
