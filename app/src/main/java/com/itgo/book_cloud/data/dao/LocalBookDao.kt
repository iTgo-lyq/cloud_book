package com.itgo.book_cloud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.itgo.book_cloud.data.entity.LocalBook

@Dao
interface LocalBookDao {
    @Insert
    fun insertOneBook(book: LocalBook)

    @Query("select * from LocalBook where cbid = :cbid limit 1")
    fun findOneBookByCBId(cbid: Long): LocalBook?

    fun exitBook(cbid: Long): Boolean {
        return findOneBookByCBId(cbid) != null
    }
}