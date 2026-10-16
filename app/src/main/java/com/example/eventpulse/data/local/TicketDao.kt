package com.example.eventpulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    // Placeholder for ticket entity operations
    // @Query("SELECT * FROM tickets")
    // fun getAllTickets(): Flow<List<Ticket>>

    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    // suspend fun insertTicket(ticket: Ticket)

    // @Query("DELETE FROM tickets WHERE id = :ticketId")
    // suspend fun deleteTicket(ticketId: String)
}
