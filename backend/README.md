# EventPulse Backend

Node.js + Express REST API connected to MongoDB Atlas.

## Prerequisites
- Node.js v18+
- A [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) cluster (free tier is fine)
- (Optional) Razorpay test account & Gemini API key

## Setup & Run

```bash
# 1. Navigate to the backend folder
cd backend

# 2. Install dependencies
npm install

# 3. Fill in your real credentials in .env
#    (replace all paste_your_*_here placeholders)
notepad .env

# 4. Start the server (production)
npm start

# 5. Start with hot-reload (development)
npm run dev
```

Server starts on `http://localhost:5000`.

## Environment Variables (`backend/.env`)

| Key | Description |
|-----|-------------|
| `PORT` | Port to listen on (default `5000`) |
| `MONGODB_URI` | Full MongoDB Atlas connection string |
| `JWT_SECRET` | Secret used to sign JWT tokens |
| `GEMINI_API_KEY` | Google Gemini API key for AI chat |
| `RAZORPAY_KEY_ID` | Razorpay test key ID |
| `RAZORPAY_KEY_SECRET` | Razorpay test secret |

> **Security**: `backend/.env` and `backend/node_modules/` are listed in `.gitignore` and will never be committed to GitHub.

## API Endpoints Summary

### Auth
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/auth/register` | None | Register user/organizer |
| POST | `/api/auth/login` | None | Login – returns JWT + role |

### Events
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/events` | None | List published events (filter: status, category, search) |
| GET | `/api/events/organizer` | ORGANIZER | List own events |
| GET | `/api/events/:id` | None | Get single event |
| POST | `/api/events` | ORGANIZER | Create event (status = PENDING_APPROVAL) |
| PUT | `/api/events/:id` | ORGANIZER | Update event |
| DELETE | `/api/events/:id` | ORGANIZER | Delete event |

### Admin
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/admin/pending-events` | ADMIN | List events awaiting approval |
| PATCH | `/api/admin/events/:id/status` | ADMIN | Approve (PUBLISHED) or reject (REJECTED) |
| GET | `/api/admin/metrics` | ADMIN | Dashboard metrics |

### Bookings & Check-in
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/bookings/my-tickets` | USER | User's ticket list |
| POST | `/api/bookings` | USER | Book tickets |
| POST | `/api/organizer/check-in` | ORGANIZER | QR check-in (`{ qrCodeHash }`) |

### Chat (Gemini)
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/chat` | USER | AI assistant chat |
