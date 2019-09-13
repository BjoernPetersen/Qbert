import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.support.delegates.ProjectDelegate
import java.io.File

object BundlePlugin {
    val all = listOf(
        "spotify"
    )
}

fun ProjectDelegate.pluginJar(name: String): FileCollection {
    val fullName = "musicbot-$name"
    return fileTree(File(rootDir, "plugins")) {
        include("$fullName/build/libs/$fullName-*-all.jar")
    }
}
