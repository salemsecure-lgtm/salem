package dev.salemlift.domain.model

/** A session's per-muscle hard-set prescription within one program week. */
public data class PlannedSession(
    val name: String,
    /** Sets per muscle this session; zero-set muscles are omitted. */
    val muscleSets: Map<Muscle, Int>,
)

/** One week of an instantiated program. */
public data class ProgramWeek(
    val week: Int,
    val isDeload: Boolean,
    val effort: WeekEffort,
    val loadMultiplier: Double,
    val sessions: List<PlannedSession>,
)

/** A full instantiated mesocycle program: a split crossed with a MesoPlan. */
public data class ProgramPlan(
    val split: Split,
    val config: MesoConfig,
    val weeks: List<ProgramWeek>,
)
