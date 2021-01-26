package org.sagebionetworks.assessmentmodel.resourcemanagement

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import org.sagebionetworks.assessmentmodel.Assessment
import org.sagebionetworks.assessmentmodel.AssessmentInfo
import org.sagebionetworks.assessmentmodel.AssessmentProvider
import org.sagebionetworks.assessmentmodel.serialization.FileModuleInfoObject
import org.sagebionetworks.assessmentmodel.serialization.Serialization
import org.sagebionetworks.assessmentmodel.serialization.TransformableAssessment

/**
 * The [ModuleInfoProvider] provides methods to lookup the necessary [ResourceInfo] and [Json] to
 * load an [Assessment] using the provide the platform specific [FileLoader].
 */
interface ModuleInfoProvider : AssessmentProvider {

    val fileLoader: FileLoader

    /**
     * A list of the modules that have been registered with this [ModuleInfoProvider].
     */
    val modules: List<ModuleInfo>

    /**
     * Look to see if there is an [ModuleInfo] that matches the given [assessmentInfo]
     * and return that as the [ResourceInfo] to use when unpacking a given [TransformableAssessment].
     */
    fun getRegisteredResourceInfo(assessmentInfo: AssessmentInfo): ResourceInfo? {
        return modules.find { it.hasAssessment(assessmentInfo) }?.resourceInfo
    }

    /**
     * Look to see if there is an [SerializableModuleInfo] that matches the given [assessmentInfo]
     * and return that as the [Json] to use when unpacking a given [TransformableAssessment].
     */
    fun getRegisteredJsonDecoder(assessmentInfo: AssessmentInfo): Json? {
        return modules.find {
            it.hasAssessment(assessmentInfo) && it is SerializableModuleInfo
        }.let {
            (it as SerializableModuleInfo).jsonCoder
        }

    }
}

class FileAssessmentProvider(override val fileLoader: FileLoader, override val modules: List<FileModuleInfo>) : ModuleInfoProvider {
    override fun canLoadAssessment(assessmentInfo: AssessmentInfo): Boolean {
        return modules.find { it.hasAssessment(assessmentInfo) } != null
    }

    override fun loadAssessment(assessmentInfo: AssessmentInfo): Assessment? {
        return modules.find { it.hasAssessment(assessmentInfo) }?.let { moduleInfo ->
            val assessment = moduleInfo.getAssessment(assessmentInfo)
            val jsonCoder = moduleInfo.jsonCoder ?: Serialization.JsonCoder.default
            val resourceInfo = moduleInfo.resourceInfo
            val serializer = PolymorphicSerializer(Assessment::class)
            val jsonString = fileLoader.loadFile(assessment!!, resourceInfo)
            val unpackedNode = jsonCoder.decodeFromString(serializer, jsonString)
            return unpackedNode.unpack(this, resourceInfo, jsonCoder)
        }
    }
}



