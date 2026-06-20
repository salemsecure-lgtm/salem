# WC 2026 — World Cup 2026 Companion App

A native Android app to follow the FIFA World Cup 2026 (USA · Canada · Mexico)
live: a "stadium at night" dark UI, live match cards that tick in real time,
group standings, the Golden Boot race, a full match centre, and match
notifications.

Built with **Kotlin + Jetpack Compose + Material 3**.

<p align="center">
  <em>48 teams · 12 groups · 104 matches</em>
</p>

## Features

- **Live, Upcoming & Results** tabs with a branded hero header that shows how
  many matches are live right now. Live cards have a pulsing indicator and a
  ticking match clock (updates every 30s).
- **Match Centre** for any fixture: animated scoreline, a chronological event
  timeline (goals, cards), and a possession/shots/corners/fouls comparison
  panel.
- **Group Standings** for all 12 groups with FIFA tie-break ordering (points →
  goal difference → goals for) and qualification highlighting.
- **Tournament Stats**: total goals, goals per match, cards, and a live Golden
  Boot leaderboard with progress bars.
- **Teams** directory with per-team notification toggles.
- **Notifications**: a high-importance "Match Updates" channel plus a
  WorkManager job that surfaces live scores in the background. Runtime
  `POST_NOTIFICATIONS` permission is requested on Android 13+.

## Data — real, live

The app fetches **real** FIFA World Cup 2026 data at runtime: actual teams and
crests, fixtures, results, scores, groups, standings and top scorers. It polls
every 30s while a match is live (every 2 min otherwise), posts notifications for
live games via WorkManager, and shows a "LIVE DATA" / "UPDATING…" chip in the
header. All provider config lives in `data/repo/RemoteConfig.kt`.

The data source is the public API from
[github.com/rezarahiminia/worldcup2026](https://github.com/rezarahiminia/worldcup2026)
(`worldcup26.ir`), which returns the **entire** tournament with **no key and no
signup**:

- all **104 matches** with scores and **goal scorers** (parsed into the
  match-event timeline),
- **all 12 group tables**,
- all **48 teams** with real flag images,
- the **full knockout bracket** (R32 → Final),
- a real **Golden Boot** leaderboard aggregated from goal events.

The source is implemented as `WorldCup26Source` behind a small `LiveDataSource`
interface (`data/repo/`), with the HTTP client and wire models in
`data/remote/`. The only configuration is the base URL in `RemoteConfig.kt`.

The mapping — including the quirky goal-scorer string parser (mixed
straight/curly quotes, stoppage time, own goals) — is covered by offline unit
tests under `app/src/test/java/`, so the integration is verified without any
network call.

### Online-only

The app is **online-only** — there is no bundled/offline data. On launch it
fetches live data and polls for updates (every 30s while a match is live, every
2 min otherwise). If the provider can't be reached it shows a loading spinner
or a "Can't reach live scores" screen with a Retry button instead of stale
content.

## Build

### From CI (easiest — no local setup)

Every push that touches `worldcup2026/**` runs the
**Build World Cup 2026 APK** GitHub Actions workflow
(`.github/workflows/android-build.yml`). Open the workflow run and download the
`worldcup2026-debug-apk` artifact — that's your installable APK. You can also
trigger it manually from the Actions tab ("Run workflow").

### Locally

Requirements: JDK 17 and the Android SDK (platform 34, build-tools 34.0.0).

```bash
cd worldcup2026
echo "sdk.dir=/path/to/Android/sdk" > local.properties
./gradlew :app:assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Install on a device/emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project layout

```
worldcup2026/
├── app/
│   ├── src/main/
│   │   ├── java/com/salem/worldcup2026/
│   │   │   ├── MainActivity.kt             # nav scaffold + loading/error states
│   │   │   ├── data/model/                 # Team, Match, Standing, ...
│   │   │   ├── data/remote/                 # provider DTOs + HTTP client
│   │   │   ├── data/repo/                   # LiveDataSource, repo, config
│   │   │   ├── viewmodel/                   # UiState + live polling
│   │   │   ├── notifications/               # channel + WorkManager worker
│   │   │   └── ui/{theme,components,screens}
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── build.gradle.kts
```

## Tech

- Kotlin 1.9, Jetpack Compose (BOM 2024.06), Material 3
- Navigation-Compose, Lifecycle/ViewModel
- Live data from worldcup26.ir over HttpURLConnection (no networking dep)
- Coil for loading real team crests
- kotlinx.serialization (JSON), kotlinx.coroutines
- WorkManager for background match alerts
- minSdk 24 · targetSdk 34
