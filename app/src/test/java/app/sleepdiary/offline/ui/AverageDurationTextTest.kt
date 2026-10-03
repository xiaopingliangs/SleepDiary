package app.sleepdiary.offline.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AverageDurationTextTest {
    @Test fun averageTibUsesHoursAndMinutesWithoutChangingStoredMinutes() {
        assertEquals("7小时10分钟",AverageDurationText.fromMinutes(430.0))
        assertEquals("8小时",AverageDurationText.fromMinutes(480.0))
        assertEquals("45分钟",AverageDurationText.fromMinutes(45.0))
        assertEquals("7小时11分钟",AverageDurationText.fromMinutes(430.5))
    }
}
