package dev.salemlift.app.analytics

import dev.salemlift.data.analytics.AnalyticsRepository
import dev.salemlift.data.analytics.E1rmPoint
import dev.salemlift.data.analytics.ExerciseRef
import dev.salemlift.data.analytics.FatigueSummary
import dev.salemlift.data.analytics.MesoProgress
import dev.salemlift.data.analytics.WeekTonnage
import dev.salemlift.data.analytics.WeekVolume
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle

/** In-memory [AnalyticsRepository] for ViewModel unit tests. */
class FakeAnalyticsRepository : AnalyticsRepository {
    var mesoId: Long? = null
    var weeklyVolume: Map<Muscle, List<WeekVolume>> = emptyMap()
    var landmarks: Map<Muscle, Landmarks> = emptyMap()
    var exercises: List<ExerciseRef> = emptyList()
    var trends: Map<String, List<E1rmPoint>> = emptyMap()
    var tonnage: List<WeekTonnage> = emptyList()
    var progress: MesoProgress? = null
    var fatigue: FatigueSummary = FatigueSummary(weeks = emptyList(), ruleFires = emptyList())

    /** Every e1rmTrend call, in order — asserts the ViewModel's trend cache. */
    val trendRequests = mutableListOf<String>()

    override suspend fun activeMesoId(): Long? = mesoId

    override suspend fun weeklyVolume(mesoId: Long): Map<Muscle, List<WeekVolume>> = weeklyVolume

    override suspend fun landmarks(): Map<Muscle, Landmarks> = landmarks

    override suspend fun e1rmTrend(
        exerciseId: String,
        mesoId: Long,
    ): List<E1rmPoint> {
        trendRequests += exerciseId
        return trends[exerciseId].orEmpty()
    }

    override suspend fun exercisesWithHistory(): List<ExerciseRef> = exercises

    override suspend fun tonnage(mesoId: Long): List<WeekTonnage> = tonnage

    override suspend fun mesoProgress(): MesoProgress? = progress

    override suspend fun fatigue(mesoId: Long): FatigueSummary = fatigue
}
