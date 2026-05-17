<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>房间状态概览</span>
          <div style="display: flex; gap: 15px">
            <el-tag type="success">可预订: {{ roomStats.available }}</el-tag>
            <el-tag type="warning">已入住: {{ roomStats.occupied }}</el-tag>
            <el-tag type="primary">清洁中: {{ roomStats.cleaning }}</el-tag>
            <el-tag type="info">待复查: {{ roomStats.pending }}</el-tag>
            <el-tag type="danger">维修中: {{ roomStats.repair }}</el-tag>
          </div>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="room in rooms" :key="room.id">
          <el-card
            shadow="hover"
            :body-style="{ padding: '15px' }"
            style="margin-bottom: 15px; cursor: pointer"
            @click="showRoomDetail(room)"
          >
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
              <span style="font-size: 18px; font-weight: bold">🏠 {{ room.roomNo }}</span>
              <el-tag :type="getStatusType(room.status)" size="small">
                {{ getStatusText(room.status) }}
              </el-tag>
            </div>
            <div style="font-size: 14px; color: #666; margin-bottom: 5px">
              {{ room.roomType }} · ¥{{ room.price }}/晚
            </div>
            <div v-if="room.status === 'OCCUPIED'" style="font-size: 12px; color: #e6a23c">
              👤 {{ getGuestName(room.id) }}
            </div>
            <div v-else-if="room.status === 'OUT_OF_SERVICE'" style="font-size: 12px; color: #f56c6c">
              🚧 {{ room.repairDescription || '维修中' }}
            </div>
            <div v-else style="font-size: 12px; color: #67c23c">
              ✓ 空闲可预订
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>房间详情列表</span>
          <el-button type="primary" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="rooms" border stripe>
        <el-table-column prop="roomNo" label="房间号" width="100" align="center" />
        <el-table-column prop="roomType" label="房型" width="120" align="center" />
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前客人" min-width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'OCCUPIED'" style="color: #e6a23c">
              {{ getGuestName(row.id) }}
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="入住时间" width="180" align="center">
          <template #default="{ row }">
            <span v-if="row.checkInTime" style="font-size: 12px">
              {{ formatDate(row.checkInTime) }}
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="250">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'OCCUPIED'"
              type="primary"
              size="small"
              @click.stop="handleCheckOut(row)"
            >
              办理退房
            </el-button>
            <el-button
              v-if="row.status !== 'OUT_OF_SERVICE'"
              type="danger"
              size="small"
              @click.stop="showRepairDialog(row)"
            >
              报修
            </el-button>
            <el-button
              v-if="row.status === 'OUT_OF_SERVICE'"
              type="success"
              size="small"
              @click.stop="fixRoom(row)"
            >
              修复完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="repairDialogVisible" title="房间报修" width="400px">
      <el-form :model="repairForm" label-width="80px">
        <el-form-item label="房间号">
          <el-input v-model="repairForm.roomNo" disabled />
        </el-form-item>
        <el-form-item label="报修描述">
          <el-input type="textarea" v-model="repairForm.description" :rows="4" placeholder="请输入报修描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roomApi, bookingApi, cleanTaskApi } from '../api'
import { Refresh, View } from '@element-plus/icons-vue'

const rooms = ref([])
const bookings = ref([])
const cleanTasks = ref([])
const repairDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedRoom = ref(null)
const loading = ref(false)

const repairForm = ref({
  id: null,
  roomNo: '',
  description: ''
})

const roomStats = computed(() => {
  const stats = {
    available: 0,
    occupied: 0,
    cleaning: 0,
    pending: 0,
    repair: 0
  }
  rooms.value.forEach(room => {
    if (room.status === 'AVAILABLE') stats.available++
    else if (room.status === 'OCCUPIED') stats.occupied++
    else if (room.status === 'CLEANING') stats.cleaning++
    else if (room.status === 'PENDING_INSPECTION') stats.pending++
    else if (room.status === 'OUT_OF_SERVICE') stats.repair++
  })
  return stats
})

const getStatusType = (status) => {
  const types = {
    AVAILABLE: 'success',
    OCCUPIED: 'warning',
    CLEANING: 'primary',
    PENDING_INSPECTION: 'info',
    OUT_OF_SERVICE: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    AVAILABLE: '可预订',
    OCCUPIED: '已入住',
    CLEANING: '清洁中',
    PENDING_INSPECTION: '待复查',
    OUT_OF_SERVICE: '维修中'
  }
  return texts[status] || status
}

const getGuestName = (roomId) => {
  const booking = bookings.value.find(
    b => b.roomId === roomId && b.checkedIn
  )
  return booking ? booking.guestName : '-'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadData = async () => {
  try {
    const [roomsRes, bookingsRes, tasksRes] = await Promise.all([
      roomApi.getAll(),
      bookingApi.getAll(),
      cleanTaskApi.getAll()
    ])
    rooms.value = roomsRes.data
    bookings.value = bookingsRes.data
    cleanTasks.value = tasksRes.data
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const showRoomDetail = (room) => {
  selectedRoom.value = room
  detailDialogVisible.value = true
}

const getRoomTask = (roomId) => {
  return cleanTasks.value.find(
    t => t.roomId === roomId && t.status !== 'COMPLETED'
  )
}

const handleCheckOut = async (room) => {
  try {
    await ElMessageBox.confirm(
      `确认办理 ${room.roomNo} 房间退房？系统将自动生成清洁任务。`,
      '退房确认',
      {
        confirmButtonText: '确认退房',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const booking = bookings.value.find(
      b => b.roomId === room.id && b.checkedIn
    )
    if (!booking) {
      ElMessage.error('未找到该房间的入住记录')
      return
    }
    
    loading.value = true
    await bookingApi.checkOut(booking.id)
    ElMessage.success('退房成功，已生成清洁任务')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '退房失败')
    }
  } finally {
    loading.value = false
  }
}

const showRepairDialog = (room) => {
  repairForm.value = {
    id: room.id,
    roomNo: room.roomNo,
    description: ''
  }
  repairDialogVisible.value = true
}

const submitRepair = async () => {
  try {
    await roomApi.reportRepair(repairForm.value.id, repairForm.value.description)
    ElMessage.success('报修成功')
    repairDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('报修失败')
  }
}

const fixRoom = async (room) => {
  try {
    await roomApi.fixRoom(room.id)
    ElMessage.success('房间已修复')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>
