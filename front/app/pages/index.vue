<template>
  <div class="hero">
    <i class="pi pi-file-edit" style="font-size: 4rem; color: var(--primary-color); margin-bottom: 1rem;"></i>
    <h1>Form Builder</h1>
    <p>
      Create, manage, and share powerful forms with ease.
    </p>
    <div style="display: flex; gap: 1rem; margin-top: 1rem;">
      <Button label="Create Forms" icon="pi pi-plus" size="large" @click="navigateTo('/forms/create')" />
    </div>
  </div>
  <!-- Forms List -->
  <div class="forms-grid">
     <div v-if="pending" class="text-center w-full py-5">
        <ProgressSpinner />
     </div>
     
     <div v-else-if="error" class="text-center w-full py-5 text-red-500">
        {{ error.message || 'Failed to load forms' }}
     </div>

     <div v-else v-for="form in forms" :key="form.id" class="form-card" @click="navigateTo('/forms/' + form.id)">
        <div class="form-icon">
           <i class="pi pi-file text-primary" style="font-size: 2rem"></i>
        </div>
        <div class="form-details">
           <h3>{{ form.title }}</h3>
           <p class="text-secondary text-sm">ID: {{ form.id }}</p>
        </div>
     </div>
     
     <div v-if="!pending && forms?.length === 0" class="text-center w-full py-5 text-secondary">
        No forms found. Create one to get started!
     </div>
  </div>
</template>

<script setup lang="ts">
import { useCookie, useFetch } from '#app';

definePageMeta({
  middleware: ['auth']
});

interface FormSummary {
  id: number;
  title: string;
}

const token = useCookie('auth_token');

const { data: forms, pending, error } = await useFetch<FormSummary[]>('/api/forms', {
  headers: {
    Authorization: `Bearer ${token.value}`
  }
});
</script>

<style scoped>
.forms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-top: 4rem;
  width: 100%;
}

.form-card {
  background: #18181b;
  border: 1px solid #27272a;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
}

.form-card:hover {
  transform: translateY(-4px);
  border-color: var(--primary-color);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5);
}

.form-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(52, 211, 153, 0.1); /* Transparent primary color */
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-details h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
}
</style>
