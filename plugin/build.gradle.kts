import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.nio.file.Files

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.kezong.fat-aar")
}

// TODO: Update value to your plugin's name.
val pluginName = "GodotTapTapSDK"
var tapVersion = "4.4.3"
// TODO: Update value to match your plugin's package name.
val pluginPackageName = "com.sakuya.godot_taptap"
val adnFileName = "TapAD_3.16.3.37.aar"

android {
    namespace = pluginPackageName
    compileSdk = 33

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 21

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.godotengine:godot:4.2.0.stable")

    compileOnly(files("libs/$adnFileName"))
    compileOnly("com.taptap.sdk:tap-core:$tapVersion")
    compileOnly("com.taptap.sdk:tap-kit:$tapVersion")
    compileOnly("com.taptap.sdk:tap-login:$tapVersion") // 登录
    compileOnly("com.taptap.sdk:tap-moment:$tapVersion") // 内嵌动态
    compileOnly("com.taptap.sdk:tap-compliance:$tapVersion") // 实名认证和防沉迷

    /*
    compileOnly("cn.leancloud:storage-android:8.2.19")
    compileOnly("cn.leancloud:realtime-android:8.2.19")
    compileOnly("com.squareup.okhttp3:okhttp:3.12.1")
//    compileOnly("com.android.support:appcompat-v7:28.0.0")
    compileOnly ("com.android.support:support-annotations:28.0.0")
//    compileOnly ("com.android.support:support-v4:28.0.0")
    compileOnly ("com.github.bumptech.glide:glide:4.9.0")
//    compileOnly("com.android.support:recyclerview-v7:28.0.0")
     */
}

// BUILD TASKS DEFINITION
val copyDebugAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated debug AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("demo/addons/$pluginName/bin/debug")
}

val copyReleaseAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("demo/addons/$pluginName/bin/release")
}

val copyLibsAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the plugin's addons directory"
    from("libs")
    include(adnFileName)
    into("demo/addons/$pluginName/bin")
}

val cleanDemoAddons by tasks.registering(Delete::class) {
    delete("demo/addons/$pluginName")
}

tasks.register("downloadADNLib") {
    val path = "libs/$adnFileName"
    val sourceUrl = "https://tapad-platform.tapimg.com/sdk/$adnFileName"
    val destFile = File(path)
    mkdir("libs")
    ant.invokeMethod("get", mapOf("src" to sourceUrl, "dest" to destFile))
}

val copyAddonsToDemo by tasks.registering(Copy::class) {
    description = "Copies the export scripts templates to the plugin's addons directory"

    dependsOn(cleanDemoAddons)
    finalizedBy(copyDebugAARToDemoAddons)
    finalizedBy(copyReleaseAARToDemoAddons)
    finalizedBy(copyLibsAARToDemoAddons)

    from("export_scripts_template")
    into("demo/addons/$pluginName")
}

tasks.preBuild {
    dependsOn("downloadADNLib")
}

tasks.named("assemble").configure {

    finalizedBy(copyAddonsToDemo)
}

tasks.named<Delete>("clean").apply {
    dependsOn(cleanDemoAddons)
}
