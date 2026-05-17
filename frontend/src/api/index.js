import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8003/api',
  timeout: 10000
})

export const roomApi = {
  getAll: () => api.get('/rooms'),
  getById: (id) => api.get(`/rooms/${id}`),
  getAvailable: () => api.get('/rooms/available'),
  reportRepair: (id, description) => api.post(`/rooms/${id}/repair`, { description }),
  fixRoom: (id) => api.post(`/rooms/${id}/fix`)
}

export const bookingApi = {
  getAll: () => api.get('/bookings'),
  getById: (id) => api.get(`/bookings/${id}`),
  create: (data) => api.post('/bookings', data),
  checkIn: (id) => api.post(`/bookings/${id}/checkin`),
  checkOut: (id) => api.post(`/bookings/${id}/checkout`),
  delete: (id) => api.delete(`/bookings/${id}`)
}

export const cleanTaskApi = {
  getAll: () => api.get('/clean-tasks'),
  getById: (id) => api.get(`/clean-tasks/${id}`),
  create: (roomId) => api.post('/clean-tasks', { roomId }),
  claim: (id, cleaner) => api.post(`/clean-tasks/${id}/claim`, { cleaner }),
  complete: (id) => api.post(`/clean-tasks/${id}/complete`),
  review: (id, inspector, passed, rejectReason) => api.post(`/clean-tasks/${id}/review`, { inspector, passed, rejectReason })
}

export const statisticsApi = {
  get: () => api.get('/statistics'),
  getAlerts: () => api.get('/statistics/alerts')
}

export default api
