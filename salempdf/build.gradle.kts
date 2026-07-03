import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        // :domain carries the strict overlay: no !! and no unchecked casts (build-failing).
        config.setFrom(
            if (project.name == "domain") {
                files(
                    rootProject.file("config/detekt/detekt.yml"),
                    rootProject.file("config/detekt/detekt-domain.yml"),
                )
            } else {
                files(rootProject.file("config/detekt/detekt.yml"))
            },
        )
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
    }

    // Quality gates run as part of every `check` (thus every build pipeline).
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(tasks.withType<Detekt>())
    }
}
