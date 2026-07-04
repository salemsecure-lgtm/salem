package dev.salemlift.app.navigation

object Routes {
    const val HOME = "home"
    const val ANALYTICS = "analytics"
    const val RUNNER = "runner/{sessionId}"
    const val FEEDBACK = "feedback/{sessionId}"
    const val SUMMARY = "summary/{sessionId}"
    const val SESSION_ID_ARG = "sessionId"

    fun runner(sessionId: Long) = "runner/$sessionId"

    fun feedback(sessionId: Long) = "feedback/$sessionId"

    fun summary(sessionId: Long) = "summary/$sessionId"
}
