package app.sleepdiary.offline.ui.education

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.sleepdiary.offline.data.entity.EducationRecord
import app.sleepdiary.offline.data.entity.WorkspaceRecord
import app.sleepdiary.offline.data.repository.EducationChoicePreview
import app.sleepdiary.offline.data.repository.EducationRepository
import app.sleepdiary.offline.data.repository.EducationUserDataSnapshot
import app.sleepdiary.offline.data.repository.WorkspaceRepository
import app.sleepdiary.offline.domain.model.Versions
import app.sleepdiary.offline.ui.Action
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

private const val VERSION = "PUBLIC_EDUCATION_CONTENT_1.0-CANDIDATE"
private enum class EducationPage { MAP, CHAPTER, SCENE, REVIEW, NOTES, NOTE_EDITOR }

@Composable
fun PublicEducationExperience(repository: EducationRepository?, workspace: WorkspaceRepository?, onBackToToday: () -> Unit = {}) {
    val context = LocalContext.current
    val catalog = remember { runCatching { PublicEducationCatalog.fromAssets(context.assets) }.getOrNull() }
    val scope = rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf(EducationPage.MAP.name) }
    var selectedId by rememberSaveable { mutableStateOf("") }
    var selectedNoteId by rememberSaveable { mutableStateOf("") }
    var snapshot by remember { mutableStateOf<EducationUserDataSnapshot?>(null) }
    var notes by remember { mutableStateOf(emptyList<WorkspaceRecord>()) }
    var revision by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun openOfficialSource(source: PublicEducationSource) {
        val url = source.url ?: source.doi?.let { "https://doi.org/$it" }
        if (url != null && url.startsWith("https://")) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    fun refresh() { revision++ }
    LaunchedEffect(repository, workspace, revision) {
        if (repository != null) runCatching { repository.snapshot() }.onSuccess { snapshot = it }.onFailure { error = "学习数据读取失败：${it.message}" }
        if (workspace != null) runCatching { workspace.all() }.onSuccess { notes = it }.onFailure { error = "个人记录读取失败：${it.message}" }
    }
    fun go(next: EducationPage, id: String = "") { page = next.name; selectedId = id }
    fun back() {
        when (EducationPage.valueOf(page)) {
            EducationPage.MAP -> onBackToToday()
            EducationPage.CHAPTER -> go(EducationPage.MAP)
            EducationPage.SCENE -> go(EducationPage.CHAPTER, catalog?.node(selectedId)?.parentId ?: "")
            EducationPage.REVIEW, EducationPage.NOTES -> go(EducationPage.MAP)
            EducationPage.NOTE_EDITOR -> go(EducationPage.NOTES)
        }
    }
    BackHandler(enabled = true, onBack = ::back)

    if (catalog == null) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("教育内容暂不可用", style = MaterialTheme.typography.headlineSmall)
            Text("打包的课程清单未通过完整性或内容类型校验。请更新应用后重试。")
            error?.let { Text(it) }
            Action("返回", onBackToToday)
        }
        return
    }
    val currentPage = EducationPage.valueOf(page)
    val statusById = remember(snapshot) { snapshot?.progress.orEmpty().associate { it.contentId to it.state } }
    val reviewById = remember(snapshot) { snapshot?.reviewStates.orEmpty().associateBy { it.contentId } }
    val savedChoiceByPrompt = remember(snapshot) { snapshot?.committedChoices.orEmpty().associateBy { it.promptContentId } }
    val assocByRecord = remember(snapshot) { snapshot?.records.orEmpty().associateBy { it.workspaceRecordId } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = ::back, modifier = Modifier.heightIn(min = 48.dp).testTag("education_back")) { Text("← 返回") }
            Text("一般睡眠教育", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 14.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (currentPage) {
                EducationPage.MAP -> {
                    val course = catalog.nodes.values.first { it.kind == "COURSE" }
                    Text("知识地图", style = MaterialTheme.typography.headlineMedium)
                    Text(course.title, style = MaterialTheme.typography.titleLarge)
                    Text("课程按原有章节和场景组织。打开内容只保存阅读位置，不表示已完成、掌握或适合治疗。")
                    catalog.nodes.values.filter { it.kind == "MAP_ENTRY" }.forEach { MapEntry(it, catalog, ::openOfficialSource) }
                    catalog.nodes.values.filter { it.kind == "MAP_GROUP" }.sortedBy { it.payload.optInt("order", Int.MAX_VALUE) }.forEach { group ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(group.title, style = MaterialTheme.typography.titleMedium)
                                renderPayload(group.payload, "MAP_GROUP")
                                catalog.children(group.id).filter { it.kind == "MAP_BRANCH" }.forEach { branch ->
                                    val target = branch.payload.optString("chapterId").takeIf(String::isNotBlank)?.let { targetId ->
                                        catalog.nodes.values.firstOrNull { it.kind == "CHAPTER" && it.payload.optString("id") == targetId }
                                            ?: catalog.nodes.values.firstOrNull { it.kind == "SCENE" && it.payload.optString("id") == targetId }
                                    }
                                    Text(branch.title, style = MaterialTheme.typography.titleSmall)
                                    renderPayload(branch.payload, branch.kind)
                                    LinkedKnowledgeEntries(branch, catalog, ::openOfficialSource)
                                    if (target != null) OutlinedButton(onClick = { go(if (target.kind == "SCENE") EducationPage.SCENE else EducationPage.CHAPTER, target.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                        Text(if (target.kind == "SCENE") "查看相关内容：${target.title}" else "打开章节：${target.title}", maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    } else Text("此地图节点尚未关联到公开课程内容。")
                                }
                            }
                        }
                    }
                    Text("章节", style = MaterialTheme.typography.titleLarge)
                    catalog.nodes.values.filter { it.kind == "CHAPTER" }.sortedBy { it.payload.optInt("order", Int.MAX_VALUE) }.forEach { chapter ->
                        val progress = catalog.children(chapter.id).filter { it.kind == "SCENE" }.mapNotNull { statusById[it.id] }
                        val reviewPresent = catalog.children(chapter.id).filter { it.kind == "SCENE" }.any { it.id in reviewById }
                        val stateText = when { progress.any { it == "IN_PROGRESS" } -> "正在学习"; progress.isNotEmpty() && progress.all { it == "COMPLETED" } -> "已完成"; reviewPresent -> "有稍后复习内容"; else -> "未开始" }
                        OutlinedButton(onClick = { go(EducationPage.CHAPTER, chapter.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("${chapter.title}　·　$stateText") }
                    }
                    HorizontalDivider()
                    Action("继续上次学习", {
                        val position = snapshot?.positions?.firstOrNull { it.contentVersionId == VERSION }
                        if (position != null) go(EducationPage.SCENE, position.sceneContentId) else message = "还没有保存的学习位置。打开一个场景后会保存位置。"
                    })
                    Action("稍后复习", { go(EducationPage.REVIEW) })
                    Action("学习记录与笔记", { go(EducationPage.NOTES) })
                    Text("学习不会解锁或启动 CBT-I 治疗；内容也不会读取睡眠日记来生成建议。", style = MaterialTheme.typography.bodySmall)
                }
                EducationPage.CHAPTER -> {
                    val chapter = catalog.node(selectedId)
                    Text(chapter.title, style = MaterialTheme.typography.headlineSmall)
                    renderPayload(chapter.payload, chapter.kind)
                    catalog.children(chapter.id).filter { it.kind == "SCENE" }.forEach { scene ->
                        val status = when (statusById[scene.id]) { "COMPLETED" -> "已完成"; "IN_PROGRESS" -> "正在学习"; else -> "未开始" }
                        OutlinedButton(onClick = {
                            go(EducationPage.SCENE, scene.id)
                            scope.launch { runCatching { repository?.updatePosition(VERSION, chapter.id.let { "COURSE-V4" }, scene.id) }.onFailure { error = it.message } }
                        }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("education_scene_${scene.id}")) { Text("${scene.title}　·　$status") }
                    }
                }
                EducationPage.SCENE -> {
                    val scene = catalog.node(selectedId)
                    val chapter = catalog.node(checkNotNull(scene.parentId))
                    LaunchedEffect(scene.id) {
                        runCatching {
                            repository?.updatePosition(VERSION, "COURSE-V4", scene.id)
                            if (statusById[scene.id] == null) repository?.markInProgress(VERSION, scene.id)
                        }.onFailure { error = it.message }
                    }
                    Text(scene.title, style = MaterialTheme.typography.headlineSmall)
                    renderPayload(scene.payload, scene.kind)
                    val children = catalog.children(scene.id)
                    children.filter { it.kind != "CHOICE" }.forEach { child ->
                        when (child.kind) {
                            "NOTE_PROMPT", "REFLECTION" -> EducationNotePrompt(child, scene, repository, workspace, assocByRecord, notes, ::refresh) { error = it }
                            else -> EducationContentBlock(child, catalog, onSource = ::openOfficialSource)
                        }
                    }
                    val choices = children.filter { it.kind == "CHOICE" }
                    if (choices.isNotEmpty()) EducationChoiceBlock(scene, choices, savedChoiceByPrompt, reviewById, catalog, repository, ::refresh) { error = it }
                    val review = reviewById[scene.id]
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            scope.launch { runCatching { if (review == null) repository?.saveForReview(VERSION, scene.id) else repository?.removeReviewState(VERSION, scene.id); refresh() }.onFailure { error = it.message } }
                        }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("education_review_toggle_${scene.id}")) { Text(if (review?.reviewState == "SAVED_FOR_REVIEW") "移出稍后复习" else "稍后复习") }
                        if (review?.reviewState == "SAVED_FOR_REVIEW") OutlinedButton(onClick = { scope.launch { runCatching { repository?.markReviewed(VERSION, scene.id); refresh() }.onFailure { error = it.message } } }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("education_mark_reviewed_${scene.id}")) { Text("已复习") }
                    }
                    Action(if (statusById[scene.id] == "COMPLETED") "重新打开本节" else "完成本节并继续", {
                        scope.launch { runCatching {
                            if (statusById[scene.id] == "COMPLETED") repository?.reopen(VERSION, scene.id) else repository?.completeContent(VERSION, scene.id)
                            refresh()
                            val next = scene.nextId?.let(catalog.nodes::get)
                            if (next != null && next.parentId == chapter.id) {
                                repository?.updatePosition(VERSION, "COURSE-V4", next.id); selectedId = next.id
                            } else go(EducationPage.CHAPTER, chapter.id)
                        }.onFailure { error = it.message } }
                    }, modifier = Modifier.testTag("education_complete_${scene.id}"))
                    SourceList(scene, catalog)
                }
                EducationPage.REVIEW -> {
                    Text("稍后复习", style = MaterialTheme.typography.headlineSmall)
                    val rows = snapshot?.reviewStates.orEmpty().filter { it.reviewState == "SAVED_FOR_REVIEW" }
                    if (rows.isEmpty()) Text("还没有保存为稍后复习的内容。")
                    rows.forEach { row ->
                        val node = catalog.nodes[row.contentId]
                        if (node != null) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                            Text(node.title, style = MaterialTheme.typography.titleMedium)
                            Action("打开", { go(EducationPage.SCENE, catalog.enclosingScene(node.id) ?: node.id) })
                            OutlinedButton(onClick = { scope.launch { runCatching { repository?.markReviewed(VERSION, node.id); refresh() }.onFailure { error = it.message } } }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("已复习") }
                            TextButton(onClick = { scope.launch { runCatching { repository?.removeReviewState(VERSION, node.id); refresh() }.onFailure { error = it.message } } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("移除") }
                        } }
                    }
                    snapshot?.reviewStates.orEmpty().filter { it.reviewState == "REVIEWED" }.forEach { row -> Text("已复习：${catalog.nodes[row.contentId]?.title ?: row.contentId}") }
                }
                EducationPage.NOTES -> {
                    Text("学习记录与笔记", style = MaterialTheme.typography.headlineSmall)
                    val links = snapshot?.records.orEmpty()
                    if (links.isEmpty()) Text("还没有保存的学习笔记。")
                    links.forEach { link ->
                        val record = notes.firstOrNull { it.id == link.workspaceRecordId } ?: return@forEach
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(record.title, style = MaterialTheme.typography.titleMedium)
                            Text("关联内容：${catalog.nodes[link.contentId]?.title ?: link.contentId}")
                            Text(record.body)
                            Action("编辑", { selectedNoteId = record.id; selectedId = link.contentId; go(EducationPage.NOTE_EDITOR, link.contentId) })
                            OutlinedButton(onClick = { scope.launch { runCatching { repository?.unlinkAuthoredRecord(record.id); refresh() }.onFailure { error = it.message } } }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("只移除学习关联，保留个人记录") }
                            TextButton(onClick = { scope.launch { runCatching { repository?.deleteAuthoredRecord(record.id); refresh() }.onFailure { error = it.message } } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("删除笔记和关联") }
                        } }
                    }
                }
                EducationPage.NOTE_EDITOR -> {
                    val record = notes.firstOrNull { it.id == selectedNoteId }
                    var title by rememberSaveable(selectedNoteId) { mutableStateOf(record?.title ?: "") }
                    var body by rememberSaveable(selectedNoteId) { mutableStateOf(record?.body ?: "") }
                    Text("编辑学习笔记", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(title, { title = it }, label = { Text("标题") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(body, { body = it }, label = { Text("个人记录") }, minLines = 5, modifier = Modifier.fillMaxWidth())
                    Action("保存", { scope.launch { runCatching {
                        val link = assocByRecord[selectedNoteId] ?: error("此记录已解除学习关联")
                        val now = Instant.now().toString()
                        repository?.saveAuthoredRecord(record!!.copy(title = title, body = body, updatedAtUtc = now, revision = record.revision + 1), link)
                        refresh(); go(EducationPage.NOTES)
                    }.onFailure { error = it.message } } })
                }
            }
        }
    }
    if (message != null) AlertDialog(onDismissRequest = { message = null }, text = { Text(message!!) }, confirmButton = { TextButton(onClick = { message = null }) { Text("知道了") } })
    if (error != null) AlertDialog(onDismissRequest = { error = null }, title = { Text("操作未完成") }, text = { Text(error ?: "") }, confirmButton = { TextButton(onClick = { error = null }) { Text("知道了") } })
}

@Composable
private fun MapEntry(node: PublicEducationNode, catalog: PublicEducationCatalog, onSource: (PublicEducationSource) -> Unit) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(node.title, style = MaterialTheme.typography.titleMedium); renderPayload(node.payload, node.kind)
        LinkedKnowledgeEntries(node, catalog, onSource)
    } }
}

@Composable
private fun LinkedKnowledgeEntries(parent: PublicEducationNode, catalog: PublicEducationCatalog, onSource: (PublicEducationSource) -> Unit) {
    val ids = parent.payload.optJSONArray("entryIds") ?: return
    for (i in 0 until ids.length()) {
        val reference = ids.optString(i)
        val entry = catalog.nodes.values.firstOrNull { it.kind == "KNOWLEDGE_ENTRY" && (it.id == reference || it.payload.optString("id") == reference) }
        if (entry != null) EducationContentBlock(entry, catalog, onSource = onSource)
    }
}

@Composable
private fun EducationContentBlock(node: PublicEducationNode, catalog: PublicEducationCatalog, onSource: (PublicEducationSource) -> Unit) {
    var expanded by rememberSaveable(node.id) { mutableStateOf(node.kind in setOf("SAFETY_NOTICE", "COGNITIVE_EXERCISE", "SIM_STAGE")) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(node.title, style = MaterialTheme.typography.titleMedium)
            if (node.kind in setOf("CONTENT_BLOCK", "EVIDENCE_CLAIM", "KNOWLEDGE_ENTRY")) {
                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (expanded) "收起详细解释" else "展开详细解释") }
                if (expanded) renderPayload(node.payload, node.kind)
            } else if (node.kind in setOf("SPECIAL_BLOCK", "SAFETY_NOTICE", "COGNITIVE_EXERCISE", "SIM_STAGE", "SIM_OUTCOME", "CHECKLIST_GROUP", "CHECKLIST_ITEM", "ANALYZER_CASE", "ANALYZER_LEVEL", "ANALYZER_DIMENSION", "SIM_METER", "MAP_ENTRY", "MAP_BRANCH", "MAP_GROUP")) {
                renderPayload(node.payload, node.kind)
            } else {
                TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (expanded) "收起说明" else "展开说明") }
                if (expanded) renderPayload(node.payload, node.kind)
            }
            SourceList(node, catalog, onSource)
        }
    }
}

@Composable
private fun SourceList(node: PublicEducationNode, catalog: PublicEducationCatalog, onSource: (PublicEducationSource) -> Unit = {}) {
    val refs = node.sourceRefs
    if (refs.length() == 0) return
    var open by rememberSaveable(node.id) { mutableStateOf(false) }
    TextButton(onClick = { open = !open }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (open) "收起来源" else "来源与依据") }
    if (open) for (i in 0 until refs.length()) {
        val ref = refs.getJSONObject(i); val source = catalog.sources[ref.getString("sourceId")] ?: continue
        Text(source.shortName.ifBlank { source.title }, style = MaterialTheme.typography.titleSmall)
        Text(source.organization)
        source.version?.let { Text(it) }; source.journal?.let { Text(it) }
        Text("用途：${source.usage}")
        ref.optString("locator").takeIf(String::isNotBlank)?.let { Text("相关位置：$it") }
        if (!source.url.isNullOrBlank()) TextButton(onClick = { onSource(source) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("打开官方来源") }
        HorizontalDivider()
    }
}

@Composable
private fun EducationChoiceBlock(
    scene: PublicEducationNode,
    choices: List<PublicEducationNode>,
    committed: Map<String, app.sleepdiary.offline.data.entity.EducationCommittedChoice>,
    reviews: Map<String, app.sleepdiary.offline.data.entity.EducationReviewState>,
    catalog: PublicEducationCatalog,
    repository: EducationRepository?,
    refresh: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var preview by remember(scene.id) { mutableStateOf<EducationChoicePreview?>(null) }
    val existing = committed[scene.id]
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("先预览，再决定是否保存", style = MaterialTheme.typography.titleMedium)
        choices.forEach { choice ->
            OutlinedButton(onClick = {
                runCatching { repository?.previewChoice(VERSION, scene.id, choice.id, choice.payload.getString("choiceId")) }
                    .onSuccess { preview = it }.onFailure { onError(it.message ?: "无法预览该选项") }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(choice.title) }
        }
        preview?.let { value ->
            val selected = choices.firstOrNull { it.id == value.choiceContentId }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
                Text("预览：${selected?.title.orEmpty()}", style = MaterialTheme.typography.titleSmall)
                val feedback = selected?.payload?.optJSONObject("preview")?.optJSONObject("feedback")
                feedback?.optString("perspective")?.takeIf(String::isNotBlank)?.let { Text(it) }
                feedback?.optString("plainExplanation")?.takeIf(String::isNotBlank)?.let { Text(it) }
                feedback?.optString("educationalBoundary")?.takeIf(String::isNotBlank)?.let { Text(it) }
                Text("此预览暂时只在当前页面显示，尚未写入学习记录。", style = MaterialTheme.typography.bodySmall)
                selected?.let { SourceList(it, catalog) }
                selected?.let { choice -> catalog.children(choice.id).filter { it.kind == "REVIEW_ITEM" }.forEach { reviewItem ->
                    val row = reviews[reviewItem.id]
                    Text(reviewItem.title, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            scope.launch { runCatching { if (row == null) repository?.saveForReview(VERSION, reviewItem.id) else repository?.removeReviewState(VERSION, reviewItem.id); refresh() }
                                .onFailure { onError(it.message ?: "更新复习标记失败") } }
                        }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (row?.reviewState == "SAVED_FOR_REVIEW") "移除复习项" else "稍后复习") }
                        if (row?.reviewState == "SAVED_FOR_REVIEW") OutlinedButton(onClick = {
                            scope.launch { runCatching { repository?.markReviewed(VERSION, reviewItem.id); refresh() }.onFailure { onError(it.message ?: "更新复习状态失败") } }
                        }, modifier = Modifier.heightIn(min = 48.dp)) { Text("已复习") }
                    }
                } }
                Action("确认并保存选择", {
                    scope.launch { runCatching { repository?.commitChoice(value); preview = null; refresh() }.onFailure { onError(it.message ?: "保存选择失败") } }
                }, modifier = Modifier.testTag("education_commit_${scene.id}"))
                TextButton(onClick = { preview = null }, modifier = Modifier.heightIn(min = 48.dp)) { Text("关闭预览") }
            } }
        }
        existing?.let { row ->
            val saved = choices.firstOrNull { it.id == row.choiceContentId }
            Text("已确认保存：${saved?.title ?: row.optionId}")
        }
    }
}

@Composable
private fun EducationNotePrompt(
    prompt: PublicEducationNode,
    scene: PublicEducationNode,
    repository: EducationRepository?,
    workspace: WorkspaceRepository?,
    associations: Map<String, EducationRecord>,
    notes: List<WorkspaceRecord>,
    refresh: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val linked = associations.values.firstOrNull { it.contentId == scene.id && it.promptId == prompt.id }
    val old = linked?.let { link -> notes.firstOrNull { it.id == link.workspaceRecordId } }
    var text by rememberSaveable(prompt.id, old?.id, old?.updatedAtUtc) { mutableStateOf(old?.body.orEmpty()) }
    var expanded by rememberSaveable(prompt.id) { mutableStateOf(old != null) }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(prompt.title, style = MaterialTheme.typography.titleMedium)
        Text(prompt.payload.optString("placeholder").ifBlank { "按自己的想法写几句即可，不需要标准答案。" })
        if (!expanded) OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("education_note_toggle_${prompt.id}")) { Text(if (old == null) "写一条个人记录：${prompt.title}" else "编辑这条个人记录：${prompt.title}") }
        if (expanded) {
            OutlinedTextField(text, { text = it }, minLines = 3, label = { Text("只保存你主动写下的内容") }, modifier = Modifier.fillMaxWidth().testTag("education_note_${prompt.id}"))
            Action(if (old == null) "保存到我的学习记录" else "保存修改", {
                scope.launch { runCatching {
                    require(!text.isBlank()) { "请先输入个人记录，或关闭此编辑框。" }
                    val now = Instant.now().toString(); val id = old?.id ?: UUID.randomUUID().toString()
                    val recordKind = if (prompt.kind == "REFLECTION") "REFLECTION" else "NOTE"
                    val workspaceKind = if (prompt.kind == "REFLECTION") "REFLECTION" else "LEARNING_NOTE"
                    val record = WorkspaceRecord(id, old?.kind ?: workspaceKind, old?.title ?: prompt.title, text,
                        old?.createdAtUtc ?: now, now, (old?.revision ?: 0) + 1)
                    val association = linked?.copy(contentId = scene.id, promptId = prompt.id) ?: EducationRecord(
                        workspaceRecordId = id, contentVersionId = VERSION, contentId = scene.id,
                        recordKind = recordKind, promptId = prompt.id, createdAtUtc = now
                    )
                    checkNotNull(repository) { "学习数据服务不可用" }.saveAuthoredRecord(record, association)
                    refresh(); expanded = false
                }.onFailure { onError(it.message ?: "保存学习记录失败") } }
            })
            TextButton(onClick = { expanded = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("收起") }
        }
    } }
}

@Composable
private fun renderPayload(payload: JSONObject, kind: String) {
    val technical = setOf("id", "order", "type", "kind", "unitRef", "sources", "source", "chapterId", "entryIds", "commit", "reviewEligible", "unlockAfter", "durationSec", "choiceId", "imageHint", "icon", "triggerAria", "triggerLabel", "doi", "url", "fullTextUrl", "openAccessUrl", "patientGuideUrl", "protocolVersion", "verifiedDate", "verified", "isLearning", "isLine", "isFixedSize", "allowNoBetter", "hasAnyResultNote", "notCollected", "sectionsUsed", "settingsExtra", "entryIds", "count", "rank", "longLength", "isReadOnly", "isSynchronized")
    @Composable fun value(key: String, item: Any?) {
        when (item) {
            is JSONObject -> {
                val keys = item.keys().asSequence().toList()
                keys.filterNot { it in technical }.forEach { childKey ->
                    val child = item.opt(childKey)
                    if (child != null && child !== JSONObject.NULL) {
                        if (child is String && child.isBlank()) return@forEach
                        val title = label(childKey)
                        if (title != null && child is String) Text(title, style = MaterialTheme.typography.titleSmall)
                        value(childKey, child)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until item.length()) {
                    val child = item.opt(i)
                    if (child is String) Text("• $child") else value(key, child)
                }
            }
            is String -> if (item.isNotBlank() && key !in technical && item !in setOf("TRANSIENT_ONLY", "EXPLICIT_USER_CONFIRMATION", "EducationRecord")) Text(item)
            is Number, is Boolean -> if (key !in technical) Text("${label(key) ?: key}: $item")
        }
    }
    value(kind, payload)
}

private fun label(key: String): String? = when (key) {
    "perspective" -> "换个角度看"
    "plain", "plainExplanation" -> "通俗解释"
    "long", "details", "professional", "professionalDetail" -> "展开了解"
    "text", "body" -> ""
    "claim" -> "资料中的一般说法"
    "belief" -> "原先想法"
    "realPart" -> "现实情况"
    "hiddenStep" -> "第一步：留意当下的想法"
    "reframe" -> "第二步：看看还有没有别的解释"
    "conclusion" -> "小结"
    "why" -> "为什么会这样"
    "mechanism" -> "一般原理"
    "meaningNote" -> "含义"
    "note", "notePrompt" -> "补充说明"
    "prompt" -> "想一想"
    "hint" -> "提示"
    "example" -> "例子"
    "scenario" -> "情境"
    "choices" -> "可以选择"
    "preview" -> "预览"
    "sources" -> "来源"
    "steps" -> "步骤"
    "whatItIs" -> "是什么"
    "whatItIsNot" -> "不是什么"
    "notDo" -> "这里不会做什么"
    "safetyNotice" -> "安全边界"
    "usage" -> "用途"
    "nature" -> "内容性质"
    "verifiedNote" -> "核查说明"
    "closing" -> "本节结束"
    "intro" -> "本节导读"
    "lines" -> null
    else -> null
}
