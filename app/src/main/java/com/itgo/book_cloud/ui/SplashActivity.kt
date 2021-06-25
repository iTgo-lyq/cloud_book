package com.itgo.book_cloud.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant
import com.itgo.book_cloud.common.Constant.Account_SPF_DefaultValue_Token
import com.itgo.book_cloud.common.Constant.Account_SPF_DefaultValue_Uid
import com.itgo.book_cloud.common.Constant.Account_SPF_Key_Token
import com.itgo.book_cloud.common.Constant.Account_SPF_Key_Uid
import com.itgo.book_cloud.common.notchlib.NotchScreenManager
import com.itgo.book_cloud.common.setTimeout
import com.itgo.book_cloud.data.model.UserInfo
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.AccountService
import com.itgo.book_cloud.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var navigateTask: Timer? = null
    private val accountService = ServiceFactory(this).create(AccountService::class.java)
    private val notchScreenManager: NotchScreenManager = NotchScreenManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        notchScreenManager.setDisplayInNotch(this)
    }

    override fun onResume() {
        super.onResume()

        val sp = getSharedPreferences(Constant.Account_SPF_Name, Context.MODE_PRIVATE)
        val uid = sp.getLong(Account_SPF_Key_Uid, Account_SPF_DefaultValue_Uid)
        val token = sp.getString(Account_SPF_Key_Token, Account_SPF_DefaultValue_Token)

        val intent = Intent()

        intent.setClass(this, HomeActivity::class.java)

        if (token != Account_SPF_DefaultValue_Token) {
            accountService.getUserInfo(uid).enqueue(object : Callback<UserInfo> {
                override fun onResponse(
                    call: Call<UserInfo>,
                    response: Response<UserInfo>
                ) {
                    val userInfo = response.body()
                    if ( userInfo!= null) {
                        (application as BookCloudApplication).globalStore.setUserInfo(userInfo)
                    } else {
                        Toast.makeText(this@SplashActivity, "网络出错了哦～", Toast.LENGTH_SHORT).show()
                    }
                    navigateToNextActivity(intent)
                }

                override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                    Toast.makeText(this@SplashActivity, "网络出错了哦～", Toast.LENGTH_SHORT).show()
                    navigateToNextActivity(intent)
                }
            })
        } else {
            navigateToNextActivity(intent)
        }
    }

    private fun navigateToNextActivity(intent: Intent) {
        navigateTask?.cancel()
        navigateTask = setTimeout(1000) {
            startActivity(intent)
        }
    }
}