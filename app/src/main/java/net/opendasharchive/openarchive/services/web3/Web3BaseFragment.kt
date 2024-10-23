package net.opendasharchive.openarchive.services.web3

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

abstract class Web3BaseFragment : Fragment() {
    protected val viewModel: Web3ViewModel by viewModels()
    private var webView: WebView? = null

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Create WebView
//        webView = WebView(requireContext()).apply {
//            visibility = View.GONE
//        }
//
//        return webView
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hidden WebView for web3 client
        webView = WebView(requireContext()).apply {
            visibility = View.GONE
        }

        (view as ViewGroup).addView(webView)

        // Initialize client if needed
        if (viewModel.initializationState.value is InitializationState.NotInitialized) {
            webView?.let { webView ->
                viewModel.initializeClient(
                    webView = webView,
                    privateKey = "MgCaacOmQBdTtw4LeDw9Rw7RD/GAzRUDBLCUswG4Oat7g++0BCzZli1Mf34DS7gzmsTH95/M9sqc3DgY7A1J01Bj5dtk=",
                    space = Web3Space("did:key:z6MkvyJrVMKWdQBtKR2dZci3dkZNCUaDwiAfqFCPaXXnZGn4", "Nervous")
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView = null
    }
}