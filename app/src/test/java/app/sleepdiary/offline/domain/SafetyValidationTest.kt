package app.sleepdiary.offline.domain

import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.model.SafetyGate.*
import app.sleepdiary.offline.domain.safety.SafetyGateCalculator
import org.junit.Test
import org.junit.Assert.*

class SafetyValidationTest {
    private fun a(q:String,c:String)=SafetyAnswer("a",q,c,detailJson=null)
    @Test fun priorityIsIndependentOfAnswerOrder() {
        val answers=listOf(a("S01","YES"),a("S05","YES"),a("S03","UNSURE"))
        assertEquals(DELAY_AUTOMATED_TREATMENT,SafetyGateCalculator.calculate(answers))
        assertEquals(DELAY_AUTOMATED_TREATMENT,SafetyGateCalculator.calculate(answers.reversed()))
    }
    @Test fun partialS03DraftDoesNotRequireRemainingQuestions() {
        listOf("YES","UNSURE").forEach { assertEquals(DELAY_AUTOMATED_TREATMENT,SafetyGateCalculator.calculate(listOf(a("S03",it)))) }
    }
    @Test fun invalidInputsAreNotSilentlySafe() {
        listOf(listOf(a("S01","UNSURE")),listOf(a("S99","NO")),listOf(a("S05","NO"),a("S05","YES")),
            listOf(a("S07b","STABLE")),listOf(a("S07","NO"),a("S07b","STABLE")),listOf(a("S07","YES")))
            .forEach { assertTrue(runCatching { SafetyGateCalculator.calculate(it) }.isFailure) }
    }
}
