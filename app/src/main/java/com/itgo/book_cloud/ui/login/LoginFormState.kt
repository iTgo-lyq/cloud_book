package com.itgo.book_cloud.ui.login

data class LoginFormState(
    val phoneError: String? = null,
    val captchaError: String? = null,
    val hasSalt: Boolean = false,
    val hasTimeCounter: Boolean = false,
)