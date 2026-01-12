<template>
  <div class="page-container">
    <div v-if="pending" class="loading-state">
      <ProgressSpinner />
    </div>

    <div v-else-if="error" class="error-state">
      <div class="text-center">
        <h2 class="text-red-500 mb-3">Error Loading Form</h2>
        <p class="text-secondary mb-4">{{ error.message }}</p>
        <Button label="Go Back" icon="pi pi-arrow-left" @click="router.push('/')" />
      </div>
    </div>

    <div v-else class="form-builder-wrapper">
      <!-- Header Section -->
      <div class="header-card">
        <div class="flex flex-column gap-3">
          <InputText v-model="formTitle" class="form-title-input" placeholder="Untitled Form" unstyled />
          <Textarea v-model="formDescription" class="form-desc-input" placeholder="Form description" rows="1" autoResize unstyled />
        </div>
      </div>

      <!-- Blocks List -->
      <div class="blocks-container">
        <div v-for="(block, index) in blocks" :key="index" class="block-card">
          <!-- Block Header: Title, Desc, Type -->
          <div class="block-header">
             <div class="flex-1 flex flex-column gap-2">
               <InputText v-model="block.title" placeholder="Question Title" class="block-title-input" />
             </div>
             <div class="block-type-selector">
                <SelectButton v-model="block.type" :options="typeOptions" optionLabel="label" optionValue="value" :allowEmpty="false" />
             </div>
          </div>

          <!-- Options Section -->
          <div class="options-list">
             <div v-for="(option, optIndex) in block.options" :key="optIndex" class="option-item">
                <div class="option-marker">
                   <RadioButton v-if="block.type === 'radio'" disabled />
                   <Checkbox v-else disabled />
                </div>
                <InputText v-model="option.label" placeholder="Option " class="option-input" />
                <Button icon="pi pi-times" variant="text" severity="danger" rounded @click="removeOption(index, optIndex)" v-if="block.options.length > 1" />
             </div>
             
             <!-- Add Option -->
             <div class="option-item add-option">
                <div class="option-marker">
                   <i class="pi pi-circle text-secondary" v-if="block.type === 'radio'" style="font-size: 0.8rem"></i>
                   <i class="pi pi-stop text-secondary" v-else style="font-size: 0.8rem"></i>
                </div>
                <Button label="Add option" variant="text" size="small" class="text-primary p-0" @click="addOption(index)" />
             </div>
          </div>

          <!-- Block Footer -->
          <div class="block-footer">
             <Button icon="pi pi-trash" aria-label="Delete" severity="danger" variant="text" rounded @click="removeBlock(index)" :disabled="blocks.length === 1" />
             <div class="divider"></div>
             <label class="flex align-items-center gap-2 text-sm text-secondary">
               <span class="font-medium">Required</span>
               <ToggleSwitch v-model="block.required" />
             </label>
          </div>
        </div>
      </div>

      <!-- Floating Action Buttons -->
      <div class="actions-bar">
         <Button icon="pi pi-chart-bar" rounded raised severity="info" size="large" @click="showStats" v-tooltip.left="'View Stats'" />
         <Button icon="pi pi-plus" rounded raised size="large" @click="addBlock" v-tooltip.left="'Add Question'" />
      </div>
      
      <!-- Save Button -->
      <div class="save-bar">
         <Button label="Update Form" icon="pi pi-check" @click="updateForm" :loading="saving" />
      </div>
    </div>

    <!-- Stats Dialog -->
    <Dialog v-model:visible="showStatsDialog" modal header="Form Statistics" :style="{ width: '50vw' }" maximizable>
       <div v-if="loadingStats" class="flex justify-content-center p-5">
          <ProgressSpinner />
       </div>
       <div v-else-if="statsData" class="flex flex-column gap-4">
          <div v-for="section in statsData.sections" :key="section.title" class="stats-section">
             <h3>{{ section.title }}</h3>
             <div class="stats-options mt-2">
                <div v-for="option in section.options" :key="option.value" class="flex justify-content-between align-items-center p-2 surface-hover border-round mb-1">
                   <span>{{ option.value }}</span>
                   <Badge :value="option.count" severity="info" />
                </div>
             </div>
          </div>
       </div>
       <div v-else class="text-center p-5 text-secondary">
          No statistics available.
       </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter, useRoute, useCookie, useFetch } from '#app';

definePageMeta({
  middleware: ['auth']
});

const router = useRouter();
const route = useRoute();
const token = useCookie('auth_token');
const formId = route.params.id;

const typeOptions = [
  { label: 'Multiple Choice', value: 'radio' },
  { label: 'Checkboxes', value: 'checkbox' }
];

const formTitle = ref('Untitled Form');
const formDescription = ref('');
const saving = ref(false);

interface FormOption {
  label: string;
}

interface FormBlock {
  type: 'radio' | 'checkbox';
  title: string;
  required: boolean;
  options: FormOption[];
}

const blocks = ref<FormBlock[]>([]);

// Fetch existing form data
const { data: formData, pending, error } = await useFetch<any>(`/api/forms/${formId}`, {
  headers: {
    Authorization: `Bearer ${token.value}`
  }
});

if (formData.value) {
  formTitle.value = formData.value.title;
  formDescription.value = formData.value.description;
  
  // Transform backend sections to frontend blocks
  blocks.value = formData.value.sections.map((section: any) => ({
    title: section.title,
    type: section.type === 'RADIO_BOX' ? 'radio' : 'checkbox',
    required: section.isRequired || section.required || false, // Handle both loose backend cases if needed
    options: section.options.map((opt: string) => ({ label: opt }))
  }));
}

// Ensure at least one block exists
if (blocks.value.length === 0) {
  addBlock();
}

function addBlock() {
  blocks.value.push({
    type: 'radio',
    title: '',
    required: false,
    options: [{ label: 'Option 1' }]
  });
}

function removeBlock(index: number) {
  blocks.value.splice(index, 1);
}

function addOption(blockIndex: number) {
  const block = blocks.value[blockIndex];
  block.options.push({ label: `Option ${block.options.length + 1}` });
}

function removeOption(blockIndex: number, optionIndex: number) {
  blocks.value[blockIndex].options.splice(optionIndex, 1);
}

async function updateForm() {
  saving.value = true;
  
  // Transform frontend blocks to backend schema
  const sections = blocks.value.map(block => ({
    title: block.title || 'Untitled Question',
    type: block.type === 'radio' ? 'RADIO_BOX' : 'CHECK_BOX',
    isRequired: block.required,
    options: block.options.map(opt => opt.label || 'Option')
  }));
  
  const payload = {
    title: formTitle.value || 'Untitled Form',
    description: formDescription.value,
    sections: sections
  };

  try {
    await $fetch(`/api/forms/${formId}`, {
      method: 'PUT', // Assuming PUT for update
      headers: {
        Authorization: `Bearer ${token.value}`
      },
      body: payload
    });
    
    alert('Form updated successfully');
  } catch (error) {
    console.error('Failed to update form', error);
    alert('Failed to update form. Please try again.');
  } finally {
    saving.value = false;
  }
}

// Stats Logic
const showStatsDialog = ref(false);
const statsData = ref<any>(null);
const loadingStats = ref(false);

async function showStats() {
  showStatsDialog.value = true;
  loadingStats.value = true;
  
  try {
    const data = await $fetch(`/api/stats/${formId}`, {
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });
    statsData.value = data;
  } catch (err) {
    console.error('Failed to fetch stats', err);
    // Handle error (maybe show a toast)
  } finally {
    loadingStats.value = false;
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;
  padding: 2rem 1rem 6rem 1rem;
  min-height: 100vh;
  background-color: var(--bg-color);
}

.loading-state, .error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  width: 100%;
}

.form-builder-wrapper {
  width: 100%;
  max-width: 770px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: relative;
}

/* Card Common Styles */
.header-card, .block-card {
  background-color: #18181b; /* Zinc 900 */
  border: 1px solid #27272a; /* Zinc 800 */
  border-radius: 8px;
  padding: 1.5rem;
  transition: all 0.2s ease;
}

/* Header Specifics */
.header-card {
  border-top: 10px solid var(--primary-color);
}

.form-title-input {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  border: none;
  border-bottom: 1px solid transparent;
  padding: 0.5rem 0;
  width: 100%;
  background: transparent;
}
.form-title-input:focus {
  border-bottom-color: var(--primary-color);
  outline: none;
}

.form-desc-input {
  font-size: 1rem;
  color: #a1a1aa;
  border: none;
  border-bottom: 1px solid #27272a;
  padding: 0.5rem 0;
  width: 100%;
  background: transparent;
  resize: none;
}
.form-desc-input:focus {
  border-bottom-color: var(--primary-color);
  outline: none;
}

/* Block Styles */
.block-card {
  position: relative;
  border-left: 6px solid transparent;
}
.block-card:focus-within {
  border-left-color: var(--primary-color);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.5);
}

.block-header {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  align-items: flex-start;
  flex-wrap: wrap;
}

.block-title-input {
  background: #27272a;
  border: 1px solid transparent;
  padding: 1rem;
  font-size: 1.1rem;
}
.block-title-input:focus {
  border-color: var(--primary-color);
}

/* Options Styles */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.option-marker {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
}

.option-input {
  flex: 1;
  background: transparent;
  border: none;
  border-bottom: 1px solid transparent;
  padding: 0.5rem 0;
  font-size: 0.95rem;
}
.option-input:hover {
  border-bottom-color: #3f3f46;
}
.option-input:focus {
  border-bottom-color: var(--primary-color);
  outline: none;
}

/* Footer & Actions */
.block-footer {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #27272a;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 1rem;
}

.divider {
  width: 1px;
  height: 20px;
  background-color: #3f3f46;
}

.actions-bar {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  z-index: 100;
}

.save-bar {
    display: flex;
    justify-content: flex-end;
    margin-top: 1rem;
}

/* Utilities */
.text-secondary { color: #a1a1aa; }
.text-primary { color: var(--primary-color); }
</style>
