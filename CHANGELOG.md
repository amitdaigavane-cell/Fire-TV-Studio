# Changelog

## 1.5.0 — 30 October 2026

- Handle app-initiated Home intents regardless of the caller app, including the YouTube Back-to-Home request observed on Fire TV.
- Watch Home activity-resume events for returns to an existing Home task without a new start intent.
- Preserve launch and Settings bypasses for indirect Home signals. No Back-button remapping, app allowlist, root or disabling of Amazon Home.
- Retain the rendered Home window between app visits, transparent and without input while hidden. Reveal that buffer before refreshing a changed app catalog or artwork.
- Prepare Home while the screen is off and remove the deliberate wake delay. Ignore window events queued before a newer Home request; retry a deferred Home activity request for a bounded interval.
- Run service repair from a resumed Home activity: Fire OS could kill a provider-only process between disabling and re-enabling accessibility, leaving redirection disabled after installation/repair.
- Apply the shared routing changes to Classic, Cinema and Panel without changing their designs.
- Use a translucent window format: an alpha-zero opaque overlay could still obscure a player for Fire OS power policy, suppressing its keep-screen-on request. No global sleep timers are disabled.
- Cinema's idle heading now sits lower above the dock with a time-of-day greeting above it. Focused app names retain their existing caption placement. Custom screensaver work is deferred.
- Document ARMv7 operation on AFTSS, APK architecture, OS minimums and differences from upstream device support. Add a minimum-API check to the computer installer.
- Studio 1.5 embeds TV Home 4.5. See [validation](docs/VALIDATION.md) for checks and remaining limits; this is not a zero-flash guarantee.

## 1.4.0 — 30 October 2026

Studio 1.4 bundles TV Home 4.4. First public preview.

- Restored TV Customize → Themes with Classic, Cinema and Panel, preserving saved profile options and creating an unused preset only when selected.
- Includes the preceding Cinema bold-clock, left-caption and focus-clearance fixes.
- Added prominent upstream attribution, full licenses, device screenshots, a computer installer with reversible cleanup, and phone/computer recovery guides.
- Persistent Home rendering and bounded wake recovery retained; no guarantee that Fire OS can never display stock Home.
