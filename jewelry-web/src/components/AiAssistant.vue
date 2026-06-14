<template>
  <div class="fixed right-6 bottom-6 z-[9999]">
    <div v-if="!visible" class="w-14 h-14 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-white text-2xl cursor-pointer flex items-center justify-center shadow-lg shadow-indigo-500/40 hover:scale-110 transition-transform" @click="visible = true">
      <el-icon :size="24"><Service /></el-icon>
    </div>

    <div v-if="visible" class="w-[380px] h-[520px] bg-white rounded-xl shadow-2xl flex flex-col overflow-hidden">
      <div class="h-12 bg-gradient-to-r from-indigo-500 to-purple-600 text-white flex items-center justify-between px-3 text-sm flex-shrink-0">
        <span><el-icon><Cpu /></el-icon> AI 助手</span>
        <div>
          <el-button text size="small" class="!text-white" @click="visible = false"><el-icon><Minus /></el-icon></el-button>
        </div>
      </div>

      <div ref="chatBody" class="flex-1 overflow-y-auto p-3 bg-gray-50 space-y-3">
        <div v-for="(msg, i) in messages" :key="i" :class="['flex', msg.role === 'user' ? 'flex-row-reverse' : '']">
          <div :class="msg.role === 'user' ? 'bg-blue-50 text-blue-500 ml-2' : 'bg-gray-100 text-gray-500 mr-2'" class="w-8 h-8 rounded-full flex items-center justify-center text-xs flex-shrink-0">
            <el-icon :size="14"><User v-if="msg.role === 'user'" /><Cpu v-else /></el-icon>
          </div>
          <div :class="msg.role === 'user' ? 'bg-blue-500 text-white rounded-tr-none' : 'bg-white text-gray-700 rounded-tl-none shadow-sm'" class="max-w-[260px] px-3 py-2 rounded-xl text-sm leading-relaxed break-words" v-html="formatMsg(msg.content)" />
        </div>
        <div v-if="loading" class="flex">
          <div class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center mr-2 flex-shrink-0"><el-icon :size="14"><Cpu /></el-icon></div>
          <div class="bg-white px-4 py-2 rounded-xl rounded-tl-none shadow-sm"><span class="dot-typing" /></div>
        </div>
      </div>

      <div class="p-3 border-t flex-shrink-0">
        <el-input v-model="input" placeholder="输入问题..." size="small" @keyup.enter="send">
          <template #append><el-button :disabled="!input.trim() || loading" @click="send"><el-icon><Promotion /></el-icon></el-button></template>
        </el-input>
        <div class="flex gap-2 mt-2">
          <el-tag v-for="q in quickQs" :key="q" size="small" class="cursor-pointer hover:bg-blue-50 hover:text-blue-500" @click="sendQuick(q)">{{ q }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Service, Cpu, Minus, User, Promotion } from '@element-plus/icons-vue'
import request from '@/utils/request'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const chatBody = ref(null)
const messages = ref([{ role: 'assistant', content: '您好！我是珠宝销售管理系统的AI助手，有什么可以帮您的？' }])
const quickQs = ['今日销售如何？', '库存预警？', '如何创建订单？']

function formatMsg(text) { return text.replace(/\n/g, '<br>').replace(/`([^`]+)`/g, '<code>$1</code>') }

async function send() {
  const msg = input.value.trim()
  if (!msg || loading.value) return
  messages.value.push({ role: 'user', content: msg })
  input.value = ''
  loading.value = true
  await nextTick()
  chatBody.value.scrollTop = chatBody.value.scrollHeight
  try {
    const res = await request.post('/ai/chat', { message: msg })
    messages.value.push({ role: 'assistant', content: res.data.reply || '抱歉，无法回答' })
  } catch {
    messages.value.push({ role: 'assistant', content: '网络异常，请稍后重试' })
  } finally {
    loading.value = false
    await nextTick()
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

function sendQuick(text) { input.value = text; send() }
</script>

<style scoped>
.dot-typing { display: inline-flex; gap: 4px; padding: 4px 0; }
.dot-typing::before, .dot-typing::after, .dot-typing { content: ''; width: 6px; height: 6px; border-radius: 50%; background: #bbb; animation: dot-blink 1.4s infinite both; display: inline-block; }
.dot-typing::before { animation-delay: 0s; content: ''; }
.dot-typing { animation-delay: .2s; }
.dot-typing::after { animation-delay: .4s; content: ''; }
@keyframes dot-blink { 0%,80%,100% { opacity: 0 } 40% { opacity: 1 } }
</style>
