<template>
  <div class="fixed bottom-6 right-6 z-[9999]">
    <!-- Floating 3D Glowing Button -->
    <button
      class="floating-ai-btn relative w-16 h-16 rounded-full flex items-center justify-center transition-all duration-500"
      :class="visible ? 'rotate-90' : 'rotate-0'"
      :style="btnStyle"
      @click="visible = !visible"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    >
      <div class="absolute inset-0 rounded-full bg-gradient-to-b from-white/20 to-transparent opacity-30" />
      <div class="absolute inset-0 rounded-full border-2 border-white/10" />
      <span class="relative z-10 text-white text-2xl">{{ visible ? '✕' : '✦' }}</span>
      <div class="absolute inset-0 rounded-full animate-ping opacity-20 bg-indigo-500" />
    </button>

    <!-- Chat Panel -->
    <transition name="pop">
      <div v-if="visible" ref="chatRef" class="absolute bottom-20 right-0 w-[420px] max-w-[90vw] origin-bottom-right">
        <div class="relative flex flex-col rounded-3xl border shadow-2xl overflow-hidden"
          style="background:var(--surface-solid); border-color:var(--border-strong)">

          <!-- Header -->
          <div class="flex items-center justify-between px-5 pt-4 pb-2">
            <div class="flex items-center gap-1.5">
              <span class="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
              <span class="text-xs font-medium" style="color:var(--text-muted)">珠宝AI助手</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 text-[10px] font-medium rounded-2xl" style="background:rgba(168,85,247,0.15); color:#c4b5fd">DeepSeek Chat</span>
              <span class="text-[10px] font-medium" :style="{color: remaining <= 5 ? '#ef4444' : 'var(--text-muted)'}">剩余{{ remaining }}次</span>
              <button @click="clearHistory" class="p-1 rounded-full transition-colors text-[10px]" style="color:var(--text-muted)" title="清除历史">🗑</button>
              <button @click="visible = false" class="p-1 rounded-full transition-colors" style="color:var(--text-muted)" @mouseenter="(e) => e.target.style.background='rgba(255,255,255,0.05)'" @mouseleave="(e) => e.target.style.background='transparent'">✕</button>
            </div>
          </div>

          <!-- Messages -->
          <div ref="chatBody" class="px-5 pb-2 space-y-2 max-h-[200px] overflow-y-auto">
            <!-- Welcome -->
            <div v-if="messages.length===0" class="text-xs leading-relaxed" style="color:var(--text-muted)">
              您好！我是珠宝销售管理系统的AI助手，有什么可以帮您的？
            </div>
            <!-- Chat messages -->
            <div v-for="(msg, i) in messages" :key="i" :class="['flex', msg.role==='user'?'justify-end':'justify-start']">
              <div :class="msg.role==='user'
                ? 'max-w-[80%] px-3 py-1.5 rounded-2xl rounded-br-md text-xs text-white'
                : 'max-w-[80%] px-3 py-1.5 rounded-2xl rounded-bl-md text-xs'"
                :style="msg.role==='user'
                  ? {background:'linear-gradient(135deg,#6366f1,#a855f7)'}
                  : {background:'var(--bg)', color:'var(--text-primary)'}"
              v-html="formatMsg(msg.content)" />
            </div>
            <div v-if="loading" class="flex justify-start">
              <div class="px-3 py-1.5 rounded-2xl rounded-bl-md text-xs flex items-center gap-1" style="background:var(--bg); color:var(--text-muted)">
                <span class="dot-typing" />
              </div>
            </div>
          </div>

          <!-- Quick prompts -->
          <div class="px-5 pb-2 flex flex-wrap gap-1.5">
            <button v-for="q in quickQs" :key="q" @click="sendQuick(q)"
              class="px-2.5 py-1 text-[11px] rounded-lg transition-all hover:scale-105"
              style="background:rgba(168,85,247,0.08); color:#c4b5fd; border:1px solid rgba(168,85,247,0.12)"
            >{{ q }}</button>
          </div>

          <!-- Input area -->
          <textarea
            v-model="input"
            @keydown="onKeydown"
            rows="2"
            class="w-full px-5 py-2 bg-transparent border-none outline-none resize-none text-sm leading-relaxed"
            style="color:var(--text-primary); caret-color:var(--text-primary)"
            placeholder="输入问题..."
          />

          <!-- Controls -->
          <div class="px-4 pb-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-1 p-1 rounded-xl border" style="background:var(--bg); border-color:var(--border)">
                  <button v-for="btn in toolBtns" :key="btn.icon" class="group relative p-2 rounded-lg transition-all duration-300"
                    style="color:var(--text-muted)"
                    @mouseenter="(e) => { e.target.style.color=btn.hoverColor; e.target.style.background='rgba(255,255,255,0.05)' }"
                    @mouseleave="(e) => { e.target.style.color='var(--text-muted)'; e.target.style.background='transparent' }"
                  >{{ btn.icon }}</button>
                </div>
                <button class="group p-2.5 rounded-lg border transition-all" style="border-color:rgba(255,255,255,0.06); color:var(--text-muted)"
                  @mouseenter="(e) => { e.target.style.color='#f87171'; e.target.style.background='rgba(255,255,255,0.05)' }"
                  @mouseleave="(e) => { e.target.style.color='var(--text-muted)'; e.target.style.background='transparent' }"
                >🎤</button>
              </div>

              <div class="flex items-center gap-3">
                <span class="text-[10px] font-medium" style="color:var(--text-muted)">{{ input.length }}/2000</span>
                <button @click="send"
                  class="group relative p-3 rounded-xl border-none cursor-pointer transition-all duration-300 text-white shadow-lg hover:scale-110 active:scale-95"
                  style="background:linear-gradient(135deg, #ef4444, #dc2626); box-shadow:0 8px 20px rgba(239,68,68,0.3)"
                  @mouseenter="(e) => e.target.style.boxShadow='0 0 30px rgba(239,68,68,0.6)'"
                  @mouseleave="(e) => e.target.style.boxShadow='0 8px 20px rgba(239,68,68,0.3)'"
                >➤</button>
              </div>
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-between mt-3 pt-3 border-t text-[10px]" style="border-color:var(--border); color:var(--text-muted)">
              <span>Shift + Enter 换行</span>
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-green-500" /> 系统运行正常</span>
            </div>
          </div>

          <!-- Overlay gradient -->
          <div class="absolute inset-0 rounded-3xl pointer-events-none opacity-30"
            style="background:linear-gradient(135deg, rgba(239,68,68,0.03), transparent, rgba(168,85,247,0.04))" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const chatRef = ref(null)
const chatBody = ref(null)
const remaining = ref(30)
const quickQs = ['今日销售如何？', '库存预警？', '如何创建订单？']

async function fetchRemaining() {
  try { const r = await request.get('/ai/remaining'); remaining.value = r.data.remaining } catch {}
}
const hovering = ref(false)

const toolBtns = [
  { icon: '📎', hoverColor: '#94a3b8' },
  { icon: '🔗', hoverColor: '#f87171' },
  { icon: '📋', hoverColor: '#34d399' },
  { icon: '🎨', hoverColor: '#c4b5fd' }
]

const btnStyle = {
  background: 'linear-gradient(135deg, rgba(99,102,241,0.8) 0%, rgba(168,85,247,0.8) 100%)',
  boxShadow: '0 0 20px rgba(139,92,246,0.7), 0 0 40px rgba(124,58,237,0.5), 0 0 60px rgba(109,40,217,0.3)',
  border: '2px solid rgba(255,255,255,0.2)'
}

function sendQuick(text) { input.value = text; send() }
function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

const messages = ref([])

function formatMsg(text) {
  return text
    .replace(/。/g, '。<br>')
    .replace(/【([^】]+)】/g, '<strong style="color:var(--accent)">【$1】</strong>')
}

async function send() {
  const msg = input.value.trim()
  if (!msg || loading.value) return
  messages.value.push({ role: 'user', content: msg })
  input.value = ''
  loading.value = true
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  try {
    const res = await request.post('/ai/chat', { message: msg })
    messages.value.push({ role: 'assistant', content: res.data.reply || '抱歉，无法回答' })
    if (res.data.remaining !== undefined) remaining.value = res.data.remaining
  } catch {
    messages.value.push({ role: 'assistant', content: '网络异常，请稍后重试' })
  } finally {
    loading.value = false
    await nextTick()
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

// Click outside to close
function onClickOutside(e) {
  if (chatRef.value && !chatRef.value.contains(e.target) && !e.target.closest('.floating-ai-btn')) {
    visible.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', onClickOutside))
async function loadHistory() {
  try { const r = await request.get('/ai/history'); if (r.data && r.data.length) messages.value = r.data } catch {}
}
async function clearHistory() {
  try { await request.delete('/ai/history'); messages.value = []; ElMessage.success('历史已清除') } catch {}
}
watch(visible, (v) => { if (v) { fetchRemaining(); loadHistory() } })
</script>

<style scoped>
.pop-enter-active { animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.pop-leave-active { animation: popIn 0.15s ease-in reverse; }
@keyframes popIn {
  0% { opacity: 0; transform: scale(0.8) translateY(20px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
.floating-ai-btn:hover { transform: scale(1.1) rotate(5deg); }
.dot-typing { display: inline-flex; gap: 3px; padding: 2px 0; }
.dot-typing::before, .dot-typing::after, .dot-typing { content: ''; width: 5px; height: 5px; border-radius: 50%; background: #94a3b8; animation: blink 1.4s infinite both; display: inline-block; }
.dot-typing::before { animation-delay: 0s; }
.dot-typing { animation-delay: .2s; }
.dot-typing::after { animation-delay: .4s; }
@keyframes blink { 0%,80%,100% { opacity: 0 } 40% { opacity: 1 } }
</style>
