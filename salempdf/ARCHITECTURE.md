# Salem PDF — Architecture (v0.1, Phase 0)

## Module map

```
salempdf/
├── app/        Compose UI, navigation, ViewModels, DI wiring, platform glue
├── domain/     Pure Kotlin/JVM: annotation model, doc-op commands, PDF write
│               logic (PdfBox), validity checks. No Android UI deps.
├── data/       Room database, repositories, SAF import, file management
└── (later) :render  — if the Pdfium wrapper grows, it splits out of :app
```

- **`:app`** depends on `:domain` and `:data`. Hosts PdfiumAndroid (rendering is
  inherently Android-surface-bound: bitmaps, Compose canvas).
- **`:domain`** holds the annotation model, document-operation commands, and the
  PdfBox write paths. Strictest quality bar: no `!!`, no unchecked casts (build
  fails), full unit + round-trip test coverage. PdfBox-Android is an Android
  library, so `:domain` is an Android library module without UI dependencies;
  round-trip tests run as instrumented-free JVM tests where PdfBox allows,
  otherwise as Robolectric/instrumented tests — decided per path in Phase 2.
- **`:data`** owns Room entities/DAOs/migrations and repositories exposing
  `Flow`. Knows nothing about rendering or annotation writing.

## The library boundary (the load-bearing decision)

Two engines, one seam:

| Concern | Engine | Why |
|---|---|---|
| **Read/render**: page raster, tiles, thumbnails, text boxes for selection | **PdfiumAndroid** | Fast native raster, proven tiling behavior, BSD-style licence |
| **Write/mutate**: annotations, page ops, forms, flatten | **PdfBox-Android** | Apache-2.0, writes standard PDF structures (annotation dictionaries, AcroForm values, page trees) |

The seam is a pair of `:domain`-owned interfaces:

```kotlin
interface PdfRenderSource {          // implemented over Pdfium in :app
    suspend fun pageCount(): Int
    suspend fun pageSize(index: Int): PageSize
    suspend fun renderTile(index: Int, region: RectPx, scale: Float): Bitmap
    suspend fun textBoxes(index: Int): List<TextBox>
}

interface PdfWriter {                // implemented over PdfBox in :domain
    suspend fun addAnnotations(edits: List<AnnotationEdit>): WriteResult
    suspend fun applyPageOps(ops: List<PageOp>): WriteResult
    suspend fun fillForm(values: FormValues, flatten: Boolean): WriteResult
}
```

**Consequence of the two-engine design:** after any write, the render side re-opens
the saved file (Pdfium renders what PdfBox wrote). This is deliberate — it means the
on-screen result is exactly what other viewers will see, which is also the Phase 2/4
gate criterion. In-progress (uncommitted) annotations render on the Compose overlay,
not through the PDF engine.

### MuPDF escalation path

MuPDF (AGPL) replaces a surface **only** when that surface measurably fails a budget
on the default stack, and only behind the same `:domain` interfaces — so an
escalation swaps an implementation, not the architecture. The gate report for that
phase must carry the measurements and the AGPL implication. If even MuPDF cannot hold
the budgets, we halt and report (fallback on record).

## Annotation persistence decision

Annotations are persisted as **standard PDF annotation dictionaries inside the
document** — `/Highlight`, `/Underline`, `/StrikeOut`, `/Ink`, `/Square`, `/Circle`,
`/Line`, `/Text` (sticky note), `/FreeText`, `/Stamp` — each with a generated
appearance stream (`/AP` `/N`) so every other viewer renders them identically.

- **Why not a sidecar DB as source of truth:** portability is the product promise —
  the PDF must carry its annotations to any viewer. Room stores only *metadata*
  (recents, tags, note text index for search), never the authoritative annotation.
- **Editability:** standard dictionaries round-trip — reopened annotations are parsed
  back into the domain model for move/edit/delete, then rewritten.
- Any path that cannot be expressed as a standard dictionary requires a written
  justification to ARCHITECT before implementation (per spec).

## Write-path safety (every mutation)

```
domain command → PdfBox on Dispatchers.IO → temp file
    → validity check (reopen, page count, parse; Pdfium spot-render page 1)
    → atomic replace of target ── failure at any step aborts; source untouched
```

## UI architecture

- Single-activity Compose app, Navigation-Compose. MVVM: `ViewModel` + `StateFlow`
  per screen; UI is a pure function of state.
- Viewer screen: `LazyColumn`-style virtualized page list over `PdfRenderSource`,
  tile cache keyed by (page, zoom bucket, region), Compose `Canvas` annotation
  overlay in page coordinates with a single tested screen↔page-space mapper.
- Pdfium access serialized through a dedicated single-threaded dispatcher
  (Pdfium is not thread-safe).

## Build & quality pipeline

- Gradle (Kotlin DSL), version catalog (`gradle/libs.versions.toml`).
- detekt + ktlint as failing build checks; detekt forbids `!!` and unchecked casts
  in `:domain`.
- No `INTERNET` permission; a manifest-merger check plus a dependency scan enforce
  the offline guarantee each gate.
- Release: minified, resource-shrunk, per-ABI splits, signed; size budget < 40 MB
  per ABI.

## Threading model

| Work | Where |
|---|---|
| Pdfium render calls | dedicated single-thread dispatcher |
| PdfBox writes, file I/O, Room | `Dispatchers.IO` (suspend DAOs) |
| Annotation overlay drawing | main thread (Compose), no I/O |
| Thumbnail generation | background, cached to disk |

Zero ANRs is a standing convention, not a phase goal.
