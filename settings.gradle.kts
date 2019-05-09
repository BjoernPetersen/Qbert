pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" ->
                    useModule("com.android.tools.build:gradle:${Plugin.ANDROID}")
                "androidx.navigation.safeargs.kotlin" ->
                    useModule("androidx.navigation:navigation-safe-args-gradle-plugin:${Plugin.SAFE_ARGS}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
}

include(":app")
