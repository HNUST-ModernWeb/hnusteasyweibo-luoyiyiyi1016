<template>
  <nav class="nav">
    <div class="logo">📢 微微博</div>

    <router-link to="/home" class="nav-item">
      <span>🏠</span> 首页
    </router-link>
    <router-link to="/publish" class="nav-item">
      <span>✍️</span> 发布
    </router-link>
    <router-link to="/search" class="nav-item">
      <span>🔍</span> 搜索
    </router-link>
    <router-link to="/messages" class="nav-item">
      <span>💬</span> 消息
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </router-link>
    <router-link to="/profile" class="nav-item">
      <span>👤</span> 我的
    </router-link>

    <div class="nav-right" style="margin-left: auto;">
      <div v-if="user" class="user-dropdown" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
        <img v-if="avatar" :src="avatar" class="nav-avatar" @click="goProfile" />
        <div v-else class="nav-avatar-placeholder" @click="goProfile">{{ user[0] }}</div>
        <div class="dropdown-menu" v-if="showMenu" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
          <router-link to="/profile" class="dropdown-item">👤 个人资料</router-link>
          <router-link to="/profile/edit" class="dropdown-item">✏️ 编辑资料</router-link>
          <router-link to="/settings" class="dropdown-item">🔔 互动与通知</router-link>
          <router-link to="/settings" class="dropdown-item">🔒 隐私设置</router-link>
          <div class="dropdown-divider"></div>
          <div class="dropdown-item" @click="switchAccount">🔄 切换账号</div>
          <div class="dropdown-item" @click="logout">🚪 退出登录</div>
        </div>
      </div>
      <router-link v-else to="/login" class="login-btn">去登录</router-link>
    </div>
  </nav>

  <router-view />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
const router = useRouter()
const user = ref(localStorage.getItem('user') || '')
const avatar = ref('')
const showMenu = ref(false)
const unreadCount = ref(0)
let unreadTimer = null

const goProfile = () => {
  const uid = localStorage.getItem('userId')
  if (uid) router.push(`/profile/${uid}`)
}

const loadUser = () => {
  user.value = localStorage.getItem('user') || ''
  const uid = localStorage.getItem('userId')
  if (uid) {
    axios.get(`http://localhost:8080/user/info/${uid}`).then(res => {
      avatar.value = res.data.avatar || ''
    }).catch(() => {})
  } else {
    avatar.value = ''
  }
}

const pollUnread = () => {
  const uid = localStorage.getItem('userId')
  if (uid) {
    axios.get(`http://localhost:8080/message/unread?userId=${uid}`).then(res => {
      unreadCount.value = res.data || 0
    }).catch(() => {})
  } else {
    unreadCount.value = 0
  }
}

let unreg
onMounted(() => {
  loadUser()
  pollUnread()
  unreadTimer = setInterval(pollUnread, 10000)
  unreg = router.afterEach(() => { loadUser() })
})

onUnmounted(() => {
  if (unreg) unreg()
  if (unreadTimer) clearInterval(unreadTimer)
})

const logout = () => {
  showMenu.value = false
  localStorage.clear()
  router.push('/login')
}

const switchAccount = () => {
  showMenu.value = false
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 30px;
  background: linear-gradient(135deg, #ff6f91 0%, #ff9a9e 50%, #fecfef 100%);
  box-shadow: 0 4px 15px rgba(255, 111, 145, 0.2);
  color: white;
  position: sticky;
  top: 0;
  z-index: 999;
  font-family: -apple-system, "Microsoft YaHei", "PingFang SC", "Noto Sans SC", sans-serif;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 20px;
  transition: 0.3s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.user-dropdown {
  position: relative;
  cursor: pointer;
  user-select: none;
}
.nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid rgba(255,255,255,0.6);
  transition: 0.3s;
}
.nav-avatar:hover {
  border-color: white;
  transform: scale(1.05);
}
.nav-avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  color: white;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid rgba(255,255,255,0.6);
  transition: 0.3s;
}
.nav-avatar-placeholder:hover {
  border-color: white;
  transform: scale(1.05);
}
.dropdown-menu {
  position: absolute;
  right: 0;
  top: 44px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  min-width: 160px;
  overflow: hidden;
  z-index: 1000;
  animation: dropIn 0.15s ease-out;
}
@keyframes dropIn {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}
.dropdown-item {
  display: block;
  padding: 10px 16px;
  color: #333;
  font-size: 14px;
  text-decoration: none;
  transition: 0.2s;
  cursor: pointer;
  font-family: -apple-system, "Microsoft YaHei", "PingFang SC", sans-serif;
}
.dropdown-item:hover {
  background: #fff5f7;
  color: #ff4081;
}
.dropdown-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 4px 0;
}

.login-btn {
  background: white;
  color: #ff6f91;
  padding: 6px 14px;
  border-radius: 15px;
  text-decoration: none;
  font-weight: bold;
  font-family: -apple-system, "Microsoft YaHei", "PingFang SC", sans-serif;
}

.unread-badge {
  background: #ff1744;
  color: white;
  font-size: 11px;
  font-weight: bold;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
  position: absolute;
  top: -4px;
  right: -10px;
  box-shadow: 0 2px 6px rgba(255,23,68,0.4);
}

.nav-item {
  position: relative;
}
</style>