import 'element-plus/dist/index.css'

import { createApp } from 'vue'
import { createWebHashHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import App from './App.vue'

const routes = [
  { path: '/login', name: "Login", component: () => import("./Login.vue") },
  { path: '/register', name: "Register", component: () => import("./Register.vue") },
  { path: '/route', name: "Route", component: () => import("./Route.vue") },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
