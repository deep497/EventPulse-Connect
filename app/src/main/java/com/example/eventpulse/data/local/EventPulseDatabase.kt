package com.example.eventpulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.eventpulse.data.local.entities.TicketEntity

/**
 * EventPulse Room database.
 *
 * Contains the `tickets` table which caches confirmed booking records fetched
 * from the backend, providing offline access to the user's tickets.
 *
 * **Schema version history**
 * | Version | Change |
 * |---------|--------|
 * | 1       | Initial schema (id, eventId, eventName, eventDate, eventVenue, quantity, totalPrice, qrCode, bookingDate, status) |
 * | 2       | Renamed `qrCode` → `qrCodeHash`; added `isCheckedIn` (INTEGER, default 0) |
 */
@Database(
    entities = [TicketEntity::class],
    version = 2,
    exportSchema = false
)
abstract class EventPulseDatabase : RoomDatabase() {

    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: EventPulseDatabase? = null

        /**
         * Migration from v1 → v2:
         * - Adds the `qrCodeHash` column (TEXT, empty string default)
         * - Copies old `qrCode` value into `qrCodeHash`
         * - Drops the old `qrCode` column via table rebuild
         * - Adds the `isCheckedIn` column (INTEGER, default 0)
         *
         * SQLite does not support DROP COLUMN directly (before API 35), so we
         * recreate the table using the standard rename-create-copy-drop pattern.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with the updated schema
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tickets_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        eventId TEXT NOT NULL,
                        eventName TEXT NOT NULL,
                        eventDate TEXT NOT NULL,
                        eventVenue TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        totalPrice REAL NOT NULL,
                        qrCodeHash TEXT NOT NULL DEFAULT '',
                        bookingDate INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        isCheckedIn INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                // 2. Copy existing data; map old qrCode → qrCodeHash
                db.execSQL(
                    """
                    INSERT INTO tickets_new
                        (id, eventId, eventName, eventDate, eventVenue, quantity, totalPrice, qrCodeHash, bookingDate, status, isCheckedIn)
                    SELECT
                        id, eventId, eventName, eventDate, eventVenue, quantity, totalPrice, qrCode, bookingDate, status, 0
                    FROM tickets
                    """.trimIndent()
                )
                // 3. Drop the old table and rename the new one
                db.execSQL("DROP TABLE tickets")
                db.execSQL("ALTER TABLE tickets_new RENAME TO tickets")
            }
        }

        fun getDatabase(context: Context): EventPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventPulseDatabase::class.java,
                    "eventpulse_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
