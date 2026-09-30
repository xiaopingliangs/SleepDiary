package app.sleepdiary.offline.domain.education

/** Static release content only. None of these models contain user state. */
enum class EducationSourceRole {
    EDUCATION_PRIMARY,
    SAFETY_CONTEXT,
    EVIDENCE_SUPPORT,
    LOCALIZATION_CONTEXT
}

enum class EducationContentBlockKind { PARAGRAPH }

enum class EducationBoundaryKind {
    GENERAL_EDUCATION,
    SAFETY_CONTEXT,
    SELF_REFLECTION
}

data class EducationSource(
    val sourceId: String,
    val role: EducationSourceRole,
    val titleOriginal: String,
    val titleChinese: String,
    val authors: String,
    val publisher: String,
    val publicationYear: Int,
    val editionOrVersion: String,
    val officialUrl: String,
    val usage: String
)

data class EducationSourceRef(
    val sourceId: String,
    val locators: List<String>
)

data class EducationContentBlock(
    val kind: EducationContentBlockKind,
    val text: String
)

data class EducationContentItem(
    val contentId: String,
    val contentVersionId: String,
    val moduleId: String,
    val locale: String,
    val title: String,
    val summary: String,
    val contentBlocks: List<EducationContentBlock>,
    val boundaryNotice: String,
    val boundaryKind: EducationBoundaryKind,
    val sourceRefs: List<EducationSourceRef>,
    val contentHashSha256: String
)

data class EducationContentVersion(
    val versionId: String,
    val schemaVersion: String,
    val locale: String,
    val releasedAtUtc: String,
    val sourceRegistryVersion: String,
    val migratedFromVersion: String,
    val includedContentIds: List<String>,
    val contentHashSha256: String
)

data class EducationContentManifest(
    val schemaVersion: String,
    val sourceRegistryVersion: String,
    val contentVersions: List<EducationContentVersion>
)

data class EducationRegistry(
    val manifest: EducationContentManifest,
    val sources: List<EducationSource>,
    val items: List<EducationContentItem>
)
