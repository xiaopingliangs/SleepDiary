package app.sleepdiary.offline.domain.safety

import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.model.SafetyGate
import app.sleepdiary.offline.domain.model.SafetyGate.*
import app.sleepdiary.offline.domain.model.Versions

object SafetyGateCalculator {
    /** May calculate a partial DRAFT, notably immediately after S03. Invalid codes are never treated as NO. */
    fun calculate(answers: List<SafetyAnswer>): SafetyGate {
        require(answers.map { it.questionId }.distinct().size == answers.size)
        require(answers.map { it.assessmentId }.distinct().size <= 1)
        require(answers.all { it.safetySchemaVersion == Versions.SAFETY_SCHEMA })
        val byQuestion = answers.associateBy { it.questionId }
        require(byQuestion["S07b"] == null || byQuestion["S07"]?.answerCode == "YES")
        require(byQuestion["S07"]?.answerCode != "YES" || byQuestion["S07b"] != null) { "S07b answer required" }
        val gates = answers.map { answer ->
            val code = answer.answerCode
            val choices = when (answer.questionId) {
                "S01" -> mapOf("YES" to NO_MAJOR_FLAG, "NO" to CLINICIAN_REVIEW)
                "S03", "S04", "S12" -> mapOf("NO" to NO_MAJOR_FLAG, "YES" to DELAY_AUTOMATED_TREATMENT, "UNSURE" to DELAY_AUTOMATED_TREATMENT)
                "S02", "S05", "S06", "S10", "S15" -> mapOf("NO" to NO_MAJOR_FLAG, "YES" to CLINICIAN_REVIEW, "UNSURE" to CLINICIAN_REVIEW)
                "S07" -> mapOf("NO" to NO_MAJOR_FLAG, "YES" to NO_MAJOR_FLAG, "UNSURE" to CLINICIAN_REVIEW)
                "S07b" -> mapOf("STABLE" to CLINICIAN_REVIEW, "UNSTABLE" to DELAY_AUTOMATED_TREATMENT, "UNSURE" to DELAY_AUTOMATED_TREATMENT)
                "S08" -> mapOf("NONE" to NO_MAJOR_FLAG, "OCCASIONAL" to CLINICIAN_REVIEW, "FREQUENT" to DELAY_AUTOMATED_TREATMENT, "DANGER_OCCURRED" to DELAY_AUTOMATED_TREATMENT)
                "S09" -> {
                    S09Codec.selectedCodes(answer)
                    mapOf("NONE" to NO_MAJOR_FLAG, "UNSURE" to CLINICIAN_REVIEW, "FLAGS_PRESENT" to CLINICIAN_REVIEW)
                }
                "S11" -> mapOf("SAFE" to NO_MAJOR_FLAG, "POSSIBLY_UNSAFE" to CLINICIAN_REVIEW, "UNSURE" to CLINICIAN_REVIEW)
                "S13", "S14" -> mapOf("NO" to NO_MAJOR_FLAG, "NOT_APPLICABLE" to NO_MAJOR_FLAG, "YES" to CLINICIAN_REVIEW, "UNSURE" to CLINICIAN_REVIEW)
                else -> error("Unknown questionId: "+answer.questionId)
            }
            requireNotNull(choices[code]) { "Invalid answerCode for "+answer.questionId }
        }
        return when {
            DELAY_AUTOMATED_TREATMENT in gates -> DELAY_AUTOMATED_TREATMENT
            CLINICIAN_REVIEW in gates -> CLINICIAN_REVIEW
            else -> NO_MAJOR_FLAG
        }
    }
}
