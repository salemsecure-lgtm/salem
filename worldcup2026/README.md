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

The app fetches **real** FIFA World Cup 2026 data at runtime from
[TheSportsDB](https://www.thesportsdb.com/) (league `4429`, season `2026`):
actual teams and crests, fixtures, results, scores, venues, groups and
standings. It polls every 30s while a match is live (every 2 min otherwise) and
posts notifications for live games via WorkManager. The "LIVE DATA" / "OFFLINE"
chip in the header shows whether the current view is from the network.

Networking is in
`app/src/main/java/com/salem/worldcup2026/data/remote/` and the mapping to the
app's models is in `data/repo/LiveDataSource.kt`.

### Free vs. premium key

`data/repo/RemoteConfig.kt` holds the provider key. It ships with TheSportsDB's
free public key (`"3"`), which returns **real** data but with two free-tier
limits:

- responses are **capped** (roughly the last/next ~15 fixtures and a partial
  standings table rather than all 104 matches / 48 teams), and
- there is **no minute-by-minute live progress** (matches still flip
  NS → live → FT as they're played).

Paste your own **TheSportsDB Premium** key (a few dollars/month, see
<https://www.thesportsdb.com/api.php>) into `RemoteConfig.API_KEY` to lift the
caps and get the full schedule, complete standings, and live scores. No other
code changes are needed.

> There is no fully-free, no-signup feed that provides *complete* live World Cup
> data; that always requires a provider key tied to an account.

### Offline fallback

`app/src/main/assets/tournament.json` is a bundled snapshot used **only** when
the network is unavailable, so the app always opens with content. When online,
real provider data replaces it.

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
│   │   ├── assets/tournament.json          # offline fallback snapshot
│   │   ├── java/com/salem/worldcup2026/
│   │   │   ├── MainActivity.kt             # nav scaffold + bottom bar
│   │   │   ├── data/model/                 # Team, Match, Standing, ...
│   │   │   ├── data/remote/                 # TheSportsDB DTOs + HTTP client
│   │   │   ├── data/repo/                   # live source, repo, config
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
- Live data from TheSportsDB over HttpURLConnection (no networking dep)
- Coil for loading real team crests
- kotlinx.serialization (JSON), kotlinx.coroutines
- WorkManager for background match alerts
- minSdk 24 · targetSdk 34
