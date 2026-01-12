<template>
  <div class="page-container">
    <div v-if="pending" class="loading-state">
      <ProgressSpinner />
    </div>

    <div v-else-if="error" class="error-state">
      <div class="text-center">
        <h2 class="text-red-500 mb-3">Error Loading Form</h2>
        <p class="text-secondary mb-4">{{ error.message || 'The form you are looking for does not exist or has been removed.' }}</p>
        <Button label="Go Home" icon="pi pi-home" @click="router.push('/')" />
      </div>
    </div>

    <div v-else class="public-form-wrapper">
      <!-- Title Card -->
      <div class="title-card">
        <h1>{{ formData.title }}</h1>
        <p v-if="formData.description">{{ formData.description }}</p>
      </div>

      <!-- Questions List -->
      <div class="questions-container">
        <div v-for="section in formData.sections" :key="section.id" class="question-card" :class="{ 'error-border': errors[section.id] }">
          <div class="question-header">
            <h3>{{ section.title }} <span v-if="section.required" class="text-red-500">*</span></h3>
          </div>

          <div class="options-container">
            <!-- Radio Options -->
            <div v-if="section.type === 'RADIO_BOX'" class="flex flex-column gap-3">
              <div v-for="option in section.options" :key="option" class="flex align-items-center">
                <RadioButton v-model="answers[section.id]" :inputId="`${section.id}-${option}`" :name="`section-${section.id}`" :value="option" />
                <label :for="`${section.id}-${option}`" class="ml-2 cursor-pointer">{{ option }}</label>
              </div>
            </div>

            <!-- Checkbox Options -->
            <div v-else-if="section.type === 'CHECK_BOX'" class="flex flex-column gap-3">
              <div v-for="option in section.options" :key="option" class="flex align-items-center">
                <Checkbox v-model="answers[section.id]" :inputId="`${section.id}-${option}`" :name="`section-${section.id}`" :value="option" />
                <label :for="`${section.id}-${option}`" class="ml-2 cursor-pointer">{{ option }}</label>
              </div>
            </div>
          </div>
          
          <small v-if="errors[section.id]" class="text-red-500 mt-2 block">This question is required.</small>
        </div>
      </div>

      <!-- Submit Section -->
      <div class="actions-bar">
        <Button label="Submit" icon="pi pi-send" size="large" @click="submitForm" :loading="submitting" />
        <Button label="Clear Form" class="p-button-text p-button-danger" @click="clearForm" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute, useFetch } from '#app';

const router = useRouter();
const route = useRoute();
const formId = route.params.id;

const answers = ref<Record<number, any>>({});
const errors = ref<Record<number, boolean>>({});
const submitting = ref(false);

// Fetch public form data (No Auth Header needed typically for public forms, usually)
const { data: formData, pending, error } = await useFetch<any>(`/api/public/${formId}`);

// Initialize answers for Checkboxes as arrays
if (formData.value && formData.value.sections) {
  formData.value.sections.forEach((section: any) => {
    if (section.type === 'CHECK_BOX') {
      answers.value[section.id] = [];
    }
  });
}

function validate() {
  errors.value = {};
  let isValid = true;
  
  if (!formData.value) return false;

  formData.value.sections.forEach((section: any) => {
    if (section.required) {
      const val = answers.value[section.id];
      if (!val || (Array.isArray(val) && val.length === 0)) {
        errors.value[section.id] = true;
        isValid = false;
      }
    }
  });

  return isValid;
}

async function submitForm() {
  if (!validate()) {
    // Scroll to first error?
    return;
  }

  submitting.value = true;
  
  // Transform answers to expected API format if needed. 
  // Assuming a simple map of sectionId -> answer(s) is acceptable, 
  // or a list of answers. 
  // Let's create a list of answers based on common patterns.
  
  const payload = {
     answers: Object.keys(answers.value).map(sectionId => ({
       sectionId: Number(sectionId),
       values: Array.isArray(answers.value[sectionId]) 
         ? answers.value[sectionId] 
         : [answers.value[sectionId]]
     }))
  };

  try {
    await $fetch(`/api/public/${formId}`, {
      method: 'POST',
      body: payload
    });
    
    // Redirect to a thank you page or show a success state
    alert('Thank you! Your response has been recorded.');
    // router.push('/thank-you'); // Optional
  } catch (err) {
    console.error('Submission failed', err);
    alert('Failed to submit form. Please try again.');
  } finally {
    submitting.value = false;
  }
}

function clearForm() {
  if (confirm('Clear all answers from the form?')) {
    answers.value = {};
    // Re-init arrays for checkboxes
    if (formData.value && formData.value.sections) {
      formData.value.sections.forEach((section: any) => {
        if (section.type === 'CHECK_BOX') {
          answers.value[section.id] = [];
        }
      });
    }
    errors.value = {};
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;
  padding: 2rem 1rem 6rem 1rem;
  min-height: 100vh;
  background-color: var(--bg-color); /* Usually lighter for public forms or same dark theme */
}

/* Override BG if we want a different look for public view, keeping dark for now */

.public-form-wrapper {
  width: 100%;
  max-width: 640px; /* Standard form width */
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.loading-state, .error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

/* Cards */
.title-card, .question-card {
  background-color: #18181b;
  border: 1px solid #27272a;
  border-radius: 8px;
  padding: 1.5rem;
}

.title-card {
  border-top: 10px solid var(--primary-color);
  margin-bottom: 0.5rem;
}

.title-card h1 {
  font-size: 2rem;
  font-weight: 400; /* Regular weight looks cleaner for forms */
  margin: 0 0 1rem 0;
  line-height: 1.3;
}

.title-card p {
  color: #a1a1aa;
  margin: 0;
  line-height: 1.5;
}

/* Questions */
.question-card {
  transition: all 0.2s ease;
}

.question-card.error-border {
  border: 1px solid #ef4444;
}

.question-header h3 {
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0 0 1.5rem 0;
}

.options-container {
  display: flex;
  flex-direction: column;
}

.ml-2 { margin-left: 0.5rem; }
.cursor-pointer { cursor: pointer; }

/* Actions */
.actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
}
</style>
