const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const MONGODB_URI = process.env.MONGODB_URI;

// ─── Schemas ───
const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  role: { type: String, enum: ['USER', 'ORGANIZER', 'ADMIN'], default: 'USER' },
  phoneNumber: { type: String, default: '' },
  isBlocked: { type: Boolean, default: false }
}, { timestamps: true });

const eventSchema = new mongoose.Schema({
  title: { type: String, required: true },
  description: { type: String, required: true },
  category: { type: String, required: true },
  date: { type: String, required: true },
  startDateTime: { type: Date },
  endDateTime: { type: Date },
  location: { type: String, required: true },
  price: { type: Number, default: 0 },
  totalSeats: { type: Number, default: 100 },
  availableSeats: { type: Number, default: 100 },
  imageUrl: { type: String, default: '' },
  organizerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  status: { type: String, default: 'PUBLISHED' }
}, { timestamps: true });

const bookingSchema = new mongoose.Schema({
  eventId: { type: mongoose.Schema.Types.ObjectId, ref: 'Event', required: true },
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  ticketCount: { type: Number, required: true },
  totalPrice: { type: Number, required: true },
  status: { type: String, default: 'CONFIRMED' },
  qrCodeHash: { type: String, unique: true, sparse: true },
  isCheckedIn: { type: Boolean, default: false },
  checkedInAt: { type: Date },
  expiresAt: { type: Date }
}, { timestamps: true });

const User = mongoose.model('User', userSchema);
const Event = mongoose.model('Event', eventSchema);
const Booking = mongoose.model('Booking', bookingSchema);

async function seed() {
  console.log('[Seed] Connecting to MongoDB...');
  await mongoose.connect(MONGODB_URI);
  console.log('[Seed] Connected.');

  const hashedPassword = await bcrypt.hash('password123', 10);

  // 1. Ensure Demo Users exist
  let organizer = await User.findOne({ email: 'organizer@eventpulse.com' });
  if (!organizer) {
    organizer = await User.create({
      name: 'Priya Sharma (Organizer)',
      email: 'organizer@eventpulse.com',
      password: hashedPassword,
      role: 'ORGANIZER',
      phoneNumber: '+91 98765 43210'
    });
    console.log('[Seed] Created organizer user');
  }

  let demoUser = await User.findOne({ email: 'user@eventpulse.com' });
  if (!demoUser) {
    demoUser = await User.create({
      name: 'Aarav Patel',
      email: 'user@eventpulse.com',
      password: hashedPassword,
      role: 'USER',
      phoneNumber: '+91 98111 22233'
    });
    console.log('[Seed] Created demo attendee user');
  }

  const now = Date.now();
  const pastDate1 = new Date(now - 7 * 24 * 60 * 60 * 1000); // 7 days ago
  const pastDate2 = new Date(now - 2 * 24 * 60 * 60 * 1000); // 2 days ago
  const futureDate1 = new Date(now + 5 * 24 * 60 * 60 * 1000); // in 5 days
  const futureDate2 = new Date(now + 14 * 24 * 60 * 60 * 1000); // in 14 days

  const pad = (n) => String(n).padStart(2, '0');
  const formatStr = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:00`;

  // 2. Past Event 1 (Expired, with attended tickets)
  let pastEvent1 = await Event.findOne({ title: 'Ancient Himalayan Meditation Retreat' });
  if (!pastEvent1) {
    const end1 = new Date(pastDate1.getTime() + 3 * 60 * 60 * 1000);
    pastEvent1 = await Event.create({
      title: 'Ancient Himalayan Meditation Retreat',
      description: 'A serene past spiritual meditation session held in the hills of Rishikesh.',
      category: 'Spirituality',
      date: formatStr(pastDate1),
      startDateTime: pastDate1,
      endDateTime: end1,
      location: 'Rishikesh Yoga Ashram',
      price: 499,
      totalSeats: 50,
      availableSeats: 30,
      imageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=800&q=80',
      organizerId: organizer._id,
      status: 'PUBLISHED'
    });

    // Create a checked-in booking for demoUser
    await Booking.create({
      eventId: pastEvent1._id,
      userId: demoUser._id,
      ticketCount: 2,
      totalPrice: 998,
      status: 'USED',
      isCheckedIn: true,
      checkedInAt: new Date(pastDate1.getTime() + 30 * 60 * 1000),
      qrCodeHash: 'seed_qr_past_attended_' + Date.now()
    });
    console.log('[Seed] Created past event 1 with checked-in booking');
  }

  // 3. Past Event 2 (Expired, with no-show / expired tickets)
  let pastEvent2 = await Event.findOne({ title: 'Bangalore Tech Summit 2026 Retro' });
  if (!pastEvent2) {
    const end2 = new Date(pastDate2.getTime() + 4 * 60 * 60 * 1000);
    pastEvent2 = await Event.create({
      title: 'Bangalore Tech Summit 2026 Retro',
      description: 'Previous tech summit keynote and panel discussions.',
      category: 'Technology',
      date: formatStr(pastDate2),
      startDateTime: pastDate2,
      endDateTime: end2,
      location: 'Bangalore Palace Grounds',
      price: 1200,
      totalSeats: 100,
      availableSeats: 80,
      imageUrl: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=800&q=80',
      organizerId: organizer._id,
      status: 'PUBLISHED'
    });

    // Create an un-attended expired booking for demoUser
    await Booking.create({
      eventId: pastEvent2._id,
      userId: demoUser._id,
      ticketCount: 1,
      totalPrice: 1200,
      status: 'CONFIRMED', // Was confirmed but event ended without checkin -> EXPIRED state
      isCheckedIn: false,
      qrCodeHash: 'seed_qr_past_noshow_' + Date.now()
    });
    console.log('[Seed] Created past event 2 with expired/no-show ticket');
  }

  // 4. Future Event 1 (Upcoming Active)
  let futureEvent1 = await Event.findOne({ title: 'Vedic Chanting & Mindfulness Immersion' });
  if (!futureEvent1) {
    const endF1 = new Date(futureDate1.getTime() + 3 * 60 * 60 * 1000);
    futureEvent1 = await Event.create({
      title: 'Vedic Chanting & Mindfulness Immersion',
      description: 'Experience deep peace and sacred vibrations with traditional master chanters.',
      category: 'Spirituality',
      date: formatStr(futureDate1),
      startDateTime: futureDate1,
      endDateTime: endF1,
      location: 'Iskcon Cultural Hall, New Delhi',
      price: 350,
      totalSeats: 150,
      availableSeats: 120,
      imageUrl: 'https://images.unsplash.com/photo-1545205597-3d9d02c29597?auto=format&fit=crop&w=800&q=80',
      organizerId: organizer._id,
      status: 'PUBLISHED'
    });

    // Create an ACTIVE booking for demoUser
    await Booking.create({
      eventId: futureEvent1._id,
      userId: demoUser._id,
      ticketCount: 2,
      totalPrice: 700,
      status: 'CONFIRMED',
      isCheckedIn: false,
      qrCodeHash: 'seed_qr_upcoming_active_' + Date.now()
    });
    console.log('[Seed] Created future upcoming event with active ticket');
  }

  console.log('[Seed] Completed successfully! All demo events and bookings are ready.');
  process.exit(0);
}

seed().catch((err) => {
  console.error('[Seed Error]:', err);
  process.exit(1);
});
