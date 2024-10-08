package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdBinding
import net.opendasharchive.openarchive.features.main.QRScannerActivity
import net.opendasharchive.openarchive.util.Utility
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SnowbirdFragment : Fragment() {

    private lateinit var viewBinding: FragmentSnowbirdBinding
    private val snowbirdViewModel: SnowbirdViewModel by viewModel()

    private val qrCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (scanResult != null) {
            if (scanResult.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val scannedData = scanResult.contents
                // Toast.makeText(requireContext(), "Scanned: $scannedData", Toast.LENGTH_LONG).show()
                processScannedData(scannedData)
            }
        }
    }

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
                // startQRScanner()
                // snowbirdViewModel.fetchGroup("dfdf")
                findNavController().navigate(SnowbirdFragmentDirections.navigateToSnowbirdGroupSelectionScreen())
            }
        }

        viewBinding.createGroupButton.setOnClickListener {
            navigateToCreateGroupScreen()
        }
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

        val scanningIntent = integrator.createScanIntent()

        qrCodeLauncher.launch(scanningIntent)
    }

    private fun processScannedData(data: String) {
        Utility.showMaterialPrompt(
            requireContext(),
            title = "Join Group?",
            message = "Would you like to join the group named \"asdf\" (4e2e7793)?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                findNavController().navigate(SnowbirdFragmentDirections.navigateToSnowbirdGroupOverviewScreen())
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }
}