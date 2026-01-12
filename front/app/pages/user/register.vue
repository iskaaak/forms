<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="text-center mb-5">
        <i class="pi pi-user-plus" style="font-size: 2.5rem; color: var(--primary-color);"></i>
        <h2>Create Account</h2>
        <p class="text-secondary">Join us today to get started</p>
      </div>

      <div class="flex flex-column gap-2 mb-4">
        <label for="email" class="font-semibold text-secondary">Email</label>
        <InputText id="email" v-model="email" type="email" placeholder="email@example.com" class="w-full" :invalid="!!errors.email" />
        <small v-if="errors.email" class="text-red-500">{{ errors.email }}</small>
      </div>

      <div class="flex flex-column gap-2 mb-4">
        <label for="password" class="font-semibold text-secondary">Password</label>
        <Password id="password" v-model="password" toggleMask placeholder="••••••••" fluid inputClass="w-full" :invalid="!!errors.password" />
        <small v-if="errors.password" class="text-red-500">{{ errors.password }}</small>
      </div>

      <div class="flex flex-column gap-2 mb-5">
        <label for="repeatWith" class="font-semibold text-secondary">Repeat Password</label>
        <Password id="repeatWith" v-model="repeatPassword" :feedback="false" toggleMask placeholder="••••••••" fluid inputClass="w-full" :invalid="!!errors.repeatPassword" />
        <small v-if="errors.repeatPassword" class="text-red-500">{{ errors.repeatPassword }}</small>
      </div>

      <Message v-if="errors.general" severity="error" class="mb-3" :closable="false">{{ errors.general }}</Message>

      <Button label="Register" icon="pi pi-check-circle" class="w-full mb-3" @click="register" :loading="loading" />

      <div class="text-center">
        <span class="text-secondary">Already have an account? </span>
        <NuxtLink to="/user/login" class="text-primary no-underline hover:underline font-medium">Login</NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from '#app'; // explicitly import if auto-import fails mentally for clarity, though not strictly needed in Nuxt

const router = useRouter();
const email = ref('');
const password = ref('');
const repeatPassword = ref('');
const loading = ref(false);

interface Violation {
  field: string;
  message: string;
}

interface ApiError {
  title: string;
  status: number;
  violations: Violation[];
}

const errors = reactive({
  email: '',
  password: '',
  repeatPassword: '',
  general: ''
});

function validate() {
  errors.email = '';
  errors.password = '';
  errors.repeatPassword = '';
  errors.general = '';
  
  let valid = true;

  if (password.value !== repeatPassword.value) {
    errors.repeatPassword = 'Passwords do not match';
    valid = false;
  }
  
  return valid;
}

async function register() {
  if (!validate()) return;
  
  loading.value = true;
  
  try {
    await $fetch('/api/users', {
      method: 'POST',
      body: {
        email: email.value,
        password: password.value
      }
    });

    // Success - redirect to login
    router.push('/user/login');
  } catch (err: any) {
    if (err.response?._data) {
      const data = err.response._data as ApiError;
      
      if (data.status === 400 && data.violations) {
        data.violations.forEach((violation) => {
          if (violation.field === 'create.user.email') {
            errors.email = violation.message;
          } else if (violation.field === 'create.user.password') {
            errors.password = violation.message;
          } else {
             // Fallback for unexpected fields if any, or maybe show in general
             errors.general = violation.message;
          }
        });
      } else {
        errors.general = 'An error occurred. Please try again.';
      }
    } else {
       errors.general = 'Connection error. Please check your network.';
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
  flex: 1;
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
