package dev.krgm4d.scrapto

import android.content.Intent
import android.net.Uri
import android.net.Uri.encode
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()
}

class ShareActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ShareActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                handleText(intent)
            }
            else -> {}
        }

    }

    private fun handleText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { urlString ->
            GlobalScope.launch {
                when (val document = fetchHtmlTitle(urlString)) {
                    is Result.Success -> {
                        processWithDocument(urlString, document.data)
                    }
                    is Result.Failure -> {
                        openScrapbox(urlString, urlString)
                    }
                }
            }
        }
    }

    private fun processWithDocument(url: String, document: Document) {
        val title = document.title().trim()
        openScrapbox(url, title)
    }

    private fun openScrapbox(url: String, title: String) {
        val link = "[${url} ${title}]"

        val body = listOf("", link).joinToString("\n")
        val openUrl = "https://scrapbox.io/kurogoma4d-lab/${encode(title)}?body=${body}"

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