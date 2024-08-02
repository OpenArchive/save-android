package net.opendasharchive.openarchive.services.veilid

import android.content.Intent
import android.os.Bundle
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

        viewBinding.qrCodeButton.setOnClickListener {
            startQRScanner()
        }

        return viewBinding.root
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(requireActivity())
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR Code")
        integrator.setCameraId(0)  // Use the rear camera
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.setCaptureActivity(QRScannerActivity::class.java)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Timber.d("Cancelled")
            } else {
                val scannedUrl = result.contents

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