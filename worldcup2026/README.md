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
live games via WorkManager, and shows a "LIVE DATA" / "OFFLINE" chip in the
header. All provider config lives in `data/repo/RemoteConfig.kt`.

Two interchangeable providers are implemented behind a common
`LiveDataSource` interface (`data/repo/`), with HTTP clients and wire models in
`data/remote/`. The active one is chosen automatically from the keys you set.

### Provider 1 — football-data.org (recommended, FULL data)

The **free** tier of [football-data.org](https://www.football-data.org/)
includes the **entire** FIFA World Cup competition: every match, complete group
standings, and the top-scorers list.

1. Register for a free key (email only, no card):
   <https://www.football-data.org/client/register>
2. Paste it into `RemoteConfig.FOOTBALL_DATA_KEY`.

That's it — when the key is present it's used automatically and you get the
complete dataset. (The free tier exposes live status `IN_PLAY`/`PAUSED` and live
scores, but not a minute-by-minute clock, so live games show "In play" rather
than a running minute.)

### Provider 2 — TheSportsDB (default, zero-setup)

With no football-data key, the app uses TheSportsDB's free public key (`"3"`):
**real** data, but the free tier caps responses (≈ last/next 15 fixtures and a
partial standings table) and omits live minutes. A TheSportsDB **Premium** key
(`RemoteConfig.API_KEY`, see <https://www.thesportsdb.com/api.php>) lifts those
caps.

> No fully-free, no-signup feed provides a *complete* live World Cup dataset;
> that always requires a provider account key. football-data.org's free key is
> the closest — full data, just a quick signup.

The football-data.org mapping is covered by a unit test
(`app/src/test/java/.../FootballDataMappingTest.kt`) that runs offline against
sample v4 responses, so the integration is verified even without a key.

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
│   │   │   ├── data/remote/                 # provider DTOs + HTTP clients
│   │   │   ├── data/repo/                   # LiveDataSource impls, repo, config
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
