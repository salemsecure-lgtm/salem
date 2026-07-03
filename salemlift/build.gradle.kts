import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint) apply false
}

tasks.wrapper {
    // The URL-validation probe fails behind redirecting proxies; the actual
    // distribution download still verifies its checksum.
    validateDistributionUrl = false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        if (name == "domain") {
            // The engine module bans !! and unchecked casts outright.
            config.setFrom(
                rootProject.file("config/detekt/detekt.yml"),
                rootProject.file("config/detekt/detekt-domain.yml"),
            )
        }
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
    }
}
