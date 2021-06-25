package com.itgo.book_cloud.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MediaSource(
    var cbid: Long,
    var name: String,
    var ext: String,
    var href: String,
    var localUri: String,
    var createTime: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var msid: Long = 0
}
