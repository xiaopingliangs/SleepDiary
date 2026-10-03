package app.sleepdiary.offline.ui.education

import android.content.res.AssetManager
import app.sleepdiary.offline.data.education.EducationRegistryLoader
import org.json.JSONArray
import org.json.JSONObject

/** Native read-only projection of the pinned packaged candidate registry. */
data class PublicEducationNode(
    val id: String,
    val kind: String,
    val parentId: String?,
    val title: String,
    val classification: String,
    val sourceRefs: JSONArray,
    val payload: JSONObject,
    val nextId: String?
)

data class PublicEducationSource(
    val id: String,
    val title: String,
    val shortName: String,
    val organization: String,
    val version: String?,
    val journal: String?,
    val doi: String?,
    val url: String?,
    val usage: String
)

data class PublicEducationCatalog(
    val versionId: String,
    val locale: String,
    val nodes: Map<String, PublicEducationNode>,
    val sources: Map<String, PublicEducationSource>
) {
    fun node(id: String) = checkNotNull(nodes[id]) { "Missing Education content id: $id" }
    fun enclosingScene(id: String): String? {
        var current = nodes[id]
        val visited = mutableSetOf<String>()
        while (current != null && visited.add(current.id)) {
            if (current.kind == "SCENE") return current.id
            current = current.parentId?.let(nodes::get)
        }
        return null
    }
    fun children(id: String): List<PublicEducationNode> = nodes.values.filter { it.parentId == id }.sortedWith(
        compareBy<PublicEducationNode> { it.payload.optInt("order", Int.MAX_VALUE) }.thenBy { it.id }
    )

    companion object {
        val supportedKinds = setOf(
            "COURSE", "MAP_ENTRY", "MAP_GROUP", "MAP_BRANCH", "CHAPTER", "SCENE", "CONTENT_BLOCK", "CHOICE",
            "SIM_METER", "SIM_STAGE", "SIM_OUTCOME", "ANALYZER_DIMENSION", "ANALYZER_LEVEL", "ANALYZER_CASE",
            "CHECKLIST_GROUP", "CHECKLIST_ITEM", "COGNITIVE_EXERCISE", "REFLECTION", "NOTE_PROMPT", "REVIEW_ITEM",
            "KNOWLEDGE_ENTRY", "EVIDENCE_CLAIM", "SAFETY_NOTICE", "SOURCE", "EXPORT_ELEMENT", "SPECIAL_BLOCK"
        )

        fun fromAssets(assets: AssetManager): PublicEducationCatalog {
            EducationRegistryLoader.fromAssets(assets) // verifies pinned file and content hashes before the UI reads it
            return parse(assets.open("education/public_education_manifest_1_1.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
        }

        fun parse(json: String): PublicEducationCatalog {
            val root = JSONObject(json)
            require(root.getString("contentVersionId") == "PUBLIC_EDUCATION_CONTENT_1.0-CANDIDATE")
            val nodesJson = root.getJSONArray("contentNodes")
            val nodes = LinkedHashMap<String, PublicEducationNode>(nodesJson.length())
            for (i in 0 until nodesJson.length()) {
                val item = nodesJson.getJSONObject(i)
                val id = item.getString("contentId")
                val kind = item.getString("kind")
                require(kind in supportedKinds) { "Unsupported Education content type: $kind ($id)" }
                require(id !in nodes) { "Duplicate Education content id: $id" }
                val rawPayload = item.get("payload")
                val payload = when (rawPayload) {
                    is JSONObject -> rawPayload
                    is JSONArray -> JSONObject().put("items", rawPayload)
                    is String -> JSONObject().put("text", rawPayload)
                    else -> error("Unsupported Education payload shape: $id")
                }
                nodes[id] = PublicEducationNode(
                    id, kind,
                    if (item.isNull("parentContentId")) null else item.getString("parentContentId"),
                    item.optString("title").ifBlank { payload.optString("title", payload.optString("name", id)) },
                    item.getString("classification"), item.optJSONArray("sourceRefs") ?: JSONArray(),
                    payload, if (item.isNull("nextContentId")) null else item.optString("nextContentId").takeIf(String::isNotBlank)
                )
            }
            nodes.values.forEach { node ->
                node.parentId?.let { require(it in nodes) { "Missing parent ${it} for ${node.id}" } }
                node.nextId?.let { require(it in nodes) { "Education navigation from ${node.id} targets unpublished content $it" } }
                require(node.classification in setOf("PUBLIC_AS_IS", "PUBLIC_WITH_PRESENTATION_ADAPTATION", "PUBLIC_CONCEPT_ONLY"))
            }
            val sourceArray = root.getJSONArray("sourceRegistry")
            val sources = LinkedHashMap<String, PublicEducationSource>(sourceArray.length())
            for (i in 0 until sourceArray.length()) {
                val item = sourceArray.getJSONObject(i); val id = item.getString("sourceId")
                require(id !in sources) { "Duplicate Education source id: $id" }
                sources[id] = PublicEducationSource(
                    id, item.optString("title"), item.optString("shortName"), item.optString("organization"),
                    item.nullableString("version"), item.nullableString("journal"), item.nullableString("doi"),
                    item.nullableString("officialUrl"), item.optString("usage")
                )
            }
            nodes.values.forEach { n ->
                val refs = n.sourceRefs
                for (i in 0 until refs.length()) require(refs.getJSONObject(i).getString("sourceId") in sources) {
                    "Missing source reference in ${n.id}"
                }
            }
            require(nodes.values.any { it.kind == "COURSE" })
            return PublicEducationCatalog(root.getString("contentVersionId"), root.getString("locale"), nodes, sources)
        }

        private fun JSONObject.nullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)
    }
}
