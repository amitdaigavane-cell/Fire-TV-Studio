# Planned work

## 2.0: remote button mapping

Limit this to the Live TV / Guide button opening one selected installed app. The phone app offers the app selector and a Default behavior option. Do not add general remapping, settings shortcuts, long-press actions or overrides for Home, Back, navigation, voice, volume or power. Off by default. Fire OS must pass the key to accessibility; physical-device validation is required before release.

## 2.0: dedicated welcome screen

Replace the plain startup Home label with a separate, polished welcome composition: a lightweight background, a small visual mark, and only the welcome text “Welcome back.” Keep it independent of the customizable Home name. No animation or artificial startup delay is required. Reuse the existing startup-cover timing and dismissal; do not alter Home, sleep or wake policy.

## Deferred ideas

- Optional music/lyrics screensaver. Revisit only after core reliability and memory cost are established.
- A separately profiled virtualized app library for unusually large catalogs.

The playback widget and PiP support are not included. These notes are plans, not announced release features or delivery dates.

## Development status

The 2.0 implementation is available as a prerelease for device testing. Live TV key handling and a separate welcome composition are implemented for testing. Startup cover timing/dismissal, sleep and wake policies are retained. The welcome screen uses no wallpaper decoding, network calls, timer animations or artificial delay.

## 2.0: full app-library ordering

Arrange non-favorite apps in Classic/Cinema and the full Panel library from the phone or TV app menu. Share library order across designs; keep favorites independent. New apps append alphabetically. Offer an alphabetical reset.

Local checks (8 November 2026): both debug APKs build and lint; six Python tests, fourteen Java suites, and the mocked phone UI tests pass. Live TV mapping still needs a physical remote test on Fire OS; the welcome composition and library navigation still need on-TV visual checks. The prerelease is published; no 2.0 build has been installed on the maintainer’s TV during these checks.

## Additional selected 2.0 work

- Per-design wallpaper dimming (0–100%, additional to the existing readability shade).
- Theme-default, wallpaper-matched or manual accent colors. Match the design’s first wallpaper once, cache its color, and retain it during rotation.
- Theme-default, subtle zoom, outline-only or instant focus appearance; the global animation switch remains respected.
- Six recent apps, based on existing foreground-window events. No extra monitoring service or polling. Back then Menu from neutral Home opens the tray; Customize also contains Recent apps. Hidden/uninstalled apps are omitted. Disable and clear history from phone Apps or clear from the TV tray.

Additional-feature checks: debug builds/lint, six Python tests, fifteen Java suites and mocked phone UI checks pass. Dimming, accent modes, focus modes/reset and recent-history opt-out are covered. Physical TV visual/navigation checks remain pending.
