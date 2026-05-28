<template>
  <div class="msg-page">
    <div class="msg-card">
      <h2>💬 消息</h2>
      <div v-if="list.length === 0" class="empty">暂无消息</div>
      <div v-for="c in list" :key="c.id" class="conv-item" @click="openConv(c.otherId, c.otherName)">
        <img v-if="c.otherAvatar" :src="c.otherAvatar" class="conv-avatar" />
        <div v-else class="conv-avatar-placeholder">{{ c.otherName ? c.otherName[0] : '?' }}</div>
        <div class="conv-info">
          <div class="conv-name">{{ c.otherName || '用户' }}</div>
          <div class="conv-preview">{{ c.content }}</div>
        </div>
        <span class="conv-time">{{ formatTime(c.createTime) }}</span>
      </div>
    </div>

    <!-- 聊天弹窗 -->
    <div class="chat-overlay" v-if="showChat" @click.self="closeChat">
      <div class="chat-modal">
        <div class="chat-header">
          <span>💬 与 {{ chatUser }} 聊天中</span>
          <button @click="closeChat" class="chat-close">✕</button>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="msg in messages" :key="msg.id" class="chat-msg" :class="{ mine: msg.senderId == userId }">
            <img v-if="msg.senderAvatar" :src="msg.senderAvatar" class="msg-avatar" />
            <div v-else class="msg-avatar-placeholder">{{ msg.senderName ? msg.senderName[0] : '?' }}</div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
        <div class="chat-footer">
          <div class="chat-input-wrap">
            <input v-model="chatInput" placeholder="输入消息..." @keyup.enter="sendMsg" class="chat-input" />
            <button @click="showChatEmoji = !showChatEmoji" class="chat-emoji-btn">😊</button>
            <EmojiPicker :show="showChatEmoji" @pick="e => { chatInput += e; showChatEmoji = false }" @close="showChatEmoji = false" />
          </div>
          <button @click="sendMsg" class="chat-send">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import EmojiPicker from '../components/EmojiPicker.vue'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'

const userId = Number(localStorage.getItem('userId'))
const list = ref([])
const showChat = ref(false)
const showChatEmoji = ref(false)
const messages = ref([])
const chatInput = ref('')
const chatBody = ref(null)
const chatUser = ref('')
let chatOtherId = null
let convTimer = null
let chatTimer = null

onMounted(() => {
  loadConversations()
  convTimer = setInterval(loadConversations, 5000)
})

onUnmounted(() => {
  if (convTimer) clearInterval(convTimer)
  if (chatTimer) clearInterval(chatTimer)
})

const loadConversations = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/message/conversations?userId=${userId}`)
    const raw = res.data || []
    list.value = raw.map(m => ({
      ...m,
      otherId: m.senderId === userId ? m.receiverId : m.senderId,
      otherName: m.senderId === userId ? '' : (m.senderName || ''),
      otherAvatar: m.senderId === userId ? '' : (m.senderAvatar || '')
    }))
    for (const item of list.value) {
      if (!item.otherName) {
        try {
          const u = await axios.get(`http://localhost:8080/user/info/${item.otherId}`)
          item.otherName = u.data.username || '用户'
          item.otherAvatar = u.data.avatar || ''
        } catch (_) {}
      }
    }
  } catch (e) { console.error(e) }
}

const openConv = async (otherId, otherName) => {
  chatOtherId = otherId
  chatUser.value = otherName
  showChat.value = true
  await loadChatMessages()
  await axios.post(`http://localhost:8080/message/read?user1=${userId}&user2=${otherId}`)
  loadConversations()
  if (chatTimer) clearInterval(chatTimer)
  chatTimer = setInterval(() => {
    loadChatMessages()
    loadConversations()
  }, 3000)
  nextTick(() => scrollToBottom())
}

const loadChatMessages = async () => {
  if (!chatOtherId) return
  try {
    const res = await axios.get(`http://localhost:8080/message/conversation`, {
      params: { user1: userId, user2: chatOtherId }
    })
    messages.value = res.data || []
  } catch (e) { console.error(e) }
}

const closeChat = () => {
  showChat.value = false
  messages.value = []
  chatInput.value = ''
  if (chatTimer) clearInterval(chatTimer)
  chatTimer = null
}

const sendMsg = async () => {
  if (!chatInput.value?.trim() || !chatOtherId) return
  try {
    await axios.post(`http://localhost:8080/message/send`, {
      senderId: userId,
      receiverId: chatOtherId,
      content: chatInput.value
    })
    chatInput.value = ''
    await loadChatMessages()
    loadConversations()
    nextTick(() => scrollToBottom())
  } catch (e) { console.error(e) }
}

const scrollToBottom = () => {
  const el = chatBody.value
  if (el) el.scrollTop = el.scrollHeight
}

const formatTime = (t) => t ? new Date(t).toLocaleString() : ''
</script>

<style scoped>
.msg-page {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 20px;
}
.msg-card {
  background: #fff;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}
.msg-card h2 {
  margin: 0 0 16px 0;
  color: #ff4081;
  font-size: 20px;
}
.empty {
  text-align: center;
  color: #bbb;
  padding: 40px;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f2;
  cursor: pointer;
  transition: 0.2s;
}
.conv-item:hover {
  background: #fff5f7;
  margin: 0 -24px;
  padding: 12px 24px;
  border-radius: 12px;
}
.conv-avatar, .conv-avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
  object-fit: cover;
}
.conv-avatar-placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}
.conv-preview {
  color: #999;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.conv-time {
  color: #bbb;
  font-size: 11px;
  flex-shrink: 0;
}

/* 聊天弹窗 */
.chat-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-modal {
  background: #fff;
  border-radius: 20px;
  width: 90%;
  max-width: 420px;
  height: 500px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
  color: #ff4081;
}
.chat-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-msg {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 80%;
}
.chat-msg.mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.msg-avatar, .msg-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
  object-fit: cover;
}
.msg-avatar-placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.msg-bubble {
  background: #f0f0f0;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}
.chat-msg.mine .msg-bubble {
  background: #ff4081;
  color: white;
}
.chat-footer {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid #eee;
  align-items: center;
}
.chat-input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}
.chat-input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 10px 16px;
  outline: none;
  padding-right: 40px;
}
.chat-input:focus {
  border-color: #ff4081;
}
.chat-emoji-btn {
  position: absolute;
  right: 6px;
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}
.chat-send {
  background: #ff4081;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
}
</style>
