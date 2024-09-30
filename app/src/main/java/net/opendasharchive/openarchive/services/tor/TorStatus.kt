package net.opendasharchive.openarchive.services.tor

enum class TorStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR_NETWORK,
    ERROR_AUTHENTICATION,
    ERROR_UNKNOWN;

    val isConnectedOrConnecting: Boolean
        get() = this == CONNECTED || this == CONNECTING

    val isError: Boolean
        get() = this == ERROR_NETWORK || this == ERROR_AUTHENTICATION || this == ERROR_UNKNOWN
}