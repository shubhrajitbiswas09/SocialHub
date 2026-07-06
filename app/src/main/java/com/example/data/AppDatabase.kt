package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log

@Database(
    entities = [
        Creator::class,
        Post::class,
        Subscription::class,
        Transaction::class,
        ChatMessage::class,
        Event::class,
        MarketplaceProduct::class,
        MarketplaceBanner::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): SocialHubDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "social_hub_database"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    Log.d(TAG, "Database initialized successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Database initialization failed: ${e.message}")
                    throw RuntimeException("Failed to initialize database", e)
                }
            }
        }
    }
}
