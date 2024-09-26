package net.opendasharchive.openarchive.core.di

import net.opendasharchive.openarchive.features.internetarchive.internetArchiveModule
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.upload.MediaUploadRepository
import net.opendasharchive.openarchive.upload.MediaUploadViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featuresModule = module {
    includes(internetArchiveModule)
    single { MediaUploadRepository(MediaUploadManager) }
    viewModel { MediaUploadViewModel(get()) }
}