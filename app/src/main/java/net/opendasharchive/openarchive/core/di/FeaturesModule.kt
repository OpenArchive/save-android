package net.opendasharchive.openarchive.core.di

import android.app.Application
import net.opendasharchive.openarchive.db.CollectionRepository
import net.opendasharchive.openarchive.db.FolderRepository
import net.opendasharchive.openarchive.db.ICollectionRepository
import net.opendasharchive.openarchive.db.IFolderRepository
import net.opendasharchive.openarchive.db.IMediaRepository
import net.opendasharchive.openarchive.db.MediaActionsViewModel
import net.opendasharchive.openarchive.db.MediaRepository
import net.opendasharchive.openarchive.features.internetarchive.internetArchiveModule
import net.opendasharchive.openarchive.features.main.UnixSocketClient
import net.opendasharchive.openarchive.features.main.ui.MediaGridViewModel
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdAPI
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdGroupRepository
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdMediaRepository
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdRepoRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdAPI
import net.opendasharchive.openarchive.services.snowbird.SnowbirdGroupRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdGroupViewModel
import net.opendasharchive.openarchive.services.snowbird.SnowbirdMediaRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdRepoRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdRepoViewModel
import net.opendasharchive.openarchive.services.tor.ITorRepository
import net.opendasharchive.openarchive.services.tor.TorForegroundService
import net.opendasharchive.openarchive.services.tor.TorRepository
import net.opendasharchive.openarchive.services.tor.TorViewModel
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.upload.MediaUploadRepository
import net.opendasharchive.openarchive.upload.MediaUploadStatusViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featuresModule = module {
    includes(internetArchiveModule)
    single<ISnowbirdAPI> { SnowbirdAPI(get()) }
    single { UnixSocketClient() }
    single { TorForegroundService() }
    single { MediaUploadRepository(MediaUploadManager) }
    single<IFolderRepository> { FolderRepository() }
    single<ICollectionRepository> { CollectionRepository() }
    single<IMediaRepository> { MediaRepository() }
    single<ISnowbirdMediaRepository> { SnowbirdMediaRepository(get()) }
    single<ISnowbirdGroupRepository> { SnowbirdGroupRepository(get()) }
    single<ISnowbirdRepoRepository> { SnowbirdRepoRepository(get()) }
    single<ITorRepository> { TorRepository(get()) }
    viewModel { (app: Application) -> TorViewModel(app, get()) }
    viewModel { MediaGridViewModel(get(), get()) }
    viewModel { MediaActionsViewModel(get()) }
    viewModel { MediaUploadStatusViewModel(get()) }
    viewModel { SnowbirdGroupViewModel(get()) }
    viewModel { SnowbirdRepoViewModel(get()) }
}