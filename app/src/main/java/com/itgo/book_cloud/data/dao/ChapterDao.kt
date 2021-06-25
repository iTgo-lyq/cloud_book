package com.itgo.book_cloud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.itgo.book_cloud.data.entity.Chapter

@Dao
interface ChapterDao {

    @Insert
    fun insertChapters(chapter: Iterable<Chapter>)

    @Query("select * from Chapter where cbid = :cbid order by `order`")
    fun findAllByCBId(cbid: Long): List<Chapter>
}