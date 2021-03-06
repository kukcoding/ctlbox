package myapp.util

import android.app.AlertDialog
import android.util.Log
import android.webkit.*


object AppWebViewHelper {

    fun setupWebView(webView: WebView) {
        val s = webView.settings
        s.defaultTextEncodingName = Charsets.UTF_8.name()
        s.javaScriptEnabled = true
        s.javaScriptCanOpenWindowsAutomatically = true
        webView.isVerticalScrollBarEnabled = false
    }

    open class SimpleWebChromeClient : WebChromeClient() {
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult?): Boolean {
            AlertDialog.Builder(view.context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    result?.confirm()
                }
                .setCancelable(false)
                .create()
                .show()
            return true
        }

        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult?): Boolean {
            AlertDialog.Builder(view.context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                .create()
                .show()
            return true
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            Log.d("WebView console", "$consoleMessage")
            return super.onConsoleMessage(consoleMessage)
        }
    }

    open class SimpleWebViewClient : WebViewClient() {
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            view?.loadData(
                "<html><head><meta http-equiv='Content-Type' content='text/html;charset=utf-8'/></head><body></body></html>",
                "text/html", "utf-8"
            )
            if (error != null) {
                processErrorCode(error.errorCode, error.description)
            }
        }

        private fun processErrorCode(errorCode: Int, desc: CharSequence?) {
            when (errorCode) {
                ERROR_AUTHENTICATION               // ???????????? ????????? ?????? ??????
                    , ERROR_BAD_URL                            // ????????? URL
                    , ERROR_CONNECT                           // ????????? ?????? ??????
                    , ERROR_FAILED_SSL_HANDSHAKE     // SSL handshake ?????? ??????
                    , ERROR_FILE                                   // ?????? ?????? ??????
                    , ERROR_FILE_NOT_FOUND                // ????????? ?????? ??? ????????????
                    , ERROR_HOST_LOOKUP            // ?????? ?????? ????????? ????????? ?????? ?????? ??????
                    , ERROR_IO                               // ???????????? ????????? ????????? ?????? ??????
                    , ERROR_PROXY_AUTHENTICATION    // ??????????????? ????????? ?????? ??????
                    , ERROR_REDIRECT_LOOP                // ?????? ?????? ????????????
                    , ERROR_TIMEOUT                          // ?????? ?????? ??????
                    , ERROR_TOO_MANY_REQUESTS            // ????????? ????????? ?????? ?????? ?????? ??????
                    , ERROR_UNKNOWN                         // ?????? ??????
                    , ERROR_UNSUPPORTED_AUTH_SCHEME  // ???????????? ?????? ?????? ??????
                    , ERROR_UNSUPPORTED_SCHEME -> {
                    Log.d("WebView console", "error occur. load local page: $desc")
                    // mBind.webView.loadUrl(LOCAL_PAGE_URL)

                }
            }
        }
    }

}
