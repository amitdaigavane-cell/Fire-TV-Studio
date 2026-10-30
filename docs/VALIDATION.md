# Validation — Studio 1.5 / TV Home 4.5

30 October 2026. Checks are bounded; they do not establish perfect behavior on every app or firmware.

- Both APKs build; Android Lint reports zero errors.
- Thirty standalone Java Home-return parser/policy checks pass, including ordinary app UIDs, existing-task resume signals, exact Home component matching and Settings/launch bypasses.
- Six computer-helper tests pass, including API-level rejection, explicit device selection, safe quoting, original-state preservation and refusal of core-package/mismatched restore entries.
- Phone frontend checks pass for explicit Apply, independent theme settings, reset, wallpaper guidance, weather choice, cleanup, hidden apps and absence of a remote-control tab; no JS errors or horizontal overflow.
- APK archive inspection confirms no native `lib/` entries in either APK. The physical AFTSS reports `armeabi-v7a,armeabi`, Android API 28, Fire OS build PS7717.5741N. This establishes ARMv7 operation on the test device.
- Both APK signatures match the prior release certificate. The launcher's bytes embedded in Studio match the standalone APK.
- A restart test recovered to TV Home automatically. By the first successful ADB observation, at approximately 30 seconds uptime, boot was complete and our MainActivity was resumed. This is **not** a video verification that stock Home was never visible earlier in boot.
- Service-repair testing exposed the old background-process re-enable race; the final build performs repair from a resumed Home activity.
- On the physical TV, Classic, Cinema and Panel each passed app opening, repeated YouTube Back through its menus to our resumed Home activity, Settings opening, and sleep/wake with the service connected. This complete three-theme pass preceded the final translucent-window and Cinema-caption changes.
- The final window-format change addresses the power-policy problem where an alpha-zero opaque window still obscured the player. It does not change the device's sleep or screensaver timeouts. An extended unattended movie-length test has not been completed.
- Final-build playback check: YouTube's window had `KEEP_SCREEN_ON`, was reported as `mHoldScreenWindow`, and held the WindowManager screen-bright wake lock. `mObscuringWindow` was null rather than our hidden Home surface. Home navigation returned afterward. This verifies the power-policy correction, not every streaming app's own wake-lock behavior.

Return-window logs varied under load. An early retained-window capture showed a 39 ms callback versus 885 ms with removal/recreation, but these are individual log timings, not end-to-end display latency or a performance guarantee. Later checks identified catalog refresh and deferred-activity cases and led to further fixes. Sleep interrupted screen recording on this firmware, so wake checks use resulting activity/service state rather than claiming frame-by-frame wake coverage.

See [COMPATIBILITY.md](COMPATIBILITY.md) for unsupported devices and remaining limitations. Private device logs, configuration backups and streaming-app screenshots are not published.

---

# Earlier validation — Studio 1.4 / TV Home 4.4

30 October 2026. Experimental first public release; tests are bounded, not a guarantee across devices.

- Both Android modules compile; Android Lint reports zero errors.
- The phone frontend checks pass: explicit Apply, edits to inactive themes, reset, wallpaper guidance, city weather, cleanup selection, hidden apps, no remote-control tab; no JavaScript errors or horizontal overflow.
- TV Home 4.4 was installed in place on a physical AFTSS Fire TV Stick HD. The new **Customize → Themes** picker was operated through remote key events to select Classic, Cinema and Panel. All three selections were read back successfully. Neutral/focused screenshots were captured directly from the device.
- The prior active theme, all wallpaper references/options, weather choice, Home name, favorites, hidden apps and Home-selection state were restored and compared after capture. Home service was connected with its persistent window visible.
- The computer helper has local tests for explicit-device targeting/quoting, state restoration, refusal of protected or mismatched backup entries and preservation of original states after repeated cleanup attempts. These tests use fakes, not destructive operations on the TV.
- APK signatures, embedded launcher identity, bundled attribution and absence of wallpaper binaries are checked before upload.

The previous 4.3 emulator check covers complete Cinema toolbar focus bounds, all three native layouts, app focus, visible Power, Customize and empty catalogs. Earlier local tests covered ADB framing/authentication/file transfer, config migration, weather parsing and bounded wake policy. Those older suites were not all rerun for the theme-picker release.

Not established: zero dropped frames, zero Amazon Home flashes under every condition, operation on every Fire TV model, reliable integrated-TV input switching, complete OTA blocking, or a professional security audit. There is no root or firmware modification. Stock Home remains available for Settings and recovery.
