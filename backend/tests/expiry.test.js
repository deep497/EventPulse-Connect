const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });
const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');

const PORT = process.env.PORT || 5000;
const BASE_URL = `http://localhost:${PORT}/api`;
const JWT_SECRET = process.env.JWT_SECRET || 'eventpulse_super_secret_jwt_key_2026';

// Helper to make fetch requests
async function req(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const res = await fetch(url, options);
  let json = null;
  try {
    json = await res.json();
  } catch (_) {}
  return { status: res.status, ok: res.ok, body: json };
}

async function runTests() {
  console.log('--- STARTING EXPIRY TESTS ---');
  await mongoose.connect(process.env.MONGODB_URI);

  const User = mongoose.model('User', new mongoose.Schema({}, { strict: false }));
  const Event = mongoose.model('Event', new mongoose.Schema({}, { strict: false }));
  const Booking = mongoose.model('Booking', new mongoose.Schema({}, { strict: false }));

  // 1. Get or create test users
  let organizer = await User.findOne({ role: 'ORGANIZER' });
  let user = await User.findOne({ role: 'USER' });

  if (!organizer || !user) {
    console.error('Organizer or User not found in DB. Run seedDemo.js first.');
    process.exit(1);
  }

  const organizerToken = jwt.sign({ id: organizer._id.toString(), role: organizer.role }, JWT_SECRET);
  const userToken = jwt.sign({ id: user._id.toString(), role: user.role }, JWT_SECRET);

  const userHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${userToken}`
  };
  const organizerHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${organizerToken}`
  };

  // Create an explicitly expired event in DB
  const pastTime = new Date(Date.now() - 5 * 60 * 60 * 1000); // 5 hours ago
  const expiredEvent = await Event.create({
    title: 'Test Expired Event for Automated Verification',
    description: 'Should be rejected for booking and excluded from public explore.',
    category: 'Technology',
    date: '2026-01-01 10:00',
    startDateTime: pastTime,
    endDateTime: new Date(pastTime.getTime() + 2 * 60 * 60 * 1000), // ended 3 hours ago
    location: 'Virtual Hall',
    price: 100,
    totalSeats: 50,
    availableSeats: 50,
    organizerId: organizer._id,
    status: 'PUBLISHED'
  });
  console.log(`Created test expired event: ${expiredEvent._id}`);

  // Test 1: GET /api/events must EXCLUDE the expired event
  console.log('\n[TEST 1] GET /api/events excludes expired events for public');
  const exploreRes = await req('/events');
  if (exploreRes.status !== 200) throw new Error(`GET /events returned ${exploreRes.status}`);
  const hasExpired = exploreRes.body.some(e => e._id.toString() === expiredEvent._id.toString());
  if (hasExpired) {
    throw new Error('FAILED: Expired event was found in public GET /api/events listing!');
  }
  console.log('PASSED: Expired event is excluded from public explore listing.');

  // Test 2: POST /api/payments/order rejects booking an expired event
  console.log('\n[TEST 2] POST /api/payments/order rejects expired event with 400 "This event has ended"');
  const orderRes = await req('/payments/order', {
    method: 'POST',
    headers: userHeaders,
    body: JSON.stringify({ eventId: expiredEvent._id.toString(), quantity: 1 })
  });
  if (orderRes.status !== 400 || !orderRes.body?.message?.includes('ended')) {
    throw new Error(`FAILED: Expected 400 "This event has ended", got status ${orderRes.status}: ${JSON.stringify(orderRes.body)}`);
  }
  console.log('PASSED: Booking expired event was rejected with 400 "This event has ended".');

  // Test 3: POST /api/bookings rejects booking an expired event
  console.log('\n[TEST 3] POST /api/bookings rejects expired event with 400 "This event has ended"');
  const bookRes = await req('/bookings', {
    method: 'POST',
    headers: userHeaders,
    body: JSON.stringify({ eventId: expiredEvent._id.toString(), ticketCount: 1 })
  });
  if (bookRes.status !== 400 || !bookRes.body?.message?.includes('ended')) {
    throw new Error(`FAILED: Expected 400 "This event has ended", got status ${bookRes.status}: ${JSON.stringify(bookRes.body)}`);
  }
  console.log('PASSED: Direct booking on expired event rejected with 400.');

  // Test 4: Check-in on expired event is rejected
  console.log('\n[TEST 4] POST /api/organizer/check-in rejects check-in for expired event');
  // Create a booking directly in DB for this expired event
  const expiredBooking = await Booking.create({
    eventId: expiredEvent._id,
    userId: user._id,
    ticketCount: 1,
    totalPrice: 100,
    status: 'CONFIRMED',
    isCheckedIn: false,
    qrCodeHash: 'test_expired_qr_' + Date.now()
  });

  const checkinRes = await req('/organizer/check-in', {
    method: 'POST',
    headers: organizerHeaders,
    body: JSON.stringify({ qrCodeHash: expiredBooking.qrCodeHash })
  });
  if (checkinRes.status !== 400 || !checkinRes.body?.message?.includes('ended')) {
    throw new Error(`FAILED: Expected 400 "Event has ended. Check-in is closed.", got ${checkinRes.status}: ${JSON.stringify(checkinRes.body)}`);
  }
  console.log('PASSED: Check-in on expired event rejected with 400.');

  // Test 5: Cancel after start is rejected
  console.log('\n[TEST 5] PATCH /api/bookings/:id/cancel rejects cancellation after event has started/ended');
  const cancelRes = await req(`/bookings/${expiredBooking._id}/cancel`, {
    method: 'PATCH',
    headers: userHeaders
  });
  if (cancelRes.status !== 400 || !cancelRes.body?.message?.includes('already started or ended')) {
    throw new Error(`FAILED: Expected 400 cancellation rejected, got ${cancelRes.status}: ${JSON.stringify(cancelRes.body)}`);
  }
  console.log('PASSED: Cancellation after start/end rejected with 400.');

  // Test 6: GET /api/bookings/mine returns ticketState = EXPIRED for this booking
  console.log('\n[TEST 6] GET /api/bookings/mine returns ticketState = EXPIRED for ended event');
  const mineBookingsRes = await req('/bookings/mine', { headers: userHeaders });
  if (mineBookingsRes.status !== 200) throw new Error('GET /bookings/mine failed');
  const targetBooking = mineBookingsRes.body.find(b => b._id.toString() === expiredBooking._id.toString());
  if (!targetBooking) throw new Error('Booking not found in /bookings/mine');
  if (targetBooking.ticketState !== 'EXPIRED' || targetBooking.isExpired !== true) {
    throw new Error(`FAILED: Expected ticketState="EXPIRED", isExpired=true. Got: ${JSON.stringify(targetBooking)}`);
  }
  console.log('PASSED: Ticket state correctly returned as EXPIRED.');

  // Clean up test event and booking
  await Booking.deleteOne({ _id: expiredBooking._id });
  await Event.deleteOne({ _id: expiredEvent._id });
  console.log('\nCleaned up test event and booking.');
  console.log('=== ALL EXPIRY INTEGRATION TESTS PASSED SUCCESSFULLY! ===');
  process.exit(0);
}

runTests().catch(err => {
  console.error('\nTEST FAILURE:', err);
  process.exit(1);
});
