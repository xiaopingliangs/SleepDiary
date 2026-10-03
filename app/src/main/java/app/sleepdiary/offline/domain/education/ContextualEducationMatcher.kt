package app.sleepdiary.offline.domain.education

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.domain.validation.DiaryValidator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

data class ContextualEducationSuggestion(
    val ruleId: String,
    val experienceId: String,
    val sourceEntryId: String,
    val sourceSleepDate: LocalDate,
    val estimatedSleepDurationMinutes: Int,
    val question: String
)

/** Strictly bounded matching of a neutral diary fact to one authored Education Experience. */
object ContextualEducationMatcher {
    const val RULE_ID = "RECENT_SHORT_ESTIMATED_SLEEP_TO_COGNITIVE_EXPERIENCE_1"
    const val EXPERIENCE_ID = "bad_night"
    const val RULE_WINDOW_DAYS = 7
    const val CONTENT_MATCH_LIMIT_MINUTES = 240

    fun estimatedSleepDurationMinutes(entry: SleepDiaryEntry): Int? {
        val validation = DiaryValidator.validate(entry)
        if (!validation.canSave) return null
        val tryTime = runCatching { LocalDateTime.parse(entry.trySleepAtLocal) }.getOrNull() ?: return null
        val finalWake = runCatching { LocalDateTime.parse(entry.finalAwakeningAtLocal) }.getOrNull() ?: return null
        val interval = runCatching { Duration.between(tryTime, finalWake).toMinutes() }.getOrNull() ?: return null
        if (interval < 0) return null
        val estimate = interval - entry.sleepOnsetLatencyMin.toLong() - entry.wakeAfterSleepOnsetMin.toLong()
        if (estimate < 0 || estimate > Int.MAX_VALUE) return null
        return estimate.toInt()
    }

    fun match(referenceDate: LocalDate, entries: List<SleepDiaryEntry>, enabled: Boolean = true): ContextualEducationSuggestion? {
        if (!enabled) return null
        val firstDate = referenceDate.minusDays((RULE_WINDOW_DAYS - 1).toLong())
        val latest = entries.mapNotNull { entry ->
            val date = runCatching { LocalDate.parse(entry.sleepDate) }.getOrNull() ?: return@mapNotNull null
            if (date !in firstDate..referenceDate) return@mapNotNull null
            val minutes = estimatedSleepDurationMinutes(entry) ?: return@mapNotNull null
            Triple(entry, date, minutes)
        }.maxByOrNull { it.second } ?: return null
        if (latest.third > CONTENT_MATCH_LIMIT_MINUTES) return null
        return ContextualEducationSuggestion(
            ruleId = RULE_ID,
            experienceId = EXPERIENCE_ID,
            sourceEntryId = latest.first.id,
            sourceSleepDate = latest.second,
            estimatedSleepDurationMinutes = latest.third,
            question = "一晚睡得很少，第二天真的一定会很糟吗？"
        )
    }
}
