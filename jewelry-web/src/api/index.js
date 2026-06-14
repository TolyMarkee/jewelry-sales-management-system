import request from '@/utils/request'

// ============== 登录 ==============
export function login(params) {
  return request.post('/login', null, { params })
}

// ============== 用户管理 ==============
export function getUserList(params) {
  return request.get('/user/list', { params })
}
export function saveUser(data) {
  return request.post('/user/save', data)
}
export function updateUser(data) {
  return request.put('/user/update', data)
}
export function deleteUser(id) {
  return request.delete('/user/delete/' + id)
}
export function getProfile() {
  return request.get('/user/profile')
}
export function updateProfile(data) {
  return request.put('/user/profile', data)
}
export function changePassword(data) {
  return request.put('/user/password', data)
}

// ============== 文件上传 ==============
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/product', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ============== 分类管理 ==============
export function getCategoryList() {
  return request.get('/category/list')
}
export function saveCategory(data) {
  return request.post('/category/save', data)
}
export function updateCategory(data) {
  return request.put('/category/update', data)
}
export function deleteCategory(id) {
  return request.delete('/category/delete/' + id)
}

// ============== 商品管理 ==============
export function getProductList(params) {
  return request.get('/product/list', { params })
}
export function getAllProducts() {
  return request.get('/product/all')
}
export function saveProduct(data) {
  return request.post('/product/save', data)
}
export function updateProduct(data) {
  return request.put('/product/update', data)
}
export function deleteProduct(id) {
  return request.delete('/product/delete/' + id)
}

// ============== 客户管理 ==============
export function getCustomerList(params) {
  return request.get('/customer/list', { params })
}
export function getAllCustomers() {
  return request.get('/customer/all')
}
export function saveCustomer(data) {
  return request.post('/customer/save', data)
}
export function updateCustomer(data) {
  return request.put('/customer/update', data)
}
export function deleteCustomer(id) {
  return request.delete('/customer/delete/' + id)
}

// ============== 订单管理 ==============
export function getOrderList(params) {
  return request.get('/order/list', { params })
}
export function saveOrder(data) {
  return request.post('/order/save', data)
}
export function updateOrderStatus(data) {
  return request.put('/order/updateStatus', data)
}
export function deleteOrder(id) {
  return request.delete('/order/delete/' + id)
}

// ============== 库存管理 ==============
export function getStockList(params) {
  return request.get('/stock/list', { params })
}
export function stockIn(data) {
  return request.post('/stock/in', data)
}
export function stockOut(data) {
  return request.post('/stock/out', data)
}
