import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Login from '@/Views/Login.vue'
import Register from '@/Views/Register.vue'
import Dashboard from '@/Views/Dashbord.vue'
import Projects from '@/Views/Projects.vue'
import ProjectDetail from '@/Views/ProjectDetail.vue'
import TaskDetail from '@/Views/TaskDetail.vue'
import Tasks from '@/Views/Task.vue'
import TaskForm from '@/Views/TaskForm.vue'
import DecisionLog from '@/Views/DecisionLog.vue'
import Analytics from '@/Views/Analytics.vue'
import UserAnalytics from '@/Views/UserAnalytics.vue'
import UserProfile from '@/Views/UserProfile.vue'
import People from '@/Views/People.vue'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresGuest: true }  // ← FIXED: requiresGuest, not requiresAuth
  },
  {
  path: '/profile/:id?',
  name: 'user-profile',
  component: UserProfile,
  meta: { requiresAuth: true }
},
  {
    path: '/people',
    name: 'people',
    component: People,
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks',
    name: 'tasks',
    component: Tasks,
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks/new',
    name: 'task-create',
    component: TaskForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks/:id/edit',
    name: 'task-edit',
    component: TaskForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks/:id',
    name: 'task-detail',
    component: TaskDetail,
    meta: { requiresAuth: true }
  },
    {
    path: '/projects',
    name: 'projects',
    component: Projects,
    meta: { requiresAuth: true }
  },
  {
    path: '/projects/:id',
    name: 'project-detail',
    component: ProjectDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/decisions',
    name: 'decisions',
    component: DecisionLog,
    meta: { requiresAuth: true }
  },
  {
    path: '/analytics',
    name: 'analytics',
    component: Analytics,
    meta: { requiresAuth: true }
  },
  {
    path: '/analytics/user/:userId',
    name: 'user-analytics',
    component: UserAnalytics,
    meta: { requiresAuth: true }
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