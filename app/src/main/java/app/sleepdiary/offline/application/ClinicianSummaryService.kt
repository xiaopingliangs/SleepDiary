package app.sleepdiary.offline.application

import androidx.room.withTransaction
import app.sleepdiary.offline.data.db.SleepDiaryDatabase
import app.sleepdiary.offline.data.entity.SafetyAnswer
import app.sleepdiary.offline.domain.model.Versions
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ClinicianSummaryRequest(
    val from: LocalDate, val through: LocalDate,
    val includeComments: Boolean = false, val includeCurrentSafety: Boolean = false
) {
    init { require(!through.isBefore(from)) { "结束日期不能早于开始日期" } }
}

/** Read-only local projection captured in one transaction. No file, clipboard or share operation. */
class ClinicianSummaryService(private val db: SleepDiaryDatabase, private val clock: Clock = Clock.systemUTC()) {
    suspend fun prepare(
        request: ClinicianSummaryRequest,
        questionLabel: (String) -> String = { it },
        answerLabel: (SafetyAnswer) -> String = { it.answerCode + (it.detailJson?.let { d -> " · $d" } ?: "") }
    ): String = db.withTransaction {
        val entries = db.diaryDao().all().filter {
            val date = LocalDate.parse(it.sleepDate)
            date >= request.from && date <= request.through
        }
        val currentSafety = if (request.includeCurrentSafety) db.safetyDao().all().filter { it.assessment.isCurrent } else emptyList()
        buildString {
            appendLine("SleepDiary 就诊沟通摘要")
            appendLine("生成时间（UTC）：${Instant.now(clock)}")
            appendLine("日期范围：${request.from} — ${request.through}")
            appendLine("有记录 ${entries.size} 天 / ${ChronoUnit.DAYS.between(request.from, request.through) + 1} 个日历日；无记录日未按零计算。")
            appendLine("以下为用户主观估计和自报，供就诊沟通，不是诊断、治疗处方或完整备份。")
            appendLine("App ${Versions.APP}；SPEC ${Versions.SPEC}；Diary ${Versions.DIARY_SCHEMA}；CBT-I Protocol ${Versions.CBTI_PROTOCOL}")
            appendLine("自由备注：${if (request.includeComments) "已选择包含非空备注" else "未选择，不包含"}")
            appendLine("当前安全问询：${if (request.includeCurrentSafety) "已选择包含，独立于日记日期范围" else "未选择，不包含"}")
            if (entries.isEmpty()) appendLine("所选日期范围没有睡眠记录。")
            entries.forEach { e ->
                appendLine()
                appendLine("日期：${e.sleepDate}")
                appendLine("大约进入睡觉位置：${e.bedEnteredAtLocal}")
                appendLine("大约开始尝试入睡：${e.trySleepAtLocal}")
                appendLine("估计入睡耗时：${e.sleepOnsetLatencyMin} 分钟")
                appendLine("估计夜醒次数：${e.awakeningCount}")
                appendLine("估计夜间清醒总时长：${e.wakeAfterSleepOnsetMin} 分钟")
                appendLine("大约最后醒来：${e.finalAwakeningAtLocal}")
                appendLine("大约离开睡觉位置：${e.outOfBedAtLocal}")
                appendLine("主观睡眠质量：${e.sleepQuality}（1很差、2较差、3一般、4较好、5很好）")
                appendLine("记录时区：${e.timeZoneIdAtEntry}")
                if (e.consistencyWarning) appendLine("这条记录曾由用户确认按不一致的估计保存，原值未修正。")
                if (request.includeComments && !e.comment.isNullOrBlank()) {
                    appendLine("备注（用户原文）：")
                    e.comment.lineSequence().forEach { appendLine("  $it") }
                }
                appendLine("记录 ID：${e.id}；版本：${e.diarySchemaVersion}；最后修改 UTC：${e.updatedAtUtc}")
            }
            if (request.includeCurrentSafety) {
                appendLine()
                appendLine("当前安全问询（用户自报，不是诊断；不据此确认治疗适用性）")
                if (currentSafety.isEmpty()) appendLine("没有当前安全问询；不能解释为没有安全问题。")
                currentSafety.forEach { s ->
                    appendLine("问询 ID：${s.assessment.id}；状态：${s.assessment.status}；时间 UTC：${s.assessment.answeredAtUtc}")
                    appendLine("Safety ${s.assessment.safetySchemaVersion}；Safety Rule ${s.assessment.safetyRuleVersion}")
                    appendLine("原有分流标记：${s.assessment.derivedGate}（不是诊断或风险分数）")
                    if (s.answers.isEmpty()) appendLine("尚无已保存答案。")
                    s.answers.sortedBy { it.questionId }.forEach { a ->
                        appendLine("${a.questionId} ${questionLabel(a.questionId)}")
                        answerLabel(a).lineSequence().forEach { appendLine("  $it") }
                    }
                }
            }
            appendLine()
            appendLine("当前没有已启用的治疗计划；不生成治疗窗口或治疗效果结论。")
        }
    }
}
