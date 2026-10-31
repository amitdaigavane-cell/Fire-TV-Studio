# TV Home Studio 1.7 + TV Home 4.7

**Most users: [Download the PHONE app](https://github.com/sreeego/tv-home-studio/releases/download/v1.7.0/PHONE-TV-Home-Studio-1.7.apk)** and install it on your Android phone. It includes and installs the TV launcher over Wi-Fi; you do not need both APKs.

For manual computer installation only: [TV-ONLY-Launcher-4.7.apk](https://github.com/sreeego/tv-home-studio/releases/download/v1.7.0/TV-ONLY-Launcher-4.7.apk) goes on Fire TV. [Instructions](https://github.com/sreeego/tv-home-studio/blob/main/docs/INSTALL.md#with-a-computer).

**Completely vibe coded with Codex.** Credits: [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv), [itsmeterrylin/fire-tv-cleanup](https://github.com/itsmeterrylin/fire-tv-cleanup), [udhaykumarbala/cleanyourtv-app](https://github.com/udhaykumarbala/cleanyourtv-app). Original notices and AdbLib attribution remain included.

- Wallpaper screensaver with a bold clock, weekday, date and selected-city weather. Weather follows the saved Off/city and theme settings. No bundled wallpaper or automatic location lookup.
- Same Home-only behavior: screensaver after 2 idle minutes, sleep at 15; the first key dismisses without launching an app. Other apps’ idle behavior and Fire OS timeout settings are unchanged.
- Reuses the wallpaper bitmap and weather cache. Text updates and shifts slightly once per minute, without continuous animation. Bright or static wallpapers are not burn-in protection.
- Earlier startup cover: start Home detection before artwork loading and cover a new boot while the full launcher loads. Late boot broadcasts no longer force Home over an app when our service is connected.
- Preserves the hidden-window surface release from 1.6, all three layout designs, Settings and manual Sleep.

Amazon Home can still appear before Fire OS connects our service or if permissions/services fail. It remains enabled for Settings and recovery. See [validation](https://github.com/sreeego/tv-home-studio/blob/main/docs/VALIDATION.md) and [compatibility](https://github.com/sreeego/tv-home-studio/blob/main/docs/COMPATIBILITY.md).

Also included: source ZIP and SHA256SUMS.txt. Both APKs contain no native ABI libraries; ARMv7 was tested on AFTSS. Phone requires Android 9+, launcher API 26+. Fire OS 5/6 and Vega are unsupported.
