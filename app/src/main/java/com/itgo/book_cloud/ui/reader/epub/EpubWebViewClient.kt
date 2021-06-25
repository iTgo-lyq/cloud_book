package com.itgo.book_cloud.ui.reader.epub

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.util.Log
import android.webkit.*
import com.itgo.book_cloud.common.*
import com.itgo.book_cloud.common.Constant.BookMedia_Schema
import java.io.File
import java.nio.charset.Charset


class EpubWebViewClient(private val bookInfo: CompatBookInfo) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Log.d("console-book", request?.url.toString())
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        Log.d("console-book", url.toString())
        super.onLoadResource(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        Log.d("console-book", request?.url.toString())

        when (request?.url?.scheme) {
            "epub" ->
                return WebResourceResponse(
                    getMimeTypeByExt(File(request.url.getFullPath()).extension),
                    "UTF-8",
                    view?.run {
                        getInputStreamFromAssets(
                            context,
                            "www/" + request.url.getFullPath()
                        )
                    }
                )
            BookMedia_Schema ->
                return bookInfo.mediaSourceMap[request.url.getFullPath()]?.run {
                    if (ext == ".xhtml")
                        Log.d(
                            "ebook",
                            (view?.context as ContextWrapper).openExternalFileInput(localUri)
                                .readBytes().toString(
                                    Charset.defaultCharset()
                                )
                        )

                    WebResourceResponse(
                        getMimeTypeByExt(ext),
                        "UTF-8",
                        (view?.context as ContextWrapper).openExternalFileInput(localUri)
                    )
                }
            else -> return null
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        view?.apply {
            val js = getStringFromAssets(view.context, "www/epub.js")
            view.loadUrl("javascript:(() => {$js})();")
        }
    }
}