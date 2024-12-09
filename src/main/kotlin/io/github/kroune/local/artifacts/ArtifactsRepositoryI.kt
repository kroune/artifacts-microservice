package io.github.kroune.local.artifacts

interface ArtifactsRepositoryI {
    suspend fun uploadArtifact(artifactValue: ByteArray, branchValue: String, typeValue: String, commitValue: String)
    suspend fun getArtifact(branchValue: String?, typeValue: String?, commitValue: String?): ByteArray?
    suspend fun getLatestArtifact(typeValue: String): ByteArray?
    suspend fun deleteArtifact(branchValue: String, typeValue: String, commitValue: String): Int
}