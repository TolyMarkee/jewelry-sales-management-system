<template>
  <div class="container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" size="small">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="订单编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待付款" value="pending" />
            <el-option label="已付款" value="paid" />
            <el-option label="已发货" value="shipped" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
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
      <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">创建订单</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="customerName" label="客户" width="120" />
      <el-table-column prop="userName" label="销售员" width="100" />
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template slot-scope="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="showDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status==='pending'" type="text" size="small"
            @click="updateStatus(scope.row.id, 'paid')">付款</el-button>
          <el-button v-if="scope.row.status==='paid'" type="text" size="small"
            @click="updateStatus(scope.row.id, 'shipped')">发货</el-button>
          <el-button v-if="scope.row.status==='shipped'" type="text" size="small"
            @click="updateStatus(scope.row.id, 'completed')">完成</el-button>
          <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination style="margin-top:20px;text-align:right" :current-page="searchForm.page" :page-size="searchForm.limit"
      :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
      @size-change="v => { searchForm.limit = v; loadData() }"
      @current-change="v => { searchForm.page = v; loadData() }" />

    <!-- 新增订单弹窗 -->
    <el-dialog title="创建订单" :visible.sync="dialogVisible" width="750px" @closed="resetOrderForm">
      <el-form :model="orderForm" ref="orderFormRef" label-width="90px">
        <el-form-item label="选择客户" prop="customerId">
          <el-select v-model="orderForm.customerId" placeholder="请选择客户" style="width:100%" filterable>
            <el-option v-for="c in customerList" :key="c.id" :label="c.name + ' - ' + c.phone" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <h4 style="margin-bottom:10px">订单明细</h4>
      <el-table :data="orderForm.items" border size="small">
        <el-table-column label="商品" width="200">
          <template slot-scope="scope">
            <el-select v-model="scope.row.productId" placeholder="选择商品" size="small" filterable @change="onProductChange(scope.row)">
              <el-option v-for="p in productList" :key="p.id" :label="p.name + ' (¥' + p.price + ')'" :value="p.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template slot-scope="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="100">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" size="small" style="width:80px" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template slot-scope="scope">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" style="color:#f56c6c"
              @click="removeItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="text" icon="el-icon-plus" style="margin-top:10px" @click="addItem">添加商品</el-button>

      <div style="text-align:right;margin-top:10px;font-size:16px">
        订单总额：<strong style="color:#f56c6c">¥{{ totalAmount }}</strong>
      </div>

      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </div>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="600px">
      <div v-if="currentOrder">
        <p><strong>订单编号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>客户：</strong>{{ currentOrder.customerName }}</p>
        <p><strong>销售员：</strong>{{ currentOrder.userName }}</p>
        <p><strong>状态：</strong>{{ statusLabel(currentOrder.status) }}</p>
        <p><strong>备注：</strong>{{ currentOrder.remark || '无' }}</p>
        <el-table :data="currentOrder.items" border size="small" style="margin-top:15px">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="price" label="单价">
            <template slot-scope="s">¥{{ s.row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
        </el-table>
        <p style="text-align:right;margin-top:10px;font-size:16px">
          <strong>总金额：¥{{ currentOrder.totalAmount }}</strong>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderList, saveOrder, updateOrderStatus, deleteOrder, getAllCustomers, getAllProducts } from '@/api'

export default {
  name: 'OrderManage',
  data() {
    return {
      searchForm: { orderNo: '', status: '', page: 1, limit: 10 },
      tableData: [], total: 0, loading: false,
      dialogVisible: false, detailVisible: false,
      customerList: [], productList: [], currentOrder: null,
      orderForm: { customerId: null, remark: '', items: [{ productId: null, price: 0, quantity: 1 }] }
    }
  },
  computed: {
    totalAmount() {
      return this.orderForm.items.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getOrderList(this.searchForm)
        this.tableData = res.data.list
        this.total = res.data.total
      } catch (e) { } finally { this.loading = false }
    },
    async loadRefData() {
      try {
        const [custRes, prodRes] = await Promise.all([getAllCustomers(), getAllProducts()])
        this.customerList = custRes.data
        this.productList = prodRes.data
      } catch (e) { }
    },
    handleSearch() { this.searchForm.page = 1; this.loadData() },
    handleReset() { this.searchForm = { orderNo: '', status: '', page: 1, limit: 10 }; this.loadData() },
    handleAdd() { this.resetOrderForm(); this.loadRefData(); this.dialogVisible = true },
    resetOrderForm() {
      this.orderForm = { customerId: null, remark: '', items: [{ productId: null, price: 0, quantity: 1 }] }
    },
    addItem() { this.orderForm.items.push({ productId: null, price: 0, quantity: 1 }) },
    removeItem(index) { if (this.orderForm.items.length > 1) this.orderForm.items.splice(index, 1) },
    onProductChange(item) {
      const product = this.productList.find(p => p.id === item.productId)
      if (product) { item.price = product.price }
    },
    submitOrder() {
      if (!this.orderForm.customerId) { this.$message.warning('请选择客户'); return }
      if (this.orderForm.items.some(i => !i.productId)) { this.$message.warning('请选择商品'); return }
      const data = {
        customerId: this.orderForm.customerId,
        remark: this.orderForm.remark,
        totalAmount: parseFloat(this.totalAmount),
        items: this.orderForm.items.map(i => ({ productId: i.productId, quantity: i.quantity, price: i.price }))
      }
      saveOrder(data).then(() => {
        this.$message.success('下单成功')
        this.dialogVisible = false; this.loadData()
      }).catch(() => { })
    },
    updateStatus(id, status) {
      updateOrderStatus({ id, status }).then(() => {
        this.$message.success('更新成功'); this.loadData()
      }).catch(() => { })
    },
    showDetail(row) { this.currentOrder = row; this.detailVisible = true },
    handleDelete(row) {
      this.$confirm('确定删除该订单吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteOrder(row.id); this.$message.success('删除成功'); this.loadData()
      }).catch(() => { })
    },
    statusLabel(s) {
      const map = { pending: '待付款', paid: '已付款', shipped: '已发货', completed: '已完成', cancelled: '已取消' }
      return map[s] || s
    },
    statusType(s) {
      const map = { pending: 'warning', paid: 'info', shipped: '', completed: 'success', cancelled: 'danger' }
      return map[s] || 'info'
    }
  }
}
</script>
