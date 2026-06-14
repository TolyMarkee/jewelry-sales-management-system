<template>
  <div class="w-full min-w-[200px] relative">
    <div class="relative w-full" @mouseenter="onEnter" @mouseleave="onLeave" @mousemove="onMove">
      <input
        :type="type || 'text'"
        :value="modelValue"
        :placeholder="placeholder"
        class="peer relative z-10 h-[52px] w-full rounded-md pl-4 pr-11 font-thin outline-none drop-shadow-sm transition-all duration-200 ease-in-out placeholder:font-medium"
        :style="{background:'var(--input-bg)', border:'2px solid var(--border-strong)', color:'var(--text-primary)'}"
        @input="$emit('update:modelValue', $event.target.value)"
        @keyup.enter="$emit('enter')"
      />
      <!-- Top border spotlight -->
      <div ref="topBar" class="absolute pointer-events-none top-0 left-0 right-0 h-[2px] z-20 rounded-t-md overflow-hidden" style="opacity:0" />
      <!-- Bottom border spotlight -->
      <div ref="bottomBar" class="absolute pointer-events-none bottom-0 left-0 right-0 h-[2px] z-20 rounded-b-md overflow-hidden" style="opacity:0" />
      <!-- Icon -->
      <div class="absolute right-3 top-1/2 -translate-y-1/2 z-20" style="color:var(--text-muted)">
        <svg v-if="icon === 'user'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({ modelValue: String, placeholder: String, icon: String, type: String })
defineEmits(['update:modelValue', 'enter'])

const topBar = ref(null)
const bottomBar = ref(null)

function onEnter() {
  topBar.value.style.transition = 'opacity 0.2s'
  topBar.value.style.opacity = '1'
  bottomBar.value.style.transition = 'opacity 0.2s'
  bottomBar.value.style.opacity = '1'
}

function onLeave() {
  topBar.value.style.opacity = '0'
  bottomBar.value.style.opacity = '0'
}

function onMove(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  const x = e.clientX - rect.left
  const grad = `radial-gradient(50px circle at ${x}px 0px, #C7D1DB 0%, transparent 80%)`
  topBar.value.style.background = grad
  bottomBar.value.style.background = `radial-gradient(50px circle at ${x}px 2px, #C7D1DB 0%, transparent 80%)`
}
</script>
