# Tracker for Traccar (Wear OS)

A standalone Wear OS app that sends your watch's location to a [Traccar](https://www.traccar.org)
server. It does not need a companion app on the phone, and the phone can keep running the official
Traccar Client alongside it.

This is an independent project and is not affiliated with or endorsed by Traccar.

## Why it is not a fork

The official Traccar apps are now written in Flutter, and the old native Android repositories are
no longer developed. Instead of forking, this app is built natively for Wear OS on top of the
official [Traccar Client SDK](https://github.com/traccar/traccar-client-sdk)
(`org.traccar:traccar-client-sdk`), the same tracking engine the official app uses. To pick up
upstream improvements, bump `traccarSdk` in `gradle/libs.versions.toml`.

## Features

- Wear OS interface built with Compose for Wear OS (Material 3), in English and Spanish.
- Background tracking through the official SDK: accuracy, minimum distance, interval, offline
  buffering and stop detection.
- A random 8-digit device identifier is generated on first launch, like the official app.
- **Pause near phone:** while the paired phone is connected over Bluetooth, the watch stays silent.
  Once you walk away without it, the watch starts sending positions on its own (ideal for LTE
  watches).
- "Send position now" button.
- Text fields use the system keyboard, with nothing masked, to make typing on the watch less painful.

## Pause near phone

When enabled (the default), the watch only transmits while the phone is **not** nearby.

- "Nearby" means Wear OS reports the phone as `isNearby`, that is, directly connected over
  Bluetooth. On an LTE or Wi-Fi watch the phone can still show up as connected through the cloud;
  that does **not** count as nearby.
- Triggers: Bluetooth connect/disconnect broadcasts (they need the *Nearby devices* permission),
  device boot, app start, and a safety check every 15 minutes.
- When the phone goes away, tracking starts as soon as Wear OS confirms it is gone (it retries
  after one minute if that takes longer). When the phone comes back, tracking stops after
  15 seconds to avoid reacting to Bluetooth glitches.
- While paused, the GPS is **off**, not just the uploads, which saves battery.
- If the permission is denied, the pause still works, only with more delay.

Code: `PhoneProximity`, `TrackingGate`, `GateReceiver`, `GateWorker`.

## Setup

1. On your Traccar server, create a **new device** for the watch (different from your phone) using
   the identifier shown in the app (*Settings › Device identifier*). Sharing the phone's identifier
   would mix both positions into a zig-zag track.
2. On the watch, open *Settings › Server URL* and enter the address of the Traccar client port,
   usually `http://your-server:5055` or `https://your-server:5055`.
3. Press *Start* and grant the location permissions, including "Allow all the time", plus
   *Nearby devices* and notifications.
4. Exempt the app from battery optimization when asked, so Android lets it start the GPS in the
   background when you leave your phone behind.

Plain `http://` is allowed because many self-hosted servers use it.

## Building

Requirements: Android Studio (or JDK 17+), Android Gradle Plugin 9.4 and Android SDK 37.

```sh
./gradlew assembleDebug
```

The APK is generated in `app/build/outputs/apk/debug/`. Install it on a watch or emulator with
`adb install`.

## Project structure

| Path | Purpose |
|---|---|
| `ClientSettings.kt` | Settings stored in DataStore and conversion to the SDK `Config` |
| `TrackerHolder.kt` | Single access point to the SDK `Tracker` |
| `PhoneProximity.kt`, `TrackingGate.kt`, `GateReceiver.kt`, `GateWorker.kt` | Pause near phone |
| `ui/` | Wear OS screens |

## Status and roadmap

Tested on the Wear OS API 34 emulator (UI, permissions, tracking, uploads to a test server).
Pause near phone has not been tested with a real phone/watch pair yet.

- Test on real hardware (pause, battery use, background start on Wear OS 5 and 6).
- Tile or complication with the tracking status.

## License

GNU General Public License v3.0 or later. See [LICENSE](LICENSE). The Traccar Client SDK is
licensed under Apache 2.0, which is compatible with GPLv3.
