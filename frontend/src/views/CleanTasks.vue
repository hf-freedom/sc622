<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>清洁任务统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="5">
          <div style="text-align: center; padding: 15px; background: #f5f7fa; border-radius: 8px">
            <div style="font-size: 28px; font-weight: bold; color: #909399">{{ taskStats.total }}</div>
            <div style="color: #909399; margin-top: 5px">总任务</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div style="text-align: center; padding: 15px; background: #ecf5ff; border-radius: 8px">
            <div style="font-size: 28px; font-weight: bold; color: #409eff">{{ taskStats.pending }}</div>
            <div style="color: #409eff; margin-top: 5px">待领取</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div style="text-align: center; padding: 15px; background: #fdf6ec; border-radius: 8px">
            <div style="font-size: 28px; font-weight: bold; color: #e6a23c">{{ taskStats.inProgress }}</div>
            <div style="color: #e6a23c; margin-top: 5px">清洁中</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div style="text-align: center; padding: 15px; background: #fff7e6; border: 2px solid #e6a23c; border-radius: 8px">
            <div style="display: flex; align-items: center; justify-content: center">
              <el-icon style="color: #e6a23c; margin-right: 5px"><Warning /></el-icon>
              <span style="font-size: 28px; font-weight: bold; color: #e6a23c">{{ taskStats.pendingReview }}</span>
            </div>
            <div style="color: #e6a23c; font-weight: bold; margin-top: 5px">待复查</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div style="text-align: center; padding: 15px; background: #f0f9ff; border-radius: 8px">
            <div style="font-size: 28px; font-weight: bold; color: #67c23a">{{ taskStats.completed }}</div>
            <div style="color: #67c23a; margin-top: 5px">已完成</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>清洁任务管理</span>
          <el-button type="primary" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="tasks" border stripe>
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.priority >= 2" type="danger" size="small">紧急</el-tag>
            <el-tag v-else-if="row.priority >= 1" type="warning" size="small">优先</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)" :effect="row.status === 'PENDING_REVIEW' ? 'dark' : 'light'" size="small">
              <el-icon v-if="row.status === 'PENDING_REVIEW'" style="margin-right: 3px">
                <Warning />
              </el-icon>
              {{ getTaskStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="cleaner" label="保洁员" width="100" align="center" />
        <el-table-column prop="claimTime" label="领取时间" width="160" align="center">
          <template #default="{ row }">
            {{ row.claimTime ? formatDate(row.claimTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.completeTime" style="color: #e6a23c">
              {{ formatDate(row.completeTime) }}
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="cleanDuration" label="清洁耗时" width="100" align="center">
          <template #default="{ row }">
            {{ row.cleanDuration ? row.cleanDuration + ' 分钟' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="退回原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="300">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING' || row.status === 'REJECTED'"
              type="primary"
              size="small"
              @click="showClaimDialog(row)"
            >
              领取任务
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              type="success"
              size="small"
              @click="completeTask(row.id)"
            >
              完成清洁
            </el-button>
            <el-button
              v-if="row.status === 'PENDING_REVIEW'"
              type="warning"
              size="small"
              @click="showReviewDialog(row)"
            >
              复查
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="claimDialogVisible" title="领取清洁任务" width="400px">
      <el-form :model="claimForm" label-width="80px">
        <el-form-item label="房间号">
          <el-input v-model="claimForm.roomNo" disabled />
        </el-form-item>
        <el-form-item label="保洁员姓名">
          <el-input v-model="claimForm.cleaner" placeholder="请输入保洁员姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="claimDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitClaim">确认领取</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="主管复查清洁任务" width="500px">
      <el-alert
        title="请仔细检查房间清洁质量，确认无误后点击通过"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #icon>
          <el-icon><Warning /></el-icon>
        </template>
      </el-alert>
      
      <el-descriptions :column="2" border size="small" style="margin-bottom: 20px">
        <el-descriptions-item label="房间号" label-align="right">
          <strong>{{ reviewForm.roomNo }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="保洁员" label-align="right">
          {{ reviewForm.cleaner }}
        </el-descriptions-item>
        <el-descriptions-item label="清洁完成时间" label-align="right">
          {{ formatDate(reviewForm.completeTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="清洁耗时" label-align="right">
          {{ reviewForm.cleanDuration }} 分钟
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">复查操作</el-divider>

      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="复查员">
          <el-input v-model="reviewForm.inspector" placeholder="请输入复查员姓名" />
        </el-form-item>
        <el-form-item label="复查结果" required>
          <el-radio-group v-model="reviewForm.passed" size="large">
            <el-radio :label="true" border style="margin-right: 30px">
              <el-icon style="color: #67c23a; margin-right: 5px"><Check /></el-icon>
              <span style="color: #67c23a; font-weight: bold">通过</span>
            </el-radio>
            <el-radio :label="false" border>
              <el-icon style="color: #f56c6c; margin-right: 5px"><Close /></el-icon>
              <span style="color: #f56c6c; font-weight: bold">不通过</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!reviewForm.passed" label="退回原因" required>
          <el-input
            type="textarea"
            v-model="reviewForm.rejectReason"
            :rows="4"
            placeholder="请详细描述清洁不合格的原因，以便保洁员重新清洁"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanTaskApi, roomApi, bookingApi } from '../api'
import { Refresh, Warning, Check, Close } from '@element-plus/icons-vue'

const tasks = ref([])
const rooms = ref([])
const bookings = ref([])
const claimDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const claimForm = ref({
  id: null,
  roomNo: '',
  cleaner: ''
})
const reviewForm = ref({
  id: null,
  roomNo: '',
  inspector: '',
  passed: true,
  rejectReason: ''
})

const taskStats = computed(() => {
  const stats = {
    total: tasks.value.length,
    pending: 0,
    inProgress: 0,
    pendingReview: 0,
    completed: 0
  }
  tasks.value.forEach(task => {
    if (task.status === 'PENDING' || task.status === 'REJECTED') {
      stats.pending++
    } else if (task.status === 'IN_PROGRESS') {
      stats.inProgress++
    } else if (task.status === 'PENDING_REVIEW') {
      stats.pendingReview++
    } else if (task.status === 'COMPLETED') {
      stats.completed++
    }
  })
  return stats
})

const getTaskStatusType = (status) => {
  const types = {
    PENDING: 'info',
    IN_PROGRESS: 'primary',
    PENDING_REVIEW: 'warning',
    REJECTED: 'danger',
    COMPLETED: 'success'
  }
  return types[status] || 'info'
}

const getTaskStatusText = (status) => {
  const texts = {
    PENDING: '待领取',
    IN_PROGRESS: '清洁中',
    PENDING_REVIEW: '待复查',
    REJECTED: '已退回',
    COMPLETED: '已完成'
  }
  return texts[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadData = async () => {
  try {
    const [tasksRes, roomsRes, bookingsRes] = await Promise.all([
      cleanTaskApi.getAll(),
      roomApi.getAll(),
      bookingApi.getAll()
    ])
    tasks.value = tasksRes.data
    rooms.value = roomsRes.data
    bookings.value = bookingsRes.data
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const showClaimDialog = (task) => {
  claimForm.value = {
    id: task.id,
    roomNo: task.roomNo,
    cleaner: ''
  }
  claimDialogVisible.value = true
}

const submitClaim = async () => {
  try {
    await cleanTaskApi.claim(claimForm.value.id, claimForm.value.cleaner)
    ElMessage.success('任务领取成功')
    claimDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('领取失败')
  }
}

const completeTask = async (id) => {
  try {
    await cleanTaskApi.complete(id)
    ElMessage.success('清洁完成，请等待复查')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showReviewDialog = (task) => {
  reviewForm.value = {
    id: task.id,
    roomNo: task.roomNo,
    cleaner: task.cleaner,
    completeTime: task.completeTime,
    cleanDuration: task.cleanDuration,
    inspector: '',
    passed: true,
    rejectReason: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  try {
    await cleanTaskApi.review(
      reviewForm.value.id,
      reviewForm.value.inspector,
      reviewForm.value.passed,
      reviewForm.value.rejectReason
    )
    ElMessage.success(reviewForm.value.passed ? '复查通过' : '已退回重新清洁')
    reviewDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
  setInterval(loadData, 30000)
})
</script>
