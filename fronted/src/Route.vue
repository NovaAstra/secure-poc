<template>
  <div class="route">
    <el-table :data="data">
      <el-table-column prop="code" label="Code" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="url" label="Url" />
      <el-table-column prop="method" label="Method" />
      <el-table-column prop="params" label="Params" />
    </el-table>
  </div>
</template>

<script setup lang="js">
import { ref } from 'vue'

const data = ref([])
</script>