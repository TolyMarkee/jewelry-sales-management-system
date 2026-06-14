<template>
  <button
    @click="toggle"
    class="relative w-14 h-7 rounded-full transition-all duration-500 flex items-center px-1 overflow-hidden"
    :style="{ background: isDark ? '#2C333A' : '#e2e8f0' }"
    :title="isDark ? '切换到亮色模式' : '切换到暗色模式'"
  >
    <!-- Stars (visible in dark) -->
    <div class="absolute inset-0 transition-opacity duration-500" :class="isDark ? 'opacity-100' : 'opacity-0'">
      <span class="absolute top-1 left-2 text-[8px]">✦</span>
      <span class="absolute top-2 right-3 text-[6px]">·</span>
      <span class="absolute bottom-1 left-3 text-[5px]">✦</span>
    </div>
    <!-- Clouds (visible in light) -->
    <div class="absolute inset-0 transition-opacity duration-500" :class="!isDark ? 'opacity-100' : 'opacity-0'">
      <span class="absolute top-1 right-2 text-[8px]">☁</span>
    </div>
    <!-- Toggle knob -->
    <div
      class="relative z-10 w-5 h-5 rounded-full flex items-center justify-center transition-all duration-500 shadow-md"
      :style="{ transform: isDark ? 'translateX(28px)' : 'translateX(0)', background: isDark ? '#a855f7' : '#f59e0b' }"
    >
      <!-- Sun/moon icon -->
      <svg v-if="isDark" xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="1"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="2"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
    </div>
  </button>
</template>

<script setup>
import { ref, watch } from 'vue'

const isDark = ref(localStorage.getItem('theme') !== 'light')

function toggle() {
  isDark.value = !isDark.value
  applyTheme()
}

function applyTheme() {
  const root = document.documentElement
  if (isDark.value) {
    root.classList.add('dark')
    root.classList.remove('light')
    localStorage.setItem('theme', 'dark')
  } else {
    root.classList.add('light')
    root.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}

applyTheme()
</script>
