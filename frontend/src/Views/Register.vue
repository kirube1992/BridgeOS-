<template>
  <main class="auth-shell">
    <section class="auth-art">
      <div class="art-copy">
        <div class="bridge-mark"><span class="mark-icon">B</span> BridgeOS</div>
        <div class="eyebrow">{{ t('auth.register.eyebrowArt') }}</div>
        <h1>{{ t('auth.register.titleArt') }}</h1>
        <p>{{ t('auth.register.descriptionArt') }}</p>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <div class="locale-switch" :aria-label="t('auth.languageLabel')"><button :class="{ active: locale === 'en' }" type="button" @click="changeLocale('en')">{{ t('language.english') }}</button><button :class="{ active: locale === 'zh' }" type="button" @click="changeLocale('zh')">{{ t('language.chinese') }}</button></div>
        <div class="eyebrow">{{ t('auth.register.eyebrow') }}</div>
        <h2>{{ t('auth.register.title') }}</h2>
        <p>{{ t('auth.register.description') }}</p>
        <form class="form-stack" @submit.prevent="handleRegister">
          <div class="field"><label for="name">{{ t('auth.register.name') }}</label><input id="name" v-model="name" type="text" required :placeholder="t('auth.register.namePlaceholder')" /></div>
          <div class="field"><label for="email">{{ t('auth.register.email') }}</label><input id="email" v-model="email" type="email" required :placeholder="t('auth.register.emailPlaceholder')" /></div>
          <div class="field"><label for="password">{{ t('auth.register.password') }}</label><input id="password" v-model="password" type="password" required :placeholder="t('auth.register.passwordPlaceholder')" /></div>
          <div class="field"><label for="role">{{ t('auth.register.role') }}</label><select id="role" v-model="role"><option value="ETHIOPIAN_TEAM">{{ t('auth.register.roles.ethiopian') }}</option><option value="CHINESE_DEVELOPER">{{ t('auth.register.roles.chinese') }}</option><option value="HQ_CONTACT">{{ t('auth.register.roles.hq') }}</option><option value="ADMIN">{{ t('auth.register.roles.admin') }}</option></select></div>
          <div v-if="error" class="form-error">{{ error }}</div><div v-if="success" class="form-success">{{ success }}</div>
          <button class="primary-button" type="submit" :disabled="loading">{{ loading ? t('auth.register.loading') : t('auth.register.submit') }}</button>
        </form>
        <div class="auth-footer">{{ t('auth.register.existing') }} <router-link to="/login">{{ t('auth.register.signIn') }}</router-link><br /><span>{{ t('auth.register.footer') }}</span></div>
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
    success.value = t('auth.register.success')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } else {
    error.value = result.message || t('auth.register.failed')
  }

  loading.value = false
}
</script>