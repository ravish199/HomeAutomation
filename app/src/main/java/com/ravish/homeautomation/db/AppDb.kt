package com.ravish.homeautomation.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ravish.homeautomation.model.AlarmDetails
import com.ravish.homeautomation.model.DateConverter

@Database(entities = [AlarmDetails::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun alarmDetailsDao(): AlarmDetailsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun getDatabase(context: Context): AppDb {
            var tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "HomeAutomation"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
