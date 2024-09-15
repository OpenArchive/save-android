package net.opendasharchive.openarchive.features.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import net.opendasharchive.openarchive.databinding.ActivityWebViewBinding
import net.opendasharchive.openarchive.features.core.BaseActivity

class WebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var currentUrl: String? = null

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val STATE_URL = "state_url"

        fun newIntent(context: Context, url: String): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"

        webView = binding.webView
        progressBar = binding.progressBar

        setupWebView()

        currentUrl = savedInstanceState?.getString(STATE_URL)
            ?: intent.getStringExtra(EXTRA_URL) ?: "https://www.placekitten.com"

        currentUrl?.let { url ->
            loadUrl(url)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, currentUrl)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = false
            setSupportZoom(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }

    private fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun canGoBack(): Boolean = webView.canGoBack()

    fun goBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }
}