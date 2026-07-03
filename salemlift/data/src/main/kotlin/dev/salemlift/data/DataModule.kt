package dev.salemlift.data

import dev.salemlift.domain.EngineInfo

/**
 * Phase 0 placeholder proving the :data → :domain wiring. Room database,
 * DAOs, repositories, and free-exercise-db seeding arrive in Phase 2/3.
 */
object DataModule {
    const val SCHEMA_VERSION: Int = 1
    val engineVersion: String = EngineInfo.VERSION
}
