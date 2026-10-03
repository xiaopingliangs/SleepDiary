package app.sleepdiary.offline.domain.education

enum class EducationCompatibilityProblem { APP_UPDATE_REQUIRED, CONTENT_VERSION_INCOMPATIBLE }

class EducationCompatibilityException(
    val problem: EducationCompatibilityProblem,
    message: String
) : IllegalArgumentException(message)

data class EducationVersionDescriptor(
    val contentVersionId: String,
    val manifestHashSha256: String
)

data class EducationChoiceRef(
    val promptContentId: String,
    val choiceContentId: String,
    val optionId: String,
    val commitValueId: String
)

data class EducationNodeRef(
    val contentId: String,
    val kind: String,
    val parentContentId: String?,
    val courseId: String? = null,
    val choice: EducationChoiceRef? = null
)

data class EducationVersionIndex(
    val descriptor: EducationVersionDescriptor,
    val nodes: Map<String, EducationNodeRef>
)

class EducationContentIndex(versions: List<EducationVersionIndex>) {
    private val versionsById = versions.associateBy { it.descriptor.contentVersionId }

    init { require(versionsById.size == versions.size) }

    fun descriptor(versionId: String): EducationVersionDescriptor = versionsById[versionId]?.descriptor
        ?: throw EducationCompatibilityException(
            EducationCompatibilityProblem.APP_UPDATE_REQUIRED,
            "需要兼容/更新版本的 App 才能恢复教育内容版本：$versionId"
        )

    fun validateDescriptor(value: EducationVersionDescriptor): EducationVersionIndex {
        val known = versionsById[value.contentVersionId]
            ?: throw EducationCompatibilityException(
                EducationCompatibilityProblem.APP_UPDATE_REQUIRED,
                "需要兼容/更新版本的 App 才能恢复教育内容版本：${value.contentVersionId}"
            )
        if (known.descriptor.manifestHashSha256 != value.manifestHashSha256) {
            throw EducationCompatibilityException(
                EducationCompatibilityProblem.CONTENT_VERSION_INCOMPATIBLE,
                "教育内容版本不兼容：${value.contentVersionId} 的清单哈希与本机打包版本不一致"
            )
        }
        return known
    }

    fun node(versionId: String, contentId: String): EducationNodeRef =
        descriptor(versionId).let { versionsById.getValue(versionId).nodes[contentId] }
            ?: throw EducationCompatibilityException(
                EducationCompatibilityProblem.APP_UPDATE_REQUIRED,
                "教育内容版本 $versionId 不包含内容 ID：$contentId"
            )

    fun validateChoice(versionId: String, promptId: String, choiceId: String, optionId: String) {
        val node = node(versionId, choiceId)
        val choice = node.choice
        require(node.kind == "CHOICE" && choice != null && choice.promptContentId == promptId &&
            choice.choiceContentId == choiceId && choice.optionId == optionId && choice.commitValueId == choiceId) {
            "已提交的选项与打包的教育内容不匹配"
        }
    }

    fun validatePosition(versionId: String, courseId: String, sceneId: String) {
        val scene = node(versionId, sceneId)
        require(scene.kind == "SCENE" && scene.courseId == courseId) {
            "恢复位置不属于对应课程"
        }
    }

    fun versions(ids: Set<String>): List<EducationVersionDescriptor> =
        ids.sorted().map(::descriptor)
}
