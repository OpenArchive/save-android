package net.opendasharchive.openarchive.core.di

import android.app.Application
import android.content.Context
import android.view.View
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.opendasharchive.openarchive.db.CollectionRepository
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.FolderRepository
import net.opendasharchive.openarchive.db.ICollectionRepository
import net.opendasharchive.openarchive.db.IFolderDataSource
import net.opendasharchive.openarchive.db.IFolderRepository
import net.opendasharchive.openarchive.db.IMediaRepository
import net.opendasharchive.openarchive.db.MediaActionsViewModel
import net.opendasharchive.openarchive.db.MediaRepository
import net.opendasharchive.openarchive.db.SugarFolderDataSource
import net.opendasharchive.openarchive.features.backends.ItemAction
import net.opendasharchive.openarchive.features.folders.FolderListAdapter
import net.opendasharchive.openarchive.features.folders.FolderViewModel
import net.opendasharchive.openarchive.features.internetarchive.internetArchiveModule
import net.opendasharchive.openarchive.features.main.UnixSocketClient
import net.opendasharchive.openarchive.features.main.ui.MediaGridViewModel
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdFileRepository
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdGroupRepository
import net.opendasharchive.openarchive.services.snowbird.ISnowbirdRepoRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdFileRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdFileViewModel
import net.opendasharchive.openarchive.services.snowbird.SnowbirdGroupRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdGroupViewModel
import net.opendasharchive.openarchive.services.snowbird.SnowbirdRepoRepository
import net.opendasharchive.openarchive.services.snowbird.SnowbirdRepoViewModel
import net.opendasharchive.openarchive.services.tor.ITorRepository
import net.opendasharchive.openarchive.services.tor.TorForegroundService
import net.opendasharchive.openarchive.services.tor.TorRepository
import net.opendasharchive.openarchive.services.tor.TorViewModel
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.upload.MediaUploadRepository
import net.opendasharchive.openarchive.upload.MediaUploadStatusViewModel
import net.opendasharchive.openarchive.util.AppSettings
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featuresModule = module {
    includes(internetArchiveModule)

    // Data sources
    single<IFolderDataSource> { SugarFolderDataSource(get()) }

    // Repositories
    single { MediaUploadRepository(MediaUploadManager) }
    single<IFolderRepository> {
        FolderRepository(
            folderDataSource = get(),
            settings = get(),
            dispatcher = get()
        )
    }
    single<ICollectionRepository> { CollectionRepository() }
    single<IMediaRepository> { MediaRepository() }
    single<ISnowbirdFileRepository> { SnowbirdFileRepository(get(named("unixSocket"))) }
    single<ISnowbirdGroupRepository> { SnowbirdGroupRepository(get(named("unixSocket"))) }
    single<ISnowbirdRepoRepository> { SnowbirdRepoRepository(get(named("unixSocket"))) }
    single<ITorRepository> { TorRepository(get()) }

    single { UnixSocketClient() }
    single { TorForegroundService() }

    single {
        get<Application>().getSharedPreferences(
            "app_preferences", Context.MODE_PRIVATE
        )
    }

    // ViewModels
    viewModel { (application: Application) -> TorViewModel(application, get()) }
    viewModel { (application: Application) -> SnowbirdGroupViewModel(application, get()) }
    viewModel { (application: Application) -> SnowbirdFileViewModel(application, get()) }
    viewModel { (application: Application) -> SnowbirdRepoViewModel(application, get()) }
    viewModel { FolderViewModel(folderRepository = get()) }
    viewModel { MediaGridViewModel(get(), get()) }
    viewModel { MediaActionsViewModel(get()) }
    viewModel { MediaUploadStatusViewModel(get()) }

    factory { (onItemAction: (View, Folder, ItemAction) -> Unit) ->
        FolderListAdapter(
            folderRepo = get(),
            onItemAction = onItemAction
        )
    }

    // Utilities
    single<CoroutineDispatcher> { Dispatchers.IO }
    single { AppSettings(get()) }
}