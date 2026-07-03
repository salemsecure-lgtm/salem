package dev.salemlift.domain.model

/**
 * One training day of a split: the muscles it trains, in training order
 * (order matters — new sets are added freshest-first per DOMAIN.md §5.2).
 */
public data class SessionTemplate(
    val name: String,
    val muscles: List<Muscle>,
) {
    init {
        require(name.isNotBlank()) { "session name must not be blank" }
        require(muscles.isNotEmpty()) { "a session must train at least one muscle" }
        require(muscles.size == muscles.toSet().size) { "session muscles must be distinct" }
    }
}

/** A weekly split: an ordered list of training days. */
public data class Split(
    val name: String,
    val sessions: List<SessionTemplate>,
) {
    init {
        require(name.isNotBlank()) { "split name must not be blank" }
        require(sessions.isNotEmpty()) { "a split needs at least one session" }
    }

    /** All muscles trained anywhere in the split. */
    val trainedMuscles: Set<Muscle> get() = sessions.flatMap { it.muscles }.toSet()
}
