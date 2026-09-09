package app.sleepdiary.offline.domain

import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.model.SafetyGate
import app.sleepdiary.offline.domain.model.SafetyGate.*
import app.sleepdiary.offline.domain.safety.SafetyGateCalculator
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SafetyGateCalculatorTest(private val question: String, private val code: String, private val expected: SafetyGate) {
    @Test fun frozenBranch() {
        val answers=mutableListOf(SafetyAnswer("assessment", question, code, detailJson=null))
        if(question=="S07b") answers += SafetyAnswer("assessment","S07","YES",detailJson=null)
        assertEquals(expected,SafetyGateCalculator.calculate(answers))
    }
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}={1} -> {2}") fun cases(): List<Array<Any>> {
            val rows=mutableListOf<Array<Any>>()
            fun add(q:String,c:String,g:SafetyGate) { rows += arrayOf(q,c,g) }
            add("S01","YES",NO_MAJOR_FLAG); add("S01","NO",CLINICIAN_REVIEW)
            listOf("S02","S03","S04","S05","S06","S10","S12","S15").forEach { q ->
                add(q,"NO",NO_MAJOR_FLAG)
                listOf("YES","UNSURE").forEach { add(q,it,if(q in listOf("S03","S04","S12")) DELAY_AUTOMATED_TREATMENT else CLINICIAN_REVIEW) }
            }
            add("S07","NO",NO_MAJOR_FLAG);add("S07","UNSURE",CLINICIAN_REVIEW)
            add("S07b","STABLE",CLINICIAN_REVIEW);add("S07b","UNSTABLE",DELAY_AUTOMATED_TREATMENT);add("S07b","UNSURE",DELAY_AUTOMATED_TREATMENT)
            add("S08","NONE",NO_MAJOR_FLAG);add("S08","OCCASIONAL",CLINICIAN_REVIEW)
            add("S08","FREQUENT",DELAY_AUTOMATED_TREATMENT);add("S08","DANGER_OCCURRED",DELAY_AUTOMATED_TREATMENT)
            add("S09","NONE",NO_MAJOR_FLAG);add("S09","UNSURE",CLINICIAN_REVIEW)
            add("S11","SAFE",NO_MAJOR_FLAG);add("S11","POSSIBLY_UNSAFE",CLINICIAN_REVIEW);add("S11","UNSURE",CLINICIAN_REVIEW)
            listOf("S13","S14").forEach { q ->
                listOf("NO","NOT_APPLICABLE").forEach { add(q,it,NO_MAJOR_FLAG) }
                listOf("YES","UNSURE").forEach { add(q,it,CLINICIAN_REVIEW) }
            }
            return rows
        }
    }
}
