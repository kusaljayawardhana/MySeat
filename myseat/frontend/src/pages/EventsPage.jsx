import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getEvents } from '../api/catalog'

function EventsPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      try {
        const data = await getEvents()
        setEvents(data)
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load events')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [])

  if (loading) {
    return <p className="text-sm text-slate-600">Loading events...</p>
  }

  if (error) {
    return <p className="text-sm text-red-600">{error}</p>
  }

  return (
    <div>
      <h2 className="mb-4 text-2xl font-semibold">Events</h2>
      {events.length === 0 ? (
        <p className="text-sm text-slate-600">No events available.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {events.map((eventItem) => (
            <article key={eventItem.id} className="overflow-hidden rounded-lg border bg-white shadow-sm">
              <img
                src={eventItem.imageUrl || 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80'}
                alt={eventItem.name}
                className="h-44 w-full object-cover"
              />
              <div className="p-4">
                <h3 className="text-lg font-semibold">{eventItem.name}</h3>
                <p className="mt-1 text-sm text-slate-600">
                  {new Date(eventItem.eventDate).toLocaleString()}
                </p>
              <Link
                to={`/events/${eventItem.id}`}
                className="mt-4 inline-block rounded bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                View event
              </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}

export default EventsPage
