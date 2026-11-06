package app.sleepdiary.offline.ui.education

import org.junit.Assert.assertEquals
import org.junit.Test

class SimulationModelTest {
    private val meters = listOf(
        SimulationMeter("pressure", "攒下的睡意", "pressure", 3),
        SimulationMeter("clock", "生物钟的位置", "clock", 3),
        SimulationMeter("arousal", "紧绷程度", "arousal", 2)
    )

    @Test fun deltasAreClampedToFiveLevelScale() {
        val base = mapOf("pressure" to 4, "clock" to 0, "arousal" to 2)
        val result = applySimulationDeltas(
            base,
            mapOf("pressure" to 2, "clock" to -3, "arousal" to 1)
        )
        assertEquals(mapOf("pressure" to 4, "clock" to 0, "arousal" to 3), result)
    }

    @Test fun meterChangesReportDirectionWithoutMutatingInputs() {
        val before = mapOf("pressure" to 3, "clock" to 3, "arousal" to 2)
        val after = mapOf("pressure" to 4, "clock" to 3, "arousal" to 1)
        val changes = simulationMeterChanges(before, after, meters)
        assertEquals(listOf(1, 0, -1), changes.map { it.delta })
        assertEquals(mapOf("pressure" to 3, "clock" to 3, "arousal" to 2), before)
        assertEquals(mapOf("pressure" to 4, "clock" to 3, "arousal" to 1), after)
    }
}
