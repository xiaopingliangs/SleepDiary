package app.sleepdiary.offline.domain.education

data class EducationValidationIssue(
    val code: String,
    val subject: String,
    val message: String
)

object EducationContentValidator {
    private val contentId = Regex("[A-Z][A-Z0-9-]{2,63}")
    private val moduleId = Regex("[A-Z][A-Z0-9_-]{2,63}")
    private val versionId = Regex("[A-Z][A-Z0-9_.-]{2,79}")
    private val locale = Regex("[a-z]{2}-[A-Z]{2}")
    private val sha256 = Regex("[0-9a-f]{64}")

    private val boundaryRules = listOf(
        "BOUNDARY_PERSONAL_ADVICE" to Regex("根据你的|最适合你|你应该|你必须|建议你(?:把|将|从|在)"),
        "BOUNDARY_TREATMENT_INSTRUCTION" to Regex("睡眠效率.{0,12}(?:%|％)|(?:增加|减少|调整).{0,8}(?:卧床|睡眠窗口).{0,8}(?:分钟|小时)|(?:卧床|睡眠窗口).{0,8}(?:增加|减少|调整).{0,8}(?:分钟|小时)|按周调整"),
        "BOUNDARY_DIAGNOSIS" to Regex("你患有|诊断为|说明你有|你是.{0,4}(?:失眠|疾病)"),
        "BOUNDARY_OUTCOME_PROMISE" to Regex("保证|治愈|照着做|一定(?:会|能).{0,8}(?:改善|睡着|康复)")
    )

    fun validate(registry: EducationRegistry): List<EducationValidationIssue> = buildList {
        val manifest = registry.manifest
        if (!versionId.matches(manifest.schemaVersion)) issue("SCHEMA_VERSION", "manifest", "Invalid schema version")
        if (!versionId.matches(manifest.sourceRegistryVersion)) issue("SOURCE_VERSION", "manifest", "Invalid source registry version")
        if (manifest.contentVersions.isEmpty()) issue("CONTENT_VERSIONS", "manifest", "No content version")

        duplicates(registry.sources.map { it.sourceId }).forEach { issue("DUPLICATE_SOURCE_ID", it, "Source ID is duplicated") }
        duplicates(registry.items.map { it.contentId }).forEach { issue("DUPLICATE_CONTENT_ID", it, "Content ID is duplicated") }
        duplicates(manifest.contentVersions.map { it.versionId }).forEach { issue("DUPLICATE_VERSION_ID", it, "Version ID is duplicated") }

        val sourceIds = registry.sources.map { it.sourceId }.toSet()
        registry.sources.forEach { source ->
            if (!contentId.matches(source.sourceId)) issue("SOURCE_ID", source.sourceId, "Invalid source ID")
            if (source.titleOriginal.isBlank() || source.titleChinese.isBlank() || source.publisher.isBlank() ||
                source.editionOrVersion.isBlank() || source.usage.isBlank()
            ) issue("SOURCE_COMPLETENESS", source.sourceId, "Required source metadata is blank")
            if (!source.officialUrl.startsWith("https://")) issue("SOURCE_URL", source.sourceId, "Official URL must use HTTPS")
        }

        registry.items.forEach { item ->
            if (!contentId.matches(item.contentId)) issue("CONTENT_ID", item.contentId, "Invalid content ID")
            if (!versionId.matches(item.contentVersionId)) issue("CONTENT_VERSION", item.contentId, "Invalid content version")
            if (!moduleId.matches(item.moduleId)) issue("MODULE_ID", item.contentId, "Invalid module ID")
            if (!locale.matches(item.locale)) issue("LOCALE", item.contentId, "Invalid locale")
            if (item.title.isBlank() || item.summary.isBlank() || item.boundaryNotice.isBlank() || item.contentBlocks.isEmpty() ||
                item.contentBlocks.any { it.text.isBlank() } || item.sourceRefs.isEmpty()
            ) issue("CONTENT_COMPLETENESS", item.contentId, "Required content is blank")
            item.sourceRefs.forEach { ref ->
                if (ref.sourceId !in sourceIds) issue("UNKNOWN_SOURCE", item.contentId, "Unknown source ${ref.sourceId}")
                if (ref.locators.isEmpty() || ref.locators.any { it.isBlank() }) {
                    issue("SOURCE_LOCATOR", item.contentId, "Every source reference needs a locator")
                }
            }
            val expectedHash = EducationContentHash.item(item.copy(contentHashSha256 = ""))
            if (!sha256.matches(item.contentHashSha256) || item.contentHashSha256 != expectedHash) {
                issue("CONTENT_HASH", item.contentId, "Content hash does not match canonical content")
            }
            val text = buildString {
                append(item.title).append('\n').append(item.summary).append('\n')
                item.contentBlocks.forEach { append(it.text).append('\n') }
            }
            boundaryRules.forEach { (code, pattern) ->
                if (pattern.containsMatchIn(text)) issue(code, item.contentId, "Public Education boundary phrase detected")
            }
        }

        manifest.contentVersions.forEach { version ->
            if (!versionId.matches(version.versionId) || !locale.matches(version.locale) || version.releasedAtUtc.isBlank()) {
                issue("VERSION_COMPLETENESS", version.versionId, "Content version metadata is invalid")
            }
            val selected = registry.items.filter { it.contentVersionId == version.versionId }
            if (version.includedContentIds != selected.map { it.contentId }) {
                issue("MANIFEST_CONTENT_IDS", version.versionId, "Manifest IDs do not match registry order")
            }
            val expectedHash = EducationContentHash.version(version.copy(contentHashSha256 = ""), registry.sources, selected)
            if (!sha256.matches(version.contentHashSha256) || version.contentHashSha256 != expectedHash) {
                issue("VERSION_HASH", version.versionId, "Version hash does not match canonical registry")
            }
        }
    }

    private fun duplicates(values: List<String>): Set<String> = values.groupingBy { it }.eachCount()
        .filterValues { it > 1 }.keys

    private fun MutableList<EducationValidationIssue>.issue(code: String, subject: String, message: String) {
        add(EducationValidationIssue(code, subject, message))
    }
}
