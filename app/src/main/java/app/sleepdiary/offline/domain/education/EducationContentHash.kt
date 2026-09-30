package app.sleepdiary.offline.domain.education

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Canonical UTF-8 hashing for immutable Education releases. */
object EducationContentHash {
    fun item(item: EducationContentItem): String = sha256(buildString {
        field("contentId", item.contentId)
        field("contentVersionId", item.contentVersionId)
        field("moduleId", item.moduleId)
        field("locale", item.locale)
        field("title", item.title)
        field("summary", item.summary)
        item.contentBlocks.forEachIndexed { index, block ->
            field("block[$index].kind", block.kind.name)
            field("block[$index].text", block.text)
        }
        field("boundaryNotice", item.boundaryNotice)
        field("boundaryKind", item.boundaryKind.name)
        item.sourceRefs.forEachIndexed { index, ref ->
            field("sourceRef[$index].sourceId", ref.sourceId)
            ref.locators.forEachIndexed { locatorIndex, locator ->
                field("sourceRef[$index].locator[$locatorIndex]", locator)
            }
        }
    })

    fun version(
        version: EducationContentVersion,
        sources: List<EducationSource>,
        items: List<EducationContentItem>
    ): String = sha256(buildString {
        field("versionId", version.versionId)
        field("schemaVersion", version.schemaVersion)
        field("locale", version.locale)
        field("releasedAtUtc", version.releasedAtUtc)
        field("sourceRegistryVersion", version.sourceRegistryVersion)
        field("migratedFromVersion", version.migratedFromVersion)
        version.includedContentIds.forEachIndexed { index, id -> field("contentId[$index]", id) }
        sources.sortedBy { it.sourceId }.forEachIndexed { index, source ->
            field("source[$index].sourceId", source.sourceId)
            field("source[$index].role", source.role.name)
            field("source[$index].titleOriginal", source.titleOriginal)
            field("source[$index].titleChinese", source.titleChinese)
            field("source[$index].authors", source.authors)
            field("source[$index].publisher", source.publisher)
            field("source[$index].publicationYear", source.publicationYear.toString())
            field("source[$index].editionOrVersion", source.editionOrVersion)
            field("source[$index].officialUrl", source.officialUrl)
            field("source[$index].usage", source.usage)
        }
        items.forEachIndexed { index, item ->
            field("item[$index].contentId", item.contentId)
            field("item[$index].contentHashSha256", item.contentHashSha256)
        }
    })

    private fun StringBuilder.field(name: String, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        append(name).append('=').append(bytes.size).append(':').append(value).append('\n')
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
