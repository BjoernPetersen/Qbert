import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.ben-manes.versions") version Plugin.VERSIONS

    id("com.android.application")
    kotlin("android") version Plugin.KOTLIN
    kotlin("android.extensions") version Plugin.KOTLIN
    kotlin("kapt") version Plugin.KOTLIN

    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "net.bjoernpetersen.qbert"
        minSdkVersion(26)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.ext.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    packagingOptions {
        pickFirst("META-INF/*.kotlin_module")
        pickFirst("kotlinx/**/*.kotlin_metadata")
        pickFirst("org/apache/**/*.txt")
        pickFirst("META-INF/io.netty.versions.properties")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/DEPENDENCIES")
    }
    dataBinding {
        isEnabled = true
    }
}

tasks {
    create<GradleBuild>("buildPlugins") {
        description = "Builds shadowJar jars from plugins"
        dir = File(rootDir, "plugins")
        tasks = BundlePlugin.all.map { ":musicbot-$it:shadowJar" }
        inputs.files(fileTree(File(rootDir, "plugins")) {
            include("buildSrc/src/**")
            BundlePlugin.all.map {
                include("musicbot-$it/src/**")
            }
        })
        outputs.files(fileTree(File(rootDir, "plugins")) {
            BundlePlugin.all.map {
                include("musicbot-$it/build/libs")
            }
        })
    }

    withType<KotlinCompile> {
        dependsOn("buildPlugins")
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlin.Experimental",
                "-Xnew-inference"
            )
        }
    }

    create<GradleBuild>("cleanPlugins") {
        description = "Cleans projects in plugins subdirectoy"
        dir = File(rootDir, "plugins")
        tasks = listOf("clean")
    }

    clean {
        dependsOn("cleanPlugins")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.MINUTES)
}

dependencies {
    implementation(kotlin("stdlib-jdk7", version = Lib.KOTLIN))

    implementation(group = "androidx.appcompat", name = "appcompat", version = Lib.APPCOMPAT)
    implementation(group = "androidx.core", name = "core-ktx", version = Lib.ANDROIDX_CORE)
    implementation(group = "androidx.fragment", name = "fragment-ktx", version = Lib.ANDROIDX_CORE)

    // Navigation
    implementation(
        group = "androidx.navigation",
        name = "navigation-fragment-ktx",
        version = Lib.NAVIGATION
    )
    implementation(
        group = "androidx.navigation",
        name = "navigation-ui-ktx",
        version = Lib.NAVIGATION
    )

    // Lifecycles
    implementation(
        group = "androidx.lifecycle",
        name = "lifecycle-extensions",
        version = Lib.LIFECYCLE
    )
    implementation(
        group = "androidx.lifecycle",
        name = "lifecycle-common-java8",
        version = Lib.LIFECYCLE
    )
    implementation(
        group = "androidx.lifecycle",
        name = "lifecycle-viewmodel-ktx",
        version = Lib.LIFECYCLE
    )

    // Preferences
    implementation(
        group = "androidx.preference",
        name = "preference-ktx",
        version = Lib.PREFERENCE
    )

    implementation(
        group = "com.squareup.picasso",
        name = "picasso",
        version = Lib.PICASSO
    )

    implementation(
        group = "org.slf4j",
        name = "slf4j-api",
        version = Lib.SLF4J
    )
    runtimeOnly(
        group = "org.slf4j",
        name = "slf4j-android",
        version = Lib.SLF4J
    )

    implementation(
        group = "com.github.bjoernpetersen",
        name = "musicbot",
        version = Lib.MUSICBOT
    ) {
        isChanging = Lib.MUSICBOT.contains("SNAPSHOT")
        exclude("org.slf4j")
    }

    implementation(
        group = "org.sqldroid",
        name = "sqldroid",
        version = Lib.SQLITE
    )

    // Plugins
    BundlePlugin.all.forEach {
        implementation(pluginJar(it))
    }

    // Ktor
    implementation(
        group = "io.ktor",
        name = "ktor-client-okhttp",
        version = Lib.KTOR
    )

    implementation(
        group = "io.ktor",
        name = "ktor-server-cio",
        version = Lib.KTOR
    )
    implementation(
        group = "io.ktor",
        name = "ktor-locations",
        version = Lib.KTOR
    )
    implementation(
        group = "io.ktor",
        name = "ktor-jackson",
        version = Lib.KTOR
    )

    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-android",
        version = Lib.KOTLIN_COROUTINES
    )

    testImplementation(group = "junit", name = "junit", version = Lib.JUNIT)

    androidTestRuntimeOnly(
        group = "androidx.test",
        name = "runner",
        version = Lib.ANDROID_TEST_RUNNER
    )
    androidTestImplementation(
        group = "androidx.test.espresso",
        name = "espresso-core",
        version = Lib.ESPRESSO
    )
}
