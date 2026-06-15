<template>
  <div class="flex h-screen w-full overflow-hidden" :style="{background: isLight ? 'linear-gradient(135deg, #dde1e7 0%, #c8cdd5 50%, #bcc2cc 100%)' : 'var(--bg)'}">
    <!-- Left: Form area -->
    <div
      class="relative w-full lg:w-1/2 flex items-center justify-center p-4 lg:p-12"
      @mousemove="onMouseMove"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    >
      <div
        class="absolute pointer-events-none w-[500px] h-[500px] rounded-full blur-3xl transition-opacity duration-300"
        :class="hovering ? 'opacity-100' : 'opacity-0'"
        :style="spotlightStyle"
      />
      <div class="relative z-10 w-full max-w-sm">
        <div class="text-center mb-6">
          <!-- Illustration with purple glow, inspired by mission-success-dialog -->
          <!-- Animated illustration -->
          <div class="mx-auto mb-6 w-36 h-36 flex items-center justify-center relative">
            <!-- Pulsing outer glow -->
            <div class="absolute inset-0 rounded-full animate-pulse" style="background: radial-gradient(circle, rgba(168,85,247,0.5), rgba(236,72,153,0.2), transparent 70%); animation-duration: 3s" />
            <!-- Orbiting sparkle ring -->
            <div class="absolute inset-0 animate-spin" style="animation-duration: 12s">
              <div class="absolute top-0 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full bg-yellow-300 shadow-[0_0_6px_#facc15]" />
              <div class="absolute bottom-1 right-3 w-1 h-1 rounded-full bg-purple-300 shadow-[0_0_6px_#c4b5fd]" style="animation-delay: 4s" />
              <div class="absolute bottom-2 left-2 w-1.5 h-1.5 rounded-full bg-pink-300 shadow-[0_0_8px_#f9a8d4]" style="animation-delay: 8s" />
            </div>
            <!-- Gradient border ring with soft rotation -->
            <div class="relative w-28 h-28 rounded-full p-[3px] animate-spin shadow-[0_0_40px_rgba(168,85,247,0.35)]" style="animation-duration: 8s; background: conic-gradient(from 0deg, #a855f7, #ec4899, #f59e0b, #a855f7)">
              <!-- Inner circle (counter-rotates to keep image stable) -->
              <div class="w-full h-full rounded-full overflow-hidden bg-[#101214] flex items-center justify-center animate-spin" style="animation-duration: 8s; animation-direction: reverse">
                <img
                  src="https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=200&h=200&fit=crop&auto=format"
                  class="w-full h-full object-cover rounded-full opacity-90"
                  alt="Diamond ring"
                />
              </div>
            </div>
          </div>
          <h2 class="mb-1 flex items-center justify-center gap-2 text-2xl font-bold" style="color:var(--text-primary)">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="#facc15" stroke="#facc15" stroke-width="1.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            珠宝首饰销售管理
          </h2>
          <p class="text-sm" style="color:var(--text-muted)">登录你的账户以继续</p>
        </div>
        <div class="flex justify-center gap-3 md:gap-4 mb-4">
          <a v-for="(s, i) in socialIcons" :key="i" href="#" class="w-10 h-10 md:w-12 md:h-12 rounded-full flex justify-center items-center relative z-[1] overflow-hidden group border-2" style="background:var(--input-bg); border-color:var(--text-primary)">
            <div class="absolute inset-0 w-full h-full scale-y-0 origin-bottom transition-transform duration-500 ease-in-out group-hover:scale-y-100" style="background:var(--text-primary)" />
            <span class="text-lg relative z-[2] transition-all duration-500 ease-in-out group-hover:text-[#161A1D]" style="color:var(--text-primary)" v-html="s" />
          </a>
        </div>
        <p class="text-center text-xs mb-6" style="color:var(--text-muted)">或使用账号密码登录</p>
        <div class="space-y-4">
          <AppInput v-model="form.username" placeholder="用户名" icon="user" @enter="handleLogin" />
          <AppInput v-model="form.password" placeholder="密码" icon="lock" type="password" @enter="handleLogin" />
        </div>
        <a href="#" class="text-right text-xs mt-2 block hover:underline" style="color:var(--text-muted)">忘记密码？</a>
        <div class="flex justify-center mt-5">
          <button :disabled="loading" class="group/button relative inline-flex justify-center items-center overflow-hidden rounded-md px-4 py-2 text-sm font-medium text-white transition-all duration-300 ease-in-out hover:scale-105 hover:shadow-lg cursor-pointer disabled:opacity-60 !bg-[#0f172a] hover:!bg-[#020617] shadow-[0_4px_24px_rgba(15,23,42,0.4)]" @click="isRegister ? handleRegister() : handleLogin()">
            <span class="text-sm px-3 py-1">{{ loading ? '处理中...' : (isRegister ? '注 册' : '登 录') }}</span>
            <div class="absolute inset-0 flex h-full w-full justify-center [transform:skew(-13deg)_translateX(-100%)] group-hover/button:duration-1000 group-hover/button:[transform:skew(-13deg)_translateX(100%)]">
              <div class="relative h-full w-8 bg-white/20" />
            </div>
          </button>
        </div>
        <div v-if="isRegister" class="mb-4">
          <div class="relative">
            <div class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 z-10">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <input v-model="regForm.realName" class="w-full h-12 pl-10 pr-4 bg-white/5 border-2 border-white/10 rounded-lg text-white placeholder-slate-500 outline-none transition-all duration-300 focus:border-blue-500 focus:bg-white/10" placeholder="真实姓名" />
          </div>
        </div>
        <p class="text-center text-xs mt-4" style="color:var(--text-muted)">
          <template v-if="isRegister">已有账号？<a href="#" @click.prevent="isRegister=false" style="color:var(--accent)">立即登录</a></template>
          <template v-else>没有账号？<a href="#" @click.prevent="isRegister=true" style="color:var(--accent)">立即注册</a> · 默认 admin / 123456</template>
        </p>
      </div>
    </div>
    <!-- Right: Image -->
    <div class="hidden lg:block w-1/2 h-full overflow-hidden">
      <img src="https://images.pexels.com/photos/1457801/pexels-photo-1457801.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2" class="w-full h-full object-cover transition-transform duration-300 opacity-30" alt="Jewelry" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AppInput from '@/components/AppInput.vue'

const router = useRouter()
const userStore = useUserStore()
const isLight = computed(() => document.documentElement.classList.contains('light'))
const isRegister = ref(false)
const form = reactive({ username: '', password: '' })
const regForm = reactive({ realName: '' })
const loading = ref(false)
const mouse = ref({ x: 0, y: 0 })
const hovering = ref(false)

const spotlightStyle = computed(() => ({
  background: 'radial-gradient(circle, rgba(168,85,247,0.15), rgba(96,165,250,0.08), rgba(244,114,182,0.05), transparent 70%)',
  transform: `translate(${mouse.value.x - 250}px, ${mouse.value.y - 250}px)`,
  transition: 'transform 0.1s ease-out'
}))

const socialIcons = [
  '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"><path fill="currentColor" d="M7.8 2h8.4C19.4 2 22 4.6 22 7.8v8.4a5.8 5.8 0 0 1-5.8 5.8H7.8C4.6 22 2 19.4 2 16.2V7.8A5.8 5.8 0 0 1 7.8 2m-.2 2A3.6 3.6 0 0 0 4 7.6v8.8C4 18.39 5.61 20 7.6 20h8.8a3.6 3.6 0 0 0 3.6-3.6V7.6C20 5.61 18.39 4 16.4 4zm9.65 1.5a1.25 1.25 0 0 1 1.25 1.25A1.25 1.25 0 0 1 17.25 8A1.25 1.25 0 0 1 16 6.75a1.25 1.25 0 0 1 1.25-1.25M12 7a5 5 0 0 1 5 5a5 5 0 0 1-5 5a5 5 0 0 1-5-5a5 5 0 0 1 5-5m0 2a3 3 0 0 0-3 3a3 3 0 0 0 3 3a3 3 0 0 0 3-3a3 3 0 0 0-3-3"/></svg>',
  '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"><path fill="currentColor" d="M6.94 5a2 2 0 1 1-4-.002a2 2 0 0 1 4 .002M7 8.48H3V21h4zm6.32 0H9.34V21h3.94v-6.57c0-3.66 4.77-4 4.77 0V21H22v-7.93c0-6.17-7.06-5.94-8.72-2.91z"/></svg>',
  '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"><path fill="currentColor" d="M9.198 21.5h4v-8.01h3.604l.396-3.98h-4V7.5a1 1 0 0 1 1-1h3v-4h-3a5 5 0 0 0-5 5v2.01h-2l-.396 3.98h2.396z"/></svg>'
]

function onMouseMove(e) { const r = e.currentTarget.getBoundingClientRect(); mouse.value = { x: e.clientX - r.left, y: e.clientY - r.top } }
async function handleLogin() { if (!form.username || !form.password) return; loading.value = true; try { const res = await login({ username: form.username, password: form.password }); userStore.setToken(res.data.token); userStore.setUser(res.data.user); router.push('/') } catch { loading.value = false } }
async function handleRegister() { if (!form.username || !form.password) return; loading.value = true; try { await request.post('/register', { username: form.username, password: form.password, realName: regForm.realName }); ElMessage.success('注册成功，请登录'); isRegister.value = false; regForm.realName = ''; form.password = '' } catch {} finally { loading.value = false } }
</script>
