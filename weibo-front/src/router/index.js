import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Publish from '../views/Publish.vue'
import Profile from '../views/Profile.vue'
import ProfileEdit from '../views/ProfileEdit.vue'
import Settings from '../views/Settings.vue'
import PostDetail from '../views/PostDetail.vue'
import Messages from '../views/Messages.vue'
import Search from '../views/Search.vue'

const routes = [
    { path: '/', redirect: '/home' },
    { path: '/login', component: Login },
    { path: '/home', component: Home },
    { path: '/publish', component: Publish },
    { path: '/profile', component: Profile },
    { path: '/profile/edit', component: ProfileEdit },
    { path: '/profile/:id', component: Profile },
    { path: '/settings', component: Settings },
    { path: '/post/:id', component: PostDetail },
    { path: '/messages', component: Messages },
    { path: '/search', component: Search }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router