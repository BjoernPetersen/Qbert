allprojects {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent {
                snapshotsOnly()
            }
        }
        jcenter()
        google()
    }
}
