package com.example.chatbot.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chatbot.ChatSession
import com.example.chatbot.Message
import com.example.chatbot.UriTypeConverter

@Database(entities = [Message::class, ChatSession::class], version = 1, exportSchema = false)
@TypeConverters(UriTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}