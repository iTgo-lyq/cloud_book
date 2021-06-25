package com.itgo.book_cloud.common

import android.content.Context
import java.io.*

fun saveByteArray2File(byteArray: ByteArray, outputStream: OutputStream) {
    val input = byteArray.inputStream()
    input.copyTo(outputStream)
    input.close()
    outputStream.flush()
    outputStream.close()
}

fun getInputStreamFromAssets(context: Context, fileName: String): InputStream {
    return context.resources.assets.open(fileName)
}

fun getStringFromAssets(context: Context, fileName: String): String {
    val content: String? = null //结果字符串
    try {
        val inputReader = InputStreamReader(context.resources.assets.open(fileName), "UTF-8")
        val bufReader = BufferedReader(inputReader)
        var line: String? = ""
        val builder = StringBuilder()
        while (bufReader.readLine().also { line = it } != null) {
            builder.append(line)
        }
        inputReader.close()
        bufReader.close()
        return builder.toString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return content ?: ""
}
