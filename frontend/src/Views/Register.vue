<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
      <div>
        <div class="flex justify-center">
          <div class="h-16 w-16 bg-indigo-600 rounded-2xl flex items-center justify-center">
            <svg class="h-10 w-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
            </svg>
          </div>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          BridgeOS
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Create your account
        </p>
      </div>

      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="space-y-4">
          <div>
            <label for="name" class="block text-sm font-medium text-gray-700">Full Name</label>
            <input
              id="name"
              v-model="name"
              type="text"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Your full name"
            />
          </div>
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">Email address</label>
            <input
              id="email"
              v-model="email"
              type="email"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="you@example.com"
            />
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="••••••••"
            />
          </div>
          <div>
            <label for="role" class="block text-sm font-medium text-gray-700">Role</label>
            <select
              id="role"
              v-model="role"
              class="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
            >
              <option value="ETHIOPIAN_TEAM">🇪🇹 Ethiopian Team</option>
              <option value="CHINESE_DEVELOPER">🇨🇳 Chinese Developer</option>
              <option value="HQ_CONTACT">🏢 HQ Contact</option>
              <option value="ADMIN">🔑 Admin</option>
            </select>
          </div>
        </div>

        <div v-if="error" class="text-red-600 text-sm text-center bg-red-50 p-2 rounded-lg">
          {{ error }}
        </div>
        <div v-if="success" class="text-green-600 text-sm text-center bg-green-50 p-2 rounded-lg">
          {{ success }}
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2.5 px-4 border border-transparent text-sm font-semibold rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition duration-150"
          >
            {{ loading ? 'Creating account...' : 'Create account' }}
          </button>
        </div>

        <div class="text-sm text-center">
          <router-link to="/login" class="font-medium text-indigo-600 hover:text-indigo-500 transition">
            Already have an account? <span class="underline">Sign in</span>
          </router-link>
        </div>
      </form>

      <div class="mt-6 text-center text-xs text-gray-400">
        <span>BridgeOS — Ethiopia-China Collaboration Platform</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const name = ref<string>('')
const email = ref<string>('')
const password = ref<string>('')
const role = ref<string>('ETHIOPIAN_TEAM')
const error = ref<string>('')
const success = ref<string>('')
const loading = ref<boolean>(false)

const handleRegister = async (): Promise<void> => {
  loading.value = true
  error.value = ''
  success.value = ''

  const result = await authStore.register(name.value, email.value, password.value, role.value)

  if (result.success) {
    success.value = 'Account created! Please login.'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } else {
    error.value = result.message || 'Registration failed'
  }

  loading.value = false
}
</script>