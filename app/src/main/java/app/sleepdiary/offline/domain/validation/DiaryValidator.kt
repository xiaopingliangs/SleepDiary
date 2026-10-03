package app.sleepdiary.offline.domain.validation

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.domain.model.LocalTimeMath
import java.time.*
import java.util.UUID

data class TimeConflict(val fields:List<String>,val message:String)
data class DiaryValidation(val errors: List<String>, val consistencyWarning: Boolean, val conflicts:List<TimeConflict> = emptyList()) {
    val canSave: Boolean get() = errors.isEmpty()
}

/** Validates estimates without correcting them or interpreting them medically. */
object DiaryValidator {
    fun validate(entry: SleepDiaryEntry): DiaryValidation {
        val errors = mutableListOf<String>()
        val conflicts=mutableListOf<TimeConflict>()
        fun checkField(valid: Boolean, field: String) { if (!valid) errors += field }
        checkField(runCatching { UUID.fromString(entry.id).toString() == entry.id.lowercase() }.getOrDefault(false), "id")
        checkField(entry.diarySchemaVersion == Versions.DIARY_SCHEMA, "diarySchemaVersion")
        checkField(entry.timeZoneIdAtEntry in ZoneId.getAvailableZoneIds(), "timeZoneIdAtEntry")
        checkField(entry.sleepOnsetLatencyMin in 0..1440, "sleepOnsetLatencyMin")
        checkField(entry.wakeAfterSleepOnsetMin in 0..1440, "wakeAfterSleepOnsetMin")
        checkField(entry.awakeningCount in 0..99, "awakeningCount")
        checkField(entry.sleepQuality in 1..5, "sleepQuality")
        fun dateTime(value: String, field: String): LocalDateTime? = try {
            LocalDateTime.parse(value)
        } catch (_: DateTimeException) { errors += field; null }
        val bed = dateTime(entry.bedEnteredAtLocal, "bedEnteredAtLocal")
        val attempt = dateTime(entry.trySleepAtLocal, "trySleepAtLocal")
        val final = dateTime(entry.finalAwakeningAtLocal, "finalAwakeningAtLocal")
        val out = dateTime(entry.outOfBedAtLocal, "outOfBedAtLocal")
        val date = try { LocalDate.parse(entry.sleepDate) } catch (_: DateTimeException) { errors += "sleepDate"; null }
        if (date != null && out != null) checkField(date == out.toLocalDate(), "sleepDate")
        fun relation(a:LocalDateTime?,b:LocalDateTime?,first:String,second:String,labelA:String,labelB:String) {
            if(a!=null&&b!=null&&a>b)conflicts+=TimeConflict(listOf(first,second),"‘"+labelB+"’不能早于‘"+labelA+"’。")
        }
        relation(bed,attempt,"bedEnteredAtLocal","trySleepAtLocal","进入睡觉位置","开始尝试入睡")
        relation(attempt,final,"trySleepAtLocal","finalAwakeningAtLocal","开始尝试入睡","最后一次醒来")
        relation(final,out,"finalAwakeningAtLocal","outOfBedAtLocal","最后一次醒来","真正离开睡觉位置")
        if (bed != null && attempt != null && final != null && out != null) {
            checkField(bed <= attempt && attempt <= final && final <= out, "timeOrder")
            if(date!=null) {
                val earliest=date.minusDays(1)
                checkField(listOf(bed,attempt,final,out).all { it.toLocalDate() in earliest..date },"timeDateRange")
                checkField(!Duration.between(bed,out).isNegative && Duration.between(bed,out)<=Duration.ofHours(24),"timeSpan")
            }
        }
        val warning = errors.isEmpty() && attempt != null && final != null &&
            entry.sleepOnsetLatencyMin.toLong() + entry.wakeAfterSleepOnsetMin > LocalTimeMath.minutesBetween(attempt, final)
        return DiaryValidation(errors, warning, conflicts)
    }
}
