package com.itgo.book_cloud.ui.interests

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.itgo.book_cloud.common.Constant.findTagValue
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.AccountService
import com.itgo.book_cloud.http.service.PostTabsBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InterestsViewModel(context: Context) : ViewModel() {

    private val accountService = ServiceFactory(context).create(AccountService::class.java)

    val setTagsResult = MutableLiveData(false)
    val networkError = MutableLiveData("")
    val selectedOptionMap = MutableLiveData(HashMap<Int, Boolean>())

    fun registerOption(tagIdx: Int) {
        selectedOptionMap.value?.set(tagIdx, selectedOptionMap.value?.get(tagIdx) ?: false)
    }

    fun updateOption(tagIdx: Int?, checked: Boolean) {
        tagIdx?.let {
            selectedOptionMap.value?.set(it, checked)
            selectedOptionMap.value = selectedOptionMap.value
        }
    }

    fun save(uid: Long?) {
        val tags = selectedOptionMap.value?.filter { it.value }?.mapNotNull { findTagValue(it.key, true) }

        if (tags == null || uid == null) {
            networkError.value = "网络出错啦~"
        } else {
            accountService.postTags(uid, PostTabsBody(tags)).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    networkError.value = ""
                    setTagsResult.value = true
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    networkError.value = t.message
                }
            })
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return InterestsViewModel(context) as T
        }
    }
}