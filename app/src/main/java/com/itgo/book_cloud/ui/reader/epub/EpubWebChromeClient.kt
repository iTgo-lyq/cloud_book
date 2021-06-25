package com.itgo.book_cloud.ui.reader.epub

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient


class EpubWebChromeClient : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Log.i(
            "console-chromium",
            "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")"
        )
        return true
    }
}