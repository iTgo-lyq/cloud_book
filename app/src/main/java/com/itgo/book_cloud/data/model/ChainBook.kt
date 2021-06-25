package com.itgo.book_cloud.data.model

import com.itgo.book_cloud.common.Constant
import com.itgo.book_cloud.common.Constant.Origin_Ext_Epub
import com.itgo.book_cloud.common.Constant.Origin_Ext_Pdf

data class ChainBook(
    val id: Long,
    val name: String,
    val cover: String,
    val process: Double,
    val alive: Int,
    val origin: OriginBook,
    val shelf: Long,
//    val owner: UserInfo,
) {
    val isAlive
        get() = alive == 1

    val ext
        get() =
            if (origin.originExt == Origin_Ext_Pdf)
                Origin_Ext_Pdf
            else
                Origin_Ext_Epub


    companion object {
        val testCase = ChainBook(
            123456012,
            "我是标题",
            "",
            0.562131,
            1,
            OriginBook.testCase,
            1,
        )

        val testCase2 = ChainBook(
            123456014,
            "百鬼夜行",
            "",
            0.562131,
            1,
            OriginBook.testCase2,
            1,
        )
    }
}
