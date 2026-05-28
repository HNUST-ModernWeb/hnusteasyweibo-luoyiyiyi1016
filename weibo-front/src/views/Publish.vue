<template>
  <div class="publish-page">
    <div class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
      {{ toast.message }}
    </div>

    <div class="publish-card">
      <h2 class="title">✍️ 发布新动态</h2>

      <!-- 文字输入框 -->
      <div class="text-area-wrap">
        <textarea
            v-model="content"
            placeholder="分享你的想法... 支持 #话题#"
            class="content-input"
        ></textarea>
        <div class="emoji-btn-wrap">
          <button @click="showEmoji = !showEmoji" class="emoji-toggle">😊</button>
          <EmojiPicker :show="showEmoji" @pick="addEmoji" @close="showEmoji = false" />
        </div>
      </div>

      <!-- 图片上传区 -->
      <div class="upload-area">
        <label class="file-label" :class="{ uploading: uploading }">
          <input type="file" @change="upload" accept="image/*" hidden :disabled="uploading" />
          📷 {{ uploading ? '上传中...' : '选择图片' }}
        </label>
        <span class="file-tip">{{ imgUrl ? '已选择图片' : '未选择任何文件' }}</span>
        <button v-if="imgUrl" @click="imgUrl = ''" class="remove-img-btn">✕ 移除</button>
      </div>

      <!-- 图片预览 -->
      <div v-if="imgUrl" class="preview-wrap">
        <img :src="imgUrl" class="preview-img" />
      </div>

      <!-- 发布按钮 -->
      <button @click="submit" class="publish-btn" :disabled="uploading">发布</button>
    </div>
  </div>
</template>

<script setup>
import EmojiPicker from '../components/EmojiPicker.vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
const router = useRouter()

const content = ref('')
const imgUrl = ref('')
const userId = localStorage.getItem('userId')
const uploading = ref(false)
const showEmoji = ref(false)

const addEmoji = (e) => {
  content.value += e
}

const toast = ref({
  show: false,
  type: 'success',
  message: ''
})

const showToast = (message, type = 'success') => {
  toast.value.message = message
  toast.value.type = type
  toast.value.show = true
  setTimeout(() => {
    toast.value.show = false
  }, 2500)
}

// 上传图片
const upload = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  uploading.value = true
  const form = new FormData()
  form.append('file', file)

  try {
    const res = await axios.post('/upload', form)
    imgUrl.value = 'http://localhost:8080' + res.data
    showToast('✅ 图片上传成功', 'success')
  } catch (err) {
    console.error(err)
    showToast('❌ 图片上传失败', 'error')
  } finally {
    uploading.value = false
  }
}

// 发布微博
const submit = async () => {
  if (!content.value.trim()) {
    showToast('❌ 内容不能为空', 'error')
    return
  }

  await axios.post('/weibo/add', {
    userId: userId,
    content: content.value,
    imgUrl: imgUrl.value
  })

  showToast('✅ 发布成功！', 'success')
  setTimeout(() => router.push('/home'), 800)
}
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
.toast.show {
  opacity: 1;
  top: 40px;
}
.toast.success {
  background: #52c41a;
}
.toast.error {
  background: #ff4d4f;
}

.publish-page {
  max-width: 700px;
  margin: 3rem auto;
  padding: 0 20px;
}

.publish-card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.title {
  margin: 0 0 20px 0;
  color: #ff4081;
  font-size: 22px;
}

.content-input {
  width: 100%;
  min-height: 150px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 12px;
  font-size: 15px;
  resize: vertical;
  box-sizing: border-box;
}
.content-input:focus {
  outline: none;
  border-color: #ff4081;
}
.text-area-wrap {
  position: relative;
}
.emoji-btn-wrap {
  position: absolute;
  bottom: 12px;
  right: 12px;
}
.emoji-toggle {
  background: #f5f5f5;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}
.emoji-toggle:hover {
  background: #fff0f3;
}

.upload-area {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
  flex-wrap: wrap;
}
.file-label {
  display: inline-block;
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 8px;
  cursor: pointer;
  color: #333;
  transition: 0.3s;
}
.file-label.uploading {
  opacity: 0.6;
  cursor: not-allowed;
}
.file-tip {
  color: #666;
  font-size: 14px;
}
.remove-img-btn {
  background: none;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.preview-wrap {
  margin: 10px 0;
  display: inline-block;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.preview-img {
  max-width: 200px;
  max-height: 200px;
  display: block;
}

.publish-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(to right, #ff4081, #ff6fa5);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
  transition: 0.3s;
}
.publish-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>