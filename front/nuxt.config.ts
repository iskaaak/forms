import Aura from '@primevue/themes/aura';

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxt/eslint', '@nuxt/fonts', '@nuxt/icon', '@primevue/nuxt-module'],
  primevue: {
    options: {
      theme: {
        preset: Aura
      }
    }
  },
  css: [
    '~/assets/css/base.css',
    'primeicons/primeicons.css'
  ],
  routeRules: {
    '/api/**': { proxy: 'http://localhost:8080/**' }
  }
})