<script setup>
import ContactList from './components/ContactList.vue'
</script>

<template>
  <el-container>
    <el-header>
      <h1>通讯录管理系统</h1>
    </el-header>
    <el-main>
      <ContactList />
    </el-main>
  </el-container>
</template>

<style>
.el-header {
  background-color: var(--el-color-primary);
  color: white;
  text-align: center;
  line-height: 60px;
  margin-bottom: 20px;
}

.el-main {
  padding: 0 20px;
  background-color: var(--el-bg-color-page);
}

body {
  margin: 0;
  min-height: 100vh;
  background-color: var(--el-bg-color-page);
}
</style>
