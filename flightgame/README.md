# NEON DASH — flight-mode arcade game 🎮✈️

A one-thumb, fully-offline neon arcade game built as an Android **APK**. Perfect
for a 1.5-hour flight: no internet, no accounts, no ads — just "one more run".

## The game

**Gravity-flip endless runner.** You're a glowing cube auto-running through a
neon tunnel.

- **Tap anywhere** (or press Space) to flip gravity — snap between floor and ceiling.
- **Dodge** the pink spikes.
- **Grab gold orbs** to build a **combo multiplier** and pump your score.
- Speed ramps up the longer you survive. It gets fast.
- Your **best score is saved** on the device.

Juice for the addictiveness: particle bursts, screen shake, near-miss flashes,
combo counter, milestone chimes, motion trails, and chiptune WebAudio SFX — all
generated on-device, zero assets to download.

## Install it on your phone

1. Grab **`dist/NeonDash.apk`** from this repo (download to the phone, or copy via USB).
2. On the phone, tap the file. Android will ask to allow installing from this
   source — allow it (Settings ▸ "Install unknown apps" for your browser/files app).
3. Open **Neon Dash** from your app drawer. Turn the volume up. Fly. 🛫

Requires Android 8.0 (Oreo) or newer. The APK is debug-signed, which is why
Android shows the "unknown source" prompt — that's expected for a sideloaded game.

## Rebuild from source

Needs JDK 17+, the Android SDK (build-tools 34, platform android-34), and Gradle 8.x.

```bash
cd flightgame
gradle :app:assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk
```

A GitHub Actions workflow (`.github/workflows/build-apk.yml`) also builds the
APK and uploads it as an artifact on every push.

## Project layout

```
flightgame/
├── app/src/main/assets/index.html   # the whole game (HTML5 canvas + JS, self-contained)
├── app/src/main/java/.../MainActivity.java  # fullscreen WebView host
├── app/src/main/AndroidManifest.xml
├── app/build.gradle                 # minSdk 26, targetSdk 34
└── dist/NeonDash.apk                # prebuilt, ready to sideload
```
