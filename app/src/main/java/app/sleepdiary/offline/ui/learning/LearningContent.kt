package app.sleepdiary.offline.ui.learning

/** Educational text only. These entries have no treatment actions, scores or completion state. */
data class LearningCard(val id: String, val title: String, val body: String, val pages: List<Int>)

object LearningContent {
    const val VERSION = "EDUCATION_TEXT_1.0"
    const val SOURCE_TITLE = "Cognitive Behavioral Therapy for Insomnia in Veterans: Therapist Manual"
    const val SOURCE_CHINESE = "退伍军人失眠认知行为治疗：治疗师手册（项目译名）"
    const val SOURCE_ATTRIBUTION = "Manber 等 · VA · 2014"
    const val SOURCE_URL = "https://www.mirecc.va.gov/visn19/treatmentworksforvets/docs/CBT-I-Therapist-Manual.pdf"
    const val SAFETY_TEXT = "如果担心因困倦而在驾驶等活动中发生危险，这不是要靠“想开一点”消除的问题。不要通过冒险来验证自己是否能撑住；应优先处理安全问题。"
    val cards: List<LearningCard> = listOf(
        LearningCard("COG-01", "认识睡眠担忧",
            "睡不好时，容易把对明天的担心当成已经确定的结果。可以先写下当时的想法，再看看哪些是已经发生的事实，哪些还只是预测。不需要强迫自己乐观。", listOf(61, 62, 63)),
        LearningCard("COG-SAFETY-01", "安全担忧要认真对待", SAFETY_TEXT, listOf(63)),
        LearningCard("ARO-01", "给睡前留一个缓冲",
            "可以选择自己喜欢、没有完成任务压力的安静活动。目的不是立刻把自己变困，也不用用入睡速度来判断做得好不好。", listOf(51, 58)),
        LearningCard("REL-01", "放松不是一场考试",
            "不同的人适合不同的方法。如果越努力放松越紧张，不必把它当成失败，也不必强迫自己继续同一种练习；可以讨论更适合自己的方式。", listOf(52)),
        LearningCard("COG-03", "留下自己的提醒",
            "把你觉得有帮助、也愿意再次尝试的想法记下来。这里不评判答案，也不会根据文字自动生成治疗建议。", listOf(65)),
        LearningCard("MAINT-01", "为以后做好准备",
            "回顾哪些方法对自己有帮助，以及再次遇到困难时希望向医生问些什么。重新出现问题时，不由 App 自动恢复以前的睡眠窗口。", listOf(68, 112))
    )
    fun byId(id: String?): LearningCard? = cards.firstOrNull { it.id == id }
}
