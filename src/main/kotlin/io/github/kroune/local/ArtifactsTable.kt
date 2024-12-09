package io.github.kroune.local

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object ArtifactsTable : Table("artifacts") {
    val type = varchar("type", 255)
    val commit = varchar("commit", 255).uniqueIndex()
    val branch = varchar("branch", 255)
    val artifacts = blob("artifact", true)
    val timeStamp = datetime("time_stamp")

    override val primaryKey = PrimaryKey(commit)
}