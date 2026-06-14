<template>
  <span
    class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium select-none"
    :style="badgeStyle"
  >
    <span class="relative flex h-2 w-2">
      <span class="animate-ping absolute inline-flex h-full w-full rounded-full opacity-75" :style="{background: color}" />
      <span class="relative inline-flex rounded-full h-2 w-2" :style="{background: color}" />
    </span>
    {{ label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ status: String })

const statusMap = {
  pending:  { color: '#f59e0b', bg: 'rgba(245,158,11,0.12)', label: '待付款' },
  paid:     { color: '#60a5fa', bg: 'rgba(96,165,250,0.12)', label: '已付款' },
  shipped:  { color: '#a855f7', bg: 'rgba(168,85,247,0.12)', label: '已发货' },
  completed:{ color: '#34d399', bg: 'rgba(52,211,153,0.12)', label: '已完成' },
  cancelled:{ color: '#ef4444', bg: 'rgba(239,68,68,0.12)', label: '已取消' }
}

const color = computed(() => statusMap[props.status]?.color || '#94a3b8')
const label = computed(() => statusMap[props.status]?.label || props.status)
const badgeStyle = computed(() => {
  const s = statusMap[props.status] || { bg: 'rgba(148,163,184,0.1)' }
  return { background: s.bg, color: s.color }
})
</script>
