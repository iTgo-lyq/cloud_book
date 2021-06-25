package com.itgo.book_cloud.data.model

import com.itgo.book_cloud.common.Constant.Origin_Ext_Epub
import com.itgo.book_cloud.common.Constant.Origin_Ext_Pdf

data class OriginBook(
    val id: Long,
    val name: String,
//    val owner: Long,
    val cover: String,
    val epubUrl: String,
    val originUrl: String,
    val originExt: String,
    val convertStatus: String
) {
    companion object {
        val testCase = OriginBook(
            123456005,
            "萌王",
//            0,
            "",
            "https://orange-1258976754.cos.ap-shanghai.myqcloud.com//15.epub",
            "https://orange-1258976754.cos.ap-shanghai.myqcloud.com//15.epub",
            Origin_Ext_Epub,
            ""
        )

        val testCase2 = OriginBook(
            123456006,
            "百鬼夜行",
//            0,
            "",
            "",
            "https://20210529-qimo-1257892469.cos.ap-shanghai.myqcloud.com/book_cloud_test/%5B%E8%B7%9FFBI%E5%AD%A6%E6%8E%A2%E6%A1%88%E6%9C%AF%5D.%E5%BC%A0%E8%B6%85.%E6%89%AB%E6%8F%8F%E7%89%88.pdf",
            Origin_Ext_Pdf,
            ""
        )
    }
}
