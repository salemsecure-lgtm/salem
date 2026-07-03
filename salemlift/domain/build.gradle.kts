plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}

// :domain is the pure auto-regulation engine. It must never gain an Android
// dependency — only kotlin stdlib and coroutines-core are permitted here.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    explicitApi()
}

dependencies {
    // Intentionally stdlib-only: the engine is pure synchronous functions.
    // (coroutines-core gets added only if/when a Flow-based API is needed.)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

// SPEC §6 / Phase 1 gate: 100% branch coverage of decision code. The filter
// targets the decision packages; pure data carriers live in .model (their
// compiler-generated data-class methods carry no decision branches).
kover {
    reports {
        filters {
            includes {
                classes(
                    "dev.salemlift.domain.engine.*",
                    "dev.salemlift.domain.program.*",
                    "dev.salemlift.domain.EngineInfo*",
                )
            }
        }
        verify {
            rule("engine decision branch coverage") {
                bound {
                    coverageUnits.set(kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
                    aggregationForGroup.set(kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                    minValue.set(100)
                }
            }
        }
    }
}
