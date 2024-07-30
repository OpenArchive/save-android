package net.opendasharchive.openarchive.features.internetarchive.presentation

import androidx.compose.runtime.Composable
import net.opendasharchive.openarchive.core.presentation.theme.Theme
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.IAResult
import net.opendasharchive.openarchive.features.internetarchive.presentation.details.InternetArchiveDetailsScreen
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginScreen

@Composable
fun InternetArchiveScreen(backend: Backend, isNewSpace: Boolean, onFinish: (IAResult) -> Unit) = Theme {
    if (isNewSpace) {
        InternetArchiveLoginScreen(backend) {
            onFinish(it)
        }
    } else {
        InternetArchiveDetailsScreen(backend) {
            onFinish(it)
        }
    }
}