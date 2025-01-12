<template>
  <div class="route">
    <div>
      <el-button type="primary" @click="onOpen">Add</el-button>
    </div>

    <el-table :data="data">
      <el-table-column prop="code" label="Code" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="url" label="Url" />
      <el-table-column prop="method" label="Method" />
      <el-table-column prop="params" label="Params" />
      <el-table-column prop="query" label="Query" />
      <el-table-column prop="status" label="Status" />
      <el-table-column prop="header" label="Header" />
      <el-table-column fixed="right" label="Operations">
        <template #default>
          <el-button link type="primary" size="small" @click="onOpen">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" width="500">
      <el-form :model="form" label-width="auto" style="max-width: 600px">
        <el-form-item label="Code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Url">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="Method">
          <el-input v-model="form.method" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="onSave" :loading="loading">
            Save
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
import { ref, reactive } from 'vue'
import axios from "axios"

const data = ref([])
const visible = ref(false)
const loading = ref(false)
const form = reactive({
  code: "",
  name: '',
  url: '',
  method: ""
})

const onOpen = (row) => {
  if (row) {
    Object.assign(form, row)
  }

  visible.value = true
}

const onAdd = async () => {
  loading.value = true
  await axios.post("http://localhost:8091/route/add", form)
  loading.value = false
}

const onEdit = async () => {
  loading.value = true
  await axios.post("http://localhost:8091/route/update", form)
  loading.value = false
}

const onSave = () => {
  form.id ? onEdit() : onAdd()
}
</script>

<style lang="scss" scoped>
.route {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>