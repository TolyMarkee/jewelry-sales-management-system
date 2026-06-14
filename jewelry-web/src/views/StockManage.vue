<template>
  <div class="container">
    <div class="toolbar">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleIn">入库</el-button>
      <el-button type="warning" size="small" icon="el-icon-minus" @click="handleOut">出库</el-button>
    </div>

    <!-- 库存预警提示 -->
    <el-alert v-if="lowStockProducts.length > 0" :title="'库存预警：以下商品库存不足10件：' + lowStockProducts.map(p => p.name).join('、')"
      type="warning" show-icon style="margin-bottom:15px" />

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="type" label="类型" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.type==='in'?'success':'danger'" size="small">
            {{ scope.row.type === 'in' ? '入库' : '出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="时间" width="160" />
    </el-table>

    <el-pagination style="margin-top:20px;text-align:right" :current-page="searchForm.page" :page-size="searchForm.limit"
      :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @size-change="v => { searchForm.limit = v; loadData() }"
      @current-change="v => { searchForm.page = v; loadData() }" />

    <!-- 入库/出库弹窗 -->
    <el-dialog :title="stockTitle" :visible.sync="dialogVisible" width="450px">
      <el-form :model="stockForm" ref="stockFormRef" label-width="90px">
        <el-form-item label="选择商品" prop="productId">
          <el-select v-model="stockForm.productId" placeholder="请选择商品" style="width:100%" filterable>
            <el-option v-for="p in productList" :key="p.id" :label="p.name + ' (库存:' + p.stock + ')'" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStockList, stockIn, stockOut, getAllProducts } from '@/api'

export default {
  name: 'StockManage',
  data() {
    return {
      searchForm: { page: 1, limit: 10 },
      tableData: [], total: 0, loading: false,
      productList: [], lowStockProducts: [],
      dialogVisible: false, stockType: 'in',
      stockForm: { productId: null, quantity: 1, remark: '' }
    }
  },
  computed: { stockTitle() { return this.stockType === 'in' ? '入库' : '出库' } },
  created() { this.loadData(); this.loadProducts() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getStockList(this.searchForm)
        this.tableData = res.data.list
        this.total = res.data.total
      } catch (e) { } finally { this.loading = false }
    },
    async loadProducts() {
      try {
        const res = await getAllProducts()
        this.productList = res.data
        this.lowStockProducts = res.data.filter(p => p.stock < 10)
      } catch (e) { }
    },
    handleIn() {
      this.stockType = 'in'
      this.stockForm = { productId: null, quantity: 1, remark: '' }
      this.dialogVisible = true
    },
    handleOut() {
      this.stockType = 'out'
      this.stockForm = { productId: null, quantity: 1, remark: '' }
      this.dialogVisible = true
    },
    submitStock() {
      if (!this.stockForm.productId) { this.$message.warning('请选择商品'); return }
      const action = this.stockType === 'in' ? stockIn(this.stockForm) : stockOut(this.stockForm)
      action.then(() => {
        this.$message.success(this.stockType === 'in' ? '入库成功' : '出库成功')
        this.dialogVisible = false; this.loadData(); this.loadProducts()
      }).catch(() => { })
    }
  }
}
</script>
