package com.itgo.book_cloud

import android.app.Application

class BookCloudApplication : Application() {
    lateinit var globalStore: BookCloudStore

    override fun onCreate() {
        super.onCreate()

        globalStore = BookCloudStore(applicationContext)
    }

    fun quit() {
        globalStore.clearUserInfo()
    }
}