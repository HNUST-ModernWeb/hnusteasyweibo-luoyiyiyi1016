<template>
  <div class="home-layout">
    <div class="home-main">
      <div class="tab-bar">
        <span :class="{ active: tab === 'latest' }" @click="switchTab('latest')">最新</span>
        <span :class="{ active: tab === 'hot' }" @click="switchTab('hot')">🔥 热门</span>
      </div>

      <div v-for="wb in list" :key="wb.id" :id="'post-' + wb.id" class="weibo-card">
      <div class="user-header" @click="goUser(wb.userId)">
        <img v-if="wb.avatar" :src="wb.avatar" class="avatar-img" />
        <div v-else class="avatar">👤</div>
        <div class="user-info">
          <span class="username">{{ wb.username || '匿名用户' }}</span>
          <span class="time">{{ formatTime(wb.createTime) }}</span>
        </div>
      </div>

      <div class="clickable-area" @click="viewPost(wb.id)">
        <div class="content-text">{{ wb.content }}</div>
        <img v-if="wb.imgUrl" :src="wb.imgUrl" class="weibo-img" />
      </div>

      <!-- 统一风格 + 水平对齐 点赞 / 评论 / 删除 -->
      <div class="action-bar">
        <button @click="like(wb.id)" class="action-btn" :class="{ liked: wb.isLiked }">
          {{ wb.isLiked ? '❤️' : '👍' }} {{ wb.likeCount }}
        </button>
        <button @click="showComment = wb.id" class="action-btn">
          💬 评论
        </button>
        <button
            v-if="wb.userId == userId"
            @click="del(wb.id)"
            class="action-btn delete-btn"
        >
          删除
        </button>
      </div>

      <!-- 评论区 -->
      <div class="comment-section" v-if="showComment === wb.id">
        <div class="comment-list">
          <template v-for="c in comments[wb.id] || []" :key="c.id">
            <!-- 顶级评论 -->
            <div v-if="!c.parentId" class="comment-item">
              <div class="comment-user" @click="goUser(c.userId)">
                <img v-if="c.avatar" :src="c.avatar" class="comment-avatar-img" />
                <div v-else class="comment-avatar">👤</div>
                <span class="comment-name">{{ c.username || '匿名用户' }}</span>
              </div>
              <div class="comment-body-wrap">
                <div class="comment-body">
                  <div class="comment-content">{{ c.content }}</div>
                </div>
                <div class="comment-actions">
                  <button @click="commentLike(c)" class="mini-btn" :class="{ liked: c.isLiked }">{{ c.isLiked ? '❤️' : '👍' }} {{ c.likeCount || 0 }}</button>
                  <button @click="setReply(wb.id, c.id, c.username)" class="mini-btn">💬 回复</button>
                </div>
                <!-- 回复输入 -->
                <div v-if="replyTarget && replyTarget.parentId === c.id && replyTarget.weiboId === wb.id" class="reply-input-bar">
                  <input v-model="replyContent" :placeholder="'回复 @' + replyTarget.replyTo + '...'" @keyup.enter="addReply(wb.id)" />
                  <button @click="addReply(wb.id)">发送</button>
                  <button @click="cancelReply" class="cancel-btn">取消</button>
                </div>
                <!-- 回复列表 -->
                <div v-for="r in getReplies(wb.id, c.id)" :key="r.id" class="reply-item">
                  <div class="comment-user" @click="goUser(r.userId)">
                    <img v-if="r.avatar" :src="r.avatar" class="comment-avatar-img" />
                    <div v-else class="comment-avatar">👤</div>
                    <span class="comment-name">{{ r.username || '匿名用户' }}</span>
                  </div>
                  <div class="comment-body-wrap">
                    <div class="comment-body">
                      <div class="comment-content"><span v-if="r.replyTo" class="reply-tag">回复 @{{ r.replyTo }}: </span>{{ r.content }}</div>
                    </div>
                    <div class="comment-actions">
                      <button @click="commentLike(r)" class="mini-btn" :class="{ liked: r.isLiked }">{{ r.isLiked ? '❤️' : '👍' }} {{ r.likeCount || 0 }}</button>
                      <button @click="setReply(wb.id, c.id, r.username)" class="mini-btn">💬 回复</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="comment-input-bar">
          <input v-model="cContent[wb.id]" placeholder="发表评论..." />
          <button @click="addComment(wb.id)">发送</button>
        </div>
      </div>
    </div>
  </div>

  <div class="home-sidebar">
    <div class="sidebar-card">
      <h4>🔥 推荐话题</h4>
      <div v-if="topics.length === 0" class="sidebar-empty">暂无话题</div>
      <div v-for="t in topics" :key="t.name" class="topic-item" @click="searchTopic(t.name)">
        <span class="topic-name">#{{ t.name }}#</span>
        <span class="topic-count">{{ t.count }} 条</span>
      </div>
    </div>
    <div class="sidebar-card">
      <h4>👥 推荐关注</h4>
      <div v-if="recommendUsers.length === 0" class="sidebar-empty">暂无推荐</div>
      <div v-for="u in recommendUsers" :key="u.id" class="rec-user" @click="goUser(u.id)">
        <img v-if="u.avatar" :src="u.avatar" class="rec-avatar" />
        <div v-else class="rec-avatar-placeholder">{{ u.username[0] }}</div>
        <span>{{ u.username }}</span>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userId = localStorage.getItem('userId')
const list = ref([])
const tab = ref('latest')
const comments = ref({})
const cContent = ref({})
const showComment = ref(null)
const replyTarget = ref(null)
const replyContent = ref('')
const topics = ref([])
const recommendUsers = ref([])

const loadList = async () => {
  try {
    const url = tab.value === 'hot'
      ? 'http://localhost:8080/weibo/hot'
      : 'http://localhost:8080/weibo/listAll'
    const res = await axios.get(url)
    list.value = res.data || []
    ;(res.data || []).forEach(wb => getComment(wb.id))
  } catch (e) { console.error(e) }
}

const switchTab = (t) => {
  tab.value = t
  loadList()
}

const loadSidebar = async () => {
  try {
    const res = await axios.get('http://localhost:8080/weibo/topics')
    topics.value = res.data || []
  } catch (e) {}
  if (userId) {
    try {
      const res = await axios.get(`http://localhost:8080/follow/recommend?userId=${userId}`)
      const ids = res.data || []
      const users = []
      for (const id of ids) {
        try {
          const u = await axios.get(`http://localhost:8080/user/info/${id}`)
          users.push(u.data)
        } catch (_) {}
      }
      recommendUsers.value = users
    } catch (e) {}
  }
}

onMounted(() => {
  loadList()
  loadSidebar()
  nextTick(() => {
    const postId = route.query.postId
    if (postId) {
      const el = document.getElementById('post-' + postId)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        el.classList.add('highlight')
        setTimeout(() => el.classList.remove('highlight'), 2000)
      }
    }
  })
})

const searchTopic = (name) => {
  router.push(`/search?q=${encodeURIComponent('#' + name + '#')}`)
}

const getReplies = (weiboId, parentId) => {
  return (comments.value[weiboId] || []).filter(c => c.parentId === parentId)
}

const setReply = (weiboId, parentId, replyTo) => {
  replyTarget.value = { weiboId, parentId, replyTo }
  replyContent.value = ''
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const addReply = async (wid) => {
  if (!replyContent.value?.trim()) return
  try {
    await axios.post("http://localhost:8080/comment/add", {
      weiboId: wid, userId: userId, content: replyContent.value,
      parentId: replyTarget.value.parentId,
      replyTo: replyTarget.value.replyTo
    })
    replyContent.value = ''
    replyTarget.value = null
    getComment(wid)
  } catch (e) {
    console.error(e)
  }
}

const commentLike = async (c) => {
  try {
    const res = await axios.post(`http://localhost:8080/comment/like/${c.id}?userId=${userId}`)
    c.isLiked = res.data
    c.likeCount = (c.likeCount || 0) + (res.data ? 1 : -1)
  } catch (e) {
    console.error(e)
  }
}

const getComment = async (wid) => {
  try {
    const res = await axios.get("http://localhost:8080/comment/list/" + wid)
    comments.value[wid] = res.data || []
  } catch (e) {}
}

const addComment = async (wid) => {
  if (!cContent.value[wid]?.trim()) return
  await axios.post("http://localhost:8080/comment/add", {
    weiboId: wid, userId: userId, content: cContent.value[wid]
  })
  getComment(wid)
  cContent.value[wid] = ''
}

const like = async (id) => {
  const res = await axios.post(`http://localhost:8080/weibo/like/${id}?userId=${userId}`)
  const wb = list.value.find(w => w.id === id)
  if (wb) {
    wb.isLiked = res.data
    wb.likeCount += res.data ? 1 : -1
  }
}

const del = async (id) => {
  await axios.delete(`http://localhost:8080/weibo/${id}?userId=${userId}`)
  list.value = (await axios.get("http://localhost:8080/weibo/listAll")).data
}

const formatTime = (time) => time ? new Date(time).toLocaleString() : ''

const viewPost = (id) => {
  router.push(`/post/${id}`)
}

const goUser = (id) => {
  router.push(`/profile/${id}`)
}
</script>

<style scoped>
/* 你的原有风格 100% 保持 */
.home-layout {
  max-width: 950px;
  margin: 2rem auto;
  padding: 0 1rem;
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.home-main {
  flex: 1;
  min-width: 0;
}
.home-sidebar {
  width: 260px;
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .home-sidebar { display: none; }
  .home-layout { max-width: 700px; }
}

.tab-bar {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.tab-bar span {
  flex: 1;
  text-align: center;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  color: #999;
  transition: 0.2s;
}
.tab-bar span.active {
  background: linear-gradient(135deg, #ff4081, #ff6f91);
  color: white;
  box-shadow: 0 2px 8px rgba(255,64,129,0.3);
}

.sidebar-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.sidebar-card h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #ff4081;
}
.sidebar-empty {
  color: #bbb;
  font-size: 13px;
  text-align: center;
  padding: 10px;
}
.topic-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 4px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: 0.2s;
}
.topic-item:hover {
  background: #fff5f7;
  margin: 0 -8px;
  padding: 8px 12px;
  border-radius: 8px;
}
.topic-name {
  color: #ff4081;
  font-size: 13px;
  font-weight: 500;
}
.topic-count {
  color: #bbb;
  font-size: 12px;
}
.rec-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  transition: 0.2s;
}
.rec-user:hover {
  background: #fff5f7;
  margin: 0 -8px;
  padding: 6px 12px;
  border-radius: 8px;
}
.rec-avatar, .rec-avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.rec-avatar-placeholder {
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
}
.weibo-card {
  background: #fff;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  margin-bottom: 1.5rem;
}
.clickable-area {
  cursor: pointer;
  transition: opacity 0.2s;
}
.clickable-area:hover {
  opacity: 0.8;
}
.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}
.user-header:hover .username {
  color: #ff4081;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}
.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  flex-direction: column;
}
.username {
  font-weight: bold;
  color: #333;
  transition: color 0.2s;
}
.time {
  font-size: 12px;
  color: #999;
}
.content-text {
  color: #444;
  line-height: 1.6;
  margin: 10px 0;
}
.weibo-img {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}

/* ========== 修复：统一按钮 + 水平对齐 ========== */
.action-bar {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
.action-btn {
  flex: 1;
  border: none;
  background: #f7f8fa;
  padding: 8px;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
.delete-btn {
  background: #fff1f3;
  color: #ff4081;
}
.action-btn.liked {
  background: #fff1f3;
  color: #ff4081;
}

/* ========== 评论区样式统一 ========== */
.comment-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}
.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}
.comment-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  min-width: 40px;
  flex-shrink: 0;
}
.comment-user:hover .comment-name {
  color: #ff4081;
}
.comment-avatar {
  width: 28px;
  height: 28px;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.comment-avatar-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
.comment-body-wrap {
  flex: 1;
  min-width: 0;
}
.comment-body {
  background: #f7f8fa;
  padding: 8px 12px;
  border-radius: 12px;
  display: inline-block;
}
.comment-name {
  font-size: 11px;
  color: #999;
  transition: color 0.2s;
}
.comment-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  padding-left: 4px;
}
.mini-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: 0.2s;
}
.mini-btn:hover {
  color: #ff4081;
  background: #fff1f3;
}
.mini-btn.liked {
  color: #ff4081;
}
.reply-tag {
  color: #ff4081;
  font-size: 13px;
}
.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 10px;
  margin-left: 0;
}
.reply-input-bar {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}
.reply-input-bar input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 13px;
}
.reply-input-bar button {
  background: #ff4081;
  color: white;
  border: none;
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
}
.cancel-btn {
  background: #eee !important;
  color: #666 !important;
}

.comment-input-bar {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.comment-input-bar input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 8px 14px;
}
.comment-input-bar button {
  background: #ff4081;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 14px;
}

.weibo-card.highlight {
  animation: glow 2s ease-out;
}

@keyframes glow {
  0% { box-shadow: 0 0 0 4px rgba(255, 64, 129, 0.4); }
  100% { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
}
</style>