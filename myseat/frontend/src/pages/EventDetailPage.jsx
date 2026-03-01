import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { createBooking } from '../api/bookings'
import { getEventById, getEventSeats } from '../api/catalog'

const statusMeta = {
  AVAILABLE: {
    label: 'Available',
    className: 'border-emerald-300 bg-emerald-50 text-emerald-800 hover:border-emerald-500',
  },
  RESERVED: {
    label: 'Reserved',
    className: 'cursor-not-allowed border-amber-300 bg-amber-100 text-amber-800',
  },
  BOOKED: {
    label: 'Booked',
    className: 'cursor-not-allowed border-rose-300 bg-rose-100 text-rose-800',
  },
}

function EventDetailPage() {
  const { eventId } = useParams()
  const [eventItem, setEventItem] = useState(null)
  const [seats, setSeats] = useState([])
  const [selectedSeatIds, setSelectedSeatIds] = useState([])
  const [selectedSectionId, setSelectedSectionId] = useState(null)
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)

  const [form, setForm] = useState({
    userId: '',
    payerName: '',
    payerEmail: '',
    paymentMethod: 'CARD',
    paymentReference: '',
  })

  useEffect(() => {
    const load = async () => {
      try {
        const [eventData, seatData] = await Promise.all([
          getEventById(eventId),
          getEventSeats(eventId),
        ])
        setEventItem(eventData)
        setSeats(seatData)
      } catch (err) {
        setMessage(err?.response?.data?.message || 'Failed to load event details')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [eventId])

  const groupedSeats = useMemo(() => {
    const grouped = new Map()
    seats.forEach((seat) => {
      const sectionKey = seat.sectionId
      if (!grouped.has(sectionKey)) {
        grouped.set(sectionKey, {
          seats: [],
          maxRow: 0,
          maxColumn: 0,
        })
      }
      const section = grouped.get(sectionKey)
      section.seats.push(seat)
      section.maxRow = Math.max(section.maxRow, seat.rowNumber)
      section.maxColumn = Math.max(section.maxColumn, seat.columnNumber)
    })

    return [...grouped.entries()]
      .map(([sectionId, section]) => {
        const seatsByPosition = new Map(
          section.seats.map((seat) => [`${seat.rowNumber}-${seat.columnNumber}`, seat]),
        )
        return {
          sectionId,
          seatsByPosition,
          maxRow: section.maxRow,
          maxColumn: section.maxColumn,
        }
      })
      .sort((a, b) => Number(a.sectionId) - Number(b.sectionId))
  }, [seats])

  const toggleSeat = (seat) => {
    if (seat.status !== 'AVAILABLE') {
      return
    }

    if (selectedSectionId && selectedSectionId !== seat.sectionId) {
      setMessage('Please select seats from only one section in a booking.')
      return
    }

    if (selectedSeatIds.includes(seat.id)) {
      const updated = selectedSeatIds.filter((id) => id !== seat.id)
      setSelectedSeatIds(updated)
      if (updated.length === 0) {
        setSelectedSectionId(null)
      }
      return
    }

    setSelectedSeatIds((prev) => [...prev, seat.id])
    setSelectedSectionId(seat.sectionId)
    setMessage('')
  }

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const submitBooking = async (event) => {
    event.preventDefault()

    if (!eventItem || selectedSeatIds.length === 0 || !selectedSectionId) {
      setMessage('Select at least one seat.')
      return
    }

    try {
      const payload = {
        userId: form.userId ? Number(form.userId) : null,
        eventId: Number(eventId),
        venueId: eventItem.venueId,
        sectionId: selectedSectionId,
        seatIds: selectedSeatIds,
        payerName: form.payerName,
        payerEmail: form.payerEmail,
        paymentMethod: form.paymentMethod,
        paymentReference: form.paymentReference,
      }

      const booking = await createBooking(payload)
      setMessage(`Booking created: #${booking.bookingId} (${booking.status})`)
      setSelectedSeatIds([])
      setSelectedSectionId(null)

      const seatData = await getEventSeats(eventId)
      setSeats(seatData)
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to create booking')
    }
  }

  if (loading) {
    return <p className="text-sm text-slate-600">Loading event...</p>
  }

  if (!eventItem) {
    return <p className="text-sm text-red-600">Event not found.</p>
  }

  return (
    <div className="space-y-6">
      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h2 className="text-2xl font-semibold">{eventItem.name}</h2>
        <p className="mt-1 text-sm text-slate-600">{new Date(eventItem.eventDate).toLocaleString()}</p>
        <p className="text-sm text-slate-700">{eventItem.venueName}</p>
        <p className="text-sm text-slate-500">{eventItem.venueAddress}</p>
      </section>

      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-lg font-semibold">Select seats</h3>
        <div className="mb-4 flex flex-wrap gap-2 text-xs">
          {Object.entries(statusMeta).map(([status, meta]) => (
            <span
              key={status}
              className={`inline-flex items-center rounded border px-2 py-1 font-medium ${meta.className}`}
            >
              {meta.label}
            </span>
          ))}
          <span className="inline-flex items-center rounded border border-blue-700 bg-blue-700 px-2 py-1 font-medium text-white">
            Selected
          </span>
        </div>
        <div className="space-y-4">
          {groupedSeats.map((section) => (
            <div key={section.sectionId}>
              <p className="mb-2 text-sm font-medium text-slate-700">Section #{section.sectionId}</p>
              <div className="space-y-2 overflow-x-auto">
                {Array.from({ length: section.maxRow }, (_, rowIndex) => {
                  const rowNumber = rowIndex + 1

                  return (
                    <div key={rowNumber} className="flex items-center gap-2">
                      <span className="w-10 text-xs font-medium text-slate-500">R{rowNumber}</span>
                      <div className="grid gap-2" style={{ gridTemplateColumns: `repeat(${section.maxColumn}, minmax(0, 1fr))` }}>
                        {Array.from({ length: section.maxColumn }, (_, columnIndex) => {
                          const columnNumber = columnIndex + 1
                          const seat = section.seatsByPosition.get(`${rowNumber}-${columnNumber}`)

                          if (!seat) {
                            return <div key={`${rowNumber}-${columnNumber}`} className="h-9 w-12" />
                          }

                          const isSelected = selectedSeatIds.includes(seat.id)
                          const baseClass = 'h-9 w-12 rounded border text-xs font-medium'
                          const stateClass = seat.status === 'AVAILABLE'
                            ? isSelected
                              ? 'border-blue-700 bg-blue-700 text-white'
                              : statusMeta.AVAILABLE.className
                            : statusMeta[seat.status]?.className || 'cursor-not-allowed border-slate-300 bg-slate-200 text-slate-600'

                          return (
                            <button
                              key={seat.id}
                              type="button"
                              className={`${baseClass} ${stateClass}`}
                              onClick={() => toggleSeat(seat)}
                            >
                              C{columnNumber}
                            </button>
                          )
                        })}
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-lg border bg-white p-4 shadow-sm">
        <h3 className="mb-3 text-lg font-semibold">Guest / user booking details</h3>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={submitBooking}>
          <label className="text-sm">
            User ID (optional)
            <input
              name="userId"
              value={form.userId}
              onChange={onChange}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
              placeholder="For registered user"
            />
          </label>

          <label className="text-sm">
            Payer name
            <input
              name="payerName"
              value={form.payerName}
              onChange={onChange}
              required
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
            />
          </label>

          <label className="text-sm">
            Payer email
            <input
              type="email"
              name="payerEmail"
              value={form.payerEmail}
              onChange={onChange}
              required
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
            />
          </label>

          <label className="text-sm">
            Payment method
            <select
              name="paymentMethod"
              value={form.paymentMethod}
              onChange={onChange}
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
            >
              <option value="CARD">CARD</option>
              <option value="BANK_TRANSFER">BANK_TRANSFER</option>
              <option value="CASH">CASH</option>
            </select>
          </label>

          <label className="text-sm md:col-span-2">
            Payment reference
            <input
              name="paymentReference"
              value={form.paymentReference}
              onChange={onChange}
              required
              className="mt-1 w-full rounded border border-slate-300 px-3 py-2"
            />
          </label>

          <div className="md:col-span-2">
            <button
              type="submit"
              className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Book selected seats ({selectedSeatIds.length})
            </button>
          </div>
        </form>
      </section>

      {message ? <p className="text-sm text-slate-700">{message}</p> : null}
    </div>
  )
}

export default EventDetailPage
