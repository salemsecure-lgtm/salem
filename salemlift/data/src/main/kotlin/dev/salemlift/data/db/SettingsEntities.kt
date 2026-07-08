package dev.salemlift.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * A user-tuned delta for one Fixed-delta autoregulation rule (DOMAIN.md §5:
 * the rule table is data; conditions stay fixed in v1, only deltas are
 * tunable). Absence of a row means the shipped default applies.
 */
@Serializable
@Entity(tableName = "rule_override")
data class RuleOverrideEntity(
    @PrimaryKey val ruleId: String,
    val delta: Int,
)

/**
 * Single-row app preference (id is always [RestPrefEntity.SINGLETON_ID]):
 * the rest timer's default duration in seconds. Absence of the row means the
 * shipped default of [dev.salemlift.data.settings.SettingsRepository.DEFAULT_REST_SECONDS].
 */
@Serializable
@Entity(tableName = "rest_pref")
data class RestPrefEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val restSeconds: Int,
) {
    companion object {
        const val SINGLETON_ID: Int = 1
    }
}
