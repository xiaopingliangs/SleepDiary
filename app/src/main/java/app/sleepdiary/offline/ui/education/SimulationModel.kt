package app.sleepdiary.offline.ui.education

import org.json.JSONArray
import org.json.JSONObject

internal data class SimulationMeter(
    val id: String,
    val label: String,
    val tone: String,
    val initial: Int
)

internal data class SimulationFeedback(
    val perspective: String,
    val plain: String,
    val adjustment: String
)

internal data class SimulationChoice(
    val key: String,
    val label: String,
    val tone: String,
    val toneLabel: String,
    val deltas: Map<String, Int>,
    val outcomeId: String?,
    val feedback: SimulationFeedback
)

internal data class SimulationStage(
    val id: String,
    val prompt: String,
    val hint: String,
    val choices: List<SimulationChoice>
)

internal data class SimulationOutcome(
    val id: String,
    val text: String,
    val conclusion: String
)

internal data class SimulationSpec(
    val id: String,
    val title: String,
    val intro: String,
    val meters: List<SimulationMeter>,
    val stages: List<SimulationStage>,
    val outcomes: List<SimulationOutcome>
)

internal fun buildSimulationSpec(
    scene: PublicEducationNode,
    catalog: PublicEducationCatalog
): SimulationSpec {
    val children = catalog.childrenInManifestOrder(scene.id)
    return SimulationSpec(
        id = scene.id,
        title = scene.title,
        intro = scene.payload.optString("intro"),
        meters = children.filter { it.kind == "SIM_METER" }.map(::meterFrom),
        stages = children.filter { it.kind == "SIM_STAGE" }.map(::stageFrom),
        outcomes = children.filter { it.kind == "SIM_OUTCOME" }.map(::outcomeFrom)
    )
}

private fun meterFrom(node: PublicEducationNode) = SimulationMeter(
    id = node.payload.optString("id").ifBlank { node.id },
    label = node.payload.optString("label").ifBlank { node.title },
    tone = node.payload.optString("tone"),
    initial = node.payload.optInt("initial", 2).coerceIn(0, 4)
)

private fun stageFrom(node: PublicEducationNode) = SimulationStage(
    id = node.payload.optString("id").ifBlank { node.id },
    prompt = node.payload.optString("prompt").ifBlank { node.title },
    hint = node.payload.optString("hint"),
    choices = choicesFrom(node.payload.optJSONArray("choices"))
)

private fun outcomeFrom(node: PublicEducationNode) = SimulationOutcome(
    id = node.payload.optString("id").ifBlank { node.id },
    text = node.payload.optString("text"),
    conclusion = node.payload.optString("conclusion")
)

private fun choicesFrom(array: JSONArray?): List<SimulationChoice> {
    if (array == null) return emptyList()
    val result = ArrayList<SimulationChoice>(array.length())
    for (i in 0 until array.length()) {
        val item = array.optJSONObject(i) ?: continue
        val feedback = item.optJSONObject("feedback")
        result += SimulationChoice(
            key = item.optString("key"),
            label = item.optString("label"),
            tone = item.optString("tone"),
            toneLabel = item.optString("toneLabel"),
            deltas = deltasFrom(item.optJSONObject("deltas")),
            outcomeId = item.optString("outcome").takeIf { it.isNotBlank() },
            feedback = SimulationFeedback(
                perspective = feedback?.optString("perspective").orEmpty(),
                plain = feedback?.optString("plain").orEmpty(),
                adjustment = feedback?.optString("adjustment").orEmpty()
            )
        )
    }
    return result
}

private fun deltasFrom(json: JSONObject?): Map<String, Int> {
    if (json == null) return emptyMap()
    val result = LinkedHashMap<String, Int>()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        result[key] = json.optInt(key)
    }
    return result
}

internal fun applySimulationDeltas(
    base: Map<String, Int>,
    deltas: Map<String, Int>
): Map<String, Int> = base.mapValues { (id, value) ->
    (value + (deltas[id] ?: 0)).coerceIn(0, 4)
}

internal data class SimulationMeterChange(
    val meter: SimulationMeter,
    val delta: Int
)

internal fun simulationMeterChanges(
    before: Map<String, Int>,
    after: Map<String, Int>,
    meters: List<SimulationMeter>
): List<SimulationMeterChange> = meters.map { meter ->
    SimulationMeterChange(
        meter = meter,
        delta = (after[meter.id] ?: meter.initial) - (before[meter.id] ?: meter.initial)
    )
}
