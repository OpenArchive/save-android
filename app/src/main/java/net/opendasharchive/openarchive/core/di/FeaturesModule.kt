package net.opendasharchive.openarchive.core.di

import net.opendasharchive.openarchive.db.CollectionRepository
import net.opendasharchive.openarchive.db.FolderRepository
import net.opendasharchive.openarchive.db.ICollectionRepository
import net.opendasharchive.openarchive.db.IFolderRepository
import net.opendasharchive.openarchive.db.IMediaRepository
import net.opendasharchive.openarchive.db.MediaRepository
import net.opendasharchive.openarchive.db.MediaActionsViewModel
import net.opendasharchive.openarchive.features.internetarchive.internetArchiveModule
import net.opendasharchive.openarchive.features.main.ui.MediaGridViewModel
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.upload.MediaUploadRepository
import net.opendasharchive.openarchive.upload.MediaUploadStatusViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featuresModule = module {
    includes(internetArchiveModule)
    single { MediaUploadRepository(MediaUploadManager) }
    single<IFolderRepository> { FolderRepository() }
    single<ICollectionRepository> { CollectionRepository() }
    single<IMediaRepository> { MediaRepository() }
    viewModel { MediaGridViewModel(get(), get()) }
    viewModel { MediaActionsViewModel(get()) }
    viewModel { MediaUploadStatusViewModel(get()) }
}