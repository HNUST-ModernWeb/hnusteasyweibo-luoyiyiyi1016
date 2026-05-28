<template>
  <div class="login-page">
    <!-- 漂亮提示条 -->
    <div class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
      {{ toast.message }}
    </div>

    <div class="login-card">
      <div class="logo">
        <div class="icon">📢</div>
        <h1>微微博</h1>
        <p>分享你的生活瞬间</p>
      </div>

      <div class="form">
        <input
            v-model="user.username"
            placeholder="请输入用户名"
            class="input-item"
        />
        <input
            v-model="user.password"
            placeholder="请输入密码"
            type="password"
            class="input-item"
        />

        <button @click="submit" class="btn-primary">
          {{ isLogin ? '立即登录' : '立即注册' }}
        </button>

        <button @click="isLogin = !isLogin" class="btn-switch">
          {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
const router = useRouter()

const isLogin = ref(true)
const user = ref({ username: '', password: '' })

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

const submit = async () => {
  const url = isLogin.value ? '/user/login' : '/user/register'
  const res = await axios.post(url, user.value)

  if (isLogin.value) {
    if (res.data) {
      localStorage.setItem('user', res.data.username)
      localStorage.setItem('userId', res.data.id)

      // ✅ 登录成功 漂亮提示
      showToast("✅ 登录成功！欢迎回来", "success")

      setTimeout(() => {
        router.push('/home')
      }, 800)
    } else {
      showToast("❌ 账号或密码错误", "error")
    }
  } else {
    showToast("✅ 注册成功！请登录", "success")
    isLogin.value = true
  }
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

.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  background: white;
  width: 100%;
  max-width: 420px;
  padding: 40px 30px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.logo {
  margin-bottom: 30px;
}
.icon {
  font-size: 50px;
  margin-bottom: 10px;
}
.logo h1 {
  margin: 0;
  font-size: 28px;
  color: #ff4081;
}
.logo p {
  color: #999;
  margin-top: 5px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.input-item {
  padding: 14px 16px;
  border: 1px solid #eee;
  border-radius: 12px;
  font-size: 15px;
  background: #f9f9f9;
}
.input-item:focus {
  outline: none;
  border-color: #ff4081;
  background: #fff;
}

.btn-primary {
  padding: 14px;
  background: linear-gradient(to right, #ff4081, #ff6fa5);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
}

.btn-switch {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  margin-top: 10px;
}
.btn-switch:hover {
  color: #ff4081;
}
</style>