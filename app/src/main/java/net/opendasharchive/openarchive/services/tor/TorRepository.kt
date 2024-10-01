package net.opendasharchive.openarchive.services.tor

import kotlinx.coroutines.flow.StateFlow

class TorRepository(private val torService: TorForegroundService) {
    val torStatus: StateFlow<TorStatus> = torService.torStatus
}