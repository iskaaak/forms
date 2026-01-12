<template>
  <div class="page-container">
    <div class="form-builder-wrapper">
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
         <Button icon="pi pi-plus" rounded raised size="large" @click="addBlock" v-tooltip.left="'Add Question'" />
      </div>
      
      <!-- Save Button -->
      <div class="save-bar">
         <Button label="Save Form" icon="pi pi-save" @click="saveForm" :loading="saving" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useCookie } from '#app';
// removed uuid import as we use simple generator

definePageMeta({
  middleware: ['auth']
});

const router = useRouter();
const token = useCookie('auth_token');



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

const blocks = ref<FormBlock[]>([
  {
    type: 'radio',
    title: '',
    required: false,
    options: [{ label: 'Option 1' }]
  }
]);

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

async function saveForm() {
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
    await $fetch('/api/forms', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token.value}`
      },
      body: payload
    });
    
    // On success, go back to home or show success message
    router.push('/');
  } catch (error) {
    console.error('Failed to save form', error);
    alert('Failed to save form. Please try again.');
  } finally {
    saving.value = false;
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
