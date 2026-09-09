package app.sleepdiary.offline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import app.sleepdiary.offline.ui.diary.*

const val FIRST_SAVE_EXPLANATION="为了让记录尽量简单，也减少对单个夜晚数据的反复关注，日常记录完成后应用会结束本次使用。每完成一个 7 天记录周期，再集中查看一次整体记录。"

@Composable
fun SleepDiaryApp(model:DiaryViewModel,onExit:()->Unit) {
    val state by model.state.collectAsState()
    val nav=rememberNavController()
    Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),color=MaterialTheme.colorScheme.background) {
        NavHost(navController=nav,startDestination="home") {
            composable("home") {
                when(state.page) {
                    HomePage.LOADING -> SimplePage("睡眠日记") {Text(state.message ?: "正在读取记录…");if(state.message!=null) Action("重试",model::start)}
                    HomePage.INTRO -> IntroScreen(state.introPage,model::nextIntro)
                    HomePage.TODAY -> SimplePage("今天的记录已保存") {
                        Action("查看 / 修改今天记录",model::editToday)
                        Action("设置",{nav.navigate("settings")})
                    }
                    HomePage.FORM -> DiaryScreen(state,model::answerTime,model::answerNumber,model::comment,model::save,model::dismissWarning,{nav.navigate("settings")})
                    HomePage.SUCCESS -> {
                        BackHandler {model.exitAfterSuccess(onExit)}
                        SimplePage(if(state.firstSuccess) "记录已保存" else "数据已保存成功") {
                            if(state.firstSuccess) Text(FIRST_SAVE_EXPLANATION,style=MaterialTheme.typography.bodyLarge)
                            state.message?.let {Text(it)}
                            Action("退出",{model.exitAfterSuccess(onExit)},!state.busy)
                        }
                    }
                }
            }
            composable("settings") {
                SimplePage("设置") {
                    Action("返回",{nav.popBackStack()})
                    Text("数据",style=MaterialTheme.typography.titleMedium)
                    Action("历史记录",{nav.navigate("history")})
                    listOf("导出完整备份","导出 CSV","恢复完整备份").forEach {Action(it+" · 尚未实现",{},false)}
                    Text("安全与说明",style=MaterialTheme.typography.titleMedium)
                    Action("安全问询 · 尚未实现",{},false)
                    Text("方法",style=MaterialTheme.typography.titleMedium)
                    Action("方法与依据 · 尚未实现",{},false)
                }
            }
            composable("history") {
                Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("历史记录",style=MaterialTheme.typography.headlineSmall)
                    Action("返回",{nav.popBackStack()})
                    Text("已保存记录；历史编辑尚未实现。")
                    if(state.entries.isEmpty()) Text("暂无记录")
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        items(state.entries.reversed(),key={it.id}) {entry ->
                            Card(Modifier.fillMaxWidth()) {Text(entry.sleepDate,Modifier.padding(20.dp))}
                        }
                    }
                }
            }
        }
    }
}

/** Development-stage onboarding: Safety UI is intentionally not connected yet. */
@Composable
fun IntroScreen(page:Int,onNext:()->Unit) {
    val titles=listOf("睡眠日记","数据留在本机","记录不用追求精确")
    val bodies=listOf("一个离线的主观睡眠记录工具。当前版本只记录，不诊断，也不提供 CBT-I 治疗。",
        "不需要账号，不联网。只有你主动导出或分享时，数据才会离开应用。",
        "睡眠时间、次数和时长按记忆大致估计即可。不要为了填写日记在夜间查看时间、手环或其他睡眠设备。")
    SimplePage(titles[page]) {Text(bodies[page],style=MaterialTheme.typography.bodyLarge);Action(if(page==2) "开始记录" else "下一页",onNext)}
}

@Composable
private fun SimplePage(title:String,content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        Spacer(Modifier.height(12.dp));Text(title,style=MaterialTheme.typography.headlineSmall);content()
    }
}
@Composable
private fun Action(label:String,action:()->Unit,enabled:Boolean=true) {
    Button(onClick=action,shape=MaterialTheme.shapes.medium,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) {Text(label)}
}
