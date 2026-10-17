package com.example.eventpulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventpulse.data.local.entities.TicketEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the `tickets` table.
 *
 * Confirmed [TicketEntity] records are cached here whenever the user successfully
 * fetches their bookings from the backend, enabling offline ticket access.
 */
@Dao
interface TicketDao {

    // ── Queries ─────────────────────────────────────────────────────────────────

    /** Returns all cached tickets, newest first, as a reactive [Flow]. */
    @Query("SELECT * FROM tickets ORDER BY bookingDate DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    /** Returns a single ticket by its server booking ID, or null if not cached. */
    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    /** Returns all tickets for a specific event as a reactive [Flow]. */
    @Query("SELECT * FROM tickets WHERE eventId = :eventId")
    fun getTicketsByEvent(eventId: String): Flow<List<TicketEntity>>

    /**
     * Looks up a ticket by its QR hash.
     * Used during offline check-in validation (organizer verifies a QR without network).
     */
    @Query("SELECT * FROM tickets WHERE qrCodeHash = :hash LIMIT 1")
    suspend fun getTicketByQrHash(hash: String): TicketEntity?

    // ── Writes ──────────────────────────────────────────────────────────────────

    /**
     * Inserts or replaces a ticket.
     * On conflict (same primary key) the row is fully replaced so that
     * status / check-in state stays in sync with the server.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    /**
     * Bulk-inserts or replaces a list of tickets.
     * Called after a successful `GET /api/bookings/my-tickets` to refresh the cache.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    /** Updates the status column of a single ticket. */
    @Query("UPDATE tickets SET status = :status WHERE id = :ticketId")
    suspend fun updateTicketStatus(ticketId: String, status: String)

    /** Marks a ticket as checked-in locally (reflects server-side isCheckedIn=true). */
    @Query("UPDATE tickets SET isCheckedIn = 1 WHERE id = :ticketId")
    suspend fun markCheckedIn(ticketId: String)

    // ── Deletes ─────────────────────────────────────────────────────────────────

    /** Removes a single cached ticket by ID. */
    @Query("DELETE FROM tickets WHERE id = :ticketId")
    suspend fun deleteTicket(ticketId: String)

    /** Clears the entire cache (e.g., on logout). */
    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
}
