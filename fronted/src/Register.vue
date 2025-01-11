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
        <el-input v-model="form.confirmation" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="js">
import { reactive } from 'vue'

const form = reactive({
  account: '',
  password: '',
  confirmation: ''
})
</script>

<style lang="scss" scoped>
.login {
  display: flex;
}
</style>