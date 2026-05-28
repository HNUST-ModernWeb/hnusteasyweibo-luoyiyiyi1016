<template>
  <div class="edit-page">
    <div class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
      {{ toast.message }}
    </div>

    <button @click="goBack" class="back-link">← 返回个人主页</button>

    <div class="edit-card">
      <h2 class="title">编辑个人信息</h2>

      <div class="avatar-section">
        <label class="avatar-upload" :class="{ uploading }">
          <div class="avatar-wrapper">
            <img v-if="user.avatar" :src="user.avatar" class="big-avatar-img" />
            <div v-else class="big-avatar">{{ user.username ? user.username[0] : '👤' }}</div>
            <div class="avatar-overlay">{{ uploading ? '上传中...' : '更换头像' }}</div>
          </div>
          <input type="file" accept="image/*" @change="uploadAvatar" hidden :disabled="uploading">
        </label>
      </div>

      <div class="form-row">
        <div class="form-item flex-1">
          <label>昵称</label>
          <input v-model="editUser.username" placeholder="输入昵称" />
        </div>
        <div class="form-item flex-1">
          <label>性别</label>
          <select v-model="editUser.gender">
            <option value="">保密</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
      </div>

      <div class="form-item">
        <label>生日</label>
        <input v-model="editUser.birthday" type="date" />
      </div>

      <div class="form-item">
        <label>邮箱</label>
        <input v-model="editUser.email" type="email" placeholder="example@email.com" />
      </div>

      <div class="form-item">
        <label>所在地</label>
        <input v-model="editUser.location" placeholder="如：北京" />
      </div>

      <div class="form-item">
        <label>简介</label>
        <textarea v-model="editUser.intro" placeholder="介绍一下自己吧..." rows="3"></textarea>
      </div>
      <button @click="saveInfo" class="save-btn">保存修改</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const userId = localStorage.getItem('userId')
const user = ref({})
const editUser = ref({})
const uploading = ref(false)

const toast = ref({ show: false, type: 'success', message: '' })

const showToast = (msg, type = 'success') => {
  toast.value = { show: true, type, message: msg }
  setTimeout(() => { toast.value.show = false }, 2500)
}

onMounted(async () => {
  try {
    const res = await axios.get(`http://localhost:8080/user/info/${userId}`)
    user.value = res.data || {}
    editUser.value = { ...res.data } || {}
  } catch (e) { console.error(e) }
})

const saveInfo = async () => {
  try {
    await axios.post('http://localhost:8080/user/update', editUser.value)
    user.value = { ...editUser.value }
    showToast('✅ 保存成功！', 'success')
  } catch (err) {
    console.error(err)
    showToast('❌ 保存失败', 'error')
  }
}

const uploadAvatar = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await axios.post('http://localhost:8080/user/avatar?userId=' + userId, formData)
    user.value.avatar = res.data.url
    editUser.value.avatar = res.data.url
    showToast('✅ 头像上传成功！', 'success')
  } catch (err) {
    console.error(err)
    showToast('❌ 头像上传失败', 'error')
  } finally {
    uploading.value = false
  }
}

const goBack = () => router.back()
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

.edit-page {
  max-width: 500px;
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
  margin-bottom: 1rem;
  font-weight: 500;
  display: block;
}
.back-link:hover { opacity: 0.7; }

.edit-card {
  background: #fff;
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}

.title {
  margin: 0 0 24px 0;
  color: #ff4081;
  font-size: 20px;
  text-align: center;
}

.avatar-section {
  text-align: center;
  margin-bottom: 24px;
}
.avatar-upload { cursor: pointer; display: inline-block; }
.avatar-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.avatar-upload.uploading .avatar-overlay { opacity: 1; background: rgba(0,0,0,0.6); }

.big-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6fa5, #ff86aa);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: bold;
}
.big-avatar-img {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
}

.form-row {
  display: flex;
  gap: 14px;
}
.flex-1 { flex: 1; }

.form-item { margin-bottom: 18px; }
.form-item label {
  font-size: 14px;
  color: #555;
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
}
.form-item input,
.form-item textarea,
.form-item select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #eee;
  border-radius: 12px;
  outline-color: #ff86aa;
  box-sizing: border-box;
  font-size: 15px;
}
.form-item select {
  appearance: auto;
  background: #fff;
  cursor: pointer;
}
.form-item textarea { resize: vertical; }

.save-btn {
  width: 100%;
  background: linear-gradient(to right, #ff4081, #ff6fa5);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  margin-top: 6px;
}
.save-btn:hover { opacity: 0.9; }
</style>
