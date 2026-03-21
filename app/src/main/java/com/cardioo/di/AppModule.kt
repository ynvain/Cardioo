package com.cardioo.di

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cardioo.data.db.AppDatabase
import com.cardioo.data.db.Migrations
import com.cardioo.data.db.dao.HealthMeasurementDao
import com.cardioo.data.db.dao.UserDao
import com.cardioo.data.repository.MeasurementRepositoryImpl
import com.cardioo.data.repository.UserRepositoryImpl
import com.cardioo.domain.repository.MeasurementRepository
import com.cardioo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { ctx.preferencesDataStoreFile("cardioo_session.preferences_pb") },
        )

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "cardioo.db")
            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3)
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideMeasurementDao(db: AppDatabase): HealthMeasurementDao = db.measurementDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds abstract fun bindUserRepo(impl: UserRepositoryImpl): UserRepository
    @Binds abstract fun bindMeasurementRepo(impl: MeasurementRepositoryImpl): MeasurementRepository
}

