package dev.krgm4d.scrapto

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainView()
        }
    }
}

private fun saveScrapboxName(input: String) {
    Log.d("MainActivity", input)
}

@Composable
private fun MainView() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Scrap To") })
            }
        ) {
            Column {
                InputRow()
            }
        }
    }
}

@Composable
private fun InputRow() {
    var text by remember { mutableStateOf("") }

    Row {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it }
        )
        OutlinedButton(
            onClick = { saveScrapboxName(text) }
        ) {
            Text("SAVE")
        }
    }
}