package app.sleepdiary.offline.domain.model
import app.sleepdiary.offline.BuildConfig

/** Generated from the single version.properties source; independent of Room's technical version. */
object Versions {
    const val SPEC = BuildConfig.SPEC_VERSION
    const val LEGACY_BACKUP_FORMAT = "BACKUP_1.0"
    const val LEGACY_BACKUP_1_1 = "BACKUP_1.1"
    const val LEGACY_BACKUP_2_0 = "BACKUP_2.0"
    const val WORKSPACE_SCHEMA = BuildConfig.WORKSPACE_SCHEMA
    const val APP = BuildConfig.VERSION_NAME
    const val DIARY_SCHEMA = BuildConfig.DIARY_SCHEMA
    const val SAFETY_SCHEMA = BuildConfig.SAFETY_SCHEMA
    const val SAFETY_RULE = BuildConfig.SAFETY_RULE
    const val WEEKLY_RULE = BuildConfig.WEEKLY_RULE
    const val BACKUP_FORMAT = BuildConfig.BACKUP_FORMAT
    const val CBTI_PROTOCOL = BuildConfig.CBTI_PROTOCOL
}
enum class SafetyGate { NO_MAJOR_FLAG, CLINICIAN_REVIEW, DELAY_AUTOMATED_TREATMENT }
enum class AssessmentStatus { DRAFT, COMPLETED }
