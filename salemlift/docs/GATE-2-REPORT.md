# GATE 2 REPORT — Program Builder + Exercise DB

**Verdict: PASS (VERIFIER signed; awaiting owner `GATE 2 PASS`)**

## Gate criterion (SPEC §8, Phase 2)

> A full mesocycle generates with each muscle's weekly volume inside its landmark range.

✅ Proven by the named gate test (`ProgramBuilderTest."GATE - every template
generates with weekly volume inside the landmark range"`): **all 4 shipped
templates × all weeks × all trained muscles** assert `weeklyVolume ∈ [MV, MRV]`.
Companion tests pin week 1 = exactly MEV and deload = exactly MV per muscle.
VERIFIER independently re-ran and hand-traced the distribution math.

## What shipped

### `:domain` program package (inside the 100%-branch regime)
| Surface | File | Spec |
|---|---|---|
| Split templates: PPL 3/6-day, Upper/Lower 4-day, Full-Body 3-day, custom factory — each covers all 14 muscles | `program/SplitTemplates.kt` | SPEC §3 |
| Volume distribution: even split, remainder to freshest (earliest) sessions; week-over-week redistribution with the **+2/session cap** (round-robin increases, back-first decreases, capped flag) | `program/VolumeDistributor.kt` | DOMAIN §5.2 |
| Mesocycle instantiation: engine MesoPlan × split → per-session prescriptions, weeks chained through capped redistribution | `program/ProgramBuilder.kt` | DOMAIN §3 + §5.2 |

### `:data` exercise catalog
- **free-exercise-db bundled** (873 exercises, public domain; Unlicense file ships beside the asset and inside the APK).
- Pure-Kotlin parser + `MuscleMapper`: the dataset's full 17-string muscle vocabulary maps onto the 14 canonical muscles; ambiguous "shoulders" resolved by name keywords (rear > side > front precedence, presses → front delts), policy in kdoc. **873/873 seeded, 0 excluded, every muscle has primary-exercise coverage** (histogram in test output; rear delts thinnest at 12).
- Room v1 (`ExerciseEntity`, converters, `ExerciseDao` with search/muscle/equipment filters + custom-exercise insert, exported schema committed under `data/schemas/`).
- Idempotent single-transaction `ExerciseSeeder` behind `AssetSource`/`TransactionRunner` interfaces → seeding logic proven on JVM against the real JSON, no Robolectric.

## Measurements (VERIFIER, independent re-runs)

| Metric | Result |
|---|---|
| `:domain` tests | 102, 0 failures |
| `:domain` branch coverage | **160/160 = 100%** (engine 100 + program 48 + root 12), `koverVerify` enforced |
| `:data` tests | 15 (× debug/release variants), 0 failures |
| detekt + ktlint | clean |
| Debug APK | ~9.0 MB (8.7 → 9.0 with the 1.0 MB dataset; < 30 MB release budget on track) |
| Permissions | still **no INTERNET**; runtime classpath free of any networking client |
| IP sweep | clean |

## VERIFIER findings → resolution

1. *(minor)* Test name overstated its assertion → **fixed**: now asserts session
   sums equal the planner's targets exactly (the +1/week projection can never
   hit the +2 cap), renamed accordingly.
2. *(minor)* `searchByName` didn't escape SQL LIKE wildcards → **fixed**:
   escaped variant + `escapeLikeQuery` helper with tests; a typed `%` now
   matches literally.
3. *(note)* Production DB wiring (`Room.databaseBuilder`, asset adapter,
   `withTransaction`) intentionally lands in **Phase 3** with the tracker —
   seeding is proven on JVM; on-device seeding is a Phase 3 gate item.
4. *(note)* Deload week is always terminal, so the chained distribution state
   ends there (re-verified); flagged for re-check if post-deload projections
   ever appear.
5. *(note)* PPL 3-day concentrates late-meso volume into single sessions
   (landmarks still respected) — programming-quality consideration for the UI
   to surface, not a defect.
6. *(note)* Mapper policy for shoulders/adductors/abductors is lossy but
   documented, consistent, and fully tested — inherent to the dataset's
   vocabulary.

## Deferred (explicit)

- **Landmark individualization suggestions** (DOMAIN §2.1 last paragraph):
  deferred to Phase 3+ — its natural input is real logged history, which does
  not exist until the tracker ships. Will be specced against DOMAIN.md before
  implementation.
- Exercise-selection UI per session slot is Phase 3/5 scope; the domain plan
  is muscle-level by design.

## Awaiting

Owner reply **`GATE 2 PASS`** to begin Phase 3 (session tracker: Room wiring
on device, session runner, set logging, rest timer, feedback capture → engine
next-session computation).
