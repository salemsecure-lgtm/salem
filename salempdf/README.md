# Salem PDF

A fully **offline, local-only** native Android PDF editor — view, annotate,
organize pages, fill forms — built with Kotlin + Jetpack Compose and shipped
as an installable `.apk`.

- **Spec & scope:** [SPEC.md](SPEC.md)
- **Architecture & library boundary:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Agent roster:** `../.claude/agents/` (ARCHITECT, RENDERER, ANNOTATOR,
  DOCOPS, FORMS, LIBRARY, UIUX, VERIFIER)

## Status

| Phase | State |
|---|---|
| 0 — Spec + scaffold + hello-world APK | ✅ `GATE 0 PASS` |
| 1 — Rendering (Pdfium) | ✅ `GATE 1 PASS` |
| 2 — Annotation engine (PdfBox) | ✅ built, awaiting `GATE 2 PASS` |
| 3 — Page operations | not started |
| 4 — Forms | not started |
| 5 — Library | not started |
| 6 — Polish + signed release | not started |

## Build

```bash
# from salempdf/ — requires the Android SDK (sdk.dir in local.properties or ANDROID_HOME)
./gradlew assembleDebug            # debug APK
./gradlew check                    # unit tests + detekt + ktlint
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Non-negotiables

- No network: the manifest never gains `INTERNET`; no telemetry, no accounts.
- Clean-room: functionality is original work; no other product's name, assets,
  or protected expression. Open libraries only (Pdfium, PdfBox-Android;
  MuPDF only via the documented AGPL-flagged escalation).
- Phase gates: no phase advances without a VERIFIER report and an explicit
  `GATE N PASS`.
