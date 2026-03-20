package com.cardioo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cardioo.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY id ASC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserEntity): Long

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: Long)
}

