package com.itgo.book_cloud.common

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import okhttp3.*
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class DownloadService : Service() {

    private val mBinder = DownloadBinder()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    class DownloadBinder : Binder() {

        fun startDownload(
            onlineUrl: String,
            outputStream: OutputStream,
            listener: OnDownloadListener
        ) {
            thread {
                val client = OkHttpClient()
                val request = Request.Builder().url(onlineUrl).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        listener.onDownloadFailed(e.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val bs = response.body?.byteStream()
                        val total = response.body?.contentLength()
                        if (bs == null || total == null) listener.onDownloadFailed("下载失败")
                        else {
                            var len = 0
                            var sum = 0
                            val buf = ByteArray(2048)
                            len = bs.read(buf)
                            try {
                                while (len != -1) {
                                    outputStream.write(buf, 0, len)
                                    sum += len
                                    val progress = (sum * 1.0f / total * 100).toInt()
                                    listener.onDownloading(progress);
                                    len = bs.read(buf)
                                }
                            } catch (e: Exception) {
                                listener.onDownloadFailed(e.toString())
                            } finally {
                                listener.onDownloadSuccess()
                                try {
                                    bs.close()
                                } catch (e: IOException) {
                                }
                                try {
                                    outputStream.close();
                                } catch (e: IOException) {
                                }
                            }
                        }
                    }

                })
            }
        }


    }

    interface OnDownloadListener {
        fun onDownloadSuccess()

        fun onDownloading(progress: Int)

        fun onDownloadFailed(errMsg: String)
    }
}