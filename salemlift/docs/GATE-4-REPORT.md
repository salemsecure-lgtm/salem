# GATE 4 REPORT — Analytics

**Verdict: PASS (VERIFIER signed; on-device render check owner-side; awaiting `GATE 4 PASS`)**

## Gate criterion (SPEC §8, Phase 4)

> Charts render at 384dp; values reconcile with logged data.

✅ **Reconciliation** is the verified half: 4 Robolectric tests seed a
two-week Upper/Lower scenario through the real tracker (logged sets +
committed feedback hitting R7, R2, and R1) and assert **hand-computed** values
for weekly volume, e1RM trend, tonnage, fatigue counts, rule-fire histogram,
and meso progress. The VERIFIER re-derived every constant independently from
the domain sources — and confirmed the scenario genuinely pins the engine:
BICEPS week-2 = 5 and QUADS = 7 *cannot* come from the planner projection
(which would say 7 and 9), so the tests prove the engine-rewrite path, not
the plan.
✅ **384dp** static pass: no horizontal page scroll anywhere in the analytics
package (the muscle chip row is a `LazyRow`, acceptable), charts pinned
(`scrollEnabled = false`), no fixed widths > 384dp, all targets ≥ 48dp.
On-device rendering on the S24 Ultra is the owner-side item.

## What shipped

- **`:data`** — `AnalyticsRepository` + read-only `AnalyticsDao` (no schema
  change; DB stays v1): weekly performed-vs-prescribed sets per muscle, e1RM
  trend per exercise (domain `e1rm` fn, never re-derived in SQL), weekly
  tonnage, meso progress, fatigue summary (soreness / joint pain /
  performance-down counts + decision rule-ID histogram with the engine's own
  rationale strings).
- **`:app`** — Analytics screen (Vico 2.0.0) with three tabs:
  **Volume** (columns = performed, line = prescribed, MV/MEV/MAV/MRV as
  labeled reference lines + text legend, muscle chips), **Strength** (e1RM
  line + tonnage columns, exercise picker), **Cycle** (week x/y progress card
  with RIR target and deload badge; fatigue dashboard + R1–R9 fire counts).
  Empty states on all tabs; 48dp Analytics entry on Home.

## Measurements (VERIFIER, independent re-runs)

| Metric | Result |
|---|---|
| `:data` tests | 30 (× debug/release), 0 failures — incl. 4 reconciliation tests |
| `:app` tests | 44, 0 failures |
| `:domain` | 110 tests; **168/168 branches = 100%** (kover-enforced) |
| detekt + ktlint | clean |
| Debug APK | ~11.4 MB (Vico added; < 30 MB release budget on track) |
| Permissions / networking | still no INTERNET; classpath clean |
| Schema | unchanged — v1, accessor-only diff verified via git |

## VERIFIER findings → resolution

1. *(minor)* e1RM trend was not meso-scoped and ordered by (week, day) — a
   second mesocycle would interleave points → **fixed**: `e1rmTrend(exerciseId,
   mesoId)`, ordered by commit time; ViewModel threads the active meso through.
2. *(minor)* Volume/tonnage counted sets in uncommitted sessions while the
   e1RM trend excluded them → **fixed**: all three aggregate committed
   sessions only, and the pending-session test now pins volume and tonnage
   alongside the trend (a never-committed session can no longer inflate
   analytics).
3. *(note, accepted)* `exercisesWithHistory` is deliberately all-time (the
   picker should show anything you've ever logged).
4. *(note, accepted)* One dead-code fallback in the point builder — harmless,
   left for robustness against future query changes.

## Owner on-device checklist (with the Phase 3 items)

1. Analytics tabs render cleanly at 384dp (charts, chips, tables).
2. Chart values match what you logged (spot-check a week's set count).

## Awaiting

Owner reply **`GATE 4 PASS`** to begin Phase 5 (polish + release: design
system, onboarding, settings with landmark/rule editors, local backup/export,
original icon, `verifyOffline` build check, signed release APK, full budget
verification).
