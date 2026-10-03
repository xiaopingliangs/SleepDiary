package app.sleepdiary.offline.ui.clinician

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sleepdiary.offline.application.ClinicianSummaryRequest
import app.sleepdiary.offline.application.ClinicianSummaryService
import app.sleepdiary.offline.application.SummaryTextWriter
import app.sleepdiary.offline.ui.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.io.OutputStream

/** Local preview survives rotation, but is deliberately regenerated after process death. */
class ClinicianPreviewModel : ViewModel() {
    var preview by mutableStateOf<String?>(null)
        private set
    var pendingExport by mutableStateOf<String?>(null)
        private set
    var exporting by mutableStateOf(false)
        private set
    var exportMessage by mutableStateOf<String?>(null)
        private set
    fun invalidate() { preview = null; exportMessage = null }
    fun prepared(text: String) { preview = text }
    fun stageExport(): Boolean {
        if (pendingExport != null || exporting || preview == null) return false
        exportMessage = null
        pendingExport = preview
        return true
    }
    fun launchFailed() {
        pendingExport = null
        exportMessage = "无法打开文件选择器，请重试。"
    }
    fun receiveExport(open: (() -> OutputStream?)?) {
        if (exporting) return
        val snapshot = pendingExport
        pendingExport = null
        if (open == null) return // User cancelled: no stream is opened.
        if (snapshot == null) {
            exportMessage = "预览已失效，请重新生成摘要后导出。"
            return
        }
        exporting = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { SummaryTextWriter.write(snapshot, open) }
                exportMessage = "就诊摘要已导出"
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { exportMessage = "导出未完成，请重试；文件可能未完整写入。" }
            finally { exporting = false }
        }
    }
}

@Composable fun ClinicianSummaryScreen(service: ClinicianSummaryService?, onBack: () -> Unit) {
    val model: ClinicianPreviewModel = viewModel()
    val resolver = LocalContext.current.applicationContext.contentResolver
    val scope = rememberCoroutineScope()
    var from by rememberSaveable { mutableStateOf(LocalDate.now().minusDays(13).toString()) }
    var through by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var comments by rememberSaveable { mutableStateOf(false) }
    var safety by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmExport by remember { mutableStateOf(false) }
    val enabled = !busy && !model.exporting && model.pendingExport == null
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        model.receiveExport(if (uri == null) null else ({ resolver.openOutputStream(uri, "wt") }))
    }
    BackHandler(enabled = !enabled) {}
    SimplePage("就诊摘要") {
        Action("返回我的", onBack, enabled)
        Text("选择日期和要展示的信息，先在本机预览。只有你主动导出并选择保存位置，摘要才离开应用；不会自动发送给医生。")
        OutlinedTextField(from, { from = it; model.invalidate() }, label = { Text("开始日期 YYYY-MM-DD") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth().testTag("summary_from"))
        OutlinedTextField(through, { through = it; model.invalidate() }, label = { Text("结束日期 YYYY-MM-DD") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth().testTag("summary_through"))
        SummaryOption("包含自由备注（可能有敏感信息）", "summary_comments", comments, enabled) {
            comments = it; model.invalidate()
        }
        SummaryOption("包含当前安全问询（含未完成问询的已保存答案）", "summary_safety", safety, enabled) {
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
        }, enabled && service != null)
        if (busy || model.exporting) Text("正在处理…")
        message?.let { Text(it) }
        model.exportMessage?.let { Text(it) }
        model.preview?.let { preview ->
            Text("内容以生成时的记录为准。修改日期或勾选项后，需要重新生成。")
            Text(preview, modifier = Modifier.testTag("summary_preview"))
            Action("导出这份预览 TXT", { confirmExport = true }, enabled)
        }
    }
    if (confirmExport && model.preview != null) AlertDialog(
        onDismissRequest = { confirmExport = false },
        title = { Text("导出明文就诊摘要") },
        text = { Text("只导出刚才预览的内容，其中可能包含你主动选择的自由备注和安全问询。请保存到你信任的位置；这不是完整备份。") },
        confirmButton = { TextButton(onClick = {
            confirmExport = false
            if (model.stageExport()) {
                try { export.launch("SleepDiary_clinician_summary.txt") }
                catch (_: Exception) { model.launchFailed() }
            }
        }) { Text("选择保存位置") } },
        dismissButton = { TextButton(onClick = { confirmExport = false }) { Text("取消") } }
    )
}

@Composable private fun SummaryOption(label: String, tag: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag)
        .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = change),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = null, enabled = enabled)
        Text(label)
    }
}
