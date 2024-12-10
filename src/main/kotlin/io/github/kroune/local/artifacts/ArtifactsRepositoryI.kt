package io.github.kroune.local.artifacts

import io.github.kroune.PlatformType

interface ArtifactsRepositoryI {
    suspend fun uploadArtifact(
        artifactValue: ByteArray,
        branchValue: String,
        typeValue: String,
        commitValue: String,
        platformValue: PlatformType
    )

    suspend fun getArtifact(
        branchValue: String?,
        typeValue: String?,
        commitValue: String?,
        platformType: PlatformType
    ): ByteArray?

    suspend fun getLatestArtifact(typeValue: String): ByteArray?
    suspend fun deleteArtifact(branchValue: String, typeValue: String, commitValue: String): Int
}