package com.example.toureguidee.data.repository

import com.example.toureguidee.data.database.AttractionDao
import com.example.toureguidee.data.model.Attraction
import kotlinx.coroutines.flow.Flow

class AttractionRepository(private val attractionDao: AttractionDao) {
    val allAttractions: Flow<List<Attraction>> = attractionDao.getAllAttractions()

    suspend fun insertAttraction(attraction: Attraction): Long {
        return attractionDao.insertAttraction(attraction)
    }

    suspend fun deleteAttraction(attraction: Attraction) {
        attractionDao.deleteAttraction(attraction)
    }

    suspend fun getAttractionById(id: Long): Attraction? {
        return attractionDao.getAttractionById(id)
    }
}
