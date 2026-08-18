<template>
  <main class="auth-shell">
    <section class="auth-art">
      <div class="art-copy">
        <div class="bridge-mark"><span class="mark-icon">B</span> BridgeOS</div>
        <div class="eyebrow">{{ t('auth.login.eyebrowArt') }}</div>
        <h1>{{ t('auth.login.titleArt') }}</h1>
        <p>{{ t('auth.login.descriptionArt') }}</p>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <div class="locale-switch" :aria-label="t('auth.languageLabel')">
          <button :class="{ active: locale === 'en' }" type="button" @click="changeLocale('en')">{{ t('language.english') }}</button>
          <button :class="{ active: locale === 'zh' }" type="button" @click="changeLocale('zh')">{{ t('language.chinese') }}</button>
        </div>
        <div class="eyebrow">{{ t('auth.login.eyebrow') }}</div>
        <h2>{{ t('auth.login.title') }}</h2>
        <p>{{ t('auth.login.description') }}</p>
        <form class="form-stack" @submit.prevent="handleLogin">
          <div class="field"><label for="email">{{ t('auth.login.email') }}</label><input id="email" v-model="email" type="email" required :placeholder="t('auth.login.emailPlaceholder')" /></div>
          <div class="field"><label for="password">{{ t('auth.login.password') }}</label><input id="password" v-model="password" type="password" required :placeholder="t('auth.login.passwordPlaceholder')" /></div>
          <div v-if="error" class="form-error">{{ error }}</div>
          <button class="primary-button" type="submit" :disabled="loading">{{ loading ? t('auth.login.loading') : t('auth.login.submit') }}</button>
        </form>
        <div class="auth-footer">{{ t('auth.login.newToBridge') }} <router-link to="/register">{{ t('auth.login.createAccount') }}</router-link><br /><span>{{ t('auth.login.footer') }}</span></div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { setLocale, type Locale } from '@/i18n'

const router = useRouter()
const authStore = useAuthStore()
const { t, locale } = useI18n()

const changeLocale = (nextLocale: Locale): void => {
  setLocale(nextLocale)
}

const email = ref<string>('')
const password = ref<string>('')
const error = ref<string>('')
const loading = ref<boolean>(false)

const handleLogin = async (): Promise<void> => {
  loading.value = true
  error.value = ''

  const result = await authStore.login(email.value, password.value)

  if (result.success) {
    router.push('/dashboard')
  } else {
    error.value = result.message || t('auth.login.failed')
  }

  loading.value = false
}
</script>