package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSetupSuccessBinding
import net.opendasharchive.openarchive.features.folders.FolderViewModel

class BackendSetupSuccessFragment : Fragment() {
    private lateinit var mBinding: FragmentBackendSetupSuccessBinding
    private val folderViewModel: FolderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentBackendSetupSuccessBinding.inflate(inflater)

        val backendName = getBackendName()

        mBinding.btAuthenticate.setOnClickListener { _ ->
            findNavController().popBackStack(R.id.browse_folders_screen, false)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun getBackendName(): String {
        return folderViewModel.folder.value.backend?.displayname ?: "an unknown server"
    }
}