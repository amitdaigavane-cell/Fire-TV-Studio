# TV Home Studio 1.6 + TV Home 4.6

**Most users: [Download the PHONE app](https://github.com/sreeego/tv-home-studio/releases/download/v1.6.0/PHONE-TV-Home-Studio-1.6.apk)** and install it on your Android phone. It includes and installs the TV launcher over Wi-Fi; you do not need both APKs.

For manual computer installation only: [TV-ONLY-Launcher-4.6.apk](https://github.com/sreeego/tv-home-studio/releases/download/v1.6.0/TV-ONLY-Launcher-4.6.apk) goes on the Fire TV. [Instructions](https://github.com/sreeego/tv-home-studio/blob/main/docs/INSTALL.md#with-a-computer).

**Completely vibe coded with Codex.** Credits: [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv), [itsmeterrylin/fire-tv-cleanup](https://github.com/itsmeterrylin/fire-tv-cleanup), [udhaykumarbala/cleanyourtv-app](https://github.com/udhaykumarbala/cleanyourtv-app). Original notices and AdbLib attribution remain included.

- The launcher no longer retains a continuously rendered transparent full-screen window behind apps. Its view hierarchy is reused, but the hidden rendering surface is released.
- The duplicate activity UI is hidden while the service owns Home. Visible Home is opaque, avoiding drawing the screen behind it.
- Optional Home-only idle screen: dim moving clock after 2 idle minutes; sleep after 15. First key dismisses it without selecting an app. Manual Sleep works.
- Enable or disable it in TV Customize → Home idle screen or phone Themes → Home idle screen. Enabled by default. Other apps keep their own playback/idle behavior; global Fire OS timeouts are unchanged.
- Includes 1.5 Home-return and foreground service-repair fixes. All three layouts remain available.

The black background and dim moving clock reduce static bright imagery but cannot guarantee OLED/AMOLED burn-in prevention. No claim that stock Home can never appear during cold boot or service failure. Amazon Home stays enabled for Settings and recovery. See [validation](https://github.com/sreeego/tv-home-studio/blob/main/docs/VALIDATION.md) and [compatibility](https://github.com/sreeego/tv-home-studio/blob/main/docs/COMPATIBILITY.md).

Other downloads: source ZIP and SHA256SUMS.txt. ARMv7 was tested on AFTSS; no architecture-specific APK is needed. Phone requires Android 9+, launcher API 26+. Vega and Fire OS 5/6 are unsupported.
