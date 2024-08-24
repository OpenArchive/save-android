package net.opendasharchive.openarchive.services.veilid

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import net.opendasharchive.openarchive.databinding.FragmentVeilidBinding
import net.opendasharchive.openarchive.features.main.QRScannerActivity
import net.opendasharchive.openarchive.services.CommonServiceFragment
import timber.log.Timber

class VeilidFragment : CommonServiceFragment() {

    private lateinit var viewBinding: FragmentVeilidBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentVeilidBinding.inflate(inflater)

        viewBinding.serverTextInput.setEndIconOnClickListener {
            startQRScanner()
        }

        viewBinding.serverUri.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable the button if there's text, disable it if it's empty
                viewBinding.okButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this implementation
            }
        })

        viewBinding.okButton.setOnClickListener {
            val uri = viewBinding.serverUri.text.toString()

            pushVeilidGroupSelectionFragment(uri)
        }

        return viewBinding.root
    }

    private fun pushVeilidGroupSelectionFragment(uri: String) {
        val frag = VeilidFoldersFragment()

        val args = Bundle()
        args.putString("uri", uri)
        frag.arguments = args

        val fragmentManager = requireActivity().supportFragmentManager

        val transaction = fragmentManager.beginTransaction()

//        transaction.replace(R.id.space_setup_fragment, frag)
//        transaction.commit()
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

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Timber.d("Cancelled")
            } else {
                val scannedUrl = result.contents

                viewBinding.serverUri.setText(scannedUrl)
                viewBinding.okButton.isEnabled = true

                if (isValidUrl(scannedUrl)) {
                    Timber.d("Scanned URL: $scannedUrl")
                } else {
                    Timber.d("Invalid URL in QR Code: $scannedUrl")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }
}