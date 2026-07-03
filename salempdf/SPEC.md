# Salem PDF — Specification (v0.1, Phase 0)

## Mission

Salem PDF is a fully **offline, local-only** native Android PDF editor with a premium
feature set — view, annotate, organize pages, fill forms — shipped as an installable
`.apk` (debug) and a **signed release `.apk`**. No network, no telemetry, no accounts;
fully functional in airplane mode.

## Clean-room guardrail (overrides everything below)

This is a **functionality clone**, not a copy. We reimplement *what a premium PDF
editor does*, never any product's protected expression:

- No use of the name "PDF Expert", its logo, icon, colour system, copy, or assets.
  The app is **Salem PDF** with an original brand (see Brand section).
- No decompilation, asset extraction, or DRM circumvention of any product.
- Open libraries only: PdfiumAndroid, PdfBox-Android, MuPDF (escalation only).
  No proprietary SDKs.

## Stack (locked)

| Layer | Choice |
|---|---|
| Language / UI | Kotlin + Jetpack Compose (Material 3) |
| Packaging | Gradle → `.apk` (debug + signed release, per-ABI splits) |
| Architecture | MVVM; modules `:domain` / `:data` / `:app` |
| Rendering | PdfiumAndroid (tiling, zoom/pan, thumbnails, text selection) |
| Annotation + manipulation | PdfBox-Android (Apache-2.0) — standard PDF annotations, forms, merge/split |
| Escalation path | MuPDF (AGPL — acceptable for personal use, flagged at the gate) only if a §Budgets target fails on Pdfium/PdfBox |
| Persistence | Room (SQLite), local files only |
| Async / state | Coroutines + Flow; ViewModel + StateFlow |
| Import | Storage Access Framework + share-target intent |
| Min SDK | 26 (Android 8.0); target SDK 35 |

**Escalation rule:** default is Pdfium (render) + PdfBox (write). MuPDF is adopted only
for a specific surface that measurably fails a budget, with the AGPL implication
justified in that phase's gate report.

**Fallback on record:** if native rendering cannot hold the budgets even via MuPDF, we
halt and report rather than ship a janky viewer.

## Feature scope

### In scope (MVP → v1)
- **Viewer:** open, render, continuous scroll, pinch/double-tap zoom, fit-width/page,
  thumbnails, jump-to-page, text selection & copy.
- **Annotate:** highlight / underline / strikeout (snapped to text), freehand ink,
  shapes (rect, oval, line, arrow), sticky-note text annotations, freetext,
  signature/image stamps; select, move, resize, restyle, delete. All annotations are
  **saved into the PDF** as standard PDF annotation dictionaries with appearance
  streams, so they render in other viewers.
- **Page ops:** merge, split, extract, reorder, rotate, delete, insert blank, compress.
  Every output passes a PDF validity check before replacing the user's file.
- **Forms:** detect AcroForms; fill text / checkbox / radio / dropdown; flatten on
  export (explicit user choice). XFA is detected and reported as unsupported.
- **Library:** import via SAF and share sheet; folders; tags; recents; offline
  full-text search over titles and annotation notes.
- **Export/share** the edited PDF via the Android share intent.
- **Signed release `.apk`.**

### Explicitly out of scope (v1) — agreed boundaries
- **True text-content editing / reflow** of existing body text (content-stream + font
  surgery). v1 ships annotation-based editing (freetext, whiteout-style rect + text)
  instead.
- **OCR** of scanned PDFs.
- **Cloud anything:** no sync, no accounts, no collaboration, no telemetry.

## Performance & quality budgets (measured on Galaxy S24 Ultra, ~384dp)

| Metric | Budget |
|---|---|
| First page visible, 50-page text PDF (cold) | < 500 ms |
| Scroll, 200-page doc | sustained 60 fps; no frame > 32 ms |
| Memory, 300-page doc + thumbnails | < 400 MB |
| Ink latency (pen-down → stroke on screen) | < 32 ms |
| Save/flatten, 200-page annotated PDF | < 3 s |
| Release `.apk` size | < 40 MB (per-ABI split) |
| PDF write-path round-trip tests | all passing |

Missing a budget is a gate failure — optimize, or trigger the MuPDF escalation with
justification.

## Engineering conventions (enforced by VERIFIER)

- Kotlin strict null-safety; **no `!!` and no unchecked casts in `:domain`** — build
  fails on violation (detekt rules).
- `:domain` is unit-tested; **every PDF write path has a round-trip test**
  (write → reopen → assert valid & content intact).
- detekt + ktlint wired into the build as failing checks.
- Offline-first: no `INTERNET` permission in the merged manifest; a build-time check
  flags any outbound socket/HTTP usage in code or dependencies.
- Every screen verified at 384dp: no horizontal scroll, tap targets ≥ 48dp.
- All Room / file I/O off the main thread; zero ANRs.
- Every destructive write goes temp-file → validity check → atomic swap; the user's
  source document is never corrupted by a failed operation.

## Phase plan & gates

Each phase ends with: STOP → VERIFIER gate report (what shipped, budgets measured,
tests, risks) → wait for explicit `GATE N PASS`.

| Phase | Deliverable | Gate criterion |
|---|---|---|
| 0 | Spec + architecture docs, agent roster, Gradle multi-module scaffold, hello-world APK | APK builds & installs; spec/scope/brand approved |
| 1 | Rendering: open, tiled/virtualized scroll, zoom/fit, thumbnails, text selection | 200-page doc meets render/scroll/memory budgets at 384dp, or MuPDF escalation triggered |
| 2 | Annotation engine: all types, edit/move/delete, standard PDF persistence | Annotations survive reopen and render correctly in a second viewer (Chrome/Drive) |
| 3 | Page operations | Every output passes validity check and reopens cleanly |
| 4 | Forms: detect, fill, flatten | Filled form renders correctly in another viewer |
| 5 | Library: import, Room metadata, folders/tags/recents, offline search | Metadata persists across restarts; search works in airplane mode |
| 6 | Polish + release: design system, gestures, dark mode, signature capture, settings, icon, signed release APK | Signed APK, fully offline, all budgets green, every screen verified at 384dp |

## Brand — Salem PDF (original, not derived from any product)

- **Name:** Salem PDF. Application ID: `app.salempdf`.
- **Adaptive launcher icon** (108dp canvas, 72dp safe zone):
  - Background: deep indigo → violet diagonal gradient, `#4F46E5` (top-left) →
    `#7C3AED` (bottom-right).
  - Foreground: clean white document page with rounded corners and a folded top-right
    corner (soft shadow), light-grey content lines, and one bold semi-transparent amber
    highlighter stroke (`#FBBF24`) sweeping across a line — the mark for the app's core
    annotation action.
  - Legible at 48px; no fine detail lost when masked.
- **Colour system (Material 3):** primary/seed indigo `#4F46E5`; action/highlight
  accent amber `#FBBF24`; neutral surfaces; full dark mode. Deliberately distinct from
  Adobe red and any coral PDF-app palette.
- **Voice:** active, plain, sentence case — "Save changes", "Add highlight",
  "Merge PDFs". Empty and error states give direction, not mood.
