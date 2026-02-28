import api from './client'

export const getEvents = async () => {
  const { data } = await api.get('/events')
  return data
}

export const getEventById = async (eventId) => {
  const { data } = await api.get(`/events/${eventId}`)
  return data
}

export const getEventSeats = async (eventId) => {
  const { data } = await api.get(`/events/${eventId}/seats`)
  return data
}

export const getVenues = async () => {
  const { data } = await api.get('/venues')
  return data
}

export const createVenue = async (payload) => {
  const { data } = await api.post('/venues', payload)
  return data
}

export const createEvent = async (payload) => {
  const { data } = await api.post('/events', payload)
  return data
}

export const createSection = async (payload) => {
  const { data } = await api.post('/sections', payload)
  return data
}
