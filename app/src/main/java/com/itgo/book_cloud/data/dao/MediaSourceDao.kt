package com.itgo.book_cloud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.itgo.book_cloud.data.entity.MediaSource

@Dao
interface MediaSourceDao {

    @Insert
    fun insertMediaSource(mediaSource: MediaSource): Long

    @Query("select * from MediaSource  where cbid = :cbid ")
    fun findAllMediaSourceByCBId(cbid: Long): List<MediaSource>

    fun findAllMediaSourceByCBIdToMapWithHref(cbid: Long): HashMap<String, MediaSource> {
        val res = HashMap<String, MediaSource>()

        findAllMediaSourceByCBId(cbid).forEach {
            res[it.href] = it
        }

        return res
    }
}