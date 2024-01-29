
package com.example.mynews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.mynews.screens.NewsScreen
import com.example.mynews.screens.SplashScreen
import com.example.mynews.ui.theme.MyNewsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNewsTheme {
                val showSplashScreen = remember { mutableStateOf(true) }
                if (showSplashScreen.value) {
                    SplashScreen(
                        splashScreenTime = 2000,
                        onSplashScreenEnd = { showSplashScreen.value = false }
                    )
                } else { NewsScreen() }
            }
        }
    }
}




