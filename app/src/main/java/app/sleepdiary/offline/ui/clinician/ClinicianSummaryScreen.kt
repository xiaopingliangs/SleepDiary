package app.sleepdiary.offline.ui.clinician

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sleepdiary.offline.application.ClinicianSummaryRequest
import app.sleepdiary.offline.application.ClinicianSummaryService
import app.sleepdiary.offline.ui.*
import kotlinx.coroutines.*
import java.time.LocalDate

/** Local preview survives rotation, but is deliberately regenerated after process death. */
class ClinicianPreviewModel : ViewModel() {
    var preview by mutableStateOf<String?>(null)
        private set
    fun invalidate() { preview = null }
    fun prepared(text: String) { preview = text }
}

@Composable fun ClinicianSummaryScreen(service: ClinicianSummaryService?, onBack: () -> Unit) {
    val model: ClinicianPreviewModel = viewModel()
    val scope = rememberCoroutineScope()
    var from by rememberSaveable { mutableStateOf(LocalDate.now().minusDays(13).toString()) }
    var through by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var comments by rememberSaveable { mutableStateOf(false) }
    var safety by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    SimplePage("就诊摘要") {
        Action("返回设置", onBack, !busy)
        Text("选择日期和要展示的信息，在本机生成就诊摘要。本页不会导出、复制或发送数据。")
        OutlinedTextField(from, { from = it; model.invalidate() }, label = { Text("开始日期 YYYY-MM-DD") },
            singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("summary_from"))
        OutlinedTextField(through, { through = it; model.invalidate() }, label = { Text("结束日期 YYYY-MM-DD") },
            singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("summary_through"))
        SummaryOption("包含自由备注（可能有敏感信息）", "summary_comments", comments, !busy) {
            comments = it; model.invalidate()
        }
        SummaryOption("包含当前安全问询（含未完成问询的已保存答案）", "summary_safety", safety, !busy) {
            safety = it; model.invalidate()
        }
        Action("生成本机预览", {
            model.invalidate(); message = null
            val request = runCatching { ClinicianSummaryRequest(LocalDate.parse(from), LocalDate.parse(through), comments, safety) }.getOrNull()
            if (request == null) message = "请检查日期格式和先后顺序。"
            else {
                busy = true
                scope.launch {
                    try {
                        val text = withContext(Dispatchers.IO) {
                            checkNotNull(service).prepare(request,
                                { id -> SAFETY_QUESTIONS.firstOrNull { it.id == id }?.text ?: "控制情况（$id）" },
                                { answer -> safetyAnswerLabel(answer.answerCode) +
                                    (answer.detailJson?.let { "\n" + safetyDetailLabel(it) } ?: "") })
                        }
                        model.prepared(text)
                    } catch (c: CancellationException) { throw c }
                    catch (_: Exception) { message = "摘要读取失败，请重试。" }
                    finally { busy = false }
                }
            }
        }, !busy && service != null)
        if (busy) Text("正在处理…")
        message?.let { Text(it) }
        model.preview?.let { preview ->
            Text("内容以生成时的记录为准。修改日期或勾选项后，需要重新生成。")
            Text(preview, modifier = Modifier.testTag("summary_preview"))
        }
    }
}

@Composable private fun SummaryOption(label: String, tag: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag)
        .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = change),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = null, enabled = enabled)
        Text(label)
    }
}
