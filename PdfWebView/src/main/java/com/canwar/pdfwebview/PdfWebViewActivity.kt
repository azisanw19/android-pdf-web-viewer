package com.canwar.pdfwebview

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.canwar.pdfwebview.databinding.ActivityPdfWebViewBinding
import com.canwar.pdfwebview.utils.OnSwipeTouchListener
import com.google.android.material.snackbar.Snackbar
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


class PdfWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING_URI = "extra-string-uri"
        const val EXTRA_INT_FIRST_PAGE = "first-page"
        private const val CONTENT_SECURITY_POLICY = "default-src 'none'; " +
                "form-action 'none'; " +
                "connect-src https://localhost/pdf-viewer/external/pdfjs-2.1.266-dist/web/placeholder.pdf; " +
                "img-src blob: 'self' data:; " +
                "script-src 'self'; " +
                "style-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'none'";

        private const val PERMISSION_POLICY = "accelerometer=(), " +
                "ambient-light-sensor=(), " +
                "autoplay=(), " +
                "battery=(), " +
                "camera=(), " +
                "clipboard-read=(), " +
                "clipboard-write=(), " +
                "display-capture=(), " +
                "document-domain=(), " +
                "encrypted-media=(), " +
                "fullscreen=(), " +
                "gamepad=(), " +
                "geolocation=(), " +
                "gyroscope=(), " +
                "hid=(), " +
                "idle-detection=(), " +
                "interest-cohort=(), " +
                "magnetometer=(), " +
                "microphone=(), " +
                "midi=(), " +
                "payment=(), " +
                "picture-in-picture=(), " +
                "publickey-credentials-get=(), " +
                "screen-wake-lock=(), " +
                "serial=(), " +
                "speaker-selection=(), " +
                "sync-xhr=(), " +
                "usb=(), " +
                "xr-spatial-tracking=()";

        private val REGEX_NUMBER = "(\\d)+".toRegex()
        private val PADDING = 10
    }

    private var mUri: Uri? = null
    private var mInputStream: InputStream? = null

    private lateinit var snackbar: Snackbar
    private var toast: Toast? = null
    private lateinit var textView: TextView
    private lateinit var binding: ActivityPdfWebViewBinding

    private var gotoPage: Int? = null
    private var currentPage: Int? = null
    private var isFirstTimeOpen = true
    private var totalPage: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.topAppBar.title = "Buku"

        snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        textView = TextView(this)
        textView.setBackgroundColor(Color.DKGRAY)
        textView.setTextColor(ColorStateList.valueOf(Color.WHITE))
        textView.textSize = 18f
        textView.setPadding(PADDING, 0, PADDING, 0)

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        mUri = intent.getStringExtra(EXTRA_STRING_URI)?.toUri()
        gotoPage = intent.getIntExtra(EXTRA_INT_FIRST_PAGE, 0)

        initializeWebView()
        mUri?.let { loadPdf() }
        action()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_pdf_web_view_activity, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_search -> {
                binding.layoutSearch.isVisible = !binding.layoutSearch.isVisible
            }
            R.id.menu_jump_page -> {
                binding.layoutJumpPage.isVisible = !binding.layoutJumpPage.isVisible
            }
            R.id.menu_show_sidebar -> {
                evaluateJSToggleSidebar()
            }
            R.id.menu_book_flip -> {
                evaluateJSBookFlip()
            }
            R.id.menu_vertical_scrolling -> {
                evaluateJSVerticalScrolling()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun action() {
        snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)

        binding.searchBook.doAfterTextChanged {
            binding.webview.evaluateJavascript(
                """
                    PDFViewerApplication.findBar.highlightAll.checked = true
                    PDFViewerApplication.findBar.findField.value = "${it.toString()}";
                    PDFViewerApplication.findBar.findNextButton.click();
                    PDFViewerApplication.findBar.findPreviousButton.click();
                    PDFViewerApplication.findBar.findMsg.innerHTML;
                """.trimIndent()
            ) { evaluateJS ->
                val removePrefixSuffix = evaluateJS.replace("\"", "")
                if (removePrefixSuffix.isNotEmpty()) {
                    showSnackbarText(removePrefixSuffix)
                }
            }
        }

        binding.searchNext.setOnClickListener {
            binding.webview.evaluateJavascript(
                """
                    PDFViewerApplication.findBar.findNextButton.click();
                    PDFViewerApplication.findBar.findResultsCount.innerText;
                """.trimIndent()
            ) { evaluateJS ->
                if (evaluateJS.contains("of")) {
                    val currentMatchResult = REGEX_NUMBER.find(evaluateJS)?.value
                    val currentMatch = currentMatchResult?.toIntOrNull()
                    currentMatch?.let { thisMatch ->
                        showSnackbarText(
                            "${thisMatch + 1}${
                                evaluateJS.removePrefix("\"$currentMatchResult").removeSuffix("\"")
                            }"
                        )
                    }
                }
            }
        }

        binding.jumpPage.doAfterTextChanged {
            binding.webview.evaluateJavascript("PDFViewerApplication.page = ${it.toString()}", null)
            val page = it.toString().toIntOrNull()
            if (page != null && page > totalPage!!) {
                showSnackbarText("Page not found")
            }
        }

        binding.searchPrevious.setOnClickListener {
            binding.webview.evaluateJavascript(
                """
                    PDFViewerApplication.findBar.findPreviousButton.click();
                    PDFViewerApplication.findBar.findResultsCount.innerText;
                """.trimIndent()
            ) {evaluateJS ->
                if (evaluateJS.contains("of")) {
                    val currentMatchResult = REGEX_NUMBER.find(evaluateJS)?.value
                    val currentMatch = currentMatchResult?.toIntOrNull()
                    currentMatch?.let { thisMatch ->
                        showSnackbarText(
                            "${thisMatch - 1}${
                                evaluateJS.removePrefix("\"$currentMatchResult").removeSuffix("\"")
                            }"
                        )
                    }
                }
            }
        }

        binding.webview.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                binding.webview.evaluateJavascript("""
                    PDFViewerApplication.toolbar.items.next.click();
                    PDFViewerApplication.page;
                """.trimIndent()) {
                    showPageNumber("$it/$totalPage")
                }
            }

            override fun onSwipeRight() {
                binding.webview.evaluateJavascript("""
                    PDFViewerApplication.toolbar.items.previous.click();
                    PDFViewerApplication.page;
                """.trimIndent()) {
                    showPageNumber("$it/$totalPage")
                }
            }

            override fun onZoomIn(stepDiff: Float) {
                binding.webview.evaluateJavascript("""
                    PDFViewerApplication.zoomIn(${stepDiff / 1000f})
                """.trimIndent(), null)
            }

            override fun onZoomOut(stepDiff: Float) {
                binding.webview.evaluateJavascript("""
                    PDFViewerApplication.zoomOut(${stepDiff / 1000f})
                """.trimIndent(), null)
            }
        })

        binding.closeJumpPage.setOnClickListener {
            binding.layoutJumpPage.isVisible = false
        }

    }

    private fun showSnackbarText(content: String) {
        snackbar.setText(content)
        snackbar.show()
    }

    private fun loadPdf() {
        try {
            if (mInputStream != null)
                mInputStream!!.close()
            mInputStream = contentResolver.openInputStream(mUri!!)
        } catch (e: IOException) {
            snackbar.setText(getString(R.string.error_while_opening)).show()
            return
        }

        binding.webview.loadUrl("https://localhost/pdf-viewer/external/pdfjs-2.1.266-dist/web/viewer.html")
    }

    private fun initializeWebView() {
        val settings = binding.webview.settings
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.javaScriptEnabled = true
        settings.minimumFontSize = 1
        settings.domStorageEnabled = true

        CookieManager.getInstance().setAcceptCookie(false)

        binding.webview.webChromeClient = WebChromeClient()

        binding.webview.webViewClient = object : WebViewClient() {
            private fun fromAsset(mime: String, path: String): WebResourceResponse? {
                return try {
                    val inputStream = assets.open(path.substring(1))
                    WebResourceResponse(mime, null, inputStream)
                } catch (e: IOException) {
                    null
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if ("GET" != request?.method) {
                    return null
                }

                val url = request.url
                if ("localhost" != url.host) {
                    return null
                }

                val path = url.path
                Log.d("PDFWebView", "url: $url, path: $path")

                if ("/pdf-viewer/external/pdfjs-2.1.266-dist/web/viewer.html" == path) {
                    val response = fromAsset("text/html", path)
                    val headers = HashMap<String, String>()
                    headers["Content-Security-Policy"] = CONTENT_SECURITY_POLICY
                    headers["Permissions-Policy"] = PERMISSION_POLICY
                    headers["X-Content-Type-Options"] = "nosniff"
                    response!!.responseHeaders = headers
                    return response
                }

                if (path != null) {
                    if (path.contains("placeholder.pdf")) {
                        maybeCloseInputStream()
                        try {
                            mInputStream = contentResolver.openInputStream(mUri!!)
                        }
                        catch (ignored: FileNotFoundException) {
                            snackbar.setText(getString(R.string.error_while_opening)).show()
                        }

                        return WebResourceResponse("application/pdf", null, mInputStream)
                    }

                    if ("/pdf-turn/external/jquery-3.4.1.min.js" == path) {
                        return fromAsset("application/javascript", path)
                    }

                    if (path.endsWith(".png")) {
                        return fromAsset("image/png", path)
                    }

                    if (path.endsWith(".js")) {
                        return fromAsset("application/javascript", path)
                    }

                    if (path.endsWith(".css")) {
                        return fromAsset("text/css", path)
                    }

                    if (path.endsWith(".css")) {
                        return fromAsset("text/css", path)
                    }

                    if (path.endsWith(".pdf")) {
                        return fromAsset("application/pdf", path)
                    }

                    if (path.endsWith(".properties")) {
                        return fromAsset("text/x-java-properties", path)
                    }

                }

                return null
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript("PDFViewerApplication.page") {
                    currentPage = it.toIntOrNull()
                }

                view?.evaluateJavascript("PDFViewerApplication.pagesCount") {
                    totalPage = it.toIntOrNull()
                }

                currentPage?.let {
                    jumpTopPageFirstTimeOpen()
                }
            }

        }
    }

    private fun jumpTopPageFirstTimeOpen() {
        if (currentPage != gotoPage && isFirstTimeOpen) {
            binding.webview.evaluateJavascript("PDFViewerApplication.page = $gotoPage", null)
            isFirstTimeOpen = false
        }
    }

    private fun maybeCloseInputStream() {
        val stream = mInputStream ?: return
        mInputStream = null
        try {
            stream.close()
        }
        catch (ignored: IOException) {}
    }

    private fun evaluateJSToggleSidebar() {
        binding.webview.evaluateJavascript(
            """
                PDFViewerApplication.toolbar.toolbar.firstElementChild.firstElementChild.click();
            """.trimIndent(), null
        )
    }

    private fun evaluateJSBookFlip() {
        binding.webview.evaluateJavascript(
            """
                PDFViewerApplication.secondaryToolbar.buttons[14].element.click();
            """.trimIndent(), null
        )
    }

    private fun evaluateJSVerticalScrolling() {
        binding.webview.evaluateJavascript(
            """
                PDFViewerApplication.secondaryToolbar.buttons[11].element.click();
            """.trimIndent(), null
        )
        Log.d("PDFWebViewActivity", "Verticall Scrolling")
    }

    private fun showPageNumber(page: String) {
        toast?.cancel()
        textView.text = page
        toast = Toast(this)

        toast!!.setGravity(Gravity.BOTTOM or Gravity.END, PADDING, PADDING)
        toast!!.duration = Toast.LENGTH_SHORT;
        toast!!.setText(page)
        toast!!.show();
    }
}