package com.example.eventpulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [],
    version = 1,
    exportSchema = false
)
abstract class EventPulseDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: EventPulseDatabase? = null

        fun getDatabase(context: Context): EventPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventPulseDatabase::class.java,
                    "eventpulse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
