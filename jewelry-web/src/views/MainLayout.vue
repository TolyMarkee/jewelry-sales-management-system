<template>
  <el-container class="h-full">
    <el-aside width="220px" class="bg-slate-800 overflow-y-auto">
      <div class="h-16 flex items-center justify-center text-white text-base border-b border-white/10">💎 珠宝销售管理</div>
      <el-menu :default-active="route.path" router background-color="#1e293b" text-color="#94a3b8" active-text-color="#60a5fa">
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>仪表盘</span></el-menu-item>
        <el-menu-item index="/product"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/category"><el-icon><Menu /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/order"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/customer"><el-icon><User /></el-icon><span>客户管理</span></el-menu-item>
        <el-menu-item index="/stock"><el-icon><Box /></el-icon><span>库存管理</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><TrendCharts /></el-icon><span>销售统计</span></el-menu-item>
        <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon><span>个人中心</span></el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/user"><el-icon><Setting /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="bg-white border-b border-gray-200 flex items-center justify-end h-[50px] px-5">
        <span class="text-gray-600 mr-5">{{ userStore.user?.realName || userStore.user?.username }}</span>
        <el-button type="danger" text @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main class="bg-gray-100 h-[calc(100vh-50px)] overflow-y-auto"><router-view /></el-main>
    </el-container>
    <AiAssistant />
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Goods, Menu, Tickets, User, Box, TrendCharts, UserFilled, Setting } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import AiAssistant from '@/components/AiAssistant.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>
