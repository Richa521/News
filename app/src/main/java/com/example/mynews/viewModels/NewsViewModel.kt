package com.example.mynews.viewModels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.retrofit.NewsRepository
import com.example.mynews.NewsState
import com.example.mynews.Room.NewsDao
import com.example.mynews.Room.NewsDatabase
import com.example.mynews.models.NewsArticle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val newsRepository = NewsRepository(NewsDatabase.getInstance(application).newsDao())
    private val newsDao: NewsDao = NewsDatabase.getInstance(application).newsDao()


    private val _newsState: MutableStateFlow<NewsState> = MutableStateFlow(NewsState.Loading)
    val newsState: StateFlow<NewsState> get() = _newsState
    private val uniqueHeadlines = mutableSetOf<NewsArticle>()

    private val prefs: SharedPreferences = application.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val Pinned = "pinned_headlines"

    private val pinnedHeadlines = mutableListOf<NewsArticle>()
    private val nonPinnedHeadlines = mutableListOf<NewsArticle>()

    private var timer: Timer? = null
    var currentBatchIndex = 0
    var batchSize = 100

    init {
        loadPinnedHeadlines()
        fetchTopHundredHeadlines()
        setupTimer()
    }
    private fun loadPinnedHeadlines() {
        val pinnedHeadlinesJson = prefs.getString(Pinned, null)
        pinnedHeadlinesJson?.let {
            pinnedHeadlines.addAll(Gson().fromJson(it, object : TypeToken<List<NewsArticle>>() {}.type))
        }
    }

    private fun savePinnedHeadlines() {
        val pinnedHeadlinesJson = Gson().toJson(pinnedHeadlines)
        prefs.edit().putString(Pinned, pinnedHeadlinesJson).apply()
    }


    private var isNewBatchLoaded = false

    fun fetchNextBatch() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startIndex = currentBatchIndex * batchSize
                var nextBatch = newsRepository.getTopHundredHeadlines().subList(startIndex, startIndex + batchSize)

                if (nextBatch.size < batchSize) {
                    val remainingHeadlines = newsRepository.getTopHundredHeadlines().take(batchSize - nextBatch.size)
                    nextBatch += remainingHeadlines
                }

                withContext(Dispatchers.Main) {
                    _newsState.value = NewsState.Success(pinnedHeadlines + nonPinnedHeadlines + nextBatch)

                    val isExhausted = nextBatch.size < batchSize
                    if (isExhausted && !isNewBatchLoaded) {
                        isNewBatchLoaded = true
                        clearLocalStorage()
                        fetchNextBatch()
                    } else {
                        isNewBatchLoaded = false
                    }
                }
            } catch (e: Exception) {
                _newsState.value = NewsState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }


    private fun clearLocalStorage() {
        viewModelScope.launch(Dispatchers.IO) {
           newsDao.deleteAllArticles()
        }
    }



    private fun setupTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                viewModelScope.launch(Dispatchers.IO) {
                    val randomHeadlines = getRandomHeadlines()

                    withContext(Dispatchers.Main) {
                        nonPinnedHeadlines.addAll(0, randomHeadlines)
                        _newsState.value = NewsState.Success(pinnedHeadlines + nonPinnedHeadlines)
                    }
                }
            }
        }, 10000, 10000)
    }


    private suspend fun getRandomHeadlines(): List<NewsArticle> {
        val countries = listOf("us", "in")
        val newHeadlines = newsRepository.getRandomHeadlines(countries)

        val filteredHeadlines = newHeadlines.filter {
            !uniqueHeadlines.contains(it) && !pinnedHeadlines.contains(it) && !nonPinnedHeadlines.contains(it)
        }
        var randomHeadlines = filteredHeadlines.shuffled().take(5)

        if (randomHeadlines.size < 5) {
            val additionalHeadlines: List<NewsArticle> = newsRepository.getRandomHeadlines(countries)
                .filter { !uniqueHeadlines.contains(it) && !pinnedHeadlines.contains(it) && !nonPinnedHeadlines.contains(it) }
                .shuffled().take(5 - randomHeadlines.size)

            randomHeadlines = randomHeadlines + additionalHeadlines
        }

        uniqueHeadlines.addAll(randomHeadlines)

        return randomHeadlines
    }


    fun fetchTopHundredHeadlines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val articles = newsRepository.getTopHundredHeadlines()
                    .filter { !pinnedHeadlines.contains(it) && !nonPinnedHeadlines.contains(it) }

                withContext(Dispatchers.Main) {
                    _newsState.value = NewsState.Success(articles)
                }

                getRandomHeadlines()
            } catch (e: Exception) {
                _newsState.value = NewsState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun manualTrigger(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                timer?.cancel()
                val randomHeadlines = getRandomHeadlines()
                nonPinnedHeadlines.addAll(0, randomHeadlines)

                withContext(Dispatchers.Main) {
                    _newsState.value = NewsState.Success(pinnedHeadlines + nonPinnedHeadlines)
                    setupTimer()
                    onComplete.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Error adding random headlines", Toast.LENGTH_SHORT).show()
                    onComplete.invoke()
                }
            }
        }
    }

    fun deleteArticle(article: NewsArticle) {
        pinnedHeadlines.remove(article)
        nonPinnedHeadlines.remove(article)

        updateUI()
    }

    fun pinArticle(article: NewsArticle) {
        nonPinnedHeadlines.remove(article)
        pinnedHeadlines.add(0, article)
        updateUI()
    }

    private fun updateUI() {
        _newsState.value = NewsState.Success(pinnedHeadlines + nonPinnedHeadlines)
    }

    override fun onCleared() {
        super.onCleared()
        savePinnedHeadlines()
        clearPinnedHeadlines()
        timer?.cancel()
    }

    private fun clearPinnedHeadlines() {
        pinnedHeadlines.clear()
        updateUI()
    }
}


