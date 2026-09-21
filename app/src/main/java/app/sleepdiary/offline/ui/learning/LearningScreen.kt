package app.sleepdiary.offline.ui.learning

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.sleepdiary.offline.ui.Action
import app.sleepdiary.offline.ui.SimplePage

@Composable
fun LearningScreen(onBack: () -> Unit, onNotes: () -> Unit, onSource: (String) -> Unit) {
    // Only the viewed page is restored. Reading never writes a clinical or learning completion flag.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val card = LearningContent.byId(selectedId)
    BackHandler(enabled = card != null) { selectedId = null }
    SimplePage(card?.title ?: "睡眠学习") {
        Action(if (card == null) "返回设置" else "返回学习目录", {
            if (card == null) onBack() else selectedId = null
        })
        if (card == null) {
            Text("按需阅读，不需要每天完成，也不用在夜间打开。这里提供教育说明，当前未启用 CBT-I 治疗方案。")
            LearningContent.cards.forEach { item -> Action(item.title, { selectedId = item.id }) }
        } else {
            Text(card.body, style = MaterialTheme.typography.bodyLarge)
            if (card.id == "COG-01") {
                Text("安全担忧要认真对待", style = MaterialTheme.typography.titleMedium)
                Text(LearningContent.SAFETY_TEXT)
            }
            Text("可以只阅读，不必写答案。个人记录按需保存，不评分，也不会用于自动调整治疗。")
            Action("打开个人记录", onNotes)
            Text("内容依据", style = MaterialTheme.typography.titleMedium)
            Text(LearningContent.SOURCE_TITLE)
            Text(LearningContent.SOURCE_CHINESE)
            Text("${LearningContent.SOURCE_ATTRIBUTION} · 第 ${card.pages.joinToString("、")} 页")
            Text("本项目独立中文教育表述，不是官方译本或完整治疗练习。记录方式与 App 权限属于产品设计。")
            Action("用浏览器查看官方来源", { onSource(LearningContent.SOURCE_URL) })
        }
    }
}
