package com.itgo.book_cloud.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chapter(
    var cbid: Long,
    var msHref: String,
    var size: Long,
    var title: String,
    var order: Int,
    var level: Int,
) {

    @PrimaryKey(autoGenerate = true)
    var cid: Long = 0

    companion object {
        fun create() = Chapter(-1, "404.html",0, "", 0, 0)
    }
}
