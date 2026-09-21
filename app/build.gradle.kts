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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 37
        versionCode = 4
        versionName = domainVersions.getProperty("appVersion")
        mapOf(
            "SPEC_VERSION" to "specVersion",
            "DIARY_SCHEMA" to "diarySchemaVersion",
            "SAFETY_SCHEMA" to "safetySchemaVersion",
            "SAFETY_RULE" to "safetyRuleVersion",
            "WEEKLY_RULE" to "weeklySummaryRuleVersion",
            "BACKUP_FORMAT" to "backupFormatVersion",
            "CBTI_PROTOCOL" to "cbtiProtocolVersion",
            "WORKSPACE_SCHEMA" to "workspaceSchemaVersion",
            "TREATMENT_DATA_SCHEMA" to "treatmentDataSchemaVersion"
        ).forEach { (field, key) ->
            buildConfigField("String", field, "\"" + domainVersions.getProperty(key) + "\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true; buildConfig = true }
    sourceSets.getByName("main").assets.srcDir(rootProject.file("schemas"))
    testOptions { unitTests.isIncludeAndroidResources = true }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
room { schemaDirectory("$projectDir/schemas") }
dependencies {
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(platform(libs.compose.bom))
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.coroutines.core)
    ksp(libs.room.compiler)
    testImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)
}
tasks.withType<Test>().configureEach {
    inputs.dir(rootProject.file("samples"))
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
}
