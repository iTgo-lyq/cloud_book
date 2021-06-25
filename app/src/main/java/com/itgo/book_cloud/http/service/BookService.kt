package com.itgo.book_cloud.http.service

import com.itgo.book_cloud.data.model.BookShelfItem
import com.itgo.book_cloud.data.model.ChainBook
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


data class AddBookToShelfBody(val ids: List<Long>)

interface BookService {

    @GET("book/shelf")
    fun getAllBookShelf(): Call<List<BookShelfItem>>

    @GET("book/shelf/{id}/chain")
    fun getBookByShelf(@Path("id") bsid: Long): Call<List<ChainBook>>

    @POST("book/shelf/{id}/chain")
    fun addBookToShelf(@Path("id") bsid: Long, @Body data:AddBookToShelfBody ): Call<ResponseBody>
}