package dev.salemlift.domain.model

/** Result of distributing a muscle's weekly sets across its sessions (DOMAIN.md §5.2). */
public data class Distribution(
    val perSession: List<Int>,
    /** True when the per-session +2 change cap kept the total below the requested target. */
    val capped: Boolean,
) {
    val total: Int get() = perSession.sum()
}
