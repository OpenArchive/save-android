package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdRepoList(
    var repos: List<SnowbirdRepo>
) : SugarRecord(), SerializableMarker

@Serializable
data class SnowbirdRepo(
    var key: String,
    var name: String?
) : SugarRecord(), SerializableMarker {
    companion object {
        fun getAll(): List<SnowbirdRepo> {
            return findAll(SnowbirdRepo::class.java).asSequence().toList()
        }
    }
}