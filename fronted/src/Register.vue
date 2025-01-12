<template>
  <div class="register">
    <el-form :model="form" label-width="auto">
      <el-form-item label="Account">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="form.password" />
      </el-form-item>
      <el-form-item label="Confirm Password">
        <el-input v-model="form.confirmPassword" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="loading">Submit</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="js">
import { reactive, ref } from 'vue'

const loading = ref(false)
const form = reactive({
  account: '',
  password: '',
  confirmPassword: ''
})

const onSubmit = async () => {
  loading.value = true
  await axios.post("http://localhost:8091/user/register", form)
  loading.value = false
}
</script>

<style lang="scss" scoped>
.login {
  display: flex;
}
</style>