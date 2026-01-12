<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="text-center mb-5">
        <i class="pi pi-user" style="font-size: 2.5rem; color: var(--primary-color);"></i>
        <h2>Welcome Back</h2>
        <p class="text-secondary">Please login to your account</p>
      </div>

      <div class="flex flex-column gap-2 mb-4">
        <label for="email" class="font-semibold text-secondary">Email</label>
        <InputText id="email" v-model="email" type="email" placeholder="email@example.com" class="w-full" />
      </div>

      <div class="flex flex-column gap-2 mb-5">
        <label for="password" class="font-semibold text-secondary">Password</label>
        <Password id="password" v-model="password" :feedback="false" toggleMask placeholder="••••••••" fluid inputClass="w-full" @keydown.enter="login" />
      </div>

      <Message v-if="error" severity="error" class="mb-3" :closable="false">{{ error }}</Message>

      <Button label="Login" icon="pi pi-sign-in" class="w-full mb-3" @click="login" :loading="loading" />

      <div class="text-center">
        <span class="text-secondary">Don't have an account? </span>
        <NuxtLink to="/user/register" class="text-primary no-underline hover:underline font-medium">Register</NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from '#app';

const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');
const token = useCookie('auth_token');

async function login() {
  loading.value = true;
  error.value = '';
  
  try {
    const response: any = await $fetch('/api/auth/login', {
      method: 'POST',
      body: {
        email: email.value,
        password: password.value
      }
    });

    // Save token (flexible check for common token property names)
    token.value = response.token || response.accessToken || response.jwt || response;
    
    // Redirect to home
    router.push('/');
  } catch (err: any) {
    if (err.statusCode === 401) {
      error.value = 'Invalid email or password';
    } else {
      error.value = 'An error occurred. Please try again.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1; /* Take remaining height in the flex layout */
  min-height: 70vh;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2.5rem;
  background: radial-gradient(circle at top right, #18181b 0%, #09090b 100%);
  border: 1px solid #27272a;
  border-radius: 1.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.text-center { text-align: center; }
.mb-5 { margin-bottom: 2rem; }
.mb-4 { margin-bottom: 1.5rem; }
.mb-3 { margin-bottom: 1rem; }
.flex { display: flex; }
.flex-column { flex-direction: column; }
.gap-2 { gap: 0.5rem; }
.w-full { width: 100%; }
.font-semibold { font-weight: 600; }
.text-secondary { color: #a1a1aa; }
.text-primary { color: var(--primary-color); }
.no-underline { text-decoration: none; }
.hover\:underline:hover { text-decoration: underline; }
.font-medium { font-weight: 500; }
</style>
