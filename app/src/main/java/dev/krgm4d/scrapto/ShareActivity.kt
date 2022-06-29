package dev.krgm4d.scrapto

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.Uri.encode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()
}

class ShareActivity : ComponentActivity() {

    companion object {
        const val TAG = "ShareActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = true
                )
            }

            MaterialTheme {
                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.1f))
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.size(48.dp)
                    )
                }
            }
        }

        val sharePreferences = getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
        val scrapboxName = sharePreferences.getString(MainActivity.scrapboxKey, "") ?: ""

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                handleText(scrapboxName, intent, this)
            }
            else -> {}
        }

    }

    private fun handleText(scrapboxName: String, intent: Intent, activity: Activity) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { urlString ->
            GlobalScope.launch {
                when (val document = fetchHtmlTitle(urlString)) {
                    is Result.Success -> {
                        processWithDocument(scrapboxName, urlString, document.data)
                        activity.finish()
                    }
                    is Result.Failure -> {
                        openScrapbox(scrapboxName, urlString, urlString)
                        activity.finish()
                    }
                }
            }
        }
    }

    private fun processWithDocument(scrapboxName: String, url: String, document: Document) {
        val title = document.title().trim()
        openScrapbox(scrapboxName, url, title)
    }

    private fun openScrapbox(scrapboxName: String, url: String, title: String) {
        val link = "[${url} ${title}]"

        val body = listOf("", link).joinToString("\n")
        val openUrl = "https://scrapbox.io/${scrapboxName}/${encode(title)}?body=${body}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(openUrl))
        startActivity(intent)
    }
}

private suspend fun fetchHtmlTitle(
    url: String,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Result<Document> {
    return withContext(dispatcher) {
        val document = kotlin.runCatching {
            @Suppress("BlockingMethodInNonBlockingContext")
            Jsoup.connect(url).timeout(30000).get()
        }.fold(
            onSuccess = { Result.Success(data = it) }, onFailure = { Result.Failure(error = it) }
        )

        document
    }
}