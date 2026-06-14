<template>
  <div class="flex items-center gap-2">
    <div
      class="relative flex items-center rounded-xl transition-all duration-500 ease-out overflow-hidden"
      :class="expanded ? 'w-56' : 'w-9'"
      :style="{background:'var(--input-bg)', border:'1px solid var(--border-strong)'}"
    >
      <button
        class="w-9 h-9 flex items-center justify-center flex-shrink-0 transition-colors"
        :class="expanded ? 'text-purple-400' : 'text-gray-500 hover:text-gray-300'"
        @click="toggle"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
      </button>
      <input
        ref="inputEl"
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        @keyup.enter="$emit('search')"
        @keyup.escape="collapse"
        :placeholder="placeholder"
        class="h-9 bg-transparent outline-none text-sm pr-3 transition-opacity duration-300"
        :class="expanded ? 'opacity-100 w-full' : 'opacity-0 w-0'"
        style="color:var(--text-primary)"
      />
    </div>
    <!-- Glow ring on expand -->
    <div v-if="expanded" class="absolute -inset-0.5 rounded-xl -z-10 blur opacity-30" style="background:linear-gradient(135deg,rgba(168,85,247,0.5),rgba(96,165,250,0.3))" />
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({ modelValue:String, placeholder:{type:String, default:'搜索...'} })
const emit = defineEmits(['update:modelValue','search'])

const expanded = ref(false)
const inputEl = ref(null)

function toggle() {
  expanded.value = !expanded.value
  if (expanded.value) nextTick(() => inputEl.value?.focus())
}
function collapse() { expanded.value = false }
</script>
