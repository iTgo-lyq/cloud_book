package com.itgo.book_cloud

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.itgo.book_cloud.common.Constant
import com.itgo.book_cloud.common.Constant.Account_SPF_Key_Token
import com.itgo.book_cloud.common.Constant.Account_SPF_Key_Uid
import com.itgo.book_cloud.data.model.UserInfo

class BookCloudStore(val context: Context) {
    val userInfo = MutableLiveData<UserInfo?>()

    fun setUserInfo(info: UserInfo) {
        val sp = context.getSharedPreferences(Constant.Account_SPF_Name, Context.MODE_PRIVATE)
        sp.edit {
            putString(Account_SPF_Key_Token, info.token)
            putLong(Account_SPF_Key_Uid, info.id)
            commit()
        }
        userInfo.value = info
    }

    fun clearUserInfo() {
        val sp = context.getSharedPreferences(Constant.Account_SPF_Name, Context.MODE_PRIVATE)
        sp.edit {
            putString(Account_SPF_Key_Token, null)
            putLong(Account_SPF_Key_Uid, 0)
            commit()
        }
        userInfo.value = null
    }
}