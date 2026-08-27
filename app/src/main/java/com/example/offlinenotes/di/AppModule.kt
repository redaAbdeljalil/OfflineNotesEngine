package com.example.offlinenotes.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.offlinenotes.data.local.OfflineNotesDatabase
import com.example.offlinenotes.data.remote.MockRemoteDataSource
import com.example.offlinenotes.data.repository.NoteRepositoryImpl
import com.example.offlinenotes.data.repository.SecurityRepositoryImpl
import com.example.offlinenotes.data.repository.SettingsRepositoryImpl
import com.example.offlinenotes.domain.repository.NoteRepository
import com.example.offlinenotes.domain.repository.SecurityRepository
import com.example.offlinenotes.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSecurityRepository(@ApplicationContext context: Context): SecurityRepository {
        return SecurityRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securityRepository: SecurityRepository
    ): OfflineNotesDatabase {
        val dbName = "offline_notes_premium_db" 
        val factory = SupportFactory(securityRepository.getDatabasePassphrase())
        return Room.databaseBuilder(
            context,
            OfflineNotesDatabase::class.java,
            dbName
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        database: OfflineNotesDatabase,
        workManager: WorkManager,
        settingsRepository: SettingsRepository
    ): NoteRepository {
        return NoteRepositoryImpl(database, workManager, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(): MockRemoteDataSource = MockRemoteDataSource()
}