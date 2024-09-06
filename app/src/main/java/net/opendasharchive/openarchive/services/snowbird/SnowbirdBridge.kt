package net.opendasharchive.openarchive.services.snowbird

class SnowbirdBridge {
    companion object {
        init {
            System.loadLibrary("save")
        }

        @JvmStatic
        external fun startServer(socketPath: String): String
    }
}