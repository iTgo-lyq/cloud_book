package com.itgo.book_cloud.data.model

data class BookShelfItem(val id: Long, val name: String, val isRoot: Int,val owner: UserInfo) {
    val bsid
        get() = id
}
