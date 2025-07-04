package com.erhannis.steamosts

import android.app.PendingIntent
import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.erhannis.steamosts.TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE
import com.erhannis.steamosts.ui.theme.SteamOSTsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Command {
    var tmuxCommand: Boolean
    var description: String
    var command: String

    constructor(tmuxCommand: Boolean, description: String, command: String) {
        this.tmuxCommand = tmuxCommand
        this.description = description
        this.command = command
    }
}

val commands: Array<Command> = arrayOf(
    Command(
        tmuxCommand = false,
        description = "Download FEXDroid install script",
        command = "curl -o install https://raw.githubusercontent.com/Erhannis/FEXDroid/refs/heads/fork/target_erhannis/install",
    ),
    Command(
        tmuxCommand = false,
        description = "Run FEXDroid install script",
        command = "chmod +x install && bash install",
    ),
)

class MainActivity : ComponentActivity() {
    companion object {
        private var remoteText = mutableStateOf("")

        @JvmStatic fun get(): String {
            return remoteText.value
        }

        @JvmStatic fun set(v: String) {
            remoteText.value = v
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(arrayOf(TermuxConstants.PERMISSION_RUN_COMMAND), 0)

        lifecycleScope.launch {
            while (isActive) {
                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("y_read.sh"), isReadRequest = true)
                delay(500)
            }
        }

        setContent {
            val remoteText by MainActivity.remoteText
            var localText by remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current
            SteamOSTsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        Row {
                            Button(onClick = {
                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", "curl -o install https://raw.githubusercontent.com/Erhannis/FEXDroid/refs/heads/fork/target_erhannis/install"))
                            }) {
                                Text(text = "download install")
                            }
                            Button(onClick = {
                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", "chmod +x install && bash install"))
                            }) {
                                Text(text = "run install")
                            }
                            Button(onClick = {
                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", """mkfifo pipe0 ; mkfifo pipe1 ; mkfifo pipe2 ; touch y_read.sh ; chmod +x y_read.sh ; echo ${'$'}'#!/bin/bash\n\nSESSION_NAME="x_session"\nLINES_TO_READ=16\n\ntmux capture-pane -p -S -${'$'}LINES_TO_READ -t "${'$'}SESSION_NAME"' > y_read.sh ; touch y_write.sh ; chmod +x y_write.sh ; echo ${'$'}'#!/bin/bash\n\nSESSION_NAME="x_session"\nINPUT="${'$'}1"\n\ntmux send-keys -t "${'$'}SESSION_NAME" "${'$'}INPUT" Enter' > y_write.sh ; touch z.sh ; chmod +x z.sh ; echo ${'$'}'#!/bin/bash\n\nSESSION_NAME="x_session"\n\ntmux new-session -d -y 15 -s "${'$'}SESSION_NAME" "bash"' > z.sh"""))
                            }) {
                                Text(text = "make scripts")
                            }
                        }
                        Row {
                            Button(onClick = {
                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("z.sh"))
                            }) {
                                Text(text = "start bash")
                            }
                            Button(onClick = {
                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("tmux kill-session -t x_session"))
                            }) {
                                Text(text = "kill bash")
                            }
                        }
                        Button(onClick = {
                            cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("y_read.sh"), isReadRequest = true)
                        }) {
                            Text(text = "y_read")
                        }
                        Row {
                            BasicTextField(
                                value = localText,
                                onValueChange = { localText = it },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done // or Search, Go, etc.
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        y_write(localText)
                                        localText = ""
                                        // focusManager.clearFocus() // optionally dismiss the keyboard
                                    }
                                ),
                            )
                            Button(onClick = {
                                y_write(localText)
                                localText = ""
                            }) {
                                Text(text = "==>")
                            }
                        }
                        Text(text = remoteText)
                    }
                }
            }
        }
    }

    fun y_write(cmd: String) {
        var hex: String = cmd
            .map { c ->
                String.format("\\x%02X", c.code)
            }
            .joinToString("")
        cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", """bash y_write.sh ${'$'}'""" + hex + "'"))
    }

    fun cmd(path: String, args: Array<String>, stdin: String = "", isReadRequest: Boolean = false) {
        val LOG_TAG = "MainActivity"

        Log.d(LOG_TAG, "-->test")
        val intent = Intent()
        intent.setClassName(TermuxConstants.TERMUX_PACKAGE_NAME, TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME)
        intent.action = RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, path)
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, args)
//        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_WORKDIR, "/data/data/com.termux/files/home")
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, true)
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_SESSION_ACTION, TermuxConstants.TERMUX_APP.TERMUX_SERVICE.VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY)
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_LABEL, "top command")
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_DESCRIPTION, "Runs the top command to show processes using the most resources.")
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_STDIN, stdin)

// Create the intent for the IntentService class that should be sent the result by TermuxService

// Create the intent for the IntentService class that should be sent the result by TermuxService
        val pluginResultsServiceIntent = Intent(this@MainActivity, PluginResultsService::class.java)

// Generate a unique execution id for this execution command

// Generate a unique execution id for this execution command
        val executionId: Int = PluginResultsService.getNextExecutionId()

// Optional put an extra that uniquely identifies the command internally for your app.
// This can be an Intent extra as well with more extras instead of just an int.

// Optional put an extra that uniquely identifies the command internally for your app.
// This can be an Intent extra as well with more extras instead of just an int.
        pluginResultsServiceIntent.putExtra(PluginResultsService.EXTRA_EXECUTION_ID, executionId)

        //CHECK Hacky
        pluginResultsServiceIntent.putExtra(PluginResultsService.EXTRA_IS_READ_REQUEST, isReadRequest)

// Create the PendingIntent that will be used by TermuxService to send result of
// commands back to the IntentService
// Note that the requestCode (currently executionId) must be unique for each pending
// intent, even if extras are different, otherwise only the result of only the first
// execution will be returned since pending intent will be cancelled by android
// after the first result has been sent back via the pending intent and termux
// will not be able to send more.

// Create the PendingIntent that will be used by TermuxService to send result of
// commands back to the IntentService
// Note that the requestCode (currently executionId) must be unique for each pending
// intent, even if extras are different, otherwise only the result of only the first
// execution will be returned since pending intent will be cancelled by android
// after the first result has been sent back via the pending intent and termux
// will not be able to send more.
        val pendingIntent = PendingIntent.getService(
            this@MainActivity, executionId,
            pluginResultsServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT, pendingIntent)

        try {
            // Send command intent for execution
            Log.d(LOG_TAG, "Sending execution command with id $executionId")
            startService(intent)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to start execution command with id " + executionId + ": " + e.message)
        }

        Log.d(LOG_TAG, "<--test")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SteamOSTsTheme {
        Greeting("Android")
    }
}