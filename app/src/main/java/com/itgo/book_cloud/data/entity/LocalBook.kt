package com.itgo.book_cloud.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.itgo.book_cloud.common.Constant
import java.util.*

@Entity(tableName = "LocalBook")
data class LocalBook(
    var bid: Long,
    var cbid: Long,
    var name: String,
    var author: String,
    var type: Int,
    var localUri: String,
    var remoteUrl: String,
    var process: Double,
    var size: Long,
    var lastReadTime: Long,
    var downloadTime: Long
) {

    @PrimaryKey(autoGenerate = true)
    var lbid: Long = 0

    companion object {
        fun create(
            bid: Long,
            cbid: Long,
            name: String,
            localUri: String,
            remoteUrl: String,
            process: Double,
            author: String = "",
            type: Int = TYPE_UNKNOWN,
            size: Long = 0,
            lastReadTime: Long = Date().time,
            downloadTime: Long = Date().time
        ) = LocalBook(
            bid,
            cbid,
            name,
            author,
            type,
            localUri,
            remoteUrl,
            process,
            size,
            lastReadTime,
            downloadTime,
        )

        const val TYPE_UNKNOWN = 0
        const val TYPE_EPUB = 1
        const val TYPE_PDF = 2
    }
}