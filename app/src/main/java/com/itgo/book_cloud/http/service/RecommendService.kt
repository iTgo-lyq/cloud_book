package com.itgo.book_cloud.http.service

import com.itgo.book_cloud.data.model.OriginBook
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class AdResult(
    val id: Long,
    val title: String,
    val url: String,
    val picture: String,
) {
    val adid
        get() = id
}

interface RecommendService {

    @GET("book/recommend/user")
    fun getRecsByUser(): Call<List<OriginBook>>

    @GET("book/recommend/item")
    fun getRecsByBook(): Call<List<OriginBook>>

    @GET("ad/22")
    fun fetchAd(/*@Path("bid") bid: Long, @Path("uid") uid: Long*/): Call<AdResult>
}