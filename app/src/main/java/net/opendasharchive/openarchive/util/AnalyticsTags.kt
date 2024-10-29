package net.opendasharchive.openarchive.util

fun tag() = PropertyTagDelegate.tag()

object AnalyticsTags {

    object Screens {
        val MY_MEDIA by tag()
        val SETTINGS by tag()
        val GROUP_LIST by tag()
        val REPO_LIST by tag()
        val FILE_LIST by tag()
    }

    object Settings {
        val REQUIRE_WIFI by tag()
        val USE_PASSCODE by tag()
        val USE_TOR by tag()
    }

    object UserActions {
        val CREATE_GROUP by tag()
        val CREATE_REPO by tag()
        val CONNECTED_BACKEND by tag()
        val DISCONNECTED_BACKEND by tag()
        val DOWNLOAD_FILE by tag()
        val JOIN_GROUP by tag()
        val SHARE_GROUP by tag()
        val SWIPE_TO_REFRESH by tag()
        val UPLOAD_FILE by tag()
    }

    object NetworkEvents {
        val CONNECTION_TIMEOUT by tag()
    }
}