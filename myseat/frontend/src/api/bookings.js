import api from './client'

export const createBooking = async (payload) => {
  const { data } = await api.post('/bookings', payload)
  return data
}
