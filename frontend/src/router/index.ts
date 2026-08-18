import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Login from '@/Views/login.vue'
import Register from '@/Views/Register.vue'
import Dashboard from '@/Views/Dashbord.vue'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresGuest: true }  // ← FIXED: requiresGuest, not requiresAuth
  },
  {
    path: '/register',
    name: 'register',
    component: Register,
    meta: { requiresGuest: true }  // ← FIXED: requiresGuest, not requiresAuth
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'dashboard',  // ← FIXED: correct spelling
    component: Dashboard,  // ← FIXED: correct spelling
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  // If route requires auth and user is not authenticated → redirect to login
  if (to.meta?.requiresAuth && !isAuthenticated) {
    return '/login'
  }

  // If route is for guests (login/register) and user is authenticated → redirect to dashboard
  if (to.meta?.requiresGuest && isAuthenticated) {
    return '/dashboard'
  }

  // ✅ ADD THIS: Allow navigation for all other routes
  return true
})

export default router