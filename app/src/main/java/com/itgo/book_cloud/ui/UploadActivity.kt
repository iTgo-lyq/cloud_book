package com.itgo.book_cloud.ui

import android.R.attr.data
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.chip.Chip
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant
import com.itgo.book_cloud.common.alert
import com.itgo.book_cloud.common.setTimeout
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.CommonService
import kotlinx.android.synthetic.main.activity_interest.*
import kotlinx.android.synthetic.main.activity_interest.toolbar
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UploadActivity : AppCompatActivity() {
    private val commonService = ServiceFactory(this).create(CommonService::class.java)

    private val requestFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), this::onSelectFile)

    private val requestImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), this::onSelectImage)


    var bookTitle = ""
    var bookUrl = ""
    var bookCover = ""
    var bookTags = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        for ((_, tagValue) in Constant.List_Tag_Book) {
            val id = ViewCompat.generateViewId()
            val chipView =
                layoutInflater.inflate(R.layout.chip_interest_choice, container, false) as Chip

            chipView.id = id
            chipView.text = tagValue
            chipView.setOnCheckedChangeListener(this::onChipCheckedChanged)

            bookTags[tagValue] = false

            bookTagGroup.addView(chipView)
        }

        fileBtn.setOnClickListener {
            requestFileLauncher.launch("*/*")
        }

        addBtn.setOnClickListener {
            requestImageLauncher.launch("image/*")
        }
    }

    private fun onSelectImage(uri: Uri?) {
        if (uri == null) {
            return
        }

        val documentFile = DocumentFile.fromSingleUri(this, uri)
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = documentFile?.name.toString()

        val filePart = inputStream?.readBytes()?.run {
            toRequestBody("*/*".toMediaTypeOrNull(), 0, size).let {
                MultipartBody.Part.createFormData("file", fileName, it)
            }
        }

        if (filePart != null) {
            commonService.uploadFile(filePart).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    coverImage.setImageURI(uri)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    alert("文件上传失败！")
                    Log.d("debug", t.message.toString())
                }
            })
        } else {
            alert("已取消文件选择～")
        }
    }

    private fun onSelectFile(uri: Uri?) {
        if (uri == null) {
            return
        }

        val documentFile = DocumentFile.fromSingleUri(this, uri)
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = documentFile?.name.toString()

        val filePart = inputStream?.readBytes()?.run {
            toRequestBody("*/*".toMediaTypeOrNull(), 0, size).let {
                MultipartBody.Part.createFormData("file", fileName, it)
            }
        }

        if (filePart != null) {
            commonService.uploadFile(filePart).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    bookName.setText(fileName)
                    fileBtn.text = uri.path
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    alert("文件上传失败！")
                    Log.d("debug", t.message.toString())
                }
            })
        } else {
            alert("已取消文件选择～")
        }
    }


    private fun onChipCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        if (view != null) {
            bookTags[view.text.toString()] = isChecked
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.upload_top_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.upload -> {
                loadingBox.visibility = View.VISIBLE
                setTimeout(this, 1000) {
                    alert("上传成功！")
                    loadingBox.visibility = View.INVISIBLE
                    finish()
                }
            }
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}