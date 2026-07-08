# Salem Lift

An **offline, local-only** native Android app that plans and auto-regulates
hypertrophy training: volume-landmark programming (MV/MEV/MAV/MRV), RIR-based
effort prescription, mesocycle periodization, per-muscle set-progression
autoregulation, and deload scheduling. Kotlin + Jetpack Compose, shipped as an
installable `.apk`.

**Status: v1.0.0 — all five phase gates passed** (see `docs/GATE-*-REPORT.md`).
Onboarding, mesocycle programming, the fully-tested autoregulation engine,
session tracking with rest timer, analytics, settings with landmark/rule
tuning, JSON backup, and a signed release APK (`docs/RELEASE.md`).

## Documents

| File | Contents |
|---|---|
| [`docs/SPEC.md`](docs/SPEC.md) | Mission, guardrails, scope, budgets, phase gates |
| [`docs/DOMAIN.md`](docs/DOMAIN.md) | **The training engine domain model** — landmarks, RIR schedule, the autoregulation decision table with worked examples, deload triggers, double progression, e1RM |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Module graph, engine design, data layer, tooling, testing strategy |

The ultracode agent roster lives at the repo root in `.claude/agents/`
(ARCHITECT, ENGINE, PROGRAM, TRACKER, EXERCISE-DB, ANALYTICS, UIUX, VERIFIER).

## Modules

- `:domain` — pure Kotlin/JVM auto-regulation engine (zero Android imports, no
  `!!`, 100% branch-tested from Phase 1)
- `:data` — Room persistence + repositories + open exercise DB seeding
- `:app` — Compose UI (Material 3)

## Building

Requires JDK 17+ and the Android SDK (API 35).

```sh
./gradlew assembleDebug      # → app/build/outputs/apk/debug/app-debug.apk
./gradlew :domain:test       # engine unit tests
./gradlew detekt ktlintCheck # quality gates
```

## Guardrails

Original name, icon, and design. No proprietary product's branding, tables, or
media; the methodology implemented is published exercise science with
user-tunable defaults. Open libraries and the open free-exercise-db dataset
only. No network permission — the app is fully functional in airplane mode.
Personal training tool, **not medical advice**.
