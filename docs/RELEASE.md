# TV Home Studio 1.5 + TV Home 4.5

## Which APK should I download?

**Most users: [Download the PHONE app](https://github.com/sreeego/tv-home-studio/releases/download/v1.5.0/PHONE-TV-Home-Studio-1.5.apk)** and install it on your **Android phone**. It already includes the TV launcher and installs it onto your Fire TV over Wi-Fi. **You only need this one download.**

**Using a computer instead?** Download [TV-ONLY-Launcher-4.5.apk](https://github.com/sreeego/tv-home-studio/releases/download/v1.5.0/TV-ONLY-Launcher-4.5.apk) and follow the [computer installation guide](https://github.com/sreeego/tv-home-studio/blob/main/docs/INSTALL.md#with-a-computer). This APK goes on the **Fire TV**, not your phone. It is the launcher only, without the phone cleanup/control app.

The different version numbers belong to the phone app and bundled TV launcher; they are not two editions to choose between. Download names were clarified; the APK binaries are unchanged.

Home-return fixes for all three native designs, with the updated launcher bundled in the Android companion. **Completely vibe coded with Codex.**

Credits: [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv), [itsmeterrylin/fire-tv-cleanup](https://github.com/itsmeterrylin/fire-tv-cleanup), and [udhaykumarbala/cleanyourtv-app](https://github.com/udhaykumarbala/cleanyourtv-app). Original notices and AdbLib attribution are included.

- Restore **Customize → Themes** on the TV: Classic, Cinema and Panel, preserving each profile's wallpaper/options.
- Cinema retains its centered bold clock and left-aligned app name/hint above the dock.
- Catch Home requests from ordinary apps, including YouTube Back, and existing Home-task resumes. No per-app allowlist or Back-button remapping.
- Keep the rendered Home window ready while apps are open, transparent and unable to receive input, instead of rebuilding it on every return. Prepare Home during screen-off and remove the deliberate wake delay.
- Fix service repair losing its re-enable callback when Fire OS kills the background process. Repair now runs from the foreground Home activity. Ignore stale window events and retry a deferred Home activity request for a bounded interval.
- Correct the transparent overlay's window format so it no longer obscures a video player's keep-awake request in Android window policy. Keep existing system sleep settings.
- Add the time-of-day greeting above Cinema's idle heading and lower that heading slightly toward the dock. Custom screensaver work is deferred.
- ARMv7 is confirmed on the tested AFTSS. Both APKs have no bundled native libraries. Added a compatibility matrix comparing this fork with the three references; added the minimum-OS check to the computer helper.
- Phone setup, explicit cleanup, reversible changes, per-theme wallpapers/options, city weather or Off, best-effort OTA restriction/resume.
- Real Fire TV Stick HD screenshots and computer installation instructions included.

## Downloads

- **PHONE-TV-Home-Studio-1.5.apk** → Android phone, Android 9+. Includes the launcher.
- **TV-ONLY-Launcher-4.5.apk** → Fire TV only; optional download for manual installation from a computer.
- **tv-home-studio-source-1.5.zip** → full source, docs, licenses and computer helper.
- **SHA256SUMS.txt** → verify downloads.

[Installation and recovery](https://github.com/sreeego/tv-home-studio/blob/main/docs/INSTALL.md) · [Validation](https://github.com/sreeego/tv-home-studio/blob/main/docs/VALIDATION.md) · [Credits](https://github.com/sreeego/tv-home-studio/blob/main/docs/CREDITS.md)

This is a sideloaded community development build, signed consistently with prior releases. No wallpaper, signing key or personal TV backup is included. Tested on AFTSS / Fire OS 7.7.1.7 / ARMv7. Other firmware is unverified; Fire OS 5/6 and Vega OS are unsupported. Stock Home stays installed for Settings. No guarantee of zero stock-Home frames, particularly before our service starts at cold boot. Updates may remain possible when Fire OS protects an updater. See the [compatibility details](https://github.com/sreeego/tv-home-studio/blob/main/docs/COMPATIBILITY.md).
