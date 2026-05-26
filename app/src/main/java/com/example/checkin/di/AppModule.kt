package com.example.checkin.di

import android.content.Context
import androidx.room.Room
import com.example.checkin.data.db.AppDatabase
import com.example.checkin.data.db.dao.ProjectDao
import com.example.checkin.data.db.dao.RecordDao
import com.example.checkin.data.db.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "checkin_database"
        ).addMigrations(AppDatabase.MIGRATION_1_2)
         .build()
    }

    @Provides
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideRecordDao(db: AppDatabase): RecordDao = db.recordDao()

    @Provides
    fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
}
