package com.itgo.book_cloud.common

import java.io.File
import java.util.*

// SPF = SharedPreference

object Constant {
    /** 剪切板 */
    val Application_Schema = "com.itgo.book_cloud".toLowerCase(Locale.ROOT)
    val BookMedia_Schema = "bookMedia".toLowerCase(Locale.ROOT)

    /** 账户存储相关 */
    const val Account_SPF_Name = "Account_SharedPreference_Name"
    const val Account_SPF_Key_Uid = "Account_SharedPreference_Key_Uid"
    const val Account_SPF_Key_Token = "Account_SharedPreference_Key_Token"
    const val Account_SPF_DefaultValue_Token = "unknown"
    const val Account_SPF_DefaultValue_Uid = 0L

    /** 登录验证 */
    const val Account_Captcha_Length = 6

    /** API 调用 */
    const val API_Base_Url =
//        "https://mockapi.eolinker.com/jazdnyJf98a20d43f9d966b028d7a1e166b0fbf943eaf4a/"
//        "http://192.168.184.227:8080"
        "http://192.168.43.34:8080"

    /** reader 页面的数量 */
    const val Num_Reader_Pages_Idx_Init = Int.MAX_VALUE / 2
    const val Num_Reader_Pages_Total = Int.MAX_VALUE

    /** 书籍信息 */
    const val Origin_Ext_Epub = ".epub"
    const val Origin_Ext_Pdf = ".pdf"
    private const val Local_File_Cache_Folder = "book_media"
    private const val ChainBook_Cache_Folder = "chain_book"

    fun getChainBookCacheFolder(cbid: Long) =
        Local_File_Cache_Folder + File.separator + ChainBook_Cache_Folder + File.separator + cbid + File.separator

    /** 数据库 */
    const val DB_Name = "book_cloud_database"

    /** 用户未选择标签的标识 */
    const val Flag_User_NoTags_With_Age = 0L

    /** 标签列表 */
    private const val List_Tag_Book_Idx_Offset = 1000
    private const val List_Tag_SelfDesc_Idx_Offset = 2000
    val List_Tag_Book = arrayOf(
        "网络文学",
        "科技",
        "爱情",
        "励志",
        "生活",
        "心理",
        "旅行",
        "摄影",
        "教育",
        "美食",
        "健康",
        "养生",
        "两性",
        "历史",
        "哲学",
        "社会学",
        "艺术",
        "理学",
        "工学",
        "宗教",
        "军事",
        "漫画",
        "言情",
        "科幻",
        "武侠",
        "青春",
        "推理",
        "穿越",
        "经济",
        "创业",
        "管理",
        "互联网",
        "编程",
        "营销",
        "职场"
    ).mapIndexed { idx: Int, s: String ->
        idx + List_Tag_Book_Idx_Offset to s
    }.toMap()
    val List_Tag_SelfDesc = arrayOf(
        "小清新",
        "话唠",
        "文艺",
        "工作狂",
        "内向",
        "前卫",
        "偏执狂",
        "夜猫子",
        "眼镜男",
        "宅",
        "逗比",
        "肌肉男",
        "理性",
        "中二",
        "果粉",
        "小鲜肉",
        "选择恐惧症",
        "数码控",
        "暖男",
        "浪漫",
        "完美主义",
        "拖延症",
        "大叔",
        "铲屎官",
        "感性",
        "纹身",
        "吃货",
        "潮男",
        "理工男"
    ).mapIndexed { idx: Int, s: String ->
        idx + List_Tag_SelfDesc_Idx_Offset to s
    }.toMap()

    fun findTagValue(idx: Int, focus: Boolean = false): String? {
        return if (focus || idx in List_Tag_Book_Idx_Offset until List_Tag_SelfDesc_Idx_Offset) {
           return List_Tag_Book[idx]
        } else {
            List_Tag_SelfDesc[idx]
        }
    }

    val Ext2MimeTypeMap =
        listOf(
            ".xhtml" to "application/xhtml+xml",
            ".epub" to "application/epub+zip",
            ".ncx" to "application/x-dtbncx+xml",
            ".js" to "text/javascript",
            ".css" to "text/css",
            ".jpg" to "image/jpeg",
            ".png" to "image/png",
            ".gif" to "image/gif",
            ".svg" to "image/svg+xml",
            ".ttf" to "application/x-truetype-font",
            ".otf" to "application/vnd.ms-opentype",
            ".woff" to "application/font-woff",
            ".mp3" to "audio/mpeg",
            ".ogg" to "audio/ogg",
            ".mp4" to "video/mp4",
            ".smil" to "application/smil+xml",
            ".xpgt" to "application/adobe-page-template+xml",
            ".pls" to "application/pls+xml",
        ).toMap()
}