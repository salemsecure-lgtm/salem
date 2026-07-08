import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing (ARCHITECTURE §5): credentials come from keystore.properties
// (git-ignored). Without it, a local dev keystore path with well-known dev
// credentials is used — fine for personal sideloading; see docs/RELEASE.md to
// generate your own. Neither keystore file is ever committed.
val keystoreProps =
    Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
val releaseStoreFile = rootProject.file(keystoreProps.getProperty("storeFile") ?: "release.keystore")

android {
    namespace = "dev.salemlift.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.salemlift"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = keystoreProps.getProperty("storePassword") ?: "salemlift-dev"
            keyAlias = keystoreProps.getProperty("keyAlias") ?: "salemlift"
            keyPassword = keystoreProps.getProperty("keyPassword") ?: "salemlift-dev"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.vico.compose.m3)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// Offline verification (SPEC §7, ARCHITECTURE §5): the release build must
// request no network permission and carry no networking client. Wired into
// `check` so every full build enforces it.
// ---------------------------------------------------------------------------

val bannedNetworkPermissions =
    listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE")
val bannedNetworkArtifacts =
    listOf("okhttp", "retrofit", "ktor-client", "volley", "grpc", "cronet", "apache-http")

val verifyOfflineClasspath =
    tasks.register("verifyOfflineClasspath") {
        val artifactIds =
            configurations
                .getByName("releaseRuntimeClasspath")
                .incoming
                .resolutionResult
                .rootComponent
                .map { root -> root.dependencies.mapNotNull { it.requested.displayName } }
        doLast {
            val offenders =
                artifactIds.get().filter { id -> bannedNetworkArtifacts.any { it in id.lowercase() } }
            check(offenders.isEmpty()) {
                "verifyOffline: networking artifacts on releaseRuntimeClasspath: $offenders"
            }
        }
    }

val verifyOfflineManifest =
    tasks.register("verifyOfflineManifest") {
        dependsOn("processReleaseManifest")
        val manifestDir = layout.buildDirectory.dir("intermediates/merged_manifests/release")
        doLast {
            val manifest =
                checkNotNull(
                    manifestDir
                        .get()
                        .asFile
                        .walkTopDown()
                        .firstOrNull { it.name == "AndroidManifest.xml" },
                ) { "verifyOffline: merged release manifest not found" }
            val text = manifest.readText()
            val offenders = bannedNetworkPermissions.filter { it in text }
            check(offenders.isEmpty()) {
                "verifyOffline: release manifest requests network permissions: $offenders"
            }
        }
    }

tasks.register("verifyOffline") {
    group = "verification"
    description = "Fails when the release build requests network permissions or bundles a networking client."
    dependsOn(verifyOfflineManifest, verifyOfflineClasspath)
}

tasks.named("check") {
    dependsOn("verifyOffline")
}
