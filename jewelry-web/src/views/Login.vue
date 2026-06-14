<template>
  <div class="flex h-screen w-full bg-slate-950 overflow-hidden">
    <!-- Left: Form -->
    <div
      class="relative w-full lg:w-1/2 flex items-center justify-center p-8"
      @mousemove="onMouseMove"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    >
      <div
        class="absolute pointer-events-none w-[500px] h-[500px] rounded-full blur-3xl transition-opacity duration-300"
        :class="hovering ? 'opacity-100' : 'opacity-0'"
        :style="spotStyle"
      />

      <div class="relative z-10 w-full max-w-sm">
        <div class="text-center mb-8">
          <div class="text-4xl mb-2">💎</div>
          <h1 class="text-3xl font-extrabold text-white">珠宝首饰销售管理</h1>
          <p class="text-sm text-slate-400 mt-2">登录你的账户以继续</p>
        </div>

        <div class="flex justify-center gap-3 mb-6">
          <a v-for="i in 3" :key="i"
            class="w-10 h-10 rounded-full border border-slate-700 flex items-center justify-center
                   text-slate-500 hover:text-white hover:border-white transition-all duration-300"
            href="#">
            <el-icon :size="18"><User /></el-icon>
          </a>
        </div>

        <p class="text-center text-xs text-slate-600 mb-6">或使用账号密码登录</p>

        <div class="space-y-5">
          <div class="relative">
            <div class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 z-10">
              <el-icon :size="18"><User /></el-icon>
            </div>
            <input
              v-model="form.username"
              class="w-full h-12 pl-10 pr-4 bg-white/5 border-2 border-white/10 rounded-lg
                     text-white placeholder-slate-500 outline-none transition-all duration-300
                     focus:border-blue-500 focus:bg-white/10"
              placeholder="用户名"
              autocomplete="username"
            />
          </div>

          <div class="relative">
            <div class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 z-10">
              <el-icon :size="18"><Lock /></el-icon>
            </div>
            <input
              v-model="form.password"
              type="password"
              class="w-full h-12 pl-10 pr-4 bg-white/5 border-2 border-white/10 rounded-lg
                     text-white placeholder-slate-500 outline-none transition-all duration-300
                     focus:border-blue-500 focus:bg-white/10"
              placeholder="密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </div>

          <a href="#" class="block text-right text-xs text-slate-500 hover:text-blue-400 transition">忘记密码？</a>

          <button
            :disabled="loading"
            class="group/btn relative w-full h-12 overflow-hidden rounded-lg bg-blue-600
                   text-white font-semibold transition-all duration-300 hover:scale-[1.02]
                   hover:shadow-lg hover:shadow-blue-500/25 disabled:opacity-60"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else><el-icon class="is-loading"><Loading /></el-icon> 验证中...</span>
            <div class="absolute inset-0 flex h-full w-full justify-center
                        [transform:skew(-13deg)_translateX(-100%)]
                        group-hover:duration-1000
                        group-hover:[transform:skew(-13deg)_translateX(100%)]">
              <div class="relative h-full w-8 bg-white/20" />
            </div>
          </button>
        </div>

        <p class="text-center text-xs text-slate-600 mt-6">默认账号 admin / 123456</p>
      </div>
    </div>

    <!-- Right: Image -->
    <div class="hidden lg:block w-1/2 h-full overflow-hidden">
      <img
        src="https://images.pexels.com/photos/1457801/pexels-photo-1457801.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"
        class="w-full h-full object-cover opacity-40"
        alt="Jewelry"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api'
import { User, Lock, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

const mouse = ref({ x: 0, y: 0 })
const hovering = ref(false)
const spotStyle = computed(() => ({
  background: 'radial-gradient(circle, rgba(147,197,253,0.15), rgba(147,197,253,0.03), transparent 70%)',
  transform: `translate(${mouse.value.x - 250}px, ${mouse.value.y - 250}px)`,
  transition: 'transform 0.15s ease-out'
}))

function onMouseMove(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  mouse.value = { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

async function handleLogin() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    userStore.setToken(res.data.token)
    userStore.setUser(res.data.user)
    router.push('/')
  } catch {
    loading.value = false
  }
}
</script>
