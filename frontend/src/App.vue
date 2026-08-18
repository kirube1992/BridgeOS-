<script setup lang="ts">
import {useAuthStore} from '@/stores/auth'
const authStore = useAuthStore()
authStore.loadUserFromStorage()
</script>

<template>
  
<router-view />
</template>

<style scoped></style>
