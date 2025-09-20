package com.exodus.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.exodus.data.model.Message

@Database(
    entities = [Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExodusDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ExodusDatabase? = null

        fun getDatabase(context: Context): ExodusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExodusDatabase::class.java,
                    "exodus_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
