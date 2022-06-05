package dev.krgm4d.scrapto

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val scrapboxKey = "SCRAPBOX_KEY"
        lateinit var mainSharedPreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainSharedPreferences = getPreferences(Context.MODE_PRIVATE)

        val defaultName = mainSharedPreferences.getString(scrapboxKey, "")

        setContent {
            MainView(defaultName ?: "", mainSharedPreferences)
        }
    }
}

private suspend fun saveScrapboxName(
    input: String,
    sharedPref: SharedPreferences,
    scaffoldState: ScaffoldState
) {
    sharedPref.edit().putString(MainActivity.scrapboxKey, input).apply()

    scaffoldState.snackbarHostState.showSnackbar("Scrapbox name saved.")
}

@Composable
private fun MainView(
    defaultName: String = "",
    sharedPref: SharedPreferences
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val scaffoldState = rememberScaffoldState()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Scrap To") })
            },
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { focusRequester.requestFocus() }
                )
                .focusRequester(focusRequester)
                .focusTarget(),
            scaffoldState = scaffoldState
        ) {
            Column {
                InputRow(defaultName, sharedPref, scaffoldState)
            }
        }
    }
}

@Composable
private fun InputRow(
    defaultName: String = "",
    sharedPref: SharedPreferences,
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    var text by remember { mutableStateOf(defaultName) }
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(8.dp))
        OutlinedTextField(
            value = text,
            label = { Text("Scrapbox name") },
            onValueChange = { text = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        )
        Spacer(Modifier.size(8.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    saveScrapboxName(text, sharedPref, scaffoldState)
                }
            },
            enabled = text.isNotEmpty()
        ) {
            Text("SAVE")
        }
        Spacer(Modifier.size(8.dp))
    }
}

@Preview
@Composable
fun InputRowPreview() {
    InputRow(sharedPref = MockSharedPreferences())
}