package app.sleepdiary.offline.domain.education

/**
 * Immutable Public Edition Education content release.
 *
 * This registry has no Room, navigation, diary, safety or Treatment dependency.
 */
object EducationContentRegistry {
    const val SCHEMA_VERSION = "EDUCATION_CONTENT_SCHEMA_1.0"
    const val SOURCE_REGISTRY_VERSION = "EDUCATION_SOURCES_1.0"
    const val CONTENT_VERSION = "EDUCATION_CONTENT_1.0"
    const val MIGRATED_FROM_VERSION = "EDUCATION_TEXT_1.0"
    const val LOCALE = "zh-CN"
    const val RELEASED_AT_UTC = "2026-09-30T00:00:00Z"
    const val PUBLIC_BOUNDARY = "用于一般学习；不构成诊断、个人建议或可执行治疗计划。"
    const val EXPECTED_VERSION_HASH_SHA256 = "b988e5d87f6a3ffc0c6963b6b6fc4350dc92012aa13c7b6cc64ddecc1eaa514c"
    const val STRUCTURED_SCHEMA_VERSION = "EDUCATION_CONTENT_SCHEMA_1.1"
    const val PUBLIC_CANDIDATE_CONTENT_VERSION = "PUBLIC_EDUCATION_CONTENT_1.0-CANDIDATE"
    const val PUBLIC_MANIFEST_RESOURCE = "/education/public_education_manifest_1_1.json"
    const val PUBLIC_MANIFEST_CONTENT_HASH_SHA256 = "7cdd0009ac4b8953c6af51ccae52398a5ce7b191fb50181988695c478b4b5c7a"
    const val PUBLIC_MANIFEST_FILE_SHA256 = "0d9d19992ea080a4b024cb7a910bf1972b043988166343a1a19732997afad443"

    val primarySource = EducationSource(
        sourceId = "VA-CBTI-2014",
        role = EducationSourceRole.EDUCATION_PRIMARY,
        titleOriginal = "Cognitive Behavioral Therapy for Insomnia in Veterans: Therapist Manual",
        titleChinese = "退伍军人失眠认知行为治疗：治疗师手册（项目译名）",
        authors = "Manber R, et al.",
        publisher = "U.S. Department of Veterans Affairs",
        publicationYear = 2014,
        editionOrVersion = "2014",
        officialUrl = "https://www.mirecc.va.gov/visn19/treatmentworksforvets/docs/CBT-I-Therapist-Manual.pdf",
        usage = "认知教育、睡前缓冲、放松、个人提醒与维持教育；不据此在 Public Edition 生成治疗计划。"
    )

    private data class Draft(
        val id: String,
        val moduleId: String,
        val title: String,
        val summary: String,
        val body: String,
        val pages: List<Int>,
        val boundaryKind: EducationBoundaryKind = EducationBoundaryKind.GENERAL_EDUCATION
    )

    private val drafts = listOf(
        Draft(
            "COG-01", "SLEEP_COGNITIONS", "认识睡眠担忧",
            "睡不好时，容易把对明天的担心当成已经确定的结果。",
            "睡不好时，容易把对明天的担心当成已经确定的结果。可以先写下当时的想法，再看看哪些是已经发生的事实，哪些还只是预测。不需要强迫自己乐观。",
            listOf(61, 62, 63)
        ),
        Draft(
            "COG-SAFETY-01", "SLEEP_SAFETY", "安全担忧要认真对待",
            "如果担心因困倦而在驾驶等活动中发生危险，这不是要靠“想开一点”消除的问题。",
            "如果担心因困倦而在驾驶等活动中发生危险，这不是要靠“想开一点”消除的问题。不要通过冒险来验证自己是否能撑住；应优先处理安全问题。",
            listOf(63), EducationBoundaryKind.SAFETY_CONTEXT
        ),
        Draft(
            "ARO-01", "PRE_SLEEP_AROUSAL", "给睡前留一个缓冲",
            "可以选择自己喜欢、没有完成任务压力的安静活动。",
            "可以选择自己喜欢、没有完成任务压力的安静活动。目的不是立刻把自己变困，也不用用入睡速度来判断做得好不好。",
            listOf(51, 58)
        ),
        Draft(
            "REL-01", "RELAXATION", "放松不是一场考试",
            "不同的人适合不同的方法。",
            "不同的人适合不同的方法。如果越努力放松越紧张，不必把它当成失败，也不必强迫自己继续同一种练习；可以讨论更适合自己的方式。",
            listOf(52)
        ),
        Draft(
            "COG-03", "PERSONAL_REFLECTION", "留下自己的提醒",
            "把你觉得有帮助、也愿意再次尝试的想法记下来。",
            "把你觉得有帮助、也愿意再次尝试的想法记下来。这里不评判答案，也不会根据文字自动生成治疗建议。",
            listOf(65), EducationBoundaryKind.SELF_REFLECTION
        ),
        Draft(
            "MAINT-01", "MAINTENANCE", "为以后做好准备",
            "回顾哪些方法对自己有帮助，以及再次遇到困难时希望向医生问些什么。",
            "回顾哪些方法对自己有帮助，以及再次遇到困难时希望向医生问些什么。重新出现问题时，不由 App 自动恢复以前的睡眠窗口。",
            listOf(68, 112), EducationBoundaryKind.SELF_REFLECTION
        )
    )

    val sources: List<EducationSource> = listOf(primarySource)

    val items: List<EducationContentItem> = drafts.map { draft ->
        val unhashed = EducationContentItem(
            contentId = draft.id,
            contentVersionId = CONTENT_VERSION,
            moduleId = draft.moduleId,
            locale = LOCALE,
            title = draft.title,
            summary = draft.summary,
            contentBlocks = listOf(EducationContentBlock(EducationContentBlockKind.PARAGRAPH, draft.body)),
            boundaryNotice = PUBLIC_BOUNDARY,
            boundaryKind = draft.boundaryKind,
            sourceRefs = listOf(
                EducationSourceRef(primarySource.sourceId, draft.pages.map { "PDF p.$it" })
            ),
            contentHashSha256 = ""
        )
        unhashed.copy(contentHashSha256 = EducationContentHash.item(unhashed))
    }

    private val unhashedVersion = EducationContentVersion(
        versionId = CONTENT_VERSION,
        schemaVersion = SCHEMA_VERSION,
        locale = LOCALE,
        releasedAtUtc = RELEASED_AT_UTC,
        sourceRegistryVersion = SOURCE_REGISTRY_VERSION,
        migratedFromVersion = MIGRATED_FROM_VERSION,
        includedContentIds = items.map { it.contentId },
        contentHashSha256 = ""
    )

    val version: EducationContentVersion = unhashedVersion.copy(
        contentHashSha256 = EducationContentHash.version(unhashedVersion, sources, items)
    )

    val registry = EducationRegistry(
        manifest = EducationContentManifest(
            schemaVersion = SCHEMA_VERSION,
            sourceRegistryVersion = SOURCE_REGISTRY_VERSION,
            contentVersions = listOf(version)
        ),
        sources = sources,
        items = items
    )

    fun byId(contentId: String?): EducationContentItem? = items.firstOrNull { it.contentId == contentId }
}
