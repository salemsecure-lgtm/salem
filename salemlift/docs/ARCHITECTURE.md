# ARCHITECTURE.md — Salem Lift

> **Status: DRAFT — awaiting owner approval at Gate 0.**

## 1. Module graph

```
:app  ──▶ :data ──▶ :domain
  └────────────────▶ :domain
```

| Module | Type | Contents | Hard rules |
|---|---|---|---|
| `:domain` | `java-library` + `org.jetbrains.kotlin.jvm` (pure JVM) | Volume landmarks, mesocycle planner, RIR schedule, autoregulation decision table, deload triggers, double progression, e1RM. Plain Kotlin data classes + pure functions. | **Zero Android dependencies.** No `!!`, no unchecked casts (detekt fails the build). 100% branch test coverage. No clocks/randomness — time passed in as parameters. |
| `:data` | Android library | Room database (entities, DAOs, migrations), free-exercise-db seeding, repositories mapping Room rows ⇄ `:domain` types, local JSON backup/export. | All I/O on `Dispatchers.IO`; suspend/Flow APIs only. No engine logic. |
| `:app` | Android application | Compose UI (Material 3), ViewModels, navigation, DI wiring, rest timer, charts (Vico). | UI never touches DAOs directly — repositories only. No business rules in ViewModels; they orchestrate `:domain` calls. |

Dependency rule: **arrows point inward only.** `:domain` knows nothing about
persistence or UI. `:data` knows `:domain` but not `:app`.

## 2. Engine design (`:domain`)

The engine is a set of **pure functions** behind one façade:

```kotlin
// Shape sketch — final signatures locked in Phase 1 against DOMAIN.md
object TrainingEngine {
    fun planMesocycle(config: MesoConfig, landmarks: Landmarks, split: Split): MesoPlan
    fun nextPrescription(state: MuscleState, feedback: SessionFeedback, rules: RuleTable): SetDecision
    fun checkDeloadTriggers(session: CommittedSession, plan: MesoPlan): DeloadDecision
    fun progressLoad(history: ExerciseHistory, range: RepRange): LoadPrescription
    fun e1rm(weightKg: Double, reps: Int, rir: Int): Double
}
```

Key properties:
- **Explainability**: `SetDecision` carries the matched rule ID (`R1`–`R9`),
  the raw delta, applied caps/clamps, and the resulting prescription — the UI
  shows "why" for every change, and tests assert on rule IDs, not just numbers.
- **Rules as data**: the §5 decision table is a value (`RuleTable`) with shipped
  defaults, stored per-user in `:data`, so it is tunable without code changes.
- **Determinism**: same inputs → same outputs. All feedback enums, no floats in
  decision inputs.

## 3. Data layer

Room schema (v1 sketch; finalized Phase 2/3):

```
exercise(id, name, primaryMuscle, secondaryMuscles, equipment, cues, isCustom)
landmark(muscle, mv, mev, mav, mrv)              // per-user tuned
rule_table(json)                                  // tunable autoregulation table
mesocycle(id, startDate, weeks, deloadWeek, state)
planned_session(id, mesoId, week, dayIndex, date)
planned_set(id, sessionId, exerciseId, muscle, targetReps, targetRir, targetWeight)
logged_set(id, sessionId, exerciseId, weightKg, reps, rir, timestamp)
muscle_feedback(sessionId, muscle, soreness, pump, jointPain, perfComputed, perfOverride)
```

- free-exercise-db JSON ships in `assets/`, seeded into Room on first launch
  inside a transaction (WorkManager not needed; seeding is < 1s, done behind a
  splash gate).
- Backup/export: serialize all tables to a single JSON file via SAF
  (`CreateDocument`) — no permissions needed, still offline.

## 4. App layer

- **Single-activity** Compose app, Navigation-Compose, Material 3 with dynamic
  color disabled (original brand palette), dark mode via system.
- One ViewModel per screen; `StateFlow<UiState>` pattern; events as sealed types.
- **Rest timer**: coroutine ticking on `delay()` against `SystemClock.elapsedRealtime()`
  anchors (not accumulated delays) so drift stays < 250 ms; survives rotation via
  ViewModel, survives process death via saved anchor timestamp.
- Screens (v1): Onboarding · Home/Today · Session Runner · Feedback sheet ·
  Program Builder · Exercise Picker · Analytics · Settings (landmarks & rules
  editors) · Backup.

## 5. Build & tooling

- Gradle (Kotlin DSL) + version catalog `gradle/libs.versions.toml`.
- Kotlin 2.0.x, AGP 8.7.x, compileSdk/targetSdk 35, minSdk 26, JVM target 17.
- **Quality gates wired into the build:**
  - `detekt` — all modules; `:domain` additionally enables
    `UnsafeCallOnNullableType` + `UnsafeCast` as errors (the "no `!!`" rule).
  - `ktlint` (jlleitschuh plugin) — all modules.
  - `kover` — branch coverage report for `:domain`; Phase 1 turns on a 100%
    branch verification rule for the decision-table package.
  - `verifyOffline` (custom task, Phase 5) — fails if the merged release manifest
    requests `INTERNET`/`ACCESS_NETWORK_STATE` or if any networking artifact
    (okhttp/retrofit/ktor-client) appears in `releaseRuntimeClasspath`.
- **APK outputs**: `assembleDebug` for development installs;
  `assembleRelease` signed with a project keystore (checked-in *debug-only*
  keystore is never used for release; release keystore generated locally,
  path/passwords via `keystore.properties`, git-ignored), R8 enabled, resource
  shrinking on — this is how the < 30 MB budget is met.

## 6. Testing strategy

| Layer | Tests |
|---|---|
| `:domain` | Plain JUnit5 + kotlin-test. One test per DOMAIN.md worked example (E1–E12b), property tests for clamp invariants (`MV ≤ next ≤ MRV` always), exhaustiveness test iterating all (S×P×U×J) = 108 combinations asserting exactly one rule matches. Kover branch coverage = 100% on the engine packages. |
| `:data` | Room DAO tests via Robolectric/instrumented; seed integrity (exercise count, muscle mapping non-empty); migration tests from schema v1 onward. |
| `:app` | Compose UI tests for the session-runner critical path; screenshot-level checks at 384dp width; manual gate checklist on-device (airplane mode, cold start, timer drift). |

## 7. Ultracode agent roster

Lives in `.claude/agents/` at the repo root (see files). Roles: ARCHITECT,
ENGINE, PROGRAM, TRACKER, EXERCISE-DB, ANALYTICS, UIUX, VERIFIER. Each owns the
surface described in its file; VERIFIER signs every gate report; no phase
advances without VERIFIER sign-off **and** owner approval (`GATE N PASS`).

## 8. Repository layout

```
salemlift/
  docs/            SPEC.md · ARCHITECTURE.md · DOMAIN.md · gate reports
  domain/          pure-Kotlin engine module
  data/            Room + repositories module
  app/             Compose application module
  config/detekt/   detekt.yml (+ domain-strict overrides)
  gradle/          version catalog + wrapper
```
