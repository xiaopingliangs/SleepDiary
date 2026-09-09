package app.sleepdiary.offline

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import java.time.*
import java.util.UUID

fun clockOn(date: String) = Clock.fixed(Instant.parse(date + "T12:00:00Z"), ZoneId.of("Asia/Shanghai"))
fun diary(date: String = "2026-09-09", id: String = UUID.randomUUID().toString()): SleepDiaryEntry {
    val day = LocalDate.parse(date)
    return SleepDiaryEntry(id, date, day.minusDays(1).toString()+"T23:00:00", day.minusDays(1).toString()+"T23:30:00",
        20, 2, 30, date+"T06:00:00", date+"T07:00:00", 3, "estimate", "Asia/Shanghai",
        "2026-09-09T00:00:00Z", "2026-09-09T00:00:00Z", consistencyWarning = false)
}
