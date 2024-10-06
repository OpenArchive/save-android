package net.opendasharchive.openarchive.services.tor

import kotlinx.coroutines.flow.StateFlow

interface ITorRepository {
    val torStatus: StateFlow<TorStatus>
}

class TorRepository(torService: TorForegroundService) : ITorRepository {
    override val torStatus: StateFlow<TorStatus> = torService.torStatus
}