import java.util.Locale

private val UNSTABLE_KEYWORDS = listOf(
    "alpha",
    "beta"
)

fun isUnstable(version: String): Boolean {
    val lowerVersion = version.toLowerCase(Locale.US)
    return UNSTABLE_KEYWORDS.any { it in lowerVersion }
}
