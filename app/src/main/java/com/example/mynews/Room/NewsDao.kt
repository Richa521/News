package com.example.mynews.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mynews.models.NewsArticle

@Dao
interface NewsDao {
    @Query("SELECT * FROM NewsArticle")
    suspend fun getAllArticles(): List<NewsArticle>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertArticles(articles: List<NewsArticle>)


    @Query("DELETE FROM NewsArticle")
    suspend fun deleteAllArticles()



}