<template>
  <div class="relative">
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      @keyup.enter="$emit('search')"
      @focus="onFocus"
      @blur="onBlur"
      class="h-9 px-3 pr-10 rounded-xl text-sm outline-none w-full transition-all duration-300"
      :style="{background:'var(--input-bg)', border:'1px solid var(--border-strong)', color:'var(--text-primary)'}"
      :placeholder="focused ? '' : currentHint"
    />
    <div class="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-1.5" style="color:#596773">
      <svg v-if="!focused" class="transition-opacity" :class="hintFading ? 'opacity-0' : 'opacity-100'" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
      <span class="text-[10px] transition-opacity duration-500" :class="focused ? 'opacity-0' : 'opacity-50'" style="color:var(--text-muted)">⌘K</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({ modelValue:String, hints:{type:Array, default:()=>['搜索商品名称...','按材质筛选...','输入关键词搜索...']} })
defineEmits(['update:modelValue','search'])

const focused = ref(false)
const hintIdx = ref(0)
const hintFading = ref(false)
const currentHint = ref(props.hints[0])
let interval = null

onMounted(() => {
  interval = setInterval(() => {
    hintFading.value = true
    setTimeout(() => {
      hintIdx.value = (hintIdx.value + 1) % props.hints.length
      currentHint.value = props.hints[hintIdx.value]
      hintFading.value = false
    }, 300)
  }, 3000)
})

onUnmounted(() => clearInterval(interval))

function onFocus() { focused.value = true }
function onBlur() { focused.value = false }
</script>
