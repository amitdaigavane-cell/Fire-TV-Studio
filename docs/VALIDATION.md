# Validation — Studio 1.4 / TV Home 4.4

30 October 2026. Experimental first public release; tests are bounded, not a guarantee across devices.

- Both Android modules compile; Android Lint reports zero errors.
- The phone frontend checks pass: explicit Apply, edits to inactive themes, reset, wallpaper guidance, city weather, cleanup selection, hidden apps, no remote-control tab; no JavaScript errors or horizontal overflow.
- TV Home 4.4 was installed in place on a physical AFTSS Fire TV Stick HD. The new **Customize → Themes** picker was operated through remote key events to select Classic, Cinema and Panel. All three selections were read back successfully. Neutral/focused screenshots were captured directly from the device.
- The prior active theme, all wallpaper references/options, weather choice, Home name, favorites, hidden apps and Home-selection state were restored and compared after capture. Home service was connected with its persistent window visible.
- The computer helper has local tests for explicit-device targeting/quoting, state restoration, refusal of protected or mismatched backup entries and preservation of original states after repeated cleanup attempts. These tests use fakes, not destructive operations on the TV.
- APK signatures, embedded launcher identity, bundled attribution and absence of wallpaper binaries are checked before upload.

The previous 4.3 emulator check covers complete Cinema toolbar focus bounds, all three native layouts, app focus, visible Power, Customize and empty catalogs. Earlier local tests covered ADB framing/authentication/file transfer, config migration, weather parsing and bounded wake policy. Those older suites were not all rerun for the theme-picker release.

Not established: zero dropped frames, zero Amazon Home flashes under every condition, operation on every Fire TV model, reliable integrated-TV input switching, complete OTA blocking, or a professional security audit. There is no root or firmware modification. Stock Home remains available for Settings and recovery.
