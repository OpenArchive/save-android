package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSetupSuccessBinding

class BackendSetupSuccessFragment : Fragment() {
    private lateinit var mBinding: FragmentBackendSetupSuccessBinding
    private var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(ARG_MESSAGE, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentBackendSetupSuccessBinding.inflate(inflater)

        if (message.isNotEmpty()) {
            mBinding.successMessage.text = message
        }

        mBinding.btAuthenticate.setOnClickListener { _ ->
            findNavController().popBackStack(R.id.browseFoldersFragment, false)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        const val RESP_DONE = "backend_setup_success_fragment_resp_done"
        const val RESP_BACKEND_LINKED = "backend_setup_success_fragment_resp_linked"
        const val ARG_MESSAGE = "backend_setup_success_fragment_arg_message"

        @JvmStatic
        fun newInstance(message: String) =
            BackendSetupSuccessFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE, message)
                }
            }
    }
}