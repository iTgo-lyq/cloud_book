package com.itgo.book_cloud.ui.login

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.Flag_User_NoTags_With_Age
import com.itgo.book_cloud.common.afterTextChanged
import com.itgo.book_cloud.common.setTimeout
import com.itgo.book_cloud.data.model.UserInfo
import com.itgo.book_cloud.ui.home.HomeActivity
import com.itgo.book_cloud.ui.interests.InterestsActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var userAgreementDialog: AlertDialog

    private var formHasFadeIn = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        hideForm()

        loginViewModel =
            ViewModelProvider(this, LoginViewModel.Factory(this)).get(LoginViewModel::class.java)

        userAgreementDialog = MaterialAlertDialogBuilder(this)
            .setTitle("用户协议")
            .setMessage(resources.getString(R.string.user_agreement_content))
            .setNeutralButton("取消") { _: DialogInterface, _: Int -> }
            .setPositiveButton("同意协议") { _: DialogInterface, _: Int ->
                signChecker.isChecked = true
            }
            .setOnDismissListener {
                showForm()
                startFadeInAnim()
            }.create()

        loginViewModel.loginFormState.observe(this, Observer {
            phoneTextField.error = it.phoneError
            captchaTextField.error = it.captchaError
            captchaTextField.isEnabled = it.hasSalt
            smsBtn.isEnabled = it.phoneError.isNullOrEmpty() && !it.hasTimeCounter
            loginBtn.isEnabled = it.captchaError.isNullOrEmpty() && it.hasSalt
        })

        loginViewModel.timeCounter.observe(this, Observer {
            smsBtn.text =
                resources.getString(R.string.btn_captcha) + if (it == 0) "" else " ($it)"
        })

        loginViewModel.loginResult.observe(this) { userInfo ->
            if (userInfo != null) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                setTimeout(this, Toast.LENGTH_SHORT.toLong()) {
                    navigateToNextActivity(userInfo)
                }
            }
        }

        loginViewModel.networkError.observe(this, Observer {
            if (!it.isNullOrEmpty())
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        captchaValue.afterTextChanged {
            loginViewModel.captcha = it
        }

        phoneValue.afterTextChanged {
            loginViewModel.phone = it
        }

        smsBtn.setOnClickListener {
            loginViewModel.getCaptcha()
        }

        loginBtn.setOnClickListener {
            if (signChecker.isChecked) loginViewModel.login()
            else Toast.makeText(this, "请先确认用户协议~", Toast.LENGTH_LONG).show()
        }

        backBtn.setOnClickListener {
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!formHasFadeIn) {
            setTimeout(this, 500) {
                userAgreementDialog.show()
            }
        }
    }

    private fun hideForm() {
        phoneTextField.visibility = View.INVISIBLE
        captchaTextField.visibility = View.INVISIBLE
        loginBtn.visibility = View.INVISIBLE
        smsBtn.visibility = View.INVISIBLE
        signChecker.visibility = View.INVISIBLE
    }

    private fun showForm() {
        phoneTextField.visibility = View.VISIBLE
        captchaTextField.visibility = View.VISIBLE
        loginBtn.visibility = View.VISIBLE
        smsBtn.visibility = View.VISIBLE
        signChecker.visibility = View.VISIBLE

    }

    private fun startFadeInAnim() {
        if (formHasFadeIn) return

        val alphaAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_out)
        alphaAnim.startOffset = 1500
        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.translate_alpha_in)
        fadeInAnim.startOffset = 1500
        val fadeInHorAnim = AnimationUtils.loadAnimation(this, R.anim.translate_scale_in)
        bg_bookshelf.startAnimation(alphaAnim)
        phoneTextField.startAnimation(fadeInAnim)
        captchaTextField.startAnimation(fadeInAnim)
        loginBtn.startAnimation(fadeInAnim)
        smsBtn.startAnimation(fadeInAnim)
        signChecker.startAnimation(fadeInHorAnim)

        formHasFadeIn = true
    }

    private fun navigateToNextActivity(userInfo: UserInfo) {
        val intent = Intent()

        (application as BookCloudApplication).globalStore.setUserInfo(userInfo)

        if (userInfo.age == Flag_User_NoTags_With_Age) {
            intent.setClass(this, InterestsActivity::class.java)
        } else {
            intent.setClass(this, HomeActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}