package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdRepoList(
    var repos: List<SnowbirdRepo>
) : SerializableMarker

@Serializable
data class SnowbirdRepo(
    var key: String = "",
    var name: String? = null,
    var snowbirdGroup: SnowbirdGroup? = null
) : SugarRecord(), SerializableMarker {
    companion object {
        fun getAll(): List<SnowbirdRepo> {
            return findAll(SnowbirdRepo::class.java).asSequence().toList()
        }

        fun getAllFor(group: SnowbirdGroup?): List<SnowbirdRepo> {
            if (group == null) return emptyList()

            val whereClause = "snowbird_group = ?"
            val whereArgs = mutableListOf(group.id.toString())

            return find(
                SnowbirdRepo::class.java, whereClause, whereArgs.toTypedArray(),
                null,
                null,
                null)
        }
    }
}

fun SnowbirdRepo.shortHash(): String {
    return key.take(10)
}