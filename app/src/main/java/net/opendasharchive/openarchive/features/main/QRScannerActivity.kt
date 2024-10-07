package net.opendasharchive.openarchive.features.main

import com.journeyapps.barcodescanner.CaptureActivity

class QRScannerActivity : CaptureActivity() {
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
}