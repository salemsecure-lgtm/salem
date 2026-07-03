# GATE 1 REPORT — Engine (`:domain`, no UI)

**Verdict: PASS (VERIFIER signed; awaiting owner `GATE 1 PASS`)**

## Gate criteria (SPEC §8, Phase 1)

| Criterion | Result |
|---|---|
| 100% branch coverage of decision code | ✅ **112/112 branches** (also 100% instructions/lines/methods/classes in scope), enforced by a kover verify rule (`minValue = 100`, BRANCH) that fails the build on regression |
| Every DOMAIN.md §5.1 worked example is a passing test | ✅ all 17 (E1–E12b) exist as named tests in `AutoregDecisionTableTest` and pass |

## What shipped (`domain/src/main/kotlin/dev/salemlift/domain/`)

| Surface | File | DOMAIN.md |
|---|---|---|
| Decision table R1–R9 as tunable data, first-match-wins | `engine/RuleTable.kt` | §5 |
| Autoregulator: rule evaluation → mild-pain cap → [MV, MRV] clamp, explainable `SetDecision` (rule ID, rationale, deltas, cap/clamp flags), MRV stall counter (limit 2) | `engine/Autoregulator.kt` | §5 |
| Deload triggers (a) planned, (b) MRV stall, (c) systemic ≥3 muscles down, (d) manual | `engine/DeloadTriggers.kt` | §6 |
| RIR schedules 4/5/6-week + deload RIR 4–5 band | `engine/RirSchedule.kt` | §3.1 |
| Mesocycle planner: week 1 = MEV, +1/wk projection clamped to MRV, deload = MV @ 0.80–0.90 load | `engine/MesocyclePlanner.kt` | §3 |
| Double progression: add-load/reset, hold, −5% reduce, too-easy nudge, progress-reps | `engine/DoubleProgression.kt` | §7 |
| e1RM classification (±1% band, tunable) + credit-weighted per-muscle aggregation (ties → SAME) | `engine/PerformanceClassifier.kt` | §4.1 |
| RIR-adjusted Epley e1RM | `EngineInfo.kt` | §8 |
| Landmarks: validation, MV/MAV derivation, all 14 §2.1 seeds, experience scaling with re-derivation | `model/Landmarks.kt` | §2 |

## Test suite — 80 tests, 0 failures

- `AutoregDecisionTableTest` (22): **every §5.1 example E1–E12b**, plus clamp/cap/error-path cases and the empty-rule-table failure mode.
- `ExhaustivenessTest` (7): all **108** (S×P×U×J) combinations have a first-matching rule; clamp invariant `MV ≤ next ≤ MRV` holds from **every** in-range starting volume for every combination; mild pain never increases; significant pain always −1 + swap flag; raw deltas within −1..+3.
- `DeloadTriggersTest` (6), `RirScheduleTest` (5), `MesocyclePlannerTest` (7), `DoubleProgressionTest` (8), `PerformanceClassifierTest` (10), `LandmarksTest` (10), `E1rmTest` (4), `EngineComputeBudgetTest` (1).

## VERIFIER sign-off (independent adversarial pass)

VERIFIER re-ran the full pipeline with `--rerun-tasks` (no cache), parsed the
kover XML itself (112/112 branches confirmed), recomputed all 17 worked
examples by hand from the rules, and compared the implemented table
condition-by-condition against DOMAIN.md §5: **exact match, zero blockers**.
Purity confirmed: zero Android imports, zero `!!`, zero casts, zero
clock/Random in main sources; detekt strict rules active; deps now stdlib-only.

Minor findings and their resolution:

1. **Landmark derivation lives in `.model`, outside the enforced kover scope.**
   Accepted for now: the logic is fully exercised by `LandmarksTest` (all floor
   and ordering branches), and the enforcement scope deliberately targets
   decision packages to keep the 100% rule free of data-class synthetics. If
   model logic grows in Phase 2, it moves into an engine-scoped object.
2. **Deload "RIR 4–5" band was comment-only** → fixed: `WeekEffort.maxRir`
   added; deload is now `WeekEffort(targetRir = 4, maxRir = 5)`.
3. **§4.1 auxiliary clauses not separately coded** → verified arithmetically
   subsumed by the 1% e1RM band (a one-rep/one-RIR shift always moves e1RM
   > 1.6%); now documented on `PerformanceClassifier`.
4. Unused `kotlinx-coroutines-core` dependency → removed; `:domain` is
   stdlib-only.
5–8. Notes (deload-week guard is an orchestration contract for Phase 3
   callers; stall-counter semantics documented and tested; planner projection
   explicitly non-authoritative; §5.2 distribution + landmark individualization
   are Phase 2 scope per SPEC §8).

## Budgets touched this phase

| Budget | Status |
|---|---|
| Engine branch coverage 100% | ✅ enforced in build |
| Engine full-session compute < 50 ms | ✅ smoke-tested in `EngineComputeBudgetTest` (JVM; re-measured on-device at Phase 3) |

## Risks

- On-device compute/latency numbers still pending a physical device (owner-side at later gates).
- The rule table is data-driven and user-tunable; the tunability UI (Phase 5 settings) must reuse the same totality check the engine enforces (`checkNotNull` + the 108-combination test pattern) before accepting a custom table.

## Awaiting

Owner reply **`GATE 1 PASS`** to begin Phase 2 (program builder + exercise DB).
