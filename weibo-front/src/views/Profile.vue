<template>
  <div class="profile-page">
    <button v-if="isOtherUser" @click="goBack" class="back-btn">← 返回</button>

    <div class="profile-card">
      <!-- 头像和昵称 -->
      <div class="profile-header">
        <div v-if="!isOtherUser" class="avatar-upload" @click="goEdit">
          <div class="avatar-wrapper">
            <img v-if="user.avatar" :src="user.avatar" class="big-avatar-img" />
            <div v-else class="big-avatar">{{ user.username ? user.username[0] : '👤' }}</div>
            <div class="avatar-overlay">编辑头像</div>
          </div>
        </div>
        <div v-else class="avatar-display">
          <img v-if="user.avatar" :src="user.avatar" class="big-avatar-img" />
          <div v-else class="big-avatar">{{ user.username ? user.username[0] : '👤' }}</div>
        </div>

        <div class="user-details">
          <h2>{{ user.username || '用户' }}</h2>
          <p v-if="user.intro" class="intro">{{ user.intro }}</p>
          <p>@{{ user.username }} · {{ weiboCount }} 条动态 · ❤️ 获赞 {{ totalLikes }}</p>
          <div class="follow-stats">
            <span class="stat-item clickable" @click="showFollowList('following')"><strong>{{ followCount }}</strong> 关注</span>
            <span class="stat-item clickable" @click="showFollowList('followers')"><strong>{{ followerCount }}</strong> 粉丝</span>
          </div>
        </div>
      </div>

      <div class="info-grid" v-if="user.gender || user.birthday || user.email || user.location">
        <div v-if="user.gender" class="info-item">👤 {{ user.gender }}</div>
        <div v-if="user.birthday" class="info-item">🎂 {{ user.birthday }}</div>
        <div v-if="user.email" class="info-item">📧 {{ user.email }}</div>
        <div v-if="user.location" class="info-item">📍 {{ user.location }}</div>
      </div>

      <!-- 关注/私聊按钮 (他人可见) -->
      <div v-if="isOtherUser" class="form-section" style="text-align: center;">
        <button @click="toggleFollow" class="follow-btn" :class="{ following: isFollowing }">
          {{ isFollowing ? '✅ 已关注' : '➕ 关注' }}
        </button>
        <button @click="openChat" class="chat-btn">💬 私聊</button>
      </div>

      <!-- 编辑按钮 (仅自己可见) -->
      <div v-if="!isOtherUser" class="form-section" style="text-align: center;">
        <button @click="goEdit" class="edit-btn">✏️ 编辑个人资料</button>
      </div>

      <!-- 我的动态列表 -->
      <div class="my-weibos">
        <h3>我的动态</h3>
        <div v-if="myWeibos.length === 0" class="empty-tip">还没有发布动态哦~</div>
        <div v-for="wb in myWeibos" :key="wb.id" class="mini-card" @click="viewPost(wb.id)">
          <p>{{ wb.content }}</p>
          <span class="time">{{ formatTime(wb.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 关注/粉丝列表 -->
    <div class="chat-overlay" v-if="showFollowModal" @click.self="closeFollowList">
      <div class="follow-modal">
        <div class="chat-header">
          <span>{{ followModalTitle }}</span>
          <button @click="closeFollowList" class="chat-close">✕</button>
        </div>
        <div class="follow-list-body">
          <div v-for="u in followList" :key="u.id" class="follow-user-item" @click="goToProfile(u.id)">
            <img v-if="u.avatar" :src="u.avatar" class="follow-user-avatar" />
            <div v-else class="follow-user-avatar placeholder">{{ u.username ? u.username[0] : '?' }}</div>
            <span class="follow-user-name">{{ u.username }}</span>
          </div>
          <div v-if="followList.length === 0" class="empty-tip">暂无数据</div>
        </div>
      </div>
    </div>

    <!-- 聊天弹窗 -->
    <div class="chat-overlay" v-if="showChat" @click.self="closeChat">
      <div class="chat-modal">
        <div class="chat-header">
          <span>💬 与 {{ user.username }} 聊天中</span>
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
            <button @click="showProfileEmoji = !showProfileEmoji" class="chat-emoji-btn">😊</button>
            <EmojiPicker :show="showProfileEmoji" @pick="e => { chatInput += e; showProfileEmoji = false }" @close="showProfileEmoji = false" />
          </div>
          <button @click="sendMsg" class="chat-send">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import EmojiPicker from '../components/EmojiPicker.vue'
import { ref, onActivated, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
const route = useRoute()
const router = useRouter()
const isOtherUser = computed(() => {
  const pid = route.params.id
  return !!pid && pid !== localStorage.getItem('userId')
})
const targetUserId = computed(() => route.params.id || localStorage.getItem('userId'))
const userId = localStorage.getItem('userId')
const user = ref({})
const myWeibos = ref([])
const weiboCount = ref(0)
const isFollowing = ref(false)
const followerCount = ref(0)
const followCount = ref(0)
const totalLikes = ref(0)

onMounted(() => {
  getUserInfo()
  getMyWeibos()
  getFollowData()
})

onActivated(() => {
  getUserInfo()
  getMyWeibos()
  getFollowData()
})

const getUserInfo = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/user/info/${targetUserId.value}`)
    const data = res.data || {}
    user.value = {
      id: data.id,
      username: data.username,
      avatar: data.avatar,
      intro: data.intro,
      gender: data.gender,
      birthday: data.birthday,
      email: data.email,
      location: data.location,
      createTime: data.createTime
    }
    totalLikes.value = data.totalLikes || 0
  } catch (err) { console.error(err) }
}

const getMyWeibos = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/weibo/list?userId=${targetUserId.value}`)
    myWeibos.value = res.data || []
    weiboCount.value = res.data?.length || 0
  } catch (err) { console.error(err) }
}

const formatTime = (time) => time ? new Date(time).toLocaleDateString() : ''

const goBack = () => router.back()

const goEdit = () => router.push('/profile/edit')

const getFollowData = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/follow/count?userId=${targetUserId.value}`)
    followerCount.value = res.data.followers || 0
    followCount.value = res.data.following || 0
  } catch (e) {}
  if (isOtherUser.value && userId) {
    try {
      const res = await axios.get(`http://localhost:8080/follow/check?followerId=${userId}&followingId=${targetUserId.value}`)
      isFollowing.value = res.data.following || false
    } catch (e) {}
  }
}

const toggleFollow = async () => {
  try {
    if (isFollowing.value) {
      await axios.delete(`http://localhost:8080/follow/${targetUserId.value}?followerId=${userId}`)
      isFollowing.value = false
      followerCount.value--
    } else {
      await axios.post(`http://localhost:8080/follow/${targetUserId.value}?followerId=${userId}`)
      isFollowing.value = true
      followerCount.value++
    }
  } catch (e) { console.error(e) }
}

const viewPost = (id) => {
  router.push(`/home?postId=${id}`)
}

// ===== 关注/粉丝列表 =====
const showFollowModal = ref(false)
const followModalTitle = ref('')
const followList = ref([])

const showFollowList = async (type) => {
  const uid = targetUserId.value
  followModalTitle.value = type === 'following' ? '关注' : '粉丝'
  followList.value = []
  showFollowModal.value = true
  try {
    const res = await axios.get(`http://localhost:8080/follow/${type}?userId=${uid}`)
    followList.value = res.data || []
  } catch (e) { console.error(e) }
}

const closeFollowList = () => {
  showFollowModal.value = false
  followList.value = []
}

const goToProfile = (id) => {
  closeFollowList()
  router.push(`/profile/${id}`)
}

// ===== 私聊 =====
const showChat = ref(false)
const showProfileEmoji = ref(false)
const messages = ref([])
const chatInput = ref('')
const chatBody = ref(null)

let chatTimer = null

onUnmounted(() => {
  if (chatTimer) clearInterval(chatTimer)
})

const openChat = async () => {
  showChat.value = true
  await loadMessages()
  await axios.post(`http://localhost:8080/message/read?user1=${userId}&user2=${targetUserId.value}`)
  if (chatTimer) clearInterval(chatTimer)
  chatTimer = setInterval(loadMessages, 3000)
  nextTick(() => scrollToBottom())
}

const closeChat = () => {
  showChat.value = false
  messages.value = []
  chatInput.value = ''
  if (chatTimer) clearInterval(chatTimer)
  chatTimer = null
}

const loadMessages = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/message/conversation`, {
      params: { user1: userId, user2: targetUserId.value }
    })
    messages.value = res.data || []
  } catch (e) { console.error(e) }
}

const sendMsg = async () => {
  if (!chatInput.value?.trim()) return
  try {
    await axios.post(`http://localhost:8080/message/send`, {
      senderId: Number(userId),
      receiverId: Number(targetUserId.value),
      content: chatInput.value
    })
    chatInput.value = ''
    await loadMessages()
    nextTick(() => scrollToBottom())
  } catch (e) { console.error(e) }
}

const scrollToBottom = () => {
  const el = chatBody.value
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<style scoped>
.back-btn {
  background: none;
  border: none;
  color: #ff4081;
  font-size: 15px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 10px;
  transition: 0.2s;
  margin-bottom: 4px;
}
.back-btn:hover {
  background: rgba(255,64,129,0.08);
}
.profile-page {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 20px;
}
.profile-card {
  background: #fff;
  padding: 28px;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 28px;
}
.avatar-upload {
  cursor: pointer;
}
.avatar-upload .avatar-overlay {
  cursor: pointer;
}
.avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.avatar-upload.uploading .avatar-overlay {
  opacity: 1;
  background: rgba(0,0,0,0.6);
}
.big-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6fa5, #ff86aa);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
}
.big-avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}
.user-details h2 {
  margin: 0;
  font-size: 22px;
  color: #222;
}
.user-details p {
  color: #999;
  margin: 4px 0 0 0;
  font-size: 14px;
}
.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  padding: 14px 16px;
  background: #f9f9fb;
  border-radius: 12px;
}
.info-item {
  font-size: 14px;
  color: #555;
  background: #fff;
  padding: 4px 12px;
  border-radius: 8px;
}

.form-section {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f2f2f2;
}
.form-section h3 {
  color: #ff4081;
  font-size: 16px;
}
.form-item {
  margin-bottom: 14px;
}
.form-item label {
  font-size: 14px;
  color: #555;
  display: block;
  margin-bottom: 6px;
}
.form-item input,
.form-item textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #eee;
  border-radius: 12px;
  outline-color: #ff86aa;
  box-sizing: border-box;
}
.follow-stats {
  display: flex;
  gap: 16px;
  margin-top: 6px;
}
.stat-item {
  font-size: 13px;
  color: #999;
}
.stat-item strong {
  color: #333;
}

.edit-btn, .chat-btn, .follow-btn {
  background: linear-gradient(to right, #ff4081, #ff6fa5);
  color: white;
  border: none;
  padding: 10px 28px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: 0.3s;
  margin: 0 4px;
}
.edit-btn:hover, .chat-btn:hover, .follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 64, 129, 0.3);
}
.follow-btn.following {
  background: #fff;
  color: #ff4081;
  border: 2px solid #ff4081;
}
.my-weibos h3 {
  font-size: 16px;
  color: #333;
}
.empty-tip {
  color: #bbb;
  padding: 20px;
  text-align: center;
}
.mini-card {
  background: #f9f9f9;
  padding: 14px;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: 0.2s;
}
.mini-card:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}
.mini-card p {
  margin: 0 0 6px 0;
  color: #333;
}
.time {
  font-size: 12px;
  color: #999;
}

.stat-item.clickable {
  cursor: pointer;
  transition: 0.2s;
  padding: 2px 6px;
  border-radius: 8px;
}
.stat-item.clickable:hover {
  background: #fff5f7;
}

/* 关注/粉丝弹窗 */
.follow-modal {
  background: #fff;
  border-radius: 20px;
  width: 90%;
  max-width: 380px;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.follow-list-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}
.follow-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.2s;
}
.follow-user-item:hover {
  background: #fff5f7;
}
.follow-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.follow-user-avatar.placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}
.follow-user-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
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