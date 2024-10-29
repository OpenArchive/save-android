package net.opendasharchive.openarchive

import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.opendasharchive.openarchive.db.FolderCount
import net.opendasharchive.openarchive.db.FolderRepository
import net.opendasharchive.openarchive.features.folders.FolderViewModel
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class FolderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FolderRepository
    private lateinit var viewModel: FolderViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = FolderViewModel(repository)
    }

    @Test
    fun `when repository emits folder count, viewState updates accordingly`() = runTest {
        // Given
        val folderCount = FolderCount(42)
        coEvery { repository.observeFolderCount() } returns flowOf(folderCount)
        coEvery { repository.refreshFolderCount() } just runs

        // When
        val states = mutableListOf<FolderViewState>()
        val job = launch { viewModel.viewState.toList(states) }

        // Then
        assertTrue(states[0] is FolderViewState.Loading)
        assertTrue(states[1] is FolderViewState.Success)
        assertEquals((states[1] as FolderViewState.Success).folderCount, folderCount)

        job.cancel()
    }
}