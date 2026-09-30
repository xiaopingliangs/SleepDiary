package app.sleepdiary.offline.ui.learning

import app.sleepdiary.offline.domain.education.EducationContentRegistry

/** Educational text only. These entries have no treatment actions, scores or completion state. */
data class LearningCard(val id: String, val title: String, val body: String, val pages: List<Int>)

object LearningContent {
    const val VERSION = EducationContentRegistry.CONTENT_VERSION
    val SOURCE_TITLE = EducationContentRegistry.primarySource.titleOriginal
    val SOURCE_CHINESE = EducationContentRegistry.primarySource.titleChinese
    const val SOURCE_ATTRIBUTION = "Manber 等 · VA · 2014"
    val SOURCE_URL = EducationContentRegistry.primarySource.officialUrl
    val SAFETY_TEXT = checkNotNull(EducationContentRegistry.byId("COG-SAFETY-01"))
        .contentBlocks.single().text
    val cards: List<LearningCard> = EducationContentRegistry.items.map { item ->
        LearningCard(
            id = item.contentId,
            title = item.title,
            body = item.contentBlocks.single().text,
            pages = item.sourceRefs.single().locators.map { it.removePrefix("PDF p.").toInt() }
        )
    }
    fun byId(id: String?): LearningCard? = cards.firstOrNull { it.id == id }
}
