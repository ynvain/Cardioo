package com.cardioo.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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

    @Upsert
    suspend fun upsert(entity: UserEntity): Long

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: Long)
}

