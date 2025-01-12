<template>
  <div class="login">
    <el-form :model="form" label-width="auto" label-position="top">
      <el-form-item label="Account">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="form.password" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="loading">Submit</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="js">
import { reactive } from 'vue'

const loading = ref(false)
const form = reactive({
  account: '',
  password: '',
})

const onSubmit = async () => {
  loading.value = true
  await axios.post("http://localhost:8091/user/login", form)
  loading.value = false
}
</script>

<style lang="scss" scoped>
.login {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>