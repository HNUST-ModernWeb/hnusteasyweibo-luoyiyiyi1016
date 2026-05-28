<template>
  <div class="search-page">
    <div class="search-card">
      <div class="search-bar">
        <input v-model="keyword" placeholder="搜索用户或微博..." @keyup.enter="doSearch" class="search-input" />
        <button @click="doSearch" class="search-btn">搜索</button>
      </div>
      <div v-if="!keyword" class="recommend-section">
        <div class="rec-title">👥 推荐用户</div>
        <div v-if="recUsers.length === 0" class="rec-empty">暂无推荐</div>
        <div v-for="u in recUsers" :key="u.id" class="rec-user-row" @click="goUser(u.id)">
          <img v-if="u.avatar" :src="u.avatar" class="rec-avatar" />
          <div v-else class="rec-avatar placeholder">{{ u.username ? u.username[0] : '?' }}</div>
          <span class="rec-username">{{ u.username }}</span>
        </div>
      </div>
      <div v-if="!keyword && hotTopics.length" class="recommend-section">
        <div class="rec-title">🔥 推荐话题</div>
        <div class="topic-chips">
          <span v-for="t in hotTopics" :key="t.name" class="topic-chip" @click="searchTopic(t.name)">{{ t.name }}</span>
        </div>
      </div>
      <div v-if="!keyword && recentSearches.length" class="recommend-section">
        <div class="rec-title">🕐 最近搜索</div>
        <div class="topic-chips">
          <span v-for="(s, i) in recentSearches" :key="i" class="topic-chip recent" @click="searchTopic(s)">{{ s }}</span>
          <span class="topic-chip clear" @click="clearRecent">清空</span>
        </div>
      </div>
      <div v-if="keyword" class="results">
        <div v-if="users.length" class="result-group">
          <div class="result-group-title">👤 用户</div>
          <div v-for="u in users" :key="u.id" class="user-item" @click="goUser(u.id)">
            <img v-if="u.avatar" :src="u.avatar" class="search-avatar" />
            <div v-else class="search-avatar-placeholder">{{ u.username[0] }}</div>
            <div class="user-info">
              <div class="search-username">{{ u.username }}</div>
              <div class="search-intro">{{ u.intro || '暂无简介' }}</div>
            </div>
          </div>
        </div>
        <div v-if="weibos.length" class="result-group">
          <div class="result-group-title">📝 微博</div>
          <div v-for="wb in weibos" :key="wb.id" class="weibo-item" @click="viewPost(wb.id)">
            <div class="weibo-user">
              <img v-if="wb.avatar" :src="wb.avatar" class="mini-avatar" />
              <div v-else class="mini-avatar-placeholder">{{ wb.username[0] }}</div>
              <span class="weibo-username">{{ wb.username }}</span>
            </div>
            <div class="weibo-content">{{ wb.content }}</div>
            <div class="weibo-meta">❤️ {{ wb.likeCount }} · {{ formatTime(wb.createTime) }}</div>
          </div>
        </div>
        <div v-if="users.length === 0 && weibos.length === 0" class="empty">未找到相关结果</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
const route = useRoute()
const router = useRouter()
const keyword = ref('')
const users = ref([])
const weibos = ref([])



const hotTopics = ref([])
const recentSearches = ref(JSON.parse(localStorage.getItem('recentSearches') || '[]'))
const recUsers = ref([])

onMounted(async () => {
  if (route.query.q) {
    keyword.value = route.query.q
    doSearch()
  }
  try {
    const res = await axios.get('http://localhost:8080/weibo/topics')
    hotTopics.value = (res.data || []).slice(0, 8)
  } catch (e) {}
  const uid = localStorage.getItem('userId')
  if (uid) {
    try {
      const r = await axios.get(`http://localhost:8080/follow/recommend?userId=${uid}`)
      const ids = r.data || []
      const list = []
      for (const id of ids) {
        try { const u = await axios.get(`http://localhost:8080/user/info/${id}`); list.push(u.data) } catch (_) {}
      }
      recUsers.value = list
    } catch (e) {}
  }
})

const saveRecent = (kw) => {
  const list = [kw, ...recentSearches.value.filter(s => s !== kw)].slice(0, 6)
  recentSearches.value = list
  localStorage.setItem('recentSearches', JSON.stringify(list))
}

const searchTopic = (t) => {
  keyword.value = t
  doSearch()
}

const clearRecent = () => {
  recentSearches.value = []
  localStorage.removeItem('recentSearches')
}

const doSearch = async () => {
  const kw = keyword.value.trim()
  console.log('doSearch called, keyword:', kw)
  if (!kw) return
  saveRecent(kw)
  try {
    console.log('Fetching:', `http://localhost:8080/user/search?q=${encodeURIComponent(kw)}`)
    const [ru, rw] = await Promise.all([
      axios.get(`http://localhost:8080/user/search?q=${encodeURIComponent(kw)}`),
      axios.get(`http://localhost:8080/weibo/search?q=${encodeURIComponent(kw)}`)
    ])
    console.log('Users result:', ru.data)
    console.log('Weibos result:', rw.data)
    users.value = ru.data || []
    weibos.value = rw.data || []
    console.log('users.value:', users.value.length, 'weibos.value:', weibos.value.length)
  } catch (e) {
    console.error('Search error:', e)
    alert('搜索失败: ' + (e.message || '未知错误'))
  }
}

const goUser = (id) => router.push(`/profile/${id}`)
const viewPost = (id) => router.push(`/post/${id}`)
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''
</script>

<style scoped>
.search-page {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 20px;
}
.search-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.search-input {
  flex: 1;
  border: 2px solid #eee;
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 15px;
  outline: none;
  transition: 0.2s;
}
.search-input:focus {
  border-color: #ff4081;
}
.search-btn {
  background: linear-gradient(135deg, #ff4081, #ff6f91);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: bold;
  cursor: pointer;
}
.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.user-item:hover {
  background: #fff5f7;
  margin: 0 -24px;
  padding: 10px 24px;
  border-radius: 12px;
}
.search-avatar, .search-avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.search-avatar-placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.user-info { flex: 1; }
.search-username { font-weight: bold; color: #333; }
.search-intro { font-size: 13px; color: #999; }
.weibo-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.weibo-item:hover {
  background: #fff5f7;
  margin: 0 -24px;
  padding: 12px 24px;
  border-radius: 12px;
}
.weibo-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.mini-avatar, .mini-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.mini-avatar-placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}
.weibo-username { font-size: 13px; color: #ff4081; font-weight: 500; }
.weibo-content { color: #333; line-height: 1.5; margin: 4px 0; }
.weibo-meta { font-size: 12px; color: #bbb; }
.rec-user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s;
}
.rec-user-row:hover {
  background: #fff5f7;
}
.rec-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}
.rec-avatar.placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  font-size: 14px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rec-username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.rec-empty {
  color: #bbb;
  font-size: 13px;
  padding: 4px 8px;
}
.recommend-section {
  margin-bottom: 16px;
}
.rec-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}
.topic-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic-chip {
  background: #fff5f7;
  color: #ff4081;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: 0.2s;
  border: 1px solid #ffe0e8;
}
.topic-chip:hover {
  background: #ff4081;
  color: white;
  border-color: #ff4081;
}
.topic-chip.recent {
  background: #f5f5f5;
  color: #555;
  border-color: #eee;
}
.topic-chip.recent:hover {
  background: #ff4081;
  color: white;
  border-color: #ff4081;
}
.topic-chip.clear {
  background: none;
  color: #bbb;
  border: none;
  font-size: 12px;
}
.topic-chip.clear:hover {
  background: none;
  color: #ff4081;
}
.empty { text-align: center; color: #bbb; padding: 30px; }
</style>
