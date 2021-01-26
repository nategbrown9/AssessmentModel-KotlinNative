package org.sagebionetworks.assessmentmodel.resourcemanagement

import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.sagebionetworks.assessmentmodel.Assessment
import org.sagebionetworks.assessmentmodel.AssessmentInfo
import org.sagebionetworks.assessmentmodel.serialization.Serialization
import org.sagebionetworks.assessmentmodel.serialization.TransformableAssessment

/**
 * The [ModuleInfo] is a way of collecting a group of assessments that are shared within a given module where
 * they should use the same [resourceInfo] to load any of the assessments within the [assessments] included in this group.
 */
interface ModuleInfo {
    val assessments: List<AssessmentInfo>
    val resourceInfo: ResourceInfo

    /**
     * Does this [ModuleInfo] include the assessment for the given [AssessmentInfo]?
     */
    fun hasAssessment(assessmentInfo: AssessmentInfo): Boolean {
        return assessments.firstOrNull {
            it.identifier == assessmentInfo.identifier
        } != null
    }
}

/**
 * A [SerializableModuleInfo] extends [ModuleInfo] to include the [Json] coding information with the
 * serializers  to use when decoding an [Assessment].
 */
interface SerializableModuleInfo : ModuleInfo {
    val jsonCoder: Json
}

/**
 * A [FileModuleInfo] extends [SerializableModuleInfo] to restrict the assessments to be of type
 * [TransformableAssessment].
 */
interface FileModuleInfo : SerializableModuleInfo {

    override val assessments: List<TransformableAssessment>

    fun getAssessment(assessmentInfo: AssessmentInfo): TransformableAssessment? {
        return assessments.firstOrNull {
            it.identifier == assessmentInfo.identifier
        }
    }
}