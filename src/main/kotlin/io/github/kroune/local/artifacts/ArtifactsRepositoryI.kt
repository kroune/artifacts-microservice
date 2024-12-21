package io.github.kroune.local.artifacts

import io.github.kroune.PlatformType
import io.github.kroune.PublishType

interface ArtifactsRepositoryI {
    suspend fun uploadArtifact(
        artifactValue: ByteArray,
        branchValue: String,
        typeValue: PublishType,
        commitValue: String,
        platformValue: PlatformType
    )

    suspend fun getArtifact(
        branchValue: String?,
        typeValue: PublishType?,
        commitValue: String?,
        platformType: PlatformType
    ): ByteArray?

    suspend fun getLatestArtifact(typeValue: PublishType): ByteArray?
    suspend fun deleteArtifact(branchValue: String, typeValue: PublishType, commitValue: String): Int
}