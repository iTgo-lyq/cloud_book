package com.itgo.book_cloud.data.model

data class UserInfo(
    val id: Long,
    val phone: String,
    val portrait: String,
    val age: Long,
    val nickname: String,
    val sex: String,
    val token: String,
) {
    val uid
        get() = id
}