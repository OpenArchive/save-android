package net.opendasharchive.openarchive.extensions

import android.app.Application
import androidx.lifecycle.ViewModel
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf

inline fun <reified T : ViewModel> Application.getViewModel(vararg parameters: Any): T {
    return getKoin().get { parametersOf(*parameters) }
}