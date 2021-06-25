package com.itgo.book_cloud.ui.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.itgo.book_cloud.common.Constant.Account_Captcha_Length
import com.itgo.book_cloud.common.isPhoneNum
import com.itgo.book_cloud.common.setInterval
import com.itgo.book_cloud.data.model.UserInfo
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(context: Context) : ViewModel() {
    private val accountService = ServiceFactory(context).create(AccountService::class.java)

    private var salt: String = ""

    var phone = ""
        set(value) {
            if (!loginFormState.value?.phoneError.isNullOrEmpty() && isPhoneValid(value))
                loginFormState.value = loginFormState.value?.copy(phoneError = "")
            field = value
        }
    var captcha = ""
        set(value) {
            if (!loginFormState.value?.captchaError.isNullOrEmpty() && isCaptchaValid(value))
                loginFormState.value = loginFormState.value?.copy(captchaError = "")
            field = value
        }

    val timeCounter = MutableLiveData(0)
    val loginFormState = MutableLiveData(LoginFormState())
    val networkError = MutableLiveData("")
    val loginResult = MutableLiveData<UserInfo?>(null)

    fun getCaptcha() {
        if (isPhoneValid(phone)) {
            loginFormState.value = loginFormState.value?.copy(phoneError = null)
            accountService.sendCaptcha(SendCaptchaBody(phone))
                .enqueue(object : Callback<SendCaptchaResult> {
                    override fun onResponse(
                        call: Call<SendCaptchaResult>,
                        response: Response<SendCaptchaResult>
                    ) {
                        salt = response.body()?.salt ?: ""
                        loginFormState.value = loginFormState.value?.copy(hasSalt = salt != "")

                        setTimeCounter()
                    }

                    override fun onFailure(call: Call<SendCaptchaResult>, t: Throwable) {
                        networkError.value = t.message
                    }
                })
        } else {
            loginFormState.value = loginFormState.value?.copy(phoneError = "请输入正确的手机号")
        }
    }

    fun login() {
        if (!isPhoneValid(phone)) loginFormState.value =
            loginFormState.value?.copy(phoneError = "请输入正确的手机号")
        if (!isCaptchaValid(captcha)) loginFormState.value =
            loginFormState.value?.copy(captchaError = "请输入正确的验证码")

        if (!loginFormState.value?.phoneError.isNullOrEmpty() || !loginFormState.value?.captchaError.isNullOrEmpty())
            return

        loginFormState.value = loginFormState.value?.copy(phoneError = null, captchaError = null)

        accountService.login(LoginBody(phone, salt, captcha))
            .enqueue(object : Callback<UserInfo> {
                override fun onResponse(
                    call: Call<UserInfo>,
                    response: Response<UserInfo>
                ) {
                    loginResult.value = response.body()
                }

                override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                    networkError.value = t.message
                }
            })
    }

    private fun setTimeCounter() {
        timeCounter.value = 60
        setInterval(1000) { timer ->
            val value = timeCounter.value
            if (value != null && value != 0) {
                timeCounter.postValue(value - 1)
            } else {
                timer.cancel()
                loginFormState.postValue(loginFormState.value?.copy(hasTimeCounter = false))
            }
        }
        loginFormState.value = loginFormState.value?.copy(hasTimeCounter = true)
    }

    private fun isPhoneValid(phone: String): Boolean {
        return isPhoneNum(phone)
    }

    private fun isCaptchaValid(captcha: String): Boolean {
        return captcha.length == Account_Captcha_Length
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(context) as T
        }
    }
}