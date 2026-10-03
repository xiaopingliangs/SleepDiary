package app.sleepdiary.offline.ui.education

/** Curated presentation routes over immutable V4 registry IDs. No education text is copied here. */
data class EducationExperienceDefinition(
    val id: String,
    val title: String,
    val contentIds: List<String>,
    val sceneId: String,
    val choicePromptId: String? = null
)

object EducationExperienceDefinitions {
    val all = listOf(
        EducationExperienceDefinition(
            "cbti_basics", "CBT-I 到底是什么？",
            listOf("SCENE-PROLOGUE-WHAT", "KNOWLEDGE-VAULT-WHAT-IS-CBTI"),
            "SCENE-PROLOGUE-WHAT"
        ),
        EducationExperienceDefinition(
            "bad_night", "一晚睡差，第二天真的一定完了吗？",
            listOf("SCENE-CH05-CARD1", "COGNITIVE-EXERCISE-CH05-CARD1", "CONTENT-BLOCK-CH05-CARD1-DETAIL"),
            "SCENE-CH05-CARD1"
        ),
        EducationExperienceDefinition(
            "sleep_effort", "为什么越努力睡，反而越清醒？",
            listOf("SCENE-CH05-CARD8", "COGNITIVE-EXERCISE-CH05-CARD8", "CONTENT-BLOCK-CH05-CARD8-DETAIL"),
            "SCENE-CH05-CARD8"
        ),
        EducationExperienceDefinition(
            "morning_time", "为什么固定起床时间这么重要？",
            listOf("SCENE-CH01-WHY-MORNING", "COGNITIVE-EXERCISE-CH01-WHY-MORNING", "CONTENT-BLOCK-CH01-WHY-MORNING-DETAIL", "SCENE-CH01-PLAN"),
            "SCENE-CH01-WHY-MORNING",
            "SCENE-CH01-PLAN"
        )
    )

    fun validate(catalog: PublicEducationCatalog) {
        require(all.map { it.id }.distinct().size == all.size)
        all.forEach { experience ->
            experience.contentIds.forEach(catalog::node)
            require(catalog.node(experience.sceneId).kind == "SCENE")
            experience.choicePromptId?.let { prompt ->
                require(catalog.node(prompt).kind == "SCENE")
                require(catalog.children(prompt).any { it.kind == "CHOICE" })
            }
        }
    }
}
