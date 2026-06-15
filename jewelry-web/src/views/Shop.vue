<template>
  <div class="min-h-screen" style="background:var(--bg)">
    <!-- Header -->
    <header class="h-14 flex items-center justify-between px-6 border-b" style="background:var(--surface); border-color:var(--border)">
      <div class="flex items-center gap-2">
        <span class="text-lg font-bold" style="color:var(--accent)">💎 LUXE GEM</span>
        <span class="text-xs" style="color:var(--text-muted)">在线商城</span>
      </div>
      <div class="flex items-center gap-4">
        <template v-if="customer">
          <span class="text-sm" style="color:var(--text-primary)">👤 {{ customer.name }}</span>
          <button @click="showOrders = !showOrders" class="text-xs px-3 py-1 rounded-lg" style="color:var(--accent); border:1px solid var(--accent)">我的订单</button>
          <button @click="logout" class="text-xs" style="color:var(--text-muted)">退出</button>
        </template>
        <template v-else>
          <button @click="showLogin = true" class="text-xs px-3 py-1 rounded-lg" style="color:var(--text-primary)">登录</button>
          <button @click="showRegister = true" class="text-xs px-3 py-1 rounded-lg" style="background:var(--accent); color:#fff">注册</button>
        </template>
      </div>
    </header>

    <!-- Category tabs -->
    <div class="flex gap-2 px-6 py-3 overflow-x-auto" v-if="!showOrders">
      <button v-for="c in cats" :key="c.id" @click="selCat = selCat===c.id ? null : c.id; loadProducts()"
        class="px-3 py-1.5 rounded-full text-xs whitespace-nowrap transition-all"
        :style="selCat===c.id ? {background:'var(--accent)', color:'#fff'} : {background:'var(--surface)', color:'var(--text-secondary)', border:'1px solid var(--border)'}"
      >{{ c.name }}</button>
    </div>

    <!-- Orders list -->
    <div v-if="showOrders" class="px-6 py-4 space-y-2">
      <h3 class="text-sm font-bold" style="color:var(--text-primary)">我的订单</h3>
      <div v-if="orders.length===0" class="text-center py-10 text-sm" style="color:var(--text-muted)">暂无订单</div>
      <div v-for="o in orders" :key="o.id" class="p-3 rounded-xl border" style="background:var(--surface); border-color:var(--border)">
        <div class="flex justify-between text-sm"><span class="font-mono" style="color:var(--text-primary)">{{ o.orderNo }}</span><span class="font-bold" style="color:var(--accent)">¥{{ o.totalAmount }}</span></div>
        <div class="text-xs mt-1" style="color:var(--text-muted)">{{ o.status==='pending'?'待付款':o.status==='paid'?'已付款':o.status==='shipped'?'已发货':'已完成' }} · {{ o.createTime?.substring(0,16) }}</div>
      </div>
    </div>

    <!-- Products grid -->
    <div v-if="!showOrders" class="px-6 py-4 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="p in products" :key="p.id" class="rounded-xl overflow-hidden border transition-all hover:-translate-y-1 cursor-pointer"
        style="background:var(--surface); border-color:var(--border)" @click="addToCart(p)">
        <div class="h-40 flex items-center justify-center" style="background:var(--bg)">
          <img v-if="p.image" :src="'/images/'+p.image" class="w-full h-full object-cover" />
          <span v-else class="text-4xl opacity-20">💎</span>
        </div>
        <div class="p-3">
          <div class="text-sm font-medium truncate" style="color:var(--text-primary)">{{ p.name }}</div>
          <div class="flex justify-between items-center mt-1">
            <span class="text-sm font-bold" style="color:#ef4444">¥{{ p.price }}</span>
            <span class="text-[10px] px-1.5 py-0.5 rounded" style="background:var(--bg); color:var(--text-muted)">{{ p.categoryName }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Cart sidebar -->
    <transition name="slide"><div v-if="cart.length>0" class="fixed right-4 bottom-20 w-80 rounded-2xl shadow-2xl border overflow-hidden z-50" style="background:var(--surface-solid); border-color:var(--border)">
      <div class="p-3 border-b text-sm font-bold" style="border-color:var(--border); color:var(--text-primary)">购物车 ({{ cart.length }})</div>
      <div class="max-h-60 overflow-y-auto p-3 space-y-2">
        <div v-for="(item,i) in cart" :key="i" class="flex justify-between text-xs">
          <span style="color:var(--text-primary)">{{ item.name }} x{{ item.qty }}</span>
          <span style="color:var(--text-muted)">¥{{ (item.price*item.qty).toFixed(2) }} <button @click="cart.splice(i,1)" class="ml-1" style="color:#ef4444">✕</button></span>
        </div>
      </div>
      <div class="p-3 border-t flex justify-between items-center" style="border-color:var(--border)">
        <span class="text-sm font-bold" style="color:#ef4444">¥{{ total.toFixed(2) }}</span>
        <button @click="checkout" class="px-4 py-1.5 rounded-lg text-xs font-medium text-white" style="background:var(--accent)">结算</button>
      </div>
    </div></transition>

    <!-- Login Dialog -->
    <el-dialog v-model="showLogin" title="客户登录" width="360px"><div class="space-y-3">
      <el-input v-model="loginForm.phone" placeholder="手机号" /><el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
      <el-button type="primary" class="w-full" @click="doLogin" style="background:var(--accent)">登录</el-button>
      <p class="text-xs text-center" style="color:var(--text-muted)">没有账号？<a href="#" @click.prevent="showLogin=false;showRegister=true" style="color:var(--accent)">立即注册</a></p>
    </div></el-dialog>

    <!-- Register Dialog -->
    <el-dialog v-model="showRegister" title="客户注册" width="360px"><div class="space-y-3">
      <el-input v-model="regForm.name" placeholder="姓名" /><el-input v-model="regForm.phone" placeholder="手机号" />
      <el-input v-model="regForm.password" type="password" placeholder="密码（至少6位）" show-password />
      <el-input v-model="regForm.email" placeholder="邮箱（选填）" /><el-input v-model="regForm.address" placeholder="地址（选填）" />
      <el-button type="primary" class="w-full" @click="doRegister" :loading="regLoading" style="background:var(--accent)">注册并登录</el-button>
    </div></el-dialog>

    <!-- Checkout Dialog -->
    <el-dialog v-model="showCheckout" title="确认订单" width="400px"><div v-if="!customer" class="space-y-3">
      <p class="text-sm" style="color:var(--text-muted)">请先登录或填写联系方式</p>
      <el-input v-model="guestInfo.name" placeholder="姓名" /><el-input v-model="guestInfo.phone" placeholder="手机号" />
      <el-button type="primary" class="w-full" @click="placeGuestOrder" :loading="orderLoading" style="background:var(--accent)">提交订单</el-button>
    </div><div v-else class="space-y-2">
      <p class="text-sm" style="color:var(--text-muted)">{{ customer.name }} · {{ customer.phone }}</p>
      <div v-for="item in cart" :key="item.id" class="flex justify-between text-sm"><span style="color:var(--text-primary)">{{ item.name }} x{{ item.qty }}</span><span style="color:var(--text-muted)">¥{{ item.price }}</span></div>
      <div class="text-right text-lg font-bold pt-2 border-t" style="color:#ef4444; border-color:var(--border)">¥{{ total.toFixed(2) }}</div>
      <el-button type="primary" class="w-full mt-2" @click="placeOrder" :loading="orderLoading" style="background:var(--accent)">确认下单</el-button>
    </div></el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const products = ref([]); const cats = ref([]); const selCat = ref(null)
const cart = ref([]); const showOrders = ref(false); const orders = ref([])
const showLogin = ref(false); const showRegister = ref(false); const showCheckout = ref(false)
const customer = ref(null); const token = ref(''); const orderLoading = ref(false); const regLoading = ref(false)
const loginForm = reactive({ phone:'', password:'' })
const regForm = reactive({ name:'', phone:'', password:'', email:'', address:'' })
const guestInfo = reactive({ name:'', phone:'' })
const total = computed(()=>cart.value.reduce((s,i)=>s+i.price*i.qty,0))

onMounted(()=>{ loadCats(); loadProducts(); const t=localStorage.getItem('shop_token'); if(t){ token.value=t; loadProfile() } })
async function loadCats(){ try{ cats.value=(await request.get('/shop/categories')).data }catch{} }
async function loadProducts(){ try{ products.value=(await request.get('/shop/products',{params:{categoryId:selCat.value}})).data }catch{} }
function addToCart(p){ const ex=cart.value.find(i=>i.id===p.id); if(ex) ex.qty++; else cart.value.push({...p,qty:1}) }
async function doLogin(){ try{ const r=await request.post('/shop/login',null,{params:loginForm}); customer.value=r.data.customer; token.value=r.data.token; localStorage.setItem('shop_token',token.value); showLogin.value=false; ElMessage.success('登录成功') }catch{} }
async function doRegister(){ if(!regForm.name||!regForm.phone||!regForm.password) return; regLoading.value=true; try{ const r=await request.post('/shop/register',{...regForm}); ElMessage.success('注册成功'); Object.assign(loginForm,{phone:regForm.phone,password:regForm.password}); showRegister.value=false; await doLogin() }catch{} finally{regLoading.value=false} }
async function loadProfile(){ try{ const r=await request.get('/shop/profile',{headers:{Authorization:'Bearer '+token.value}}); customer.value=r.data }catch{ logout() } }
async function loadOrders(){ try{ const r=await request.get('/shop/products',{params:{}}); orders.value=[] }catch{} }
function logout(){ customer.value=null; token.value=''; localStorage.removeItem('shop_token') }
async function placeOrder(){ if(cart.value.length===0) return; orderLoading.value=true; try{ await request.post('/shop/order',{name:customer.value.name,phone:customer.value.phone,items:cart.value.map(i=>({productId:i.id,quantity:i.qty}))},{headers:{Authorization:'Bearer '+token.value}}); ElMessage.success('下单成功'); cart.value=[]; showCheckout.value=false }catch{} finally{orderLoading.value=false} }
async function placeGuestOrder(){ if(!guestInfo.name||!guestInfo.phone) return; orderLoading.value=true; try{ await request.post('/shop/order',{...guestInfo,items:cart.value.map(i=>({productId:i.id,quantity:i.qty}))}); ElMessage.success('下单成功'); cart.value=[]; showCheckout.value=false }catch{} finally{orderLoading.value=false} }
function checkout(){ if(!customer.value){ showLogin.value=true; return }; showCheckout.value=true }
</script>

<style scoped>
.slide-enter-active,.slide-leave-active{transition:all .3s ease}
.slide-enter-from,.slide-leave-to{opacity:0;transform:translateY(20px)}
</style>
