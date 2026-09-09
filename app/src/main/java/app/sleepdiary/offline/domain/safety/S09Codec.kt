package app.sleepdiary.offline.domain.safety

import app.sleepdiary.offline.data.entity.SafetyAnswer
import org.json.JSONArray
import org.json.JSONObject

/** Uses the platform JSON implementation; no network or new runtime dependency. */
object S09Codec {
    val orderedCodes: List<String> = listOf("LOUD_SNORING", "WITNESSED_APNEA", "GASPING_AWAKENING", "DIAGNOSED_OSA")

    fun fromSelection(assessmentId: String, selections: Collection<String>): SafetyAnswer {
        require(selections.isNotEmpty())
        if (selections.any { it == "NONE" || it == "UNSURE" }) {
            require(selections.size == 1) { "Exclusive S09 choice" }
            return SafetyAnswer(assessmentId, "S09", selections.single(), detailJson = null)
        }
        require(selections.all { it in orderedCodes }) { "Unknown S09 code" }
        val codes = orderedCodes.filter { it in selections }
        return SafetyAnswer(assessmentId, "S09", "FLAGS_PRESENT", detailJson =
            JSONObject().put("selectedCodes", JSONArray(codes)).toString())
    }

    fun selectedCodes(answer: SafetyAnswer): List<String> {
        require(answer.questionId == "S09")
        if (answer.answerCode in listOf("NONE", "UNSURE")) {
            require(answer.detailJson == null)
            return emptyList()
        }
        require(answer.answerCode == "FLAGS_PRESENT" && answer.detailJson != null)
        val obj = JSONObject(answer.detailJson)
        require(obj.length() == 1 && obj.has("selectedCodes"))
        val array = obj.getJSONArray("selectedCodes")
        val codes = (0 until array.length()).map {
            val value = array.get(it)
            require(value is String && value in orderedCodes) { "Unknown S09 code" }
            value
        }
        require(codes.isNotEmpty() && codes.distinct().size == codes.size)
        return orderedCodes.filter { it in codes }
    }

    fun canonicalize(answer: SafetyAnswer): SafetyAnswer {
        val codes = selectedCodes(answer)
        return if (answer.answerCode == "FLAGS_PRESENT") answer.copy(detailJson = fromSelection(answer.assessmentId, codes).detailJson) else answer
    }
}
