package app.sleepdiary.offline.data.education

import android.content.res.AssetManager
import app.sleepdiary.offline.domain.education.*
import org.json.JSONObject

object EducationRegistryLoader {
    fun fromAssets(assets: AssetManager): EducationContentIndex {
        val legacy = EducationContentRegistryIndex.legacy()
        val bytes = assets.open("education/public_education_manifest_1_1.json").use { it.readBytes() }
        val fileHash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        require(fileHash == EducationContentRegistry.PUBLIC_MANIFEST_FILE_SHA256) { "Packaged Education manifest file integrity failure" }
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        val versionId = root.getString("contentVersionId")
        val hash = root.getString("contentHashSha256")
        require(versionId == EducationContentRegistry.PUBLIC_CANDIDATE_CONTENT_VERSION &&
            hash == EducationContentRegistry.PUBLIC_MANIFEST_CONTENT_HASH_SHA256) { "Packaged Education content version integrity failure" }
        val nodes = root.getJSONArray("contentNodes")
        val refs = LinkedHashMap<String, EducationNodeRef>(nodes.length())
        val parents = LinkedHashMap<String, String?>()
        for (i in 0 until nodes.length()) {
            val node = nodes.getJSONObject(i)
            val id = node.getString("contentId")
            val parent = if (node.isNull("parentContentId")) null else node.getString("parentContentId")
            parents[id] = parent
            var choiceRef: EducationChoiceRef? = null
            if (node.getString("kind") == "CHOICE") {
                val payload = node.getJSONObject("payload")
                val commit = payload.getJSONObject("commit")
                choiceRef = EducationChoiceRef(
                    promptContentId = checkNotNull(parent),
                    choiceContentId = id,
                    optionId = payload.getString("choiceId"),
                    commitValueId = commit.getString("commitValueId")
                )
            }
            refs[id] = EducationNodeRef(id, node.getString("kind"), parent, choice = choiceRef)
        }
        refs.keys.toList().forEach { id ->
            val node = refs.getValue(id)
            if (node.kind == "SCENE") {
                var parent = parents[id]
                val seen = mutableSetOf(id)
                var courseId: String? = null
                while (parent != null && seen.add(parent)) {
                    val parentNode = refs[parent] ?: break
                    if (parentNode.kind == "COURSE") { courseId = parent; break }
                    parent = parents[parent]
                }
                refs[id] = node.copy(courseId = courseId)
            }
        }
        val structured = EducationVersionIndex(EducationVersionDescriptor(versionId, hash), refs)
        return EducationContentIndex(listOf(legacy, structured))
    }
}

private object EducationContentRegistryIndex {
    fun legacy(): EducationVersionIndex {
        val registry = app.sleepdiary.offline.domain.education.EducationContentRegistry
        require(registry.version.contentHashSha256 == registry.EXPECTED_VERSION_HASH_SHA256) { "Packaged Education registry integrity failure" }
        val nodes = registry.items.associate { item ->
            item.contentId to EducationNodeRef(item.contentId, "CONTENT", null)
        }
        return EducationVersionIndex(
            EducationVersionDescriptor(registry.version.versionId, registry.version.contentHashSha256),
            nodes
        )
    }
}
