package net.opendasharchive.openarchive.features.internetarchive.domain.usecase

import com.google.gson.Gson
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.internetarchive.domain.model.InternetArchive
import net.opendasharchive.openarchive.features.internetarchive.infrastructure.repository.InternetArchiveRepository

class InternetArchiveLoginUseCase(
    private val repository: InternetArchiveRepository,
    private val gson: Gson,
    private val backend: Backend,
) {

    suspend operator fun invoke(email: String, password: String): Result<InternetArchive> =
        repository.login(email, password).mapCatching { response ->

            response.auth.let { auth ->
                repository.testConnection(auth).getOrThrow()
                backend.username = auth.access
                backend.password = auth.secret
            }

            // TODO: use local data source for database
            backend.metaData = gson.toJson(response.meta)
            backend.save()

            // Backend.current = backend

            response
        }

}
