package net.opendasharchive.openarchive.services.snowbird

import kotlinx.serialization.Serializable

@Serializable
data class OAGroup(val status: String, val version: String)
