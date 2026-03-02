import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

api.interceptors.request.use((config) => {
  const rawUser = localStorage.getItem('myseat_user')
  if (!rawUser) {
    return config
  }

  try {
    const user = JSON.parse(rawUser)
    if (user?.token) {
      config.headers.Authorization = `Bearer ${user.token}`
    }
  } catch {
    localStorage.removeItem('myseat_user')
  }

  return config
})

export default api
