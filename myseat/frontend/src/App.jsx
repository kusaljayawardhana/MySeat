import { Link, Navigate, Route, Routes } from 'react-router-dom'
import { useState } from 'react'
import EventsPage from './pages/EventsPage'
import EventDetailPage from './pages/EventDetailPage'
import AdminPage from './pages/AdminPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'

function App() {
  const [currentUser, setCurrentUser] = useState(() => {
    const rawUser = localStorage.getItem('myseat_user')
    return rawUser ? JSON.parse(rawUser) : null
  })

  const handleLogin = (user) => {
    setCurrentUser(user)
    localStorage.setItem('myseat_user', JSON.stringify(user))
  }

  const handleLogout = () => {
    setCurrentUser(null)
    localStorage.removeItem('myseat_user')
  }

  return (
    <div className="min-h-screen">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <Link to="/" className="text-xl font-semibold">MySeat</Link>
          <nav className="flex gap-4 text-sm">
            <Link to="/" className="text-blue-600 hover:underline">Home</Link>
            <Link to="/events" className="text-blue-600 hover:underline">Events</Link>
            {currentUser?.role === 'ADMIN' ? (
              <Link to="/admin" className="text-blue-600 hover:underline">Admin</Link>
            ) : null}
            {!currentUser ? (
              <Link to="/login" className="text-blue-600 hover:underline">Login</Link>
            ) : (
              <button type="button" onClick={handleLogout} className="text-blue-600 hover:underline">
                Logout
              </button>
            )}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6">
        <Routes>
          <Route path="/" element={<HomePage currentUser={currentUser} onLogout={handleLogout} />} />
          <Route path="/login" element={currentUser ? <Navigate to="/" replace /> : <LoginPage onLogin={handleLogin} />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/:eventId" element={<EventDetailPage />} />
          <Route path="/admin" element={currentUser?.role === 'ADMIN' ? <AdminPage /> : <Navigate to="/login" replace />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
