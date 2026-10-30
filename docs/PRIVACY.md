# Privacy and permissions

No project accounts, ads, analytics or telemetry uploads are implemented.

- The phone uses authenticated ADB over your local network. Its authorization key, TV address and restore ledger stay in app-private storage.
- Exported restore files include a device identifier or fingerprint. Keep them private; they are not included in this repository.
- User-selected photos are resized and sent to the connected TV. No wallpaper is bundled, and this project does not upload your photos to a hosted service.
- Weather is off until you select a city. City search and forecasts contact Open-Meteo, which receives the search/coordinates and the request's network address. There is no automatic location lookup or GPS permission.
- The launcher's accessibility service observes foreground-window changes for Home routing and provides its persistent Home window. WRITE_SECURE_SETTINGS allows enabling/disabling its own service without erasing unrelated services.
- READ_LOGS is granted through ADB so a local filtered watcher can recognize Home transitions earlier. The launcher does not store or upload log streams.
- Amazon's own apps and your installed streaming apps have their own privacy practices; hiding an icon does not disable that app.

This first public release is a sideloaded development build, with source and checksums supplied. Its APKs are signed with the maintainer's existing development certificate to preserve updates for earlier builds. The private key is not in the repository. Independent builds use the builder's own debug key unless they configure release signing.
