package com.example.mynews.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.mynews.NewsState
import com.example.mynews.R
import com.example.mynews.models.NewsArticle
import com.example.mynews.viewModels.NewsViewModel
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen() {
    val viewModel: NewsViewModel = viewModel()
    val newsState by viewModel.newsState.collectAsState()
    var selectedNewsUrl by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) { viewModel.fetchTopHundredHeadlines() }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(60.dp)
                    .padding(top = 6.dp).background(MaterialTheme.colorScheme.primary),

                title = { Text(text = "NewsApp", modifier = Modifier.padding(top = 10.dp)) },
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                isRefreshing = true
                                viewModel.manualTrigger {
                                    isRefreshing = false
                                }
                            },
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            when (newsState) {
                is NewsState.Loading -> { CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.CenterHorizontally)
                    )
                }
                is NewsState.Success -> {
                    val articles = (newsState as NewsState.Success).articles


                    val isExhausted = articles.size >= viewModel.batchSize * (viewModel.currentBatchIndex + 1)

                    if (isExhausted) {
                        viewModel.fetchNextBatch()
                        Toast.makeText(LocalContext.current, "New batch loaded!", Toast.LENGTH_SHORT).show()
                    }

                    NewsList(
                        articles = articles.take(10),
                        onSwipeToDelete = { article -> viewModel.deleteArticle(article)},
                        onSwipeToPin = { article -> viewModel.pinArticle(article)},
                        onNewsItemSelected = { url -> selectedNewsUrl = url } )

                }
                is NewsState.Error -> {
                    Text(text = "Error: ${(newsState as NewsState.Error).message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
    selectedNewsUrl?.let { url ->
        NewsWebView(url = url) { selectedNewsUrl = null }
    }
}




@Composable
fun NewsList(articles: List<NewsArticle>, onSwipeToDelete: (NewsArticle) -> Unit, onSwipeToPin: (NewsArticle) -> Unit, onNewsItemSelected: (String) -> Unit
) {
    LazyColumn {
        items(articles) { article -> NewsItem(article = article,
                onSwipeToDelete = { onSwipeToDelete.invoke(article) },
                onSwipeToPin = { onSwipeToPin.invoke(article) },
                onNewsItemSelected = onNewsItemSelected
            )
        }
    }
}


@Composable
fun NewsItem(
    article: NewsArticle,
    onSwipeToDelete: () -> Unit,
    onSwipeToPin: () -> Unit,
    onNewsItemSelected: (String) -> Unit


) {
    SwipeableActionsBox(
        startActions = listOf(
            SwipeAction(icon = { Icon(Icons.Default.Delete, contentDescription = "Delete",modifier = Modifier.padding(16.dp)) },
                background = Color.Red,
                onSwipe = { onSwipeToDelete.invoke() }
            )
        ),
        endActions = listOf(
            SwipeAction(icon = { Icon(Icons.Default.Star, contentDescription = "Pin", modifier = Modifier.padding(16.dp)) },
                background = MaterialTheme.colorScheme.tertiary,
                onSwipe = { onSwipeToPin.invoke() }
            )
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
                .padding(6.dp).clickable { article.url?.let { url ->
                    onNewsItemSelected(url)
                } }
        ) {
            Column {
                Image(
                    painter = rememberImagePainter(data = article.urlToImage, builder = {
                        placeholder(R.drawable.ic_launcher_foreground) }),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth()
                ) {
                    article.title?.let { Text(
                        text = it,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                        )
                    }
                }
            }
        }
    }
}