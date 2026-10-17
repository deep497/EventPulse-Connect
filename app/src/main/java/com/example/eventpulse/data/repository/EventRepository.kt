package com.example.eventpulse.data.repository

import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.remote.EventPulseApi

/**
 * Repository for all Event-related API calls.
 *
 * All methods return [Result] so callers can handle both success and failure
 * without relying on exceptions flowing up to the UI layer.
 */
class EventRepository(
    private val api: EventPulseApi
) {

    // ── Public listing ─────────────────────────────────────────────────────────

    /**
     * `GET /api/events` – returns published events.
     * @param status   filter by status (e.g. `PUBLISHED`); null → backend defaults to PUBLISHED
     * @param category optional category filter
     * @param search   full-text search term
     */
    suspend fun getEvents(
        status: String? = null,
        category: String? = null,
        search: String? = null
    ): Result<List<Event>> {
        return try {
            val response = api.getEvents(status, category, search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch events: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** `GET /api/events/organizer` – the authenticated organizer's own events. */
    suspend fun getOrganizerEvents(): Result<List<Event>> {
        return try {
            val response = api.getOrganizerEvents()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch organizer events: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** `GET /api/events/:id` */
    suspend fun getEventById(eventId: String): Result<Event> {
        return try {
            val response = api.getEventById(eventId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch event: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── CRUD ───────────────────────────────────────────────────────────────────

    /** `POST /api/events` – creates with status = `PENDING_APPROVAL`. */
    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val response = api.createEvent(event)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create event: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** `PUT /api/events/:id` – organizer updates their event (triggers re-approval). */
    suspend fun updateEvent(eventId: String, event: Event): Result<Event> {
        return try {
            val response = api.updateEvent(eventId, event)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update event: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** `DELETE /api/events/:id` */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val response = api.deleteEvent(eventId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete event: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Admin operations ───────────────────────────────────────────────────────

    /** `GET /api/admin/pending-events` */
    suspend fun getPendingEvents(): Result<List<Event>> {
        return try {
            val response = api.getPendingEvents()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pending events: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * `PATCH /api/admin/events/:id/status`
     * @param status Must be `"PUBLISHED"` or `"REJECTED"`.
     */
    suspend fun updateEventStatus(eventId: String, status: String): Result<Event> {
        return try {
            val response = api.updateEventStatus(eventId, mapOf("status" to status))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update event status: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** `GET /api/admin/metrics` – returns dashboard counters (totalUsers, revenue, etc.). */
    suspend fun getAdminMetrics(): Result<Map<String, Any>> {
        return try {
            val response = api.getAdminMetrics()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch metrics: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
