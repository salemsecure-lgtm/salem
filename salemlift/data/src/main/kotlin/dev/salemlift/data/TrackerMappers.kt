package dev.salemlift.data

import dev.salemlift.data.db.DecisionEntity
import dev.salemlift.data.db.LandmarkEntity
import dev.salemlift.data.db.LoggedSetEntity
import dev.salemlift.data.db.MuscleFeedbackEntity
import dev.salemlift.data.db.MuscleWeekStateEntity
import dev.salemlift.data.db.PlannedSessionEntity
import dev.salemlift.data.db.SessionMuscleTargetEntity
import dev.salemlift.data.db.TrackerCodecs
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.MuscleWeekState
import dev.salemlift.domain.model.SetDecision
import dev.salemlift.domain.model.WeekEffort

// Row <-> model mappers between the tracker entities and the repository's
// contract DTOs / domain models. Pure and total in both directions.

internal fun LandmarkEntity.toLandmarks(): Landmarks = Landmarks(mev = mev, mrv = mrv, mv = mv, mav = mav)

internal fun Landmarks.toEntity(muscle: Muscle): LandmarkEntity =
    LandmarkEntity(muscle = muscle, mv = mv, mev = mev, mav = mav, mrv = mrv)

internal fun PlannedSessionEntity.toSummary(targets: List<SessionMuscleTargetEntity>): SessionSummary =
    SessionSummary(
        sessionId = id,
        mesoId = mesoId,
        week = week,
        dayIndex = dayIndex,
        name = name,
        isDeload = isDeload,
        effort = WeekEffort(targetRir = targetRir, allowZeroOnLastSet = allowZeroOnLastSet, maxRir = maxRir),
        loadMultiplier = loadMultiplier,
        state = state,
        muscleTargets = targets.associate { it.muscle to it.sets },
    )

internal fun LoggedSetEntity.toModel(): LoggedSet =
    LoggedSet(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        muscle = muscle,
        weightKg = weightKg,
        reps = reps,
        rir = rir,
        orderInSession = orderInSession,
        loggedAtEpochMillis = loggedAtEpochMillis,
    )

internal fun LoggedSet.toEntity(): LoggedSetEntity =
    LoggedSetEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        muscle = muscle,
        weightKg = weightKg,
        reps = reps,
        rir = rir,
        orderInSession = orderInSession,
        loggedAtEpochMillis = loggedAtEpochMillis,
    )

internal fun FeedbackDraft.toEntity(sessionId: Long): MuscleFeedbackEntity =
    MuscleFeedbackEntity(
        sessionId = sessionId,
        muscle = muscle,
        soreness = soreness,
        pump = pump,
        jointPain = jointPain,
        performance = performance,
    )

internal fun MuscleWeekStateEntity.toModel(): MuscleWeekState =
    MuscleWeekState(
        weeklySets = weeklySets,
        distribution = TrackerCodecs.decodeDistribution(distributionJson),
        stallCount = stallCount,
    )

internal fun MuscleWeekState.toEntity(
    mesoId: Long,
    muscle: Muscle,
): MuscleWeekStateEntity =
    MuscleWeekStateEntity(
        mesoId = mesoId,
        muscle = muscle,
        weeklySets = weeklySets,
        distributionJson = TrackerCodecs.encodeDistribution(distribution),
        stallCount = stallCount,
    )

internal fun SetDecision.toEntity(
    sessionId: Long,
    muscle: Muscle,
): DecisionEntity =
    DecisionEntity(
        sessionId = sessionId,
        muscle = muscle,
        ruleId = ruleId,
        rationale = rationale,
        rawDelta = rawDelta,
        cappedDelta = cappedDelta,
        previousSets = previousSets,
        nextSets = nextSets,
        mildPainCapped = mildPainCapped,
        clampedAt = clampedAt,
        exerciseSwapFlagged = exerciseSwapFlagged,
    )

internal fun DecisionEntity.toModel(): SetDecision =
    SetDecision(
        ruleId = ruleId,
        rationale = rationale,
        rawDelta = rawDelta,
        cappedDelta = cappedDelta,
        previousSets = previousSets,
        nextSets = nextSets,
        mildPainCapped = mildPainCapped,
        clampedAt = clampedAt,
        exerciseSwapFlagged = exerciseSwapFlagged,
    )
