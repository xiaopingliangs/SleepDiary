package app.sleepdiary.offline.ui.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sleepdiary.offline.data.entity.WorkspaceRecord
import app.sleepdiary.offline.data.repository.*
import app.sleepdiary.offline.ui.Action
import app.sleepdiary.offline.ui.SimplePage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID

class WorkspaceModel(private val repository: WorkspaceRepository, private val saved: SavedStateHandle) : ViewModel() {
    var records by mutableStateOf<List<WorkspaceRecord>>(emptyList()); private set
    var busy by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null); private set
    var draft by mutableStateOf(saved.get<String>("recordId")?.let {
        WorkspaceDraft(it, saved["recordKind"] ?: "", saved["recordTitle"] ?: "", saved["recordBody"] ?: "", saved["recordRevision"] ?: 0, saved["recordFingerprint"])
    }); private set
    var dirty by mutableStateOf(saved.get<Boolean>("recordDirty") ?: false); private set
    init { reload() }
    private fun put(value: WorkspaceDraft?, changed: Boolean) {
        draft = value; dirty = changed; saved["recordDirty"] = changed
        saved["recordId"] = value?.id; saved["recordKind"] = value?.kind
        saved["recordTitle"] = value?.title; saved["recordBody"] = value?.body; saved["recordRevision"] = value?.baseRevision
        saved["recordFingerprint"] = value?.baseFingerprint
    }
    fun create(kind: WorkspaceKind) { if (!busy) { put(WorkspaceDraft(UUID.randomUUID().toString(), kind.name, "", ""), false); message = null } }
    fun edit(record: WorkspaceRecord) { if (!busy) { put(WorkspaceDraft(record.id, record.kind, record.title, record.body, record.revision, WorkspaceValidation.fingerprint(record)), false); message = null } }
    fun title(value: String) {
        if (busy) return
        if (value.codePointCount(0, value.length) > 200) { message = "标题最多 200 个字符，未应用超限输入。"; return }
        draft?.let { if(it.title != value) { put(it.copy(title = value), true); message = null } }
    }
    fun body(value: String) {
        if (busy) return
        if (value.codePointCount(0, value.length) > 20000) { message = "正文最多 20000 个字符，未应用超限输入。"; return }
        draft?.let { if(it.body != value) { put(it.copy(body = value), true); message = null } }
    }
    fun closeEditor() { if (!busy) { put(null, false); message = null } }
    fun reload() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { records = repository.all() }
            catch (c: CancellationException) { throw c }
            catch (_: Exception) { message = "记录读取失败，请重试。" }
            finally { busy = false }
        }
    }
    fun save() {
        val current = draft ?: return
        if (busy) return
        busy = true; message = null
        viewModelScope.launch {
            var savedSuccessfully = false
            try {
                when (val result = repository.save(current)) {
                    is WorkspaceSaveResult.Saved -> {
                        savedSuccessfully = true; put(null, false); message = "个人记录已保存"
                        records = repository.all()
                    }
                    is WorkspaceSaveResult.Rejected -> message = when (result.reason) {
                        WorkspaceRejection.INVALID_INPUT -> "请填写正文，并检查标题与正文长度。"
                        WorkspaceRejection.STALE_EDIT -> "记录已被其他操作修改。当前草稿仍保留，请先保存好草稿内容，再重新打开记录。"
                        WorkspaceRejection.CLOCK_MOVED_BACKWARD -> "设备时间早于记录的修改时间，请检查设备时间后重试。"
                    }
                    is WorkspaceSaveResult.Failed -> message = if(result.mayHaveCommitted)
                        "写入结果尚未确认。草稿已保留，可重试核对，不会重复创建记录。" else "保存失败，草稿已保留，请重试。"
                }
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { message = if(savedSuccessfully) "记录已保存，但列表刷新失败，请重试读取。" else "保存未确认，草稿已保留，请重试。" }
            finally { busy = false }
        }
    }
}

@Composable fun WorkspaceScreen(repository: WorkspaceRepository?, onBack: () -> Unit) {
    if (repository == null) { SimplePage("个人记录") { Action("返回设置", onBack); Text("当前无法读取个人记录。") }; return }
    val model: WorkspaceModel = viewModel(factory = viewModelFactory { initializer { WorkspaceModel(repository, createSavedStateHandle()) } })
    var discard by remember { mutableStateOf(false) }
    fun leaveEditor() { if(model.dirty) discard = true else model.closeEditor() }
    BackHandler(enabled = model.busy || model.draft != null) { if(!model.busy) leaveEditor() }
    val draft = model.draft
    SimplePage(if(draft == null) "个人记录" else WorkspaceKind.entries.firstOrNull { it.name == draft.kind }?.label ?: "个人记录") {
        Action(if(draft == null) "返回设置" else "返回记录列表", { if(draft == null) onBack() else leaveEditor() }, !model.busy)
        model.message?.let { Text(it, modifier = Modifier.testTag("workspace_message")) }
        if (model.busy) Text("正在处理…")
        if (draft == null) {
            Text("按需记下自己的想法或沟通内容，不需要每天填写。已保存记录会进入完整 JSON 备份，不会自动加入日记图片、CSV 或就诊摘要。")
            WorkspaceKind.entries.forEach { kind -> Action("新建${kind.label}", { model.create(kind) }, !model.busy) }
            Action("刷新记录", model::reload, !model.busy)
            if(!model.busy && model.records.isEmpty()) Text("还没有个人记录。")
            model.records.forEach { record ->
                Action(record.title.ifBlank { WorkspaceKind.valueOf(record.kind).label }, { model.edit(record) }, !model.busy)
                Text("${WorkspaceKind.valueOf(record.kind).label} · 修改时间 UTC ${record.updatedAtUtc}")
            }
        } else {
            if(draft.kind == WorkspaceKind.CLINICIAN_NOTE.name) Text("这里是你自己录入的就诊沟通内容。App 不核验医生身份，也不会据此执行或调整治疗。")
            OutlinedTextField(draft.title, model::title, label = { Text("标题（可选）") }, singleLine = true,
                enabled = !model.busy, modifier = Modifier.fillMaxWidth().testTag("workspace_title"))
            OutlinedTextField(draft.body, model::body, label = { Text("写下你的记录") }, minLines = 5, maxLines = 12,
                enabled = !model.busy, modifier = Modifier.fillMaxWidth().testTag("workspace_body"))
            Text("点击保存后才写入本机数据库。")
            Action("保存个人记录", model::save, !model.busy)
        }
    }
    if(discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("放弃未保存的编辑？") },
        text = { Text("原来已经保存的记录不会改变。") },
        confirmButton = { TextButton(onClick = { discard = false; model.closeEditor() }) { Text("放弃编辑") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("继续编辑") } })
}
