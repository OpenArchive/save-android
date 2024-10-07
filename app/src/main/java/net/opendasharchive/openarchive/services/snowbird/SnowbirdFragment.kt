package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdBinding
import net.opendasharchive.openarchive.features.main.QRScannerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SnowbirdFragment : Fragment() {

    private lateinit var viewBinding: FragmentSnowbirdBinding
    private val snowbirdViewModel: SnowbirdViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            snowbirdViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it.friendlyMessage, Toast.LENGTH_SHORT).show()
                    Timber.d("Error = $it")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewBinding.serverTextInput.setEndIconOnClickListener {
//            startQRScanner()
//        }

//        setupTextListener()

        viewBinding.joinGroupButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                startQRScanner()
                // snowbirdViewModel.fetchGroup("dfdf")
            }
        }

        viewBinding.createGroupButton.setOnClickListener {
            navigateToCreateGroupScreen()
        }

//        viewBinding.serverUri.requestFocus()

//        createMenu()
    }

    private fun createMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_browse_folder, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        navigateToCreateGroupScreen()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

//    private fun enableIfReady() {
//        val isComplete = !viewBinding.serverUri.text.isNullOrEmpty()
//    }

    private fun navigateToCreateGroupScreen() {
        findNavController().navigate(SnowbirdFragmentDirections.navigateToSnowbirdCreateGroupScreen())
    }

    private fun navigateToListGroupsScreen(uri: String) {
        findNavController().navigate(SnowbirdFragmentDirections.navigateToSnowbirdGroupSelectionScreen())
    }

//    private fun setupTextListener() {
//        viewBinding.serverUri.addTextChangedListener(object : ReadyToAuthTextWatcher() {
//            override fun afterTextChanged(s: Editable?) {
//                enableIfReady()
//            }
//        })
//    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(requireActivity())
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setCameraId(0)  // Use the rear camera
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.setCaptureActivity(QRScannerActivity::class.java)
        integrator.initiateScan()
    }

//    @Deprecated("")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//
//        if (result != null) {
//            if (result.contents == null) {
//                Timber.d("Cancelled")
//            } else {
//                val scannedUrl = result.contents
//
//                viewBinding.serverUri.setText(scannedUrl)
////                viewBinding.okButton.isEnabled = true
//
//                if (isValidUrl(scannedUrl)) {
//                    Timber.d("Scanned URL: $scannedUrl")
//                } else {
//                    Timber.d("Invalid URL in QR Code: $scannedUrl")
//                }
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }

    private fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }
}