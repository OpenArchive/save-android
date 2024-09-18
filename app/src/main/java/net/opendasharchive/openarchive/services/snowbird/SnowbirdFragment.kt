package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdBinding
import net.opendasharchive.openarchive.features.main.QRScannerActivity
import net.opendasharchive.openarchive.services.webdav.ReadyToAuthTextWatcher


class SnowbirdFragment : Fragment() {

    private lateinit var viewBinding: FragmentSnowbirdBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.serverTextInput.setEndIconOnClickListener {
            startQRScanner()
        }

        setupTextListener()

        viewBinding.findGroupsButton.setOnClickListener {
            val uri = viewBinding.serverUri.text.toString()

            pushSnowbirdGroupSelectionFragment(uri)
        }

        viewBinding.serverUri.requestFocus()
    }

    private fun enableIfReady() {
        val isComplete = !viewBinding.serverUri.text.isNullOrEmpty()

//        viewBinding.okButton.isEnabled = isComplete
    }

    private fun pushSnowbirdGroupSelectionFragment(uri: String) {
        findNavController().navigate(SnowbirdFragmentDirections.navigateToSnowbirdGroupSelectionScreen())
    }

    private fun setupTextListener() {
        viewBinding.serverUri.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })
    }

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