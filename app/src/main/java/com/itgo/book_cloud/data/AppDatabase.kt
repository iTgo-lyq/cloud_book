package com.itgo.book_cloud.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itgo.book_cloud.common.Constant.DB_Name
import com.itgo.book_cloud.data.dao.LocalBookDao
import com.itgo.book_cloud.data.dao.ChapterDao
import com.itgo.book_cloud.data.dao.MediaSourceDao
import com.itgo.book_cloud.data.entity.Chapter
import com.itgo.book_cloud.data.entity.LocalBook
import com.itgo.book_cloud.data.entity.MediaSource

@Database(
    version = 10,
    exportSchema = false,
    entities = [LocalBook::class, Chapter::class, MediaSource::class],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chapterDap(): ChapterDao
    abstract fun localBookDao(): LocalBookDao
    abstract fun mediaSourceDao(): MediaSourceDao

    companion object {
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_Name
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build().apply {
                    instance = this
                }
        }
    }
}