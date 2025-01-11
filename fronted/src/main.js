import 'element-plus/dist/index.css'

import { createApp } from 'vue'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import App from './App.vue'

const routes = [
  { path: '/login', component: () => import("./Login.vue") },
  { path: '/register', component: () => import("./Register.vue") },
  { path: '/route', component: () => import("./Route.vue") },
]

const router = createRouter({
  history: createMemoryHistory(),
  routes,
})

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
