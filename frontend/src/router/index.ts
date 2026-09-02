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
import AdminDepartments from '@/Views/AdminDepartments.vue'
import AdminDepartmentDetail from '@/Views/AdminDepartmentDetail.vue'
import WeeklyReport from '@/Views/WeeklyReport.vue'
import AITools from '@/Views/AITools.vue'
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
    path: '/reports',
    name: 'weekly-report',
    component: WeeklyReport,
    meta: { requiresAuth: true }
  },
  {
    path: '/ai',
    name: 'ai-tools',
    component: AITools,
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
    path: '/admin/departments',
    name: 'admin-departments',
    component: AdminDepartments,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/departments/:id',
    name: 'admin-department-detail',
    component: AdminDepartmentDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
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

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  if (to.meta?.requiresAuth && !isAuthenticated) {
    return '/login'
  }

  if (to.meta?.requiresGuest && isAuthenticated) {
    return '/dashboard'
  }

  if (to.meta?.requiresAdmin && authStore.getUserRole !== 'ADMIN') {
    return '/dashboard'
  }

  return true
})

export default router 