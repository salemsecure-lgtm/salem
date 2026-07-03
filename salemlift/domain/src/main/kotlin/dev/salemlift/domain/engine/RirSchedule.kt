package dev.salemlift.domain.engine

import dev.salemlift.domain.model.WeekEffort

/** RIR effort schedules across accumulation (DOMAIN.md §3.1) — tunable defaults. */
public object RirSchedule {
    /** Deload effort: RIR 4 prescribed, up to 5 acceptable (DOMAIN.md §3). */
    public val deload: WeekEffort = WeekEffort(targetRir = 4)

    public fun forAccumulation(weeks: Int): List<WeekEffort> {
        require(weeks in 4..6) { "accumulation weeks must be 4..6, was $weeks" }
        return when (weeks) {
            4 ->
                listOf(
                    WeekEffort(3),
                    WeekEffort(2),
                    WeekEffort(1),
                    WeekEffort(1, allowZeroOnLastSet = true),
                )
            5 ->
                listOf(
                    WeekEffort(3),
                    WeekEffort(2),
                    WeekEffort(2),
                    WeekEffort(1),
                    WeekEffort(1, allowZeroOnLastSet = true),
                )
            else ->
                listOf(
                    WeekEffort(3),
                    WeekEffort(2),
                    WeekEffort(2),
                    WeekEffort(1),
                    WeekEffort(1),
                    WeekEffort(0),
                )
        }
    }
}
