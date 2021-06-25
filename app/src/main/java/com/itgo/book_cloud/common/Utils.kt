package com.itgo.book_cloud.common

import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.itgo.book_cloud.common.Constant.Ext2MimeTypeMap
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.concurrent.thread


fun isPhoneNum(phone: String): Boolean {
    val compile = Pattern.compile("^(13|14|15|16|17|18|19)\\d{9}$")
    val matcher = compile.matcher(phone)
    return matcher.matches()
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun Context.alert(message: String, time: Int = Toast.LENGTH_LONG) {
    val mainHandler = Handler(this.mainLooper)

    mainHandler.post {
        Toast.makeText(this, message, time).show()
    }
}

fun ContextWrapper.openFileOutput(dir: String, name: String): FileOutputStream {
    val folder = this.getExternalFilesDir(dir)
    val file = File(folder, name)
    file.createNewFile()
    return FileOutputStream(file)
}

fun ContextWrapper.openFileInput(dir: String, name: String): FileInputStream {
    val folder = this.getExternalFilesDir(dir)
    return FileInputStream(File(folder, name))
}

fun ContextWrapper.openExternalFileInput(path: String): FileInputStream {
    val folder = this.getExternalFilesDir(null)
    return FileInputStream(File(folder, path))
}

fun ContextWrapper.getExternalFile(path: String): File {
    val folder = this.getExternalFilesDir(null)
    return File(folder, path)
}

fun Uri.getFullPath(): String {
    return this.toString().split("//").last()
}

fun getMimeTypeByExt(ext: String): String {
    return Ext2MimeTypeMap[if (ext[0] == '.') ext else ".$ext"] ?: ""
}

fun createApplicationClipString(uri: Uri, des: String = "👋"): String {
    return "一起来看📖呐！$des👋${
        Base64.encode(uri.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            .toString(Charsets.UTF_8)
    }「橙心书盘」"
}

fun parseApplicationClipString(item: ClipData.Item?): Uri? {
    val matcher = Pattern.compile("一起来看📖呐！(.*?)👋(.*?)「橙心书盘」").matcher(item?.text.toString())
    return if (matcher.find()) {
        val uri = matcher.group(2)
        Uri.parse(
            Base64.decode(uri.toByteArray(Charsets.UTF_8), Base64.NO_WRAP).toString(Charsets.UTF_8)
        )
    } else {
        null
    }
}


fun CircleImageView.setImageURI(url: String?, default: Int) {
    if (url == null) this.setImageResource(default)
    else {
        thread {
            val myurl = URL(url)
            // 获得连接
            val conn = myurl.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000;//设置超时
            conn.doInput = true;
            conn.useCaches = false;//不缓存
            conn.connect();
            val input = conn.inputStream;//获得图片的数据流
            var bmp = BitmapFactory.decodeStream(input)
            input.close()
            this.post { this.setImageBitmap(bmp) }
        }
    }
}

fun ImageView.setImageURI(url: String?, default: Int) {
    if (url == null) this.setImageResource(default)
    else {
        thread {
            val myurl = URL(url)
            // 获得连接
            val conn = myurl.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000;//设置超时
            conn.doInput = true
            conn.connect();
            val input = conn.inputStream;//获得图片的数据流
            var bmp = BitmapFactory.decodeStream(input)
            input.close()
            this.post { this.setImageBitmap(bmp) }
        }
    }
}