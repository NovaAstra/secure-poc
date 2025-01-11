<template>
  <div class="route">
    <el-button>Add</el-button>
    
    <el-table :data="data">
      <el-table-column prop="code" label="Code" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="url" label="Url" />
      <el-table-column prop="method" label="Method" />
      <el-table-column prop="params" label="Params" />
      <el-table-column prop="query" label="Query" />
      <el-table-column prop="header" label="Header" />
      <el-table-column fixed="right" label="Operations">
        <template #default>
          <el-button link type="primary" size="small">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="js">
import { ref } from 'vue'

const data = ref([])
</script>