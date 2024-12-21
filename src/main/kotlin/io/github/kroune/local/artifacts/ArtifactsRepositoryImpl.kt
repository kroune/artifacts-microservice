package io.github.kroune.local.artifacts

import io.github.kroune.PlatformType
import io.github.kroune.PublishType
import io.github.kroune.local.ArtifactsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class ArtifactsRepositoryImpl : ArtifactsRepositoryI {
    init {
        transaction {
            SchemaUtils.create(ArtifactsTable)
        }
    }

    override suspend fun uploadArtifact(
        artifactValue: ByteArray,
        branchValue: String,
        typeValue: PublishType,
        commitValue: String,
        platformValue: PlatformType
    ) {
        newSuspendedTransaction {
            ArtifactsTable.insert {
                it[artifacts] = ExposedBlob(artifactValue)
                it[branch] = branchValue
                it[type] = typeValue.encodeToString()
                it[commit] = commitValue
                it[timeStamp] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                it[platform] = platformValue.encodeToString()
            }
        }
    }

    override suspend fun getArtifact(
        branchValue: String?,
        typeValue: PublishType?,
        commitValue: String?,
        platformType: PlatformType
    ): ByteArray? {
        return newSuspendedTransaction {
            ArtifactsTable.select(ArtifactsTable.artifacts).where {
                val branch =
                    (if (branchValue != null)
                        (ArtifactsTable.branch eq branchValue)
                    else
                        Op.TRUE)
                val type =
                    (if (typeValue != null)
                        (ArtifactsTable.type eq typeValue.encodeToString())
                    else
                        Op.TRUE)
                val commit =
                    (if (commitValue != null)
                        (ArtifactsTable.commit eq commitValue)
                    else
                        Op.TRUE)
                branch and type and commit and (ArtifactsTable.platform eq platformType.encodeToString())
            }.limit(1).map {
                it[ArtifactsTable.artifacts].bytes
            }.firstOrNull()
        }
    }

    override suspend fun getLatestArtifact(typeValue: PublishType): ByteArray? {
        return newSuspendedTransaction {
            ArtifactsTable.select(ArtifactsTable.artifacts).where {
                (ArtifactsTable.type eq typeValue.encodeToString())
            }.orderBy(ArtifactsTable.timeStamp, SortOrder.DESC).limit(1).map {
                it[ArtifactsTable.artifacts].bytes
            }.firstOrNull()
        }
    }

    override suspend fun deleteArtifact(branchValue: String, typeValue: PublishType, commitValue: String): Int {
        return newSuspendedTransaction {
            ArtifactsTable.deleteWhere {
                (branch eq branchValue) and (type eq typeValue.encodeToString()) and (commit eq commitValue)
            }
        }
    }
}