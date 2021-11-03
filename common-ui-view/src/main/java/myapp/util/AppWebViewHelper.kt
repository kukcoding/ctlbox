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
                ERROR_AUTHENTICATION               // 서버에서 사용자 인증 실패
                    , ERROR_BAD_URL                            // 잘못된 URL
                    , ERROR_CONNECT                           // 서버로 연결 실패
                    , ERROR_FAILED_SSL_HANDSHAKE     // SSL handshake 수행 실패
                    , ERROR_FILE                                   // 일반 파일 오류
                    , ERROR_FILE_NOT_FOUND                // 파일을 찾을 수 없습니다
                    , ERROR_HOST_LOOKUP            // 서버 또는 프록시 호스트 이름 조회 실패
                    , ERROR_IO                               // 서버에서 읽거나 서버로 쓰기 실패
                    , ERROR_PROXY_AUTHENTICATION    // 프록시에서 사용자 인증 실패
                    , ERROR_REDIRECT_LOOP                // 너무 많은 리디렉션
                    , ERROR_TIMEOUT                          // 연결 시간 초과
                    , ERROR_TOO_MANY_REQUESTS            // 페이지 로드중 너무 많은 요청 발생
                    , ERROR_UNKNOWN                         // 일반 오류
                    , ERROR_UNSUPPORTED_AUTH_SCHEME  // 지원되지 않는 인증 체계
                    , ERROR_UNSUPPORTED_SCHEME -> {
                    Log.d("WebView console", "error occur. load local page: $desc")
                    // mBind.webView.loadUrl(LOCAL_PAGE_URL)

                }
            }
        }
    }

}
