require('dotenv').config();

const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Razorpay = require('razorpay');

const app = express();

// ─── Middleware ────────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());

// ─── Config (all read from backend/.env via dotenv) ───────────────────────────
const PORT = process.env.PORT || 5000;
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-2.5-flash';
const MONGODB_URI = process.env.MONGODB_URI;
const JWT_SECRET = process.env.JWT_SECRET;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const RAZORPAY_KEY_ID = process.env.RAZORPAY_KEY_ID;
const RAZORPAY_KEY_SECRET = process.env.RAZORPAY_KEY_SECRET;

// ─── Razorpay client (lazy-init so missing keys don't crash on startup) ───────
let razorpay;
try {
  razorpay = new Razorpay({ key_id: RAZORPAY_KEY_ID, key_secret: RAZORPAY_KEY_SECRET });
} catch (err) {
  console.warn('[Razorpay] Skipped init – keys not configured yet.');
}

// ─── MongoDB Connection ────────────────────────────────────────────────────────
mongoose
  .connect(MONGODB_URI)
  .then(() => console.log('[MongoDB] Connected to Atlas ✔'))
  .catch((err) => console.error('[MongoDB] Connection error:', err.message));

// ═══════════════════════════════════════════════════════════════════════════════
// MONGOOSE MODELS
// ═══════════════════════════════════════════════════════════════════════════════

// ─── User ─────────────────────────────────────────────────────────────────────
const userSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    password: { type: String, required: true },
    role: { type: String, enum: ['USER', 'ORGANIZER', 'ADMIN'], default: 'USER' },
  },
  { timestamps: true }
);
const User = mongoose.model('User', userSchema);

// ─── Event ────────────────────────────────────────────────────────────────────
const eventSchema = new mongoose.Schema(
  {
    title: { type: String, required: true, trim: true },
    description: { type: String, required: true },
    category: { type: String, required: true },
    date: { type: String, required: true },
    location: { type: String, required: true },
    price: { type: Number, default: 0 },
    totalSeats: { type: Number, required: true, default: 100 },
    availableSeats: { type: Number, required: true, default: 100 },
    imageUrl: { type: String, default: '' },
    organizerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    status: {
      type: String,
      enum: ['PENDING_APPROVAL', 'PUBLISHED', 'REJECTED', 'CANCELLED'],
      default: 'PENDING_APPROVAL',
    },
  },
  { timestamps: true }
);
eventSchema.index({ title: 'text', description: 'text', location: 'text' });
const Event = mongoose.model('Event', eventSchema);

// ─── Booking ──────────────────────────────────────────────────────────────────
const bookingSchema = new mongoose.Schema(
  {
    eventId: { type: mongoose.Schema.Types.ObjectId, ref: 'Event', required: true },
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    ticketCount: { type: Number, required: true, min: 1 },
    totalPrice: { type: Number, required: true },
    status: { type: String, enum: ['CONFIRMED', 'CANCELLED', 'USED'], default: 'CONFIRMED' },
    qrCodeHash: { type: String, unique: true, sparse: true },
    isCheckedIn: { type: Boolean, default: false },
    checkedInAt: { type: Date },
    razorpayOrderId: { type: String },
    razorpayPaymentId: { type: String },
  },
  { timestamps: true }
);
const Booking = mongoose.model('Booking', bookingSchema);

// ═══════════════════════════════════════════════════════════════════════════════
// AUTH MIDDLEWARE
// ═══════════════════════════════════════════════════════════════════════════════
const authenticate = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Unauthorized – token missing' });
  }
  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded; // { id, role }
    next();
  } catch {
    return res.status(401).json({ message: 'Unauthorized – invalid token' });
  }
};

const requireRole = (...roles) =>
  (req, res, next) => {
    if (!roles.includes(req.user?.role)) {
      return res.status(403).json({ message: 'Forbidden – insufficient role' });
    }
    next();
  };

// ═══════════════════════════════════════════════════════════════════════════════
// HELPER – generate a simple unique QR hash
// ═══════════════════════════════════════════════════════════════════════════════
const generateQrHash = (bookingId, userId) => {
  const crypto = require('crypto');
  return crypto
    .createHash('sha256')
    .update(`${bookingId}-${userId}-${Date.now()}`)
    .digest('hex');
};

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Auth  (/api/auth)
// ═══════════════════════════════════════════════════════════════════════════════
const authRouter = express.Router();

// POST /api/auth/register
authRouter.post('/register', async (req, res) => {
  try {
    const { name, email, password, role } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ message: 'name, email, and password are required' });
    }
    const existing = await User.findOne({ email });
    if (existing) return res.status(409).json({ message: 'Email already registered' });

    const allowedRoles = ['USER', 'ORGANIZER'];
    const userRole = allowedRoles.includes(role) ? role : 'USER';
    const hashedPassword = await bcrypt.hash(password, 12);
    const user = new User({ name, email, password: hashedPassword, role: userRole });
    await user.save();

    const token = jwt.sign({ id: user._id.toString(), role: user.role }, JWT_SECRET, { expiresIn: '7d' });
    res.status(201).json({
      token,
      user: { id: user._id.toString(), name: user.name, email: user.email, role: user.role },
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// POST /api/auth/login
authRouter.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ message: 'email and password are required' });

    const user = await User.findOne({ email });
    if (!user) return res.status(401).json({ message: 'Invalid credentials' });

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(401).json({ message: 'Invalid credentials' });

    const token = jwt.sign({ id: user._id.toString(), role: user.role }, JWT_SECRET, { expiresIn: '7d' });
    res.json({
      token,
      user: { id: user._id.toString(), name: user.name, email: user.email, role: user.role },
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Events  (/api/events)
// ═══════════════════════════════════════════════════════════════════════════════
const eventsRouter = express.Router();

// GET /api/events  – public, supports ?status=&category=&search=
eventsRouter.get('/', async (req, res) => {
  try {
    const { status, category, search } = req.query;
    const filter = {};
    if (status) filter.status = status;
    else filter.status = 'PUBLISHED'; // default to published only for public listing
    if (category) filter.category = category;
    if (search) filter.$text = { $search: search };
    const events = await Event.find(filter).sort({ createdAt: -1 });
    res.json(events);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/events/organizer – organizer sees their own events
eventsRouter.get('/organizer', authenticate, requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const events = await Event.find({ organizerId: req.user.id }).sort({ createdAt: -1 });
    res.json(events);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/events/:id
eventsRouter.get('/:id', async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    res.json(event);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// POST /api/events – organizer creates event (defaults to PENDING_APPROVAL)
eventsRouter.post('/', authenticate, requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const { title, description, category, date, location, price, totalSeats, imageUrl } = req.body;
    const event = new Event({
      title,
      description,
      category,
      date,
      location,
      price: price || 0,
      totalSeats: totalSeats || 100,
      availableSeats: totalSeats || 100,
      imageUrl: imageUrl || '',
      organizerId: req.user.id,
      status: 'PENDING_APPROVAL',
    });
    await event.save();
    res.status(201).json(event);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// PUT /api/events/:id – organizer updates own event
eventsRouter.put('/:id', authenticate, requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    if (event.organizerId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden – not your event' });
    }
    const allowed = ['title', 'description', 'category', 'date', 'location', 'price', 'totalSeats', 'imageUrl'];
    allowed.forEach((f) => { if (req.body[f] !== undefined) event[f] = req.body[f]; });
    // If seats changed, keep availableSeats consistent
    if (req.body.totalSeats !== undefined) event.availableSeats = req.body.totalSeats;
    event.status = 'PENDING_APPROVAL'; // re-approval required after edit
    await event.save();
    res.json(event);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// DELETE /api/events/:id – organizer deletes own event
eventsRouter.delete('/:id', authenticate, requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    if (event.organizerId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden – not your event' });
    }
    await Event.deleteOne({ _id: req.params.id });
    res.json({ message: 'Event deleted successfully' });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Admin  (/api/admin)
// ═══════════════════════════════════════════════════════════════════════════════
const adminRouter = express.Router();
adminRouter.use(authenticate, requireRole('ADMIN'));

// GET /api/admin/pending-events
adminRouter.get('/pending-events', async (req, res) => {
  try {
    const events = await Event.find({ status: 'PENDING_APPROVAL' })
      .populate('organizerId', 'name email')
      .sort({ createdAt: -1 });
    res.json(events);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// PATCH /api/admin/events/:id/status  – PUBLISHED | REJECTED
adminRouter.patch('/events/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    if (!['PUBLISHED', 'REJECTED'].includes(status)) {
      return res.status(400).json({ message: 'status must be PUBLISHED or REJECTED' });
    }
    const event = await Event.findByIdAndUpdate(req.params.id, { status }, { new: true });
    if (!event) return res.status(404).json({ message: 'Event not found' });
    res.json(event);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/admin/metrics
adminRouter.get('/metrics', async (req, res) => {
  try {
    const [totalUsers, totalEvents, totalBookings, revenueAgg] = await Promise.all([
      User.countDocuments(),
      Event.countDocuments(),
      Booking.countDocuments({ status: 'CONFIRMED' }),
      Booking.aggregate([{ $match: { status: 'CONFIRMED' } }, { $group: { _id: null, total: { $sum: '$totalPrice' } } }]),
    ]);
    const pendingEvents = await Event.countDocuments({ status: 'PENDING_APPROVAL' });
    const publishedEvents = await Event.countDocuments({ status: 'PUBLISHED' });
    const totalRevenue = revenueAgg[0]?.total || 0;
    res.json({ totalUsers, totalEvents, pendingEvents, publishedEvents, totalBookings, totalRevenue });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Bookings  (/api/bookings)
// ═══════════════════════════════════════════════════════════════════════════════
const bookingsRouter = express.Router();
bookingsRouter.use(authenticate);

// GET /api/bookings/my-tickets – user's confirmed bookings
bookingsRouter.get('/my-tickets', async (req, res) => {
  try {
    const bookings = await Booking.find({ userId: req.user.id })
      .populate('eventId', 'title date location imageUrl')
      .sort({ createdAt: -1 });
    res.json(bookings);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// POST /api/bookings – create booking
bookingsRouter.post('/', async (req, res) => {
  try {
    const { eventId, ticketCount } = req.body;
    if (!eventId || !ticketCount) return res.status(400).json({ message: 'eventId and ticketCount required' });

    const event = await Event.findById(eventId);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    if (event.status !== 'PUBLISHED') return res.status(400).json({ message: 'Event is not available for booking' });
    if (event.availableSeats < ticketCount) return res.status(400).json({ message: 'Not enough available seats' });

    const totalPrice = event.price * ticketCount;
    const booking = new Booking({
      eventId,
      userId: req.user.id,
      ticketCount,
      totalPrice,
      status: 'CONFIRMED',
    });
    const saved = await booking.save();

    // Generate QR hash after we have the _id
    saved.qrCodeHash = generateQrHash(saved._id.toString(), req.user.id);
    await saved.save();

    // Decrement available seats
    event.availableSeats -= ticketCount;
    await event.save();

    res.status(201).json(saved);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/bookings – all bookings for current user (generic list)
bookingsRouter.get('/', async (req, res) => {
  try {
    const bookings = await Booking.find({ userId: req.user.id })
      .populate('eventId', 'title date location imageUrl')
      .sort({ createdAt: -1 });
    res.json(bookings);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/bookings/:id
bookingsRouter.get('/:id', async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id).populate('eventId');
    if (!booking) return res.status(404).json({ message: 'Booking not found' });
    if (booking.userId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden' });
    }
    res.json(booking);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// POST /api/bookings/:id/verify (legacy compat)
bookingsRouter.post('/:id/verify', requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id);
    if (!booking) return res.status(404).json({ message: 'Booking not found' });
    if (booking.isCheckedIn) return res.status(409).json({ message: 'Already checked in', booking });
    booking.isCheckedIn = true;
    booking.checkedInAt = new Date();
    booking.status = 'USED';
    await booking.save();
    res.json({ message: 'Check-in successful', booking });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Organizer Check-in  (/api/organizer/check-in)
// ═══════════════════════════════════════════════════════════════════════════════
const organizerRouter = express.Router();
organizerRouter.use(authenticate, requireRole('ORGANIZER', 'ADMIN'));

// POST /api/organizer/check-in  – validates qrCodeHash and marks isCheckedIn = true
organizerRouter.post('/check-in', async (req, res) => {
  try {
    const { qrCodeHash } = req.body;
    if (!qrCodeHash) return res.status(400).json({ message: 'qrCodeHash is required' });

    const booking = await Booking.findOne({ qrCodeHash }).populate('eventId', 'title date location organizerId');
    if (!booking) return res.status(404).json({ message: 'Invalid QR code – booking not found' });

    // Verify organizer owns the event
    if (
      booking.eventId.organizerId.toString() !== req.user.id &&
      req.user.role !== 'ADMIN'
    ) {
      return res.status(403).json({ message: 'Forbidden – not your event' });
    }

    if (booking.isCheckedIn) {
      return res.status(409).json({ message: 'Attendee already checked in', booking });
    }

    booking.isCheckedIn = true;
    booking.checkedInAt = new Date();
    booking.status = 'USED';
    await booking.save();

    res.json({ message: 'Check-in successful', booking });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Chat / Gemini proxy  (/api/chat)
// ═══════════════════════════════════════════════════════════════════════════════
const chatRouter = express.Router();
chatRouter.post('/', authenticate, async (req, res) => {
  try {
    const { message, role, history = [], draftContext } = req.body;
    if (!message) return res.status(400).json({ message: 'message is required' });

    const userRole = role || req.user?.role || 'USER';

    let systemInstructionText = '';
    if (userRole === 'ORGANIZER') {
      systemInstructionText = `You are EventPulse AI Co-Pilot, an expert assistant for event organizers.
Your job is to assist organizers with:
- Suggesting catchy, viral event names and themes
- Drafting engaging, professional event descriptions
- Recommending competitive ticket prices in Indian Rupees (INR / ₹)
- Providing marketing strategies and agenda ideas.
${draftContext ? `Current Event Draft Context: ${JSON.stringify(draftContext)}` : ''}
Keep your responses well-structured, clear, inspiring, and concise.`;
    } else {
      // USER role: fetch current PUBLISHED events from DB as context
      const publishedEvents = await Event.find({ status: 'PUBLISHED' })
        .select('title description category date location price availableSeats')
        .limit(20)
        .lean();

      const eventsSummary = publishedEvents.map(e => 
        `- "${e.title}" [${e.category}] on ${e.date} at ${e.location}. Price: ${e.price > 0 ? '₹' + e.price : 'Free'}, Available Seats: ${e.availableSeats}`
      ).join('\n');

      systemInstructionText = `You are EventPulse Discovery Assistant, a friendly AI guide for event attendees.
Recommend upcoming events to users based on their questions, preferences, and mood.
Always mention the event name, date, venue/location, and ticket price (in ₹ / INR).
Here is the live catalog of currently PUBLISHED events available in EventPulse:
${eventsSummary || 'No published events currently available in the database.'}

If the user asks about something not in the list, recommend the closest matching categories or advise them to check back soon. Keep answers concise, helpful, and formatted with bullet points.`;
    }

    // Build Gemini contents with history + current prompt
    const contents = [];
    if (Array.isArray(history)) {
      for (const item of history) {
        if (item.content) {
          contents.push({
            role: item.isFromUser ? 'user' : 'model',
            parts: [{ text: item.content }]
          });
        }
      }
    }
    contents.push({
      role: 'user',
      parts: [{ text: message }]
    });

    const geminiRequestBody = {
      system_instruction: {
        parts: [{ text: systemInstructionText }]
      },
      contents: contents
    };

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;
    const response = await fetch(geminiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(geminiRequestBody)
    });

    if (!response.ok) {
      const errText = await response.text();
      console.error('[Gemini API Error]:', response.status, errText);
      return res.status(response.status).json({ message: 'Gemini API error', error: errText });
    }

    const data = await response.json();
    const reply = data?.candidates?.[0]?.content?.parts?.[0]?.text || 'Sorry, I could not generate a response.';
    res.json({ reply });
  } catch (err) {
    console.error('[Chat Server Error]:', err);
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// HEALTH CHECK
// ═══════════════════════════════════════════════════════════════════════════════
app.get('/health', (req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));

// ─── Mount all routers ────────────────────────────────────────────────────────
app.use('/api/auth', authRouter);
app.use('/api/events', eventsRouter);
app.use('/api/admin', adminRouter);
app.use('/api/bookings', bookingsRouter);
app.use('/api/organizer', organizerRouter);
app.use('/api/chat', chatRouter);

// ─── 404 handler ─────────────────────────────────────────────────────────────
app.use((req, res) => res.status(404).json({ message: `Route ${req.originalUrl} not found` }));

// ─── Start server ─────────────────────────────────────────────────────────────
app.listen(PORT, () => console.log(`[EventPulse API] Server running on http://localhost:${PORT}`));
