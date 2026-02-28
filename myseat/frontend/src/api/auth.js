import api from './client'

export const login = async (payload) => {
  const { data } = await api.post('/auth/login', payload)
  return data
}
