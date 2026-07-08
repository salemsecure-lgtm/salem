# GATE 5 REPORT — Polish + Release (FINAL)

**Verdict: PASS (VERIFIER signed; owner on-device checklist is the last step; awaiting `GATE 5 PASS`)**

## Gate criteria (SPEC §8, Phase 5)

| Criterion | Status |
|---|---|
| Signed release `.apk` | ✅ `app-release.apk`, 1.92 MiB, apksigner-verified (CN=Salem Lift), R8 + resource shrinking |
| Airplane-mode verified | ✅ by construction (no INTERNET permission, no networking client) — **and the guard is falsification-tested**: VERIFIER injected an INTERNET permission, watched `verifyOffline` fail, reverted, watched it pass. On-device confirmation = owner item |
| All §6 budgets green | ✅ everything measurable here (table below); 4 runtime numbers are owner on-device items |
| Every screen at 384dp | ✅ static sweep clean across all screens incl. onboarding/settings; visual pass = owner item |

## What shipped in Phase 5

- **Release pipeline** — signing via git-ignored `keystore.properties` (documented dev fallback, `docs/RELEASE.md`), R8 + resource shrinking, `verifyOffline` wired into `check` (manifest permissions + runtime-classpath networking-client scan).
- **Settings** — landmark editor (invariant-validated steppers, experience re-seeding), autoregulation rule editor (Fixed deltas −3..+3, R4 read-only, reset) whose overrides provably reach the engine inside the commit transaction, rest-timer preference, **versioned JSON backup/export + destructive-replace import via SAF** (round-trip preserves mid-meso state exactly), About with attribution + not-medical-advice.
- **Design system** — original "ember on charcoal" theme, light + dark, WCAG AA verified (VERIFIER recomputed contrast ratios independently; worst sampled pair 5.48:1), tabular numerals on timer/counters, themed deload/cap/swap accent badges.
- **Onboarding** — welcome (offline + medical disclaimers) → experience level (applies scaled landmark seeds *before* anything else can run) → days/week → suggested split (2–3 Full Body / 4 Upper-Lower / 5–6 PPL) → review → first mesocycle.
- **Original launcher icon** — hand-written adaptive vector (three ascending ember pillars = volume climbing MEV→MRV), monochrome themed-icon layer, no raster assets.

## Final measurements (VERIFIER, `--rerun-tasks` from scratch)

| Metric | Result |
|---|---|
| `:domain` | 110 tests, 0 failures; **168/168 branches (100%)**, 0 missed instructions/lines/methods; kover-enforced |
| `:data` | 45 tests × debug/release (now 46 with the import-validation fix), 0 failures |
| `:app` | 65 tests, 0 failures |
| detekt + ktlint + verifyOffline | clean / passing |
| Release APK | **1.92 MiB** vs 30 MB budget; correct signature; only the AndroidX self-permission; dataset + Unlicense bundled |
| IP sweep (full tree) | clean — only guardrail-prohibition sentences; icon confirmed original hand-written geometry |

### SPEC §6 budgets

| Budget | Target | Status |
|---|---|---|
| Engine branch coverage | 100% decision branches | ✅ 168/168, build-enforced |
| Engine full-session compute | < 50 ms | ✅ asserted in-suite |
| Release APK size | < 30 MB | ✅ 1.92 MiB |
| Offline | no network surface | ✅ verified + falsification-tested |
| 384dp / ≥48dp | all screens | ✅ static; visual = owner |
| Cold start | < 1.5 s | ⏳ owner on-device |
| Set-logging latency | < 32 ms | ⏳ owner on-device |
| Rest-timer drift | < 250 ms | ✅ logic unit-tested; ⏳ wall-clock on-device |
| Zero ANR / main-thread Room | none | ✅ suspend DAOs throughout; ⏳ confirm on-device |

## VERIFIER findings → resolution

1. *(minor)* `importBackup` accepted hand-edited rule-override rows that bypass
   the delta guard → **fixed**: overrides are re-validated (editable id +
   −3..+3 range) before any write, with a regression test.
2. *(minor, accepted)* Dev fallback signing credentials are hardcoded — by
   design for personal sideloading; `docs/RELEASE.md` documents generating a
   real keystore (nothing secret is committed; verified).
3–4. *(notes)* Resource shrinker renames the icon resource (still resolves);
   kover scope excludes `.model` data carriers (documented, consistent across
   gates).

## Owner's final on-device checklist (Galaxy S24 Ultra)

1. Install `salemlift/app/build/outputs/apk/release/app-release.apk` (build with `./gradlew :app:assembleRelease`), enable **airplane mode**, run the full flow: onboarding → start meso → log a session → feedback → next-session plan → analytics → settings → backup export/import.
2. Cold start < 1.5 s (charcoal splash, no white flash).
3. Set logging feels instant; no dropped frames.
4. Rest timer vs stopwatch over 3+ min: drift < 250 ms; survives screen-off.
5. No ANRs anywhere in the flow.
6. Visual pass at 384dp, both dark and light themes.

## Project complete on `GATE 5 PASS`

All five phases shipped through their gates: domain lock → engine (100%
branch-enforced) → program builder + open exercise DB → session tracker →
analytics → polish + signed release. The autoregulation engine — the product —
is fully explainable (every prescription carries its rule ID and rationale),
user-tunable (landmarks + rule deltas), and proven against every worked
example in the approved DOMAIN.md.
