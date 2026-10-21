require('dotenv').config();

const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const { getPaymentProvider } = require('./paymentProvider');

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
const PAYMENT_MODE = process.env.PAYMENT_MODE || 'mock';

// ─── Payment Provider ──────────────────────────────────────────────────────────
const paymentProvider = getPaymentProvider(PAYMENT_MODE);

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
    phoneNumber: { type: String, default: '', trim: true },
    isBlocked: { type: Boolean, default: false },
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
    price: { type: Number, default: 0, min: 0 },
    totalSeats: { type: Number, required: true, default: 100, min: 1 },
    availableSeats: { type: Number, required: true, default: 100, min: 0 },
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

// ─── Payment ──────────────────────────────────────────────────────────────────
const paymentSchema = new mongoose.Schema(
  {
    bookingId: { type: mongoose.Schema.Types.ObjectId, ref: 'Booking', required: true },
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    amount: { type: Number, required: true, min: 0 },
    currency: { type: String, default: 'INR' },
    status: {
      type: String,
      enum: ['CREATED', 'PAID', 'FAILED', 'REFUNDED'],
      default: 'CREATED',
    },
    gatewayToken: { type: String, default: '' },
    providerOrderId: { type: String, default: '' },
    providerTransactionId: { type: String, default: '' },
    refundId: { type: String, default: '' },
  },
  { timestamps: true }
);
const Payment = mongoose.model('Payment', paymentSchema);

// ─── Booking ──────────────────────────────────────────────────────────────────
const bookingSchema = new mongoose.Schema(
  {
    eventId: { type: mongoose.Schema.Types.ObjectId, ref: 'Event', required: true },
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    ticketCount: { type: Number, required: true, min: 1 },
    totalPrice: { type: Number, required: true, min: 0 },
    status: {
      type: String,
      enum: ['PENDING', 'CONFIRMED', 'CANCELLED', 'USED'],
      default: 'PENDING',
    },
    qrCodeHash: { type: String, unique: true, sparse: true },
    isCheckedIn: { type: Boolean, default: false },
    checkedInAt: { type: Date },
    expiresAt: { type: Date },
    paymentId: { type: mongoose.Schema.Types.ObjectId, ref: 'Payment' },
  },
  { timestamps: true }
);
const Booking = mongoose.model('Booking', bookingSchema);

// ═══════════════════════════════════════════════════════════════════════════════
// AUTH MIDDLEWARE
// ═══════════════════════════════════════════════════════════════════════════════
const authenticate = async (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Unauthorized – token missing' });
  }
  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const dbUser = await User.findById(decoded.id).select('role isBlocked name email phoneNumber');
    if (!dbUser) {
      return res.status(401).json({ message: 'Unauthorized – user no longer exists' });
    }
    if (dbUser.isBlocked) {
      return res.status(403).json({ message: 'Account is blocked by administrator' });
    }
    req.user = { id: dbUser._id.toString(), role: dbUser.role, email: dbUser.email, name: dbUser.name };
    next();
  } catch (err) {
    return res.status(401).json({ message: 'Unauthorized – invalid or expired token' });
  }
};

const requireRole = (...roles) => (req, res, next) => {
  if (!roles.includes(req.user?.role)) {
    return res.status(403).json({ message: 'Forbidden – insufficient permissions' });
  }
  next();
};

// ═══════════════════════════════════════════════════════════════════════════════
// HELPER – QR Hash
// ═══════════════════════════════════════════════════════════════════════════════
const generateQrHash = (bookingId, userId) => {
  return crypto
    .createHash('sha256')
    .update(`${bookingId}-${userId}-${Date.now()}-${Math.random()}`)
    .digest('hex');
};

// ═══════════════════════════════════════════════════════════════════════════════
// BACKGROUND SWEEP: Expired PENDING Bookings
// ═══════════════════════════════════════════════════════════════════════════════
const sweepExpiredBookings = async () => {
  try {
    const now = new Date();
    const expiredBookings = await Booking.find({
      status: 'PENDING',
      expiresAt: { $lt: now },
    });

    for (const booking of expiredBookings) {
      booking.status = 'CANCELLED';
      await booking.save();

      if (booking.paymentId) {
        await Payment.findByIdAndUpdate(booking.paymentId, { status: 'FAILED' });
      }

      // Return held seats back to the event
      const event = await Event.findById(booking.eventId);
      if (event) {
        event.availableSeats = Math.min(event.totalSeats, event.availableSeats + booking.ticketCount);
        await event.save();
      }

      console.log(`[Expiry Sweep] Cancelled expired booking ${booking._id} and restored ${booking.ticketCount} seats.`);
    }
  } catch (err) {
    console.error('[Expiry Sweep Error]:', err.message);
  }
};
// Run sweep every 60 seconds
setInterval(sweepExpiredBookings, 60 * 1000);

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Auth  (/api/auth)
// ═══════════════════════════════════════════════════════════════════════════════
const authRouter = express.Router();

// POST /api/auth/register
authRouter.post('/register', async (req, res) => {
  try {
    const { name, email, password, role, phoneNumber } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ message: 'name, email, and password are required' });
    }
    const normalizedEmail = email.trim().toLowerCase();
    const existing = await User.findOne({ email: normalizedEmail });
    if (existing) return res.status(409).json({ message: 'Email already registered' });

    const allowedRoles = ['USER', 'ORGANIZER'];
    const userRole = allowedRoles.includes(role) ? role : 'USER';
    const hashedPassword = await bcrypt.hash(password, 12);
    const user = new User({
      name: name.trim(),
      email: normalizedEmail,
      password: hashedPassword,
      role: userRole,
      phoneNumber: phoneNumber ? phoneNumber.trim() : '',
    });
    await user.save();

    const token = jwt.sign({ id: user._id.toString(), role: user.role }, JWT_SECRET, { expiresIn: '7d' });
    res.status(201).json({
      token,
      user: {
        id: user._id.toString(),
        name: user.name,
        email: user.email,
        role: user.role,
        phoneNumber: user.phoneNumber,
      },
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

    const normalizedEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: normalizedEmail });
    if (!user) return res.status(401).json({ message: 'Invalid credentials' });

    if (user.isBlocked) {
      return res.status(403).json({ message: 'Account is blocked by administrator' });
    }

    const isMatch = await bcrypt.compare(typeof password === 'string' ? password.trim() : password, user.password);
    if (!isMatch) return res.status(401).json({ message: 'Invalid credentials' });

    const token = jwt.sign({ id: user._id.toString(), role: user.role }, JWT_SECRET, { expiresIn: '7d' });
    res.json({
      token,
      user: {
        id: user._id.toString(),
        name: user.name,
        email: user.email,
        role: user.role,
        phoneNumber: user.phoneNumber,
      },
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// PUT /api/auth/profile  – update user's own profile
authRouter.put('/profile', authenticate, async (req, res) => {
  try {
    const { name, phoneNumber } = req.body;
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ message: 'User not found' });

    if (name && name.trim()) user.name = name.trim();
    if (phoneNumber !== undefined) user.phoneNumber = phoneNumber.trim();

    await user.save();
    res.json({
      id: user._id.toString(),
      name: user.name,
      email: user.email,
      role: user.role,
      phoneNumber: user.phoneNumber,
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
    else filter.status = 'PUBLISHED';
    if (category && category !== 'All') filter.category = new RegExp(`^${category}$`, 'i');
    if (search) filter.$text = { $search: search };

    const events = await Event.find(filter).sort({ createdAt: -1 });
    res.json(events);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/events/organizer – organizer's own events
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
    if (!title || !description || !category || !date || !location) {
      return res.status(400).json({ message: 'title, description, category, date, and location are required' });
    }
    const seats = parseInt(totalSeats) || 100;
    const event = new Event({
      title: title.trim(),
      description: description.trim(),
      category: category.trim(),
      date: date.trim(),
      location: location.trim(),
      price: Math.max(0, parseFloat(price) || 0),
      totalSeats: seats,
      availableSeats: seats,
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
    allowed.forEach((f) => {
      if (req.body[f] !== undefined) event[f] = req.body[f];
    });

    if (req.body.totalSeats !== undefined) {
      const bookedSeats = event.totalSeats - event.availableSeats;
      event.totalSeats = Math.max(bookedSeats, parseInt(req.body.totalSeats));
      event.availableSeats = event.totalSeats - bookedSeats;
    }

    event.status = 'PENDING_APPROVAL'; // re-approval required after edit
    await event.save();
    res.json(event);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// DELETE /api/events/:id – organizer deletes own event (safely handles confirmed bookings)
eventsRouter.delete('/:id', authenticate, requireRole('ORGANIZER', 'ADMIN'), async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    if (event.organizerId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden – not your event' });
    }

    // Cancel all confirmed/pending bookings and issue mock refunds
    const bookings = await Booking.find({
      eventId: event._id,
      status: { $in: ['CONFIRMED', 'PENDING'] },
    });

    for (const b of bookings) {
      b.status = 'CANCELLED';
      await b.save();

      if (b.paymentId) {
        const payment = await Payment.findById(b.paymentId);
        if (payment && payment.status === 'PAID') {
          const refundRes = await paymentProvider.refundPayment({
            paymentId: payment._id.toString(),
            amount: payment.amount,
            reason: 'Event deleted by organizer',
          });
          payment.status = 'REFUNDED';
          payment.refundId = refundRes.refundId;
          await payment.save();
        }
      }
    }

    await Event.deleteOne({ _id: event._id });
    res.json({
      message: `Event deleted successfully. ${bookings.length} associated bookings were cancelled and refunded.`,
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Payments  (/api/payments)
// ═══════════════════════════════════════════════════════════════════════════════
const paymentsRouter = express.Router();
paymentsRouter.use(authenticate);

// POST /api/payments/order – Initiates booking and creates payment order
paymentsRouter.post('/order', async (req, res) => {
  try {
    const { eventId, quantity } = req.body;
    const ticketCount = parseInt(quantity);
    if (!eventId || !ticketCount || ticketCount <= 0) {
      return res.status(400).json({ message: 'Valid eventId and quantity (>= 1) are required' });
    }

    const event = await Event.findById(eventId);
    if (!event) return res.status(404).json({ message: 'Event not found' });
    if (event.status !== 'PUBLISHED') {
      return res.status(400).json({ message: 'Event is not published for booking' });
    }

    // Recalculate total amount strictly from DB event price (never trust client)
    const totalPrice = event.price * ticketCount;

    // Check capacity
    if (event.availableSeats < ticketCount) {
      return res.status(400).json({
        message: `Only ${event.availableSeats} seat(s) available. Cannot book ${ticketCount} seat(s).`,
      });
    }

    // Hold seats immediately
    event.availableSeats -= ticketCount;
    await event.save();

    // 15-minute reservation window
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    // Free event handling (price === 0)
    if (totalPrice === 0) {
      const booking = new Booking({
        eventId: event._id,
        userId: req.user.id,
        ticketCount,
        totalPrice: 0,
        status: 'CONFIRMED',
        expiresAt: null,
      });
      booking.qrCodeHash = generateQrHash(booking._id.toString(), req.user.id);
      await booking.save();

      const payment = new Payment({
        bookingId: booking._id,
        userId: req.user.id,
        amount: 0,
        currency: 'INR',
        status: 'PAID',
        providerOrderId: 'free_order_' + booking._id,
        providerTransactionId: 'free_tx_' + Date.now(),
      });
      await payment.save();

      booking.paymentId = payment._id;
      await booking.save();

      return res.status(201).json({
        paymentId: payment._id.toString(),
        bookingId: booking._id.toString(),
        amount: 0,
        currency: 'INR',
        status: 'CONFIRMED',
        message: 'Free event booked successfully',
      });
    }

    // Paid event: create pending booking and payment provider order
    const booking = new Booking({
      eventId: event._id,
      userId: req.user.id,
      ticketCount,
      totalPrice,
      status: 'PENDING',
      expiresAt,
    });
    booking.qrCodeHash = generateQrHash(booking._id.toString(), req.user.id);
    await booking.save();

    const providerOrder = await paymentProvider.createOrder({
      amount: totalPrice,
      currency: 'INR',
      receipt: `rcpt_${booking._id}`,
      metadata: { eventId, userId: req.user.id },
    });

    const payment = new Payment({
      bookingId: booking._id,
      userId: req.user.id,
      amount: totalPrice,
      currency: 'INR',
      status: 'CREATED',
      providerOrderId: providerOrder.orderId,
    });
    await payment.save();

    booking.paymentId = payment._id;
    await booking.save();

    res.status(201).json({
      paymentId: payment._id.toString(),
      bookingId: booking._id.toString(),
      amount: totalPrice,
      currency: 'INR',
      status: 'PENDING',
      expiresAt: expiresAt.toISOString(),
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// POST /api/payments/confirm – Confirms payment result (Idempotent)
paymentsRouter.post('/confirm', async (req, res) => {
  try {
    const { paymentId, result, gatewayToken } = req.body;
    if (!paymentId || !result) {
      return res.status(400).json({ message: 'paymentId and result (SUCCESS | FAILURE) are required' });
    }

    const payment = await Payment.findById(paymentId);
    if (!payment) return res.status(404).json({ message: 'Payment record not found' });

    // Verify ownership
    if (payment.userId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden – payment does not belong to you' });
    }

    const booking = await Booking.findById(payment.bookingId);
    if (!booking) return res.status(404).json({ message: 'Associated booking not found' });

    // ─── Idempotency Check ──────────────────────────────────────────────────
    if (payment.status === 'PAID' && booking.status === 'CONFIRMED') {
      return res.json({
        message: 'Payment already confirmed',
        booking,
        payment,
      });
    }
    if (payment.status === 'FAILED' || booking.status === 'CANCELLED') {
      return res.json({
        message: 'Payment already marked as failed or cancelled',
        booking,
        payment,
      });
    }

    // ─── Check Expiration ───────────────────────────────────────────────────
    if (booking.expiresAt && booking.expiresAt < new Date()) {
      booking.status = 'CANCELLED';
      payment.status = 'FAILED';
      // Release held seats
      const event = await Event.findById(booking.eventId);
      if (event) {
        event.availableSeats = Math.min(event.totalSeats, event.availableSeats + booking.ticketCount);
        await event.save();
      }
      await booking.save();
      await payment.save();
      return res.status(400).json({ message: 'Order expired. Reserved seats have been released.' });
    }

    // ─── Handle Failure Result ──────────────────────────────────────────────
    if (result.toUpperCase() === 'FAILURE') {
      payment.status = 'FAILED';
      booking.status = 'CANCELLED';
      // Release held seats
      const event = await Event.findById(booking.eventId);
      if (event) {
        event.availableSeats = Math.min(event.totalSeats, event.availableSeats + booking.ticketCount);
        await event.save();
      }
      await booking.save();
      await payment.save();
      return res.json({ message: 'Payment failed. Booking cancelled and seats released.', booking, payment });
    }

    // ─── Handle Success Result ──────────────────────────────────────────────
    if (result.toUpperCase() === 'SUCCESS') {
      const verification = await paymentProvider.verifyPayment({
        paymentId: payment._id.toString(),
        gatewayToken,
        result: 'SUCCESS',
      });

      if (!verification.success) {
        payment.status = 'FAILED';
        booking.status = 'CANCELLED';
        const event = await Event.findById(booking.eventId);
        if (event) {
          event.availableSeats = Math.min(event.totalSeats, event.availableSeats + booking.ticketCount);
          await event.save();
        }
        await booking.save();
        await payment.save();
        return res.status(400).json({ message: verification.error || 'Payment verification failed' });
      }

      payment.status = 'PAID';
      payment.gatewayToken = gatewayToken || '';
      payment.providerTransactionId = verification.transactionId || '';
      booking.status = 'CONFIRMED';
      booking.expiresAt = null;

      await payment.save();
      await booking.save();

      return res.json({
        message: 'Payment verified and booking confirmed successfully',
        booking,
        payment,
      });
    }

    res.status(400).json({ message: 'Invalid result value (must be SUCCESS or FAILURE)' });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Bookings  (/api/bookings)
// ═══════════════════════════════════════════════════════════════════════════════
const bookingsRouter = express.Router();
bookingsRouter.use(authenticate);

// GET /api/bookings/my-tickets – user's tickets (only CONFIRMED & USED)
bookingsRouter.get('/my-tickets', async (req, res) => {
  try {
    const bookings = await Booking.find({
      userId: req.user.id,
      status: { $in: ['CONFIRMED', 'USED', 'CANCELLED'] },
    })
      .populate('eventId', 'title date location imageUrl price')
      .sort({ createdAt: -1 });
    res.json(bookings);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/bookings – all bookings for current user
bookingsRouter.get('/', async (req, res) => {
  try {
    const bookings = await Booking.find({ userId: req.user.id })
      .populate('eventId', 'title date location imageUrl price')
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

// PATCH /api/bookings/:id/cancel – Cancel booking with 48-hour check
bookingsRouter.patch('/:id/cancel', async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id);
    if (!booking) return res.status(404).json({ message: 'Booking not found' });

    if (booking.userId.toString() !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(403).json({ message: 'Forbidden – not your booking' });
    }

    if (booking.status !== 'CONFIRMED') {
      return res.status(400).json({ message: `Cannot cancel booking with status: ${booking.status}` });
    }

    const event = await Event.findById(booking.eventId);
    if (!event) return res.status(404).json({ message: 'Event not found' });

    // 48-hour cancellation policy check
    const eventTime = new Date(event.date).getTime();
    if (!isNaN(eventTime)) {
      const diffHours = (eventTime - Date.now()) / (1000 * 60 * 60);
      if (diffHours < 48) {
        return res.status(400).json({
          message: 'Cancellation not allowed. Cancellations must be made at least 48 hours before the event start time.',
        });
      }
    }

    booking.status = 'CANCELLED';
    await booking.save();

    // Release seats
    event.availableSeats = Math.min(event.totalSeats, event.availableSeats + booking.ticketCount);
    await event.save();

    // Process mock refund
    let refundInfo = null;
    if (booking.paymentId) {
      const payment = await Payment.findById(booking.paymentId);
      if (payment && payment.status === 'PAID') {
        const refundRes = await paymentProvider.refundPayment({
          paymentId: payment._id.toString(),
          amount: payment.amount,
          reason: 'User cancelled booking',
        });
        payment.status = 'REFUNDED';
        payment.refundId = refundRes.refundId;
        await payment.save();
        refundInfo = refundRes;
      }
    }

    res.json({
      message: 'Booking cancelled successfully. Seats have been released and refund processed.',
      booking,
      refund: refundInfo,
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════════════════════
// ROUTES – Organizer Check-in  (/api/organizer/check-in)
// ═══════════════════════════════════════════════════════════════════════════════
const organizerRouter = express.Router();
organizerRouter.use(authenticate, requireRole('ORGANIZER', 'ADMIN'));

// POST /api/organizer/check-in – QR or Manual Booking ID check-in
organizerRouter.post('/check-in', async (req, res) => {
  try {
    const { qrCodeHash, bookingId } = req.body;
    if (!qrCodeHash && !bookingId) {
      return res.status(400).json({ message: 'Either qrCodeHash or bookingId is required' });
    }

    const query = qrCodeHash ? { qrCodeHash } : { _id: bookingId };
    const booking = await Booking.findOne(query).populate('eventId', 'title date location organizerId');
    if (!booking) {
      return res.status(404).json({ message: 'Ticket not found. Invalid QR code or Booking ID.' });
    }

    // Verify organizer owns the event (or is ADMIN)
    if (
      booking.eventId.organizerId.toString() !== req.user.id &&
      req.user.role !== 'ADMIN'
    ) {
      return res.status(403).json({ message: 'Forbidden – you are not the organizer of this event' });
    }

    if (booking.status === 'CANCELLED') {
      return res.status(400).json({ message: 'Warning: This ticket has been CANCELLED and is not valid for entry.' });
    }

    if (booking.status === 'PENDING') {
      return res.status(400).json({ message: 'Warning: This ticket is PENDING payment and not confirmed.' });
    }

    if (booking.isCheckedIn || booking.status === 'USED') {
      return res.status(409).json({
        message: `Warning: Already checked in on ${booking.checkedInAt ? new Date(booking.checkedInAt).toLocaleString() : 'earlier date'}.`,
        booking,
      });
    }

    booking.isCheckedIn = true;
    booking.checkedInAt = new Date();
    booking.status = 'USED';
    await booking.save();

    res.json({
      message: 'Check-in successful! Entry approved.',
      booking,
    });
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
    const events = await Event.find({ status: 'PENDING_APPROVAL' }).sort({ createdAt: -1 });
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
      Booking.aggregate([
        { $match: { status: { $in: ['CONFIRMED', 'USED'] } } },
        { $group: { _id: null, total: { $sum: '$totalPrice' } } },
      ]),
    ]);
    const pendingEvents = await Event.countDocuments({ status: 'PENDING_APPROVAL' });
    const publishedEvents = await Event.countDocuments({ status: 'PUBLISHED' });
    const totalRevenue = revenueAgg[0]?.total || 0;
    res.json({ totalUsers, totalEvents, pendingEvents, publishedEvents, totalBookings, totalRevenue });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// GET /api/admin/users – List all users
adminRouter.get('/users', async (req, res) => {
  try {
    const users = await User.find({})
      .select('name email role isBlocked phoneNumber createdAt')
      .sort({ createdAt: -1 });
    res.json(users);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
});

// PATCH /api/admin/users/:id/block – Block / Unblock user
adminRouter.patch('/users/:id/block', async (req, res) => {
  try {
    const { isBlocked } = req.body;
    if (typeof isBlocked !== 'boolean') {
      return res.status(400).json({ message: 'isBlocked boolean is required' });
    }

    if (req.params.id === req.user.id) {
      return res.status(400).json({ message: 'Admin cannot block their own account' });
    }

    const user = await User.findByIdAndUpdate(
      req.params.id,
      { isBlocked },
      { new: true }
    ).select('name email role isBlocked phoneNumber');

    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json({ message: `User ${user.name} is now ${isBlocked ? 'blocked' : 'unblocked'}`, user });
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
      const publishedEvents = await Event.find({ status: 'PUBLISHED' })
        .select('title description category date location price availableSeats')
        .limit(20)
        .lean();

      const eventsSummary = publishedEvents
        .map(
          (e) =>
            `- "${e.title}" [${e.category}] on ${e.date} at ${e.location}. Price: ${
              e.price > 0 ? '₹' + e.price : 'Free'
            }, Available Seats: ${e.availableSeats}`
        )
        .join('\n');

      systemInstructionText = `You are EventPulse Discovery Assistant, a friendly AI guide for event attendees.
Recommend upcoming events to users based on their questions, preferences, and mood.
Always mention the event name, date, venue/location, and ticket price (in ₹ / INR).
Here is the live catalog of currently PUBLISHED events available in EventPulse:
${eventsSummary || 'No published events currently available in the database.'}

If the user asks about something not in the list, recommend the closest matching categories or advise them to check back soon. Keep answers concise, helpful, and formatted with bullet points.`;
    }

    const contents = [];
    if (Array.isArray(history)) {
      for (const item of history) {
        if (item.content) {
          contents.push({
            role: item.isFromUser ? 'user' : 'model',
            parts: [{ text: item.content }],
          });
        }
      }
    }
    contents.push({
      role: 'user',
      parts: [{ text: message }],
    });

    const geminiRequestBody = {
      system_instruction: {
        parts: [{ text: systemInstructionText }],
      },
      contents,
    };

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;
    const response = await fetch(geminiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(geminiRequestBody),
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
app.get('/health', (req, res) => res.json({ status: 'ok', paymentMode: PAYMENT_MODE, timestamp: new Date().toISOString() }));

// ─── Mount all routers ────────────────────────────────────────────────────────
app.use('/api/auth', authRouter);
app.use('/api/events', eventsRouter);
app.use('/api/payments', paymentsRouter);
app.use('/api/bookings', bookingsRouter);
app.use('/api/organizer', organizerRouter);
app.use('/api/admin', adminRouter);
app.use('/api/chat', chatRouter);

// ─── 404 handler ─────────────────────────────────────────────────────────────
app.use((req, res) => res.status(404).json({ message: `Route ${req.originalUrl} not found` }));

// ─── Start server ─────────────────────────────────────────────────────────────
app.listen(PORT, () => console.log(`[EventPulse API] Server running on http://localhost:${PORT} (Payment Mode: ${PAYMENT_MODE})`));
