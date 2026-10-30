# Build from source

Requirements: JDK 17+, Android SDK platform 34 and Build Tools 34.0.0, and internet access for the Gradle wrapper and Android Gradle Plugin dependencies. Android Studio provides the SDK manager. Gradle 8.9 / AGP 8.5.2 are pinned in the project.

Set ANDROID_HOME to your SDK, or create `launcher/local.properties` with `sdk.dir=...` for your machine. Do not commit it or any signing key.

From `launcher/`:

```sh
./gradlew :app:assembleDebug :companion:assembleDebug :app:lintDebug :companion:lintDebug
```

Windows:

```powershell
.\gradlew.bat :app:assembleDebug :companion:assembleDebug :app:lintDebug :companion:lintDebug
```

Outputs:

- `app/build/outputs/apk/debug/app-debug.apk`: TV launcher.
- `companion/build/outputs/apk/debug/companion-debug.apk`: phone companion, including that same TV APK under assets/TV-Home.apk.

The companion's bundleLauncher task builds and embeds the TV APK automatically. Sources are native Java for TV, a local HTML/CSS/JavaScript UI in the phone WebView, and a shared Java weather client. No Flutter/React runtime is used by these two apps.

Your local debug key will differ from the maintainer's key, so the result may not update over the published APK. Keep your signing key private and stable for your own distribution. This repository does not contain private signing keys, ADB keys, personal wallpapers, TV backups or IPTV sources.

## Tests

`python -m unittest discover -s tests -p 'test_*.py'` exercises the computer installer's package-state recovery and device/allowlist checks without contacting a TV.

Native UI instrumentation is under `launcher/app/src/androidTest`; run it **only on a disposable emulator**, never a personal TV. It deliberately uses fixture apps and replaces test preferences. The expected viewport is 1920 × 1080 at density 320:

```text
adb -s emulator-5580 shell wm size 1920x1080
adb -s emulator-5580 shell wm density 320
```

Build `:app:assembleDebugAndroidTest`, install app-debug.apk and app-debug-androidTest.apk on that emulator, then run `adb -s emulator-5580 shell am instrument -w com.gathin.tvhome.test/com.gathin.tvhome.HomeVisualTest`.

The standalone Java test sources in tests/ document additional protocol/config/weather/wake-policy cases from development. They require Android's org.json implementation or the Android test environment; they are not part of the Python suite. Current public release checks are listed in [VALIDATION.md](VALIDATION.md).

### Home-return regression checks

These checks use the real HomeSignal parser/policy and need only a JDK; no TV or Android SDK runtime is contacted. From the repository root:

```text
javac -d build/home-signal launcher/app/src/main/java/com/gathin/tvhome/HomeSignal.java tests/HomeSignalTest.java
java -cp build/home-signal com.gathin.tvhome.HomeSignalTest
```

They cover the observed YouTube app-UID Home intent, other app UIDs, exact Home components/categories, existing-task resume events, and Settings/external-launch bypass guards.
