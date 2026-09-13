package app.sleepdiary.offline.domain.protocol

import java.math.BigDecimal

/** Review-only source extraction. No application/UI caller; never a prescription. */
internal object WindowRuleExtraction {
    const val REVISION = "VA_2014_WINDOW_EXTRACTION_1"
    val source = SourceLocation(
        "VA_CBTI_MANUAL_2014",
        "https://www.mirecc.va.gov/visn19/treatmentworksforvets/docs/CBT-I-Therapist-Manual.pdf",
        "dac9178a4a23c8f3e53e9f85ac05def3012fd439b2959ad1398118f8b49e2eb0"
    )
    private val minimumInitialMinutes = BigDecimal("300")
    private val lowerEfficiencyBoundary = BigDecimal("80")
    private val upperEfficiencyBoundary = BigDecimal("85")

    fun initial(input: InitialReviewInput): ExtractionResult {
        readinessIssues(input.readiness).takeIf { it.isNotEmpty() }?.let {
            return ExtractionResult.Incomplete(it)
        }
        val average = input.averageSleepMinutes
            ?: return ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_MEASUREMENT))
        if (average.signum() < 0) return ExtractionResult.Invalid(ReviewIssue.INVALID_MEASUREMENT)
        return ExtractionResult.Candidate(
            ExtractedValue.InitialMinutes(average.max(minimumInitialMinutes)),
            trace("VA.SRT.INITIAL", 43, "A2.1"),
            listOf(ReviewRequirement.INITIAL_CLINICIAN_DISCUSSION,
                ReviewRequirement.SCHEDULE_AND_APPLICABILITY_REVIEW)
        )
    }

    fun weekly(input: WeeklyReviewInput): ExtractionResult {
        readinessIssues(input.readiness).takeIf { it.isNotEmpty() }?.let {
            return ExtractionResult.Incomplete(it)
        }
        if (input.adherence != AdherenceReview.FOLLOWED_FOR_AT_LEAST_ONE_WEEK) {
            return ExtractionResult.Incomplete(listOf(
                if (input.adherence == AdherenceReview.OBSTACLES_IDENTIFIED)
                    ReviewIssue.ADHERENCE_OBSTACLES else ReviewIssue.ADHERENCE_NOT_ESTABLISHED))
        }
        val efficiency = input.sleepEfficiencyPercent
            ?: return ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_MEASUREMENT))
        val sleepNeed = input.sleepNeedQuestionnaireScore
            ?: return ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_SLEEP_NEED))
        if (efficiency < BigDecimal.ZERO || efficiency > BigDecimal("100")) {
            return ExtractionResult.Invalid(ReviewIssue.INVALID_MEASUREMENT)
        }
        // Appendix F p106: four items scored 1..5; no questionnaire is reproduced here.
        if (sleepNeed !in 4..20) return ExtractionResult.Invalid(ReviewIssue.INVALID_SLEEP_NEED)
        val (rule, delta) = when {
            efficiency >= upperEfficiencyBoundary && sleepNeed <= 9 -> "HIGH_SE.LOW_NEED" to 0
            efficiency >= upperEfficiencyBoundary && sleepNeed <= 12 -> "HIGH_SE.MID_NEED" to 15
            efficiency >= upperEfficiencyBoundary -> "HIGH_SE.HIGH_NEED" to 30
            efficiency < lowerEfficiencyBoundary && sleepNeed <= 9 -> "LOW_SE.LOW_NEED" to -15
            else -> "EXPLORE_CONTEXT" to 0
        }
        val requirements = buildList {
            add(ReviewRequirement.CLINICAL_SLEEP_NEED_JUDGMENT)
            if (efficiency < upperEfficiencyBoundary) add(ReviewRequirement.EXPLORE_CONTEXT)
            if (delta < 0) add(ReviewRequirement.REDUCTION_SAFETY_AND_LIMIT_REVIEW)
        }
        // A table delta only: never apply to a current plan or invent a weekly lower bound.
        return ExtractionResult.Candidate(ExtractedValue.WeeklyDeltaMinutes(delta),
            trace("VA.SRT.REVIEW.$rule", 48, "A2.6; Figure 6 p49; Appendix F p106"), requirements)
    }

    private fun readinessIssues(readiness: ReviewReadiness): List<ReviewIssue> = buildList {
        if (readiness.safety != SafetyReview.NO_UNRESOLVED_CONCERNS) {
            add(if (readiness.safety == SafetyReview.REQUIRES_ADAPTATION_OR_DELAY)
                ReviewIssue.SAFETY_ADAPTATION_OR_DELAY else ReviewIssue.SAFETY_NOT_REVIEWED)
        }
        if (!readiness.atLeastOneWeekOfDailyDiariesReviewed) add(ReviewIssue.BASELINE_NOT_REVIEWED)
        if (!readiness.applicabilityReviewed) add(ReviewIssue.APPLICABILITY_NOT_REVIEWED)
    }

    private fun trace(rule: String, page: Int, section: String) = ExtractionTrace(
        REVISION, rule, source, page, section, ExtractionStatus.EXTRACTED_NOT_RELEASED)
}

internal data class SourceLocation(val id: String, val url: String, val sha256: String)
internal enum class ExtractionStatus { EXTRACTED_NOT_RELEASED }
internal data class ExtractionTrace(
    val extractionRevision: String, val ruleId: String, val source: SourceLocation,
    val printedAndPdfPage: Int, val section: String, val status: ExtractionStatus)

/** Explicit review facts, never inferred from the existing V0.1 safety gate. */
internal data class ReviewReadiness(
    val safety: SafetyReview = SafetyReview.NOT_REVIEWED,
    val atLeastOneWeekOfDailyDiariesReviewed: Boolean = false,
    val applicabilityReviewed: Boolean = false)
internal enum class SafetyReview { NOT_REVIEWED, REQUIRES_ADAPTATION_OR_DELAY, NO_UNRESOLVED_CONCERNS }
internal enum class AdherenceReview { NOT_REVIEWED, OBSTACLES_IDENTIFIED, FOLLOWED_FOR_AT_LEAST_ONE_WEEK }
internal data class InitialReviewInput(
    val averageSleepMinutes: BigDecimal?, val readiness: ReviewReadiness = ReviewReadiness())
internal data class WeeklyReviewInput(
    val sleepEfficiencyPercent: BigDecimal?, val sleepNeedQuestionnaireScore: Int?,
    val adherence: AdherenceReview = AdherenceReview.NOT_REVIEWED,
    val readiness: ReviewReadiness = ReviewReadiness())
internal sealed interface ExtractedValue {
    data class InitialMinutes(val minutes: BigDecimal) : ExtractedValue
    data class WeeklyDeltaMinutes(val minutes: Int) : ExtractedValue
}
internal enum class ReviewIssue {
    SAFETY_NOT_REVIEWED, SAFETY_ADAPTATION_OR_DELAY, BASELINE_NOT_REVIEWED,
    APPLICABILITY_NOT_REVIEWED, MISSING_MEASUREMENT, INVALID_MEASUREMENT,
    MISSING_SLEEP_NEED, INVALID_SLEEP_NEED, ADHERENCE_NOT_ESTABLISHED, ADHERENCE_OBSTACLES
}
internal enum class ReviewRequirement {
    INITIAL_CLINICIAN_DISCUSSION, SCHEDULE_AND_APPLICABILITY_REVIEW,
    CLINICAL_SLEEP_NEED_JUDGMENT, EXPLORE_CONTEXT, REDUCTION_SAFETY_AND_LIMIT_REVIEW
}
internal sealed interface ExtractionResult {
    data class Candidate(val value: ExtractedValue, val trace: ExtractionTrace,
        val outstandingReview: List<ReviewRequirement>) : ExtractionResult
    data class Incomplete(val issues: List<ReviewIssue>) : ExtractionResult
    data class Invalid(val issue: ReviewIssue) : ExtractionResult
}
