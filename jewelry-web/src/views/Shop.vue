<template>
  <div class="min-h-screen" style="background:var(--bg)">
    <!-- Header -->
    <header class="sticky top-0 z-50 h-14 flex items-center justify-between px-6 border-b backdrop-blur-xl" style="background:rgba(16,18,20,0.85); border-color:var(--border)">
      <div class="flex items-center gap-3">
        <span class="text-xl font-extrabold tracking-tight" style="background:linear-gradient(135deg,#a855f7,#6366f1); -webkit-background-clip:text; -webkit-text-fill-color:transparent">LUXE GEM</span>
        <span class="text-[10px] px-2 py-0.5 rounded-full font-medium" style="background:rgba(168,85,247,0.12); color:#c4b5fd">珠宝商城</span>
      </div>
      <div class="flex items-center gap-4">
        <template v-if="customer">
          <button @click="showOrders=!showOrders" class="text-xs px-3 py-1.5 rounded-lg transition-all hover:scale-105" :style="showOrders?{background:'var(--accent)',color:'#fff'}:{color:'var(--text-muted)'}">我的订单</button>
          <span class="text-sm font-medium" style="color:var(--text-primary)">{{ customer.name }}</span>
          <button @click="logout" class="text-xs" style="color:var(--text-muted)">退出</button>
        </template>
        <template v-else>
          <button @click="showLogin=true" class="text-xs px-4 py-1.5 rounded-lg font-medium transition-all hover:scale-105" style="color:var(--text-primary); border:1px solid var(--border)">登录</button>
          <button @click="showRegister=true" class="text-xs px-4 py-1.5 rounded-lg font-medium text-white transition-all hover:scale-105" style="background:var(--accent)">注册</button>
        </template>
      </div>
    </header>

    <!-- Hero Banner -->
    <div v-if="!showOrders" class="relative mx-6 mt-4 rounded-2xl overflow-hidden h-48 flex items-center" style="background:linear-gradient(135deg, rgba(168,85,247,0.15), rgba(99,102,241,0.1), rgba(16,18,20,0.5))">
      <div class="absolute right-0 top-0 w-1/2 h-full opacity-20" style="background:radial-gradient(circle at center, #a855f7, transparent 70%)" />
      <div class="relative z-10 px-8">
        <div class="text-[10px] font-medium px-2 py-0.5 rounded-full inline-block mb-2" style="background:rgba(168,85,247,0.2); color:#c4b5fd">✨ 新品上市</div>
        <h1 class="text-2xl font-extrabold tracking-tight" style="color:var(--text-primary)">精工珠宝 · 典藏之选</h1>
        <p class="text-sm mt-1" style="color:var(--text-muted)">每件作品皆为匠心之作，为您呈现永恒之美</p>
      </div>
    </div>

    <!-- Category pills -->
    <div class="flex gap-2 px-6 py-4 overflow-x-auto" v-if="!showOrders">
      <button @click="selCat=null;load()" class="px-4 py-2 rounded-full text-xs font-medium whitespace-nowrap transition-all hover:scale-105" :style="!selCat?{background:'var(--accent)',color:'#fff'}:{background:'var(--surface)',color:'var(--text-secondary)',border:'1px solid var(--border)'}">全部</button>
      <button v-for="c in cats" :key="c.id" @click="selCat=selCat===c.id?null:c.id;load()" class="px-4 py-2 rounded-full text-xs font-medium whitespace-nowrap transition-all hover:scale-105" :style="selCat===c.id?{background:'var(--accent)',color:'#fff'}:{background:'var(--surface)',color:'var(--text-secondary)',border:'1px solid var(--border)'}">{{ c.name }}</button>
    </div>

    <!-- Orders list -->
    <div v-if="showOrders" class="px-6 py-4 space-y-3 max-w-2xl mx-auto">
      <h3 class="text-lg font-bold" style="color:var(--text-primary)">我的订单</h3>
      <div v-if="orders.length===0" class="text-center py-16"><div class="text-5xl mb-3 opacity-20">📦</div><div class="text-sm" style="color:var(--text-muted)">暂无订单，去逛逛吧</div></div>
      <div v-for="o in orders" :key="o.id" class="p-4 rounded-2xl border transition-all hover:-translate-y-0.5" style="background:var(--surface); border-color:var(--border)">
        <div class="flex justify-between items-start"><div><span class="text-xs font-mono" style="color:var(--text-muted)">{{ o.orderNo }}</span><span class="ml-2 text-[10px] px-2 py-0.5 rounded-full font-medium" :style="o.status==='pending'?{background:'rgba(245,158,11,0.15)',color:'#f59e0b'}:o.status==='paid'?{background:'rgba(96,165,250,0.15)',color:'#60a5fa'}:{background:'rgba(52,211,153,0.15)',color:'#34d399'}">{{ o.status==='pending'?'待付款':o.status==='paid'?'已付款':o.status==='shipped'?'已发货':'已完成' }}</span></div><span class="text-lg font-bold" style="color:#ef4444">¥{{ o.totalAmount }}</span></div>
        <div class="flex items-center justify-between mt-3"><span class="text-xs" style="color:var(--text-muted)">{{ o.createTime?.substring(0,16) }}</span><button v-if="o.status==='pending'" @click="payOrder(o.id)" class="text-xs px-3 py-1 rounded-lg font-medium text-white transition-all hover:scale-105" style="background:linear-gradient(135deg,#f59e0b,#ef4444)">立即支付</button></div>
      </div>
    </div>

    <!-- Product Grid - Product Reveal Card style -->
    <div v-if="!showOrders" class="px-6 py-2 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="p in products" :key="p.id" class="group rounded-2xl overflow-hidden border transition-all duration-300 hover:-translate-y-1.5 hover:shadow-2xl cursor-pointer" style="background:var(--surface); border-color:var(--border)" @click="addToCart(p)">
        <!-- Image with overlay -->
        <div class="relative h-48 overflow-hidden" style="background:var(--bg)">
          <img v-if="p.image" :src="'/images/'+p.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
          <div v-else class="w-full h-full flex items-center justify-center text-5xl opacity-10">💎</div>
          <!-- Hover overlay -->
          <div class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center" style="background:rgba(0,0,0,0.4)">
            <span class="px-4 py-2 rounded-full text-xs font-bold text-white border border-white/40 backdrop-blur-sm">加入购物车</span>
          </div>
          <!-- Category badge -->
          <span class="absolute top-2 left-2 text-[10px] px-2 py-0.5 rounded-full backdrop-blur-sm" style="background:rgba(0,0,0,0.5); color:#fff">{{ p.categoryName }}</span>
          <!-- Discount badge -->
          <span v-if="p.costPrice" class="absolute top-2 right-2 text-[10px] px-2 py-0.5 rounded-full font-bold text-white" style="background:rgba(239,68,68,0.8)">热卖</span>
        </div>
        <!-- Info -->
        <div class="p-3 space-y-1">
          <h3 class="text-sm font-semibold truncate" style="color:var(--text-primary)">{{ p.name }}</h3>
          <p class="text-[11px] truncate" style="color:var(--text-muted)">{{ p.material }}</p>
          <div class="flex items-center justify-between pt-1">
            <span class="text-base font-extrabold" style="color:#ef4444">¥{{ p.price }}</span>
            <span v-if="p.costPrice" class="text-[10px]" style="color:var(--text-muted)">利润 ¥{{ (p.price-p.costPrice).toFixed(0) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating Cart -->
    <transition name="slide"><div v-if="cart.length" class="fixed right-6 bottom-24 w-80 rounded-2xl shadow-2xl border z-50 overflow-hidden" style="background:var(--surface-solid); border-color:var(--border)">
      <div class="px-4 py-3 border-b flex items-center justify-between" style="border-color:var(--border)"><span class="text-sm font-bold" style="color:var(--text-primary)">🛒 购物车 ({{ cart.length }})</span><button @click="cart=[]" class="text-[10px]" style="color:var(--text-muted)">清空</button></div>
      <div class="max-h-56 overflow-y-auto p-3 space-y-2">
        <div v-for="(item,i) in cart" :key="i" class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex-shrink-0 overflow-hidden" style="background:var(--bg)"><img v-if="item.image" :src="'/images/'+item.image" class="w-full h-full object-cover" /></div>
          <div class="flex-1 min-w-0"><div class="text-xs font-medium truncate" style="color:var(--text-primary)">{{ item.name }}</div><div class="flex items-center gap-2 mt-0.5"><button @click="item.qty>1?item.qty--:cart.splice(i,1)" class="text-xs w-5 h-5 rounded flex items-center justify-center" style="background:var(--bg); color:var(--text-muted)">−</button><span class="text-xs font-bold" style="color:var(--text-primary)">{{ item.qty }}</span><button @click="item.qty++" class="text-xs w-5 h-5 rounded flex items-center justify-center" style="background:var(--bg); color:var(--text-muted)">+</button></div></div>
          <span class="text-sm font-bold flex-shrink-0" style="color:#ef4444">¥{{ (item.price*item.qty).toFixed(2) }}</span>
        </div>
      </div>
      <div class="px-4 py-3 border-t flex items-center justify-between" style="border-color:var(--border)"><span class="font-extrabold text-lg" style="color:#ef4444">¥{{ total.toFixed(2) }}</span><button @click="checkout" class="px-5 py-2 rounded-xl text-sm font-bold text-white transition-all hover:scale-105" style="background:linear-gradient(135deg,#a855f7,#6366f1)">去结算</button></div>
    </div></transition>

    <!-- Dialogs (same as before) -->
    <el-dialog v-model="showLogin" title="客户登录" width="360px">
      <div class="space-y-3"><el-input v-model="loginForm.phone" placeholder="手机号" /><el-input v-model="loginForm.pwd" type="password" placeholder="密码" show-password /><el-button class="w-full !text-white" @click="doLogin" style="background:var(--accent)">登录</el-button></div>
    </el-dialog>
    <el-dialog v-model="showRegister" title="客户注册" width="360px">
      <div class="space-y-3"><el-input v-model="reg.name" placeholder="姓名" /><el-input v-model="reg.phone" placeholder="手机号" /><el-input v-model="reg.password" type="password" placeholder="密码(6位+)" show-password /><el-button class="w-full !text-white" @click="doRegister" style="background:var(--accent)">注册并登录</el-button></div>
    </el-dialog>
    <el-dialog v-model="showCheckout" title="确认订单" width="420px">
      <div v-if="customer"><p class="text-sm mb-3" style="color:var(--text-muted)">{{ customer.name }} · {{ customer.phone }}</p><div v-for="item in cart" :key="item.id" class="flex justify-between text-sm py-1"><span style="color:var(--text-primary)">{{ item.name }} x{{ item.qty }}</span><span style="color:var(--text-muted)">¥{{ item.price }}</span></div><div class="text-right font-extrabold text-xl pt-3 mt-3 border-t" style="color:#ef4444; border-color:var(--border)">¥{{ total.toFixed(2) }}</div><el-button class="w-full mt-4 !text-white !font-bold" size="large" @click="place" :loading="loading" style="background:linear-gradient(135deg,#a855f7,#6366f1)">确认下单</el-button></div>
      <div v-else class="space-y-3"><el-input v-model="guest.name" placeholder="姓名" /><el-input v-model="guest.phone" placeholder="手机号" /><el-button class="w-full !text-white" @click="guestOrder" style="background:var(--accent)">提交订单</el-button></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const products=ref([]),cats=ref([]),selCat=ref(null),cart=ref([]),orders=ref([]),showOrders=ref(false)
const customer=ref(null),token=ref(''),showLogin=ref(false),showRegister=ref(false),showCheckout=ref(false),loading=ref(false)
const loginForm=reactive({phone:'',pwd:''}),reg=reactive({name:'',phone:'',password:''}),guest=reactive({name:'',phone:''})
const total=computed(()=>cart.value.reduce((s,i)=>s+i.price*i.qty,0))

onMounted(()=>{load();request.get('/shop/categories').then(r=>cats.value=r.data);const t=localStorage.getItem('shop_token');if(t){token.value=t;loadProfile()}})
async function load(){products.value=(await request.get('/shop/products',{params:{categoryId:selCat.value}})).data}
async function loadOrders(){if(!token.value)return;try{orders.value=(await request.get('/shop/orders',{headers:{Authorization:'Bearer '+token.value}})).data}catch{}}
function addToCart(p){const e=cart.value.find(i=>i.id===p.id);e?e.qty++:cart.value.push({...p,qty:1});ElMessage.success('已加入购物车')}
async function doLogin(){const r=await request.post('/shop/login',null,{params:{phone:loginForm.phone,password:loginForm.pwd}});customer.value=r.data.customer;token.value=r.data.token;localStorage.setItem('shop_token',token.value);showLogin.value=false;ElMessage.success('登录成功')}
async function doRegister(){await request.post('/shop/register',{...reg});showRegister.value=false;loginForm.phone=reg.phone;loginForm.pwd=reg.password;await doLogin()}
async function loadProfile(){try{customer.value=(await request.get('/shop/profile',{headers:{Authorization:'Bearer '+token.value}})).data}catch{logout()}}
function logout(){customer.value=null;token.value='';localStorage.removeItem('shop_token');showOrders.value=false}
function checkout(){customer.value?showCheckout.value=true:showLogin.value=true}
async function place(){loading.value=true;const r=await request.post('/shop/order',{name:customer.value.name,phone:customer.value.phone,items:cart.value.map(i=>({productId:i.id,quantity:i.qty}))});ElMessage.success('下单成功，请支付');cart.value=[];showCheckout.value=false;loading.value=false}
async function guestOrder(){loading.value=true;await request.post('/shop/order',{...guest,items:cart.value.map(i=>({productId:i.id,quantity:i.qty}))});ElMessage.success('下单成功');cart.value=[];showCheckout.value=false;loading.value=false}
async function payOrder(id){await request.post('/shop/pay',{orderId:id});ElMessage.success('支付成功');loadOrders()}
</script>
<style scoped>.slide-enter-active,.slide-leave-active{transition:all .3s ease}.slide-enter-from,.slide-leave-to{opacity:0;transform:translateY(20px)}</style>
