<template>
  <div class="ai-assistant">
    <!-- 浮动按钮 -->
    <div v-if="!visible" class="ai-float-btn" @click="toggleChat">
      <i class="el-icon-service" />
    </div>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-if="visible" class="ai-chat-panel">
        <div class="ai-chat-header">
          <span><i class="el-icon-cpu" /> AI 助手</span>
          <div>
            <el-button type="text" size="mini" icon="el-icon-minus" @click="toggleChat" style="color:#fff" />
            <el-button type="text" size="mini" icon="el-icon-close" @click="visible = false" style="color:#fff" />
          </div>
        </div>

        <div class="ai-chat-body" ref="chatBody">
          <div v-for="(msg, idx) in messages" :key="idx" :class="['msg-row', msg.role]">
            <div class="msg-avatar">
              <i v-if="msg.role === 'user'" class="el-icon-user" />
              <i v-else class="el-icon-cpu" />
            </div>
            <div class="msg-bubble" v-html="formatMsg(msg.content)" />
          </div>
          <div v-if="loading" class="msg-row assistant">
            <div class="msg-avatar"><i class="el-icon-cpu" /></div>
            <div class="msg-bubble typing"><span>.</span><span>.</span><span>.</span></div>
          </div>
        </div>

        <div class="ai-chat-footer">
          <el-input v-model="input" placeholder="输入问题..." size="small" @keyup.enter.native="send">
            <el-button slot="append" icon="el-icon-s-promotion" :disabled="!input.trim() || loading" @click="send" />
          </el-input>
          <div class="quick-actions">
            <el-tag size="mini" @click="sendQuick('今天销售额怎么样？')">今日销售</el-tag>
            <el-tag size="mini" @click="sendQuick('有哪些库存不足的商品？')">库存预警</el-tag>
            <el-tag size="mini" @click="sendQuick('如何创建订单？')">使用帮助</el-tag>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AiAssistant',
  data() {
    return {
      visible: false,
      input: '',
      loading: false,
      messages: [
        { role: 'assistant', content: '您好！我是珠宝销售管理系统的AI助手。我可以帮您解答系统使用问题、分析销售数据、提供珠宝知识建议等。试试问我吧！' }
      ]
    }
  },
  watch: {
    visible(val) {
      if (val) this.$nextTick(() => this.scrollBottom())
    }
  },
  methods: {
    toggleChat() {
      this.visible = !this.visible
    },
    async send() {
      const msg = this.input.trim()
      if (!msg || this.loading) return
      this.messages.push({ role: 'user', content: msg })
      this.input = ''
      this.loading = true
      this.$nextTick(() => this.scrollBottom())

      try {
        const res = await request.post('/ai/chat', { message: msg })
        const reply = res.data.reply || '抱歉，我暂时无法回答这个问题。'
        this.messages.push({ role: 'assistant', content: reply })
      } catch (e) {
        this.messages.push({ role: 'assistant', content: '网络异常，请稍后重试。' })
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollBottom())
      }
    },
    sendQuick(text) {
      this.input = text
      this.send()
    },
    scrollBottom() {
      const el = this.$refs.chatBody
      if (el) el.scrollTop = el.scrollHeight
    },
    formatMsg(text) {
      return text.replace(/\n/g, '<br>').replace(/`([^`]+)`/g, '<code>$1</code>')
    }
  }
}
</script>

<style scoped>
.ai-assistant { position: fixed; right: 24px; bottom: 24px; z-index: 9999; }

.ai-float-btn {
  width: 54px; height: 54px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 24px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: transform .3s;
}
.ai-float-btn:hover { transform: scale(1.1); }

.ai-chat-panel {
  width: 380px; height: 520px; background: #fff; border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.15); display: flex; flex-direction: column; overflow: hidden;
}

.ai-chat-header {
  height: 46px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff;
  display: flex; align-items: center; justify-content: space-between; padding: 0 12px; font-size: 15px;
  flex-shrink: 0;
}

.ai-chat-body {
  flex: 1; overflow-y: auto; padding: 12px; background: #f7f8fa;
}
.msg-row { display: flex; margin-bottom: 14px; }
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 14px; flex-shrink: 0;
}
.msg-row.user .msg-avatar { background: #e6f7ff; color: #1890ff; margin-left: 8px; }
.msg-row.assistant .msg-avatar { background: #f0f0f0; color: #667eea; margin-right: 8px; }
.msg-bubble {
  max-width: 260px; padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.6; word-break: break-word;
}
.msg-row.user .msg-bubble { background: #1890ff; color: #fff; border-top-right-radius: 4px; }
.msg-row.assistant .msg-bubble { background: #fff; color: #333; border-top-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }

.typing span {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #bbb; margin: 0 2px;
  animation: blink 1.4s infinite both;
}
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%,80%,100% { opacity: 0; } 40% { opacity: 1; } }

.ai-chat-footer {
  padding: 10px 12px; border-top: 1px solid #eee; flex-shrink: 0;
}
.quick-actions { margin-top: 8px; display: flex; gap: 6px; }
.quick-actions .el-tag { cursor: pointer; }
.quick-actions .el-tag:hover { background: #ecf5ff; color: #409EFF; }

.slide-up-enter-active, .slide-up-leave-active { transition: all .3s ease; }
.slide-up-enter, .slide-up-leave-to { opacity: 0; transform: translateY(20px); }
</style>
