package dev.salemlift.domain.program

import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SessionTemplate
import dev.salemlift.domain.model.Split

/**
 * Shipped split templates (SPEC §3). Every template trains all 14 muscles so
 * no muscle silently drops out of a mesocycle; users can customize freely via
 * [custom]. Muscle order within a session is training order (compounds first).
 */
public object SplitTemplates {
    private val push =
        listOf(Muscle.CHEST, Muscle.FRONT_DELTS, Muscle.SIDE_DELTS, Muscle.TRICEPS)
    private val pull =
        listOf(Muscle.BACK, Muscle.TRAPS, Muscle.REAR_DELTS, Muscle.BICEPS, Muscle.FOREARMS)
    private val legs =
        listOf(Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.CALVES, Muscle.ABS)
    private val upper =
        listOf(
            Muscle.CHEST,
            Muscle.BACK,
            Muscle.FRONT_DELTS,
            Muscle.SIDE_DELTS,
            Muscle.REAR_DELTS,
            Muscle.TRAPS,
            Muscle.BICEPS,
            Muscle.TRICEPS,
            Muscle.FOREARMS,
        )
    private val lower =
        listOf(Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.CALVES, Muscle.ABS)
    private val fullBody =
        listOf(
            Muscle.QUADS,
            Muscle.CHEST,
            Muscle.BACK,
            Muscle.HAMSTRINGS,
            Muscle.GLUTES,
            Muscle.FRONT_DELTS,
            Muscle.SIDE_DELTS,
            Muscle.REAR_DELTS,
            Muscle.TRAPS,
            Muscle.BICEPS,
            Muscle.TRICEPS,
            Muscle.FOREARMS,
            Muscle.CALVES,
            Muscle.ABS,
        )

    public val pplThreeDay: Split =
        Split(
            name = "Push / Pull / Legs (3-day)",
            sessions =
                listOf(
                    SessionTemplate("Push", push),
                    SessionTemplate("Pull", pull),
                    SessionTemplate("Legs", legs),
                ),
        )

    public val pplSixDay: Split =
        Split(
            name = "Push / Pull / Legs (6-day)",
            sessions =
                listOf(
                    SessionTemplate("Push A", push),
                    SessionTemplate("Pull A", pull),
                    SessionTemplate("Legs A", legs),
                    SessionTemplate("Push B", push),
                    SessionTemplate("Pull B", pull),
                    SessionTemplate("Legs B", legs),
                ),
        )

    public val upperLowerFourDay: Split =
        Split(
            name = "Upper / Lower (4-day)",
            sessions =
                listOf(
                    SessionTemplate("Upper A", upper),
                    SessionTemplate("Lower A", lower),
                    SessionTemplate("Upper B", upper),
                    SessionTemplate("Lower B", lower),
                ),
        )

    public val fullBodyThreeDay: Split =
        Split(
            name = "Full Body (3-day)",
            sessions =
                listOf(
                    SessionTemplate("Full Body A", fullBody),
                    SessionTemplate("Full Body B", fullBody),
                    SessionTemplate("Full Body C", fullBody),
                ),
        )

    public val all: List<Split> =
        listOf(pplThreeDay, pplSixDay, upperLowerFourDay, fullBodyThreeDay)

    /** A user-defined split; validation lives in the Split/SessionTemplate models. */
    public fun custom(
        name: String,
        sessions: List<SessionTemplate>,
    ): Split = Split(name, sessions)
}
