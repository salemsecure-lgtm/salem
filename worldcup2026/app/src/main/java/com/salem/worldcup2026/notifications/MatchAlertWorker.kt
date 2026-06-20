package com.salem.worldcup2026.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.salem.worldcup2026.data.model.MatchStatus
import com.salem.worldcup2026.data.repo.TournamentRepository

/**
 * Periodic background check that posts a notification for any match that is
 * currently live. Scheduled from [com.salem.worldcup2026.MainActivity] via
 * WorkManager. With a live provider configured this is where new goals would be
 * diffed against the last seen score; on bundled data it surfaces live games so
 * the notification pipeline is demonstrable end to end.
 */
class MatchAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = TournamentRepository(applicationContext)
        val data = repo.refresh().data
        val live = data.matches.filter { it.status == MatchStatus.LIVE }
        val teams = data.teams.associateBy { it.id }
        live.take(3).forEach { m ->
            val h = teams[m.homeId]?.name ?: "Home"
            val a = teams[m.awayId]?.name ?: "Away"
            MatchNotifications.show(
                applicationContext,
                id = m.id.hashCode(),
                title = "$h ${m.homeScore} - ${m.awayScore} $a",
                body = "LIVE ${m.minute}' · Group ${m.group} · ${m.venue}"
            )
        }
        return Result.success()
    }
}
