<template>
  <div class="flex h-screen" style="background:var(--bg)">
    <!-- Glass sidebar -->
    <aside class="w-60 flex-shrink-0 flex flex-col border-r" style="background:var(--surface); backdrop-filter:blur(24px); border-color:var(--border)">
      <!-- Logo -->
      <div class="h-14 flex items-center gap-3 px-5 border-b" style="border-color:var(--border)">
        <div class="relative">
          <div class="absolute -inset-1 rounded-lg blur opacity-60" style="background:linear-gradient(135deg,#a855f7,#6366f1)" />
          <div class="relative w-9 h-9 rounded-lg flex items-center justify-center" style="background:linear-gradient(135deg,#a855f7,#6366f1)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          </div>
        </div>
        <div>
          <div class="text-sm font-bold tracking-wide" style="color:var(--text-primary)">LUXE GEM</div>
          <div class="text-[10px] -mt-0.5 font-medium tracking-wider" style="background:linear-gradient(135deg,#a855f7,#6366f1); -webkit-background-clip:text; -webkit-text-fill-color:transparent">MANAGEMENT</div>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <button
          v-for="item in menuItems"
          :key="item.path"
          @click="router.push(item.path)"
          class="group relative w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-300"
          :style="{color: isActive(item.path) ? 'var(--text-primary)' : 'var(--text-muted)'}"
          @mouseenter="(e) => { if (!isActive(item.path)) e.target.style.color = 'var(--text-primary)' }"
          @mouseleave="(e) => { if (!isActive(item.path)) e.target.style.color = 'var(--text-muted)' }"
        >
          <!-- Active glow background -->
          <div
            v-if="isActive(item.path)"
            class="absolute inset-0 rounded-xl opacity-40"
            style="background:linear-gradient(135deg,rgba(168,85,247,0.3),rgba(99,102,241,0.2))"
          />
          <!-- Hover background -->
          <div class="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity bg-white/[0.04]" />
          <!-- Icon -->
          <component :is="item.icon" class="w-5 h-5 relative z-10 flex-shrink-0" />
          <!-- Label -->
          <span class="relative z-10 truncate">{{ item.label }}</span>
        </button>
      </nav>

      <!-- Theme + User footer -->
      <div class="px-3 py-3 border-t space-y-2" style="border-color:var(--border)">
        <div class="flex items-center justify-between px-3">
          <span class="text-[10px] font-medium" style="color:var(--text-muted)">主题切换</span>
          <ThemeToggle />
        </div>
        <div class="flex items-center gap-3 px-3 py-2">
          <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white" style="background:linear-gradient(135deg,#a855f7,#6366f1)">
            {{ (userStore.user?.realName || userStore.user?.username || '?')[0] }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-xs font-medium truncate" style="color:var(--text-primary)">{{ userStore.user?.realName }}</div>
            <div class="text-[10px] truncate" style="color:var(--text-muted)">{{ userStore.user?.username }}</div>
          </div>
          <button @click="handleLogout" class="w-7 h-7 rounded-lg flex items-center justify-center text-gray-500 hover:text-red-400 hover:bg-red-400/10 transition-all" title="退出登录">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-12 flex items-center justify-end px-6 border-b flex-shrink-0" style="background:var(--surface); backdrop-filter:blur(16px); border-color:var(--border)">
        <div class="flex items-center gap-3 text-xs" style="color:#596773">
          <span class="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
          系统运行中
        </div>
      </header>
      <main class="flex-1 overflow-y-auto p-5" style="background:var(--bg)">
        <router-view />
      </main>
    </div>

    <AiAssistant />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import AiAssistant from '@/components/AiAssistant.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { HomeFilled, Goods, Menu, Tickets, User, Box, TrendCharts, UserFilled, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: HomeFilled },
  { path: '/product', label: '商品管理', icon: Goods },
  { path: '/category', label: '分类管理', icon: Menu },
  { path: '/order', label: '订单管理', icon: Tickets },
  { path: '/customer', label: '客户管理', icon: User },
  { path: '/stock', label: '库存管理', icon: Box },
  { path: '/statistics', label: '销售统计', icon: TrendCharts },
  { path: '/profile', label: '个人中心', icon: UserFilled },
  { path: '/user', label: '用户管理', icon: Setting, admin: true }
]

const visibleItems = computed(() => menuItems.filter(i => !i.admin || userStore.isManagerOrAdmin))

function isActive(path) { return route.path === path }

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>
