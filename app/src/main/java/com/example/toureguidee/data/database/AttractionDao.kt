package com.example.toureguidee.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toureguidee.data.model.Attraction
import kotlinx.coroutines.flow.Flow

@Dao
interface AttractionDao {
    @Query("SELECT * FROM attractions ORDER BY name")
    fun getAllAttractions(): Flow<List<Attraction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttraction(attraction: Attraction): Long

    @Delete
    suspend fun deleteAttraction(attraction: Attraction)

    @Query("SELECT * FROM attractions WHERE id = :id")
    suspend fun getAttractionById(id: Long): Attraction?
}
