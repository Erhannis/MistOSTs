package com.erhannis.mistosts

import android.app.PendingIntent
import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.erhannis.mistosts.TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE
import com.erhannis.mistosts.ui.theme.MistOSTsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class Command(var tmuxCommand: Boolean, var description: String, var command: String, var specialTags: String = "")

data class Soundtrack(var id: String, var name: String)

//RAINY Would be nice to not dump all the scripts in the root directory

val introCommands: Array<Command> = arrayOf(
    Command(
        tmuxCommand = false,
        description = "This is a (very rough, slow) program to download OSTs from your Steam account to your phone.  Commands show up in this list, in the order you should run them, and tabs across the top group them into ordered sections.  There's some installation steps after the warnings.  Hit the v button.  (\"Run\" runs any command shown here, as written (minus any indicative \"[tmux]\").)",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Warning 1: Your Steam credentials will be sent to a tmux session running in Termux.  MistOSTs, Termux, and any program with the com.termux.permission.RUN_COMMAND permission or similar capability could theoretically steal your Steam credentials.  I don't think this will happen, but be aware.  For my part I promise not to steal your Steam credentials, but understand that if you want to be sure I'm not doing that (or anything else illicit with Termux), you'll have to examine and compile the source code yourself.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Warning 2: This program is VERY rough.  It basically throws text commands towards Termux (with a lot of extra steps) and hopes everything works.  I don't BELIEVE it has much chance of damaging any of your data, with the possible exception of anything in /storage/emulated/0/Music/Steam , but since you're granting Termux storage permissions and I'm running scripts somewhat blind, I'm not totally confident it couldn't somehow delete all your cat pictures.  I again don't THINK that will happen, but if you're concerned about it, you could NOT give termux storage permissions, skip the \"Link steam music folder\" step, and manually copy OSTs out of the ubuntu's /home/steam/Steam/steamapps/music yourself.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Warning 3: If you personally use Termux a lot, note that this process litters scripts in your home directory, AND will delete and replace any proot-distro ubuntu you currently have.  Sorry.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "The source code for the app can be found at https://github.com/Erhannis/MistOSTs , and the scripts can be found at https://github.com/Erhannis/FEXDroid/tree/fork/mistosts .",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Termux, FEXDroid, FEX-Emu, Ubuntu, SteamCMD, Steam, and whatever you download are property of their respective owners, and I am not affiliated with any of the properties named.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "All the commands should give some indication when they're done or ready for input - a \">\" or \"$\" or \"#\" prompt, or a request for confirmation.  Some may take a few minutes to finish, but I don't expect anything to stop moving entirely for more than a few minutes.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Understand that the only response MistOSTs parses is the soundtrack IDs and names - everything else, you're the one that decides when a command has failed, and how to fix it.  Good luck.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "(Tip: sometimes SteamCMD is SUPER slow preallocating or downloading.  Sometimes I get fatal errors abt \"appears to have stalled\".  Try killing Termux and trying again; might help.  Maybe reboot your phone even.  Sometimes though, I just let it run and after like 10 minutes it starts going fast again???)",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Step 1: Install Termux from F-Droid ( f-droid.org , download and install and open F-Droid, search Termux. )  You may need to enable installation sources or st; check f-droid's instructions.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Step 2: Open Termux.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Step 3: nano .termux/termux.properties\nUncomment \"allow-external-apps = true\" to permit other apps (like MistOSTs) to request/use permissions to have Termux run commands.\n\nctrl-o to save, ctrl-x to exit nano.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Step 4: Grant Termux relevant permissions.\n1. Grant termux \"display over other apps\" permission. (Possibly optional, not sure.)\n2. Grant termux files permissions etc.  (Sortof optional; see warning 2.)\n3. Disable battery optimizations for Termux.  See https://dontkillmyapp.com if needed.  (Also optional, but you may encounter problems like the scripts getting killed in the middle.)",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Step 5: Grant MistOSTs the \"Run commands in Termux environment\" permission.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Click the \"Setup\" tab at the top.",
        command = "",
    ),
)

val setupCommands: Array<Command> = arrayOf(
    Command(
        tmuxCommand = false,
        description = "The large box above (may not yet be visible) shows console output; it scrolls.  \"Long read\" extends it back like 200 lines.  Note this may be slower; it's fetching the whole contents twice a second.\n\nThe small box just below it lets you send keypresses to the tmux session, once that's started (command 3, here).\n\nThis box here shows a description, and a command.  Clicking \"Run\" runs the command and advances; the up and down arrows on the right move forwards or backwards in the list.",
        command = "",
    ),
    Command(
        tmuxCommand = false,
        description = "Download and run preinstall script; then please wait for maybe 30 seconds.  (Click \"Run\" now.)",
        command = "curl -o preinstall.sh https://raw.githubusercontent.com/Erhannis/FEXDroid/refs/heads/fork/mistosts/preinstall.sh && chmod +x preinstall.sh && bash preinstall.sh",
    ),
    Command( // Recurring
        tmuxCommand = false,
        description = "Start tmux session - this needs to be running before any subsequent command, on any run of this program.  It enables you to communicate with a persistent bash session.",
        command = "bash start_session.sh",
    ),
    Command(
        tmuxCommand = true,
        description = "Install fexdroid stuff",
        command = "bash install",
    ),
    Command(
        tmuxCommand = true,
        description = "Say \"yes\" to installing ubuntu over whatever was there",
        command = "y",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "Start fexdroid",
        command = "fexdroid",
    ),
    Command(
        tmuxCommand = true,
        description = "Run rootfs to install some stuff",
        command = "./rootfs",
    ),
    Command(
        tmuxCommand = true,
        description = "\"yes\" to install more stuff",
        command = "y",
    ),
    Command(
        tmuxCommand = true,
        description = "Create user \"steam\", move some files around, and download the SteamCMD",
        command = "./setup_steam.sh",
    ),
    Command(
        tmuxCommand = true,
        description = "Link steam music folder to your own.  Run your own command if you want it to point somewhere other than /storage/emulated/0/Music/Steam .  You might get errors if the existing Steam music dir is not empty, or if Termux doesn't have the right file permissions.",
        command = "( cd /home/steam/Steam/steamapps && mkdir -p /storage/emulated/0/Music/Steam && ( rmdir music ; rm -f music ) && ln -s /storage/emulated/0/Music/Steam music && echo success ) ; cd /root",
    ),
    Command(
        tmuxCommand = true,
        description = "Setup is done; move to tab Main at top, step 3.  On future runs you can skip straight to Main, step 1.",
        command = "",
    ),
)

val mainCommands: Array<Command> = arrayOf(
    Command( // Recurring
        tmuxCommand = false,
        description = "Start tmux session - this needs to be running before any subsequent command, on any run of this program.  It enables you to communicate with a persistent bash session.",
        command = "bash start_session.sh",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "Start fexdroid",
        command = "fexdroid",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "Start SteamCMD",
        command = "./start_steam.sh",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "You'll have to type for this one.  Type \"login <USERNAME>\" above, no quotes, hit enter.  Type in your password if prompted, hit enter.",
        command = "",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "Go confirm the login via Steam Guard if prompted.",
        command = "",
    ),
    Command( // Recurring
        tmuxCommand = true,
        description = "List soundtracks.  You may need to enable long read at the top.",
        command = "app_info_find common/type Music",
        specialTags = "soundtracks",
    ),
    Command( // Recurring
        tmuxCommand = false,
        description = "Wait until the soundtrack list has been retrieved, then go to the OSTs tab and click GET on one you want to download.  If it seems like they're not all there, check \"Long read\" for a second so MistOSTs definitely sees the whole log with all the soundtracks listed, to parse them out.",
        command = "",
    ),
    Command( // Recurring
        tmuxCommand = false,
        description = "Forcibly stop tmux session.  PREFERABLY skip this one and repeatedly use the `exit` command next in the list.",
        command = "tmux kill-session -t x_session",
    ),
    Command( // Recurring
        tmuxCommand = false,
        description = "The `exit` command works for most shells and consoles.",
        command = "exit",
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

    /*
    Tabs:
    0 - Intro
    1 - Setup
    2 - Main
    3 - OST Selection
     */
    private var _tab = mutableStateOf(0)

    private var _currentIntroCommand = mutableStateOf(0)
    private var _currentSetupCommand = mutableStateOf(0)
    private var _currentMainCommand = mutableStateOf(0)

    private var _longRead = mutableStateOf(false)

    private val _soundtracks = mutableStateMapOf<String, Soundtrack>()

    private fun parseSoundtracks(s: String) {
        var input = s.replace(Regex("[\n\r]"), "")
        val regex = Regex("""AppID (\d+) "(.*?)" : "Music"""")
        val matches = regex.findAll(input)
        for (match in matches) {
            val id = match.groupValues[1]
            val name = match.groupValues[2]
            // println("Found AppID: $id, Name: $name")
            val soundtrack = Soundtrack(id, name)
            _soundtracks[id] = soundtrack
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(arrayOf(TermuxConstants.PERMISSION_RUN_COMMAND), 0)

        lifecycleScope.launch {
            while (isActive) {
                //DUMMY Check if permission
                if (_tab.value == 1 || _tab.value == 2) {
                    val script = if (_longRead.value) "long_read_session.sh" else "read_session.sh";
                    cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf(script), isReadRequest = true)
                } else {
                    remoteText.value = ""
                }
                parseSoundtracks(remoteText.value)
                delay(500)
            }
        }

        setContent {
            val soundtracks = remember { _soundtracks }
            val remoteText by MainActivity.remoteText
            var localText by remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current
            // var currentSetupCommand by remember { mutableStateOf(0) }
            // var currentMainCommand by remember { mutableStateOf(0) }
            var longRead by _longRead
            var tab by _tab
            val commands = when (tab) {
                0 -> introCommands
                1 -> setupCommands
                2 -> mainCommands
                3 -> arrayOf()
                else -> arrayOf()
            }
            var currentCommand by when (tab) {
                0 -> _currentIntroCommand
                1 -> _currentSetupCommand
                2 -> _currentMainCommand
                3 -> remember { mutableStateOf(0) }
                else -> remember { mutableStateOf(0) }
            }
            MistOSTsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        Row {
                            Button(
                                enabled = (tab != 0),
                                onClick = {
                                    tab = 0
                                },
                            ) {
                                Text("Intro")
                            }
                            Button(
                                enabled = (tab != 1),
                                onClick = {
                                    tab = 1
                                },
                            ) {
                                Text("Setup")
                            }
                            Button(
                                enabled = (tab != 2),
                                onClick = {
                                    tab = 2
                                },
                            ) {
                                Text("Main")
                            }
                            Button(
                                enabled = (tab != 3),
                                onClick = {
                                    tab = 3
                                },
                            ) {
                                Text("OSTs")
                            }
                        }
                        if (tab == 3) {
                            // OSTs
                            Text("This page only works while you are logged into Steam on the Main tab, and after listing soundtracks.  Wait until one has downloaded before starting the next.")
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                for (soundtrack in soundtracks.values.sortedBy { "${it.name} ${it.id}" }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(onClick = {
                                            write_session("app_update ${soundtrack.id}")
                                            tab = 2
                                        }) {
                                            Text("GET")
                                        }
                                        Text(soundtrack.id)
                                        Text(" - ")
                                        Text(soundtrack.name)
                                    }
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = longRead, onCheckedChange = { longRead = it })
                                Text("Long read")
                            }
                            Text(
                                text = remoteText,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState()),
                            )
                            Row {
                                BasicTextField(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline)
                                        .padding(4.dp),
                                    value = localText,
                                    onValueChange = { localText = it },
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Go
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            write_session(localText)
                                            localText = ""
                                            // focusManager.clearFocus() // optionally dismiss the keyboard
                                        }
                                    ),
                                )
                                Button(onClick = {
                                    write_session(localText)
                                    localText = ""
                                }) {
                                    Text(text = "==>")
                                }
                            }
                            Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)) { // Command tile
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, MaterialTheme.colorScheme.outline)
                                            .verticalScroll(rememberScrollState()),
                                    ) {
                                        Row {
                                            Text("" + (currentCommand + 1) + "/" + commands.size + "  ")
                                            if (currentCommand < commands.size) {
                                                SelectionContainer { Text(commands[currentCommand].description) }
                                            } else {
                                                SelectionContainer { Text("ERROR") }
                                            }
                                        }
                                        // Text("-")
                                        if (currentCommand < commands.size) {
                                            if (commands[currentCommand].tmuxCommand) {
                                                SelectionContainer { Text("[tmux] " + commands[currentCommand].command) }
                                            } else {
                                                SelectionContainer { Text(commands[currentCommand].command) }
                                            }
                                            Row {
                                                // Checkbox(checked = false, onCheckedChange = {}) //RAINY
                                                Button(onClick = {
                                                    if (commands[currentCommand].tmuxCommand) {
                                                        write_session(commands[currentCommand].command)
                                                        if (commands[currentCommand].specialTags.contains("soundtracks")) {
                                                            // Queue up a couple long reads
                                                            lifecycleScope.launch { // I don't know whose lifecycle this is
                                                                delay(2000)
                                                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("long_read_session.sh"), isReadRequest = true)
                                                                delay(13000)
                                                                cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("long_read_session.sh"), isReadRequest = true)
                                                            }
                                                        }
                                                    } else {
                                                        cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", commands[currentCommand].command))
                                                    }
                                                    // Move to next command
                                                    if (currentCommand < commands.size - 1) {
                                                        currentCommand++
                                                    }
                                                }) {
                                                    Text("Run")
                                                }
                                            }
                                        } else {
                                            Text("ERROR")
                                        }
                                    }
                                    Column {
                                        Button(
                                            onClick = {
                                                if (currentCommand > 0) {
                                                    currentCommand--
                                                }
                                            },
                                            enabled = (currentCommand > 0),
                                        ) {
                                            Text("^")
                                        }
                                        Button(
                                            onClick = {
                                                if (currentCommand < commands.size - 1) {
                                                    currentCommand++
                                                }
                                            },
                                            enabled = (currentCommand < commands.size - 1),
                                        ) {
                                            Text("v")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun write_session(cmd: String) {
        var hex: String = cmd
            .map { c ->
                String.format("\\x%02X", c.code)
            }
            .joinToString("")
        cmd("/data/data/com.termux/files/usr/bin/bash", arrayOf("-c", """bash write_session.sh ${'$'}'""" + hex + "'"))
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
    MistOSTsTheme {
        Greeting("Android")
    }
}