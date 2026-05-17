<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>新建预订</span>
          <el-tag v-if="validationMessage" :type="validationType">
            {{ validationMessage }}
          </el-tag>
        </div>
      </template>
      <el-form :model="bookingForm" label-width="100px" inline :rules="bookingRules" ref="bookingFormRef">
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="bookingForm.roomId" placeholder="请选择房间" style="width: 200px" @change="validateBooking">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNo} - ${room.roomType} - ¥${room.price}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客人姓名" prop="guestName">
          <el-input v-model="bookingForm.guestName" placeholder="请输入姓名" style="width: 150px" />
        </el-form-item>
        <el-form-item label="联系电话" prop="guestPhone">
          <el-input v-model="bookingForm.guestPhone" placeholder="请输入电话" style="width: 150px" />
        </el-form-item>
        <el-form-item label="入住时间" prop="checkInDate">
          <el-date-picker
            v-model="bookingForm.checkInDate"
            type="datetime"
            placeholder="选择入住时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledCheckInDate"
            @change="validateBooking"
          />
        </el-form-item>
        <el-form-item label="退房时间" prop="checkOutDate">
          <el-date-picker
            v-model="bookingForm.checkOutDate"
            type="datetime"
            placeholder="选择退房时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledCheckOutDate"
            @change="validateBooking"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createBooking" :loading="submitting">提交预订</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>预订列表</span>
          <el-button type="primary" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="bookings" border stripe>
        <el-table-column prop="roomNo" label="房间号" width="100" align="center" />
        <el-table-column prop="guestName" label="客人姓名" width="120" align="center" />
        <el-table-column prop="guestPhone" label="联系电话" width="130" align="center" />
        <el-table-column prop="checkInDate" label="入住日期" width="180" align="center" />
        <el-table-column prop="checkOutDate" label="退房日期" width="180" align="center" />
        <el-table-column prop="checkedIn" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.checkedIn ? 'success' : 'warning'">
              {{ row.checkedIn ? '已入住' : '待入住' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="{ row }">
            <el-button
              v-if="!row.checkedIn"
              type="success"
              size="small"
              @click="handleCheckIn(row)"
            >
              办理入住
            </el-button>
            <el-button
              v-if="row.checkedIn"
              type="primary"
              size="small"
              @click="handleCheckOut(row)"
            >
              办理退房
            </el-button>
            <el-button
              v-if="!row.checkedIn"
              type="danger"
              size="small"
              @click="cancelBooking(row)"
            >
              取消预订
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { bookingApi, roomApi } from '../api'
import { Refresh, Check, Warning } from '@element-plus/icons-vue'

const bookings = ref([])
const availableRooms = ref([])
const bookingFormRef = ref(null)
const submitting = ref(false)
const validationMessage = ref('')
const validationType = ref('')

const bookingForm = ref({
  roomId: null,
  guestName: '',
  guestPhone: '',
  checkInDate: '',
  checkOutDate: ''
})

const bookingRules = {
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  guestName: [
    { required: true, message: '请输入客人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  guestPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住时间', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择退房时间', trigger: 'change' }
  ]
}

const disabledCheckInDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledCheckOutDate = (time) => {
  if (!bookingForm.value.checkInDate) return true
  const checkInTime = new Date(bookingForm.value.checkInDate).getTime()
  return time.getTime() <= checkInTime
}

const validateBooking = async () => {
  if (!bookingForm.value.roomId || !bookingForm.value.checkInDate || !bookingForm.value.checkOutDate) {
    validationMessage.value = ''
    return
  }
  try {
    const checkIn = new Date(bookingForm.value.checkInDate)
    const checkOut = new Date(bookingForm.value.checkOutDate)
    
    if (checkIn >= checkOut) {
      validationMessage.value = '⚠️ 入住日期必须早于退房日期'
      validationType.value = 'warning'
      return
    }
    
    const room = availableRooms.value.find(r => r.id === bookingForm.value.roomId)
    if (room) {
      const roomBookings = bookings.value.filter(
        b => b.roomId === bookingForm.value.roomId && !b.checkedIn
      )
      
      let hasConflict = false
      for (const booking of roomBookings) {
        const bookCheckIn = new Date(booking.checkInDate)
        const bookCheckOut = new Date(booking.checkOutDate)
        if (!(checkOut < bookCheckIn || checkIn > bookCheckOut)) {
          hasConflict = true
          validationMessage.value = `⚠️ 该房间在 ${booking.checkInDate} 至 ${booking.checkOutDate} 已有预订`
          validationType.value = 'danger'
          break
        }
      }
      
      if (!hasConflict) {
        validationMessage.value = '✓ 房型库存充足，可以预订'
        validationType.value = 'success'
      }
    }
  } catch (error) {
    validationMessage.value = ''
  }
}

const loadData = async () => {
  try {
    const [bookingsRes, roomsRes] = await Promise.all([
      bookingApi.getAll(),
      roomApi.getAvailable()
    ])
    bookings.value = bookingsRes.data
    availableRooms.value = roomsRes.data
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const createBooking = async () => {
  if (!bookingFormRef.value) return
  
  try {
    await bookingFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请完善预订信息')
    return
  }
  
  submitting.value = true
  try {
    await bookingApi.create(bookingForm.value)
    ElMessage.success('预订成功')
    validationMessage.value = ''
    bookingForm.value = {
      roomId: null,
      guestName: '',
      guestPhone: '',
      checkInDate: '',
      checkOutDate: ''
    }
    bookingFormRef.value.resetFields()
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data || '预订失败')
  } finally {
    submitting.value = false
  }
}

const handleCheckIn = async (booking) => {
  try {
    await ElMessageBox.confirm(
      `确认为 ${booking.guestName} 办理 ${booking.roomNo} 房间入住？`,
      '入住确认',
      {
        confirmButtonText: '确认入住',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    await bookingApi.checkIn(booking.id)
    ElMessage.success(`${booking.guestName} 已成功入住 ${booking.roomNo}`)
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '入住失败')
    }
  }
}

const handleCheckOut = async (booking) => {
  try {
    await ElMessageBox.confirm(
      `确认为 ${booking.guestName} 办理 ${booking.roomNo} 房间退房？系统将自动生成清洁任务。`,
      '退房确认',
      {
        confirmButtonText: '确认退房',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await bookingApi.checkOut(booking.id)
    ElMessage.success('退房成功，已生成清洁任务，请及时安排保洁')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '退房失败')
    }
  }
}

const cancelBooking = async (booking) => {
  try {
    await ElMessageBox.confirm(
      `确认取消 ${booking.guestName} 的 ${booking.roomNo} 房间预订？`,
      '取消预订',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留',
        type: 'warning'
      }
    )
    
    await bookingApi.delete(booking.id)
    ElMessage.success('预订已取消')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>
