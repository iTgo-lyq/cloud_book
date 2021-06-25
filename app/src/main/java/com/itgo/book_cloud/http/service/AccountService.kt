package com.itgo.book_cloud.http.service

import com.itgo.book_cloud.data.model.UserInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

data class SendCaptchaBody(val phone: String)
data class SendCaptchaResult(val salt: String)

data class LoginBody(val phone: String, val salt: String, val captcha: String)

data class PostTabsBody(val tabs: List<String>)

interface AccountService {

    @POST("account/captcha")
    fun sendCaptcha(@Body data: SendCaptchaBody): Call<SendCaptchaResult>

    @POST("account/book")
    fun login(@Body data: LoginBody, /*@Header("mock") mock: String = "unselectedTags"*/): Call<UserInfo>

    @GET("account/book/{uid}")
    fun getUserInfo(@Path("uid") uid: Long, /*@Header("mock") mock: String = "unselectedTags"*/): Call<UserInfo>

    @POST("account/book/{uid}/tabs")
    fun postTags(@Path("uid") uid: Long, @Body data: PostTabsBody): Call<ResponseBody>
}