package app.sleepdiary.offline.ui.diary

import app.sleepdiary.offline.data.entity.SleepDiaryEntry
import java.io.Serializable
import java.time.*
import java.util.UUID

enum class DiaryField(val label: String, val unit: String = "") {
    BED("大约几点躺到床上或平时睡觉的位置"), TRY("躺下以后，大约多久开始尝试入睡", "分钟"),
    SOL("估计入睡耗时", "分钟"), COUNT("估计夜间醒来次数", "次"),
    WASO("估计夜间清醒总时长", "分钟"), FINAL("最后一次大约几点醒来"),
    OUT("醒来以后，大约多久离开床或睡觉的位置", "分钟"), QUALITY("主观睡眠质量")
}

data class DiaryForm(
    val id: String = UUID.randomUUID().toString(),
    val referenceDate: LocalDate,
    val zoneId: String,
    val createdAtUtc: String? = null,
    val bed: LocalDateTime? = null, val attempt: LocalDateTime? = null,
    val attemptDelayMinutes: Int? = null,
    val sol: Int? = null, val count: Int? = null, val waso: Int? = null,
    val final: LocalDateTime? = null, val out: LocalDateTime? = null, val outDelayMinutes: Int? = null,
    val quality: Int? = null, val comment: String? = null
) : Serializable {
    fun time(field: DiaryField): LocalDateTime? = when(field) {
        DiaryField.BED -> bed
        DiaryField.TRY -> attemptDelayMinutes?.let { bed?.plusMinutes(it.toLong()) } ?: attempt
        DiaryField.FINAL -> final
        DiaryField.OUT -> outDelayMinutes?.let { final?.plusMinutes(it.toLong()) } ?: out
        else -> error("Not a time field")
    }
    fun delay(field: DiaryField): Int? = when (field) {
        DiaryField.TRY -> attemptDelayMinutes ?: if (bed != null && attempt != null) java.time.Duration.between(bed, attempt).toMinutes().toInt() else null
        DiaryField.OUT -> outDelayMinutes ?: if (final != null && out != null) java.time.Duration.between(final, out).toMinutes().toInt() else null
        else -> error("Not a relative duration field")
    }
    fun number(field: DiaryField): Int? = when(field) {
        DiaryField.SOL -> sol; DiaryField.COUNT -> count; DiaryField.WASO -> waso; DiaryField.QUALITY -> quality
        else -> error("Not a numeric field")
    }
    fun firstUnanswered(): DiaryField? = DiaryField.entries.firstOrNull { field ->
        when(field) { DiaryField.BED,DiaryField.FINAL -> time(field)==null; DiaryField.TRY,DiaryField.OUT -> delay(field)==null; else -> number(field)==null }
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
        val candidate=pickerPosition(field)
        // For an unanswered bedtime / attempt field, the 00:00 clock value
        // crosses the default evening anchor into the sleepDate calendar day.
        // Keeping the prior date made 00:00→07:00 appear to last 31 hours.
        val date=if(time==LocalTime.MIDNIGHT && candidate.toLocalDate().isBefore(referenceDate)) referenceDate else candidate.toLocalDate()
        return LocalDateTime.of(date,time)
    }

    fun answerTime(field: DiaryField, value: LocalDateTime): DiaryForm = when(field) {
        DiaryField.BED -> copy(bed=value, attempt=attemptDelayMinutes?.let { value.plusMinutes(it.toLong()) } ?: attempt)
        DiaryField.TRY -> copy(attempt=value, attemptDelayMinutes=bed?.let { java.time.Duration.between(it, value).toMinutes().toInt() })
        DiaryField.FINAL -> copy(final=value, out=outDelayMinutes?.let { value.plusMinutes(it.toLong()) } ?: out)
        DiaryField.OUT -> copy(out=value, outDelayMinutes=final?.let { java.time.Duration.between(it, value).toMinutes().toInt() })
        else -> error("Not a time field")
    }
    fun answerDelay(field: DiaryField, minutes: Int): DiaryForm {
        require(field == DiaryField.TRY || field == DiaryField.OUT)
        require(minutes in 0..1440)
        return when (field) {
            DiaryField.TRY -> copy(attemptDelayMinutes = minutes, attempt = bed?.plusMinutes(minutes.toLong()) ?: attempt)
            DiaryField.OUT -> copy(outDelayMinutes = minutes, out = final?.plusMinutes(minutes.toLong()) ?: out)
            else -> error("Not a relative duration field")
        }
    }
    fun answerNumber(field: DiaryField, value: Int): DiaryForm {
        require(value in when(field) { DiaryField.COUNT -> 0..99; DiaryField.QUALITY -> 1..5; DiaryField.SOL,DiaryField.WASO -> 0..1440; else -> error("Not numeric") })
        return when(field) { DiaryField.SOL -> copy(sol=value); DiaryField.COUNT -> copy(count=value); DiaryField.WASO -> copy(waso=value); else -> copy(quality=value) }
    }
    fun toEntry(now: Instant): SleepDiaryEntry? {
        if(firstUnanswered()!=null) return null
        val derivedAttempt = checkNotNull(time(DiaryField.TRY))
        val derivedOut = checkNotNull(time(DiaryField.OUT))
        return SleepDiaryEntry(id, derivedOut.toLocalDate().toString(),checkNotNull(bed).toString(),derivedAttempt.toString(),sol!!,count!!,waso!!,
            checkNotNull(final).toString(),derivedOut.toString(),quality!!,comment,zoneId,createdAtUtc ?: now.toString(),now.toString(),consistencyWarning=false)
    }
    companion object {
        fun from(entry:SleepDiaryEntry) = DiaryForm(id=entry.id,referenceDate=LocalDate.parse(entry.sleepDate),zoneId=entry.timeZoneIdAtEntry,
            createdAtUtc=entry.createdAtUtc,bed=LocalDateTime.parse(entry.bedEnteredAtLocal),attempt=LocalDateTime.parse(entry.trySleepAtLocal),
            attemptDelayMinutes=runCatching { java.time.Duration.between(LocalDateTime.parse(entry.bedEnteredAtLocal), LocalDateTime.parse(entry.trySleepAtLocal)).toMinutes().toInt() }.getOrNull(),
            sol=entry.sleepOnsetLatencyMin,count=entry.awakeningCount,waso=entry.wakeAfterSleepOnsetMin,
            final=LocalDateTime.parse(entry.finalAwakeningAtLocal),out=LocalDateTime.parse(entry.outOfBedAtLocal),
            outDelayMinutes=runCatching { java.time.Duration.between(LocalDateTime.parse(entry.finalAwakeningAtLocal), LocalDateTime.parse(entry.outOfBedAtLocal)).toMinutes().toInt() }.getOrNull(),
            quality=entry.sleepQuality,comment=entry.comment)
    }
}
