package app.sleepdiary.offline.domain.education

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EducationContentRegistryTest {
    private val registry = EducationContentRegistry.registry

    @Test fun releasedRegistryIsSchemaValidAndComplete() {
        assertEquals(emptyList<EducationValidationIssue>(), EducationContentValidator.validate(registry))
        assertEquals(EducationContentRegistry.SCHEMA_VERSION, registry.manifest.schemaVersion)
        assertEquals(EducationContentRegistry.SOURCE_REGISTRY_VERSION, registry.manifest.sourceRegistryVersion)
        assertEquals(1, registry.manifest.contentVersions.size)
    }

    @Test fun migratedIdsAreUniqueStableAndKeepOriginalTextVersion() {
        assertEquals(
            listOf("COG-01", "COG-SAFETY-01", "ARO-01", "REL-01", "COG-03", "MAINT-01"),
            registry.items.map { it.contentId }
        )
        assertEquals(registry.items.size, registry.items.map { it.contentId }.toSet().size)
        assertEquals("EDUCATION_TEXT_1.0", EducationContentRegistry.version.migratedFromVersion)
        assertEquals(6, registry.items.size)
    }

    @Test fun everyReferenceResolvesToCompleteSourceAndLocator() {
        val sources = registry.sources.associateBy { it.sourceId }
        registry.items.forEach { item ->
            assertTrue(item.sourceRefs.isNotEmpty())
            item.sourceRefs.forEach { ref ->
                assertTrue(sources.containsKey(ref.sourceId))
                assertTrue(ref.locators.isNotEmpty() && ref.locators.all { it.isNotBlank() })
            }
        }
    }

    @Test fun canonicalHashesDetectContentAndManifestChanges() {
        assertEquals(EducationContentRegistry.EXPECTED_VERSION_HASH_SHA256, EducationContentRegistry.version.contentHashSha256)
        val changed = registry.items.first().copy(title = "changed")
        val changedRegistry = registry.copy(items = listOf(changed) + registry.items.drop(1))
        assertTrue(EducationContentValidator.validate(changedRegistry).any { it.code == "CONTENT_HASH" })

        val rehashed = changed.copy(
            contentHashSha256 = EducationContentHash.item(changed.copy(contentHashSha256 = ""))
        )
        val rehashedRegistry = registry.copy(items = listOf(rehashed) + registry.items.drop(1))
        assertTrue(EducationContentValidator.validate(rehashedRegistry).any { it.code == "VERSION_HASH" })
    }

    @Test fun treatmentBoundaryRejectsPersonalAdviceInstructionsDiagnosisAndPromises() {
        val cases = mapOf(
            "根据你的日记，最适合你的是这个安排。" to "BOUNDARY_PERSONAL_ADVICE",
            "把睡眠窗口减少15分钟。" to "BOUNDARY_TREATMENT_INSTRUCTION",
            "这说明你患有失眠。" to "BOUNDARY_DIAGNOSIS",
            "照着做就一定能改善。" to "BOUNDARY_OUTCOME_PROMISE"
        )
        cases.forEach { (text, expectedCode) ->
            val original = registry.items.first()
            val changed = original.copy(
                contentBlocks = listOf(EducationContentBlock(EducationContentBlockKind.PARAGRAPH, text)),
                contentHashSha256 = ""
            ).let { it.copy(contentHashSha256 = EducationContentHash.item(it)) }
            val candidate = registry.copy(items = listOf(changed) + registry.items.drop(1))
            assertTrue(
                "$expectedCode was not reported for $text",
                EducationContentValidator.validate(candidate).any { it.code == expectedCode }
            )
        }
    }

    @Test fun registryContainsNoUserStateOrExecutableTreatmentModel() {
        val modelNames = EducationRegistry::class.java.declaredFields.map { it.name } +
            EducationContentItem::class.java.declaredFields.map { it.name }
        assertTrue(modelNames.none { it.contains("progress", true) || it.contains("answer", true) || it.contains("treatmentPlan", true) })
        assertTrue(registry.items.all { item ->
            item.contentBlocks.all { it.kind == EducationContentBlockKind.PARAGRAPH }
        })
    }
}
