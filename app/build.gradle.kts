import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
val domainVersions = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
android {
    namespace = "app.sleepdiary.offline"
    compileSdk = 37
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "app.sleepdiary.offline"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = domainVersions.getProperty("appVersion")
        mapOf(
            "DIARY_SCHEMA" to "diarySchemaVersion",
            "SAFETY_SCHEMA" to "safetySchemaVersion",
            "SAFETY_RULE" to "safetyRuleVersion",
            "WEEKLY_RULE" to "weeklySummaryRuleVersion",
            "BACKUP_FORMAT" to "backupFormatVersion",
            "CBTI_PROTOCOL" to "cbtiProtocolVersion"
        ).forEach { (field, key) ->
            buildConfigField("String", field, "\"" + domainVersions.getProperty(key) + "\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true; buildConfig = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
room { schemaDirectory("$projectDir/schemas") }
dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.coroutines.core)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)
}
tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
}
