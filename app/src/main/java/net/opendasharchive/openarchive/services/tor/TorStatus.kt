package net.opendasharchive.openarchive.services.tor

enum class TorStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR;

    val isConnectedOrConnecting: Boolean
        get() = this == CONNECTED || this == CONNECTING
}