package app.sleepdiary.offline.ui.diary

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import java.io.Serializable
import java.time.*
import java.util.UUID

enum class DiaryField(val label: String, val unit: String = "") {
    BED("大约进入睡觉位置"), TRY("大约开始尝试入睡"),
    SOL("估计入睡耗时", "分钟"), COUNT("估计夜间醒来次数", "次"),
    WASO("估计夜间清醒总时长", "分钟"), FINAL("大约最后一次醒来"),
    OUT("大约离开睡觉位置"), QUALITY("主观睡眠质量")
}

data class DiaryForm(
    val id: String = UUID.randomUUID().toString(),
    val referenceDate: LocalDate,
    val zoneId: String,
    val createdAtUtc: String? = null,
    val bed: LocalDateTime? = null, val attempt: LocalDateTime? = null,
    val sol: Int? = null, val count: Int? = null, val waso: Int? = null,
    val final: LocalDateTime? = null, val out: LocalDateTime? = null,
    val quality: Int? = null, val comment: String? = null
) : Serializable {
    fun time(field: DiaryField): LocalDateTime? = when(field) {
        DiaryField.BED -> bed; DiaryField.TRY -> attempt; DiaryField.FINAL -> final; DiaryField.OUT -> out
        else -> error("Not a time field")
    }
    fun number(field: DiaryField): Int? = when(field) {
        DiaryField.SOL -> sol; DiaryField.COUNT -> count; DiaryField.WASO -> waso; DiaryField.QUALITY -> quality
        else -> error("Not a numeric field")
    }
    fun firstUnanswered(): DiaryField? = DiaryField.entries.firstOrNull { field ->
        when(field) { DiaryField.BED,DiaryField.TRY,DiaryField.FINAL,DiaryField.OUT -> time(field)==null; else -> number(field)==null }
    }
    /** A candidate viewport, never an answer. The full candidate date is shown before confirmation. */
    fun pickerPosition(field: DiaryField): LocalDateTime = time(field) ?: when(field) {
        DiaryField.BED -> referenceDate.minusDays(1).atTime(22,0)
        DiaryField.TRY -> bed?.plusMinutes(30) ?: referenceDate.minusDays(1).atTime(22,30)
        DiaryField.FINAL -> referenceDate.atTime(6,0)
        DiaryField.OUT -> final?.plusMinutes(30) ?: referenceDate.atTime(6,30)
        else -> error("Not a time field")
    }
    /** Calendar positioning only: no definition of normal sleep and no changes to other answers. */
    fun positionAt(field:DiaryField,time:LocalTime):LocalDateTime {
        val existing=this.time(field)
        if(existing!=null) return existing.with(time)
        // The dialog owns a stable full date. A clock change must not invent another day.
        return pickerPosition(field).with(time)
    }

    fun answerTime(field: DiaryField, value: LocalDateTime): DiaryForm = when(field) {
        DiaryField.BED -> copy(bed=value); DiaryField.TRY -> copy(attempt=value)
        DiaryField.FINAL -> copy(final=value); DiaryField.OUT -> copy(out=value)
        else -> error("Not a time field")
    }
    fun answerNumber(field: DiaryField, value: Int): DiaryForm {
        require(value in when(field) { DiaryField.COUNT -> 0..99; DiaryField.QUALITY -> 1..5; DiaryField.SOL,DiaryField.WASO -> 0..1440; else -> error("Not numeric") })
        return when(field) { DiaryField.SOL -> copy(sol=value); DiaryField.COUNT -> copy(count=value); DiaryField.WASO -> copy(waso=value); else -> copy(quality=value) }
    }
    fun toEntry(now: Instant): SleepDiaryEntry? {
        if(firstUnanswered()!=null) return null
        return SleepDiaryEntry(id, out!!.toLocalDate().toString(),bed!!.toString(),attempt!!.toString(),sol!!,count!!,waso!!,
            final!!.toString(),out.toString(),quality!!,comment,zoneId,createdAtUtc ?: now.toString(),now.toString(),consistencyWarning=false)
    }
    companion object {
        fun from(entry:SleepDiaryEntry) = DiaryForm(id=entry.id,referenceDate=LocalDate.parse(entry.sleepDate),zoneId=entry.timeZoneIdAtEntry,
            createdAtUtc=entry.createdAtUtc,bed=LocalDateTime.parse(entry.bedEnteredAtLocal),attempt=LocalDateTime.parse(entry.trySleepAtLocal),
            sol=entry.sleepOnsetLatencyMin,count=entry.awakeningCount,waso=entry.wakeAfterSleepOnsetMin,
            final=LocalDateTime.parse(entry.finalAwakeningAtLocal),out=LocalDateTime.parse(entry.outOfBedAtLocal),quality=entry.sleepQuality,comment=entry.comment)
    }
}
