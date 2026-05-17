import { createRouter, createWebHistory } from 'vue-router'
import Rooms from '../views/Rooms.vue'
import Bookings from '../views/Bookings.vue'
import CleanTasks from '../views/CleanTasks.vue'
import Dashboard from '../views/Dashboard.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard },
  { path: '/rooms', component: Rooms },
  { path: '/bookings', component: Bookings },
  { path: '/clean-tasks', component: CleanTasks }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
