package com.itgo.book_cloud.http

import android.content.Context
import com.itgo.book_cloud.common.Constant.API_Base_Url
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServiceFactory(private val context: Context) {
    fun <T> create(service: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(API_Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(30, TimeUnit.SECONDS)//设置读取超时时间
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(HeaderInterceptor(context))
                    .build()
            )
            .build()
            .create(service)
    }
}