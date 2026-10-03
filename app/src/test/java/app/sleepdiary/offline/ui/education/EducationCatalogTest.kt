package app.sleepdiary.offline.ui.education

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EducationCatalogTest {
    private fun manifest() = checkNotNull(javaClass.classLoader?.getResourceAsStream("education/public_education_manifest_1_1.json"))
        .bufferedReader(Charsets.UTF_8).use { it.readText() }

    @Test fun allCandidatePublicNodesAreAddressableAndUseSupportedRendererKinds() {
        val source = JSONObject(manifest())
        val catalog = PublicEducationCatalog.parse(source.toString())
        assertEquals(462, catalog.nodes.size)
        assertTrue(catalog.nodes.values.all { it.kind in PublicEducationCatalog.supportedKinds })
        assertEquals("COURSE-V4", catalog.nodes.values.single { it.kind == "COURSE" }.id)
        assertEquals(11, catalog.nodes.values.count { it.kind == "CHAPTER" })
        assertEquals(98, catalog.nodes.values.count { it.kind == "SCENE" })
        assertTrue(catalog.nodes.values.none { it.classification == "FUTURE_TREATMENT_ONLY" || it.classification == "BLOCKED" })
    }

    @Test fun unknownContentKindFailsVisiblyInsteadOfBeingDropped() {
        val changed = manifest().replaceFirst("\"contentId\": \"COURSE-V4\",\n      \"kind\": \"COURSE\"", "\"contentId\": \"COURSE-V4\",\n      \"kind\": \"UNRECOGNIZED_NODE\"")
        val error = runCatching { PublicEducationCatalog.parse(changed) }.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error!!.message.orEmpty().contains("Unsupported Education content type"))
    }

    @Test fun mapBranchTargetsResolveOrRemainExplicitlyUnmapped() {
        val catalog = PublicEducationCatalog.parse(manifest())
        val chapterIds = catalog.nodes.values.filter { it.kind == "CHAPTER" }.map { it.payload.optString("id") }.toSet()
        val sceneIds = catalog.nodes.values.filter { it.kind == "SCENE" }.map { it.payload.optString("id") }.toSet()
        val references = catalog.nodes.values.filter { it.kind == "MAP_BRANCH" }.mapNotNull { it.payload.optString("chapterId").takeIf(String::isNotBlank) }
        assertEquals(0, references.count { it !in chapterIds && it !in sceneIds })
        assertTrue("diary" in sceneIds) // The diary branch links to its published scene, not an invented chapter.
    }
}
