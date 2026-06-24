# TCL Remote

An Android phone app that controls a **TCL Google TV / Android TV** over your local
Wi-Fi network. It speaks the official **Android TV Remote v2 protocol** — the same
one Google's own remote app uses — so no extra app is needed on the TV.

## Features

- **Auto-discovery** of the TV on the same network (mDNS), or enter the IP manually.
- **Secure pairing** with the on-screen code (TLS + certificate, persisted so you
  only pair once).
- **D-Pad** with OK/select.
- **Touchpad mode** — swipe to move the highlight, tap to select.
- **Power, Input, Settings, Back, Home, Menu, Guide.**
- **Volume +/-, Mute, Channel +/-.**
- **Media transport:** previous, rewind, play/pause, fast-forward, next, stop.

## Getting the APK (no local tools needed)

This repo includes a GitHub Actions workflow that compiles the APK for you:

1. Push to the `claude/tcl-remote-apk-mousepad-cdauaw` branch (already done), or open
   **Actions → Build TCL Remote APK → Run workflow**.
2. When the run finishes, open it and download the **`tcl-remote-debug-apk`** artifact.
3. Unzip it and copy `app-debug.apk` to your phone.
4. On the phone, allow "install from unknown sources" and tap the APK to install.

## Building locally (optional)

Open the `tclremote/` folder in Android Studio (Giraffe or newer) and press Run, or:

```bash
cd tclremote
gradle wrapper          # first time only, to generate ./gradlew
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17, Android SDK with platform 34 and build-tools 34.0.0.

## Using it

1. Make sure your phone and TV are on the **same Wi-Fi network**.
2. Open the app, tap **Devices**, and pick your TV (or enter its IP).
3. The first time, it will offer to **pair** — a 6-digit code shows on the TV; type it in.
4. After pairing, it connects automatically. Use the D-Pad or switch to **Touchpad**.

> On the TV, the device must allow remote connections. This is on by default on TCL
> Google TV models. If discovery shows nothing, enter the TV's IP manually
> (Settings → Network & Internet → your network → IP address).

## How it works

| Concern        | Detail                                                                 |
|----------------|------------------------------------------------------------------------|
| Pairing        | Polo protocol on TCP **6467** (TLS). Secret = SHA-256 over both certs' modulus/exponent + the code nonce. |
| Control        | Protobuf messages on TCP **6466** (TLS), length-delimited. Keys sent as Android `KeyEvent` codes. |
| Identity       | A self-signed RSA client cert generated on first launch, stored in a PKCS#12 keystore in app-private storage and reused for every connection. |
| Discovery      | mDNS service type `_androidtvremote2._tcp`.                            |

## Notes & limitations

- This targets **Google/Android TV** TCL models. Roku-based TCL TVs use a different
  protocol (ECP on port 8060) and are not supported by this build.
- The "mousepad" is gesture-to-D-pad navigation (Android TV has no absolute cursor),
  which matches how the highlight moves on the TV UI.
- It's a **debug** APK (unsigned for release). Fine for personal use; for the Play
  Store you'd add a release signing config.
