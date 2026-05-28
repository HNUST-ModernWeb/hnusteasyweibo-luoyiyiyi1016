<template>
  <div class="detail-page">
    <div class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
      {{ toast.message }}
    </div>

    <button @click="goBack" class="back-btn">← 返回</button>

    <div v-if="wb" class="detail-card">
      <div class="user-header" @click="goUser(wb.userId)">
        <img v-if="wb.avatar" :src="wb.avatar" class="avatar-img" />
        <div v-else class="avatar">👤</div>
        <div class="user-info">
          <span class="username">{{ wb.username || '匿名用户' }}</span>
          <span class="time">{{ formatTime(wb.createTime) }}</span>
        </div>
      </div>

      <div class="content-text">{{ wb.content }}</div>
      <img v-if="wb.imgUrl" :src="wb.imgUrl" class="weibo-img" />

      <div class="action-bar">
        <button @click="like" class="action-btn" :class="{ liked: wb.isLiked }">{{ wb.isLiked ? '❤️' : '👍' }} {{ wb.likeCount }}</button>
      </div>

      <div class="comment-section">
        <h3>评论 ({{ commentList.length }})</h3>
        <div v-if="commentList.length === 0" class="empty-tip">暂无评论，来说两句吧~</div>
        <div v-for="c in commentList" :key="c.id" class="comment-item">
          <template v-if="!c.parentId">
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
                <button @click="setReply(c.id, c.username)" class="mini-btn">💬 回复</button>
              </div>
              <div v-if="replyTarget === c.id" class="reply-input-bar">
                <input v-model="replyContent" :placeholder="'回复 @' + c.username + '...'" @keyup.enter="addReply" />
                <button @click="addReply">发送</button>
                <button @click="cancelReply" class="cancel-btn">取消</button>
              </div>
              <div v-for="r in getReplies(c.id)" :key="r.id" class="reply-item">
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
                    <button @click="setReply(c.id, r.username)" class="mini-btn">💬 回复</button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="comment-input-bar">
          <input v-model="cContent" placeholder="发表评论..." @keyup.enter="addComment" />
          <button @click="addComment">发送</button>
        </div>
      </div>
    </div>

    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userId = localStorage.getItem('userId')
const wb = ref(null)
const commentList = ref([])
const cContent = ref('')
const replyTarget = ref(null)
const replyContent = ref('')

const getReplies = (parentId) => {
  return commentList.value.filter(c => c.parentId === parentId)
}

const setReply = (parentId, replyTo) => {
  replyTarget.value = parentId
  replyContent.value = ''
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const addReply = async () => {
  if (!replyContent.value?.trim() || !replyTarget.value) return
  try {
    const parent = commentList.value.find(c => c.id === replyTarget.value)
    await axios.post("http://localhost:8080/comment/add", {
      weiboId: wb.value.id, userId, content: replyContent.value,
      parentId: replyTarget.value,
      replyTo: parent?.username || ''
    })
    replyContent.value = ''
    replyTarget.value = null
    getComments()
  } catch (e) {
    console.error(e)
    alert('回复失败，请检查数据库是否已加 parent_id 和 reply_to 列')
  }
}

const commentLike = async (c) => {
  try {
    const res = await axios.post(`http://localhost:8080/comment/like/${c.id}?userId=${userId}`)
    c.isLiked = res.data
    c.likeCount = (c.likeCount || 0) + (res.data ? 1 : -1)
  } catch (e) {
    console.error(e)
    alert('点赞失败，请检查 comment_like 表是否存在')
  }
}

const toast = ref({ show: false, type: 'success', message: '' })

const showToast = (msg, type = 'success') => {
  toast.value = { show: true, type, message: msg }
  setTimeout(() => { toast.value.show = false }, 2500)
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await axios.get("http://localhost:8080/weibo/listAll")
    wb.value = res.data.find(w => w.id == id)
  } catch (e) {}
  if (wb.value) getComments()
})

const getComments = async () => {
  try {
    const res = await axios.get("http://localhost:8080/comment/list/" + wb.value.id)
    commentList.value = res.data || []
  } catch (e) {}
}

const like = async () => {
  const res = await axios.post(`http://localhost:8080/weibo/like/${wb.value.id}?userId=${userId}`)
  wb.value.isLiked = res.data
  wb.value.likeCount += res.data ? 1 : -1
}

const addComment = async () => {
  if (!cContent.value?.trim()) return
  await axios.post("http://localhost:8080/comment/add", {
    weiboId: wb.value.id, userId, content: cContent.value
  })
  cContent.value = ''
  getComments()
  showToast('✅ 评论成功', 'success')
}

const goBack = () => router.back()

const goUser = (id) => {
  router.push(`/profile/${id}`)
}

const formatTime = (time) => time ? new Date(time).toLocaleString() : ''
</script>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  z-index: 9999;
  opacity: 0;
  transition: all 0.3s ease;
}
.toast.show { opacity: 1; top: 40px; }
.toast.success { background: #52c41a; }
.toast.error { background: #ff4d4f; }

.detail-page {
  max-width: 700px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.back-btn {
  background: none;
  border: none;
  color: #ff4081;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 1rem;
  font-weight: 500;
}
.back-btn:hover { opacity: 0.7; }

.detail-card {
  background: #fff;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.loading {
  text-align: center;
  color: #999;
  padding: 3rem;
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
  width: 40px; height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6f91, #ff9a9e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-img {
  width: 40px; height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; }
.username { font-weight: bold; color: #333; transition: color 0.2s; }
.time { font-size: 12px; color: #999; }

.content-text {
  color: #444;
  line-height: 1.6;
  margin: 10px 0;
  font-size: 17px;
}
.weibo-img {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}

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
  padding: 10px;
  border-radius: 8px;
  color: #666;
  font-size: 15px;
  cursor: pointer;
}
.action-btn:hover { background: #ff6f91; color: white; }
.action-btn.liked { background: #fff1f3; color: #ff4081; }
.action-btn.liked:hover { background: #ff4081; color: white; }

.comment-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.comment-section h3 {
  font-size: 15px;
  color: #333;
  margin-bottom: 12px;
}
.empty-tip {
  color: #bbb;
  text-align: center;
  padding: 20px;
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
  width: 28px; height: 28px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.comment-avatar-img {
  width: 28px; height: 28px;
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
.comment-name { font-size: 11px; color: #999; transition: color 0.2s; }
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
  margin-top: 12px;
}
.comment-input-bar input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 10px 14px;
}
.comment-input-bar button {
  background: #ff4081;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 18px;
  cursor: pointer;
}
</style>
