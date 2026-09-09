package app.sleepdiary.offline.domain

import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.model.SafetyGate.*
import app.sleepdiary.offline.domain.safety.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk=[28])
class S09CodecTest {
    @Test fun noneAndUnsureHaveNullDetails() {
        listOf("NONE","UNSURE").forEach { c ->
            val a=S09Codec.fromSelection("a",listOf(c));assertNull(a.detailJson)
            assertEquals(if(c=="NONE") NO_MAJOR_FLAG else CLINICIAN_REVIEW,SafetyGateCalculator.calculate(listOf(a)))
        }
    }
    @Test fun eachFlagAloneIsReview() {
        S09Codec.orderedCodes.forEach { c ->
            val a=S09Codec.fromSelection("a",listOf(c))
            assertEquals("FLAGS_PRESENT",a.answerCode);assertEquals(listOf(c),S09Codec.selectedCodes(a))
            assertEquals(CLINICIAN_REVIEW,SafetyGateCalculator.calculate(listOf(a)))
        }
    }
    @Test fun selectionDeduplicatesAndOrdersDeterministically() {
        val a=S09Codec.fromSelection("a",S09Codec.orderedCodes.reversed()+"LOUD_SNORING")
        assertEquals(S09Codec.orderedCodes,S09Codec.selectedCodes(a))
        assertEquals(a,S09Codec.canonicalize(a));assertEquals(CLINICIAN_REVIEW,SafetyGateCalculator.calculate(listOf(a)))
        assertEquals("{\"selectedCodes\":[\"LOUD_SNORING\",\"WITNESSED_APNEA\",\"GASPING_AWAKENING\",\"DIAGNOSED_OSA\"]}",a.detailJson)
    }
    @Test fun exclusiveSelectionsCannotMix() {
        listOf(listOf("NONE","LOUD_SNORING"),listOf("UNSURE","LOUD_SNORING"),listOf("NONE","UNSURE"))
            .forEach { assertTrue(runCatching { S09Codec.fromSelection("a",it) }.isFailure) }
    }
    @Test fun emptyUnknownAndMixedStoredCodesFail() {
        listOf("[]","[\"UNKNOWN\"]","[\"NONE\"]","[\"UNSURE\"]","[\"LOUD_SNORING\",\"LOUD_SNORING\"]","[1]").forEach { codes ->
            val a=SafetyAnswer("a","S09","FLAGS_PRESENT",detailJson="{\"selectedCodes\":"+codes+"}")
            assertTrue(runCatching { S09Codec.selectedCodes(a) }.isFailure)
        }
    }
    @Test fun nullBranchesAndDirectFlagAnswerAreRejected() {
        listOf(SafetyAnswer("a","S09","FLAGS_PRESENT",detailJson=null),
            SafetyAnswer("a","S09","NONE",detailJson="{}"),SafetyAnswer("a","S09","UNSURE",detailJson="{}"),
            SafetyAnswer("a","S09","LOUD_SNORING",detailJson=null)).forEach {
            assertTrue(runCatching { SafetyGateCalculator.calculate(listOf(it)) }.isFailure)
        }
    }
}
