import { useEffect, useState } from 'react'
import { createEvent, createSection, createVenue, getVenues } from '../api/catalog'

function AdminPage() {
  const [venues, setVenues] = useState([])
  const [message, setMessage] = useState('')

  const [venueForm, setVenueForm] = useState({ name: '', address: '' })
  const [eventForm, setEventForm] = useState({ name: '', description: '', eventDate: '', venueId: '' })
  const [sectionForm, setSectionForm] = useState({
    name: '',
    price: '',
    totalRows: '',
    totalColumns: '',
    venueId: '',
  })

  const refreshVenues = async () => {
    const data = await getVenues()
    setVenues(data)
  }

  useEffect(() => {
    refreshVenues().catch(() => setMessage('Failed to load venues'))
  }, [])

  const handleVenueChange = (event) => {
    const { name, value } = event.target
    setVenueForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleEventChange = (event) => {
    const { name, value } = event.target
    setEventForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSectionChange = (event) => {
    const { name, value } = event.target
    setSectionForm((prev) => ({ ...prev, [name]: value }))
  }

  const submitVenue = async (event) => {
    event.preventDefault()
    try {
      await createVenue(venueForm)
      setVenueForm({ name: '', address: '' })
      setMessage('Venue created')
      await refreshVenues()
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to create venue')
    }
  }

  const submitEvent = async (event) => {
    event.preventDefault()
    try {
      await createEvent({
        name: eventForm.name,
        description: eventForm.description,
        eventDate: eventForm.eventDate,
        venueId: Number(eventForm.venueId),
      })
      setEventForm({ name: '', description: '', eventDate: '', venueId: '' })
      setMessage('Event created')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to create event')
    }
  }

  const submitSection = async (event) => {
    event.preventDefault()
    try {
      await createSection({
        name: sectionForm.name,
        price: Number(sectionForm.price),
        totalRows: Number(sectionForm.totalRows),
        totalColumns: Number(sectionForm.totalColumns),
        venueId: Number(sectionForm.venueId),
      })
      setSectionForm({
        name: '',
        price: '',
        totalRows: '',
        totalColumns: '',
        venueId: '',
      })
      setMessage('Section created')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to create section')
    }
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold">Admin</h2>

      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-lg font-semibold">Create venue</h3>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={submitVenue}>
          <input
            name="name"
            value={venueForm.name}
            onChange={handleVenueChange}
            required
            placeholder="Venue name"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <input
            name="address"
            value={venueForm.address}
            onChange={handleVenueChange}
            required
            placeholder="Venue address"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <div className="md:col-span-2">
            <button className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700" type="submit">
              Create venue
            </button>
          </div>
        </form>
      </section>

      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-lg font-semibold">Create event (use existing venue)</h3>
        <form className="grid gap-3 md:grid-cols-3" onSubmit={submitEvent}>
          <input
            name="name"
            value={eventForm.name}
            onChange={handleEventChange}
            required
            placeholder="Event name"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <input
            name="description"
            value={eventForm.description}
            onChange={handleEventChange}
            required
            placeholder="Event description"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <input
            name="eventDate"
            value={eventForm.eventDate}
            onChange={handleEventChange}
            required
            placeholder="2026-03-20T20:00:00"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <select
            name="venueId"
            value={eventForm.venueId}
            onChange={handleEventChange}
            required
            className="rounded border border-slate-300 px-3 py-2"
          >
            <option value="">Select venue</option>
            {venues.map((venue) => (
              <option key={venue.id} value={venue.id}>{venue.name}</option>
            ))}
          </select>
          <div className="md:col-span-3">
            <button className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700" type="submit">
              Create event
            </button>
          </div>
        </form>
      </section>

      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-lg font-semibold">Create section (in venue)</h3>
        <form className="grid gap-3 md:grid-cols-3" onSubmit={submitSection}>
          <input
            name="name"
            value={sectionForm.name}
            onChange={handleSectionChange}
            required
            placeholder="Section name"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <input
            name="price"
            value={sectionForm.price}
            onChange={handleSectionChange}
            required
            placeholder="Price"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <select
            name="venueId"
            value={sectionForm.venueId}
            onChange={handleSectionChange}
            required
            className="rounded border border-slate-300 px-3 py-2"
          >
            <option value="">Select venue</option>
            {venues.map((venue) => (
              <option key={venue.id} value={venue.id}>{venue.name}</option>
            ))}
          </select>
          <input
            name="totalRows"
            value={sectionForm.totalRows}
            onChange={handleSectionChange}
            required
            placeholder="Rows"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <input
            name="totalColumns"
            value={sectionForm.totalColumns}
            onChange={handleSectionChange}
            required
            placeholder="Columns"
            className="rounded border border-slate-300 px-3 py-2"
          />
          <div className="md:col-span-3">
            <button className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700" type="submit">
              Create section
            </button>
          </div>
        </form>
      </section>

      {message ? <p className="text-sm text-slate-700">{message}</p> : null}
    </div>
  )
}

export default AdminPage
