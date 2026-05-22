package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppSettingDao
import com.example.data.dao.PdfShelfItemDao
import com.example.data.dao.SubjectDao
import com.example.data.entity.AppSetting
import com.example.data.entity.PdfShelfItem
import com.example.data.entity.Subject

@Database(
    entities = [Subject::class, PdfShelfItem::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class StudyShelfDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun pdfShelfItemDao(): PdfShelfItemDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: StudyShelfDatabase? = null

        fun getDatabase(context: Context): StudyShelfDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyShelfDatabase::class.java,
                    "studyshelf_database"
                )
                .fallbackToDestructiveMigration() // safe for our school app local db
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
