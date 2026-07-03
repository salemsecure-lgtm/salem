# DOMAIN.md — Salem Lift Training Engine Domain Model

> **Status: DRAFT — awaiting owner approval at Gate 0.**
> This document is the single source of truth for the `:domain` engine. Every decision
> branch below must be covered by a unit test in Phase 1, and every worked example in
> §5 becomes a literal test case. No engine code is written until this document is
> approved.

All numeric defaults in this document are **user-tunable**. They are seeded from
published exercise-science ranges (volume-landmark theory, RIR-based effort
prescription, double progression) — not copied from any proprietary product table.
Salem Lift is a personal training tool, not medical advice; every downward adjustment
(fewer sets, deload, exercise swap) can always be taken earlier or made larger by the
user, and joint-pain logic is deliberately conservative.

---

## 1. Core vocabulary

| Term | Definition |
|---|---|
| **Hard set** | A working set taken to within the prescribed RIR (reps in reserve). Warm-ups don't count. |
| **Weekly volume** | Hard sets per muscle group per week. The engine's central control variable. |
| **RIR** | Reps in reserve at the end of a set. 0 = could not do another rep. |
| **Session feedback** | Per-muscle ratings collected after (and during) a session: soreness/recovery, performance vs last, pump, joint pain. |
| **Mesocycle** | A block of accumulation weeks followed by a deload week. |

### 1.1 Muscle groups (extensible enum)

Chest · Back · Front delts · Side delts · Rear delts · Biceps · Triceps · Forearms ·
Quads · Hamstrings · Glutes · Calves · Abs · Traps.

An exercise maps to one **primary** muscle (full set credit, 1.0) and zero or more
**secondary** muscles (fractional credit, default 0.5, tunable). Example: a barbell
row credits Back 1.0 and Biceps 0.5 per hard set.

---

## 2. Volume landmarks (per muscle, weekly hard sets)

| Landmark | Meaning | Default range |
|---|---|---|
| **MV** — Maintenance Volume | Volume that maintains current size; deload target | ~6 sets/wk (defined per muscle as `round(MEV / 2)`, min 2) |
| **MEV** — Minimum Effective Volume | Lowest volume that produces growth; mesocycle starting point | 4–10, muscle-specific |
| **MAV** — Maximum Adaptive Volume | The productive sweet-spot band the mesocycle climbs through | 12–20 |
| **MRV** — Maximum Recoverable Volume | Ceiling; exceeding it accumulates unproductive fatigue | 18–30, the most individual number |

### 2.1 Intermediate seed defaults (all editable in Settings)

| Muscle | MEV | MAV (mid) | MRV |
|---|---|---|---|
| Chest | 8 | 16 | 22 |
| Back | 10 | 18 | 25 |
| Front delts | 4 | 10 | 16 |
| Side delts | 8 | 18 | 26 |
| Rear delts | 6 | 14 | 22 |
| Biceps | 6 | 16 | 26 |
| Triceps | 6 | 12 | 18 |
| Forearms | 4 | 10 | 16 |
| Quads | 8 | 14 | 20 |
| Hamstrings | 6 | 12 | 18 |
| Glutes | 6 | 12 | 20 |
| Calves | 8 | 14 | 20 |
| Abs | 6 | 14 | 22 |
| Traps | 6 | 12 | 20 |

**Experience scaling** applied at onboarding (multiplies MEV and MRV, rounding to
nearest integer, MEV floor 2):

| Experience | Factor |
|---|---|
| Beginner (< 1 yr) | × 0.7 |
| Intermediate (1–4 yr) | × 1.0 |
| Advanced (> 4 yr) | × 1.15 |

Over time landmarks are **individualized from logged history**: if a muscle
repeatedly triggers the MRV deload signal (§6b) at N sets, the engine suggests
lowering that muscle's MRV toward N; if a user reaches planned MRV with no
recovery signal, it suggests raising it. Suggestions only — the user confirms.

---

## 3. Mesocycle structure

- Default: **4 accumulation weeks + 1 deload week**. Configurable to 4–6
  accumulation weeks + deload.
- Every muscle **starts week 1 at its MEV** (as distributed across the split by the
  program builder; ±1 set of rounding tolerance).
- Weekly volume then moves per the §5 autoregulation rules — normally climbing
  through MAV toward MRV by the final accumulation week.
- **Deload week**: per-muscle volume drops to **MV ≈ 50% of MEV**, target effort
  RIR 4–5, and prescribed loads drop 10–20% (default −15%, tunable). No
  autoregulation feedback is acted on during a deload week.

### 3.1 RIR (effort) schedule across accumulation — tunable

| Meso length | Week-by-week target RIR | Deload |
|---|---|---|
| 4 weeks (default) | 3 → 2 → 1 → 0–1 | 4–5 |
| 5 weeks | 3 → 2 → 2 → 1 → 0–1 | 4–5 |
| 6 weeks | 3 → 2 → 2 → 1 → 1 → 0 | 4–5 |

"0–1" means sets are prescribed at 1 RIR with the final set of each exercise
allowed to 0 RIR.

---

## 4. Session feedback inputs (per muscle, per session)

| Input | Values | Asked when |
|---|---|---|
| **Soreness / recovery** | `NEVER_SORE` · `RECOVERED_EARLY` · `RECOVERED_ON_TIME` · `STILL_SORE` | At the *start* of the next session that trains the muscle ("how did you recover from last time?") |
| **Performance vs last** | `UP` · `SAME` · `DOWN` | Computed by the engine from logged sets (see §4.1), user-overridable |
| **Pump** | `LOW` · `MODERATE` · `HIGH` | Immediately after the muscle's last exercise in the session |
| **Joint pain** | `NONE` · `MILD` · `SIGNIFICANT` | Immediately after the muscle's last exercise in the session |

### 4.1 Performance comparison

For each exercise, compare this session's best-set **RIR-adjusted e1RM** (§8) against
the previous session of the same exercise at comparable prescription:

- `UP` — e1RM up > 1.0% (tunable), or same load/reps achieved at lower effort
- `SAME` — within ±1.0%
- `DOWN` — e1RM down > 1.0%, or failed to match prescribed reps at target RIR

Per-muscle performance = the credit-weighted majority across that muscle's
exercises in the session (ties resolve to `SAME`).

---

## 5. Set-progression autoregulation — THE decision table

Evaluated **per muscle** when a session's feedback is committed; produces the set
delta applied to that muscle's *next* session that trains it. First matching rule
wins (top-down). The rules table itself is stored as data and is **user-tunable**;
below are the shipped defaults.

**Inputs:** soreness `S`, performance `P`, pump `U`, joint pain `J`, current
prescribed weekly sets `sets`, landmarks `MV/MEV/MAV/MRV`.

| # | Condition (first match wins) | Raw Δ | Side effect |
|---|---|---|---|
| R1 | `J = SIGNIFICANT` | **−1** | Flag the offending exercise(s) for swap |
| R2 | `S = STILL_SORE` and `P = DOWN` | **−1** | Recovery exceeded — possible MRV; note on muscle |
| R3 | `S = STILL_SORE` and `P ∈ {SAME, UP}` | **0** | Hold |
| R4 | `S = NEVER_SORE` and `P = UP` and `U = LOW` | **+3** if `MRV − sets ≥ 4`, else **+2** | Far from MRV → aggressive add |
| R5 | `S ∈ {NEVER_SORE, RECOVERED_EARLY}` and `P = UP` and `U ∈ {LOW, MODERATE}` | **+2** | — |
| R6 | `S ∈ {NEVER_SORE, RECOVERED_EARLY}` and `P = UP` and `U = HIGH` | **+1** | Stimulus already high; standard add |
| R7 | `S = RECOVERED_ON_TIME` and `P ∈ {SAME, UP}` | **+1** | Standard progression |
| R8 | `S ∈ {NEVER_SORE, RECOVERED_EARLY}` and `P = SAME` | **+1** | Capacity to spare |
| R9 | `P = DOWN` (any non-sore recovery, `J ≠ SIGNIFICANT`) | **0** | Hold — don't add fatigue to a down day |

R1–R9 are exhaustive: every (S, P, U, J) combination hits exactly one rule.

**Post-processing, applied in order:**

1. **Mild-pain cap:** if `J = MILD`, `Δ ← min(Δ, 0)` (never add volume on a
   painful joint; reductions pass through).
2. **Clamp:** `nextSets = clamp(sets + Δ, MV, MRV)`. The "at MRV → hold" rule of
   the source table is realized by this clamp: positive deltas at MRV clamp to 0;
   negative deltas at MRV still apply.
3. **Deload week:** during a deload week the table is not evaluated; volume is
   fixed at MV (§3).

**MRV stall tracking (runs regardless of the matched rule):** if `sets ≥ MRV` and
`P = DOWN`, increment the muscle's consecutive-MRV-down counter; any `P ∈ {SAME, UP}`
at MRV resets it. When the counter reaches **2**, deload trigger (b) fires (§6).

### 5.1 Worked examples — one per rule branch (these become Phase 1 unit tests)

Chest landmarks used throughout: MV 4, MEV 8, MAV ~16, MRV 22.

| Ex | Rule | Inputs (S, P, U, J) | sets | Raw Δ | Final nextSets | Why |
|---|---|---|---|---|---|---|
| E1 | R1 | on-time, UP, MOD, **SIGNIFICANT** | 12 | −1 | **11** + swap flag | Significant joint pain overrides everything, even good performance |
| E2 | R2 | **STILL_SORE, DOWN**, LOW, NONE | 18 | −1 | **17** | Recovery exceeded; back off and note possible MRV |
| E3 | R3 | **STILL_SORE, SAME**, MOD, NONE | 14 | 0 | **14** | Sore but performing — hold, don't dig deeper |
| E4 | R4 | **NEVER_SORE, UP, LOW**, NONE | 10 | +3 (22−10 ≥ 4) | **13** | No stimulus signal at all, far below MRV → aggressive |
| E4b | R4 | NEVER_SORE, UP, LOW, NONE | 19 | +2 (22−19 = 3 < 4) | **21** | Same signal near MRV → +2, not +3 |
| E5 | R5 | **RECOVERED_EARLY, UP, MOD**, NONE | 12 | +2 | **14** | Under-stimulated and progressing → fast add |
| E6 | R6 | NEVER_SORE, UP, **HIGH**, NONE | 12 | +1 | **13** | Great pump says stimulus is adequate; standard add |
| E7 | R7 | **RECOVERED_ON_TIME, SAME**, MOD, NONE | 12 | +1 | **13** | The bread-and-butter weekly +1 |
| E7b | R7 | RECOVERED_ON_TIME, UP, HIGH, NONE | 16 | +1 | **17** | On-time + up = standard add regardless of pump |
| E8 | R8 | RECOVERED_EARLY, **SAME**, LOW, NONE | 10 | +1 | **11** | Recovered early with flat performance — room to add |
| E9 | R9 | RECOVERED_ON_TIME, **DOWN**, MOD, NONE | 14 | 0 | **14** | Down day without soreness — hold, investigate, don't add |
| E10 | mild-pain cap | on-time, SAME, MOD, **MILD** | 12 | +1 → capped 0 | **12** | R7 fires but mild pain caps the increase at 0 |
| E10b | mild-pain cap | STILL_SORE, DOWN, LOW, MILD | 12 | −1 (R2) | **11** | Cap only blocks increases; reductions pass through |
| E11 | clamp @ MRV | on-time, UP, MOD, NONE | 22 (=MRV) | +1 → clamp | **22** (hold) | "At MRV → 0" via the clamp |
| E11b | clamp @ MV | STILL_SORE, DOWN, LOW, NONE | 4 (=MV) | −1 → clamp | **4** | Never prescribed below MV by autoregulation |
| E12 | MRV stall → deload | at MRV, P = DOWN twice consecutively | 22, 22 | — | deload trigger (b) fires | Two straight down sessions at the ceiling = unrecoverable |
| E12b | stall reset | at MRV: DOWN, then SAME, then DOWN | 22 | — | counter 1 → 0 → 1, **no trigger** | Counter resets on any non-down session |

### 5.2 Distribution of a weekly delta across sessions

The weekly set target lives per muscle; the program builder distributes it across
the split's sessions as evenly as possible (e.g. 13 sets over 2 chest days → 7+6),
adding new sets first to the session where the muscle is trained freshest (earliest
in the session). Per-session change is capped at **+2 sets** per muscle to avoid
one session ballooning.

---

## 6. Deload triggers

A deload is scheduled when **any** of the following fires:

| Trigger | Condition | Scope |
|---|---|---|
| (a) Planned | Final accumulation week completes | Whole plan |
| (b) MRV stall | Muscle at MRV with performance DOWN in 2 consecutive sessions (§5 counter) | Whole plan (fatigue is systemic), engine reports the triggering muscle |
| (c) Systemic | Performance DOWN in ≥ 3 muscles within a single committed session | Whole plan |
| (d) Manual | User taps "start deload" | Whole plan |

Trigger (b)/(c)/(d) end the accumulation phase early: the next week becomes the
deload week, after which the mesocycle ends and a new one is generated (starting
back at MEV, with landmark-individualization suggestions from §2.1 applied).

---

## 7. Load / rep progression within an exercise (double progression)

Each exercise is assigned a **rep range** (default 8–12 for compounds via machines
/ dumbbells, 5–8 for heavy barbell compounds, 12–20 for isolation/pump work —
tunable per exercise).

1. Work at a fixed load; try to add reps session to session at the prescribed RIR.
2. When **all prescribed sets** hit the **top of the rep range** at (or easier
   than) target RIR → **add load** (+2.5 kg upper-body / +5 kg lower-body defaults,
   respecting equipment increments) and reset reps to the **bottom** of the range.
3. RIR-guided corrections from **actual logged RIR**:
   - Actual RIR > target + 2 (way too easy) → engine suggests a load bump next
     session even mid-range.
   - Actual RIR < target (harder than prescribed, e.g. hit 0 when 2 was
     prescribed) → hold or reduce load 5% next session so the weekly RIR ramp
     stays intact.

Weight prescriptions during deload: −10–20% (default −15%) off the last working
load.

---

## 8. Strength-trend metric (deterministic)

RIR-adjusted Epley estimated 1RM, used for performance comparison (§4.1) and
analytics trends:

```
e1RM = weight × (1 + (reps + RIR) / 30)
```

Examples: 100 kg × 8 reps @ 2 RIR → 100 × (1 + 10/30) = **133.3 kg**.
60 kg × 12 @ 0 → 60 × 1.4 = **84.0 kg**. Bodyweight-only exercises use logged
added-load + estimated bodyweight fraction (Phase 2 detail).

---

## 9. Explicit non-goals of the engine

- No nutrition, no HRV/sleep inputs (possible later phase), no cloud calls — the
  engine is a pure function of local logged history and tunable parameters.
- No medical claims: soreness/pain inputs drive conservative volume math only, and
  significant-pain handling is "reduce and suggest swapping the exercise", never
  "push through".
