package net.opendasharchive.openarchive.db

data class WebDAVModel (
    val ocs: Ocs
)

data class Ocs (
    val meta: Meta,
    val data: Data
)

data class Data (
    val storageLocation: String,
    val id: String,
    val lastLogin: Long,
    val backend: String,
    val subadmin: List<Any?>,
    val quota: Quota,
    val avatarScope: String,
    val email: String,
    val emailScope: String,
    val additionalMail: List<Any?>,
    val additionalMailScope: List<Any?>,
    val displayname: String,
    val displaynameScope: String,
    val phone: String,
    val phoneScope: String,
    val address: String,
    val addressScope: String,
    val website: String,
    val websiteScope: String,
    val twitter: String,
    val twitterScope: String,
    val groups: List<String>,
    val language: String,
    val locale: String,
    val notifyEmail: Any? = null,
    val backendCapabilities: BackendCapabilities
)

data class BackendCapabilities (
    val setDisplayName: Boolean,
    val setPassword: Boolean
)

data class Quota (
    val free: Long,
    val used: Long,
    val total: Long,
    val relative: Double,
    val quota: Long
)

data class Meta (
    val status: String,
    val statuscode: Long,
    val message: String,
    val totalitems: String,
    val itemsperpage: String
)