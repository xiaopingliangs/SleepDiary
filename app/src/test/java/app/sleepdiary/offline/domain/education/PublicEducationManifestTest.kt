package app.sleepdiary.offline.domain.education

import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PublicEducationManifestTest {
    private fun resourceBytes(): ByteArray = requireNotNull(
        javaClass.getResourceAsStream(EducationContentRegistry.PUBLIC_MANIFEST_RESOURCE)
    ).use { it.readBytes() }

    private fun manifest() = JSONObject(resourceBytes().toString(Charsets.UTF_8))

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private fun stable(value: Any?): String = when (value) {
        null, JSONObject.NULL -> "null"
        is JSONObject -> value.keys().asSequence().toList().sorted().joinToString(",", "{", "}") {
            jsonQuote(it) + ":" + stable(value.get(it))
        }
        is JSONArray -> (0 until value.length()).joinToString(",", "[", "]") { stable(value.get(it)) }
        is String -> jsonQuote(value)
        is Boolean, is Number -> value.toString()
        else -> error("Unsupported JSON value: ${value::class.java.name}")
    }

    private fun jsonQuote(value: String) = JSONObject.quote(value).replace("\\/", "/")

    private fun arrayObjects(array: JSONArray) = (0 until array.length()).map { array.getJSONObject(it) }

    @Test fun completeV4InventoryHasExactlyOneClassificationPerUnit() {
        val root = manifest()
        val inventory = arrayObjects(root.getJSONArray("inventory"))
        val nodes = arrayObjects(root.getJSONArray("contentNodes"))
        val allowed = EducationContentClassification.entries.map { it.name }.toSet()

        assertEquals(EducationContentRegistry.STRUCTURED_SCHEMA_VERSION, root.getString("schemaVersion"))
        assertEquals(EducationContentRegistry.PUBLIC_CANDIDATE_CONTENT_VERSION, root.getString("contentVersionId"))
        assertEquals(562, inventory.size)
        assertEquals(462, nodes.size)
        assertEquals(root.getInt("inventoryUnitCount"), inventory.size)
        assertEquals(root.getInt("publicContentCount"), nodes.size)
        assertEquals(inventory.size, inventory.map { it.getString("v4UnitId") }.toSet().size)
        assertTrue(inventory.all { it.getString("classification") in allowed })

        val expected = mapOf(
            "PUBLIC_AS_IS" to 146,
            "PUBLIC_WITH_PRESENTATION_ADAPTATION" to 178,
            "PUBLIC_CONCEPT_ONLY" to 144,
            "FUTURE_TREATMENT_ONLY" to 40,
            "BLOCKED" to 54
        )
        val counts = root.getJSONObject("classificationCounts")
        expected.forEach { (status, count) -> assertEquals(count, counts.getInt(status)) }
    }

    @Test fun publicIdsAreStableUniqueAndExcludedUnitsExplainWhy() {
        val inventory = arrayObjects(manifest().getJSONArray("inventory"))
        val publicStatuses = setOf("PUBLIC_AS_IS", "PUBLIC_WITH_PRESENTATION_ADAPTATION", "PUBLIC_CONCEPT_ONLY")
        val ids = inventory.filter { it.getString("classification") in publicStatuses && it.getBoolean("contentEligible") }
            .map { it.getString("publicContentId") }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.matches(Regex("[A-Z][A-Z0-9_-]{2,127}")) })
        inventory.filter { it.getString("classification") !in publicStatuses || !it.getBoolean("contentEligible") }.forEach {
            assertTrue(it.isNull("publicContentId"))
            assertTrue(it.getString("reason").isNotBlank())
        }
    }

    @Test fun hierarchySceneLinksClaimsAndSourcesAllResolve() {
        val root = manifest()
        val nodes = arrayObjects(root.getJSONArray("contentNodes"))
        val nodeIds = nodes.map { it.getString("contentId") }.toSet()
        val sourceIds = arrayObjects(root.getJSONArray("sourceRegistry")).map { it.getString("sourceId") }.toSet()
        val claimIds = arrayObjects(root.getJSONArray("inventory"))
            .filter { it.getString("kind") == "EVIDENCE_CLAIM" }
            .map { it.getString("v4UnitId").removePrefix("claim/") }.toSet()

        nodes.forEach { node ->
            if (!node.isNull("parentContentId")) assertTrue(node.getString("parentContentId") in nodeIds)
            if (!node.isNull("nextContentId")) assertTrue(node.getString("nextContentId") in nodeIds)
            arrayObjects(node.getJSONArray("sourceRefs")).forEach { ref ->
                assertTrue(ref.getString("sourceId") in sourceIds)
                assertTrue(ref.getString("locator").isNotBlank())
            }
            val refs = node.getJSONArray("claimRefs")
            for (index in 0 until refs.length()) assertTrue(refs.getString(index) in claimIds)
        }
    }

    @Test fun manifestAndEveryPublicNodeHaveDeterministicHashes() {
        val bytes = resourceBytes()
        assertEquals(EducationContentRegistry.PUBLIC_MANIFEST_FILE_SHA256, sha256(bytes))
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        arrayObjects(root.getJSONArray("contentNodes")).forEach { node ->
            val expected = node.getString("contentHashSha256")
            val unhashed = JSONObject(node.toString()).apply { put("contentHashSha256", "") }
            assertEquals(node.getString("contentId"), expected, sha256(stable(unhashed).toByteArray()))
        }
        root.put("contentHashSha256", "")
        assertEquals(EducationContentRegistry.PUBLIC_MANIFEST_CONTENT_HASH_SHA256, sha256(stable(root).toByteArray()))
    }

    @Test fun registryPreservesV4LearningStructures() {
        val nodes = arrayObjects(manifest().getJSONArray("contentNodes"))
        val kinds = nodes.map { it.getString("kind") }.toSet()
        val required = setOf(
            "COURSE", "MAP_ENTRY", "MAP_GROUP", "MAP_BRANCH", "CHAPTER", "SCENE", "CONTENT_BLOCK",
            "CHOICE", "SIM_STAGE", "ANALYZER_CASE", "CHECKLIST_GROUP", "CHECKLIST_ITEM",
            "COGNITIVE_EXERCISE", "REFLECTION", "NOTE_PROMPT", "REVIEW_ITEM", "KNOWLEDGE_ENTRY",
            "EVIDENCE_CLAIM", "SAFETY_NOTICE", "SOURCE", "EXPORT_ELEMENT"
        )
        assertTrue(kinds.containsAll(required))
        assertEquals(11, nodes.count { it.getString("kind") == "CHAPTER" })
        assertTrue(nodes.count { it.getString("kind") == "COGNITIVE_EXERCISE" } >= 10)
        assertTrue(nodes.any { it.getString("kind") == "REFLECTION" })
    }

    @Test fun publicPayloadHasNoExecutableOrPersonalizedTreatmentContract() {
        val nodes = arrayObjects(manifest().getJSONArray("contentNodes"))
        val prohibited = Regex(
            "TreatmentState|TreatmentEpisode|TreatmentPlan|根据你的日记|最适合你|你今晚应该|为你计算|" +
                "睡眠窗口.{0,8}(增加|减少).{0,8}(分钟|小时)|今晚.{0,8}开始睡眠限制|应开始睡眠限制",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        nodes.filter { it.getString("kind") != "SOURCE" }.forEach { node ->
            assertFalse(node.getString("contentId"), prohibited.containsMatchIn(node.toString()))
        }
        assertTrue(nodes.all { it.getString("kind") != "TREATMENT_PLAN" })
    }

    @Test fun knownUnsafeTreatmentAndRightsUnitsStayOutOfPublicManifest() {
        val root = manifest()
        val inventory = arrayObjects(root.getJSONArray("inventory")).associateBy { it.getString("v4UnitId") }
        val nodes = arrayObjects(root.getJSONArray("contentNodes"))
        val sourceIds = nodes.map { it.getString("sourceV4UnitId") }.toSet()

        mapOf(
            "scene/ch04/earliest" to "FUTURE_TREATMENT_ONLY",
            "scene/ch04/next_cycle" to "FUTURE_TREATMENT_ONLY",
            "scene/ch04/sleep_need" to "BLOCKED",
            "scene/ch08/signals" to "BLOCKED",
            "vault/sleep_pressure" to "BLOCKED",
            "export/ai-knowledge-pack" to "BLOCKED",
            "export/ai-prompt" to "BLOCKED"
        ).forEach { (id, status) ->
            assertNotNull(id, inventory[id])
            val unit = requireNotNull(inventory[id])
            assertEquals(status, unit.getString("classification"))
            assertTrue(unit.getString("reason").isNotBlank())
            assertFalse(id in sourceIds)
        }
    }

    @Test fun structuredModelKeepsPreviewSeparateFromExplicitCommit() {
        val previewFields = EducationPreviewFeedback::class.java.declaredFields.map { it.name }
        val commitFields = EducationCommittedChoiceContract::class.java.declaredFields.map { it.name }
        assertFalse(previewFields.any { it.contains("commitValue", ignoreCase = true) })
        assertTrue(commitFields.containsAll(listOf("contentId", "contentVersionId", "sceneId", "choiceId", "commitValueId")))
        assertFalse(EducationReviewEligibility("X", true).automaticSchedulingAllowed)
        assertFalse(EducationReflectionContract("P", "C", true).interpretationAllowed)
    }

    @Test fun everyPublicChoiceEncodesTransientPreviewAndExplicitCommit() {
        val choices = arrayObjects(manifest().getJSONArray("contentNodes")).filter { it.getString("kind") == "CHOICE" }
        assertTrue(choices.isNotEmpty())
        choices.forEach { node ->
            val payload=node.getJSONObject("payload")
            val preview=payload.getJSONObject("preview")
            val commit=payload.getJSONObject("commit")
            assertEquals("TRANSIENT_ONLY",preview.getString("persistence"))
            assertEquals("EXPLICIT_USER_CONFIRMATION",commit.getString("requiredAction"))
            assertEquals(node.getString("contentId"),commit.getString("commitValueId"))
            assertEquals("EducationRecord",commit.getString("persistedAs"))
            assertTrue(preview.getJSONObject("feedback").getString("educationalBoundary").isNotBlank())
        }
    }

    @Test fun versionedSchemaDeclaresKindsAndStrictChoiceShape() {
        val schemaText=requireNotNull(javaClass.getResourceAsStream("/education/education_content_schema_1_1.json"))
            .use { it.readBytes().toString(Charsets.UTF_8) }
        val schema=JSONObject(schemaText)
        assertEquals("EDUCATION_CONTENT_SCHEMA_1.1",schema.getString("$"+"id").substringAfterLast('/'))
        val nodeKind=schema.getJSONObject("\$defs").getJSONObject("nodeKind").getJSONArray("enum")
        val declaredKinds=(0 until nodeKind.length()).map { nodeKind.getString(it) }.toSet()
        val actualKinds=arrayObjects(manifest().getJSONArray("contentNodes")).map { it.getString("kind") }.toSet()
        assertTrue(declaredKinds.containsAll(actualKinds))
        val choice=schema.getJSONObject("\$defs").getJSONObject("choicePayload")
        assertFalse(choice.getBoolean("additionalProperties"))
        val required=choice.getJSONArray("required").let { (0 until it.length()).map(it::getString).toSet() }
        assertTrue(required.containsAll(setOf("choiceId","label","preview","commit","reviewEligible")))
    }

    @Test fun publicAppServicesDoesNotExposeTreatmentController() {
        val exposedFields = app.sleepdiary.offline.application.AppServices::class.java.declaredFields
            .map { it.name }
        assertFalse(exposedFields.any { it.contains("treatment", ignoreCase = true) })
    }
}
