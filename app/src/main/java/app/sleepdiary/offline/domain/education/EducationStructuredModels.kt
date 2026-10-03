package app.sleepdiary.offline.domain.education

/** Public Education content classification; only the first three enter a public manifest. */
enum class EducationContentClassification {
    PUBLIC_AS_IS,
    PUBLIC_WITH_PRESENTATION_ADAPTATION,
    PUBLIC_CONCEPT_ONLY,
    FUTURE_TREATMENT_ONLY,
    BLOCKED
}

enum class EducationStructuredNodeKind {
    COURSE,
    MAP_ENTRY,
    MAP_GROUP,
    MAP_BRANCH,
    CHAPTER,
    SCENE,
    CONTENT_BLOCK,
    CHOICE,
    SIM_METER,
    SIM_STAGE,
    SIM_OUTCOME,
    ANALYZER_DIMENSION,
    ANALYZER_LEVEL,
    ANALYZER_CASE,
    CHECKLIST_GROUP,
    CHECKLIST_ITEM,
    COGNITIVE_EXERCISE,
    REFLECTION,
    NOTE_PROMPT,
    REVIEW_ITEM,
    KNOWLEDGE_ENTRY,
    EVIDENCE_CLAIM,
    SAFETY_NOTICE,
    SOURCE,
    EXPORT_ELEMENT
}

enum class EducationTextRelation {
    PARAPHRASE,
    PARAPHRASE_WITH_SOURCE_EXCERPT_IN_V4
}

data class EducationStructuredSourceRef(
    val sourceId: String,
    val locator: String,
    val textRelation: EducationTextRelation,
    val claimId: String? = null
)

data class EducationLayeredExplanation(
    val perspective: String?,
    val plainExplanation: String?,
    val professionalDetail: String?
)

data class EducationPreviewFeedback(
    val perspective: String,
    val plainExplanation: String?,
    val educationalBoundary: String
)

data class EducationChoiceDefinition(
    val choiceId: String,
    val label: String,
    val previewFeedback: EducationPreviewFeedback,
    val commitValueId: String,
    val reviewEligible: Boolean,
    val sourceRefs: List<EducationStructuredSourceRef>
)

data class EducationSceneDefinition(
    val sceneId: String,
    val chapterId: String,
    val sceneType: String,
    val title: String?,
    val layeredExplanation: EducationLayeredExplanation?,
    val choices: List<EducationChoiceDefinition>,
    val safetyNoticeId: String?,
    val reflectionPromptIds: List<String>,
    val notePromptIds: List<String>,
    val nextSceneId: String?,
    val sourceRefs: List<EducationStructuredSourceRef>
)

data class EducationChapterDefinition(
    val chapterId: String,
    val courseId: String,
    val title: String,
    val subtitle: String?,
    val sceneIds: List<String>
)

data class EducationMapBranchDefinition(
    val branchId: String,
    val groupId: String,
    val title: String,
    val explanation: String,
    val chapterId: String?,
    val knowledgeEntryIds: List<String>
)

data class EducationMapGroupDefinition(
    val groupId: String,
    val title: String,
    val explanation: String,
    val branchIds: List<String>
)

data class EducationCourseDefinition(
    val courseId: String,
    val locale: String,
    val title: String,
    val mapGroupIds: List<String>,
    val chapterIds: List<String>
)

/** Preview is transient; only a user's explicit commit may become P2 durable state. */
data class EducationCommittedChoiceContract(
    val contentId: String,
    val contentVersionId: String,
    val sceneId: String,
    val choiceId: String,
    val commitValueId: String
)

data class EducationReflectionContract(
    val promptId: String,
    val contentId: String,
    val userAuthoredText: Boolean,
    val interpretationAllowed: Boolean = false
)

data class EducationReviewEligibility(
    val contentId: String,
    val reviewEligible: Boolean,
    val automaticSchedulingAllowed: Boolean = false
)
