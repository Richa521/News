package com.example.mynews.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class NewsArticle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @TypeConverters(Converters::class)
    val source: Source,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

