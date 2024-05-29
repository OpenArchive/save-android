package net.opendasharchive.openarchive.services.internetarchive

interface RequestListener {
    fun transferred(bytes: Long)
    fun continueUpload(): Boolean
    fun transferComplete()
}