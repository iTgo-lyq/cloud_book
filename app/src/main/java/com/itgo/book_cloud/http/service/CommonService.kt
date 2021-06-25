package com.itgo.book_cloud.http.service

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CommonService {
    @Multipart
    @POST("common/file")
    fun uploadFile(@Part file: MultipartBody.Part): Call<ResponseBody>
}