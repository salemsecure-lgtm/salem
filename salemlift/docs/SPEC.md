# SPEC.md — Salem Lift

> **Status: DRAFT — awaiting owner approval at Gate 0.**

## 1. Mission

Salem Lift is a fully **offline, local-only** native Android app that plans and
auto-regulates hypertrophy training using the volume-landmark methodology
(MV/MEV/MAV/MRV), RIR-based effort prescription, mesocycle periodization, and
per-muscle set-progression autoregulation. It is delivered as an installable
debug `.apk` and a signed release `.apk`. The **auto-regulation engine is the
product**; everything else exists to feed it data and display its output.

## 2. IP & safety guardrail (binding)

- Functionality clone of a *published training methodology*, not of any product.
  The science (volume landmarks, RIR progression, mesocycle periodization,
  set-progression autoregulation, double progression) is published exercise
  science and fair to implement.
- **Prohibited:** the names "RP", "RP Hypertrophy", "Renaissance Periodization",
  their logo/icon/colours/copy, and any of their technique videos or media.
  Original name (Salem Lift), original icon, original design language.
- All landmark/RIR defaults are user-tunable seeds grounded in published ranges
  and individualized from the user's own logged history — no proprietary table is
  reproduced verbatim.
- Open-source libraries and the open **free-exercise-db** dataset only. No
  proprietary SDKs, no scraped assets, no bundled third-party media.
- Personal training tool, **not medical or physiotherapy advice**. Pain/deload
  logic is conservative and the user can always override *downward* (train less,
  deload earlier, swap an exercise).

## 3. Product scope

### In scope (MVP → v1)
1. **Onboarding** — experience level, training days/week, editable volume
   landmarks per muscle.
2. **Program builder** — split templates (PPL, Upper/Lower, Full-Body, custom),
   exercise selection per session slot, muscle mapping, weekly-volume
   distribution, mesocycle instantiation (4–6 accumulation weeks + deload).
3. **Session tracker** — live session runner: prescribed sets with target
   weight/reps/RIR, per-set logging, rest timer (coroutine-driven, < 250 ms
   drift), per-muscle feedback capture (soreness, pump, joint pain; performance
   auto-computed), session commit → engine computes the next session.
4. **Auto-regulation engine** (`:domain`, pure Kotlin) — everything in
   `DOMAIN.md`: landmarks, mesocycle planner, RIR schedule, §5 set-progression
   decision table, deload triggers, double progression, e1RM.
5. **Analytics** — weekly volume per muscle vs landmarks, e1RM & tonnage trends,
   mesocycle progress, fatigue dashboard. Charts via Vico.
6. **Exercise database** — free-exercise-db seeded into Room at first launch;
   search/filter by muscle & equipment; custom exercises; text-cue technique
   notes (the clean substitute for proprietary media).
7. **Data ownership** — export/backup and restore to a local file (JSON).
8. **Fully offline** — no network permission, no telemetry, no accounts; fully
   functional in airplane mode.

### Explicitly out of scope (v1)
- Proprietary technique videos or branded media (text cues + open DB only).
- Cloud sync, accounts, social features.
- Wearable / HRV / sleep integration (candidate later phase).
- Nutrition & macro tracking (separate app).

## 4. Users & core flows

1. **New user:** onboard → pick split & days → app generates mesocycle week 1 at
   MEV → train.
2. **Training day:** open today's session → warm up → log working sets
   (weight/reps/RIR) with rest timer → rate pump/joint pain per muscle at last
   exercise → commit → app confirms next session's prescription changes.
3. **Next session for a muscle:** app asks soreness/recovery since last time →
   engine's precomputed delta finalizes.
4. **Week over week:** volume climbs per the decision table toward MRV; RIR
   target descends per schedule.
5. **Deload:** planned or triggered (MRV stall / systemic fatigue / manual) →
   week at MV volume, RIR 4–5, −15% load → new mesocycle generated.
6. **Review:** analytics screens show volume vs landmarks, e1RM trends, and what
   the engine decided and *why* (every delta is explainable — rule ID shown).

## 5. Stack (locked)

| Layer | Choice |
|---|---|
| Language / UI | Kotlin + Jetpack Compose (Material 3) |
| Packaging | Gradle → `.apk` (debug + signed release); no store submission |
| Architecture | MVVM; modules `:domain` (pure engine) / `:data` (Room) / `:app` (UI) |
| Engine | `:domain` pure Kotlin/JVM, **zero Android imports**, 100% branch-tested |
| Persistence | Room (SQLite), local file only |
| Async / state | Coroutines + Flow; ViewModel + StateFlow |
| Charts | Vico (Compose-native) |
| Exercise data | free-exercise-db seeded into Room |
| DI | Hilt (fallback: lean manual DI if Hilt/KSP friction outweighs value) |
| Min SDK | 26 (Android 8.0); target SDK 35 |

## 6. Quality budgets (measured on Galaxy S24 Ultra, ~384dp width)

| Metric | Budget |
|---|---|
| Engine unit-test branch coverage | **100% of `:domain` decision branches** |
| Engine next-session compute (full session) | < 50 ms |
| Cold start | < 1.5 s |
| Set-logging input latency | < 32 ms (60 fps) |
| Rest-timer drift | < 250 ms |
| Release `.apk` size | < 30 MB |
| Main-thread Room I/O | none (zero ANRs) |
| Layout | no horizontal scroll at 384dp; tap targets ≥ 48dp |

Missing a budget is a gate failure.

## 7. Engineering conventions

- Kotlin strict null-safety. **No `!!` and no unchecked casts in `:domain`** —
  enforced by detekt (`UnsafeCallOnNullableType`, `UnsafeCast`) failing the build.
- detekt + ktlint wired into the build; violations fail CI/gates.
- Offline check: release verification asserts the merged manifest contains **no
  `INTERNET` permission** and the dependency tree contains no networking client.
- Every engine decision must be **explainable**: the engine returns the rule ID
  and inputs alongside every delta so the UI can show "why".
- Deterministic engine: same inputs → same outputs; no clocks or randomness
  inside `:domain` (time is passed in).

## 8. Phase plan & gates

Each phase ends: **STOP → VERIFIER gate report → wait for explicit `GATE N PASS`.**

| Phase | Deliverable | Gate criterion |
|---|---|---|
| 0 | SPEC/ARCHITECTURE/DOMAIN docs, agent roster, Gradle scaffold, hello-world APK | APK installs & launches; §4 domain model approved by owner |
| 1 | `:domain` engine, no UI | 100% branch coverage; every DOMAIN.md §5 example is a passing test |
| 2 | Program builder + exercise DB | Full mesocycle generates; weekly volume within landmarks for every muscle |
| 3 | Session tracker | Log session → correct next-session sets per decision table; survives restart & airplane mode |
| 4 | Analytics | Charts render at 384dp; values reconcile with logged data |
| 5 | Polish + signed release APK | Airplane-mode verified; all budgets green; every screen verified at 384dp |
