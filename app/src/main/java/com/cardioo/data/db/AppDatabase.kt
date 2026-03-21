package com.cardioo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cardioo.data.db.dao.HealthMeasurementDao
import com.cardioo.data.db.dao.UserDao
import com.cardioo.data.db.entity.HealthMeasurementEntity
import com.cardioo.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HealthMeasurementEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun measurementDao(): HealthMeasurementDao
}
