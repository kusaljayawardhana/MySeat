import { Link } from 'react-router-dom'

function HomePage({ currentUser, onLogout }) {
  return (
    <div className="rounded-lg border bg-white p-6 shadow-sm">
      <h2 className="text-2xl font-semibold">Welcome to MySeat</h2>
      <p className="mt-2 text-sm text-slate-600">Book seats for events quickly.</p>

      {!currentUser ? (
        <div className="mt-6 flex gap-3">
          <Link
            to="/login"
            className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            Login
          </Link>
          <Link
            to="/events"
            className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Browse events
          </Link>
        </div>
      ) : (
        <div className="mt-6 space-y-3">
          <p className="text-sm text-slate-700">
            Logged in as <span className="font-medium">{currentUser.name}</span> ({currentUser.role})
          </p>
          <div className="flex gap-3">
            <Link
              to="/events"
              className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Browse events
            </Link>
            {currentUser.role === 'ADMIN' ? (
              <Link
                to="/admin"
                className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Go to admin
              </Link>
            ) : null}
            <button
              type="button"
              onClick={onLogout}
              className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Logout
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default HomePage
