package app.sleepdiary.offline.domain.protocol

import app.sleepdiary.offline.domain.model.Versions
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class WindowRuleExtractionTest {
    private val reviewed = ReviewReadiness(SafetyReview.NO_UNRESOLVED_CONCERNS, true, true)
    private fun weekly(se: String?, need: Int?,
        adherence: AdherenceReview = AdherenceReview.FOLLOWED_FOR_AT_LEAST_ONE_WEEK,
        readiness: ReviewReadiness = reviewed) = WindowRuleExtraction.weekly(
            WeeklyReviewInput(se?.let(::BigDecimal), need, adherence, readiness))
    private fun delta(se: String, need: Int): Int =
        ((weekly(se, need) as ExtractionResult.Candidate).value as ExtractedValue.WeeklyDeltaMinutes).minutes

    @Test fun initialPreservesSourceExample() {
        val r = WindowRuleExtraction.initial(InitialReviewInput(BigDecimal("400"), reviewed)) as ExtractionResult.Candidate
        assertEquals(ExtractedValue.InitialMinutes(BigDecimal("400")), r.value)
        assertEquals(43, r.trace.printedAndPdfPage)
        assertTrue(ReviewRequirement.INITIAL_CLINICIAN_DISCUSSION in r.outstandingReview)
    }
    @Test fun initialFloorIncludesExactBoundary() {
        listOf("0", "180", "299.999", "300").forEach {
            val r = WindowRuleExtraction.initial(InitialReviewInput(BigDecimal(it), reviewed)) as ExtractionResult.Candidate
            assertEquals(0, (r.value as ExtractedValue.InitialMinutes).minutes.compareTo(BigDecimal("300")))
        }
    }
    @Test fun initialDoesNotRoundToPickerStep() {
        val r = WindowRuleExtraction.initial(InitialReviewInput(BigDecimal("400.125"), reviewed)) as ExtractionResult.Candidate
        assertEquals(ExtractedValue.InitialMinutes(BigDecimal("400.125")), r.value)
    }
    @Test fun negativeInitialIsNotCorrected() {
        assertEquals(ExtractionResult.Invalid(ReviewIssue.INVALID_MEASUREMENT),
            WindowRuleExtraction.initial(InitialReviewInput(BigDecimal("-1"), reviewed)))
    }
    @Test fun missingInitialIsNotZero() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_MEASUREMENT)),
            WindowRuleExtraction.initial(InitialReviewInput(null, reviewed)))
    }
    @Test fun defaultReadinessCannotYieldCandidate() {
        val r = WindowRuleExtraction.initial(InitialReviewInput(BigDecimal("400"))) as ExtractionResult.Incomplete
        assertEquals(listOf(ReviewIssue.SAFETY_NOT_REVIEWED, ReviewIssue.BASELINE_NOT_REVIEWED,
            ReviewIssue.APPLICABILITY_NOT_REVIEWED), r.issues)
    }
    @Test fun safetyConcernPrecedesFavorableMeasurements() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.SAFETY_ADAPTATION_OR_DELAY)),
            weekly("95", 15, readiness = reviewed.copy(safety = SafetyReview.REQUIRES_ADAPTATION_OR_DELAY)))
    }
    @Test fun unreviewedBaselineBlocksWeeklyCandidate() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.BASELINE_NOT_REVIEWED)),
            weekly("90", 15, readiness = reviewed.copy(atLeastOneWeekOfDailyDiariesReviewed = false)))
    }
    @Test fun obstaclesDoNotBecomeKeepPlanSuccess() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.ADHERENCE_OBSTACLES)),
            weekly("90", 15, AdherenceReview.OBSTACLES_IDENTIFIED))
    }
    @Test fun unknownAdherenceIsNotAssumed() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.ADHERENCE_NOT_ESTABLISHED)),
            weekly("90", 15, AdherenceReview.NOT_REVIEWED))
    }
    @Test fun highEfficiencyLowNeedKeepsTableDeltaZero() { assertEquals(0, delta("85", 9)) }
    @Test fun midNeedBoundariesIncreaseFifteen() { listOf(10, 12).forEach { assertEquals(15, delta("85", it)) } }
    @Test fun highNeedBoundariesIncreaseThirty() { listOf(13, 20).forEach { assertEquals(30, delta("85", it)) } }
    @Test fun strictlyBelowEightyAndLowNeedReducesFifteen() { assertEquals(-15, delta("79.999999", 9)) }
    @Test fun exactlyEightyDoesNotReduce() { assertEquals(0, delta("80", 9)) }
    @Test fun justBelowEightyFiveDoesNotIncrease() { assertEquals(0, delta("84.999999", 13)) }
    @Test fun lowEfficiencyWithHigherNeedDoesNotReduce() {
        listOf(10, 12, 13, 20).forEach { assertEquals(0, delta("79", it)) }
    }
    @Test fun fullScoreRangeAndEfficiencyPartitionsMatchTable() {
        val highRow = listOf(0,0,0,0,0,0,15,15,15,30,30,30,30,30,30,30,30)
        val lowRow = listOf(-15,-15,-15,-15,-15,-15,0,0,0,0,0,0,0,0,0,0,0)
        for (need in 4..20) {
            assertEquals(highRow[need - 4], delta("90", need))
            assertEquals(0, delta("82", need))
            assertEquals(lowRow[need - 4], delta("70", need))
        }
    }
    @Test fun missingMeasurementsAndSleepNeedAreDistinct() {
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_MEASUREMENT)), weekly(null, 10))
        assertEquals(ExtractionResult.Incomplete(listOf(ReviewIssue.MISSING_SLEEP_NEED)), weekly("85", null))
    }
    @Test fun outOfRangeScoresAreRejected() {
        listOf(Int.MIN_VALUE, 0, 3, 21, Int.MAX_VALUE).forEach {
            assertEquals(ExtractionResult.Invalid(ReviewIssue.INVALID_SLEEP_NEED), weekly("85", it))
        }
    }
    @Test fun outOfRangeEfficiencyIsRejectedWithoutClamping() {
        listOf("-0.001", "100.001").forEach {
            assertEquals(ExtractionResult.Invalid(ReviewIssue.INVALID_MEASUREMENT), weekly(it, 10))
        }
    }
    @Test fun zeroAndHundredRemainDefinedBoundaries() { assertEquals(-15, delta("0", 4)); assertEquals(30, delta("100", 20)) }
    @Test fun contextAndClinicalJudgmentAreNotLostInZeroDelta() {
        val r = weekly("82", 10) as ExtractionResult.Candidate
        assertTrue(ReviewRequirement.EXPLORE_CONTEXT in r.outstandingReview)
        assertTrue(ReviewRequirement.CLINICAL_SLEEP_NEED_JUDGMENT in r.outstandingReview)
    }
    @Test fun reductionDoesNotInventWeeklyFloorOrPrescribeWindow() {
        val r = weekly("70", 8) as ExtractionResult.Candidate
        assertTrue(ReviewRequirement.REDUCTION_SAFETY_AND_LIMIT_REVIEW in r.outstandingReview)
        assertTrue(r.value is ExtractedValue.WeeklyDeltaMinutes)
    }
    @Test fun traceIsDeterministicAndNotReleased() {
        val r = weekly("90", 12) as ExtractionResult.Candidate
        assertEquals(r, weekly("90", 12))
        assertEquals(ExtractionStatus.EXTRACTED_NOT_RELEASED, r.trace.status)
        assertEquals(48, r.trace.printedAndPdfPage)
        assertEquals(64, r.trace.source.sha256.length)
        assertEquals("NONE", Versions.CBTI_PROTOCOL)
    }
}
