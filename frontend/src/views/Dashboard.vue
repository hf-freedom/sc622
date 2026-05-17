<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 40px; color: #67c23a">{{ statistics.totalRooms }}</div>
            <div style="color: #909399; margin-top: 10px">总房间数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 40px; color: #e6a23c">{{ statistics.occupiedRooms }}</div>
            <div style="color: #909399; margin-top: 10px">已入住</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 40px; color: #409eff">{{ statistics.cleaningRooms }}</div>
            <div style="color: #909399; margin-top: 10px">清洁中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 40px; color: #f56c6c">{{ statistics.repairRooms }}</div>
            <div style="color: #909399; margin-top: 10px">维修中</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>房间周转率</span>
            </div>
          </template>
          <div style="text-align: center; padding: 20px 0">
            <el-progress type="dashboard" :percentage="Math.round(statistics.turnoverRate * 100)" :color="colors" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>平均清洁耗时</span>
            </div>
          </template>
          <div style="text-align: center; padding: 30px 0">
            <div style="font-size: 36px; color: #409eff">{{ statistics.avgCleanDuration.toFixed(1) }}</div>
            <div style="color: #909399; margin-top: 10px">分钟</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>维修影响天数</span>
            </div>
          </template>
          <div style="text-align: center; padding: 30px 0">
            <div style="font-size: 36px; color: #f56c6c">{{ statistics.totalRepairDays }}</div>
            <div style="color: #909399; margin-top: 10px">天</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>系统预警</span>
          <el-badge :value="alerts.length" class="item" type="danger">
            <el-icon><Bell /></el-icon>
          </el-badge>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(alert, index) in alerts.slice(0, 10)"
          :key="index"
          :timestamp="alert.split(' - ')[0]"
          placement="top"
          type="danger"
        >
          {{ alert.split(' - ')[1] }}
        </el-timeline-item>
        <el-timeline-item v-if="alerts.length === 0" type="success">
          暂无预警信息
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { statisticsApi } from '../api'
import { Bell } from '@element-plus/icons-vue'

const statistics = ref({
  totalRooms: 0,
  occupiedRooms: 0,
  cleaningRooms: 0,
  repairRooms: 0,
  turnoverRate: 0,
  avgCleanDuration: 0,
  totalRepairDays: 0
})
const alerts = ref([])
const colors = ['#67c23a', '#e6a23c', '#f56c6c']

const loadData = async () => {
  try {
    const [statsRes, alertsRes] = await Promise.all([
      statisticsApi.get(),
      statisticsApi.getAlerts()
    ])
    statistics.value = statsRes.data
    alerts.value = alertsRes.data
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
  setInterval(loadData, 10000)
})
</script>
