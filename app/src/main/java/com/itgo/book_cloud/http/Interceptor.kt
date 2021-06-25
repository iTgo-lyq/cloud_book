package com.itgo.book_cloud.http

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.itgo.book_cloud.common.Constant.Account_SPF_DefaultValue_Token
import com.itgo.book_cloud.common.Constant.Account_SPF_Key_Token
import com.itgo.book_cloud.common.Constant.Account_SPF_Name
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

class HeaderInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = context.getSharedPreferences(Account_SPF_Name, MODE_PRIVATE)
            .getString(Account_SPF_Key_Token, Account_SPF_DefaultValue_Token).toString()

        val request = original.newBuilder()
            .header("Authorization", token)
            .header("Client", "ANDROID")
            .method(original.method, original.body)
            .build()

        return chain.proceed(request);
    }
}