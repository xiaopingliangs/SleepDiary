package app.sleepdiary.offline.ui.education

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val SimulationMeterSaver = listSaver<Map<String, Int>, String>(
    save = { values -> values.entries.flatMap { listOf(it.key, it.value.toString()) } },
    restore = { saved ->
        saved.chunked(2).mapNotNull { pair ->
            val value = pair.getOrNull(1)?.toIntOrNull()
            if (pair.size == 2 && value != null) pair[0] to value else null
        }.toMap()
    }
)

/**
 * Native chapter-two simulation. Results stay in this screen only; the
 * Education persistence contract deliberately has no simulator-result table.
 */
@Composable
internal fun SimulationBlock(spec: SimulationSpec) {
    val initialValues = remember(spec) { spec.meters.associate { it.id to it.initial } }
    var values by rememberSaveable(spec.id, stateSaver = SimulationMeterSaver) {
        mutableStateOf(initialValues)
    }
    var stageIndex by rememberSaveable(spec.id) { mutableIntStateOf(0) }
    var previewKey by rememberSaveable(spec.id, stageIndex) { mutableStateOf<String?>(null) }
    var chosenOutcomeCsv by rememberSaveable(spec.id) { mutableStateOf("") }

    val chosenOutcomeIds = remember(chosenOutcomeCsv) {
        chosenOutcomeCsv.split(',').filter { it.isNotBlank() }
    }
    val stage = spec.stages.getOrNull(stageIndex)
    val selectedChoice = stage?.choices?.firstOrNull { it.key == previewKey }
    val previewValues = selectedChoice?.let { applySimulationDeltas(values, it.deltas) } ?: values

    fun restart() {
        values = initialValues
        stageIndex = 0
        previewKey = null
        chosenOutcomeCsv = ""
    }

    Card(Modifier.fillMaxWidth().testTag("sim_block")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "互动模拟",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (spec.intro.isNotBlank()) {
                Text(spec.intro, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                "这是一个假设情境，只用来理解一般概念，不是给你安排个人作息。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                spec.stages.isEmpty() -> {
                    if (spec.meters.isNotEmpty()) {
                        Text("开始时的状态", style = MaterialTheme.typography.titleMedium)
                        SimulationMeterTable(spec.meters, initialValues)
                    }
                    Text("这一节只保留概念说明，没有可执行的模拟选择。")
                }

                stage != null -> {
                    if (stageIndex == 0 && spec.meters.isNotEmpty()) {
                        Text("开始时的状态", style = MaterialTheme.typography.titleMedium)
                        SimulationMeterTable(spec.meters, initialValues)
                    }
                    HorizontalDivider()
                    if (spec.stages.size > 1) {
                        Text(
                            "第 ${stageIndex + 1} 步，共 ${spec.stages.size} 步",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(stage.prompt, style = MaterialTheme.typography.titleMedium)
                    if (stage.hint.isNotBlank()) {
                        Text(
                            stage.hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "先点一个看看它的说法，随时可以换；点「下一步」才算选定。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    stage.choices.forEachIndexed { index, choice ->
                        val selected = choice.key == previewKey
                        val keyLabel = if (index < 26) ('A'.code + index).toChar().toString() else "${index + 1}"
                        Card(
                            onClick = { previewKey = choice.key },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .testTag("sim_choice_${choice.key}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            keyLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    choice.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    selectedChoice?.let { choice ->
                        SimulationFeedbackCard(
                            choice = choice,
                            before = values,
                            after = previewValues,
                            meters = spec.meters,
                            onNext = {
                                values = applySimulationDeltas(values, choice.deltas)
                                val outcomeId = choice.outcomeId
                                if (!outcomeId.isNullOrBlank() && outcomeId !in chosenOutcomeIds) {
                                    chosenOutcomeCsv = (chosenOutcomeIds + outcomeId).joinToString(",")
                                }
                                stageIndex += 1
                                previewKey = null
                            }
                        )
                    }
                }

                else -> {
                    SimulationSummary(
                        spec = spec,
                        initialValues = initialValues,
                        values = values,
                        chosenOutcomeIds = chosenOutcomeIds,
                        onRestart = ::restart
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulationMeterTable(
    meters: List<SimulationMeter>,
    values: Map<String, Int>,
    compareTo: Map<String, Int>? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        meters.forEach { meter ->
            val value = (values[meter.id] ?: meter.initial).coerceIn(0, 4)
            val delta = compareTo?.let { value - (it[meter.id] ?: value) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    meter.label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = buildString {
                        append("第 ${value + 1} / 5 档")
                        when {
                            delta == null -> Unit
                            delta > 0 -> append("  ↑")
                            delta < 0 -> append("  ↓")
                            else -> append("  与开始相同")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "${meter.label}，第 ${value + 1} 档，共 5 档"
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (index <= value) meterColor(meter.tone)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun meterColor(tone: String): Color = when (tone) {
    "pressure" -> MaterialTheme.colorScheme.primary
    "clock" -> MaterialTheme.colorScheme.secondary
    "arousal" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun SimulationFeedbackCard(
    choice: SimulationChoice,
    before: Map<String, Int>,
    after: Map<String, Int>,
    meters: List<SimulationMeter>,
    onNext: () -> Unit
) {
    val changes = simulationMeterChanges(before, after, meters).filter { it.delta != 0 }
    val container = when (choice.tone) {
        "better" -> MaterialTheme.colorScheme.secondaryContainer
        "caution" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when (choice.tone) {
        "better" -> MaterialTheme.colorScheme.onSecondaryContainer
        "caution" -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("sim_feedback")
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (choice.toneLabel.isNotBlank()) {
                Text(choice.toneLabel, style = MaterialTheme.typography.labelLarge)
            }
            if (choice.feedback.perspective.isNotBlank()) {
                Text(choice.feedback.perspective, style = MaterialTheme.typography.titleMedium)
            }
            if (choice.feedback.plain.isNotBlank()) {
                Text(choice.feedback.plain, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = if (changes.isEmpty()) "如果这样选：这几项基本没变。"
                else "如果这样选：" + changes.joinToString("，") {
                    "${it.meter.label} ${if (it.delta > 0) "↑" else "↓"}"
                },
                style = MaterialTheme.typography.labelLarge
            )
            if (choice.feedback.adjustment.isNotBlank()) {
                Text("什么情况下要换个做法", style = MaterialTheme.typography.titleSmall)
                Text(choice.feedback.adjustment, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "这只是预览，点「下一步」才会记下你的选择。",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("sim_next")
            ) {
                Text("下一步")
            }
        }
    }
}

@Composable
private fun SimulationSummary(
    spec: SimulationSpec,
    initialValues: Map<String, Int>,
    values: Map<String, Int>,
    chosenOutcomeIds: List<String>,
    onRestart: () -> Unit
) {
    val outcomes = spec.outcomes.filter { it.id in chosenOutcomeIds }
    Text("走完了", style = MaterialTheme.typography.titleLarge)
    outcomes.forEach { outcome ->
        if (outcome.text.isNotBlank()) {
            Text(outcome.text, style = MaterialTheme.typography.bodyLarge)
        }
        if (outcome.conclusion.isNotBlank()) {
            Text("这一轮的结论", style = MaterialTheme.typography.titleSmall)
            Text(outcome.conclusion, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (spec.meters.isNotEmpty()) {
        Text("走完之后的状态", style = MaterialTheme.typography.titleMedium)
        SimulationMeterTable(spec.meters, values, initialValues)
    }
    Text(
        "这只是用来理解概念的教学模拟，不是你本人的睡眠数据，也不会生成治疗建议。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        "如果准备好了，可以用下面的按钮结束本节。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = onRestart,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("sim_restart")
    ) {
        Text("再走一次")
    }
}
