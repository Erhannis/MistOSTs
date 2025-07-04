Unofficial app to download music (OSTs) from your Steam account.
Mechanism:
Termux is a terminal emulator.
  https://github.com/termux/termux-app
In Termux, install FEXDroid, which lets you run x86/64 code.
  https://github.com/gamextra4u/FEXDroid
(FEXDroid btw installs Ubuntu.  If you get errors, some links might need updating.)
  See https://rootfs.fex-emu.gg/RootFS_links.json
In FEXDroid, install linux SteamCMD, the official Steam command line client.
  https://developer.valvesoftware.com/wiki/SteamCMD
Login to Steam via SteamCMD (and confirm login via the Steam app).
  Warning: we're doing a lot of this via tmux, which means any other program on the phone that has termux permissions could (theoretically) send commands to our running session and steal your steam credentials or something.
  It probably WON'T happen, but it MIGHT.
Have SteamCMD list music packages.
Have SteamCMD download the ones you want.

Prep:
Install Termux from F-Droid
  f-droid.org
  search termux
Open termux
  nano .termux/termux.properties
  uncomment "allow-external-apps = true" to permit other apps (like SteamOSTs) to request/use permissions to have Termux run commands
Grant termux "display over other apps" permission (possibly optional)
Grant termux files permissions etc, probably
Disable battery optimizations for Termux
  https://dontkillmyapp.com/


Released MIT license.  Termux, FEXDroid, FEX-Emu, Ubuntu, SteamCMD, Steam, and whatever you download are property of their respective owners, and I am not affiliated with any of the properties named.
-Erhannis