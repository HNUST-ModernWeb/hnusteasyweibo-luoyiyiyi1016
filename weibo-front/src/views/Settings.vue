<template>
  <div class="settings-page">
    <button @click="goBack" class="back-link">← 返回</button>

    <div class="settings-card">
      <h2 class="title">⚙️ 设置</h2>

      <!-- 编辑资料 -->
      <section class="setting-section">
        <h3 @click="toggleSection('profile')" class="section-header">
          ✏️ 编辑资料
          <span class="chevron" :class="{ open: openSections.profile }">▼</span>
        </h3>
        <div v-if="openSections.profile" class="section-body">
          <router-link to="/profile/edit" class="setting-link">→ 修改个人资料和头像</router-link>
        </div>
      </section>

      <!-- 互动与通知 -->
      <section class="setting-section">
        <h3 @click="toggleSection('notify')" class="section-header">
          🔔 互动与通知
          <span class="chevron" :class="{ open: openSections.notify }">▼</span>
        </h3>
        <div v-if="openSections.notify" class="section-body">
          <label class="setting-row">
            <span>评论通知</span>
            <input type="checkbox" v-model="settings.commentNotify" />
          </label>
          <label class="setting-row">
            <span>点赞通知</span>
            <input type="checkbox" v-model="settings.likeNotify" />
          </label>
          <label class="setting-row">
            <span>私信通知</span>
            <input type="checkbox" v-model="settings.msgNotify" />
          </label>
        </div>
      </section>

      <!-- 隐私设置 -->
      <section class="setting-section">
        <h3 @click="toggleSection('privacy')" class="section-header">
          🔒 隐私设置
          <span class="chevron" :class="{ open: openSections.privacy }">▼</span>
        </h3>
        <div v-if="openSections.privacy" class="section-body">
          <label class="setting-row">
            <span>公开我的资料</span>
            <input type="checkbox" v-model="settings.publicProfile" />
          </label>
          <label class="setting-row">
            <span>允许他人评论</span>
            <input type="checkbox" v-model="settings.allowComment" />
          </label>
        </div>
      </section>

      <!-- 账号 -->
      <section class="setting-section">
        <h3 @click="toggleSection('account')" class="section-header">
          🛡️ 账号
          <span class="chevron" :class="{ open: openSections.account }">▼</span>
        </h3>
        <div v-if="openSections.account" class="section-body">
          <div class="setting-row">
            <span>当前账号：{{ user }}</span>
          </div>
          <button @click="logout" class="danger-btn">退出登录</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref('')

const openSections = reactive({
  profile: true,
  notify: false,
  privacy: false,
  account: false
})

const settings = reactive({
  commentNotify: true,
  likeNotify: true,
  msgNotify: false,
  publicProfile: true,
  allowComment: true
})

onMounted(() => {
  user.value = localStorage.getItem('user') || ''
  const saved = localStorage.getItem('settings')
  if (saved) Object.assign(settings, JSON.parse(saved))
})

const toggleSection = (key) => {
  openSections[key] = !openSections[key]
}

const goBack = () => router.back()

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.settings-page {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 20px;
}
.back-link {
  background: none;
  border: none;
  color: #ff4081;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 0.5rem;
  font-weight: 500;
  display: block;
}
.back-link:hover { opacity: 0.7; }
.settings-card {
  background: #fff;
  padding: 28px;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}
.title {
  margin: 0 0 24px 0;
  color: #ff4081;
  font-size: 22px;
}
.setting-section {
  border-bottom: 1px solid #f2f2f2;
  padding-bottom: 4px;
  margin-bottom: 4px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  color: #333;
  padding: 14px 0;
  margin: 0;
  cursor: pointer;
  user-select: none;
}
.section-header:hover { color: #ff4081; }
.chevron {
  font-size: 12px;
  transition: transform 0.2s;
}
.chevron.open { transform: rotate(180deg); }
.section-body {
  padding: 4px 0 14px 0;
}
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
  color: #555;
  cursor: pointer;
}
.setting-row input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #ff4081;
  cursor: pointer;
}
.setting-link {
  display: block;
  padding: 8px 0;
  color: #ff4081;
  text-decoration: none;
  font-size: 14px;
}
.setting-link:hover { opacity: 0.7; }
.danger-btn {
  margin-top: 8px;
  background: #fff1f3;
  color: #ff4081;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.danger-btn:hover { background: #ffe0e6; }
</style>
