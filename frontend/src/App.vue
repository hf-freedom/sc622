<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #233042">
      <div style="color: white; text-align: center; padding: 20px 0; font-size: 18px; font-weight: bold">
        🏨 酒店房态系统
      </div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        router
        background-color="#233042"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/rooms">
          <el-icon><OfficeBuilding /></el-icon>
          <span>房间管理</span>
        </el-menu-item>
        <el-menu-item index="/bookings">
          <el-icon><Calendar /></el-icon>
          <span>预订管理</span>
        </el-menu-item>
        <el-menu-item index="/clean-tasks">
          <el-icon><Box /></el-icon>
          <span>清洁任务</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between">
        <span style="font-size: 20px; font-weight: bold">{{ pageTitle }}</span>
        <span style="color: #666">{{ currentTime }}</span>
      </el-header>
      <el-main style="background-color: #f5f7fa">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { DataLine, OfficeBuilding, Calendar, Box } from '@element-plus/icons-vue'

const route = useRoute()
const currentTime = ref('')

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': '数据概览',
    '/rooms': '房间管理',
    '/bookings': '预订管理',
    '/clean-tasks': '清洁任务'
  }
  return titles[route.path] || ''
})

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
